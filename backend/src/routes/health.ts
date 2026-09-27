import { Router } from 'express';
import { env } from '../config/env.js';
import { supabaseAdmin } from '../lib/supabase.js';
import { seedSupabaseCatalog, resetSupabaseDatabase } from '../lib/supabaseSync.js';

const router = Router();

router.get('/health', async (req, res) => {
  const isSupaConfigured = env.SUPABASE_URL.startsWith('http') && !env.SUPABASE_URL.includes('mock.supabase.co');
  let supaStatus = isSupaConfigured ? 'connecting' : 'mock_standalone';
  let supaError: string | null = null;
  let profilesCount: number | null = null;
  let categoriesCount: number | null = null;
  let productsCount: number | null = null;
  let ordersCount: number | null = null;
  let paymentsCount: number | null = null;
  let trackingCount: number | null = null;
  let addressesCount: number | null = null;
  let seededNow: boolean = false;

  if (isSupaConfigured) {
    try {
      const [profRes, catRes, prodRes, ordRes, payRes, trackRes, addrRes] = await Promise.all([
        supabaseAdmin.from('profiles').select('*', { count: 'exact', head: true }),
        supabaseAdmin.from('categories').select('*', { count: 'exact', head: true }),
        supabaseAdmin.from('products').select('*', { count: 'exact', head: true }),
        supabaseAdmin.from('orders').select('*', { count: 'exact', head: true }),
        supabaseAdmin.from('payments').select('*', { count: 'exact', head: true }),
        supabaseAdmin.from('delivery_tracking').select('*', { count: 'exact', head: true }),
        supabaseAdmin.from('addresses').select('*', { count: 'exact', head: true }),
      ]);

      if (profRes.error) {
        supaStatus = 'error';
        supaError = profRes.error.message;
      } else {
        supaStatus = 'connected';
        profilesCount = profRes.count ?? 0;
        categoriesCount = catRes.count ?? 0;
        productsCount = prodRes.count ?? 0;
        ordersCount = ordRes.count ?? 0;
        paymentsCount = payRes.count ?? 0;
        trackingCount = trackRes.count ?? 0;
        addressesCount = addrRes.count ?? 0;

        // Auto-seed catalog if categories table is currently empty or ?seed=true requested
        if (categoriesCount === 0 || req.query.seed === 'true') {
          const seedResult = await seedSupabaseCatalog();
          if (seedResult.success) {
            seededNow = true;
            categoriesCount = seedResult.categoriesCount;
            productsCount = seedResult.productsCount;
          }
        }
      }
    } catch (e: any) {
      supaStatus = 'error';
      supaError = e?.message || 'Unknown connection error';
    }
  }

  res.json({
    success: true,
    service: 'kidsG-api',
    status: 'ok',
    environment: env.NODE_ENV,
    appEnv: env.APP_ENV,
    supabase: {
      configured: isSupaConfigured,
      url: env.SUPABASE_URL.replace(/https:\/\/(.{4}).*(\.supabase\.co)/, 'https://$1...$2'),
      status: supaStatus,
      error: supaError,
      counts: {
        profiles: profilesCount,
        categories: categoriesCount,
        products: productsCount,
        orders: ordersCount,
        payments: paymentsCount,
        delivery_tracking: trackingCount,
        addresses: addressesCount,
      },
      seededNow,
    }
  });
});

// Explicit Admin Seed Endpoint
router.post('/admin/seed', async (_req, res) => {
  const result = await seedSupabaseCatalog();
  res.json(result);
});

// Admin Reset Database Endpoint (Completely clears transactional tables and re-seeds catalog)
router.all('/admin/reset-db', async (_req, res) => {
  const result = await resetSupabaseDatabase();
  res.json(result);
});

export default router;
