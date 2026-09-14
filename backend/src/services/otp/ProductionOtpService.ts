import { OtpService, OtpSendResult, OtpVerifyResult } from './OtpService.js';
import { env } from '../../config/env.js';
import { MockOtpService } from './MockOtpService.js';

export class ProductionOtpService implements OtpService {
  private apiUrl: string;
  private apiKey: string;

  constructor() {
    this.apiUrl = env.OTP_API_URL || 'https://api.msg91.com/api/v5';
    this.apiKey = env.OTP_API_KEY || '';
  }

  async sendOtp(phone: string): Promise<OtpSendResult> {
    if (!this.apiKey) {
      throw new Error('Production OTP provider configured but OTP_API_KEY is missing');
    }

    try {
      // Integration call for production Indian SMS gateways (MSG91 / Twilio / 2Factor)
      const response = await fetch(`${this.apiUrl}/otp?template_id=KIDSG_AUTH&mobile=${phone}`, {
        method: 'POST',
        headers: {
          'authkey': this.apiKey,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`SMS Provider error: ${response.statusText}`);
      }

      return {
        success: true,
        message: 'OTP sent to mobile number',
        expiresInSeconds: 300,
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.message || 'Failed to send OTP via production provider',
        expiresInSeconds: 0,
      };
    }
  }

  async verifyOtp(phone: string, otp: string): Promise<OtpVerifyResult> {
    if (!this.apiKey) {
      throw new Error('Production OTP provider configured but OTP_API_KEY is missing');
    }

    try {
      const response = await fetch(`${this.apiUrl}/otp/verify?otp=${otp}&mobile=${phone}`, {
        method: 'POST',
        headers: {
          'authkey': this.apiKey,
        },
      });

      const data = await response.json() as any;
      if (data?.type === 'success' || response.ok) {
        return { success: true, message: 'OTP verified successfully' };
      }

      return { success: false, message: data?.message || 'Invalid OTP' };
    } catch (error: any) {
      return { success: false, message: error.message || 'Verification failed' };
    }
  }
}

export function getOtpService(): OtpService {
  if (env.OTP_PROVIDER === 'mock') {
    return new MockOtpService();
  }
  return new ProductionOtpService();
}
