import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { db } from '../lib/db.js';

const router = Router();

const checkoutPreviewSchema = z.object({
  couponCode: z.string().nullish(),
});

const checkoutCreateSchema = z.object({
  addressId: z.string().optional().default('addr_default'),
  couponCode: z.string().nullish(),
  paymentMethod: z.string().default('UPI'),
  notes: z.string().nullish(),
  items: z.array(z.object({
    productId: z.string(),
    quantity: z.number().default(1),
    selectedVariant: z.string().nullish(),
    price: z.number().optional(),
    name: z.string().optional(),
  })).optional(),
  deliveryAddress: z.any().optional(),
});

// POST /api/checkout/preview
router.post('/checkout/preview', requireAuth(), (req: Request, res: Response) => {
  const result = checkoutPreviewSchema.safeParse(req.body);
  const couponCode = result.success ? (result.data.couponCode || undefined) : undefined;

  const checkout = db.calculateCheckout(req.user!.id, couponCode);

  sendSuccess(res, {
    items: checkout.items,
    subtotal: checkout.subtotal,
    discount: checkout.discount,
    couponDiscount: checkout.couponDiscount,
    couponCode: checkout.couponCode || null,
    deliveryFee: checkout.deliveryFee,
    tax: checkout.tax,
    total: checkout.total,
    freeDeliveryThreshold: checkout.freeDeliveryThreshold,
    freeDeliveryUnlocked: checkout.deliveryFee === 0 && checkout.subtotal > 0,
    amountNeededForFreeDelivery: Math.max(0, checkout.freeDeliveryThreshold - checkout.subtotal),
  });
});

// POST /api/checkout/create
router.post('/checkout/create', requireAuth(), (req: Request, res: Response) => {
  const result = checkoutCreateSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { addressId, couponCode, paymentMethod, notes, items, deliveryAddress } = result.data;
  const orderRes = db.createOrder(
    req.user!.id,
    addressId || 'addr_default',
    paymentMethod,
    couponCode || undefined,
    notes || undefined,
    items,
    deliveryAddress
  );

  if (!orderRes.success || !orderRes.order) {
    sendError(res, orderRes.error || 'Failed to initialize checkout', 'CHECKOUT_FAILED', 400);
    return;
  }

  sendSuccess(res, {
    orderId: orderRes.order.id,
    orderNumber: orderRes.order.orderNumber,
    total: orderRes.order.total,
    paymentMethod: orderRes.order.paymentMethod,
    order: orderRes.order,
  }, 'Checkout initiated successfully', 201);
});

export default router;
