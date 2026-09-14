export interface PaymentIntent {
  paymentId: string;
  orderId: string;
  amount: number;
  currency: string;
  provider: string;
  gatewayOrderId?: string;
  clientSecret?: string;
  metadata?: Record<string, any>;
}

export interface PaymentVerificationResult {
  success: boolean;
  orderId: string;
  paymentId: string;
  transactionId: string;
  status: 'SUCCESS' | 'FAILED' | 'PENDING';
  message: string;
}

export interface PaymentRefundResult {
  success: boolean;
  refundId?: string;
  message: string;
}

export interface PaymentService {
  createPayment(
    orderId: string,
    amount: number,
    currency?: string,
    metadata?: Record<string, any>
  ): Promise<PaymentIntent>;

  verifyPayment(
    orderId: string,
    paymentId: string,
    signature?: string,
    payload?: Record<string, any>
  ): Promise<PaymentVerificationResult>;

  refundPayment(paymentId: string, amount?: number): Promise<PaymentRefundResult>;
}
