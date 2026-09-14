import { Router, Request, Response } from 'express';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { db } from '../lib/db.js';

const router = Router();

// GET /api/products/featured (Must come before /:id)
router.get('/products/featured', (_req: Request, res: Response) => {
  const result = db.getProducts({ featured: true, limit: 10 });
  sendSuccess(res, result.products);
});

// GET /api/products/recommended
router.get('/products/recommended', (_req: Request, res: Response) => {
  const result = db.getProducts({ limit: 10 });
  sendSuccess(res, result.products);
});

// GET /api/products/category/:categoryId
router.get('/products/category/:categoryId', (req: Request, res: Response) => {
  const result = db.getProducts({ categoryId: String(req.params.categoryId), limit: 30 });
  sendSuccess(res, result.products);
});

// GET /api/products
router.get('/products', (req: Request, res: Response) => {
  const page = parseInt(req.query.page as string) || 1;
  const limit = parseInt(req.query.limit as string) || 20;
  const search = (req.query.search as string) || (req.query.q as string);
  const categoryId = req.query.category as string;
  const brand = req.query.brand as string;
  const sort = req.query.sort as string;

  const result = db.getProducts({
    page,
    limit,
    search,
    categoryId,
    brand,
    sort,
  });

  sendSuccess(res, {
    products: result.products,
    pagination: {
      total: result.total,
      page: result.page,
      limit: result.limit,
      totalPages: Math.ceil(result.total / limit),
    },
  });
});

// GET /api/products/:id
router.get('/products/:id', (req: Request, res: Response) => {
  const product = db.getProductById(String(req.params.id));
  if (!product) {
    sendError(res, 'Product not found', 'PRODUCT_NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, product);
});

// GET /api/search?q=
router.get('/search', (req: Request, res: Response) => {
  const q = (req.query.q as string) || '';
  if (!q.trim()) {
    sendSuccess(res, { products: [], categories: [], query: q });
    return;
  }

  const { products } = db.getProducts({ search: q, limit: 20 });
  const allCategories = db.getCategories();
  const matchingCategories = allCategories.filter(c =>
    c.name.toLowerCase().includes(q.toLowerCase())
  );

  sendSuccess(res, {
    query: q,
    products,
    categories: matchingCategories,
  });
});

// GET /api/search/suggestions?q=
router.get('/search/suggestions', (req: Request, res: Response) => {
  const q = ((req.query.q as string) || '').toLowerCase();
  const sampleSuggestions = [
    'Classmate single line notebook',
    'Hauser XO blue ball pen',
    'Camlin geometry box',
    'DOMS brush pens',
    'Apsara platinum pencils',
    'Faber-Castell pastel highlighters',
    'Sticky notes 3M',
    'Exam writing pad clipboard',
    'Transparent exam pouch',
    'Cello gel pens',
  ];

  const suggestions = sampleSuggestions.filter(s => s.toLowerCase().includes(q)).slice(0, 5);
  sendSuccess(res, suggestions);
});

// GET /api/wishlist
router.get('/wishlist', requireAuth(), (req: Request, res: Response) => {
  const wishlist = db.getWishlist(req.user!.id);
  sendSuccess(res, wishlist);
});

// POST /api/wishlist/:productId
router.post('/wishlist/:productId', requireAuth(), (req: Request, res: Response) => {
  const success = db.addToWishlist(req.user!.id, String(req.params.productId));
  if (!success) {
    sendError(res, 'Product not found', 'PRODUCT_NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, { added: true }, 'Added to wishlist');
});

// DELETE /api/wishlist/:productId
router.delete('/wishlist/:productId', requireAuth(), (req: Request, res: Response) => {
  db.removeFromWishlist(req.user!.id, String(req.params.productId));
  sendSuccess(res, { removed: true }, 'Removed from wishlist');
});

export default router;
