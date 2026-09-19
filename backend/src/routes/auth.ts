import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { rateLimit } from '../middleware/rateLimit.js';
import { getOtpService } from '../services/otp/ProductionOtpService.js';
import { getEmailService } from '../services/email/EmailService.js';
import { db } from '../lib/db.js';

const router = Router();
const otpService = getOtpService();

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

// POST /api/auth/send-otp
router.post('/auth/send-otp', rateLimit(20, 60000, 'auth_send_otp'), async (req: Request, res: Response) => {
  const result = sendOtpSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { email, phone } = result.data;

  // 1. Email OTP handling (Direct 6-Digit Numeric Code via Resend/Email Service)
  if (email) {
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    await (otpService as any).sendOtp(email, otp);

    console.log(`\n========================================`);
    console.log(`[KIDSG][EMAIL][OTP] Target email: ${email}`);
    console.log(`[KIDSG][EMAIL][OTP] Generated 6-Digit CODE: ${otp}`);
    console.log(`========================================\n`);

    // Dispatch 6-digit numeric verification code via Email Service
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
    }, `Verification code sent to ${email} (OTP Code: ${otp})`);
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

  // Verify exact 6-digit numeric code against our active OTP store
  const verifyRes = await otpService.verifyOtp(contact, otp);

  if (!verifyRes.success) {
    sendError(res, verifyRes.message || 'Invalid or expired verification code', 'INVALID_OTP', 400);
    return;
  }

  const userId = email ? `user_${email.replace(/[^a-zA-Z0-9]/g, '_')}` : `user_${phone?.replace(/[^a-zA-Z0-9]/g, '_')}`;
  const profile = db.getProfile(userId);
  const token = `kidsg-jwt-${userId}`;

  sendSuccess(res, {
    verified: true,
    token,
    user: {
      id: userId,
      phone: profile.phone || (phone ?? undefined),
      email: profile.email || (email ?? undefined),
      role: profile.role || 'CUSTOMER',
      firstName: profile.firstName || 'Student',
      lastName: profile.lastName || '',
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
