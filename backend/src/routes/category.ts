import { Router, Request, Response } from 'express';
import { sendSuccess, sendError } from '../lib/response.js';
import { db } from '../lib/db.js';

const router = Router();

// GET /api/categories
router.get('/categories', (_req: Request, res: Response) => {
  const categories = db.getCategories();
  sendSuccess(res, categories);
});

// GET /api/categories/:id
router.get('/categories/:id', (req: Request, res: Response) => {
  const category = db.getCategoryById(String(req.params.id));
  if (!category) {
    sendError(res, 'Category not found', 'CATEGORY_NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, category);
});

// GET /api/categories/:id/subcategories
router.get('/categories/:id/subcategories', (req: Request, res: Response) => {
  const subcategories = db.getSubcategories(String(req.params.id));
  sendSuccess(res, subcategories);
});

// GET /api/subcategories
router.get('/subcategories', (_req: Request, res: Response) => {
  const subcategories = db.getSubcategories();
  sendSuccess(res, subcategories);
});

export default router;
