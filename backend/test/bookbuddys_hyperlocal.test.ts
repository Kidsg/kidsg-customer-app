import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest';
import { Server } from 'http';
import app from '../src/server.js';
import { db } from '../src/lib/db.js';

describe('BookBuddys Hyperlocal Platform — Master Test Suite', () => {
  let server: Server;
  let baseUrl: string;

  beforeAll(async () => {
    await new Promise<void>((resolve) => {
      server = app.listen(0, () => {
        const address = server.address();
        if (address && typeof address === 'object') {
          baseUrl = `http://localhost:${address.port}`;
        }
        resolve();
      });
    });
  });

  afterAll(async () => {
    await new Promise<void>((resolve) => {
      server.close(() => resolve());
    });
  });

  beforeEach(() => {
    db.clearTransactionalData();
  });

  describe('1. Security & Founder Authentication', () => {
    it('authenticates founder with correct credentials and returns ADMIN token', async () => {
      const res = await fetch(`${baseUrl}/api/admin/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'founder@bookbuddys.in',
          password: 'FounderPassword2026!',
        }),
      });

      const body = await res.json();
      expect(res.status).toBe(200);
      expect(body.success).toBe(true);
      expect(body.data.user.role).toBe('ADMIN');
      expect(body.data.token).toBeDefined();
    });

    it('rejects invalid founder credentials with 401', async () => {
      const res = await fetch(`${baseUrl}/api/admin/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'founder@bookbuddys.in',
          password: 'WrongPassword123',
        }),
      });

      const body = await res.json();
      expect(res.status).toBe(401);
      expect(body.success).toBe(false);
    });

    it('blocks unauthenticated requests from accessing admin overview', async () => {
      const res = await fetch(`${baseUrl}/api/admin/overview`);
      const body = await res.json();
      expect(res.status).toBe(401);
      expect(body.success).toBe(false);
    });

    it('blocks customer tokens from accessing admin privileged endpoints', async () => {
      const res = await fetch(`${baseUrl}/api/admin/overview`, {
        headers: { Authorization: 'Bearer dev-token-user_dev_default' },
      });
      const body = await res.json();
      expect(res.status).toBe(403);
      expect(body.success).toBe(false);
    });
  });

  describe('2. Shop Management & Bengaluru Coordinates', () => {
    let adminToken: string;

    beforeEach(async () => {
      const loginRes = await fetch(`${baseUrl}/api/admin/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: 'founder@bookbuddys.in', password: 'FounderPassword2026!' }),
      });
      const loginBody = await loginRes.json();
      adminToken = loginBody.data.token;
    });

    it('founder can create a new verified partner shop with map coordinates', async () => {
      const res = await fetch(`${baseUrl}/api/admin/shops`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${adminToken}`,
        },
        body: JSON.stringify({
          name: 'BookBuddys Whitefield Depot',
          ownerName: 'Venkatesh Rao',
          phone: '+91 98450 11223',
          email: 'whitefield@bookbuddys.in',
          address: '#102 ITPL Main Road, Whitefield',
          locality: 'Whitefield',
          city: 'Bengaluru',
          state: 'Karnataka',
          pincode: '560066',
          latitude: 12.9698,
          longitude: 77.7499,
          landmark: 'Opposite ITPL Gate 2',
          serviceRadiusKm: 12,
        }),
      });

      const body = await res.json();
      expect(res.status).toBe(201);
      expect(body.success).toBe(true);
      expect(body.data.name).toBe('BookBuddys Whitefield Depot');
      expect(body.data.latitude).toBe(12.9698);
      expect(body.data.longitude).toBe(77.7499);
    });

    it('founder can update operational status and business hours', async () => {
      const shopsRes = await fetch(`${baseUrl}/api/admin/shops`, {
        headers: { Authorization: `Bearer ${adminToken}` },
      });
      const shopsBody = await shopsRes.json();
      const shopId = shopsBody.data[0].id;

      const updateRes = await fetch(`${baseUrl}/api/admin/shops/${shopId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${adminToken}`,
        },
        body: JSON.stringify({
          status: 'TEMPORARILY_UNAVAILABLE',
          notes: 'Renovation of storage racks',
        }),
      });

      const updateBody = await updateRes.json();
      expect(updateRes.status).toBe(200);
      expect(updateBody.success).toBe(true);
      expect(updateBody.data.status).toBe('TEMPORARILY_UNAVAILABLE');
    });
  });

  describe('3. Shop-Specific Inventory & Price Rules', () => {
    let adminToken: string;
    let testStoreId: string;
    let testProductId: string;

    beforeEach(async () => {
      const loginRes = await fetch(`${baseUrl}/api/admin/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: 'founder@bookbuddys.in', password: 'FounderPassword2026!' }),
      });
      const loginBody = await loginRes.json();
      adminToken = loginBody.data.token;

      const shops = db.getStores();
      testStoreId = shops[0].id;
      const prods = db.getAllProductsAdmin();
      testProductId = prods[0].id;
    });

    it('sets shop-specific selling price and stock quantity', async () => {
      const res = await fetch(`${baseUrl}/api/admin/shops/${testStoreId}/inventory`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${adminToken}`,
        },
        body: JSON.stringify({
          productId: testProductId,
          pricePaise: 4800,
          mrpPaise: 5500,
          quantity: 25,
          lowStockThreshold: 5,
        }),
      });

      const body = await res.json();
      expect(res.status).toBe(200);
      expect(body.success).toBe(true);
      expect(body.data.pricePaise).toBe(4800);
      expect(body.data.availableQuantity).toBe(25);
    });

    it('rejects negative stock quantities', async () => {
      const res = await fetch(`${baseUrl}/api/admin/shops/${testStoreId}/inventory`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${adminToken}`,
        },
        body: JSON.stringify({
          productId: testProductId,
          pricePaise: 4800,
          quantity: -10,
        }),
      });

      const body = await res.json();
      expect(res.status).toBe(400);
      expect(body.success).toBe(false);
    });

    it('bulk imports inventory safely via CSV rows', async () => {
      const csvData = [
        {
          productId: testProductId,
          pricePaise: 5200,
          mrpPaise: 6000,
          availableQuantity: 40,
          lowStockThreshold: 8,
          isAvailable: true,
        },
      ];

      const res = await fetch(`${baseUrl}/api/admin/shops/${testStoreId}/inventory/bulk-csv`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${adminToken}`,
        },
        body: JSON.stringify({ items: csvData }),
      });

      const body = await res.json();
      expect(res.status).toBe(200);
      expect(body.success).toBe(true);
      expect(body.data.updatedCount).toBe(1);
    });
  });

  describe('4. Geospatial Discovery for Bengaluru Customers', () => {
    it('returns nearby shops sorted by distance from Koramangala coordinates', async () => {
      const res = await fetch(`${baseUrl}/api/stores/nearby?latitude=12.9352&longitude=77.6245&radiusKm=15`);
      const body = await res.json();

      expect(res.status).toBe(200);
      expect(body.success).toBe(true);
      expect(body.data.length).toBeGreaterThan(0);
      expect(body.data[0].distanceKm).toBeLessThan(2);
    });

    it('returns no eligible stores for a distant location outside Bengaluru', async () => {
      // Mumbai coordinates
      const res = await fetch(`${baseUrl}/api/stores/nearby?latitude=19.0760&longitude=72.8777&radiusKm=15`);
      const body = await res.json();

      expect(res.status).toBe(200);
      expect(body.success).toBe(true);
      expect(body.data.length).toBe(0);
    });

    it('returns location-eligible product catalogue with store-specific stock and price', async () => {
      const res = await fetch(`${baseUrl}/api/products/location-eligible?latitude=12.9352&longitude=77.6245`);
      const body = await res.json();

      expect(res.status).toBe(200);
      expect(body.success).toBe(true);
      expect(body.data.store).toBeDefined();
      expect(body.data.products.length).toBeGreaterThan(0);
      expect(body.data.products[0].inventory).toBeDefined();
    });
  });

  describe('5. Complete 10-Step Order & Payment Lifecycle', () => {
    let adminToken: string;
    let orderId: string;
    let orderNumber: string;

    beforeEach(async () => {
      const loginRes = await fetch(`${baseUrl}/api/admin/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: 'founder@bookbuddys.in', password: 'FounderPassword2026!' }),
      });
      const loginBody = await loginRes.json();
      adminToken = loginBody.data.token;
    });

    it('executes full 10-step order workflow from placement to delivery completion', async () => {
      // STEP 1: Customer places order
      const products = db.getAllProductsAdmin();
      const placeRes = await fetch(`${baseUrl}/api/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          paymentMethod: 'UPI',
          items: [
            {
              productId: products[0].id,
              quantity: 2,
              price: 50,
              name: products[0].name,
            },
          ],
          deliveryAddress: {
            recipientName: 'Aarav Sharma',
            phoneNumber: '+91 98765 43210',
            addressLine1: '#42 4th Block, Koramangala',
            city: 'Bengaluru',
            pincode: '560034',
            latitude: 12.9352,
            longitude: 77.6245,
          },
        }),
      });

      const placeBody = await placeRes.json();
      expect(placeRes.status).toBe(201);
      expect(placeBody.success).toBe(true);
      const order = placeBody.data;
      orderId = order.id;
      orderNumber = order.orderNumber;
      expect(order.status).toBe('ORDER_RECEIVED');

      // STEP 2: Verify immediate founder notification exists
      const notifRes = await fetch(`${baseUrl}/api/notifications`, {
        headers: { Authorization: 'Bearer dev-token-user_founder_01' },
      });
      const notifBody = await notifRes.json();
      expect(notifRes.status).toBe(200);
      const newOrderNotif = notifBody.data.find((n: any) => n.type === 'NEW_ORDER');
      expect(newOrderNotif).toBeDefined();

      // STEP 3: Founder verifies shop availability & stock
      const confirmRes = await fetch(`${baseUrl}/api/admin/orders/${orderId}/confirm-availability`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${adminToken}`,
        },
        body: JSON.stringify({
          isAvailable: true,
          supplierNotes: 'Verified with shop owner Mr. Raman - all 2 units in stock.',
        }),
      });
      const confirmBody = await confirmRes.json();
      expect(confirmRes.status).toBe(200);
      expect(confirmBody.data.order.status).toBe('SHOP_CONFIRMED');

      // STEP 4: Founder accepts order and requests payment
      const acceptRes = await fetch(`${baseUrl}/api/admin/orders/${orderId}/accept`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${adminToken}` },
      });
      const acceptBody = await acceptRes.json();
      expect(acceptRes.status).toBe(200);
      expect(acceptBody.data.order.status).toBe('PAYMENT_REQUIRED');

      // STEP 5: Customer submits UPI UTR reference
      const submitUtrRes = await fetch(`${baseUrl}/api/orders/${orderId}/submit-payment`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          utrNumber: '429810982341',
          paymentApp: 'Google Pay',
          notes: 'Paid from HDFC Bank',
        }),
      });
      const submitUtrBody = await submitUtrRes.json();
      expect(submitUtrRes.status).toBe(200);
      expect(submitUtrBody.data.order.status).toBe('PAYMENT_SUBMITTED');

      // STEP 6: Founder verifies actual UPI bank receipt
      const verifyPayRes = await fetch(`${baseUrl}/api/admin/orders/${orderId}/verify-payment`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${adminToken}`,
        },
        body: JSON.stringify({
          verified: true,
          notes: 'Credited to BookBuddys Current Account',
        }),
      });
      const verifyPayBody = await verifyPayRes.json();
      expect(verifyPayRes.status).toBe(200);
      expect(verifyPayBody.data.order.paymentStatus).toBe('PAYMENT_VERIFIED');
      expect(verifyPayBody.data.order.status).toBe('PAYMENT_VERIFIED');

      // STEP 7: Founder procures & marks items ready/packed
      const markReadyRes = await fetch(`${baseUrl}/api/admin/orders/${orderId}/mark-ready`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${adminToken}` },
      });
      const markReadyBody = await markReadyRes.json();
      expect(markReadyRes.status).toBe(200);
      expect(markReadyBody.data.order.status).toBe('READY_FOR_PICKUP');

      // STEP 8: Founder coordinates and books delivery
      const bookDeliveryRes = await fetch(`${baseUrl}/api/admin/orders/${orderId}/book-delivery`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${adminToken}`,
        },
        body: JSON.stringify({
          provider: 'Porter',
          bookingReference: 'PTR-BLR-98432',
          riderName: 'Manjunath K',
          riderPhone: '+91 98450 77889',
          deliveryFeePaise: 4500,
          estimatedDeliveryTime: '30 mins',
        }),
      });
      const bookDeliveryBody = await bookDeliveryRes.json();
      expect(bookDeliveryRes.status).toBe(200);
      expect(bookDeliveryBody.data.order.status).toBe('DELIVERY_BOOKED');
      expect(bookDeliveryBody.data.booking.provider.toUpperCase()).toBe('PORTER');

      // STEP 9: Order is picked up and Out for Delivery
      const dispatchRes = await fetch(`${baseUrl}/api/admin/orders/${orderId}/update-delivery-status`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${adminToken}`,
        },
        body: JSON.stringify({
          deliveryStatus: 'OUT_FOR_DELIVERY',
          notes: 'Rider on way to Koramangala 4th Block',
        }),
      });
      const dispatchBody = await dispatchRes.json();
      expect(dispatchRes.status).toBe(200);
      expect(dispatchBody.data.order.status).toBe('OUT_FOR_DELIVERY');

      // STEP 10: Order Delivered
      const deliverRes = await fetch(`${baseUrl}/api/admin/orders/${orderId}/update-delivery-status`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${adminToken}`,
        },
        body: JSON.stringify({
          deliveryStatus: 'DELIVERED',
          notes: 'Handed over to recipient Aarav Sharma',
        }),
      });
      const deliverBody = await deliverRes.json();
      expect(deliverRes.status).toBe(200);
      expect(deliverBody.data.order.status).toBe('DELIVERED');

      // Verify live 10-step timeline
      const timelineRes = await fetch(`${baseUrl}/api/orders/${orderNumber}/timeline`);
      const timelineBody = await timelineRes.json();
      expect(timelineRes.status).toBe(200);
      expect(timelineBody.data.timeline.length).toBe(10);
      expect(timelineBody.data.currentStatus).toBe('DELIVERED');

      // Verify Audit Log was recorded
      const auditRes = await fetch(`${baseUrl}/api/admin/audit-logs`, {
        headers: { Authorization: `Bearer ${adminToken}` },
      });
      const auditBody = await auditRes.json();
      expect(auditRes.status).toBe(200);
      expect(auditBody.data.length).toBeGreaterThan(0);
    });
  });
});
