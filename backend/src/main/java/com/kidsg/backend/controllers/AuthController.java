package com.kidsg.backend.controllers;

import com.kidsg.backend.models.ERole;
import com.kidsg.backend.models.RefreshToken;
import com.kidsg.backend.models.Role;
import com.kidsg.backend.models.User;
import com.kidsg.backend.payload.request.ForgotPasswordRequest;
import com.kidsg.backend.payload.request.LoginRequest;
import com.kidsg.backend.payload.request.ProfileRegisterRequest;
import com.kidsg.backend.payload.request.ResetPasswordRequest;
import com.kidsg.backend.payload.request.SendOtpRequest;
import com.kidsg.backend.payload.request.SignupRequest;
import com.kidsg.backend.payload.request.TokenRefreshRequest;
import com.kidsg.backend.payload.request.VerifyOtpRequest;
import com.kidsg.backend.payload.response.JwtResponse;
import com.kidsg.backend.payload.response.MessageResponse;
import com.kidsg.backend.payload.response.TokenRefreshResponse;
import com.kidsg.backend.repository.RoleRepository;
import com.kidsg.backend.repository.UserRepository;
import com.kidsg.backend.security.jwt.JwtUtils;
import com.kidsg.backend.security.services.OtpService;
import com.kidsg.backend.security.services.RefreshTokenService;
import com.kidsg.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already registered! Please login instead."));
        }

        otpService.generateAndSendOtp(request.getEmail(), request.getMobileNumber());
        return ResponseEntity.ok(new MessageResponse("OTP sent successfully to " + request.getEmail()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid or expired OTP!"));
        }

        return ResponseEntity.ok(new MessageResponse("OTP verified successfully! You may now proceed to profile registration."));
    }

    @PostMapping("/register-profile")
    public ResponseEntity<?> registerProfile(@Valid @RequestBody ProfileRegisterRequest request) {
        // 1. Validate that password and confirm password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Password and Confirm Password do not match!"));
        }

        // 2. Check if email already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already registered!"));
        }

        // 3. Ensure email has been verified via OTP
        if (!otpService.isEmailVerified(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email has not been verified with OTP. Please verify OTP first!"));
        }

        // 4. Create new user with profile fields
        User user = new User(request.getEmail(), encoder.encode(request.getPassword()));
        user.setKidName(request.getKidName());
        user.setAge(request.getAge());
        user.setGrade(request.getGrade());
        user.setParentName(request.getParentName());
        user.setMobileNumber(request.getMobileNumber());

        Role userRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        // Clear OTP after successful registration
        otpService.clearOtpForEmail(request.getEmail());

        // 5. Automatically log the user in and return JWT session
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roleNames = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        String jwt = jwtUtils.generateJwtToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getEmail(), roleNames));
    }

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

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getEmail(), roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role defaultRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(defaultRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getEmail());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            refreshTokenService.deleteByUserId(userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Log out successful!"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Error: You must provide a valid Bearer token in the Authorization header to log out."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No account found with email " + request.getEmail()));
        }

        otpService.generateAndSendOtp(request.getEmail(), null);
        return ResponseEntity.ok(new MessageResponse("Password reset OTP sent successfully to " + request.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // 1. Verify passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: New password and Confirm password do not match!"));
        }

        // 2. Verify OTP
        boolean isValidOtp = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValidOtp) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid or expired OTP!"));
        }

        // 3. Find user and update password
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: User not found!"));

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 4. Invalidate sessions and clear OTP
        refreshTokenService.deleteByUserId(user.getId());
        otpService.clearOtpForEmail(request.getEmail());

        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully! You can now log in with your new password."));
    }
}
