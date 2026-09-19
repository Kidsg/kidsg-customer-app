import nodemailer from 'nodemailer';
import { Resend } from 'resend';
import { env } from '../../config/env.js';

export interface SendEmailResult {
  success: boolean;
  messageId?: string;
  error?: string;
}

export interface EmailService {
  sendOtpEmail(toEmail: string, otp: string): Promise<SendEmailResult>;
  sendOrderConfirmationEmail(toEmail: string, orderNumber: string, total: number): Promise<SendEmailResult>;
}

/**
 * Standard SMTP Email Service using Nodemailer.
 * Works seamlessly with Gmail (App Password), Outlook, Brevo, AWS SES, or any custom SMTP server.
 */
export class NodemailerEmailService implements EmailService {
  private transporter: any = null;

  constructor() {
    if (env.SMTP_USER && env.SMTP_PASS) {
      const cleanPass = env.SMTP_PASS.replace(/\s+/g, '');
      if (env.SMTP_HOST === 'smtp.gmail.com' || env.SMTP_USER.endsWith('@gmail.com')) {
        this.transporter = nodemailer.createTransport({
          service: 'gmail',
          auth: {
            user: env.SMTP_USER,
            pass: cleanPass,
          },
        });
      } else if (env.SMTP_HOST) {
        this.transporter = nodemailer.createTransport({
          host: env.SMTP_HOST,
          port: env.SMTP_PORT,
          secure: env.SMTP_SECURE,
          auth: {
            user: env.SMTP_USER,
            pass: cleanPass,
          },
        });
      }
    }
  }

  async sendOtpEmail(toEmail: string, otp: string): Promise<SendEmailResult> {
    if (!this.transporter) {
      console.log(`\n========================================`);
      console.log(`[KidsG][EMAIL][OTP - FALLBACK]`);
      console.log(`Recipient: ${toEmail}`);
      console.log(`Verification Code: ${otp}`);
      console.log(`(Configure SMTP_HOST, SMTP_USER, SMTP_PASS to dispatch via live SMTP)`);
      console.log(`========================================\n`);
      return {
        success: true,
        messageId: `dev_fallback_${Date.now()}`,
      };
    }

    try {
      const from = env.SMTP_FROM || 'KidsG <support@kidsg.in>';
      const info = await this.transporter.sendMail({
        from,
        to: toEmail,
        subject: `Your KidsG Verification Code: ${otp}`,
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 12px;">
            <div style="text-align: center; margin-bottom: 24px;">
              <h1 style="color: #FF7A00; margin: 0; font-size: 28px;">KidsG</h1>
              <p style="color: #666; font-size: 14px; margin-top: 4px;">Small Supplies. Big Futures.</p>
            </div>
            <div style="background-color: #FFF6F0; border-radius: 8px; padding: 20px; text-align: center;">
              <p style="color: #333; font-size: 16px; margin: 0 0 12px 0;">Use the code below to complete your login:</p>
              <h2 style="color: #111; font-size: 36px; letter-spacing: 6px; margin: 0; font-weight: bold;">${otp}</h2>
              <p style="color: #888; font-size: 12px; margin-top: 12px;">This code is valid for 5 minutes. Do not share it with anyone.</p>
            </div>
            <p style="color: #aaa; font-size: 11px; text-align: center; margin-top: 24px;">
              Stationery today. Brighter tomorrows with KidsG.
            </p>
          </div>
        `,
      });

      console.log(`[KidsG][Email] OTP delivered to ${toEmail} (Message ID: ${info.messageId})`);
      return { success: true, messageId: info.messageId };
    } catch (err: any) {
      console.warn(`[KidsG][Email] SMTP send failed: ${err?.message}`);
      return { success: false, error: err?.message || 'Failed to send OTP via SMTP' };
    }
  }

  async sendOrderConfirmationEmail(toEmail: string, orderNumber: string, total: number): Promise<SendEmailResult> {
    if (!this.transporter) {
      return { success: true, messageId: `mock_order_${Date.now()}` };
    }

    try {
      const from = env.SMTP_FROM || 'KidsG <support@kidsg.in>';
      const info = await this.transporter.sendMail({
        from,
        to: toEmail,
        subject: `Order Confirmed: ${orderNumber} - KidsG`,
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px;">
            <h2 style="color: #FF7A00;">Order Confirmed! 🎒</h2>
            <p>Your stationery order <strong>${orderNumber}</strong> has been received.</p>
            <p><strong>Total:</strong> ₹${total}</p>
            <p>Your partner store is packing your school essentials right now for 15-min delivery!</p>
          </div>
        `,
      });
      return { success: true, messageId: info.messageId };
    } catch (err: any) {
      return { success: false, error: err?.message };
    }
  }
}

export class ResendEmailService implements EmailService {
  private resend: Resend | null = null;

  constructor() {
    if (env.RESEND_API_KEY && env.RESEND_API_KEY.startsWith('re_')) {
      this.resend = new Resend(env.RESEND_API_KEY);
    }
  }

  async sendOtpEmail(toEmail: string, otp: string): Promise<SendEmailResult> {
    if (!this.resend) {
      return {
        success: false,
        error: 'Resend API key is not configured or invalid.',
      };
    }

    try {
      const senderEmail = (env.RESEND_FROM_EMAIL && env.RESEND_FROM_EMAIL !== 'REPLACE_ME')
        ? env.RESEND_FROM_EMAIL
        : 'onboarding@resend.dev';
      const fromAddress = `${env.RESEND_FROM_NAME || 'KidsG'} <${senderEmail}>`;
      const response = await this.resend.emails.send({
        from: fromAddress,
        to: [toEmail],
        subject: `Your KidsG Verification Code: ${otp}`,
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 12px;">
            <div style="text-align: center; margin-bottom: 24px;">
              <h1 style="color: #FF7A00; margin: 0; font-size: 28px;">KidsG</h1>
              <p style="color: #666; font-size: 14px; margin-top: 4px;">Small Supplies. Big Futures.</p>
            </div>
            <div style="background-color: #FFF6F0; border-radius: 8px; padding: 20px; text-align: center;">
              <p style="color: #333; font-size: 16px; margin: 0 0 12px 0;">Use the code below to complete your login:</p>
              <h2 style="color: #111; font-size: 36px; letter-spacing: 6px; margin: 0; font-weight: bold;">${otp}</h2>
              <p style="color: #888; font-size: 12px; margin-top: 12px;">This code is valid for 5 minutes. Do not share it with anyone.</p>
            </div>
            <p style="color: #aaa; font-size: 11px; text-align: center; margin-top: 24px;">
              Stationery today. Brighter tomorrows with KidsG.
            </p>
          </div>
        `,
      });

      if (response.error) {
        return {
          success: false,
          error: response.error.message || 'Failed to send email via Resend',
        };
      }

      return {
        success: true,
        messageId: response.data?.id,
      };
    } catch (err: any) {
      return {
        success: false,
        error: err?.message || 'Error occurred while dispatching email via Resend',
      };
    }
  }

  async sendOrderConfirmationEmail(toEmail: string, orderNumber: string, total: number): Promise<SendEmailResult> {
    if (!this.resend || !env.RESEND_FROM_EMAIL || env.RESEND_FROM_EMAIL === 'REPLACE_ME') {
      return { success: false, error: 'Resend sender not configured' };
    }

    try {
      const fromAddress = `${env.RESEND_FROM_NAME} <${env.RESEND_FROM_EMAIL}>`;
      const response = await this.resend.emails.send({
        from: fromAddress,
        to: [toEmail],
        subject: `Order Confirmed: ${orderNumber} - KidsG`,
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px;">
            <h2 style="color: #FF7A00;">Order Confirmed! 🎒</h2>
            <p>Your stationery order <strong>${orderNumber}</strong> has been received.</p>
            <p><strong>Total:</strong> ₹${total}</p>
            <p>Your partner store is packing your school essentials right now for 15-min delivery!</p>
          </div>
        `,
      });

      return { success: !response.error, messageId: response.data?.id };
    } catch {
      return { success: false, error: 'Failed to send confirmation email' };
    }
  }
}

export class MockEmailService implements EmailService {
  async sendOtpEmail(toEmail: string, otp: string): Promise<SendEmailResult> {
    console.log(`\n========================================`);
    console.log(`[KidsG][DEV][MOCK EMAIL OTP]`);
    console.log(`To: ${toEmail}`);
    console.log(`Code: ${otp}`);
    console.log(`========================================\n`);
    return {
      success: true,
      messageId: `mock_email_${Date.now()}`,
    };
  }

  async sendOrderConfirmationEmail(_toEmail: string, _orderNumber: string, _total: number): Promise<SendEmailResult> {
    return {
      success: true,
      messageId: `mock_email_order_${Date.now()}`,
    };
  }
}

export function getEmailService(): EmailService {
  if (env.EMAIL_PROVIDER === 'smtp' || env.SMTP_HOST) {
    return new NodemailerEmailService();
  }
  if (env.EMAIL_PROVIDER === 'resend' && env.RESEND_API_KEY && env.RESEND_API_KEY.startsWith('re_')) {
    return new ResendEmailService();
  }
  return new NodemailerEmailService();
}
