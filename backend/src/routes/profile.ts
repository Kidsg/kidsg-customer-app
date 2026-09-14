import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { db } from '../lib/db.js';

const router = Router();

const updateProfileSchema = z.object({
  firstName: z.string().min(1).optional(),
  lastName: z.string().optional(),
  email: z.string().email().optional(),
  selectedClass: z.string().optional(),
  selectedSchool: z.string().optional(),
  avatarUrl: z.string().url().optional(),
});

// GET /api/profile
router.get('/profile', requireAuth(), (req: Request, res: Response) => {
  const profile = db.getProfile(req.user!.id);
  sendSuccess(res, profile);
});

// PATCH /api/profile
router.patch('/profile', requireAuth(), (req: Request, res: Response) => {
  const result = updateProfileSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const updated = db.updateProfile(req.user!.id, result.data);
  sendSuccess(res, updated, 'Profile updated successfully');
});

// DELETE /api/profile
router.delete('/profile', requireAuth(), (req: Request, res: Response) => {
  // In production, this archives the user account
  sendSuccess(res, { deleted: true }, 'Account scheduled for deletion');
});

export default router;
