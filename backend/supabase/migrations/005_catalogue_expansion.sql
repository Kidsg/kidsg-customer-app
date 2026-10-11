-- =============================================================================
-- Migration 005: Catalogue Expansion, Subcategories, Reviews, Q&A, and Bundles
-- =============================================================================

-- 1. Create Subcategories table
CREATE TABLE IF NOT EXISTS public.subcategories (
    id TEXT PRIMARY KEY,
    category_id TEXT NOT NULL REFERENCES public.categories(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    slug TEXT NOT NULL,
    icon_name TEXT,
    item_count INT DEFAULT 20,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index on category_id and slug
CREATE INDEX IF NOT EXISTS idx_subcategories_category_id ON public.subcategories(category_id);
CREATE INDEX IF NOT EXISTS idx_subcategories_slug ON public.subcategories(slug);

-- 2. Alter Products table to add subcategory, gallery, sku and badge
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS subcategory_id TEXT;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS gallery_images TEXT[] DEFAULT '{}';
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS sku TEXT DEFAULT '';
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS badge_text TEXT;

CREATE INDEX IF NOT EXISTS idx_products_subcategory_id ON public.products(subcategory_id);

-- 3. Create Product Reviews table
CREATE TABLE IF NOT EXISTS public.product_reviews (
    id TEXT PRIMARY KEY,
    product_id TEXT NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    author_name TEXT NOT NULL,
    author_role TEXT DEFAULT 'Student',
    rating NUMERIC(2, 1) NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
    date_text TEXT NOT NULL,
    comment TEXT NOT NULL,
    is_verified_purchase BOOLEAN DEFAULT TRUE,
    helpful_count INT DEFAULT 0,
    photo_urls TEXT[] DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_reviews_product_id ON public.product_reviews(product_id);

-- 4. Create Product Q&A table
CREATE TABLE IF NOT EXISTS public.product_qa (
    id TEXT PRIMARY KEY,
    product_id TEXT NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    answered_by TEXT DEFAULT 'KidsG Support',
    answered_date_text TEXT DEFAULT 'Recently verified',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_qa_product_id ON public.product_qa(product_id);

-- 5. Create Product Bundles table
CREATE TABLE IF NOT EXISTS public.product_bundles (
    id TEXT PRIMARY KEY,
    product_id TEXT NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    title TEXT DEFAULT 'Frequently Bought Together',
    discount_percent INT DEFAULT 10,
    bundled_product_ids TEXT[] NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_bundles_product_id ON public.product_bundles(product_id);

-- Enable RLS
ALTER TABLE public.subcategories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.product_reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.product_qa ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.product_bundles ENABLE ROW LEVEL SECURITY;

-- Read policies for public access
CREATE POLICY "Public read access for subcategories" ON public.subcategories FOR SELECT USING (true);
CREATE POLICY "Public read access for product reviews" ON public.product_reviews FOR SELECT USING (true);
CREATE POLICY "Public read access for product qa" ON public.product_qa FOR SELECT USING (true);
CREATE POLICY "Public read access for product bundles" ON public.product_bundles FOR SELECT USING (true);

-- Insert Subcategories
INSERT INTO public.subcategories (id, category_id, name, slug, icon_name, item_count, display_order)
VALUES
    ('sub_notebooks_single', 'cat_stationery', 'Single Line Notebooks', 'notebooks-single', 'Notebook', 22, 1),
    ('sub_notebooks_four', 'cat_stationery', 'Four Line Notebooks', 'notebooks-four', 'Notebook', 20, 2),
    ('sub_notebooks_grid', 'cat_stationery', 'Square Grid Maths', 'notebooks-grid', 'Notebook', 20, 3),
    ('sub_notebooks_unruled', 'cat_stationery', 'Unruled Drawing & Rough', 'notebooks-unruled', 'Notebook', 20, 4),
    ('sub_pens_ball', 'cat_stationery', 'Ball Point Pens', 'pens-ball', 'Pencil', 21, 5),
    ('sub_pens_gel', 'cat_stationery', 'Gel Pens', 'pens-gel', 'Pencil', 20, 6),
    ('sub_pencils_graphite', 'cat_stationery', 'Graphite Pencils', 'pencils-graphite', 'Pencil', 21, 7),
    ('sub_erasers', 'cat_stationery', 'Dust-Free Erasers', 'erasers', 'Eraser', 21, 8),
    ('sub_sharpeners', 'cat_stationery', 'Precision Sharpeners', 'sharpeners', 'Sharpener', 21, 9),
    ('sub_geometry_boxes', 'cat_stationery', 'Geometry Box Sets', 'geometry-boxes', 'Geometry', 21, 10),
    ('sub_drawing_colours', 'cat_stationery', 'Colouring & Drawing', 'drawing-colours', 'Palette', 21, 11),
    ('sub_crafts', 'cat_stationery', 'Craft & Origami', 'crafts', 'Craft', 21, 12),
    ('sub_bags', 'cat_bags', 'Ergonomic Backpacks', 'bags', 'SchoolBag', 21, 13),
    ('sub_bottles', 'cat_bottles', 'Insulated Stainless Steel Bottles', 'bottles', 'WaterBottle', 21, 14),
    ('sub_lunch', 'cat_bottles', 'Stainless Steel Lunch Boxes', 'lunch', 'LunchBox', 21, 15),
    ('sub_kits', 'cat_bundles', 'School Starter Kits', 'kits', 'SchoolBag', 21, 16)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    item_count = EXCLUDED.item_count,
    display_order = EXCLUDED.display_order;
