import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { db } from '../lib/db.js';

const router = Router();

const addItemSchema = z.object({
  productId: z.string().min(1, 'Product ID is required'),
  quantity: z.number().int().positive('Quantity must be greater than zero').default(1),
  selectedVariant: z.string().nullish(),
});

const updateItemSchema = z.object({
  quantity: z.number().int().min(0, 'Quantity cannot be negative'),
});

// GET /api/cart
router.get('/cart', requireAuth(), (req: Request, res: Response) => {
  const cart = db.getCart(req.user!.id);
  sendSuccess(res, cart);
});

// POST /api/cart/items
router.post('/cart/items', requireAuth(), (req: Request, res: Response) => {
  const result = addItemSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { productId, quantity, selectedVariant } = result.data;
  const resCart = db.addToCart(req.user!.id, productId, quantity, selectedVariant);

  if (!resCart.success) {
    sendError(res, resCart.error || 'Could not add to bag', 'CART_ERROR', 400);
    return;
  }

  sendSuccess(res, resCart.cart, 'Item added to School Bag', 201);
});

// PATCH /api/cart/items/:id
router.patch('/cart/items/:id', requireAuth(), (req: Request, res: Response) => {
  const result = updateItemSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const resCart = db.updateCartItem(req.user!.id, String(req.params.id), result.data.quantity);
  if (!resCart.success) {
    sendError(res, resCart.error || 'Could not update item', 'CART_ERROR', 400);
    return;
  }

  sendSuccess(res, resCart.cart, 'School Bag updated');
});

// DELETE /api/cart/items/:id
router.delete('/cart/items/:id', requireAuth(), (req: Request, res: Response) => {
  const resCart = db.removeFromCart(req.user!.id, String(req.params.id));
  sendSuccess(res, resCart.cart, 'Item removed from School Bag');
});

// DELETE /api/cart
router.delete('/cart', requireAuth(), (req: Request, res: Response) => {
  db.clearCart(req.user!.id);
  sendSuccess(res, { cleared: true, items: [], subtotal: 0, totalItems: 0 }, 'School Bag emptied');
});

export default router;
