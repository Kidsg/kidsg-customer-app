import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { randomUUID } from 'crypto';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireRole } from '../middleware/auth.js';
import { db } from '../lib/db.js';
import { getNotificationService } from '../services/notification/NotificationService.js';

const router = Router();
const notificationService = getNotificationService();

// Validation Schemas
const adminLoginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
});

const createShopSchema = z.object({
  name: z.string().min(2),
  code: z.string().optional(),
  ownerName: z.string().min(2),
  phone: z.string().min(10),
  email: z.string().email().optional(),
  address: z.string().min(5),
  locality: z.string().optional(),
  area: z.string().optional(),
  city: z.string().default('Bengaluru'),
  state: z.string().default('Karnataka'),
  pincode: z.string().min(6),
  landmark: z.string().optional(),
  latitude: z.number(),
  longitude: z.number(),
  deliveryRadiusKm: z.number().default(6.0),
  openTime: z.string().default('08:00'),
  closeTime: z.string().default('21:30'),
  businessHours: z.any().optional(),
  categoriesSold: z.array(z.string()).optional(),
  founderNotes: z.string().optional(),
  verificationStatus: z.enum(['ACTIVE', 'INACTIVE', 'PENDING_REVIEW']).default('ACTIVE'),
});

const updateInventorySchema = z.object({
  price: z.number().positive().optional(),
  pricePaise: z.number().positive().optional(),
  mrp: z.number().positive().optional(),
  mrpPaise: z.number().positive().optional(),
  discountPercent: z.number().min(0).max(100).optional(),
  quantity: z.number().min(0, 'Quantity cannot be negative').optional(),
  reservedQuantity: z.number().min(0).optional(),
  lowStockThreshold: z.number().min(0).optional(),
  isAvailable: z.boolean().optional(),
  costPrice: z.number().positive().optional(),
  costPricePaise: z.number().positive().optional(),
  founderNotes: z.string().optional(),
});

const createProductSchema = z.object({
  name: z.string().min(2),
  brand: z.string().default('BookBuddys Partner'),
  categoryId: z.string(),
  subcategoryId: z.string().optional(),
  description: z.string().optional(),
  imageUrl: z.string().url(),
  galleryImages: z.array(z.string()).optional(),
  price: z.number().positive(),
  mrp: z.number().positive(),
  discountPercent: z.number().min(0).max(100).default(0),
  stock: z.number().min(0).default(20),
  unit: z.string().default('piece'),
  gradeLevel: z.string().default('All'),
  isFeatured: z.boolean().default(false),
  isActive: z.boolean().default(true),
  sku: z.string().optional(),
  badgeText: z.string().optional(),
  specs: z.record(z.string()).optional(),
});

// ============================================================================
// 1. FOUNDER / ADMIN AUTHENTICATION
// ============================================================================

// POST /api/admin/auth/login
router.post('/admin/auth/login', async (req: Request, res: Response) => {
  const parsed = adminLoginSchema.safeParse(req.body);
  if (!parsed.success) {
    sendError(res, parsed.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { email, password } = parsed.data;
  const authRes = db.authenticateWithPassword(email.toLowerCase(), password);

  if (!authRes.success || !authRes.user) {
    sendError(res, 'Invalid founder credentials or password', 'INVALID_CREDENTIALS', 401);
    return;
  }

  if (authRes.user.role !== 'ADMIN') {
    sendError(res, 'Access denied: Admin/Founder credentials required', 'FORBIDDEN', 403);
    return;
  }

  const token = `kidsg-jwt-${authRes.user.id}`;

  db.addAuditLog({
    adminId: authRes.user.id,
    adminName: `${authRes.user.firstName} ${authRes.user.lastName}`,
    action: 'ADMIN_LOGIN',
    entityType: 'SETTINGS',
    entityId: authRes.user.id,
    details: { email },
    ipAddress: req.ip,
    userAgent: req.headers['user-agent'],
  });

  sendSuccess(res, {
    token,
    user: {
      id: authRes.user.id,
      email: authRes.user.email,
      firstName: authRes.user.firstName,
      lastName: authRes.user.lastName,
      role: authRes.user.role,
    },
    message: 'Founder authentication successful',
  });
});

// ============================================================================
// 2. DASHBOARD OVERVIEW & METRICS
// ============================================================================

// GET /api/admin/overview
router.get('/admin/overview', requireRole('ADMIN'), (_req: Request, res: Response) => {
  const overview = db.getAdminOverview();
  sendSuccess(res, overview);
});

// ============================================================================
// 3. SHOP MANAGEMENT
// ============================================================================

// GET /api/admin/shops
router.get('/admin/shops', requireRole('ADMIN'), (_req: Request, res: Response) => {
  const shops = db.getStoresAdmin();
  sendSuccess(res, shops);
});

// POST /api/admin/shops
router.post('/admin/shops', requireRole('ADMIN'), (req: Request, res: Response) => {
  const parsed = createShopSchema.safeParse(req.body);
  if (!parsed.success) {
    sendError(res, parsed.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const adminId = req.user?.id || 'user_founder_01';
  const newShop = db.createStore(parsed.data, adminId);
  sendSuccess(res, newShop, 'Shop created successfully', 201);
});

// GET /api/admin/shops/:id
router.get('/admin/shops/:id', requireRole('ADMIN'), (req: Request, res: Response) => {
  const shopId = String(req.params.id);
  const shop = db.getStoreByIdAdmin(shopId);
  if (!shop) {
    sendError(res, 'Shop not found', 'NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, shop);
});

// PUT /api/admin/shops/:id
router.put('/admin/shops/:id', requireRole('ADMIN'), (req: Request, res: Response) => {
  const shopId = String(req.params.id);
  const adminId = req.user?.id || 'user_founder_01';
  const updated = db.updateStore(shopId, req.body, adminId);
  if (!updated) {
    sendError(res, 'Shop not found', 'NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, updated);
});

// PATCH /api/admin/shops/:id
router.patch('/admin/shops/:id', requireRole('ADMIN'), (req: Request, res: Response) => {
  const shopId = String(req.params.id);
  const adminId = req.user?.id || 'user_founder_01';
  const updated = db.updateStore(shopId, req.body, adminId);
  if (!updated) {
    sendError(res, 'Shop not found', 'NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, updated);
});

// DELETE /api/admin/shops/:id (Deactivate)
router.delete('/admin/shops/:id', requireRole('ADMIN'), (req: Request, res: Response) => {
  const shopId = String(req.params.id);
  const adminId = req.user?.id || 'user_founder_01';
  const updated = db.toggleStoreStatus(shopId, false, adminId);
  if (!updated) {
    sendError(res, 'Shop not found', 'NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, { message: 'Shop successfully deactivated', shop: updated });
});

// ============================================================================
// 4. SHOP-SPECIFIC INVENTORY MANAGEMENT
// ============================================================================

// GET /api/admin/shops/:shopId/inventory
router.get('/admin/shops/:shopId/inventory', requireRole('ADMIN'), (req: Request, res: Response) => {
  const shopId = String(req.params.shopId);
  const inventory = db.getStoreInventory(shopId);
  sendSuccess(res, inventory);
});

// POST /api/admin/shops/:shopId/inventory
router.post('/admin/shops/:shopId/inventory', requireRole('ADMIN'), (req: Request, res: Response) => {
  const shopId = String(req.params.shopId);
  const productId = req.body.productId;
  if (!productId) {
    sendError(res, 'productId is required', 'VALIDATION_ERROR', 400);
    return;
  }
  const parsed = updateInventorySchema.safeParse(req.body);
  if (!parsed.success) {
    sendError(res, parsed.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const adminId = req.user?.id || 'user_founder_01';
  const updated = db.updateStoreInventoryItem(shopId, productId, parsed.data, adminId);
  sendSuccess(res, updated);
});

// PUT /api/admin/shops/:shopId/inventory/:productId
router.put('/admin/shops/:shopId/inventory/:productId', requireRole('ADMIN'), (req: Request, res: Response) => {
  const shopId = String(req.params.shopId);
  const productId = String(req.params.productId);
  const parsed = updateInventorySchema.safeParse(req.body);
  if (!parsed.success) {
    sendError(res, parsed.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const adminId = req.user?.id || 'user_founder_01';
  const updated = db.updateStoreInventoryItem(shopId, productId, parsed.data, adminId);
  sendSuccess(res, updated);
});

// POST /api/admin/shops/:shopId/inventory/bulk
router.post('/admin/shops/:shopId/inventory/bulk', requireRole('ADMIN'), (req: Request, res: Response) => {
  const shopId = String(req.params.shopId);
  const items = req.body.items;
  if (!Array.isArray(items)) {
    sendError(res, 'Expected array of inventory items', 'VALIDATION_ERROR', 400);
    return;
  }

  const result = db.bulkUpdateStoreInventory(shopId, items);
  sendSuccess(res, result);
});

// POST /api/admin/shops/:shopId/inventory/bulk-csv
router.post('/admin/shops/:shopId/inventory/bulk-csv', requireRole('ADMIN'), (req: Request, res: Response) => {
  const shopId = String(req.params.shopId);
  const items = req.body.items;
  if (!Array.isArray(items)) {
    sendError(res, 'Expected array of inventory items', 'VALIDATION_ERROR', 400);
    return;
  }

  const result = db.bulkUpdateStoreInventory(shopId, items);
  sendSuccess(res, result);
});

// ============================================================================
// 5. PRODUCT CATALOGUE MANAGEMENT
// ============================================================================

// GET /api/admin/products
router.get('/admin/products', requireRole('ADMIN'), (_req: Request, res: Response) => {
  const prods = db.getAllProductsAdmin();
  sendSuccess(res, prods);
});

// POST /api/admin/products
router.post('/admin/products', requireRole('ADMIN'), (req: Request, res: Response) => {
  const parsed = createProductSchema.safeParse(req.body);
  if (!parsed.success) {
    sendError(res, parsed.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const adminId = req.user?.id || 'user_founder_01';
  const newProd = db.createProduct(parsed.data, adminId);
  sendSuccess(res, newProd, 'Product created successfully', 201);
});

// PUT /api/admin/products/:id
router.put('/admin/products/:id', requireRole('ADMIN'), (req: Request, res: Response) => {
  const prodId = String(req.params.id);
  const adminId = req.user?.id || 'user_founder_01';
  const updated = db.updateProduct(prodId, req.body, adminId);
  if (!updated) {
    sendError(res, 'Product not found', 'NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, updated);
});

// DELETE /api/admin/products/:id
router.delete('/admin/products/:id', requireRole('ADMIN'), (req: Request, res: Response) => {
  const prodId = String(req.params.id);
  const adminId = req.user?.id || 'user_founder_01';
  const updated = db.toggleProductStatus(prodId, false, adminId);
  if (!updated) {
    sendError(res, 'Product not found', 'NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, { message: 'Product deactivated', product: updated });
});

// ============================================================================
// 6. ORDER WORKFLOW MANAGEMENT (10-STEP LIFECYCLE)
// ============================================================================

// GET /api/admin/orders
router.get('/admin/orders', requireRole('ADMIN'), (req: Request, res: Response) => {
  const status = req.query.status as string | undefined;
  const search = req.query.search as string | undefined;
  const storeId = req.query.storeId as string | undefined;

  const orders = db.getAdminOrders({ status, search, storeId });
  sendSuccess(res, orders);
});

// GET /api/admin/orders/:id
router.get('/admin/orders/:id', requireRole('ADMIN'), (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const order = db.getOrderByIdAdmin(orderId);
  if (!order) {
    sendError(res, 'Order not found', 'NOT_FOUND', 404);
    return;
  }

  const history = db.getOrderStatusHistory(order.id);
  const payments = db.getPaymentSubmissionsForOrder(order.id);
  const booking = db.getDeliveryBookingForOrder(order.id);

  sendSuccess(res, {
    order,
    history,
    paymentSubmissions: payments,
    deliveryBooking: booking,
  });
});

// Step 3: Confirm shop availability
router.post('/admin/orders/:id/availability', requireRole('ADMIN'), async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const action = req.body.action as 'CONFIRM' | 'REJECT' | 'SUBSTITUTE';
  const notes = req.body.notes as string | undefined;
  const adminId = req.user?.id || 'user_founder_01';

  if (!action || !['CONFIRM', 'REJECT', 'SUBSTITUTE'].includes(action)) {
    sendError(res, 'Invalid availability action. Must be CONFIRM, REJECT, or SUBSTITUTE', 'VALIDATION_ERROR', 400);
    return;
  }

  const result = db.confirmOrderAvailability(orderId, action, notes, adminId);
  if (!result.success) {
    sendError(res, result.error || 'Failed to update order availability', 'STATE_ERROR', 400);
    return;
  }

  if (result.order && action === 'CONFIRM') {
    await notificationService.send(
      result.order.userId,
      'SHOP_CONFIRMED',
      'Stock Confirmed! 🎒',
      `Partner store has confirmed stock for your order ${result.order.orderNumber}.`,
      { orderId: result.order.id }
    );
  }

  sendSuccess(res, { message: `Shop availability marked as ${action}`, order: result.order });
});

// Step 4: Accept order & request payment
router.post('/admin/orders/:id/accept-payment-due', requireRole('ADMIN'), async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || 'user_founder_01';
  const notes = req.body.notes as string | undefined;

  const result = db.acceptOrderRequestPayment(orderId, adminId, notes);
  if (!result.success) {
    sendError(res, result.error || 'Failed to accept order', 'STATE_ERROR', 400);
    return;
  }

  if (result.order) {
    await notificationService.send(
      result.order.userId,
      'PAYMENT_REQUIRED',
      'Payment Due - Order Accepted 💳',
      `Order ${result.order.orderNumber} is confirmed! Please submit payment of ₹${result.order.total} via UPI.`,
      { orderId: result.order.id, total: result.order.total }
    );
  }

  sendSuccess(res, { message: 'Order accepted. Payment is now requested from customer.', order: result.order });
});

// Step 6: Payment verification queue
router.get('/admin/payments/pending', requireRole('ADMIN'), (_req: Request, res: Response) => {
  const pending = db.getPendingPayments();
  sendSuccess(res, pending);
});

router.post('/admin/payments/:submissionId/verify', requireRole('ADMIN'), async (req: Request, res: Response) => {
  const submissionId = String(req.params.submissionId);
  const action = req.body.action as 'VERIFY' | 'REJECT';
  const reason = req.body.reason as string | undefined;
  const adminId = req.user?.id || 'user_founder_01';

  if (!action || !['VERIFY', 'REJECT'].includes(action)) {
    sendError(res, 'Action must be VERIFY or REJECT', 'VALIDATION_ERROR', 400);
    return;
  }

  const result = db.verifyManualPayment(submissionId, action, reason, adminId);
  if (!result.success) {
    sendError(res, result.error || 'Failed to verify payment', 'VERIFICATION_ERROR', 400);
    return;
  }

  if (result.order) {
    await notificationService.send(
      result.order.userId,
      action === 'VERIFY' ? 'PAYMENT_CONFIRMED' : 'PAYMENT_FAILED',
      action === 'VERIFY' ? 'Payment Verified! ✅' : 'Payment Needs Review ⚠️',
      action === 'VERIFY'
        ? `Payment for order ${result.order.orderNumber} is verified. We are preparing your supplies!`
        : `Payment could not be verified: ${reason || 'Please check transaction reference'}.`,
      { orderId: result.order.id }
    );
  }

  sendSuccess(res, { message: `Payment ${action.toLowerCase()}ed successfully`, order: result.order });
});

// Step 7: Founder purchases & prepares items
router.post('/admin/orders/:id/procure', requireRole('ADMIN'), (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || 'user_founder_01';
  const notes = req.body.notes as string | undefined;

  const result = db.markOrderProcuring(orderId, adminId, notes);
  if (!result.success) {
    sendError(res, result.error || 'Failed to transition to preparing', 'STATE_ERROR', 400);
    return;
  }
  sendSuccess(res, { message: 'Order marked as being procured & packed', order: result.order });
});

router.post('/admin/orders/:id/ready', requireRole('ADMIN'), (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || 'user_founder_01';
  const notes = req.body.notes as string | undefined;

  const result = db.markOrderReadyForPickup(orderId, adminId, notes);
  if (!result.success) {
    sendError(res, result.error || 'Failed to transition to ready for pickup', 'STATE_ERROR', 400);
    return;
  }
  sendSuccess(res, { message: 'Order is ready for delivery pickup', order: result.order });
});

// Step 8: Book delivery
router.post('/admin/orders/:id/book-delivery', requireRole('ADMIN'), (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || 'user_founder_01';
  const result = db.bookDelivery(orderId, req.body, adminId);
  if (!result.success) {
    sendError(res, result.error || 'Failed to book delivery', 'DELIVERY_ERROR', 400);
    return;
  }
  const order = db.getOrderByIdAdmin(orderId);
  sendSuccess(res, { message: 'Delivery recorded successfully', booking: result.booking, order });
});

// Step 9: Update delivery status
router.post('/admin/orders/:id/delivery-status', requireRole('ADMIN'), (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const status = (req.body.status || req.body.deliveryStatus) as 'PICKED_UP' | 'OUT_FOR_DELIVERY' | 'DELIVERED';
  const adminId = req.user?.id || 'user_founder_01';

  if (!status || !['PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED'].includes(status)) {
    sendError(res, 'Invalid delivery status', 'VALIDATION_ERROR', 400);
    return;
  }

  const result = db.updateDeliveryBookingStatus(orderId, status, req.body, adminId);
  if (!result.success) {
    sendError(res, result.error || 'Failed to update delivery status', 'STATE_ERROR', 400);
    return;
  }
  sendSuccess(res, { message: `Delivery status updated to ${status}`, order: result.order });
});

// Alias: /admin/orders/:id/confirm-availability
router.post('/admin/orders/:id/confirm-availability', requireRole('ADMIN'), async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const action = req.body.isAvailable === false ? 'REJECT' : (req.body.action || 'CONFIRM');
  const notes = req.body.notes || req.body.supplierNotes;
  const adminId = req.user?.id || 'user_founder_01';

  const result = db.confirmOrderAvailability(orderId, action as any, notes, adminId);
  if (!result.success) {
    sendError(res, result.error || 'Failed to update order availability', 'STATE_ERROR', 400);
    return;
  }
  sendSuccess(res, { message: 'Shop availability confirmed', order: result.order });
});

// Alias: /admin/orders/:id/accept
router.post('/admin/orders/:id/accept', requireRole('ADMIN'), async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || 'user_founder_01';
  const notes = req.body.notes;

  const result = db.acceptOrderRequestPayment(orderId, adminId, notes);
  if (!result.success) {
    sendError(res, result.error || 'Failed to accept order', 'STATE_ERROR', 400);
    return;
  }
  sendSuccess(res, { message: 'Order accepted', order: result.order });
});

// Alias: /admin/orders/:id/verify-payment
router.post('/admin/orders/:id/verify-payment', requireRole('ADMIN'), async (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const verified = req.body.verified !== false;
  const reason = req.body.notes || req.body.reason;
  const adminId = req.user?.id || 'user_founder_01';

  // Find latest payment submission for this order
  const submissions = db.getPaymentSubmissionsForOrder(orderId);
  if (submissions.length === 0) {
    // If no submission record, create one directly and verify
    const order = db.getOrderByIdAdmin(orderId);
    if (!order) {
      sendError(res, 'Order not found', 'NOT_FOUND', 404);
      return;
    }
    const sub = db.submitManualPayment(orderId, {
      userId: order.userId,
      amount: order.total,
      paymentMethod: order.paymentMethod,
      transactionReference: 'AUTO_VERIFIED',
    });
    const result = db.verifyManualPayment(sub.id, verified ? 'VERIFY' : 'REJECT', reason, adminId);
    sendSuccess(res, { message: 'Payment verified', order: result.order });
    return;
  }

  const latestSub = submissions[0];
  const result = db.verifyManualPayment(latestSub.id, verified ? 'VERIFY' : 'REJECT', reason, adminId);
  if (!result.success) {
    sendError(res, result.error || 'Failed to verify payment', 'VERIFICATION_ERROR', 400);
    return;
  }
  sendSuccess(res, { message: 'Payment verified', order: result.order });
});

// Alias: /admin/orders/:id/mark-ready
router.post('/admin/orders/:id/mark-ready', requireRole('ADMIN'), (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || 'user_founder_01';
  const notes = req.body.notes;

  const result = db.markOrderReadyForPickup(orderId, adminId, notes);
  if (!result.success) {
    sendError(res, result.error || 'Failed to mark ready', 'STATE_ERROR', 400);
    return;
  }
  sendSuccess(res, { message: 'Order ready for delivery', order: result.order });
});

// Alias: /admin/orders/:id/update-delivery-status
router.post('/admin/orders/:id/update-delivery-status', requireRole('ADMIN'), (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const status = (req.body.status || req.body.deliveryStatus) as 'PICKED_UP' | 'OUT_FOR_DELIVERY' | 'DELIVERED';
  const adminId = req.user?.id || 'user_founder_01';

  if (!status || !['PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED'].includes(status)) {
    sendError(res, 'Invalid delivery status', 'VALIDATION_ERROR', 400);
    return;
  }

  const result = db.updateDeliveryBookingStatus(orderId, status, req.body, adminId);
  if (!result.success) {
    sendError(res, result.error || 'Failed to update delivery status', 'STATE_ERROR', 400);
    return;
  }
  sendSuccess(res, { message: `Delivery status updated to ${status}`, order: result.order });
});

// Step 10: Cancellation & Refund
router.post('/admin/orders/:id/cancel', requireRole('ADMIN'), (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const reason = req.body.reason || 'Cancelled by founder';
  const adminId = req.user?.id || 'user_founder_01';

  const result = db.cancelOrderAdmin(orderId, reason, adminId);
  if (!result.success) {
    sendError(res, result.error || 'Failed to cancel order', 'STATE_ERROR', 400);
    return;
  }
  sendSuccess(res, { message: 'Order cancelled', order: result.order });
});

router.post('/admin/orders/:id/refund', requireRole('ADMIN'), (req: Request, res: Response) => {
  const orderId = String(req.params.id);
  const refundRef = req.body.refundRef || `REF-${randomUUID().substring(0, 8)}`;
  const adminId = req.user?.id || 'user_founder_01';

  const result = db.refundOrderAdmin(orderId, refundRef, adminId);
  if (!result.success) {
    sendError(res, result.error || 'Failed to process refund', 'STATE_ERROR', 400);
    return;
  }
  sendSuccess(res, { message: 'Refund recorded successfully', order: result.order });
});

// ============================================================================
// 7. AUDIT LOGS & PLATFORM SETTINGS
// ============================================================================

// GET /api/admin/audit-logs
router.get('/admin/audit-logs', requireRole('ADMIN'), (req: Request, res: Response) => {
  const limit = req.query.limit ? parseInt(req.query.limit as string) : 100;
  const logs = db.getAuditLogs(limit);
  sendSuccess(res, logs);
});

// GET /api/admin/settings
router.get('/admin/settings', requireRole('ADMIN'), (_req: Request, res: Response) => {
  const settings = db.getPlatformSettings();
  sendSuccess(res, settings);
});

// PUT /api/admin/settings
router.put('/admin/settings', requireRole('ADMIN'), (req: Request, res: Response) => {
  const adminId = req.user?.id || 'user_founder_01';
  const updated = db.updatePlatformSettings(req.body, adminId);
  sendSuccess(res, updated);
});

export default router;
