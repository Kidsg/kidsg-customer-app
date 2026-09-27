import { randomUUID } from 'crypto';
import { supabaseAdmin } from './supabase.js';
import { db } from './db.js';
import { env } from '../config/env.js';
import { Order } from '../types/index.js';

export async function seedSupabaseCatalog(): Promise<{
  success: boolean;
  categoriesCount: number;
  productsCount: number;
  storesCount: number;
  couponsCount: number;
  error?: string;
}> {
  const isConfigured = env.SUPABASE_URL.startsWith('http') && !env.SUPABASE_URL.includes('mock.supabase.co');
  if (!isConfigured) {
    return { success: false, categoriesCount: 0, productsCount: 0, storesCount: 0, couponsCount: 0, error: 'Supabase not configured' };
  }

  try {
    // 1. Seed Categories (10 items)
    const categoryMapping: Record<string, string> = {
      'cat_notebooks': 'c0000000-0000-0000-0000-000000000001',
      'cat_pens': 'c0000000-0000-0000-0000-000000000002',
      'cat_pencils': 'c0000000-0000-0000-0000-000000000003',
      'cat_geometry': 'c0000000-0000-0000-0000-000000000004',
      'cat_art': 'c0000000-0000-0000-0000-000000000005',
      'cat_exam': 'c0000000-0000-0000-0000-000000000006',
      'cat_highlighters': 'c0000000-0000-0000-0000-000000000007',
      'cat_sticky': 'c0000000-0000-0000-0000-000000000008',
      'cat_bags': 'c0000000-0000-0000-0000-000000000009',
      'cat_bottles': 'c0000000-0000-0000-0000-000000000010',
    };

    const categories = db.getCategories();
    for (const cat of categories) {
      const uuid = categoryMapping[cat.id] || `c0000000-0000-0000-0000-${cat.displayOrder.toString().padStart(12, '0')}`;
      await supabaseAdmin.from('categories').upsert({
        id: uuid,
        name: cat.name,
        slug: cat.slug,
        icon_name: cat.iconName,
        display_order: cat.displayOrder,
        is_active: cat.isActive,
      }, { onConflict: 'slug' });
    }

    // 2. Seed Partner Stores
    const stores = db.getStores();
    for (let i = 0; i < stores.length; i++) {
      const s = stores[i];
      const uuid = `a0000000-0000-0000-0000-${(i + 1).toString().padStart(12, '0')}`;
      const { error: sErr } = await supabaseAdmin.from('stores').upsert({
        id: uuid,
        name: s.name,
        address: s.address,
        city: s.city || 'Bengaluru',
        latitude: s.latitude || 12.9352,
        longitude: s.longitude || 77.6245,
        phone: s.phone || '+91 80 2553 1234',
        delivery_radius_km: s.deliveryRadiusKm || 5.0,
        is_active: s.isActive,
        open_time: s.openTime || '07:30',
        close_time: s.closeTime || '21:30',
      }, { onConflict: 'id' });
      if (sErr) console.error('[KidsG][Supabase] Store upsert error:', sErr.message);
    }

    // 3. Seed Products
    const { products } = db.getProducts({ limit: 100 });
    let insertedProds = 0;
    for (let i = 0; i < products.length; i++) {
      const p = products[i];
      const prodUuid = `b0000000-0000-0000-0000-${(i + 1).toString().padStart(12, '0')}`;
      const catUuid = categoryMapping[p.categoryId] || 'c0000000-0000-0000-0000-000000000001';

      const { error: pErr } = await supabaseAdmin.from('products').upsert({
        id: prodUuid,
        name: p.name,
        slug: p.slug,
        description: p.description,
        brand: p.brand,
        category_id: catUuid,
        image_url: p.imageUrl,
        price: p.price,
        mrp: p.mrp,
        discount_percent: p.discountPercent,
        stock: p.stock,
        unit: p.unit,
        grade_level: p.gradeLevel,
        is_featured: p.isFeatured,
        is_active: p.isActive,
        specs: p.specs,
      }, { onConflict: 'slug' });
      if (pErr) {
        console.error(`[KidsG][Supabase] Product upsert error for ${p.slug}:`, pErr.message);
      } else {
        insertedProds++;
      }
    }

    // 4. Seed Coupons
    const coupons = db.getCoupons();
    for (let i = 0; i < coupons.length; i++) {
      const cp = coupons[i];
      const cpUuid = `d0000000-0000-0000-0000-${(i + 1).toString().padStart(12, '0')}`;
      await supabaseAdmin.from('coupons').upsert({
        id: cpUuid,
        code: cp.code,
        description: cp.description,
        discount_type: cp.discountType,
        discount_value: cp.discountValue,
        min_order_value: cp.minOrderValue,
        max_discount_amount: cp.maxDiscountAmount || null,
        valid_until: cp.validUntil,
        is_active: cp.isActive,
      }, { onConflict: 'code' });
    }

    return {
      success: true,
      categoriesCount: categories.length,
      productsCount: insertedProds,
      storesCount: stores.length,
      couponsCount: coupons.length,
    };
  } catch (err: any) {
    console.error('[KidsG][Supabase] Seed error:', err);
    return {
      success: false,
      categoriesCount: 0,
      productsCount: 0,
      storesCount: 0,
      couponsCount: 0,
      error: err?.message || 'Failed to seed Supabase',
    };
  }
}

export async function syncOrderToSupabase(userEmail: string, order: Order, clientAddress?: any) {
  const isConfigured = env.SUPABASE_URL.startsWith('http') && !env.SUPABASE_URL.includes('mock.supabase.co');
  if (!isConfigured) return;

  try {
    // 1. Ensure Profile Exists in Supabase
    let profileId: string | null = null;
    const { data: existingProfile } = await supabaseAdmin
      .from('profiles')
      .select('id')
      .eq('email', userEmail)
      .maybeSingle();

    if (existingProfile?.id) {
      profileId = existingProfile.id;
    } else {
      const { data: newProf, error: pErr } = await supabaseAdmin
        .from('profiles')
        .upsert({
          email: userEmail,
          first_name: order.addressSnapshot?.name || clientAddress?.recipientName || 'Student',
          last_name: '',
          phone: order.addressSnapshot?.phone || clientAddress?.phoneNumber || null,
          role: 'CUSTOMER',
          onboarding_completed: true,
        }, { onConflict: 'email' })
        .select('id')
        .maybeSingle();

      if (!pErr && newProf?.id) {
        profileId = newProf.id;
      }
    }

    if (!profileId) {
      console.warn('[KidsG][Supabase] Profile lookup/creation failed during order sync');
      return;
    }

    // 2. Ensure Store Exists
    let storeId = 'a0000000-0000-0000-0000-000000000001';
    const { data: storeData } = await supabaseAdmin.from('stores').select('id').limit(1).maybeSingle();
    if (storeData?.id) {
      storeId = storeData.id;
    } else {
      await supabaseAdmin.from('stores').insert({
        id: storeId,
        name: order.storeSnapshot?.name || 'Vidya Book & Stationery Depot',
        address: order.storeSnapshot?.address || 'No. 42, 12th Main Road, Bengaluru',
        city: 'Bengaluru',
        phone: '+91 80 2553 1234',
        latitude: 12.9352,
        longitude: 77.6245,
        delivery_radius_km: 5.0,
        is_active: true,
      });
    }

    // 3. Save to `addresses` table
    try {
      await supabaseAdmin.from('addresses').insert({
        user_id: profileId,
        label: order.addressSnapshot?.label || clientAddress?.label || 'Home',
        name: order.addressSnapshot?.name || clientAddress?.recipientName || 'Student Desk',
        phone: order.addressSnapshot?.phone || clientAddress?.phoneNumber || '9876543210',
        address_line_1: order.addressSnapshot?.addressLine1 || clientAddress?.addressLine1 || 'KidsG Desk Delivery',
        address_line_2: order.addressSnapshot?.addressLine2 || clientAddress?.addressLine2 || '',
        city: order.addressSnapshot?.city || clientAddress?.city || 'Bengaluru',
        state: 'Karnataka',
        postal_code: order.addressSnapshot?.postalCode || clientAddress?.pincode || '560001',
        is_default: true,
      });
    } catch (_addrErr) {}

    // 4. Save to `orders` table
    const { data: insertedOrder, error: orderErr } = await supabaseAdmin
      .from('orders')
      .insert({
        order_number: order.orderNumber,
        user_id: profileId,
        store_id: storeId,
        status: order.status,
        subtotal: order.subtotal,
        discount: order.discount,
        coupon_discount: order.couponDiscount,
        delivery_fee: order.deliveryFee,
        tax: order.tax,
        total: order.total,
        payment_status: order.paymentStatus,
        payment_method: order.paymentMethod,
        delivery_status: order.deliveryStatus,
        address_snapshot: order.addressSnapshot,
        notes: order.notes || '',
      })
      .select('id')
      .maybeSingle();

    if (orderErr) {
      console.error('[KidsG][Supabase] Order insert error:', orderErr.message);
      return;
    }

    const orderDbId = insertedOrder?.id;
    if (!orderDbId) return;

    // 5. Save Items to `order_items` table
    for (const item of order.items) {
      let prodDbId = 'b0000000-0000-0000-0000-000000000001';
      const { data: prodData } = await supabaseAdmin
        .from('products')
        .select('id')
        .eq('slug', item.product?.slug || item.productId)
        .maybeSingle();

      if (prodData?.id) {
        prodDbId = prodData.id;
      } else {
        const { data: catData } = await supabaseAdmin.from('categories').select('id').limit(1).maybeSingle();
        const catId = catData?.id || 'c0000000-0000-0000-0000-000000000001';
        const { data: newProd } = await supabaseAdmin.from('products').insert({
          name: item.product?.name || 'Classmate Stationery',
          slug: item.product?.slug || item.productId || `prod_${randomUUID().substring(0, 8)}`,
          description: item.product?.description || 'School stationery item',
          brand: item.product?.brand || 'Classmate',
          category_id: catId,
          image_url: item.product?.imageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500',
          price: item.priceSnapshot || 95,
          mrp: item.mrpSnapshot || 110,
          discount_percent: 10,
          stock: 50,
          unit: 'piece',
          grade_level: 'All',
          is_active: true,
        }).select('id').maybeSingle();
        if (newProd?.id) prodDbId = newProd.id;
      }

      await supabaseAdmin.from('order_items').insert({
        order_id: orderDbId,
        product_id: prodDbId,
        product_name_snapshot: item.product?.name || 'Stationery Item',
        price_snapshot: item.priceSnapshot,
        mrp_snapshot: item.mrpSnapshot,
        quantity: item.quantity,
        variant_snapshot: item.selectedVariant || null,
      });
    }

    // 6. Save to `payments` table (Strict schema match: user_id, order_id, provider, amount, currency, status)
    const { error: payErr } = await supabaseAdmin.from('payments').insert({
      order_id: orderDbId,
      user_id: profileId,
      provider: order.paymentMethod?.toLowerCase() || 'mock',
      transaction_id: `txn_${randomUUID().substring(0, 10)}`,
      gateway_order_id: `gpay_${randomUUID().substring(0, 8)}`,
      amount: order.total,
      currency: 'INR',
      status: order.paymentStatus || 'SUCCESS',
      payment_metadata: { method: order.paymentMethod, simulated: true },
    });
    if (payErr) {
      console.error('[KidsG][Supabase] Payments insert notice:', payErr.message);
    }

    // 7. Save to `delivery_tracking` table (Strict schema match: order_id, rider_name, rider_phone, current_lat, current_lng, status_history)
    const { error: trackErr } = await supabaseAdmin.from('delivery_tracking').insert({
      order_id: orderDbId,
      rider_name: 'KidsG Express Partner',
      rider_phone: '+919876543210',
      current_lat: 12.9352,
      current_lng: 77.6245,
      estimated_delivery_time: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
      status_history: [
        {
          status: order.deliveryStatus || 'CONFIRMED',
          timestamp: new Date().toISOString(),
          message: 'Order placed & scheduled for store preparation'
        }
      ],
    });
    if (trackErr) {
      console.error('[KidsG][Supabase] Delivery tracking insert notice:', trackErr.message);
    }

    console.log(`[KidsG][Supabase] Successfully synced order ${order.orderNumber} to Supabase (orders, order_items, payments, tracking)!`);
  } catch (err: any) {
    console.error('[KidsG][Supabase] syncOrderToSupabase error:', err?.message);
  }
}

/**
 * Fetch orders for a user directly from Supabase, ensuring cross-instance persistence.
 */
export async function fetchUserOrdersFromSupabase(userEmail: string): Promise<Order[]> {
  const isConfigured = env.SUPABASE_URL.startsWith('http') && !env.SUPABASE_URL.includes('mock.supabase.co');
  if (!isConfigured) return [];

  try {
    const { data: profile } = await supabaseAdmin
      .from('profiles')
      .select('id')
      .eq('email', userEmail)
      .maybeSingle();

    if (!profile?.id) return [];

    const { data: supaOrders, error } = await supabaseAdmin
      .from('orders')
      .select(`
        *,
        order_items (*)
      `)
      .eq('user_id', profile.id)
      .order('created_at', { ascending: false });

    if (error || !supaOrders) return [];

    return supaOrders.map((o: any) => ({
      id: o.id,
      orderNumber: o.order_number,
      userId: o.user_id,
      storeId: o.store_id,
      items: (o.order_items || []).map((oi: any) => ({
        id: oi.id,
        productId: oi.product_id,
        quantity: oi.quantity,
        priceSnapshot: Number(oi.price_snapshot),
        mrpSnapshot: Number(oi.mrp_snapshot),
        selectedVariant: oi.variant_snapshot || undefined,
        product: {
          id: oi.product_id,
          name: oi.product_name_snapshot,
          slug: `prod_${oi.product_id.substring(0, 8)}`,
          description: '',
          price: Number(oi.price_snapshot),
          mrp: Number(oi.mrp_snapshot),
          discountPercent: 10,
          imageUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500',
          categoryId: 'c0000000-0000-0000-0000-000000000001',
          stock: 50,
          isActive: true,
          brand: 'KidsG Partner',
          unit: 'piece',
          tags: [],
          specs: {},
          createdAt: o.created_at,
          updatedAt: o.updated_at,
        },
      })),
      subtotal: Number(o.subtotal),
      discount: Number(o.discount),
      couponDiscount: Number(o.coupon_discount),
      deliveryFee: Number(o.delivery_fee),
      tax: Number(o.tax),
      total: Number(o.total),
      status: o.status,
      paymentStatus: o.payment_status,
      paymentMethod: o.payment_method,
      deliveryStatus: o.delivery_status,
      addressSnapshot: o.address_snapshot,
      storeSnapshot: {
        id: o.store_id,
        name: 'Vidya Book & Stationery Depot',
        address: 'No. 42, 12th Main Road, Bengaluru',
        phone: '+91 80 2553 1234',
      },
      createdAt: o.created_at,
      updatedAt: o.updated_at,
    }));
  } catch (err: any) {
    console.warn('[KidsG][Supabase] fetchUserOrdersFromSupabase notice:', err?.message);
    return [];
  }
}

/**
 * Fetch all orders for Shop Owner directly from Supabase
 */
export async function fetchShopOrdersFromSupabase(storeId?: string, statusFilter?: string): Promise<Order[]> {
  const isConfigured = env.SUPABASE_URL.startsWith('http') && !env.SUPABASE_URL.includes('mock.supabase.co');
  if (!isConfigured) return [];

  try {
    let query = supabaseAdmin
      .from('orders')
      .select(`
        *,
        order_items (*),
        profiles (first_name, last_name, email, phone)
      `)
      .order('created_at', { ascending: false });

    if (storeId) {
      query = query.eq('store_id', storeId);
    }
    if (statusFilter && statusFilter !== 'ALL') {
      query = query.eq('status', statusFilter);
    }

    const { data: supaOrders, error } = await query;
    if (error || !supaOrders) return [];

    return supaOrders.map((o: any) => ({
      id: o.id,
      orderNumber: o.order_number,
      userId: o.user_id,
      storeId: o.store_id,
      items: (o.order_items || []).map((oi: any) => ({
        id: oi.id,
        productId: oi.product_id,
        quantity: oi.quantity,
        priceSnapshot: Number(oi.price_snapshot),
        mrpSnapshot: Number(oi.mrp_snapshot),
        selectedVariant: oi.variant_snapshot || undefined,
        product: {
          id: oi.product_id,
          name: oi.product_name_snapshot,
          slug: `prod_${oi.product_id.substring(0, 8)}`,
          description: '',
          price: Number(oi.price_snapshot),
          mrp: Number(oi.mrp_snapshot),
          discountPercent: 10,
          imageUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500',
          categoryId: 'c0000000-0000-0000-0000-000000000001',
          stock: 50,
          isActive: true,
          brand: 'KidsG Partner',
          unit: 'piece',
          tags: [],
          specs: {},
          createdAt: o.created_at,
          updatedAt: o.updated_at,
        },
      })),
      subtotal: Number(o.subtotal),
      discount: Number(o.discount),
      couponDiscount: Number(o.coupon_discount),
      deliveryFee: Number(o.delivery_fee),
      tax: Number(o.tax),
      total: Number(o.total),
      status: o.status,
      paymentStatus: o.payment_status,
      paymentMethod: o.payment_method,
      deliveryStatus: o.delivery_status,
      addressSnapshot: o.address_snapshot,
      storeSnapshot: {
        id: o.store_id,
        name: 'Vidya Book & Stationery Depot',
        address: 'No. 42, 12th Main Road, Bengaluru',
        phone: '+91 80 2553 1234',
      },
      createdAt: o.created_at,
      updatedAt: o.updated_at,
    }));
  } catch (err: any) {
    console.warn('[KidsG][Supabase] fetchShopOrdersFromSupabase notice:', err?.message);
    return [];
  }
}

/**
 * Update order status synchronously in Supabase
 */
export async function updateSupabaseOrderStatus(orderId: string, status: string, deliveryStatus?: string) {
  const isConfigured = env.SUPABASE_URL.startsWith('http') && !env.SUPABASE_URL.includes('mock.supabase.co');
  if (!isConfigured) return;

  try {
    const updateData: any = {
      status,
      updated_at: new Date().toISOString(),
    };
    if (deliveryStatus) {
      updateData.delivery_status = deliveryStatus;
    }

    await supabaseAdmin
      .from('orders')
      .update(updateData)
      .or(`id.eq.${orderId},order_number.eq.${orderId}`);

    // Update tracking status history
    const { data: order } = await supabaseAdmin
      .from('orders')
      .select('id')
      .or(`id.eq.${orderId},order_number.eq.${orderId}`)
      .maybeSingle();

    if (order?.id) {
      const { data: track } = await supabaseAdmin
        .from('delivery_tracking')
        .select('*')
        .eq('order_id', order.id)
        .maybeSingle();

      const existingHistory = track?.status_history || [];
      const updatedHistory = [
        ...existingHistory,
        {
          status,
          timestamp: new Date().toISOString(),
          message: `Order transitioned to ${status} by shop partner`,
        }
      ];

      await supabaseAdmin
        .from('delivery_tracking')
        .upsert({
          order_id: order.id,
          rider_name: track?.rider_name || 'KidsG Express Partner',
          rider_phone: track?.rider_phone || '+919876543210',
          current_lat: 12.9352,
          current_lng: 77.6245,
          status_history: updatedHistory,
          updated_at: new Date().toISOString(),
        }, { onConflict: 'order_id' });
    }
  } catch (err: any) {
    console.warn('[KidsG][Supabase] updateSupabaseOrderStatus error:', err?.message);
  }
}

/**
 * Complete Database Reset:
 * Wipes all transactional data (orders, items, payments, tracking, addresses, profiles, auth users)
 * Re-seeds catalog (categories, products, stores, coupons)
 */
export async function resetSupabaseDatabase(): Promise<{
  success: boolean;
  message: string;
  cleared: Record<string, number | string>;
  catalog: { categories: number; products: number; stores: number; coupons: number };
}> {
  const isConfigured = env.SUPABASE_URL.startsWith('http') && !env.SUPABASE_URL.includes('mock.supabase.co');
  if (!isConfigured) {
    return {
      success: false,
      message: 'Supabase is not configured',
      cleared: {},
      catalog: { categories: 0, products: 0, stores: 0, coupons: 0 },
    };
  }

  const clearedCounts: Record<string, number | string> = {};

  try {
    // 1. Delete in foreign-key dependency order
    const tablesToClear = [
      'delivery_tracking',
      'payments',
      'order_items',
      'orders',
      'addresses',
      'cart_items',
      'carts',
      'wishlist_items',
      'wishlists',
      'notifications',
      'coupon_redemptions',
      'support_tickets',
      'profiles',
    ];

    for (const table of tablesToClear) {
      const { count, error } = await supabaseAdmin
        .from(table)
        .delete({ count: 'exact' })
        .neq('id', '00000000-0000-0000-0000-000000000000');

      if (error) {
        console.warn(`[KidsG][Reset] Notice on table ${table}:`, error.message);
        clearedCounts[table] = `error: ${error.message}`;
      } else {
        clearedCounts[table] = count ?? 0;
      }
    }

    // 2. Clear Auth users
    try {
      const { data: authUsers } = await supabaseAdmin.auth.admin.listUsers({ page: 1, perPage: 1000 });
      let deletedUsers = 0;
      if (authUsers?.users) {
        for (const u of authUsers.users) {
          await supabaseAdmin.auth.admin.deleteUser(u.id);
          deletedUsers++;
        }
      }
      clearedCounts['auth_users'] = deletedUsers;
    } catch (authErr: any) {
      clearedCounts['auth_users'] = `auth cleanup notice: ${authErr?.message}`;
    }

    // 3. Clear local in-memory transactions
    db.clearTransactionalData();

    // 4. Fresh re-seed catalog
    const seedResult = await seedSupabaseCatalog();

    return {
      success: true,
      message: 'Database completely cleared and catalog re-seeded successfully',
      cleared: clearedCounts,
      catalog: {
        categories: seedResult.categoriesCount,
        products: seedResult.productsCount,
        stores: seedResult.storesCount,
        coupons: seedResult.couponsCount,
      },
    };
  } catch (err: any) {
    console.error('[KidsG][Supabase] Database reset error:', err);
    return {
      success: false,
      message: err?.message || 'Database reset failed',
      cleared: clearedCounts,
      catalog: { categories: 0, products: 0, stores: 0, coupons: 0 },
    };
  }
}
