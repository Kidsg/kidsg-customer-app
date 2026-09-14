import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { getPaymentService } from '../services/payment/RazorpayPaymentService.js';
import { getNotificationService } from '../services/notification/NotificationService.js';
import { db } from '../lib/db.js';

const router = Router();
const paymentService = getPaymentService();
const notificationService = getNotificationService();

const createPaymentSchema = z.object({
  orderId: z.string().min(1, 'Order ID is required'),
});

const verifyPaymentSchema = z.object({
  orderId: z.string().min(1, 'Order ID is required'),
  paymentId: z.string().min(1, 'Payment ID is required'),
  signature: z.string().optional(),
  payload: z.record(z.any()).optional(),
});

// POST /api/payment/create
router.post('/payment/create', requireAuth(), async (req: Request, res: Response) => {
  const result = createPaymentSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { orderId } = result.data;
  const order = db.getOrderById(orderId, req.user!.id);
  if (!order) {
    sendError(res, 'Order not found', 'ORDER_NOT_FOUND', 404);
    return;
  }

  const intent = await paymentService.createPayment(orderId, order.total, 'INR', {
    orderNumber: order.orderNumber,
    userId: req.user!.id,
  });

  sendSuccess(res, intent, 'Payment intent created');
});

// POST /api/payment/verify
router.post('/payment/verify', requireAuth(), async (req: Request, res: Response) => {
  const result = verifyPaymentSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { orderId, paymentId, signature, payload } = result.data;
  const order = db.getOrderById(orderId, req.user!.id);
  if (!order) {
    sendError(res, 'Order not found', 'ORDER_NOT_FOUND', 404);
    return;
  }

  const verification = await paymentService.verifyPayment(orderId, paymentId, signature, payload);

  if (!verification.success) {
    order.paymentStatus = 'FAILED';
    sendError(res, verification.message, 'PAYMENT_VERIFICATION_FAILED', 400);
    return;
  }

  // Update order status upon server-authoritative payment confirmation
  order.paymentStatus = 'SUCCESS';
  db.transitionOrderStatus(orderId, 'CONFIRMED');

  await notificationService.send(
    req.user!.id,
    'PAYMENT_SUCCESS',
    'Payment Received! 💳',
    `Your payment of ₹${order.total} for order ${order.orderNumber} is confirmed.`,
    { orderId, orderNumber: order.orderNumber }
  );

  sendSuccess(res, {
    verified: true,
    orderId,
    paymentId,
    transactionId: verification.transactionId,
    orderStatus: order.status,
    order,
  }, 'Payment verified and order confirmed successfully');
});

// POST /api/payment/webhook
router.post('/payment/webhook', async (req: Request, res: Response) => {
  const event = req.body;
  // Handle webhook callbacks asynchronously from Razorpay / Payment Gateway
  if (event?.event === 'payment.captured' || event?.status === 'captured') {
    const orderId = event.payload?.payment?.entity?.notes?.orderId || event.orderId;
    if (orderId) {
      const order = db.getOrderById(orderId, 'user_dev_default');
      if (order) {
        order.paymentStatus = 'SUCCESS';
        db.transitionOrderStatus(orderId, 'CONFIRMED');
      }
    }
  }

  sendSuccess(res, { received: true });
});

export default router;
