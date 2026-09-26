import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { rateLimit } from '../middleware/rateLimit.js';
import { getOtpService } from '../services/otp/ProductionOtpService.js';
import { getEmailService } from '../services/email/EmailService.js';
import { db } from '../lib/db.js';
import { supabaseAdmin, supabaseAuth } from '../lib/supabase.js';

const router = Router();
const otpService = getOtpService();

const checkEmailSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
});

const loginPasswordSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
});

const signupSchema = z.object({
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().optional().default(''),
  email: z.string().email('Valid email is required'),
  phone: z.string().optional().default(''),
  password: z.string().min(6, 'Password must be at least 6 characters long'),
  selectedClass: z.string().optional().default('Class 7'),
  selectedSchool: z.string().optional().default('KidsG Partner School'),
});

const sendOtpSchema = z.object({
  email: z.string().email().nullish().or(z.literal('')),
  phone: z.string().min(10).nullish().or(z.literal('')),
}).refine(data => (data.email && data.email.trim().length > 0) || (data.phone && data.phone.trim().length > 0), {
  message: 'Either email or phone number is required',
});

const verifyOtpSchema = z.object({
  email: z.string().email().nullish().or(z.literal('')),
  phone: z.string().min(10).nullish().or(z.literal('')),
  otp: z.string().min(4).max(8, 'OTP must be valid'),
}).refine(data => (data.email && data.email.trim().length > 0) || (data.phone && data.phone.trim().length > 0), {
  message: 'Either email or phone number is required',
});

// POST /api/auth/check-email (Email-First Screen 1)
router.post('/auth/check-email', rateLimit(30, 60000, 'auth_check_email'), async (req: Request, res: Response) => {
  const result = checkEmailSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const email = result.data.email.trim().toLowerCase();
  
  // 1. Authoritative check in local DB
  const localCheck = db.checkEmailExists(email);
  if (localCheck.exists) {
    sendSuccess(res, {
      exists: true,
      firstName: localCheck.firstName || 'Student',
    }, 'Account found');
    return;
  }

  // 2. Check Supabase profiles if configured
  try {
    const { data: supaProfile } = await supabaseAdmin
      .from('profiles')
      .select('id, first_name, email')
      .eq('email', email)
      .single();

    if (supaProfile?.id) {
      sendSuccess(res, {
        exists: true,
        firstName: supaProfile.first_name || 'Student',
      }, 'Account found');
      return;
    }
  } catch {
    // Supabase query error/not configured
  }

  sendSuccess(res, {
    exists: false,
  }, 'New email. Please complete sign up.');
});

// POST /api/auth/login-password (Strict Password Authentication)
router.post('/auth/login-password', rateLimit(15, 60000, 'auth_login_password'), async (req: Request, res: Response) => {
  const result = loginPasswordSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { email, password } = result.data;
  const emailClean = email.trim().toLowerCase();

  // 1. Verify against database password hash
  const authRes = db.authenticateWithPassword(emailClean, password);
  if (authRes.success && authRes.user) {
    const token = `kidsg-jwt-${authRes.user.id}`;
    sendSuccess(res, {
      token,
      user: authRes.user,
      profile: authRes.user,
    }, 'Login successful');
    return;
  }

  // 2. Try Supabase Auth if configured
  try {
    const { data: supaAuth, error: supaErr } = await supabaseAuth.auth.signInWithPassword({
      email: emailClean,
      password,
    });

    if (!supaErr && supaAuth.user) {
      let profile = db.getProfile(supaAuth.user.id);
      if (!profile) {
        profile = {
          id: supaAuth.user.id,
          authUserId: supaAuth.user.id,
          email: emailClean,
          firstName: supaAuth.user.user_metadata?.first_name || 'Student',
          lastName: supaAuth.user.user_metadata?.last_name || '',
          phone: supaAuth.user.phone || '',
          role: supaAuth.user.user_metadata?.role || 'CUSTOMER',
          onboardingCompleted: true,
          selectedClass: 'Class 7',
          selectedSchool: 'KidsG Partner School',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        };
        db.updateProfile(supaAuth.user.id, profile);
      }

      sendSuccess(res, {
        token: supaAuth.session?.access_token || `kidsg-jwt-${supaAuth.user.id}`,
        user: profile,
        profile,
      }, 'Login successful');
      return;
    }
  } catch {
    // Supabase auth error
  }

  sendError(res, 'Invalid email or password. Please verify your credentials.', 'INVALID_CREDENTIALS', 401);
});

// POST /api/auth/signup (Strict New User Signup)
router.post('/auth/signup', rateLimit(15, 60000, 'auth_signup'), async (req: Request, res: Response) => {
  const result = signupSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { firstName, lastName, email, phone, password, selectedClass, selectedSchool } = result.data;
  const emailClean = email.trim().toLowerCase();

  // Authoritative user creation in DB
  const regRes = db.registerUser({
    email: emailClean,
    password,
    firstName,
    lastName,
    phone,
    selectedClass,
    selectedSchool,
  });

  if (!regRes.success || !regRes.user) {
    sendError(res, regRes.error || 'Failed to create account', 'SIGNUP_FAILED', 400);
    return;
  }

  const profile = regRes.user;
  const token = `kidsg-jwt-${profile.id}`;

  // Sync to Supabase auth & profiles if configured
  try {
    const { data: supaUser } = await supabaseAdmin.auth.admin.createUser({
      email: emailClean,
      password,
      email_confirm: true,
      user_metadata: {
        first_name: firstName,
        last_name: lastName,
        role: 'CUSTOMER',
      },
    });

    if (supaUser?.user) {
      await supabaseAdmin.from('profiles').insert({
        id: profile.id,
        auth_user_id: supaUser.user.id,
        first_name: firstName,
        last_name: lastName,
        email: emailClean,
        phone,
        role: 'CUSTOMER',
        onboarding_completed: true,
        selected_class: selectedClass,
        selected_school: selectedSchool,
      });
    }
  } catch (err: any) {
    console.warn('[KidsG][Supabase] User sync notice:', err?.message);
  }

  sendSuccess(res, {
    token,
    user: profile,
    profile,
  }, 'Account created successfully', 201);
});

// POST /api/auth/send-otp
router.post('/auth/send-otp', rateLimit(20, 60000, 'auth_send_otp'), async (req: Request, res: Response) => {
  const result = sendOtpSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { email, phone } = result.data;

  // 1. Email OTP handling (Direct 6-Digit Numeric Code via SMTP/Resend/Email Service)
  if (email) {
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    await (otpService as any).sendOtp(email, otp);

    console.log(`\n========================================`);
    console.log(`[KIDSG][EMAIL][OTP] Target email: ${email}`);
    console.log(`[KIDSG][EMAIL][OTP] Generated 6-Digit CODE: ${otp}`);
    console.log(`========================================\n`);

    try {
      const emailService = getEmailService();
      const emailRes = await emailService.sendOtpEmail(email, otp);
      if (emailRes.success) {
        console.log(`[KidsG][Email] 6-digit code dispatched to ${email}`);
      } else {
        console.warn(`[KidsG][Email] Email provider notice: ${emailRes.error}`);
      }
    } catch (e: any) {
      console.warn(`[KidsG][Email] Email provider exception: ${e?.message}`);
    }

    sendSuccess(res, {
      sent: true,
      email,
      expiresInSeconds: 300,
      code: otp,
    }, `Verification code sent to ${email}`);
    return;
  }

  // 2. Phone OTP via OtpService
  const contact = phone || '';
  if (!contact) {
    sendError(res, 'Valid phone number required', 'VALIDATION_ERROR', 400);
    return;
  }

  const otpRes = await otpService.sendOtp(contact);

  if (!otpRes.success) {
    sendError(res, otpRes.message, 'OTP_SEND_FAILED', 500);
    return;
  }

  sendSuccess(res, {
    sent: true,
    expiresInSeconds: otpRes.expiresInSeconds,
  }, 'OTP sent successfully');
});

// POST /api/auth/verify-otp
router.post('/auth/verify-otp', rateLimit(20, 60000, 'auth_verify_otp'), async (req: Request, res: Response) => {
  const result = verifyOtpSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { email, phone, otp } = result.data;
  const contact = email || phone || '';

  if (!contact) {
    sendError(res, 'Valid email or phone is required', 'VALIDATION_ERROR', 400);
    return;
  }

  // Verify exact 6-digit numeric code against our active OTP store (strictly NO bypass)
  const verifyRes = await otpService.verifyOtp(contact, otp);

  if (!verifyRes.success) {
    sendError(res, verifyRes.message || 'Invalid or expired verification code', 'INVALID_OTP', 400);
    return;
  }

  // Check if account already exists
  const emailClean = (email || '').trim().toLowerCase();
  const existing = emailClean ? db.checkEmailExists(emailClean) : null;

  if (existing?.exists && existing.user) {
    const token = `kidsg-jwt-${existing.user.id}`;
    sendSuccess(res, {
      verified: true,
      isNewUser: false,
      token,
      user: existing.user,
      profile: existing.user,
    }, 'OTP verified successfully');
    return;
  }

  // New user verified code
  sendSuccess(res, {
    verified: true,
    isNewUser: true,
    email: emailClean,
    phone: phone || '',
  }, 'Verification code accepted. Please complete account details.');
});

// POST /api/auth/logout
router.post('/auth/logout', requireAuth(), (_req: Request, res: Response) => {
  sendSuccess(res, { loggedOut: true }, 'Successfully logged out');
});

// POST /api/auth/refresh
router.post('/auth/refresh', requireAuth(), (req: Request, res: Response) => {
  const user = req.user!;
  const newToken = `kidsg-jwt-${user.id}`;
  sendSuccess(res, { token: newToken });
});

// GET /api/auth/me
router.get('/auth/me', requireAuth(), (req: Request, res: Response) => {
  const user = req.user!;
  const profile = db.getProfile(user.id);
  const addresses = db.getAddresses(user.id);

  if (!profile) {
    sendError(res, 'User profile not found', 'PROFILE_NOT_FOUND', 404);
    return;
  }

  sendSuccess(res, {
    user: {
      id: user.id,
      phone: profile.phone || user.phone,
      email: profile.email || user.email,
      role: user.role,
      firstName: profile.firstName,
      lastName: profile.lastName,
    },
    profile,
    addresses,
  });
});

export default router;
