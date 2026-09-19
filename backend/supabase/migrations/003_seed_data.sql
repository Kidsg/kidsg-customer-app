-- ============================================================================
-- KIDSG SUPABASE SEED DATA (CATEGORIES, PRODUCTS, STORES, COUPONS, PROFILES)
-- ============================================================================

-- 1. SEED CATEGORIES (10 Categories)
INSERT INTO categories (id, name, slug, icon_name, display_order, is_active)
VALUES ('c0000000-0000-0000-0000-000000000001', 'Notebooks & Registers', 'notebooks', 'notebook', 1, true)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  display_order = EXCLUDED.display_order,
  is_active = EXCLUDED.is_active;

INSERT INTO categories (id, name, slug, icon_name, display_order, is_active)
VALUES ('c0000000-0000-0000-0000-000000000002', 'Pens & Refills', 'pens', 'pen', 2, true)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  display_order = EXCLUDED.display_order,
  is_active = EXCLUDED.is_active;

INSERT INTO categories (id, name, slug, icon_name, display_order, is_active)
VALUES ('c0000000-0000-0000-0000-000000000003', 'Pencils & Erasers', 'pencils', 'pencil', 3, true)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  display_order = EXCLUDED.display_order,
  is_active = EXCLUDED.is_active;

INSERT INTO categories (id, name, slug, icon_name, display_order, is_active)
VALUES ('c0000000-0000-0000-0000-000000000004', 'Geometry & Scales', 'geometry', 'ruler', 4, true)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  display_order = EXCLUDED.display_order,
  is_active = EXCLUDED.is_active;

INSERT INTO categories (id, name, slug, icon_name, display_order, is_active)
VALUES ('c0000000-0000-0000-0000-000000000005', 'Art & Craft Colors', 'art-craft', 'palette', 5, true)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  display_order = EXCLUDED.display_order,
  is_active = EXCLUDED.is_active;

INSERT INTO categories (id, name, slug, icon_name, display_order, is_active)
VALUES ('c0000000-0000-0000-0000-000000000006', 'Exam Essentials', 'exam-essentials', 'clipboard', 6, true)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  display_order = EXCLUDED.display_order,
  is_active = EXCLUDED.is_active;

INSERT INTO categories (id, name, slug, icon_name, display_order, is_active)
VALUES ('c0000000-0000-0000-0000-000000000007', 'Highlighters & Markers', 'highlighters', 'highlighter', 7, true)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  display_order = EXCLUDED.display_order,
  is_active = EXCLUDED.is_active;

INSERT INTO categories (id, name, slug, icon_name, display_order, is_active)
VALUES ('c0000000-0000-0000-0000-000000000008', 'Sticky Notes & Flags', 'sticky-notes', 'sticky', 8, true)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  display_order = EXCLUDED.display_order,
  is_active = EXCLUDED.is_active;

INSERT INTO categories (id, name, slug, icon_name, display_order, is_active)
VALUES ('c0000000-0000-0000-0000-000000000009', 'School Bags & Pouches', 'bags-pouches', 'backpack', 9, true)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  display_order = EXCLUDED.display_order,
  is_active = EXCLUDED.is_active;

INSERT INTO categories (id, name, slug, icon_name, display_order, is_active)
VALUES ('c0000000-0000-0000-0000-000000000010', 'Water Bottles & Lunch', 'bottles-lunch', 'bottle', 10, true)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  display_order = EXCLUDED.display_order,
  is_active = EXCLUDED.is_active;


-- 2. SEED STORES (Partner Hubs)
INSERT INTO stores (id, name, address, city, latitude, longitude, phone, delivery_radius_km, is_active, open_time, close_time)
VALUES ('s0000000-0000-0000-0000-000000000001', 'Vidya Book & Stationery Depot', 'No. 42, 12th Main Road, 4th Block, Koramangala', 'Bengaluru', 12.9352, 77.6245, '+91 80 2553 1234', 4.5, true, '07:30', '21:30')
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  address = EXCLUDED.address;

INSERT INTO stores (id, name, address, city, latitude, longitude, phone, delivery_radius_km, is_active, open_time, close_time)
VALUES ('s0000000-0000-0000-0000-000000000002', 'Campus Student Corner', 'Shop 7, 80 Feet Road, Indiranagar', 'Bengaluru', 12.9716, 77.6412, '+91 80 2525 5678', 5, true, '08:00', '22:00')
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  address = EXCLUDED.address;


-- 3. SEED PRODUCTS (30+ Stationery Items)
INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000001', 'Classmate Pulse Spiral Single Line Notebook', 'classmate-pulse-spiral-single-line', 'Single line, 180 pages, high-grade 70 GSM paper for smooth fountain and ball pen writing.', 'Classmate', 'c0000000-0000-0000-0000-000000000001', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500', 95, 110, 14, 50, 'book', '6th - 12th', true, true, '{"Pages":"180","Ruling":"Single Line","Paper":"70 GSM"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000002', 'Classmate 4-Line English Exercise Book', 'classmate-four-line-english-book', 'Primary school 4-line ruled notebook with red and blue margin guidelines.', 'Classmate', 'c0000000-0000-0000-0000-000000000001', 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500', 45, 50, 10, 80, 'book', '1st - 3rd', false, true, '{"Pages":"120","Ruling":"Four Line"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000003', 'Classmate Math Square Grid Notebook (Small)', 'classmate-math-square-grid-notebook', 'Square ruled notebook specifically for mathematics and numerical calculations.', 'Classmate', 'c0000000-0000-0000-0000-000000000001', 'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=500', 50, 55, 9, 65, 'book', '1st - 8th', false, true, '{"Pages":"172","Ruling":"Square Grid"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000004', 'Navneet Youva Soft Bound Long Notebook', 'navneet-youva-soft-bound-long-notebook', 'Hardcover long register for college, CBSE board examinations, and high school note taking.', 'Navneet', 'c0000000-0000-0000-0000-000000000001', 'https://images.unsplash.com/photo-1516962215378-7fa2e137ae93?w=500', 75, 85, 12, 45, 'register', '8th - 12th', true, true, '{"Pages":"160","Size":"A4"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000005', 'Hauser XO 0.7mm Ball Pen (Pack of 5, Blue)', 'hauser-xo-ball-pen-pack-of-5', 'Super fluid German ink technology with non-slip textured comfort grip.', 'Hauser', 'c0000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1585336261026-775af4609c0d?w=500', 50, 50, 0, 120, 'pack', 'All', true, true, '{"Tip":"0.7mm","Color":"Blue","Count":"5"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000006', 'Cello Butterflow Classic Blue Gel Pen (Pack of 3)', 'cello-butterflow-classic-blue', 'Lubriflow ink system for ultra-smooth skip-free school exam writing.', 'Cello', 'c0000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1569683795645-b62e50fbf103?w=500', 60, 75, 20, 100, 'pack', '6th - 12th', false, true, '{"Tip":"0.7mm","Color":"Blue"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000007', 'Uni-ball Eye Fine 0.7mm Roller Pen', 'uniball-eye-fine-07-roller-pen', 'Waterproof fade-proof pigment ink rollerball pen with stainless steel tip.', 'Uni-ball', 'c0000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500', 85, 95, 11, 40, 'piece', '8th - 12th', true, true, '{"Tip":"0.7mm Fine","Ink":"Black/Blue"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000008', 'Pilot Hi-Tecpoint V5 0.5mm Liquid Ink Pen', 'pilot-hi-tecpoint-v5-pen', 'Precision Japanese 0.5mm needle point tip for ultra-crisp diagram labeling.', 'Pilot', 'c0000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500', 65, 70, 7, 60, 'piece', '8th - 12th', false, true, '{"Tip":"0.5mm Needle","Ink":"Pure Liquid"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000009', 'Apsara Platinum Extra Dark Pencils (Box of 10)', 'apsara-platinum-extra-dark-pencils', 'Soft wood easy sharpening pencils with bonus sharpener and eraser included.', 'Apsara', 'c0000000-0000-0000-0000-000000000003', 'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=500', 70, 80, 13, 90, 'box', '1st - 10th', true, true, '{"Lead":"Extra Dark HB","Count":"10 Pencils"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000010', 'Nataraj Classic 621 Red & Black Pencils (Pack of 10)', 'nataraj-classic-621-pencils', 'The trusted school classic pencil for students across India.', 'Nataraj', 'c0000000-0000-0000-0000-000000000003', 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500', 55, 60, 8, 110, 'box', 'All', false, true, '{"Lead":"HB"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000011', 'Milan Capsule Dual Eraser & Sharpener', 'milan-capsule-eraser-sharpener', 'Compact Spanish eraser capsule with safety blade sharpener reservoir.', 'Milan', 'c0000000-0000-0000-0000-000000000003', 'https://images.unsplash.com/photo-1588854337221-4cf9fa96059c?w=500', 45, 50, 10, 75, 'piece', 'All', false, true, '{"Type":"Dust-free Eraser + Sharpener"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000012', 'Apsara Non-Dust Erasers (Pack of 5)', 'apsara-non-dust-erasers-pack-5', 'Pencil marks erased without creating loose messy graphite dust.', 'Apsara', 'c0000000-0000-0000-0000-000000000003', 'https://images.unsplash.com/photo-1587614382346-4ec70e388b28?w=500', 25, 30, 17, 140, 'pack', 'All', false, true, '{"Type":"Non-Dust","Count":"5"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000013', 'Camlin Scholar Mathematical Drawing Instruments Box', 'camlin-scholar-geometry-box', 'Self-centering compass, divider, protractor, set squares, and 15cm ruler in sturdy metal tin.', 'Camlin', 'c0000000-0000-0000-0000-000000000004', 'https://images.unsplash.com/photo-1509228468518-180dd4864904?w=500', 135, 150, 10, 35, 'tin', '6th - 12th', true, true, '{"Case":"Rust-free Metal Tin","Instruments":"9 items"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000014', 'Classmate Victor Precision Geometry Box', 'classmate-victor-geometry-box', 'Specially engineered die-cast compass for wobble-free circles in mathematics board exams.', 'Classmate', 'c0000000-0000-0000-0000-000000000004', 'https://images.unsplash.com/photo-1584697964190-7bb9348c5825?w=500', 160, 180, 11, 30, 'box', '8th - 12th', false, true, '{"Precision":"Die-cast gears"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000015', 'DOMS Transparent Acrylic 30cm Metric Scale', 'doms-transparent-acrylic-scale-30cm', 'Scratch-resistant transparent acrylic ruler with mm, cm and inch dual measurements.', 'DOMS', 'c0000000-0000-0000-0000-000000000004', 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=500', 20, 20, 0, 150, 'piece', 'All', false, true, '{"Length":"30 cm","Material":"Virgin Acrylic"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000016', 'DOMS Brush Pens (14 Shades with Blender)', 'doms-brush-pens-14-shades', 'Super-flexible nylon brush tips for calligraphy, lettering, and blending poster art.', 'DOMS', 'c0000000-0000-0000-0000-000000000005', 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=500', 199, 225, 12, 40, 'pack', '3rd - 12th', true, true, '{"Shades":"14 Colors","Tip":"Flexible Brush"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000017', 'Camlin Kokuyo Oil Pastels (25 Shades)', 'camlin-oil-pastels-25-shades', 'Bright vivid non-toxic oil pastels with scraping tool for rich art shading.', 'Camlin', 'c0000000-0000-0000-0000-000000000005', 'https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500', 110, 125, 12, 55, 'box', '1st - 8th', false, true, '{"Shades":"25 Shades","Tool":"Scraper Included"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000018', 'Faber-Castell Connector Sketch Pens (Pack of 20)', 'faber-castell-connector-pens-20', 'Washable bright food-grade dye sketch pens that connect together into crafts.', 'Faber-Castell', 'c0000000-0000-0000-0000-000000000005', 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500', 140, 160, 13, 60, 'pack', '1st - 8th', false, true, '{"Count":"20 Pens","Washable":"Yes"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000019', 'Pidilite Fevicryl Acrylic Colors Kit (6 Shades)', 'pidilite-fevicryl-acrylic-colors-6', 'Fast drying water-based colors suitable for canvas, cardboard, and school models.', 'Pidilite', 'c0000000-0000-0000-0000-000000000005', 'https://images.unsplash.com/photo-1572945753563-804956783134?w=500', 90, 100, 10, 50, 'kit', '4th - 12th', false, true, '{"Bottles":"6 x 15ml","Medium":"Multi-surface"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000020', 'Fevistik Glue Stick 15g (Mess-free Crafting)', 'fevistik-super-glue-stick-15g', 'Smooth lipstick-twist mechanism paper glue for clean craft projects.', 'Pidilite', 'c0000000-0000-0000-0000-000000000005', 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=500', 35, 40, 13, 120, 'stick', 'All', false, true, '{"Weight":"15 grams","Safe":"Non-toxic"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000021', 'KidsG Board Exam Approved Transparent Stationery Pouch', 'kidsg-transparent-exam-pouch', '100% transparent durable PVC zipper pouch conforming to CBSE/ICSE exam hall rules.', 'KidsG Essentials', 'c0000000-0000-0000-0000-000000000006', 'https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500', 65, 80, 19, 85, 'piece', '9th - 12th', true, true, '{"Material":"Clear PVC","Regulation":"CBSE / ICSE Board Compliant"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000022', 'Hardboard Wooden School Exam Writing Pad (A4)', 'hardboard-wooden-exam-clipboard', 'Smooth polished tempered clipboard with sturdy stainless steel clip.', 'Classmate', 'c0000000-0000-0000-0000-000000000006', 'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=500', 85, 99, 14, 45, 'piece', 'All', false, true, '{"Size":"A4","Clip":"Heavy duty spring"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000023', 'Faber-Castell 1546 Pastel Textliner Highlighters (Set of 4)', 'faber-castell-pastel-highlighters-4', 'Soft pastel water-based inks that don’t bleed through notebook paper.', 'Faber-Castell', 'c0000000-0000-0000-0000-000000000007', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500', 110, 130, 15, 65, 'pack', '6th - 12th', true, true, '{"Colors":"4 Pastel Shades","Tip":"Chisel"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000024', 'Camlin Whiteboard Markers with Duster (Set of 4)', 'camlin-whiteboard-markers-set-4', 'Dry wipe markers for student study rooms and teacher boards.', 'Camlin', 'c0000000-0000-0000-0000-000000000007', 'https://images.unsplash.com/photo-1585336261026-775af4609c0d?w=500', 120, 140, 14, 40, 'set', 'All', false, true, '{"Colors":"Black, Blue, Red, Green"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000025', '3M Post-it Canary Yellow Notes (3 x 3 inch, 100 Sheets)', '3m-post-it-yellow-notes', 'Genuine 3M repositionable adhesive notes for textbook bookmarks and revision tags.', '3M Post-it', 'c0000000-0000-0000-0000-000000000008', 'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=500', 55, 65, 15, 90, 'pad', 'All', true, true, '{"Size":"76mm x 76mm","Sheets":"100"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000026', 'Neon Index Arrow Page Marker Sticky Flags (5 Colors)', 'neon-index-page-markers-5-colors', 'Translucent self-adhesive tabs for indexing textbook chapters.', 'KidsG Essentials', 'c0000000-0000-0000-0000-000000000008', 'https://images.unsplash.com/photo-1588854337221-4cf9fa96059c?w=500', 40, 50, 20, 110, 'pack', '6th - 12th', false, true, '{"Strips":"125 tabs"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000027', 'KidsG Dual-Compartment Canvas Desk Pouch (Orange & Jet Black)', 'kidsg-dual-compartment-canvas-pouch', 'Signature KidsG branded pencil box with dedicated pen loops and mesh pocket.', 'KidsG', 'c0000000-0000-0000-0000-000000000009', 'https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500', 180, 249, 28, 35, 'piece', 'All', true, true, '{"Material":"600D Canvas","Pockets":"2 Main + 1 Mesh"}'::jsonb)
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

INSERT INTO products (id, name, slug, description, brand, category_id, image_url, price, mrp, discount_percent, stock, unit, grade_level, is_featured, is_active, specs)
VALUES ('p0000000-0000-0000-0000-000000000028', 'Milton Flip Lid Insulated Stainless Steel Bottle (500ml)', 'milton-thermosteel-bottle-500ml', 'Keeps water cold for 12 hours throughout long school and tuition days.', 'Milton', 'c0000000-0000-0000-0000-000000000010', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500', 380, 440, 14, 25, 'piece', 'All', false, true, '{"Capacity":"500 ml","Grade":"304 Stainless Steel"}'::jsonb)
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


-- 4. SEED COUPONS
INSERT INTO coupons (id, code, description, discount_type, discount_value, min_order_value, max_discount_amount, valid_until, is_active)
VALUES ('cp000000-0000-0000-0000-000000000001', 'KIDSG50', 'Flat ₹50 discount for school orders above ₹199', 'FLAT', 50, 199, NULL, '2026-12-31T23:59:59Z', true)
ON CONFLICT (code) DO UPDATE SET
  description = EXCLUDED.description,
  discount_value = EXCLUDED.discount_value,
  is_active = EXCLUDED.is_active;

INSERT INTO coupons (id, code, description, discount_type, discount_value, min_order_value, max_discount_amount, valid_until, is_active)
VALUES ('cp000000-0000-0000-0000-000000000002', 'FIRSTORDER', 'Flat ₹40 off for new students', 'FLAT', 40, 149, NULL, '2026-12-31T23:59:59Z', true)
ON CONFLICT (code) DO UPDATE SET
  description = EXCLUDED.description,
  discount_value = EXCLUDED.discount_value,
  is_active = EXCLUDED.is_active;

INSERT INTO coupons (id, code, description, discount_type, discount_value, min_order_value, max_discount_amount, valid_until, is_active)
VALUES ('cp000000-0000-0000-0000-000000000003', 'EXAMREADY', '15% discount on exam stationery essentials', 'PERCENTAGE', 15, 249, 75, '2026-12-31T23:59:59Z', true)
ON CONFLICT (code) DO UPDATE SET
  description = EXCLUDED.description,
  discount_value = EXCLUDED.discount_value,
  is_active = EXCLUDED.is_active;


-- 5. SEED PROFILES (Active Student Accounts)
INSERT INTO profiles (id, first_name, last_name, email, phone, role, onboarding_completed, selected_class, selected_school)
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
