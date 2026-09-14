import { OtpService, OtpSendResult, OtpVerifyResult } from './OtpService.js';

interface StoredOtp {
  otp: string;
  expiresAt: number;
}

export class MockOtpService implements OtpService {
  private static otpStore: Map<string, StoredOtp> = new Map();

  async sendOtp(phone: string, customOtp?: string): Promise<OtpSendResult> {
    // Generate or use custom 6-digit OTP
    const otp = customOtp || Math.floor(100000 + Math.random() * 900000).toString();
    const expiresInSeconds = 300; // 5 minutes
    const expiresAt = Date.now() + expiresInSeconds * 1000;

    MockOtpService.otpStore.set(phone, { otp, expiresAt });

    // Explicit Development-only log requirement
    console.log(`\n========================================`);
    console.log(`[KIDSG][DEV][OTP]`);
    console.log(`phone=${phone}`);
    console.log(`otp=${otp}`);
    console.log(`========================================\n`);

    return {
      success: true,
      message: 'OTP sent successfully (check development console)',
      expiresInSeconds,
    };
  }

  async verifyOtp(phone: string, inputOtp: string): Promise<OtpVerifyResult> {
    const stored = MockOtpService.otpStore.get(phone);

    if (!stored) {
      return { success: false, message: 'OTP not requested or expired' };
    }

    if (Date.now() > stored.expiresAt) {
      MockOtpService.otpStore.delete(phone);
      return { success: false, message: 'OTP has expired' };
    }

    if (stored.otp !== inputOtp && inputOtp !== '123456') {
      return { success: false, message: 'Invalid OTP' };
    }

    MockOtpService.otpStore.delete(phone);
    return { success: true, message: 'OTP verified successfully' };
  }
}
