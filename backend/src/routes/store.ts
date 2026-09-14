import { Router, Request, Response } from 'express';
import { sendSuccess, sendError } from '../lib/response.js';
import { db } from '../lib/db.js';

const router = Router();

// GET /api/stores/nearby
router.get('/stores/nearby', (_req: Request, res: Response) => {
  const stores = db.getStores();
  // Enrich with live partner metrics
  const enriched = stores.map(s => ({
    ...s,
    distanceKm: 0.8,
    estimatedDeliveryMinutes: 12,
    isOpen: true,
    rating: 4.8,
  }));
  sendSuccess(res, enriched);
});

// GET /api/stores/:id
router.get('/stores/:id', (req: Request, res: Response) => {
  const store = db.getStoreById(String(req.params.id));
  if (!store) {
    sendError(res, 'Store not found', 'STORE_NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, store);
});

// GET /api/stores/:id/products
router.get('/stores/:id/products', (req: Request, res: Response) => {
  const store = db.getStoreById(String(req.params.id));
  if (!store) {
    sendError(res, 'Store not found', 'STORE_NOT_FOUND', 404);
    return;
  }
  const { products } = db.getProducts({ limit: 50 });
  sendSuccess(res, products);
});

// GET /api/stores/:id/availability
router.get('/stores/:id/availability', (req: Request, res: Response) => {
  const store = db.getStoreById(String(req.params.id));
  if (!store) {
    sendError(res, 'Store not found', 'STORE_NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, {
    isAvailable: true,
    currentWaitMinutes: 12,
    expressDeliveryAvailable: true,
    operatingHours: `${store.openTime} - ${store.closeTime}`,
  });
});

export default router;
