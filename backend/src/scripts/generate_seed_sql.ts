import fs from 'fs';
import path from 'path';
import { db } from '../lib/db.js';

const categoryUuidMap: Record<string, string> = {
  cat_notebooks: 'c0000000-0000-0000-0000-000000000001',
  cat_pens: 'c0000000-0000-0000-0000-000000000002',
  cat_pencils: 'c0000000-0000-0000-0000-000000000003',
  cat_geometry: 'c0000000-0000-0000-0000-000000000004',
  cat_art: 'c0000000-0000-0000-0000-000000000005',
  cat_exam: 'c0000000-0000-0000-0000-000000000006',
  cat_highlighters: 'c0000000-0000-0000-0000-000000000007',
  cat_sticky: 'c0000000-0000-0000-0000-000000000008',
  cat_bags: 'c0000000-0000-0000-0000-000000000009',
  cat_bottles: 'c0000000-0000-0000-0000-000000000010',
};

function escapeSql(str: string | undefined): string {
  if (!str) return "''";
  return `'${str.replace(/'/g, "''")}'`;
}

function generate() {
  const categories = db.getCategories();
  const { products } = db.getProducts({ limit: 200 });
  const stores = db.getStores();
  const coupons = db.getCoupons();

  let sql = `-- ============================================================================
-- KIDSG SUPABASE SEED DATA (CATEGORIES, PRODUCTS, STORES, COUPONS, PROFILES)
-- ============================================================================

-- 1. SEED CATEGORIES (10 Categories)
`;

  for (const c of categories) {
    const uuid = categoryUuidMap[c.id] || 'c0000000-0000-0000-0000-000000000099';
    sql += `INSERT INTO categories (id, name, slug, icon_name, display_order, is_active)
VALUES ('${uuid}', ${escapeSql(c.name)}, ${escapeSql(c.slug)}, ${escapeSql(c.iconName)}, ${c.displayOrder}, ${c.isActive})
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  display_order = EXCLUDED.display_order,
  is_active = EXCLUDED.is_active;

`;
  }

  sql += `\n-- 2. SEED STORES (Partner Hubs)\n`;
  let storeIdx = 1;
  const storeUuidMap: Record<string, string> = {};
  for (const s of stores) {
    const sUuid = `s0000000-0000-0000-0000-${String(storeIdx).padStart(12, '0')}`;
    storeUuidMap[s.id] = sUuid;
    storeIdx++;
    sql += `INSERT INTO stores (id, name, address, city, latitude, longitude, phone, delivery_radius_km, is_active, open_time, close_time)
VALUES ('${sUuid}', ${escapeSql(s.name)}, ${escapeSql(s.address)}, ${escapeSql(s.city)}, ${s.latitude}, ${s.longitude}, ${escapeSql(s.phone)}, ${s.deliveryRadiusKm}, ${s.isActive}, ${escapeSql(s.openTime)}, ${escapeSql(s.closeTime)})
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  address = EXCLUDED.address;

`;
  }

  sql += `\n-- 3. SEED PRODUCTS (30+ Stationery Items)\n`;
  let prodIdx = 1;
  for (const p of products) {
    const pUuid = `p0000000-0000-0000-0000-${String(prodIdx).padStart(12, '0')}`;
    prodIdx++;
    const catUuid = categoryUuidMap[p.categoryId] || 'c0000000-0000-0000-0000-000000000001';
    const specsJson = JSON.stringify(p.specs || {}).replace(/'/g, "''");

    sql += `INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('${pUuid}', ${escapeSql(p.name)}, ${escapeSql(p.slug)}, ${escapeSql(p.description)}, ${escapeSql(p.brand)}, '${catUuid}', ${escapeSql(p.imageUrl)}, ${p.price}, ${p.mrp}, ${p.discountPercent}, ${p.stock}, ${escapeSql(p.unit)}, ${escapeSql(p.gradeLevel)}, ${p.isFeatured}, ${p.isActive}, '${specsJson}'::jsonb)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  brand = EXCLUDED.brand,
  image_url = EXCLUDED.image_url,
  price = EXCLUDED.price,
  mrp = EXCLUDED.mrp,
  discount_percent = EXCLUDED.discount_percent,
  stock = EXCLUDED.stock,
  is_featured = EXCLUDED.is_featured,
  specs = EXCLUDED.specs;

`;
  }

  sql += `\n-- 4. SEED COUPONS\n`;
  let cpIdx = 1;
  for (const cp of coupons) {
    const cpUuid = `cp000000-0000-0000-0000-${String(cpIdx).padStart(12, '0')}`;
    cpIdx++;
    sql += `INSERT INTO coupons (id, code, description, discount_type, discount_value, min_order_value, max_discount_amount, valid_until, is_active)
VALUES ('${cpUuid}', ${escapeSql(cp.code)}, ${escapeSql(cp.description)}, '${cp.discountType}', ${cp.discountValue}, ${cp.minOrderValue}, ${cp.maxDiscountAmount || 'NULL'}, '${cp.validUntil}', ${cp.isActive})
ON CONFLICT (code) DO UPDATE SET
  description = EXCLUDED.description,
  discount_value = EXCLUDED.discount_value,
  is_active = EXCLUDED.is_active;

`;
  }

  sql += `\n-- 5. SEED PROFILES (Active Student Accounts)\n`;
  sql += `INSERT INTO profiles (id, first_name, last_name, email, phone, role, onboarding_completed, selected_class, selected_school)
VALUES 
  ('u0000000-0000-0000-0000-000000000001', 'Veenith', 'S', 'veeniths31@gmail.com', '+919876543211', 'CUSTOMER', true, 'Class 5', 'St. Joseph''s'),
  ('u0000000-0000-0000-0000-000000000002', 'Aarav', 'Sharma', 'veenithkumars31@gmail.com', '+919876543212', 'CUSTOMER', true, 'Class 7', 'National Public School')
ON CONFLICT (email) DO UPDATE SET
  first_name = EXCLUDED.first_name,
  last_name = EXCLUDED.last_name,
  onboarding_completed = EXCLUDED.onboarding_completed,
  selected_class = EXCLUDED.selected_class,
  selected_school = EXCLUDED.selected_school;

-- 6. STORE INVENTORY LINKING
INSERT INTO store_inventory (store_id, product_id, stock_quantity, is_available)
SELECT s.id, p.id, p.stock, true
FROM stores s
CROSS JOIN products p
ON CONFLICT (store_id, product_id) DO UPDATE SET
  stock_quantity = EXCLUDED.stock_quantity;
`;

  const outputPath = path.resolve(process.cwd(), 'supabase', 'migrations', '003_seed_data.sql');
  fs.writeFileSync(outputPath, sql, 'utf-8');
  console.log(`✅ Generated seed SQL file at: ${outputPath} (${sql.length} bytes)`);
}

generate();
