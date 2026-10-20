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

// GET /api/products/location-eligible (Location-aware shop stock & pricing)
router.get('/products/location-eligible', (req: Request, res: Response) => {
  const lat = req.query.latitude ? parseFloat(req.query.latitude as string) : undefined;
  const lng = req.query.longitude ? parseFloat(req.query.longitude as string) : undefined;
  const categoryId = (req.query.category as string) || (req.query.categoryId as string);
  const search = (req.query.search as string) || (req.query.q as string);

  const result = db.getLocationEligibleProducts(lat, lng, categoryId, search);
  sendSuccess(res, result);
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
  const subcategoryId = req.query.subcategory as string;
  const brand = req.query.brand as string;
  const sort = req.query.sort as string;

  const result = db.getProducts({
    page,
    limit,
    search,
    categoryId,
    subcategoryId,
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

// GET /api/products/:id/reviews
router.get('/products/:id/reviews', (req: Request, res: Response) => {
  const reviews = db.getProductReviews(String(req.params.id));
  sendSuccess(res, reviews);
});

// POST /api/products/:id/reviews
router.post('/products/:id/reviews', (req: Request, res: Response) => {
  const { authorName, authorRole, rating, comment } = req.body;
  if (!comment) {
    sendError(res, 'Comment is required', 'VALIDATION_ERROR', 400);
    return;
  }
  const review = db.addProductReview(String(req.params.id), {
    productId: String(req.params.id),
    authorName: authorName || 'Student Reviewer',
    authorRole: authorRole || 'Student',
    rating: Number(rating) || 5.0,
    dateText: 'Just now',
    comment,
    isVerifiedPurchase: true,
    helpfulCount: 0,
  });
  sendSuccess(res, review, 'Review submitted successfully');
});

// GET /api/products/:id/qa
router.get('/products/:id/qa', (req: Request, res: Response) => {
  const qa = db.getProductQa(String(req.params.id));
  sendSuccess(res, qa);
});

// POST /api/products/:id/qa
router.post('/products/:id/qa', (req: Request, res: Response) => {
  const { question } = req.body;
  if (!question) {
    sendError(res, 'Question is required', 'VALIDATION_ERROR', 400);
    return;
  }
  const newQa = db.addProductQa(String(req.params.id), question);
  sendSuccess(res, newQa, 'Question submitted successfully');
});

// GET /api/products/:id/bundle
router.get('/products/:id/bundle', (req: Request, res: Response) => {
  const bundle = db.getProductBundle(String(req.params.id));
  sendSuccess(res, bundle);
});

// GET /api/products/:id/related
router.get('/products/:id/related', (req: Request, res: Response) => {
  const product = db.getProductById(String(req.params.id));
  if (!product) {
    sendError(res, 'Product not found', 'PRODUCT_NOT_FOUND', 404);
    return;
  }
  const type = (req.query.type as string) || 'similar';
  let related = db.getProducts({ limit: 10 }).products.filter(p => p.id !== product.id);
  if (type === 'brand') {
    related = related.filter(p => p.brand.toLowerCase() === product.brand.toLowerCase());
  } else if (type === 'category') {
    related = related.filter(p => p.categoryId === product.categoryId);
  }
  sendSuccess(res, related.slice(0, 8));
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
