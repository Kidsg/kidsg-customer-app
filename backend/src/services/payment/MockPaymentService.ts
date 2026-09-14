import { PaymentService, PaymentIntent, PaymentVerificationResult, PaymentRefundResult } from './PaymentService.js';
import { randomUUID } from 'crypto';

export class MockPaymentService implements PaymentService {
  private static payments: Map<string, PaymentIntent> = new Map();

  async createPayment(
    orderId: string,
    amount: number,
    currency = 'INR',
    metadata: Record<string, any> = {}
  ): Promise<PaymentIntent> {
    const paymentId = `pay_mock_${randomUUID().substring(0, 12)}`;
    const gatewayOrderId = `order_mock_${randomUUID().substring(0, 12)}`;

    const intent: PaymentIntent = {
      paymentId,
      orderId,
      amount,
      currency,
      provider: 'mock',
      gatewayOrderId,
      clientSecret: `sec_mock_${randomUUID().substring(0, 16)}`,
      metadata: {
        ...metadata,
        isDevelopmentMock: true,
      },
    };

    MockPaymentService.payments.set(paymentId, intent);
    return intent;
  }

  async verifyPayment(
    orderId: string,
    paymentId: string,
    _signature?: string,
    _payload?: Record<string, any>
  ): Promise<PaymentVerificationResult> {
    const payment = MockPaymentService.payments.get(paymentId);

    // In mock mode, if payment exists or has valid format, simulate backend verified success
    if (payment || paymentId.startsWith('pay_mock_') || paymentId.startsWith('mock_')) {
      return {
        success: true,
        orderId,
        paymentId,
        transactionId: `txn_mock_${Date.now()}`,
        status: 'SUCCESS',
        message: 'Mock payment verified successfully by server',
      };
    }

    return {
      success: false,
      orderId,
      paymentId,
      transactionId: '',
      status: 'FAILED',
      message: 'Payment verification failed: Payment not found',
    };
  }

  async refundPayment(paymentId: string, _amount?: number): Promise<PaymentRefundResult> {
    return {
      success: true,
      refundId: `rfnd_mock_${randomUUID().substring(0, 10)}`,
      message: 'Mock payment refunded successfully',
    };
  }
}
