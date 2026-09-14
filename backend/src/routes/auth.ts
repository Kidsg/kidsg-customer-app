import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { rateLimit } from '../middleware/rateLimit.js';
import { getOtpService } from '../services/otp/ProductionOtpService.js';
import { db } from '../lib/db.js';

const router = Router();
const otpService = getOtpService();

const signupSchema = z.object({
  phone: z.string().min(10, 'Valid phone number required'),
  firstName: z.string().min(2, 'First name is required'),
  lastName: z.string().optional(),
  email: z.string().email().optional(),
  role: z.enum(['CUSTOMER', 'ADMIN', 'PARTNER', 'DELIVERY_PARTNER']).default('CUSTOMER'),
});

const loginSchema = z.object({
  phone: z.string().min(10),
  password: z.string().optional(),
});

import { supabaseAuth } from '../lib/supabase.js';
import { env } from '../config/env.js';

const sendOtpSchema = z.object({
  email: z.string().email().optional(),
  phone: z.string().min(10).optional(),
}).refine(data => data.email || data.phone, {
  message: 'Either email or phone number is required',
});

const verifyOtpSchema = z.object({
  email: z.string().email().optional(),
  phone: z.string().min(10).optional(),
  otp: z.string().min(6).max(8, 'OTP must be valid'),
}).refine(data => data.email || data.phone, {
  message: 'Either email or phone number is required',
});

// POST /api/auth/send-otp
router.post('/auth/send-otp', rateLimit(5, 60000, 'auth_send_otp'), async (req: Request, res: Response) => {
  const result = sendOtpSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { email, phone } = result.data;

  // 1. Email OTP via Supabase Auth
  if (email && env.OTP_PROVIDER === 'supabase') {
    try {
      const { error } = await supabaseAuth.auth.signInWithOtp({
        email,
        options: { shouldCreateUser: true },
      });

      if (error) {
        sendError(res, error.message, 'OTP_SEND_FAILED', 400);
        return;
      }

      sendSuccess(res, {
        sent: true,
        email,
        expiresInSeconds: 300,
      }, 'Verification code sent to your email');
      return;
    } catch (err: any) {
      sendError(res, 'Failed to send email verification code', 'OTP_SEND_FAILED', 500);
      return;
    }
  }

  // 2. Phone OTP via OtpService (or dev mock)
  const contact = phone || email || '+919876543210';
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
router.post('/auth/verify-otp', rateLimit(10, 60000, 'auth_verify_otp'), async (req: Request, res: Response) => {
  const result = verifyOtpSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { email, phone, otp } = result.data;

  // 1. Email verification via Supabase Auth
  if (email && env.OTP_PROVIDER === 'supabase') {
    try {
      const { data, error } = await supabaseAuth.auth.verifyOtp({
        email,
        token: otp,
        type: 'email',
      });

      if (error || !data.user) {
        sendError(res, error?.message || 'Invalid or expired OTP', 'INVALID_OTP', 400);
        return;
      }

      const userId = data.user.id;
      const profile = db.getProfile(userId);
      const token = data.session?.access_token || `dev-token-${userId}`;

      sendSuccess(res, {
        verified: true,
        token,
        user: {
          id: userId,
          email: data.user.email,
          phone: data.user.phone,
          role: profile.role || 'CUSTOMER',
          firstName: profile.firstName,
          lastName: profile.lastName,
        },
        profile,
      }, 'Email OTP verified successfully');
      return;
    } catch (err: any) {
      sendError(res, 'Verification failed', 'VERIFICATION_ERROR', 500);
      return;
    }
  }

  // 2. Phone / Fallback verification
  const contact = phone || email || '+919876543210';
  const verifyRes = await otpService.verifyOtp(contact, otp);

  if (!verifyRes.success) {
    sendError(res, verifyRes.message, 'INVALID_OTP', 400);
    return;
  }

  const userId = 'user_dev_default';
  const profile = db.getProfile(userId);
  const token = `dev-token-${userId}`;

  sendSuccess(res, {
    verified: true,
    token,
    user: {
      id: userId,
      phone: profile.phone,
      email: profile.email,
      role: profile.role,
      firstName: profile.firstName,
      lastName: profile.lastName,
    },
    profile,
  }, 'OTP verified successfully');
});

// POST /api/auth/logout
router.post('/auth/logout', requireAuth(), (_req: Request, res: Response) => {
  sendSuccess(res, { loggedOut: true }, 'Successfully logged out');
});

// POST /api/auth/refresh
router.post('/auth/refresh', requireAuth(), (req: Request, res: Response) => {
  const user = req.user!;
  const newToken = `dev-token-${user.id}`;
  sendSuccess(res, { token: newToken });
});

// GET /api/auth/me
router.get('/auth/me', requireAuth(), (req: Request, res: Response) => {
  const user = req.user!;
  const profile = db.getProfile(user.id);
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
