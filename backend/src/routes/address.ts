import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { db } from '../lib/db.js';

const router = Router();

const addressSchema = z.object({
  label: z.string().default('Home'),
  name: z.string().min(2, 'Contact name is required'),
  phone: z.string().min(10, 'Valid phone is required'),
  addressLine1: z.string().min(5, 'Address line 1 is required'),
  addressLine2: z.string().optional().default(''),
  city: z.string().min(2, 'City is required'),
  state: z.string().default('Karnataka'),
  postalCode: z.string().min(5, 'Postal code is required'),
  latitude: z.number().optional(),
  longitude: z.number().optional(),
  deliveryInstructions: z.string().optional(),
  isDefault: z.boolean().optional().default(false),
});

// GET /api/addresses
router.get('/addresses', requireAuth(), (req: Request, res: Response) => {
  const addresses = db.getAddresses(req.user!.id);
  sendSuccess(res, addresses);
});

// POST /api/addresses
router.post('/addresses', requireAuth(), (req: Request, res: Response) => {
  const result = addressSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const newAddress = db.addAddress(req.user!.id, result.data);
  sendSuccess(res, newAddress, 'Address saved successfully', 201);
});

// PATCH /api/addresses/:id
router.patch('/addresses/:id', requireAuth(), (req: Request, res: Response) => {
  const addressId = String(req.params.id);
  const result = addressSchema.partial().safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const updated = db.updateAddress(req.user!.id, addressId, result.data);
  if (!updated) {
    sendError(res, 'Address not found or unauthorized', 'NOT_FOUND', 404);
    return;
  }

  sendSuccess(res, updated, 'Address updated successfully');
});

// DELETE /api/addresses/:id
router.delete('/addresses/:id', requireAuth(), (req: Request, res: Response) => {
  const addressId = String(req.params.id);
  const success = db.deleteAddress(req.user!.id, addressId);
  if (!success) {
    sendError(res, 'Address not found', 'NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, { deleted: true }, 'Address deleted successfully');
});

// POST /api/addresses/:id/default
router.post('/addresses/:id/default', requireAuth(), (req: Request, res: Response) => {
  const addressId = String(req.params.id);
  const success = db.setDefaultAddress(req.user!.id, addressId);
  if (!success) {
    sendError(res, 'Address not found', 'NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, { isDefault: true }, 'Default address updated');
});

export default router;
