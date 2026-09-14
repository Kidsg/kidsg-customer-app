export interface OtpSendResult {
  success: boolean;
  message: string;
  expiresInSeconds: number;
}

export interface OtpVerifyResult {
  success: boolean;
  message: string;
}

export interface OtpService {
  sendOtp(phone: string): Promise<OtpSendResult>;
  verifyOtp(phone: string, otp: string): Promise<OtpVerifyResult>;
}
