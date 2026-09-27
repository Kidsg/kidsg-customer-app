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
      verified: true,
      token,
      user: authRes.user,
      profile: authRes.user,
    }, 'Login successful');
    return;
  }

  // 2. Try Supabase Auth
  try {
    const { data: supaAuth, error: supaErr } = await supabaseAdmin.auth.signInWithPassword({
      email: emailClean,
      password,
    });

    if (!supaErr && supaAuth.user) {
      // Authoritatively fetch saved student profile from Supabase profiles table
      const { data: supaProfile } = await supabaseAdmin
        .from('profiles')
        .select('*')
        .eq('email', emailClean)
        .maybeSingle();

      const profile = {
        id: supaProfile?.id || supaAuth.user.id,
        authUserId: supaAuth.user.id,
        email: emailClean,
        firstName: supaProfile?.first_name || supaAuth.user.user_metadata?.first_name || 'Student',
        lastName: supaProfile?.last_name || supaAuth.user.user_metadata?.last_name || '',
        phone: supaProfile?.phone || supaAuth.user.phone || '',
        role: supaProfile?.role || supaAuth.user.user_metadata?.role || 'CUSTOMER',
        onboardingCompleted: supaProfile?.onboarding_completed ?? true,
        selectedClass: supaProfile?.selected_class || 'Class 1',
        selectedSchool: supaProfile?.selected_school || 'KidsG Partner School',
        createdAt: supaProfile?.created_at || new Date().toISOString(),
        updatedAt: supaProfile?.updated_at || new Date().toISOString(),
      };
      db.updateProfile(profile.id, profile as any);

      sendSuccess(res, {
        verified: true,
        token: supaAuth.session?.access_token || `kidsg-jwt-${profile.id}`,
        user: profile,
        profile,
      }, 'Login successful');
      return;
    }
  } catch (err: any) {
    console.warn('[KidsG][Supabase] signInWithPassword error:', err?.message);
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
    let authUserId: string | undefined = undefined;
    const { data: supaUser, error: authErr } = await supabaseAdmin.auth.admin.createUser({
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
      authUserId = supaUser.user.id;
    } else if (authErr) {
      console.warn('[KidsG][Supabase] createUser note:', authErr.message);
      try {
        const { data: listData } = await supabaseAdmin.auth.admin.listUsers({ page: 1, perPage: 1000 });
        const existingAuth = listData?.users?.find(u => u.email === emailClean);
        if (existingAuth) {
          authUserId = existingAuth.id;
          await supabaseAdmin.auth.admin.updateUserById(existingAuth.id, {
            password,
            email_confirm: true,
            user_metadata: {
              first_name: firstName,
              last_name: lastName,
              role: 'CUSTOMER',
            }
          });
        }
      } catch (_e) {}
    }

    const { data: upsertedProf, error: insertErr } = await supabaseAdmin.from('profiles').upsert({
      ...(authUserId ? { auth_user_id: authUserId } : {}),
      first_name: firstName,
      last_name: lastName,
      email: emailClean,
      phone: phone || null,
      role: 'CUSTOMER',
      onboarding_completed: true,
      selected_class: selectedClass || null,
      selected_school: selectedSchool || null,
      updated_at: new Date().toISOString(),
    }, { onConflict: 'email' }).select('id').maybeSingle();

    if (insertErr) {
      console.error('[KidsG][Supabase] Profile upsert error:', insertErr);
    } else if (upsertedProf?.id) {
      profile.id = upsertedProf.id;
      db.updateProfile(upsertedProf.id, profile);
    }
  } catch (err: any) {
    console.warn('[KidsG][Supabase] User sync notice:', err?.message);
  }

  sendSuccess(res, {
    verified: true,
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

  const emailClean = (email || '').trim().toLowerCase();

  // 1. Authoritative check in local DB
  const localExisting = emailClean ? db.checkEmailExists(emailClean) : null;
  if (localExisting?.exists && localExisting.user) {
    const token = `kidsg-jwt-${localExisting.user.id}`;
    sendSuccess(res, {
      verified: true,
      isNewUser: false,
      token,
      user: localExisting.user,
      profile: localExisting.user,
    }, 'OTP verified successfully');
    return;
  }

  // 2. Authoritative check against Supabase profiles table
  if (emailClean) {
    try {
      const { data: supaProf } = await supabaseAdmin
        .from('profiles')
        .select('*')
        .eq('email', emailClean)
        .maybeSingle();

      if (supaProf?.id) {
        const userObj = {
          id: supaProf.id,
          authUserId: supaProf.auth_user_id || supaProf.id,
          email: supaProf.email,
          firstName: supaProf.first_name || 'Student',
          lastName: supaProf.last_name || '',
          phone: supaProf.phone || phone || '',
          role: supaProf.role || 'CUSTOMER',
          onboardingCompleted: supaProf.onboarding_completed ?? true,
          selectedClass: supaProf.selected_class || 'Class 1',
          selectedSchool: supaProf.selected_school || 'KidsG Partner School',
          createdAt: supaProf.created_at || new Date().toISOString(),
          updatedAt: supaProf.updated_at || new Date().toISOString(),
        };

        db.updateProfile(supaProf.id, userObj as any);

        const token = `kidsg-jwt-${supaProf.id}`;
        sendSuccess(res, {
          verified: true,
          isNewUser: false,
          token,
          user: userObj,
          profile: userObj,
        }, 'OTP verified successfully');
        return;
      }
    } catch (e: any) {
      console.warn('[KidsG][Supabase] Profile lookup during verify-otp notice:', e?.message);
    }
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
router.get('/auth/me', requireAuth(), async (req: Request, res: Response) => {
  const user = req.user!;
  let profile = db.getProfile(user.id);

  if (!profile) {
    try {
      const { data: supaProf } = await supabaseAdmin
        .from('profiles')
        .select('*')
        .or(`id.eq.${user.id},email.eq.${user.email}`)
        .maybeSingle();

      if (supaProf?.id) {
        profile = {
          id: supaProf.id,
          authUserId: supaProf.auth_user_id || supaProf.id,
          email: supaProf.email,
          firstName: supaProf.first_name || 'Student',
          lastName: supaProf.last_name || '',
          phone: supaProf.phone || '',
          role: supaProf.role || 'CUSTOMER',
          onboardingCompleted: supaProf.onboarding_completed ?? true,
          selectedClass: supaProf.selected_class || 'Class 1',
          selectedSchool: supaProf.selected_school || 'KidsG Partner School',
          createdAt: supaProf.created_at || new Date().toISOString(),
          updatedAt: supaProf.updated_at || new Date().toISOString(),
        };
        db.updateProfile(user.id, profile as any);
      }
    } catch (_e) {}
  }

  if (!profile) {
    sendError(res, 'User profile not found', 'PROFILE_NOT_FOUND', 404);
    return;
  }

  const addresses = db.getAddresses(user.id);

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
