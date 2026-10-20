import { Router, Request, Response } from 'express';
import { sendSuccess, sendError } from '../lib/response.js';
import { db } from '../lib/db.js';

const router = Router();

// GET /api/stores/nearby
router.get('/stores/nearby', (req: Request, res: Response) => {
  const lat = req.query.latitude ? parseFloat(req.query.latitude as string) : 12.9358;
  const lng = req.query.longitude ? parseFloat(req.query.longitude as string) : 77.6251;
  const radius = req.query.radiusKm ? parseFloat(req.query.radiusKm as string) : 12.0;

  const stores = db.findNearbyStores(lat, lng, radius);
  sendSuccess(res, stores);
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
