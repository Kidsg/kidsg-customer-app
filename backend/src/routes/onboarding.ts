import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { db } from '../lib/db.js';

const router = Router();

const onboardingCompleteSchema = z.object({
  selectedClass: z.string().min(1, 'Class/Standard is required'),
  selectedSchool: z.string().min(2, 'School name is required'),
  preferredCategories: z.array(z.string()).optional(),
});

const locationSchema = z.object({
  latitude: z.number(),
  longitude: z.number(),
  address: z.string().optional(),
  city: z.string().optional(),
  postalCode: z.string().optional(),
});

// GET /api/onboarding
router.get('/onboarding', requireAuth(), (req: Request, res: Response) => {
  const profile = db.getProfile(req.user!.id);
  sendSuccess(res, {
    onboardingCompleted: profile.onboardingCompleted || false,
    selectedClass: profile.selectedClass || null,
    selectedSchool: profile.selectedSchool || null,
  });
});

// POST /api/onboarding/complete
router.post('/onboarding/complete', requireAuth(), (req: Request, res: Response) => {
  const result = onboardingCompleteSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { selectedClass, selectedSchool } = result.data;
  const updated = db.updateProfile(req.user!.id, {
    selectedClass,
    selectedSchool,
    onboardingCompleted: true,
  });

  sendSuccess(res, {
    onboardingCompleted: true,
    profile: updated,
  }, 'Onboarding completed successfully');
});

// POST /api/location
router.post('/location', requireAuth(), (req: Request, res: Response) => {
  const result = locationSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, 'Valid coordinates required', 'VALIDATION_ERROR', 400);
    return;
  }

  const locationData = {
    ...result.data,
    city: result.data.city || 'Bengaluru',
    postalCode: result.data.postalCode || '560034',
  };

  db.updateProfile(req.user!.id, { lastLocation: locationData });
  sendSuccess(res, locationData, 'Location updated successfully');
});

// GET /api/location/current
router.get('/location/current', requireAuth(), (req: Request, res: Response) => {
  const profile = db.getProfile(req.user!.id);
  const location = profile.lastLocation || {
    latitude: 12.9352,
    longitude: 77.6245,
    address: 'Koramangala, Bengaluru',
    city: 'Bengaluru',
    postalCode: '560034',
  };

  sendSuccess(res, location);
});

export default router;
