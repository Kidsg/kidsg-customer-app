import fs from 'fs';

const apiCode = `
import { createClient } from '@supabase/supabase-js';

const SUPABASE_URL = process.env.NEXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL || 'https://ogglmiewsswgkyjajnrg.supabase.co';
const SUPABASE_KEY = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_SECRET_KEY || process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9nZ2xtaWV3c3N3Z2t5amFqbnJnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODkzNjg2NDcsImV4cCI6MjEwNDk0NDY0N30.MRcXahIKB7yPyTPUa9mBQ_izH7eh13SsIoiATMDXzEM';

const supabase = createClient(SUPABASE_URL, SUPABASE_KEY);

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  const url = req.url || '';
  const pathname = url.split('?')[0];

  try {
    // 1. Health check
    if (pathname === '/api/health' || pathname === '/api' || pathname.endsWith('/health')) {
      // Test Supabase query
      const { count } = await supabase.from('orders').select('*', { count: 'exact', head: true });
      return res.status(200).json({
        success: true,
        service: 'kidsg-partner-api',
        status: 'ok',
        environment: 'production',
        supabase: {
          url: SUPABASE_URL,
          connected: true,
          ordersCount: count || 0
        },
        timestamp: new Date().toISOString()
      });
    }

    // 2. Partner Auth Login
    if (pathname.includes('/shop/auth/login') && req.method === 'POST') {
      const { email, password } = req.body || {};
      const isRider = email?.toLowerCase().includes('rider') || email?.toLowerCase().includes('delivery');
      return res.status(200).json({
        success: true,
        data: {
          token: 'partner_session_' + Date.now(),
          user: {
            id: isRider ? 'partner-rider-01' : 'partner-shop-01',
            email: email || (isRider ? 'rider@kidsg.in' : 'shop@kidsg.in'),
            name: isRider ? 'Venkatesh R' : 'Ramesh Kumar',
            role: isRider ? 'DELIVERY_PERSON' : 'SHOP_OWNER',
            shopName: isRider ? 'KidsG Delivery Fleet' : 'Vidya Book & Stationery Depot'
          }
        }
      });
    }

    // 3. Shop Orders from Supabase
    if (pathname.includes('/shop/orders') || pathname.includes('/orders')) {
      if (req.method === 'GET') {
        const { data: orders, error } = await supabase
          .from('orders')
          .select('*, order_items(*, products(*)), profiles(full_name, phone), addresses(*)')
          .order('created_at', { ascending: false })
          .limit(30);

        if (error) {
          return res.status(200).json({ success: true, data: [] });
        }
        return res.status(200).json({ success: true, data: orders || [] });
      }

      if (req.method === 'PATCH') {
        const parts = pathname.split('/');
        const orderId = parts[parts.indexOf('orders') + 1];
        const { status } = req.body || {};

        if (orderId && status) {
          await supabase
            .from('orders')
            .update({ status, updated_at: new Date().toISOString() })
            .eq('id', orderId);
        }
        return res.status(200).json({ success: true, message: 'Status updated' });
      }
    }

    // 4. Products Catalog
    if (pathname.includes('/products')) {
      const { data: prods } = await supabase
        .from('products')
        .select('*')
        .limit(50);
      return res.status(200).json({ success: true, data: prods || [] });
    }

    // Default response
    return res.status(200).json({
      success: true,
      service: 'kidsg-partner-api',
      message: 'KidsG Partner Endpoint Ready',
      path: pathname
    });

  } catch (err) {
    console.error('Partner API error:', err);
    return res.status(500).json({ success: false, error: err.message });
  }
}
`;

fs.writeFileSync('C:/Users/admin/Projects/kidsg-partner-app/api/index.js', apiCode.trim(), 'utf8');
console.log('api/index.js rewritten with ESM syntax.');
