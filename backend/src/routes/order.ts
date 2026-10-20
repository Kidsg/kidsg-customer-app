import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth, optionalAuth } from '../middleware/auth.js';
import { getDeliveryTrackingService } from '../services/delivery/DeliveryTrackingService.js';
import { getNotificationService } from '../services/notification/NotificationService.js';
import { db } from '../lib/db.js';
import { supabaseAdmin } from '../lib/supabase.js';
import { syncOrderToSupabase, fetchUserOrdersFromSupabase, updateSupabaseOrderStatus } from '../lib/supabaseSync.js';

const router = Router();
const deliveryTrackingService = getDeliveryTrackingService();
const notificationService = getNotificationService();

const createOrderSchema = z.object({
  addressId: z.string().optional().default('addr_default'),
  paymentMethod: z.string().default('UPI'),
  couponCode: z.string().optional(),
  notes: z.string().optional(),
  items: z.array(z.object({
    productId: z.string(),
    quantity: z.number().default(1),
    selectedVariant: z.string().nullish(),
    price: z.number().optional(),
    name: z.string().optional(),
  })).optional(),
  deliveryAddress: z.any().optional(),
});

// POST /api/orders
router.post('/orders', optionalAuth(), async (req: Request, res: Response) => {
  const result = createOrderSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { addressId, paymentMethod, couponCode, notes, items, deliveryAddress } = result.data;
  const userId = req.user?.id || 'user_dev_default';
  const orderRes = db.createOrder(
    userId,
    addressId || 'addr_default',
    paymentMethod,
    couponCode,
    notes,
    items,
    deliveryAddress,
    'ORDER_RECEIVED'
  );

  if (!orderRes.success || !orderRes.order) {
    sendError(res, orderRes.error || 'Failed to place order', 'ORDER_CREATION_FAILED', 400);
    return;
  }

  const order = orderRes.order;

  const userEmail = req.user?.email || 'student@kidsg.in';
  // Synchronously await Supabase write to guarantee instant DB visibility
  try {
    await syncOrderToSupabase(userEmail, order, deliveryAddress);
  } catch (e: any) {
    console.warn('[KidsG][Supabase] Order sync warning:', e?.message);
  }

  // Send notification to customer
  await notificationService.send(
    userId,
    'ORDER_CONFIRMED',
    'Order Placed! 🚀',
    `Your stationery order ${order.orderNumber} is confirmed and sent to the store.`,
    { orderId: order.id, orderNumber: order.orderNumber }
  );

  // Send persistent notification to founder/admin
  await notificationService.send(
    'user_founder_01',
    'NEW_ORDER',
    'New BookBuddys Order Received! 🎒',
    `Order #${order.orderNumber} (₹${order.total}) from ${order.addressSnapshot?.city || 'Bengaluru'} requires stock confirmation.`,
    { orderId: order.id, orderNumber: order.orderNumber, storeId: order.storeId, total: order.total }
  );

  sendSuccess(res, order, 'Order placed successfully', 201);
});

// GET /api/orders
router.get('/orders', requireAuth(), async (req: Request, res: Response) => {
  const userEmail = req.user?.email || '';
  const supaOrders = userEmail ? await fetchUserOrdersFromSupabase(userEmail) : [];
  
  if (supaOrders.length > 0) {
    sendSuccess(res, supaOrders);
    return;
  }

  const localOrders = db.getOrders(req.user!.id);
  sendSuccess(res, localOrders);
});

// GET /api/orders/:id
router.get('/orders/:id', requireAuth(), async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const userEmail = req.user?.email || '';
  const supaOrders = userEmail ? await fetchUserOrdersFromSupabase(userEmail) : [];
  const supaOrder = supaOrders.find(o => o.id === orderId || o.orderNumber === orderId);

  if (supaOrder) {
    const statusHistory = db.getOrderStatusHistory(supaOrder.id);
    sendSuccess(res, {
      ...supaOrder,
      statusHistory,
    });
    return;
  }

  const order = db.getOrderById(orderId, req.user!.id);
  if (!order) {
    sendError(res, 'Order not found', 'ORDER_NOT_FOUND', 404);
    return;
  }
  const statusHistory = db.getOrderStatusHistory(order.id);
  sendSuccess(res, {
    ...order,
    statusHistory,
  });
});

// POST /api/orders/:id/cancel
router.post('/orders/:id/cancel', requireAuth(), async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const cancelRes = db.cancelOrder(orderId, req.user!.id);
  if (!cancelRes.success) {
    sendError(res, cancelRes.error || 'Could not cancel order', 'CANCEL_FAILED', 400);
    return;
  }

  await updateSupabaseOrderStatus(orderId, 'CANCELLED');

  await notificationService.send(
    req.user!.id,
    'ORDER_CANCELLED',
    'Order Cancelled',
    `Order ${cancelRes.order!.orderNumber} has been cancelled.`,
    { orderId }
  );

  sendSuccess(res, cancelRes.order, 'Order cancelled successfully');
});

// GET /api/orders/:id/tracking
router.get('/orders/:id/tracking', requireAuth(), async (req: Request, res: Response) => {
  const order = db.getOrderById(String(req.params.id), req.user!.id);
  if (!order) {
    sendError(res, 'Order not found', 'ORDER_NOT_FOUND', 404);
    return;
  }

  const tracking = await deliveryTrackingService.getTracking(
    order.id,
    order.createdAt,
    order.status
  );

  const persistedHistory = db.getOrderStatusHistory(order.id);

  sendSuccess(res, {
    ...tracking,
    timeline: persistedHistory,
  });
});

// POST /api/orders/:id/submit-payment (Customer submits UPI transaction reference / proof)
router.post('/orders/:id/submit-payment', optionalAuth(), async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const utr = req.body.transactionReference || req.body.utr || req.body.utrNumber;
  const amount = req.body.amount ? Number(req.body.amount) : undefined;
  const screenshotUrl = req.body.screenshotUrl;
  const notes = req.body.notes || (req.body.paymentApp ? `App: ${req.body.paymentApp}` : undefined);
  const userId = req.user?.id || 'user_dev_default';

  if (!utr || String(utr).trim().length < 6) {
    sendError(res, 'Valid UPI Transaction Reference (UTR / 12-digit Ref) is required', 'VALIDATION_ERROR', 400);
    return;
  }

  const result = db.submitManualPayment(orderId, userId, {
    transactionReference: String(utr),
    amount,
    screenshotUrl,
    notes,
  });

  if (!result.success) {
    sendError(res, result.error || 'Failed to submit payment', 'PAYMENT_SUBMISSION_FAILED', 400);
    return;
  }

  // Notify founder that a payment requires verification
  await notificationService.send(
    'user_founder_01',
    'PAYMENT_SUBMITTED',
    'Payment Verification Required 💳',
    `Payment UTR ${utr} submitted for order. Please verify bank credit in Founder Dashboard.`,
    { orderId, utr, amount }
  );

  const order = db.getOrderByIdAdmin(orderId);

  sendSuccess(res, {
    submission: result.submission,
    order,
    message: 'Payment reference submitted successfully. Founder will verify bank credit shortly.',
  });
});

// GET /api/orders/:id/timeline (Customer full 10-step order tracking journey)
router.get('/orders/:id/timeline', optionalAuth(), (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const order = db.getOrderByIdAdmin(orderId);
  if (!order) {
    sendError(res, 'Order not found', 'ORDER_NOT_FOUND', 404);
    return;
  }

  const persistedHistory = db.getOrderStatusHistory(order.id);
  const delivery = db.getDeliveryBookingForOrder(order.id);

  const stepsDefinition = [
    { step: 1, title: 'Order Received', status: 'ORDER_RECEIVED', desc: 'Order request received and queued' },
    { step: 2, title: 'Shop Stock Confirmed', status: 'SHOP_CONFIRMED', desc: 'Partner shop confirmed inventory' },
    { step: 3, title: 'Payment Required', status: 'PAYMENT_REQUIRED', desc: 'Order accepted; payment due' },
    { step: 4, title: 'Payment Submitted', status: 'PAYMENT_SUBMITTED', desc: 'UPI reference submitted by customer' },
    { step: 5, title: 'Payment Verified', status: 'PAID', desc: 'Bank receipt confirmed by founder' },
    { step: 6, title: 'Items Procured & Ready', status: 'ITEMS_PREPARED', desc: 'Supplies packed and labelled' },
    { step: 7, title: 'Delivery Booked', status: 'DELIVERY_BOOKED', desc: 'Hyperlocal delivery partner assigned' },
    { step: 8, title: 'Picked Up', status: 'PICKED_UP', desc: 'Package collected from store' },
    { step: 9, title: 'Out for Delivery', status: 'OUT_FOR_DELIVERY', desc: 'Rider on way to delivery address' },
    { step: 10, title: 'Delivered', status: 'DELIVERED', desc: 'Delivered successfully' },
  ];

  const statusPriority: Record<string, number> = {
    ORDER_RECEIVED: 1,
    SHOP_CONFIRMED: 2,
    PAYMENT_REQUIRED: 3,
    PAYMENT_SUBMITTED: 4,
    PAID: 5,
    PAYMENT_VERIFIED: 5,
    ITEMS_PREPARED: 6,
    PREPARING: 6,
    READY_FOR_PICKUP: 6,
    DELIVERY_BOOKED: 7,
    DELIVERY_ASSIGNED: 7,
    PICKED_UP: 8,
    OUT_FOR_DELIVERY: 9,
    DELIVERED: 10,
    CANCELLED: 0,
    REFUNDED: 0,
  };

  const currentPriority = statusPriority[order.status] ?? 1;

  const timeline = stepsDefinition.map(s => {
    const isCompleted = currentPriority > s.step || (currentPriority === 10 && s.step === 10);
    const isCurrent = currentPriority === s.step;
    const hist = persistedHistory.find(h => h.status === s.status);
    return {
      step: s.step,
      title: s.title,
      description: s.desc,
      isCompleted,
      isCurrent,
      timestamp: hist?.timestamp || (isCompleted ? order.createdAt : undefined),
    };
  });

  sendSuccess(res, {
    orderNumber: order.orderNumber,
    currentStatus: order.status,
    total: order.total,
    storeName: order.storeName,
    paymentStatus: order.paymentStatus,
    timeline,
    persistedHistory,
    delivery,
  });
});

export default router;
