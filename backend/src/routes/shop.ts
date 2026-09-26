import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { db } from '../lib/db.js';
import { getNotificationService } from '../services/notification/NotificationService.js';

const router = Router();
const notificationService = getNotificationService();

// GET /api/shop/orders - View all shop orders
router.get('/shop/orders', (req: Request, res: Response) => {
  const storeId = req.query.storeId as string | undefined;
  const statusFilter = req.query.status as string | undefined;

  const orders = db.getShopOrders(storeId, statusFilter);
  sendSuccess(res, orders);
});

// GET /api/shop/orders/:id - View single shop order detail with items snapshot
router.get('/shop/orders/:id', (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const order = db.getOrderByIdAdmin(orderId);

  if (!order) {
    sendError(res, 'Order not found', 'ORDER_NOT_FOUND', 404);
    return;
  }

  const history = db.getOrderStatusHistory(orderId);
  sendSuccess(res, {
    order,
    statusHistory: history,
  });
});

// POST /api/shop/orders/:id/accept - Shop owner accepts order
router.post('/shop/orders/:id/accept', async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const result = db.shopAcceptOrder(orderId);

  if (!result.success || !result.order) {
    sendError(res, result.error || 'Failed to accept order', 'ACCEPT_FAILED', 400);
    return;
  }

  // Notify customer
  await notificationService.send(
    result.order.userId,
    'STORE_ACCEPTED',
    'Order Accepted! 🛍️',
    `Vidya Stationery Depot has accepted your order ${result.order.orderNumber}.`,
    { orderId, orderNumber: result.order.orderNumber }
  );

  sendSuccess(res, result.order, 'Order accepted by store');
});

// POST /api/shop/orders/:id/reject - Shop owner rejects order
router.post('/shop/orders/:id/reject', async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const reason = req.body.reason as string | undefined;
  const result = db.shopRejectOrder(orderId, undefined, reason);

  if (!result.success || !result.order) {
    sendError(res, result.error || 'Failed to reject order', 'REJECT_FAILED', 400);
    return;
  }

  // Notify customer
  await notificationService.send(
    result.order.userId,
    'ORDER_CANCELLED',
    'Order Update',
    `Order ${result.order.orderNumber} could not be fulfilled by the store. Any amount paid will be refunded.`,
    { orderId, orderNumber: result.order.orderNumber }
  );

  sendSuccess(res, result.order, 'Order rejected');
});

// POST /api/shop/orders/:id/packing - Shop owner marks order packing
router.post('/shop/orders/:id/packing', async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const result = db.shopStartPacking(orderId);

  if (!result.success || !result.order) {
    sendError(res, result.error || 'Failed to update packing status', 'UPDATE_FAILED', 400);
    return;
  }

  // Notify customer
  await notificationService.send(
    result.order.userId,
    'ORDER_PREPARING',
    'Stationery Being Packed 📦',
    `Your school supplies for order ${result.order.orderNumber} are being packed with care.`,
    { orderId, orderNumber: result.order.orderNumber }
  );

  sendSuccess(res, result.order, 'Order status changed to PREPARING');
});

// POST /api/shop/orders/:id/ready - Shop owner marks ready for pickup
router.post('/shop/orders/:id/ready', async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const result = db.shopReadyForPickup(orderId);

  if (!result.success || !result.order) {
    sendError(res, result.error || 'Failed to update status', 'UPDATE_FAILED', 400);
    return;
  }

  // Notify customer
  await notificationService.send(
    result.order.userId,
    'READY_FOR_PICKUP',
    'Order Ready for Pickup 🚀',
    `Order ${result.order.orderNumber} is packed and ready. Delivery partner assigned.`,
    { orderId, orderNumber: result.order.orderNumber }
  );

  sendSuccess(res, result.order, 'Order status changed to READY_FOR_PICKUP');
});

// POST /api/delivery/orders/:id/advance - Mock delivery partner progression
const advanceDeliverySchema = z.object({
  status: z.enum(['PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED']),
});

router.post('/delivery/orders/:id/advance', async (req: Request, res: Response) => {
  const parseResult = advanceDeliverySchema.safeParse(req.body);
  if (!parseResult.success) {
    sendError(res, 'Valid status required: PICKED_UP, OUT_FOR_DELIVERY, or DELIVERED', 'VALIDATION_ERROR', 400);
    return;
  }

  const orderId = String(req.params.id);
  const targetStatus = parseResult.data.status;
  const result = db.advanceDeliveryStatus(orderId, targetStatus);

  if (!result.success || !result.order) {
    sendError(res, result.error || 'Failed to advance delivery', 'DELIVERY_UPDATE_FAILED', 400);
    return;
  }

  // Notify customer of delivery updates
  const titles: Record<string, string> = {
    PICKED_UP: 'Stationery Picked Up 🛵',
    OUT_FOR_DELIVERY: 'Out for Delivery 🚀',
    DELIVERED: 'Delivered Successfully! 🎉',
  };
  const messages: Record<string, string> = {
    PICKED_UP: `Delivery partner Venkatesh has picked up your stationery bag for order ${result.order.orderNumber}.`,
    OUT_FOR_DELIVERY: `Rider is on the way with your books and stationery for order ${result.order.orderNumber}.`,
    DELIVERED: `Order ${result.order.orderNumber} has been delivered. Have a bright school day!`,
  };

  await notificationService.send(
    result.order.userId,
    targetStatus,
    titles[targetStatus] || 'Delivery Update',
    messages[targetStatus] || `Status: ${targetStatus}`,
    { orderId, orderNumber: result.order.orderNumber, status: targetStatus }
  );

  sendSuccess(res, result.order, `Delivery status advanced to ${targetStatus}`);
});

export default router;
