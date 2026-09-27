import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { getDeliveryTrackingService } from '../services/delivery/DeliveryTrackingService.js';
import { getNotificationService } from '../services/notification/NotificationService.js';
import { db } from '../lib/db.js';
import { supabaseAdmin } from '../lib/supabase.js';
import { syncOrderToSupabase } from '../lib/supabaseSync.js';

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
router.post('/orders', requireAuth(), async (req: Request, res: Response) => {
  const result = createOrderSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { addressId, paymentMethod, couponCode, notes, items, deliveryAddress } = result.data;
  const orderRes = db.createOrder(
    req.user!.id,
    addressId || 'addr_default',
    paymentMethod,
    couponCode,
    notes,
    items,
    deliveryAddress
  );

  if (!orderRes.success || !orderRes.order) {
    sendError(res, orderRes.error || 'Failed to place order', 'ORDER_CREATION_FAILED', 400);
    return;
  }

  const order = orderRes.order;

  const userEmail = req.user?.email || 'student@kidsg.in';
  syncOrderToSupabase(userEmail, order, deliveryAddress).catch(e => {
    console.warn('[KidsG][Supabase] Order sync warning:', e?.message);
  });

  // Send notification to customer
  await notificationService.send(
    req.user!.id,
    'ORDER_CONFIRMED',
    'Order Placed! 🚀',
    `Your stationery order ${order.orderNumber} is confirmed and sent to the store.`,
    { orderId: order.id, orderNumber: order.orderNumber }
  );

  // Send persistent notification to shop owner
  await notificationService.send(
    'shop_owner_vidya',
    'NEW_ORDER',
    'New KidsG order received 🎒',
    `Order #${order.orderNumber} (${order.items.length} items, ₹${order.total}) requires your attention.`,
    { orderId: order.id, orderNumber: order.orderNumber, storeId: order.storeId, total: order.total }
  );

  sendSuccess(res, order, 'Order placed successfully', 201);
});

// GET /api/orders
router.get('/orders', requireAuth(), (req: Request, res: Response) => {
  const orders = db.getOrders(req.user!.id);
  sendSuccess(res, orders);
});

// GET /api/orders/:id
router.get('/orders/:id', requireAuth(), (req: Request, res: Response) => {
  const order = db.getOrderById(String(req.params.id), req.user!.id);
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

export default router;
