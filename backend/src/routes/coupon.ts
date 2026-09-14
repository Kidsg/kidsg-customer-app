import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { db } from '../lib/db.js';

const router = Router();

const validateCouponSchema = z.object({
  code: z.string().min(1, 'Coupon code is required'),
});

// GET /api/coupons
router.get('/coupons', (_req: Request, res: Response) => {
  const coupons = db.getCoupons();
  sendSuccess(res, coupons);
});

// POST /api/coupons/validate
router.post('/coupons/validate', requireAuth(), (req: Request, res: Response) => {
  const result = validateCouponSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { code } = result.data;
  const { subtotal } = db.getCart(req.user!.id);
  const validation = db.validateCoupon(code, subtotal);

  if (!validation.valid) {
    sendError(res, validation.message, 'COUPON_INVALID', 400);
    return;
  }

  sendSuccess(res, {
    valid: true,
    code: validation.coupon!.code,
    discountAmount: validation.discount,
    description: validation.coupon!.description,
    message: validation.message,
  });
});

export default router;
