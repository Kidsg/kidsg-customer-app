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
      const uuid = `s0000000-0000-0000-0000-${(i + 1).toString().padStart(12, '0')}`;
      await supabaseAdmin.from('stores').upsert({
        id: uuid,
        name: s.name,
        address: s.address,
        city: 'Bengaluru',
        latitude: s.location.latitude,
        longitude: s.location.longitude,
        phone: s.phone,
        delivery_radius_km: s.deliveryRadiusKm,
        is_active: s.isActive,
        open_time: s.operatingHours.open,
        close_time: s.operatingHours.close,
      }, { onConflict: 'id' });
    }

    // 3. Seed Products
    const { products } = db.getProducts({ limit: 100 });
    for (let i = 0; i < products.length; i++) {
      const p = products[i];
      const prodUuid = `p0000000-0000-0000-0000-${(i + 1).toString().padStart(12, '0')}`;
      const catUuid = categoryMapping[p.categoryId] || 'c0000000-0000-0000-0000-000000000001';

      await supabaseAdmin.from('products').upsert({
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
    }

    // 4. Seed Coupons
    const coupons = db.getCoupons();
    for (let i = 0; i < coupons.length; i++) {
      const cp = coupons[i];
      const cpUuid = `cp000000-0000-0000-0000-${(i + 1).toString().padStart(12, '0')}`;
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
      productsCount: products.length,
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
    let storeId = 's0000000-0000-0000-0000-000000000001';
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
      let prodDbId = 'p0000000-0000-0000-0000-000000000001';
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

    // 6. Save to `payments` table
    await supabaseAdmin.from('payments').insert({
      order_id: orderDbId,
      amount: order.total,
      currency: 'INR',
      method: order.paymentMethod,
      status: order.paymentStatus,
      transaction_id: `txn_${randomUUID().substring(0, 10)}`,
      gateway: 'MOCK',
    });

    // 7. Save to `delivery_tracking` table
    await supabaseAdmin.from('delivery_tracking').insert({
      order_id: orderDbId,
      status: order.deliveryStatus || 'CONFIRMED',
      current_location_lat: 12.9352,
      current_location_lng: 77.6245,
      estimated_delivery_time: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
    });

    console.log(`[KidsG][Supabase] Successfully synced order ${order.orderNumber} to Supabase!`);
  } catch (err: any) {
    console.error('[KidsG][Supabase] syncOrderToSupabase unexpected error:', err?.message);
  }
}
