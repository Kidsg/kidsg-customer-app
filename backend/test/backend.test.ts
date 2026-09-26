import { describe, it, expect, beforeEach } from 'vitest';
import { db } from '../src/lib/db.js';
import { MockOtpService } from '../src/services/otp/MockOtpService.js';
import { MockPaymentService } from '../src/services/payment/MockPaymentService.js';
import { MockDeliveryTrackingService } from '../src/services/delivery/DeliveryTrackingService.js';

describe('KidsG Master Backend Suite', () => {
  const testUserId = 'user_test_aarav';
  const testAddressId = 'addr_test_1';

  beforeEach(() => {
    db.clearCart(testUserId);
  });

  describe('1. Authentication & OTP Service', () => {
    it('generates, logs, and verifies 6-digit OTP correctly', async () => {
      const otpService = new MockOtpService();
      const phone = '+919988776655';

      const testCode = '849201';
      const sendRes = await otpService.sendOtp(phone, testCode);
      expect(sendRes.success).toBe(true);
      expect(sendRes.expiresInSeconds).toBe(300);

      // Verify wrong OTP fails
      const badVerify = await otpService.verifyOtp(phone, '000000');
      expect(badVerify.success).toBe(false);

      // Verify 123456 does NOT bypass
      const bypassVerify = await otpService.verifyOtp(phone, '123456');
      expect(bypassVerify.success).toBe(false);

      // Verify actual OTP succeeds
      const goodVerify = await otpService.verifyOtp(phone, testCode);
      expect(goodVerify.success).toBe(true);
    });
  });

  describe('2. Authoritative Price & Price Tampering Prevention', () => {
    it('CRITICAL: Ignores client-supplied prices and strictly uses database prices', () => {
      const product = db.getProductById('prod_doms_brush_pens_14')!;
      expect(product).toBeDefined();
      expect(product.price).toBe(199);

      // Add to cart with quantity 2
      const addRes = db.addToCart(testUserId, product.id, 2);
      expect(addRes.success).toBe(true);

      const cart = db.getCart(testUserId);
      // Even if an attacker modified the client payload to price=1, cart subtotal must be 199 * 2 = 398
      expect(cart.subtotal).toBe(199 * 2);
      expect(cart.totalItems).toBe(2);
    });

    it('validates stock availability when adding or updating items', () => {
      const product = db.getProductById('prod_milton_thermosteel_bottle')!;
      expect(product).toBeDefined();

      // Attempt to add more than available stock (stock is 25)
      const resExceed = db.addToCart(testUserId, product.id, 9999);
      expect(resExceed.success).toBe(false);
      expect(resExceed.error).toContain('stock');
    });
  });

  describe('3. Coupon Validation & Business Rules', () => {
    it('applies flat discount when minimum order value is satisfied', () => {
      // Subtotal = 200 (satisfies KIDSG50 minOrderValue 199)
      const val = db.validateCoupon('KIDSG50', 200);
      expect(val.valid).toBe(true);
      expect(val.discount).toBe(50);
    });

    it('rejects coupon when minimum order value is not met', () => {
      // Subtotal = 100 (< 199 required for KIDSG50)
      const val = db.validateCoupon('KIDSG50', 100);
      expect(val.valid).toBe(false);
      expect(val.discount).toBe(0);
      expect(val.message).toContain('minimum order value');
    });

    it('applies percentage coupon with maximum discount cap', () => {
      // EXAMREADY: 15% off, max ₹75, min order ₹249
      const val = db.validateCoupon('EXAMREADY', 1000); // 15% of 1000 is 150 -> capped at 75
      expect(val.valid).toBe(true);
      expect(val.discount).toBe(75);
    });
  });

  describe('4. Server-Side Checkout Calculation', () => {
    it('computes free delivery when subtotal meets or exceeds FREE_DELIVERY_THRESHOLD (₹199)', () => {
      // Add product of price ₹199
      db.addToCart(testUserId, 'prod_doms_brush_pens_14', 1);

      const checkout = db.calculateCheckout(testUserId);
      expect(checkout.subtotal).toBe(199);
      expect(checkout.deliveryFee).toBe(0);
      expect(checkout.tax).toBe(0);
      expect(checkout.total).toBe(199);
    });

    it('adds delivery fee (₹30) when subtotal is below ₹199', () => {
      // Add single book of price ₹95
      db.addToCart(testUserId, 'prod_classmate_single_line', 1);

      const checkout = db.calculateCheckout(testUserId);
      expect(checkout.subtotal).toBe(95);
      expect(checkout.deliveryFee).toBe(30);
      expect(checkout.total).toBe(95 + 30);
    });

    it('applies coupon discount server-side during checkout preview', () => {
      // Add item worth ₹380
      db.addToCart(testUserId, 'prod_milton_thermosteel_bottle', 1);

      const checkout = db.calculateCheckout(testUserId, 'KIDSG50');
      expect(checkout.subtotal).toBe(380);
      expect(checkout.couponDiscount).toBe(50);
      expect(checkout.deliveryFee).toBe(0); // >= 199
      expect(checkout.total).toBe(380 - 50); // 330
    });
  });

  describe('5. Order Creation & Snapshots', () => {
    it('creates order with immutable snapshots and resets cart', () => {
      // Set up address
      const addr = db.addAddress(testUserId, {
        label: 'Home',
        name: 'Aarav',
        phone: '+919876543210',
        addressLine1: '12th Cross',
        addressLine2: '4th Block',
        city: 'Bengaluru',
        state: 'Karnataka',
        postalCode: '560034',
        isDefault: true,
      });

      // Add item to bag
      db.addToCart(testUserId, 'prod_camlin_scholar_geometry', 1); // ₹135

      const orderRes = db.createOrder(testUserId, addr.id, 'UPI', undefined);
      expect(orderRes.success).toBe(true);
      expect(orderRes.order).toBeDefined();

      const order = orderRes.order!;
      expect(order.orderNumber).toMatch(/^KG-\d{4}-\d{6}$/);
      expect(order.status).toBe('CONFIRMED');
      expect(order.items.length).toBe(1);
      expect(order.items[0].productName).toContain('Camlin');
      expect(order.items[0].price).toBe(135);
      expect(order.addressSnapshot.addressLine1).toBe('12th Cross');

      // Verify bag is now cleared
      const emptyCart = db.getCart(testUserId);
      expect(emptyCart.items.length).toBe(0);
    });
  });

  describe('6. Order State Machine Transitions', () => {
    it('enforces legal transitions and rejects invalid state jumps', () => {
      const addr = db.getAddresses('user_dev_default')[0];
      db.addToCart('user_dev_default', 'prod_hauser_xo_ball', 1);
      const { order } = db.createOrder('user_dev_default', addr.id);
      expect(order).toBeDefined();

      // Current is CONFIRMED. Legal next: PREPARING.
      const validTrans = db.transitionOrderStatus(order!.id, 'PREPARING');
      expect(validTrans.success).toBe(true);
      expect(validTrans.order?.status).toBe('PREPARING');

      // Illegal jump: PREPARING directly to DELIVERED without PICKED_UP or OUT_FOR_DELIVERY
      const illegalTrans = db.transitionOrderStatus(order!.id, 'DELIVERED');
      expect(illegalTrans.success).toBe(false);
      expect(illegalTrans.error).toContain('Invalid state transition');
    });
  });

  describe('7. Customer Isolation & Permissions', () => {
    it('prevents user B from viewing orders belonging to user A', () => {
      const addr = db.addAddress('user_A', {
        label: 'Home',
        name: 'User A',
        phone: '+919999988888',
        addressLine1: 'User A St',
        addressLine2: '',
        city: 'Bengaluru',
        state: 'Karnataka',
        postalCode: '560034',
        isDefault: true,
      });
      db.addToCart('user_A', 'prod_hauser_xo_ball', 1);
      const { order } = db.createOrder('user_A', addr.id);
      expect(order).toBeDefined();

      // User B tries to get user A's order
      const orderForB = db.getOrderById(order!.id, 'user_B');
      expect(orderForB).toBeUndefined();

      // User A can access it
      const orderForA = db.getOrderById(order!.id, 'user_A');
      expect(orderForA).toBeDefined();
    });
  });

  describe('8. Mock Payment & Mock Delivery Tracking', () => {
    it('creates and verifies payment via MockPaymentService', async () => {
      const paymentService = new MockPaymentService();
      const intent = await paymentService.createPayment('ord_test_123', 250);

      expect(intent.paymentId).toMatch(/^pay_mock_/);
      expect(intent.amount).toBe(250);

      const verification = await paymentService.verifyPayment('ord_test_123', intent.paymentId);
      expect(verification.success).toBe(true);
      expect(verification.status).toBe('SUCCESS');
    });

    it('returns deterministic delivery status steps with MockDeliveryTrackingService', async () => {
      const trackingService = new MockDeliveryTrackingService();
      const tracking = await trackingService.getTracking('ord_test_123', new Date().toISOString(), 'CONFIRMED');

      expect(tracking.storeName).toBe('Vidya Stationery Depot');
      expect(tracking.statusHistory.length).toBe(6);
      expect(tracking.statusHistory[0].status).toBe('CONFIRMED');
      expect(tracking.statusHistory[0].completed).toBe(true);
      expect(tracking.statusHistory[5].status).toBe('DELIVERED');
      expect(tracking.statusHistory[5].completed).toBe(false);
    });
  });

  describe('9. Complete Production End-to-End Verification (Part 30 Requirements)', () => {
    const cleanEmail = `test_student_${Date.now()}@kidsg.in`;
    const cleanPassword = 'StrongStudentPassword2026!';
    let registeredUserId = '';

    it('TEST 1: New email -> Signup -> Profile created -> Authenticated', () => {
      const emailCheck = db.checkEmailExists(cleanEmail);
      expect(emailCheck.exists).toBe(false);

      const signupRes = db.registerUser({
        email: cleanEmail,
        password: cleanPassword,
        firstName: 'Devika',
        lastName: 'Menon',
        phone: '+919876543299',
        selectedClass: 'Class 8',
        selectedSchool: 'Delhi Public School',
      });
      expect(signupRes.success).toBe(true);
      expect(signupRes.user.email).toBe(cleanEmail);
      expect(signupRes.user.firstName).toBe('Devika');
      registeredUserId = signupRes.user.id;
    });

    it('TEST 2: Existing email -> Continue -> Password authentication -> Success', () => {
      const check = db.checkEmailExists(cleanEmail);
      expect(check.exists).toBe(true);
      expect(check.firstName).toBe('Devika');

      const loginRes = db.authenticateWithPassword(cleanEmail, cleanPassword);
      expect(loginRes.success).toBe(true);
      expect(loginRes.user.id).toBe(registeredUserId);
    });

    it('TEST 3: Existing email -> OTP authentication -> Success', async () => {
      const otpService = new MockOtpService();
      const code = '736291';
      await otpService.sendOtp(cleanEmail, code);
      const verifyRes = await otpService.verifyOtp(cleanEmail, code);
      expect(verifyRes.success).toBe(true);
    });

    it('TEST 4 & 5: Wrong password and random credentials -> Rejected', () => {
      const wrongPass = db.authenticateWithPassword(cleanEmail, 'WrongPassword123');
      expect(wrongPass.success).toBe(false);

      const randomUser = db.authenticateWithPassword('random_nobody@unknown.com', 'SomePassword');
      expect(randomUser.success).toBe(false);
    });

    it('TEST 6: password = 123456 -> Must NOT authenticate', () => {
      const bypassAttempt = db.authenticateWithPassword(cleanEmail, '123456');
      expect(bypassAttempt.success).toBe(false);
    });

    it('TEST 7: Add products -> Cart persists per user', () => {
      db.clearCart(registeredUserId);
      db.addToCart(registeredUserId, 'prod_classmate_single_line', 2);
      db.addToCart(registeredUserId, 'prod_doms_brush_pens_14', 1);

      const cart = db.getCart(registeredUserId);
      expect(cart.totalItems).toBe(3);
      expect(cart.items.length).toBe(2);
    });

    it('TEST 8: Checkout -> Payment created -> Order created -> Items snapshot created -> Status history created', () => {
      const addr = db.addAddress(registeredUserId, {
        userId: registeredUserId,
        label: 'Home',
        name: 'Devika Menon',
        phone: '+919876543299',
        addressLine1: 'Villa 14, Palm Meadows',
        addressLine2: 'Whitefield',
        city: 'Bengaluru',
        state: 'Karnataka',
        postalCode: '560066',
        isDefault: true,
      });

      const { success, order } = db.createOrder(registeredUserId, addr.id, 'UPI');
      expect(success).toBe(true);
      expect(order).toBeDefined();
      expect(order!.status).toBe('CONFIRMED');
      expect(order!.items.length).toBe(2);
      expect(order!.items[0].productName).toBeDefined();
      expect(order!.items[0].price).toBeGreaterThan(0);

      // Verify payment record persisted
      const payments = db.getPaymentsForOrder(order!.id);
      expect(payments.length).toBe(1);
      expect(payments[0].status).toBe('SUCCESS');
      expect(payments[0].provider).toBe('MOCK');

      // Verify status history recorded
      const history = db.getOrderStatusHistory(order!.id);
      expect(history.length).toBe(1);
      expect(history[0].status).toBe('CONFIRMED');

      // Verify cart was cleared
      expect(db.getCart(registeredUserId).items.length).toBe(0);
    });

    it('TEST 9-19: Complete Order Lifecycle (Shop Acceptance -> Packing -> Ready -> Delivery Assigned -> Picked Up -> Out for Delivery -> Delivered)', () => {
      const orders = db.getOrders(registeredUserId);
      expect(orders.length).toBeGreaterThan(0);
      const order = orders[0];

      // TEST 9: Shop owner receives order
      const shopOrders = db.getShopOrders(order.storeId, 'NEW');
      expect(shopOrders.some(o => o.id === order.id)).toBe(true);

      // TEST 10 & 11: Shop owner accepts order -> STORE_ACCEPTED
      const acceptRes = db.shopAcceptOrder(order.id);
      expect(acceptRes.success).toBe(true);
      expect(db.getOrderById(order.id, registeredUserId)!.status).toBe('STORE_ACCEPTED');

      // TEST 12 & 13: Shop owner marks packing -> PREPARING
      const packRes = db.shopStartPacking(order.id);
      expect(packRes.success).toBe(true);
      expect(db.getOrderById(order.id, registeredUserId)!.status).toBe('PREPARING');

      // TEST 14: Shop owner marks ready for pickup -> READY_FOR_PICKUP
      const readyRes = db.shopReadyForPickup(order.id);
      expect(readyRes.success).toBe(true);
      expect(db.getOrderById(order.id, registeredUserId)!.status).toBe('READY_FOR_PICKUP');

      // TEST 15 & 16 & 17: Mock delivery progresses -> PICKED_UP -> OUT_FOR_DELIVERY
      db.advanceDeliveryStatus(order.id, 'PICKED_UP');
      expect(db.getOrderById(order.id, registeredUserId)!.status).toBe('PICKED_UP');

      db.advanceDeliveryStatus(order.id, 'OUT_FOR_DELIVERY');
      expect(db.getOrderById(order.id, registeredUserId)!.status).toBe('OUT_FOR_DELIVERY');

      // TEST 18 & 19: Delivery marked DELIVERED
      db.advanceDeliveryStatus(order.id, 'DELIVERED');
      expect(db.getOrderById(order.id, registeredUserId)!.status).toBe('DELIVERED');

      // TEST 20: Complete tracking timeline is persisted in database
      const fullHistory = db.getOrderStatusHistory(order.id);
      expect(fullHistory.length).toBe(7); // CONFIRMED -> STORE_ACCEPTED -> PREPARING -> READY_FOR_PICKUP -> PICKED_UP -> OUT_FOR_DELIVERY -> DELIVERED
    });
  });
});
