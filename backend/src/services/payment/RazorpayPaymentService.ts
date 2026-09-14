import { PaymentService, PaymentIntent, PaymentVerificationResult, PaymentRefundResult } from './PaymentService.js';
import { env } from '../../config/env.js';
import { MockPaymentService } from './MockPaymentService.js';
import crypto from 'crypto';

export class RazorpayPaymentService implements PaymentService {
  private keyId: string;
  private keySecret: string;
  private webhookSecret: string;

  constructor() {
    this.keyId = env.RAZORPAY_KEY_ID || '';
    this.keySecret = env.RAZORPAY_KEY_SECRET || '';
    this.webhookSecret = env.RAZORPAY_WEBHOOK_SECRET || '';
  }

  async createPayment(
    orderId: string,
    amount: number,
    currency = 'INR',
    metadata: Record<string, any> = {}
  ): Promise<PaymentIntent> {
    if (!this.keyId || !this.keySecret) {
      throw new Error('Razorpay credentials not configured');
    }

    const auth = Buffer.from(`${this.keyId}:${this.keySecret}`).toString('base64');
    const response = await fetch('https://api.razorpay.com/v1/orders', {
      method: 'POST',
      headers: {
        'Authorization': `Basic ${auth}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        amount: Math.round(amount * 100), // Razorpay accepts paise
        currency,
        receipt: orderId,
        notes: metadata,
      }),
    });

    if (!response.ok) {
      const err = await response.text();
      throw new Error(`Razorpay order creation failed: ${err}`);
    }

    const data = await response.json() as any;
    return {
      paymentId: data.id,
      orderId,
      amount,
      currency,
      provider: 'razorpay',
      gatewayOrderId: data.id,
      metadata,
    };
  }

  async verifyPayment(
    orderId: string,
    paymentId: string,
    signature?: string,
    payload?: Record<string, any>
  ): Promise<PaymentVerificationResult> {
    if (!this.keySecret) {
      throw new Error('Razorpay key secret not configured');
    }

    const gatewayOrderId = payload?.razorpay_order_id || '';
    const razorpayPaymentId = payload?.razorpay_payment_id || paymentId;

    if (!signature || !gatewayOrderId) {
      return {
        success: false,
        orderId,
        paymentId,
        transactionId: '',
        status: 'FAILED',
        message: 'Missing signature or gateway order ID for verification',
      };
    }

    const body = `${gatewayOrderId}|${razorpayPaymentId}`;
    const expectedSignature = crypto
      .createHmac('sha256', this.keySecret)
      .update(body)
      .digest('hex');

    const isValid = crypto.timingSafeEqual(
      Buffer.from(expectedSignature),
      Buffer.from(signature)
    );

    if (isValid) {
      return {
        success: true,
        orderId,
        paymentId: razorpayPaymentId,
        transactionId: razorpayPaymentId,
        status: 'SUCCESS',
        message: 'Razorpay payment verified successfully',
      };
    }

    return {
      success: false,
      orderId,
      paymentId,
      transactionId: '',
      status: 'FAILED',
      message: 'Signature mismatch during payment verification',
    };
  }

  async refundPayment(paymentId: string, amount?: number): Promise<PaymentRefundResult> {
    if (!this.keyId || !this.keySecret) {
      throw new Error('Razorpay credentials not configured');
    }

    const auth = Buffer.from(`${this.keyId}:${this.keySecret}`).toString('base64');
    const response = await fetch(`https://api.razorpay.com/v1/payments/${paymentId}/refund`, {
      method: 'POST',
      headers: {
        'Authorization': `Basic ${auth}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(amount ? { amount: Math.round(amount * 100) } : {}),
    });

    if (!response.ok) {
      return { success: false, message: 'Refund failed at gateway' };
    }

    const data = await response.json() as any;
    return {
      success: true,
      refundId: data.id,
      message: 'Refund initiated successfully',
    };
  }
}

export function getPaymentService(): PaymentService {
  if (env.PAYMENT_PROVIDER === 'mock') {
    return new MockPaymentService();
  }
  return new RazorpayPaymentService();
}
