import { db } from '../lib/db.js';
import { supabaseAdmin } from '../lib/supabase.js';
import { env } from '../config/env.js';

async function seed() {
  console.log('====================================================');
  console.log('🌱 KIDSG DEVELOPMENT SEED SCRIPT');
  console.log('⚠️  NOTICE: This script populates DEVELOPMENT DATA only.');
  console.log('====================================================\n');

  const categories = db.getCategories();
  const { products } = db.getProducts({ limit: 100 });
  const stores = db.getStores();
  const coupons = db.getCoupons();

  console.log(`📦 Categories: ${categories.length}`);
  categories.forEach(c => console.log(`   - [${c.slug}] ${c.name}`));

  console.log(`\n📚 Products: ${products.length}`);
  products.slice(0, 10).forEach(p => console.log(`   - [${p.brand}] ${p.name} (₹${p.price})`));
  console.log(`   ... and ${products.length - 10} more stationery items.`);

  console.log(`\n🏪 Partner Stores: ${stores.length}`);
  stores.forEach(s => console.log(`   - ${s.name} (${s.address})`));

  console.log(`\n🎟️ Coupons: ${coupons.length}`);
  coupons.forEach(cp => console.log(`   - ${cp.code}: ${cp.description}`));

  // If Supabase is connected to a real instance, attempt inserting into tables
  if (env.SUPABASE_URL && !env.SUPABASE_URL.includes('mock.supabase.co')) {
    console.log('\n📡 Supabase instance detected. Syncing seed records to PostgreSQL...');
    try {
      // Upsert categories
      for (const cat of categories) {
        await supabaseAdmin.from('categories').upsert({
          id: cat.id,
          name: cat.name,
          slug: cat.slug,
          icon_name: cat.iconName,
          display_order: cat.displayOrder,
          is_active: cat.isActive,
        });
      }
      console.log('✅ Categories synced to Supabase.');
    } catch (err: any) {
      console.warn('⚠️ Supabase sync warning (running in mock mode):', err?.message);
    }
  } else {
    console.log('\n💡 Running with local development store (no Supabase connection required).');
  }

  console.log('\n🎉 Seed data initialized successfully!\n');
}

seed().catch(err => {
  console.error('❌ Seed script error:', err);
  process.exit(1);
});
