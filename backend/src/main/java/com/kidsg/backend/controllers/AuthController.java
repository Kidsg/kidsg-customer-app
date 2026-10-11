package com.kidsg.backend.controllers;

import com.kidsg.backend.models.ERole;
import com.kidsg.backend.models.RefreshToken;
import com.kidsg.backend.models.Role;
import com.kidsg.backend.models.User;
import com.kidsg.backend.payload.request.*;
import com.kidsg.backend.payload.response.*;
import com.kidsg.backend.repository.RoleRepository;
import com.kidsg.backend.repository.UserRepository;
import com.kidsg.backend.security.jwt.JwtUtils;
import com.kidsg.backend.security.services.OtpService;
import com.kidsg.backend.security.services.RefreshTokenService;
import com.kidsg.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    OtpService otpService;

    // Helper to map User -> UserDto
    private UserDto mapToUserDto(User user) {
        String roleStr = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name().replace("ROLE_", ""))
                .orElse("CUSTOMER");
        return new UserDto(
                String.valueOf(user.getId()),
                user.getMobileNumber(),
                user.getEmail(),
                roleStr,
                user.getFirstName(),
                user.getLastName()
        );
    }

    // Helper to map User -> ProfileDto
    private ProfileDto mapToProfileDto(User user) {
        String roleStr = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name().replace("ROLE_", ""))
                .orElse("CUSTOMER");
        return new ProfileDto(
                String.valueOf(user.getId()),
                String.valueOf(user.getId()),
                user.getFirstName(),
                user.getLastName(),
                user.getMobileNumber(),
                user.getEmail(),
                roleStr,
                true,
                user.getSelectedClass(),
                user.getSelectedSchool(),
                null
        );
    }

    /**
     * Mobile & Web: Check if email exists
     */
    @PostMapping("/check-email")
    public ResponseEntity<ApiResponse<CheckEmailResponse>> checkEmail(@Valid @RequestBody CheckEmailRequest request) {
        String email = request.getEmail().trim();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return ResponseEntity.ok(ApiResponse.success(new CheckEmailResponse(true, user.getFirstName()), "Email registered"));
        } else {
            return ResponseEntity.ok(ApiResponse.success(new CheckEmailResponse(false, null), "Email not registered"));
        }
    }

    /**
     * Mobile: Login with email and password
     */
    @PostMapping("/login-password")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> loginPassword(@Valid @RequestBody LoginPasswordRequest request) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail().trim(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.getEmail().trim())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDto userDto = mapToUserDto(user);
            ProfileDto profileDto = mapToProfileDto(user);

            VerifyOtpResponse responseData = new VerifyOtpResponse(true, jwt, userDto, profileDto, false);
            return ResponseEntity.ok(ApiResponse.success(responseData, "Login successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password", "AUTH_INVALID_CREDENTIALS"));
        }
    }

    /**
     * Mobile & Web: Send OTP (supports email and phone)
     * Dispatches OTP email via Gmail SMTP using Google App Password.
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<SendOtpResponse>> sendOtp(@RequestBody SendOtpRequest request) {
        String contact = request.getTargetContact();
        if (contact == null || contact.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email or phone number is required", "INVALID_CONTACT"));
        }

        String email = contact.contains("@") ? contact : null;
        String phone = !contact.contains("@") ? contact : null;

        otpService.generateAndSendOtp(email, phone);
        return ResponseEntity.ok(ApiResponse.success(new SendOtpResponse(true, 300), "Verification code sent to " + contact));
    }

    /**
     * Mobile & Web: Verify OTP
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        String contact = request.getTargetContact();
        if (contact == null || contact.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Contact is required", "INVALID_CONTACT"));
        }

        boolean isValid = otpService.verifyOtp(contact, request.getOtp());
        if (!isValid) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid or expired OTP code", "INVALID_OTP"));
        }

        String email = contact.contains("@") ? contact : contact + "@kidsg.in";
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String jwt = jwtUtils.generateTokenFromUsername(user.getEmail());
            UserDto userDto = mapToUserDto(user);
            ProfileDto profileDto = mapToProfileDto(user);
            return ResponseEntity.ok(ApiResponse.success(new VerifyOtpResponse(true, jwt, userDto, profileDto, false), "Verified successfully"));
        } else {
            // New user registration flow
            User newUser = new User(email, encoder.encode(UUID.randomUUID().toString()));
            newUser.setMobileNumber(!contact.contains("@") ? contact : "");
            Role customerRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                    .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_CUSTOMER)));
            Set<Role> roles = new HashSet<>();
            roles.add(customerRole);
            newUser.setRoles(roles);
            userRepository.save(newUser);

            String jwt = jwtUtils.generateTokenFromUsername(newUser.getEmail());
            UserDto userDto = mapToUserDto(newUser);
            ProfileDto profileDto = mapToProfileDto(newUser);
            return ResponseEntity.ok(ApiResponse.success(new VerifyOtpResponse(true, jwt, userDto, profileDto, true), "Account verified successfully"));
        }
    }

    /**
     * Mobile & Web: Signup endpoint
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail().trim())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email is already registered! Please login.", "EMAIL_ALREADY_EXISTS"));
        }

        User user = new User(signUpRequest.getEmail().trim(), encoder.encode(signUpRequest.getPassword()));
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setMobileNumber(signUpRequest.getPhone());
        user.setSelectedClass(signUpRequest.getSelectedClass());
        user.setSelectedSchool(signUpRequest.getSelectedSchool());
        user.setKidName(signUpRequest.getFirstName());

        Role customerRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_CUSTOMER)));
        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);
        user.setRoles(roles);

        userRepository.save(user);

        // Auto authenticate
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(signUpRequest.getEmail().trim(), signUpRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDto userDto = mapToUserDto(user);
        ProfileDto profileDto = mapToProfileDto(user);

        return ResponseEntity.ok(ApiResponse.success(new VerifyOtpResponse(true, jwt, userDto, profileDto, true), "User registered successfully!"));
    }

    /**
     * Mobile: Get current authenticated user profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized", "UNAUTHORIZED"));
        }

        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDto userDto = mapToUserDto(user);
        ProfileDto profileDto = mapToProfileDto(user);

        return ResponseEntity.ok(ApiResponse.success(new MeResponse(userDto, profileDto), "Profile retrieved successfully"));
    }

    /**
     * Web session login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        String jwt = jwtUtils.generateJwtToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getEmail(), roles), "Login successful"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getEmail());
                    return ResponseEntity.ok(ApiResponse.success(new TokenRefreshResponse(token, requestRefreshToken), "Token refreshed"));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            refreshTokenService.deleteByUserId(userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Log out successful!"));
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Log out successful!"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No account found with email " + request.getEmail(), "NOT_FOUND"));
        }

        otpService.generateAndSendOtp(request.getEmail(), null);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset OTP sent successfully to " + request.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("New password and Confirm password do not match!", "MISMATCH"));
        }

        boolean isValidOtp = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValidOtp) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid or expired OTP!", "INVALID_OTP"));
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: User not found!"));

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenService.deleteByUserId(user.getId());
        otpService.clearOtpForEmail(request.getEmail());

        return ResponseEntity.ok(ApiResponse.success(null, "Password has been reset successfully!"));
    }
}
