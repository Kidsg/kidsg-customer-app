import { OtpService, OtpSendResult, OtpVerifyResult } from './OtpService.js';

interface StoredOtp {
  otp: string;
  expiresAt: number;
  attempts: number;
}

/**
 * Standalone In-Memory OTP Service for fast, reliable verification of email and phone codes.
 * Independent of third-party SMS/Auth gateways, with expiration and attempt limiting.
 */
export class MockOtpService implements OtpService {
  private static otpStore: Map<string, StoredOtp> = new Map();

  async sendOtp(contact: string, customOtp?: string): Promise<OtpSendResult> {
    const otp = customOtp || Math.floor(100000 + Math.random() * 900000).toString();
    const expiresInSeconds = 300; // 5 minutes
    const expiresAt = Date.now() + expiresInSeconds * 1000;

    MockOtpService.otpStore.set(contact.toLowerCase().trim(), {
      otp,
      expiresAt,
      attempts: 0,
    });

    console.log(`\n========================================`);
    console.log(`[KIDSG][AUTH][OTP]`);
    console.log(`Contact: ${contact}`);
    console.log(`Verification Code: ${otp}`);
    console.log(`Expires in: 5 minutes`);
    console.log(`========================================\n`);

    return {
      success: true,
      message: 'OTP generated and sent',
      expiresInSeconds,
    };
  }

  async verifyOtp(contact: string, inputOtp: string): Promise<OtpVerifyResult> {
    const key = contact.toLowerCase().trim();
    const stored = MockOtpService.otpStore.get(key);

    if (!stored) {
      return { success: false, message: 'OTP not requested or expired' };
    }

    if (Date.now() > stored.expiresAt) {
      MockOtpService.otpStore.delete(key);
      return { success: false, message: 'OTP has expired. Please request a new code.' };
    }

    stored.attempts += 1;
    if (stored.attempts > 5) {
      MockOtpService.otpStore.delete(key);
      return { success: false, message: 'Too many invalid attempts. Please request a new code.' };
    }

    // Allow standard testing/development bypass '123456' in non-production mode
    const isDevBypass = process.env.NODE_ENV !== 'production' && inputOtp.trim() === '123456';

    if (stored.otp !== inputOtp.trim() && !isDevBypass) {
      return { success: false, message: 'Invalid verification code' };
    }

    MockOtpService.otpStore.delete(key);
    return { success: true, message: 'OTP verified successfully' };
  }
}
