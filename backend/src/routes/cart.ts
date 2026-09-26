import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { optionalAuth } from '../middleware/auth.js';
import { db } from '../lib/db.js';

const router = Router();

function getEffectiveUserId(req: Request): string {
  if (req.user?.id) return req.user.id;
  const guestHeader = (req.headers['x-guest-id'] || req.headers['x-session-id']) as string | undefined;
  if (guestHeader && typeof guestHeader === 'string' && guestHeader.trim().length > 0) {
    return `guest_${guestHeader.trim()}`;
  }
  return 'guest_default_user';
}

const addItemSchema = z.object({
  productId: z.string().min(1, 'Product ID is required'),
  quantity: z.number().int().positive('Quantity must be greater than zero').default(1),
  selectedVariant: z.string().nullish(),
});

const updateItemSchema = z.object({
  quantity: z.number().int().min(0, 'Quantity cannot be negative'),
});

// GET /api/cart
router.get('/cart', optionalAuth(), (req: Request, res: Response) => {
  const userId = getEffectiveUserId(req);
  const cart = db.getCart(userId);
  sendSuccess(res, cart);
});

// POST /api/cart/items
router.post('/cart/items', optionalAuth(), (req: Request, res: Response) => {
  const result = addItemSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const userId = getEffectiveUserId(req);
  const { productId, quantity, selectedVariant } = result.data;
  const resCart = db.addToCart(userId, productId, quantity, selectedVariant || undefined);

  if (!resCart.success) {
    sendError(res, resCart.error || 'Could not add to bag', 'CART_ERROR', 400);
    return;
  }

  sendSuccess(res, resCart.cart, 'Item added to School Bag', 201);
});

// PATCH /api/cart/items/:id
router.patch('/cart/items/:id', optionalAuth(), (req: Request, res: Response) => {
  const result = updateItemSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const userId = getEffectiveUserId(req);
  const resCart = db.updateCartItem(userId, String(req.params.id), result.data.quantity);
  if (!resCart.success) {
    sendError(res, resCart.error || 'Could not update item', 'CART_ERROR', 400);
    return;
  }

  sendSuccess(res, resCart.cart, 'School Bag updated');
});

// DELETE /api/cart/items/:id
router.delete('/cart/items/:id', optionalAuth(), (req: Request, res: Response) => {
  const userId = getEffectiveUserId(req);
  const resCart = db.removeFromCart(userId, String(req.params.id));
  sendSuccess(res, resCart.cart, 'Item removed from School Bag');
});

// DELETE /api/cart
router.delete('/cart', optionalAuth(), (req: Request, res: Response) => {
  const userId = getEffectiveUserId(req);
  db.clearCart(userId);
  sendSuccess(res, { cleared: true, items: [], subtotal: 0, totalItems: 0 }, 'School Bag emptied');
});

export default router;
