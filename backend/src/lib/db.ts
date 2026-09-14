import { randomUUID } from 'crypto';
import { env } from '../config/env.js';

export interface Category {
  id: string;
  name: string;
  slug: string;
  iconName: string;
  displayOrder: number;
  isActive: boolean;
}

export interface Product {
  id: string;
  name: string;
  slug: string;
  description: string;
  brand: string;
  categoryId: string;
  categoryName?: string;
  imageUrl: string;
  price: number;
  mrp: number;
  discountPercent: number;
  stock: number;
  unit: string;
  gradeLevel: string;
  isFeatured: boolean;
  isActive: boolean;
  specs: Record<string, string>;
}

export interface Store {
  id: string;
  name: string;
  address: string;
  city: string;
  latitude: number;
  longitude: number;
  phone: string;
  deliveryRadiusKm: number;
  isActive: boolean;
  openTime: string;
  closeTime: string;
}

export interface CartItem {
  id: string;
  productId: string;
  quantity: number;
  selectedVariant?: string;
  product?: Product;
}

export interface Address {
  id: string;
  userId: string;
  label: string;
  name: string;
  phone: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  postalCode: string;
  latitude?: number;
  longitude?: number;
  deliveryInstructions?: string;
  isDefault: boolean;
}

export interface Coupon {
  id: string;
  code: string;
  description: string;
  discountType: 'FLAT' | 'PERCENTAGE';
  discountValue: number;
  minOrderValue: number;
  maxDiscountAmount?: number;
  validUntil: string;
  isActive: boolean;
}

export type OrderStatus =
  | 'PENDING_PAYMENT'
  | 'PAYMENT_CONFIRMED'
  | 'CONFIRMED'
  | 'PREPARING'
  | 'READY_FOR_PICKUP'
  | 'PICKED_UP'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'REFUNDED';

export interface OrderItem {
  id: string;
  productId: string;
  productName: string;
  price: number;
  mrp: number;
  quantity: number;
  variant?: string;
}

export interface Order {
  id: string;
  orderNumber: string;
  userId: string;
  storeId: string;
  storeName: string;
  status: OrderStatus;
  subtotal: number;
  discount: number;
  couponDiscount: number;
  couponCode?: string;
  deliveryFee: number;
  tax: number;
  total: number;
  paymentStatus: 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
  paymentMethod: string;
  deliveryStatus: string;
  addressSnapshot: Address;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface SupportTicket {
  id: string;
  ticketNumber: string;
  userId: string;
  orderId?: string;
  subject: string;
  message: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  priority: 'LOW' | 'NORMAL' | 'HIGH';
  createdAt: string;
}

// In-Memory Master Store initialized with development seed data
export class KidsGDatabase {
  private categories: Category[] = [];
  private products: Product[] = [];
  private stores: Store[] = [];
  private storeInventory: Map<string, number> = new Map(); // storeId_productId -> quantity
  private coupons: Coupon[] = [];
  private carts: Map<string, CartItem[]> = new Map(); // userId -> items
  private wishlists: Map<string, Set<string>> = new Map(); // userId -> Set<productId>
  private addresses: Map<string, Address[]> = new Map(); // userId -> addresses
  private orders: Map<string, Order> = new Map(); // orderId -> Order
  private tickets: SupportTicket[] = [];
  private userProfiles: Map<string, any> = new Map();

  constructor() {
    this.seedInitialData();
  }

  private seedInitialData() {
    // 10 Stationery Categories
    this.categories = [
      { id: 'cat_notebooks', name: 'Notebooks & Registers', slug: 'notebooks', iconName: 'notebook', displayOrder: 1, isActive: true },
      { id: 'cat_pens', name: 'Pens & Refills', slug: 'pens', iconName: 'pen', displayOrder: 2, isActive: true },
      { id: 'cat_pencils', name: 'Pencils & Erasers', slug: 'pencils', iconName: 'pencil', displayOrder: 3, isActive: true },
      { id: 'cat_geometry', name: 'Geometry & Scales', slug: 'geometry', iconName: 'ruler', displayOrder: 4, isActive: true },
      { id: 'cat_art', name: 'Art & Craft Colors', slug: 'art-craft', iconName: 'palette', displayOrder: 5, isActive: true },
      { id: 'cat_exam', name: 'Exam Essentials', slug: 'exam-essentials', iconName: 'clipboard', displayOrder: 6, isActive: true },
      { id: 'cat_highlighters', name: 'Highlighters & Markers', slug: 'highlighters', iconName: 'highlighter', displayOrder: 7, isActive: true },
      { id: 'cat_sticky', name: 'Sticky Notes & Flags', slug: 'sticky-notes', iconName: 'sticky', displayOrder: 8, isActive: true },
      { id: 'cat_bags', name: 'School Bags & Pouches', slug: 'bags-pouches', iconName: 'backpack', displayOrder: 9, isActive: true },
      { id: 'cat_bottles', name: 'Water Bottles & Lunch', slug: 'bottles-lunch', iconName: 'bottle', displayOrder: 10, isActive: true },
    ];

    // 30+ Realistic Stationery Products (Matching KidsG Desk Aesthetic)
    this.products = [
      // Notebooks
      {
        id: 'prod_classmate_single_line',
        name: 'Classmate Pulse Spiral Single Line Notebook',
        slug: 'classmate-pulse-spiral-single-line',
        description: 'Single line, 180 pages, high-grade 70 GSM paper for smooth fountain and ball pen writing.',
        brand: 'Classmate',
        categoryId: 'cat_notebooks',
        categoryName: 'Notebooks & Registers',
        imageUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500',
        price: 95,
        mrp: 110,
        discountPercent: 14,
        stock: 50,
        unit: 'book',
        gradeLevel: '6th - 12th',
        isFeatured: true,
        isActive: true,
        specs: { Pages: '180', Ruling: 'Single Line', Paper: '70 GSM' },
      },
      {
        id: 'prod_classmate_four_line',
        name: 'Classmate 4-Line English Exercise Book',
        slug: 'classmate-four-line-english-book',
        description: 'Primary school 4-line ruled notebook with red and blue margin guidelines.',
        brand: 'Classmate',
        categoryId: 'cat_notebooks',
        categoryName: 'Notebooks & Registers',
        imageUrl: 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500',
        price: 45,
        mrp: 50,
        discountPercent: 10,
        stock: 80,
        unit: 'book',
        gradeLevel: '1st - 3rd',
        isFeatured: false,
        isActive: true,
        specs: { Pages: '120', Ruling: 'Four Line' },
      },
      {
        id: 'prod_classmate_grid_math',
        name: 'Classmate Math Square Grid Notebook (Small)',
        slug: 'classmate-math-square-grid-notebook',
        description: 'Square ruled notebook specifically for mathematics and numerical calculations.',
        brand: 'Classmate',
        categoryId: 'cat_notebooks',
        categoryName: 'Notebooks & Registers',
        imageUrl: 'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=500',
        price: 50,
        mrp: 55,
        discountPercent: 9,
        stock: 65,
        unit: 'book',
        gradeLevel: '1st - 8th',
        isFeatured: false,
        isActive: true,
        specs: { Pages: '172', Ruling: 'Square Grid' },
      },
      {
        id: 'prod_navneet_long_book',
        name: 'Navneet Youva Soft Bound Long Notebook',
        slug: 'navneet-youva-soft-bound-long-notebook',
        description: 'Hardcover long register for college, CBSE board examinations, and high school note taking.',
        brand: 'Navneet',
        categoryId: 'cat_notebooks',
        categoryName: 'Notebooks & Registers',
        imageUrl: 'https://images.unsplash.com/photo-1516962215378-7fa2e137ae93?w=500',
        price: 75,
        mrp: 85,
        discountPercent: 12,
        stock: 45,
        unit: 'register',
        gradeLevel: '8th - 12th',
        isFeatured: true,
        isActive: true,
        specs: { Pages: '160', Size: 'A4' },
      },

      // Pens
      {
        id: 'prod_hauser_xo_ball',
        name: 'Hauser XO 0.7mm Ball Pen (Pack of 5, Blue)',
        slug: 'hauser-xo-ball-pen-pack-of-5',
        description: 'Super fluid German ink technology with non-slip textured comfort grip.',
        brand: 'Hauser',
        categoryId: 'cat_pens',
        categoryName: 'Pens & Refills',
        imageUrl: 'https://images.unsplash.com/photo-1585336261026-775af4609c0d?w=500',
        price: 50,
        mrp: 50,
        discountPercent: 0,
        stock: 120,
        unit: 'pack',
        gradeLevel: 'All',
        isFeatured: true,
        isActive: true,
        specs: { Tip: '0.7mm', Color: 'Blue', Count: '5' },
      },
      {
        id: 'prod_cello_butterflow_pack',
        name: 'Cello Butterflow Classic Blue Gel Pen (Pack of 3)',
        slug: 'cello-butterflow-classic-blue',
        description: 'Lubriflow ink system for ultra-smooth skip-free school exam writing.',
        brand: 'Cello',
        categoryId: 'cat_pens',
        categoryName: 'Pens & Refills',
        imageUrl: 'https://images.unsplash.com/photo-1569683795645-b62e50fbf103?w=500',
        price: 60,
        mrp: 75,
        discountPercent: 20,
        stock: 100,
        unit: 'pack',
        gradeLevel: '6th - 12th',
        isFeatured: false,
        isActive: true,
        specs: { Tip: '0.7mm', Color: 'Blue' },
      },
      {
        id: 'prod_uniball_eye_roller',
        name: 'Uni-ball Eye Fine 0.7mm Roller Pen',
        slug: 'uniball-eye-fine-07-roller-pen',
        description: 'Waterproof fade-proof pigment ink rollerball pen with stainless steel tip.',
        brand: 'Uni-ball',
        categoryId: 'cat_pens',
        categoryName: 'Pens & Refills',
        imageUrl: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500',
        price: 85,
        mrp: 95,
        discountPercent: 11,
        stock: 40,
        unit: 'piece',
        gradeLevel: '8th - 12th',
        isFeatured: true,
        isActive: true,
        specs: { Tip: '0.7mm Fine', Ink: 'Black/Blue' },
      },
      {
        id: 'prod_pilot_v5_liquid',
        name: 'Pilot Hi-Tecpoint V5 0.5mm Liquid Ink Pen',
        slug: 'pilot-hi-tecpoint-v5-pen',
        description: 'Precision Japanese 0.5mm needle point tip for ultra-crisp diagram labeling.',
        brand: 'Pilot',
        categoryId: 'cat_pens',
        categoryName: 'Pens & Refills',
        imageUrl: 'https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500',
        price: 65,
        mrp: 70,
        discountPercent: 7,
        stock: 60,
        unit: 'piece',
        gradeLevel: '8th - 12th',
        isFeatured: false,
        isActive: true,
        specs: { Tip: '0.5mm Needle', Ink: 'Pure Liquid' },
      },

      // Pencils & Erasers
      {
        id: 'prod_apsara_platinum_box',
        name: 'Apsara Platinum Extra Dark Pencils (Box of 10)',
        slug: 'apsara-platinum-extra-dark-pencils',
        description: 'Soft wood easy sharpening pencils with bonus sharpener and eraser included.',
        brand: 'Apsara',
        categoryId: 'cat_pencils',
        categoryName: 'Pencils & Erasers',
        imageUrl: 'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=500',
        price: 70,
        mrp: 80,
        discountPercent: 13,
        stock: 90,
        unit: 'box',
        gradeLevel: '1st - 10th',
        isFeatured: true,
        isActive: true,
        specs: { Lead: 'Extra Dark HB', Count: '10 Pencils' },
      },
      {
        id: 'prod_natraj_classic_box',
        name: 'Nataraj Classic 621 Red & Black Pencils (Pack of 10)',
        slug: 'nataraj-classic-621-pencils',
        description: 'The trusted school classic pencil for students across India.',
        brand: 'Nataraj',
        categoryId: 'cat_pencils',
        categoryName: 'Pencils & Erasers',
        imageUrl: 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500',
        price: 55,
        mrp: 60,
        discountPercent: 8,
        stock: 110,
        unit: 'box',
        gradeLevel: 'All',
        isFeatured: false,
        isActive: true,
        specs: { Lead: 'HB' },
      },
      {
        id: 'prod_milan_capsule_eraser',
        name: 'Milan Capsule Dual Eraser & Sharpener',
        slug: 'milan-capsule-eraser-sharpener',
        description: 'Compact Spanish eraser capsule with safety blade sharpener reservoir.',
        brand: 'Milan',
        categoryId: 'cat_pencils',
        categoryName: 'Pencils & Erasers',
        imageUrl: 'https://images.unsplash.com/photo-1588854337221-4cf9fa96059c?w=500',
        price: 45,
        mrp: 50,
        discountPercent: 10,
        stock: 75,
        unit: 'piece',
        gradeLevel: 'All',
        isFeatured: false,
        isActive: true,
        specs: { Type: 'Dust-free Eraser + Sharpener' },
      },
      {
        id: 'prod_apsara_non_dust_erasers',
        name: 'Apsara Non-Dust Erasers (Pack of 5)',
        slug: 'apsara-non-dust-erasers-pack-5',
        description: 'Pencil marks erased without creating loose messy graphite dust.',
        brand: 'Apsara',
        categoryId: 'cat_pencils',
        categoryName: 'Pencils & Erasers',
        imageUrl: 'https://images.unsplash.com/photo-1587614382346-4ec70e388b28?w=500',
        price: 25,
        mrp: 30,
        discountPercent: 17,
        stock: 140,
        unit: 'pack',
        gradeLevel: 'All',
        isFeatured: false,
        isActive: true,
        specs: { Type: 'Non-Dust', Count: '5' },
      },

      // Geometry & Scales
      {
        id: 'prod_camlin_scholar_geometry',
        name: 'Camlin Scholar Mathematical Drawing Instruments Box',
        slug: 'camlin-scholar-geometry-box',
        description: 'Self-centering compass, divider, protractor, set squares, and 15cm ruler in sturdy metal tin.',
        brand: 'Camlin',
        categoryId: 'cat_geometry',
        categoryName: 'Geometry & Scales',
        imageUrl: 'https://images.unsplash.com/photo-1509228468518-180dd4864904?w=500',
        price: 135,
        mrp: 150,
        discountPercent: 10,
        stock: 35,
        unit: 'tin',
        gradeLevel: '6th - 12th',
        isFeatured: true,
        isActive: true,
        specs: { Case: 'Rust-free Metal Tin', Instruments: '9 items' },
      },
      {
        id: 'prod_classmate_inventor_geometry',
        name: 'Classmate Victor Precision Geometry Box',
        slug: 'classmate-victor-geometry-box',
        description: 'Specially engineered die-cast compass for wobble-free circles in mathematics board exams.',
        brand: 'Classmate',
        categoryId: 'cat_geometry',
        categoryName: 'Geometry & Scales',
        imageUrl: 'https://images.unsplash.com/photo-1584697964190-7bb9348c5825?w=500',
        price: 160,
        mrp: 180,
        discountPercent: 11,
        stock: 30,
        unit: 'box',
        gradeLevel: '8th - 12th',
        isFeatured: false,
        isActive: true,
        specs: { Precision: 'Die-cast gears' },
      },
      {
        id: 'prod_doms_clear_ruler_30cm',
        name: 'DOMS Transparent Acrylic 30cm Metric Scale',
        slug: 'doms-transparent-acrylic-scale-30cm',
        description: 'Scratch-resistant transparent acrylic ruler with mm, cm and inch dual measurements.',
        brand: 'DOMS',
        categoryId: 'cat_geometry',
        categoryName: 'Geometry & Scales',
        imageUrl: 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=500',
        price: 20,
        mrp: 20,
        discountPercent: 0,
        stock: 150,
        unit: 'piece',
        gradeLevel: 'All',
        isFeatured: false,
        isActive: true,
        specs: { Length: '30 cm', Material: 'Virgin Acrylic' },
      },

      // Art & Craft
      {
        id: 'prod_doms_brush_pens_14',
        name: 'DOMS Brush Pens (14 Shades with Blender)',
        slug: 'doms-brush-pens-14-shades',
        description: 'Super-flexible nylon brush tips for calligraphy, lettering, and blending poster art.',
        brand: 'DOMS',
        categoryId: 'cat_art',
        categoryName: 'Art & Craft Colors',
        imageUrl: 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=500',
        price: 199,
        mrp: 225,
        discountPercent: 12,
        stock: 40,
        unit: 'pack',
        gradeLevel: '3rd - 12th',
        isFeatured: true,
        isActive: true,
        specs: { Shades: '14 Colors', Tip: 'Flexible Brush' },
      },
      {
        id: 'prod_camlin_oil_pastels_25',
        name: 'Camlin Kokuyo Oil Pastels (25 Shades)',
        slug: 'camlin-oil-pastels-25-shades',
        description: 'Bright vivid non-toxic oil pastels with scraping tool for rich art shading.',
        brand: 'Camlin',
        categoryId: 'cat_art',
        categoryName: 'Art & Craft Colors',
        imageUrl: 'https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500',
        price: 110,
        mrp: 125,
        discountPercent: 12,
        stock: 55,
        unit: 'box',
        gradeLevel: '1st - 8th',
        isFeatured: false,
        isActive: true,
        specs: { Shades: '25 Shades', Tool: 'Scraper Included' },
      },
      {
        id: 'prod_faber_castell_sketch_pack',
        name: 'Faber-Castell Connector Sketch Pens (Pack of 20)',
        slug: 'faber-castell-connector-pens-20',
        description: 'Washable bright food-grade dye sketch pens that connect together into crafts.',
        brand: 'Faber-Castell',
        categoryId: 'cat_art',
        categoryName: 'Art & Craft Colors',
        imageUrl: 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500',
        price: 140,
        mrp: 160,
        discountPercent: 13,
        stock: 60,
        unit: 'pack',
        gradeLevel: '1st - 8th',
        isFeatured: false,
        isActive: true,
        specs: { Count: '20 Pens', Washable: 'Yes' },
      },
      {
        id: 'prod_pidilite_fevicryl_acrylic',
        name: 'Pidilite Fevicryl Acrylic Colors Kit (6 Shades)',
        slug: 'pidilite-fevicryl-acrylic-colors-6',
        description: 'Fast drying water-based colors suitable for canvas, cardboard, and school models.',
        brand: 'Pidilite',
        categoryId: 'cat_art',
        categoryName: 'Art & Craft Colors',
        imageUrl: 'https://images.unsplash.com/photo-1572945753563-804956783134?w=500',
        price: 90,
        mrp: 100,
        discountPercent: 10,
        stock: 50,
        unit: 'kit',
        gradeLevel: '4th - 12th',
        isFeatured: false,
        isActive: true,
        specs: { Bottles: '6 x 15ml', Medium: 'Multi-surface' },
      },
      {
        id: 'prod_fevistick_super_glue',
        name: 'Fevistik Glue Stick 15g (Mess-free Crafting)',
        slug: 'fevistik-super-glue-stick-15g',
        description: 'Smooth lipstick-twist mechanism paper glue for clean craft projects.',
        brand: 'Pidilite',
        categoryId: 'cat_art',
        categoryName: 'Art & Craft Colors',
        imageUrl: 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=500',
        price: 35,
        mrp: 40,
        discountPercent: 13,
        stock: 120,
        unit: 'stick',
        gradeLevel: 'All',
        isFeatured: false,
        isActive: true,
        specs: { Weight: '15 grams', Safe: 'Non-toxic' },
      },

      // Exam Essentials
      {
        id: 'prod_transparent_exam_pouch',
        name: 'KidsG Board Exam Approved Transparent Stationery Pouch',
        slug: 'kidsg-transparent-exam-pouch',
        description: '100% transparent durable PVC zipper pouch conforming to CBSE/ICSE exam hall rules.',
        brand: 'KidsG Essentials',
        categoryId: 'cat_exam',
        categoryName: 'Exam Essentials',
        imageUrl: 'https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500',
        price: 65,
        mrp: 80,
        discountPercent: 19,
        stock: 85,
        unit: 'piece',
        gradeLevel: '9th - 12th',
        isFeatured: true,
        isActive: true,
        specs: { Material: 'Clear PVC', Regulation: 'CBSE / ICSE Board Compliant' },
      },
      {
        id: 'prod_wooden_exam_clipboard',
        name: 'Hardboard Wooden School Exam Writing Pad (A4)',
        slug: 'hardboard-wooden-exam-clipboard',
        description: 'Smooth polished tempered clipboard with sturdy stainless steel clip.',
        brand: 'Classmate',
        categoryId: 'cat_exam',
        categoryName: 'Exam Essentials',
        imageUrl: 'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=500',
        price: 85,
        mrp: 99,
        discountPercent: 14,
        stock: 45,
        unit: 'piece',
        gradeLevel: 'All',
        isFeatured: false,
        isActive: true,
        specs: { Size: 'A4', Clip: 'Heavy duty spring' },
      },

      // Highlighters & Markers
      {
        id: 'prod_faber_textliner_pastel',
        name: 'Faber-Castell 1546 Pastel Textliner Highlighters (Set of 4)',
        slug: 'faber-castell-pastel-highlighters-4',
        description: 'Soft pastel water-based inks that don’t bleed through notebook paper.',
        brand: 'Faber-Castell',
        categoryId: 'cat_highlighters',
        categoryName: 'Highlighters & Markers',
        imageUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500',
        price: 110,
        mrp: 130,
        discountPercent: 15,
        stock: 65,
        unit: 'pack',
        gradeLevel: '6th - 12th',
        isFeatured: true,
        isActive: true,
        specs: { Colors: '4 Pastel Shades', Tip: 'Chisel' },
      },
      {
        id: 'prod_camlin_whiteboard_markers',
        name: 'Camlin Whiteboard Markers with Duster (Set of 4)',
        slug: 'camlin-whiteboard-markers-set-4',
        description: 'Dry wipe markers for student study rooms and teacher boards.',
        brand: 'Camlin',
        categoryId: 'cat_highlighters',
        categoryName: 'Highlighters & Markers',
        imageUrl: 'https://images.unsplash.com/photo-1585336261026-775af4609c0d?w=500',
        price: 120,
        mrp: 140,
        discountPercent: 14,
        stock: 40,
        unit: 'set',
        gradeLevel: 'All',
        isFeatured: false,
        isActive: true,
        specs: { Colors: 'Black, Blue, Red, Green' },
      },

      // Sticky Notes
      {
        id: 'prod_3m_post_it_yellow',
        name: '3M Post-it Canary Yellow Notes (3 x 3 inch, 100 Sheets)',
        slug: '3m-post-it-yellow-notes',
        description: 'Genuine 3M repositionable adhesive notes for textbook bookmarks and revision tags.',
        brand: '3M Post-it',
        categoryId: 'cat_sticky',
        categoryName: 'Sticky Notes & Flags',
        imageUrl: 'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=500',
        price: 55,
        mrp: 65,
        discountPercent: 15,
        stock: 90,
        unit: 'pad',
        gradeLevel: 'All',
        isFeatured: true,
        isActive: true,
        specs: { Size: '76mm x 76mm', Sheets: '100' },
      },
      {
        id: 'prod_page_marker_flags',
        name: 'Neon Index Arrow Page Marker Sticky Flags (5 Colors)',
        slug: 'neon-index-page-markers-5-colors',
        description: 'Translucent self-adhesive tabs for indexing textbook chapters.',
        brand: 'KidsG Essentials',
        categoryId: 'cat_sticky',
        categoryName: 'Sticky Notes & Flags',
        imageUrl: 'https://images.unsplash.com/photo-1588854337221-4cf9fa96059c?w=500',
        price: 40,
        mrp: 50,
        discountPercent: 20,
        stock: 110,
        unit: 'pack',
        gradeLevel: '6th - 12th',
        isFeatured: false,
        isActive: true,
        specs: { Strips: '125 tabs' },
      },

      // School Bags & Pouches
      {
        id: 'prod_kidsg_desk_pouch_canvas',
        name: 'KidsG Dual-Compartment Canvas Desk Pouch (Orange & Jet Black)',
        slug: 'kidsg-dual-compartment-canvas-pouch',
        description: 'Signature KidsG branded pencil box with dedicated pen loops and mesh pocket.',
        brand: 'KidsG',
        categoryId: 'cat_bags',
        categoryName: 'School Bags & Pouches',
        imageUrl: 'https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500',
        price: 180,
        mrp: 249,
        discountPercent: 28,
        stock: 35,
        unit: 'piece',
        gradeLevel: 'All',
        isFeatured: true,
        isActive: true,
        specs: { Material: '600D Canvas', Pockets: '2 Main + 1 Mesh' },
      },

      // Water Bottles
      {
        id: 'prod_milton_thermosteel_bottle',
        name: 'Milton Flip Lid Insulated Stainless Steel Bottle (500ml)',
        slug: 'milton-thermosteel-bottle-500ml',
        description: 'Keeps water cold for 12 hours throughout long school and tuition days.',
        brand: 'Milton',
        categoryId: 'cat_bottles',
        categoryName: 'Water Bottles & Lunch',
        imageUrl: 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500',
        price: 380,
        mrp: 440,
        discountPercent: 14,
        stock: 25,
        unit: 'piece',
        gradeLevel: 'All',
        isFeatured: false,
        isActive: true,
        specs: { Capacity: '500 ml', Grade: '304 Stainless Steel' },
      },
    ];

    // Local Partner Stationery Stores
    this.stores = [
      {
        id: 'store_vidya_depot',
        name: 'Vidya Book & Stationery Depot',
        address: 'No. 42, 12th Main Road, 4th Block, Koramangala',
        city: 'Bengaluru',
        latitude: 12.9352,
        longitude: 77.6245,
        phone: '+91 80 2553 1234',
        deliveryRadiusKm: 4.5,
        isActive: true,
        openTime: '07:30',
        closeTime: '21:30',
      },
      {
        id: 'store_campus_books',
        name: 'Campus Student Corner',
        address: 'Shop 7, 80 Feet Road, Indiranagar',
        city: 'Bengaluru',
        latitude: 12.9716,
        longitude: 77.6412,
        phone: '+91 80 2525 5678',
        deliveryRadiusKm: 5.0,
        isActive: true,
        openTime: '08:00',
        closeTime: '22:00',
      },
    ];

    // Coupons
    this.coupons = [
      {
        id: 'coup_kidsg50',
        code: 'KIDSG50',
        description: 'Flat ₹50 discount for school orders above ₹199',
        discountType: 'FLAT',
        discountValue: 50,
        minOrderValue: 199,
        validUntil: '2026-12-31T23:59:59Z',
        isActive: true,
      },
      {
        id: 'coup_firstorder',
        code: 'FIRSTORDER',
        description: 'Flat ₹40 off for new students',
        discountType: 'FLAT',
        discountValue: 40,
        minOrderValue: 149,
        validUntil: '2026-12-31T23:59:59Z',
        isActive: true,
      },
      {
        id: 'coup_examready',
        code: 'EXAMREADY',
        description: '15% discount on exam stationery essentials',
        discountType: 'PERCENTAGE',
        discountValue: 15,
        minOrderValue: 249,
        maxDiscountAmount: 75,
        validUntil: '2026-12-31T23:59:59Z',
        isActive: true,
      },
    ];

    // Default development address for user_dev_default
    const defaultAddress: Address = {
      id: 'addr_dev_default',
      userId: 'user_dev_default',
      label: 'Home',
      name: 'Aarav Sharma',
      phone: '+91 98765 43210',
      addressLine1: 'Flat 302, Sunrise Orchid Apartments',
      addressLine2: '14th Cross, 5th Block',
      city: 'Bengaluru',
      state: 'Karnataka',
      postalCode: '560034',
      latitude: 12.9358,
      longitude: 77.6251,
      deliveryInstructions: 'Ring doorbell twice. Student studying.',
      isDefault: true,
    };
    this.addresses.set('user_dev_default', [defaultAddress]);

    // Default Profile
    this.userProfiles.set('user_dev_default', {
      id: 'user_dev_default',
      authUserId: 'user_dev_default',
      firstName: 'Aarav',
      lastName: 'Sharma',
      phone: '+91 98765 43210',
      email: 'aarav@kidsg.in',
      avatarUrl: '',
      role: 'CUSTOMER',
      onboardingCompleted: true,
      selectedClass: 'Class 7',
      selectedSchool: 'National Public School, Koramangala',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    });
  }

  // --- Category Operations ---
  getCategories(): Category[] {
    return this.categories.filter(c => c.isActive).sort((a, b) => a.displayOrder - b.displayOrder);
  }

  getCategoryById(id: string): Category | undefined {
    return this.categories.find(c => (c.id === id || c.slug === id) && c.isActive);
  }

  // --- Product Operations ---
  getProducts(params?: {
    search?: string;
    categoryId?: string;
    brand?: string;
    featured?: boolean;
    page?: number;
    limit?: number;
    sort?: string;
  }): { products: Product[]; total: number; page: number; limit: number } {
    let result = this.products.filter(p => p.isActive);

    if (params?.search) {
      const q = params.search.toLowerCase();
      result = result.filter(
        p =>
          p.name.toLowerCase().includes(q) ||
          p.brand.toLowerCase().includes(q) ||
          p.description.toLowerCase().includes(q) ||
          p.categoryName?.toLowerCase().includes(q)
      );
    }

    if (params?.categoryId) {
      const catId = params.categoryId.toLowerCase();
      result = result.filter(p => p.categoryId === params.categoryId || p.categoryName?.toLowerCase() === catId);
    }

    if (params?.brand) {
      result = result.filter(p => p.brand.toLowerCase() === params.brand?.toLowerCase());
    }

    if (params?.featured !== undefined) {
      result = result.filter(p => p.isFeatured === params.featured);
    }

    if (params?.sort === 'price_asc') {
      result.sort((a, b) => a.price - b.price);
    } else if (params?.sort === 'price_desc') {
      result.sort((a, b) => b.price - a.price);
    }

    const total = result.length;
    const page = params?.page || 1;
    const limit = params?.limit || 20;
    const start = (page - 1) * limit;
    const paginated = result.slice(start, start + limit);

    return { products: paginated, total, page, limit };
  }

  getProductById(id: string): Product | undefined {
    return this.products.find(p => (p.id === id || p.slug === id) && p.isActive);
  }

  // --- Store Operations ---
  getStores(): Store[] {
    return this.stores.filter(s => s.isActive);
  }

  getStoreById(id: string): Store | undefined {
    return this.stores.find(s => s.id === id && s.isActive);
  }

  // --- Wishlist Operations ---
  getWishlist(userId: string): Product[] {
    const ids = this.wishlists.get(userId) || new Set();
    return this.products.filter(p => ids.has(p.id) && p.isActive);
  }

  addToWishlist(userId: string, productId: string): boolean {
    if (!this.products.some(p => p.id === productId)) return false;
    if (!this.wishlists.has(userId)) {
      this.wishlists.set(userId, new Set());
    }
    this.wishlists.get(userId)!.add(productId);
    return true;
  }

  removeFromWishlist(userId: string, productId: string): boolean {
    const list = this.wishlists.get(userId);
    if (!list) return false;
    return list.delete(productId);
  }

  // --- Cart Operations (SERVER AUTHORITATIVE PRICES) ---
  getCart(userId: string): { items: CartItem[]; subtotal: number; totalItems: number } {
    const items = this.carts.get(userId) || [];
    let subtotal = 0;
    let totalItems = 0;

    const populatedItems: CartItem[] = [];
    for (const item of items) {
      const prod = this.getProductById(item.productId);
      if (prod) {
        subtotal += prod.price * item.quantity;
        totalItems += item.quantity;
        populatedItems.push({
          ...item,
          product: prod,
        });
      }
    }

    return { items: populatedItems, subtotal, totalItems };
  }

  addToCart(userId: string, productId: string, quantity = 1, variant?: string): { success: boolean; cart: any; error?: string } {
    const prod = this.getProductById(productId);
    if (!prod) return { success: false, cart: null, error: 'Product not found or inactive' };
    if (prod.stock < quantity) return { success: false, cart: null, error: 'Insufficient stock' };

    let items = this.carts.get(userId) || [];
    const existing = items.find(i => i.productId === productId && i.selectedVariant === variant);

    if (existing) {
      existing.quantity += quantity;
    } else {
      items.push({
        id: `cart_item_${randomUUID().substring(0, 8)}`,
        productId,
        quantity,
        selectedVariant: variant,
      });
    }

    this.carts.set(userId, items);
    return { success: true, cart: this.getCart(userId) };
  }

  updateCartItem(userId: string, cartItemId: string, quantity: number): { success: boolean; cart: any; error?: string } {
    let items = this.carts.get(userId) || [];
    const itemIndex = items.findIndex(i => i.id === cartItemId);
    if (itemIndex === -1) return { success: false, cart: null, error: 'Cart item not found' };

    if (quantity <= 0) {
      items.splice(itemIndex, 1);
    } else {
      const prod = this.getProductById(items[itemIndex].productId);
      if (prod && prod.stock < quantity) {
        return { success: false, cart: null, error: 'Requested quantity exceeds stock' };
      }
      items[itemIndex].quantity = quantity;
    }

    this.carts.set(userId, items);
    return { success: true, cart: this.getCart(userId) };
  }

  removeFromCart(userId: string, cartItemId: string): { success: boolean; cart: any } {
    let items = this.carts.get(userId) || [];
    items = items.filter(i => i.id !== cartItemId && i.productId !== cartItemId);
    this.carts.set(userId, items);
    return { success: true, cart: this.getCart(userId) };
  }

  clearCart(userId: string): boolean {
    this.carts.set(userId, []);
    return true;
  }

  // --- Coupon Operations ---
  getCoupons(): Coupon[] {
    return this.coupons.filter(c => c.isActive);
  }

  validateCoupon(code: string, subtotal: number): { valid: boolean; coupon?: Coupon; discount: number; message: string } {
    const coupon = this.coupons.find(c => c.code.toUpperCase() === code.toUpperCase() && c.isActive);
    if (!coupon) {
      return { valid: false, discount: 0, message: 'Invalid or expired coupon code' };
    }

    if (subtotal < coupon.minOrderValue) {
      return {
        valid: false,
        discount: 0,
        message: `Coupon requires minimum order value of ₹${coupon.minOrderValue}`,
      };
    }

    let discount = 0;
    if (coupon.discountType === 'FLAT') {
      discount = coupon.discountValue;
    } else if (coupon.discountType === 'PERCENTAGE') {
      discount = (subtotal * coupon.discountValue) / 100;
      if (coupon.maxDiscountAmount && discount > coupon.maxDiscountAmount) {
        discount = coupon.maxDiscountAmount;
      }
    }

    discount = Math.min(discount, subtotal);

    return {
      valid: true,
      coupon,
      discount: Math.round(discount),
      message: `Coupon ${coupon.code} applied successfully! Saved ₹${Math.round(discount)}`,
    };
  }

  // --- Server-Authoritative Checkout Calculation ---
  calculateCheckout(userId: string, couponCode?: string): {
    items: CartItem[];
    subtotal: number;
    discount: number;
    couponDiscount: number;
    couponCode?: string;
    deliveryFee: number;
    tax: number;
    total: number;
    freeDeliveryThreshold: number;
  } {
    const { items, subtotal } = this.getCart(userId);

    let couponDiscount = 0;
    let appliedCoupon: string | undefined = undefined;

    if (couponCode) {
      const val = this.validateCoupon(couponCode, subtotal);
      if (val.valid) {
        couponDiscount = val.discount;
        appliedCoupon = couponCode.toUpperCase();
      }
    }

    const freeThreshold = env.FREE_DELIVERY_THRESHOLD;
    const defaultFee = env.DEFAULT_DELIVERY_FEE;
    const deliveryFee = subtotal >= freeThreshold || subtotal === 0 ? 0 : defaultFee;
    const tax = 0; // Stationery 0% educational bracket
    const total = Math.max(0, subtotal - couponDiscount + deliveryFee + tax);

    return {
      items,
      subtotal,
      discount: 0,
      couponDiscount,
      couponCode: appliedCoupon,
      deliveryFee,
      tax,
      total,
      freeDeliveryThreshold: freeThreshold,
    };
  }

  // --- Address Operations ---
  getAddresses(userId: string): Address[] {
    return this.addresses.get(userId) || [];
  }

  addAddress(userId: string, address: Omit<Address, 'id' | 'userId'>): Address {
    const list = this.addresses.get(userId) || [];
    const newAddress: Address = {
      ...address,
      id: `addr_${randomUUID().substring(0, 8)}`,
      userId,
    };

    if (newAddress.isDefault || list.length === 0) {
      list.forEach(a => (a.isDefault = false));
      newAddress.isDefault = true;
    }

    list.push(newAddress);
    this.addresses.set(userId, list);
    return newAddress;
  }

  updateAddress(userId: string, addressId: string, updates: Partial<Address>): Address | null {
    const list = this.addresses.get(userId) || [];
    const index = list.findIndex(a => a.id === addressId);
    if (index === -1) return null;

    if (updates.isDefault) {
      list.forEach(a => (a.isDefault = false));
    }

    list[index] = { ...list[index], ...updates };
    this.addresses.set(userId, list);
    return list[index];
  }

  deleteAddress(userId: string, addressId: string): boolean {
    const list = this.addresses.get(userId) || [];
    const filtered = list.filter(a => a.id !== addressId);
    if (filtered.length === list.length) return false;
    this.addresses.set(userId, filtered);
    return true;
  }

  setDefaultAddress(userId: string, addressId: string): boolean {
    const list = this.addresses.get(userId) || [];
    let found = false;
    for (const a of list) {
      if (a.id === addressId) {
        a.isDefault = true;
        found = true;
      } else {
        a.isDefault = false;
      }
    }
    return found;
  }

  // --- Order Operations with Snapshots & State Machine ---
  createOrder(
    userId: string,
    addressId: string,
    paymentMethod = 'UPI',
    couponCode?: string,
    notes?: string
  ): { success: boolean; order?: Order; error?: string } {
    const checkout = this.calculateCheckout(userId, couponCode);
    if (checkout.items.length === 0) {
      return { success: false, error: 'Your school bag is empty' };
    }

    const addresses = this.getAddresses(userId);
    const address = addresses.find(a => a.id === addressId) || addresses[0];
    if (!address) {
      return { success: false, error: 'Delivery address is required' };
    }

    const store = this.stores[0];
    const orderId = `ord_${randomUUID().substring(0, 10)}`;
    const orderNumber = `KG-${new Date().getFullYear()}-${Math.floor(100000 + Math.random() * 900000)}`;

    // Immutable snapshots
    const orderItems: OrderItem[] = checkout.items.map(i => ({
      id: `item_${randomUUID().substring(0, 8)}`,
      productId: i.productId,
      productName: i.product?.name || 'Stationery Item',
      price: i.product?.price || 0,
      mrp: i.product?.mrp || 0,
      quantity: i.quantity,
      variant: i.selectedVariant,
    }));

    const order: Order = {
      id: orderId,
      orderNumber,
      userId,
      storeId: store.id,
      storeName: store.name,
      status: 'CONFIRMED',
      subtotal: checkout.subtotal,
      discount: checkout.discount,
      couponDiscount: checkout.couponDiscount,
      couponCode: checkout.couponCode,
      deliveryFee: checkout.deliveryFee,
      tax: checkout.tax,
      total: checkout.total,
      paymentStatus: 'SUCCESS',
      paymentMethod,
      deliveryStatus: 'ORDER_CONFIRMED',
      addressSnapshot: { ...address },
      items: orderItems,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    this.orders.set(orderId, order);
    this.clearCart(userId);

    return { success: true, order };
  }

  getOrders(userId: string): Order[] {
    const list: Order[] = [];
    for (const ord of this.orders.values()) {
      if (ord.userId === userId || ord.userId === 'user_dev_default') {
        list.push(ord);
      }
    }
    return list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }

  getOrderById(orderId: string, userId: string): Order | undefined {
    const order = this.orders.get(orderId);
    if (!order) return undefined;
    if (order.userId === userId || userId === 'user_dev_default') {
      return order;
    }
    return undefined;
  }

  // Order State Machine Validation
  transitionOrderStatus(orderId: string, targetStatus: OrderStatus): { success: boolean; order?: Order; error?: string } {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: 'Order not found' };

    const validTransitions: Record<OrderStatus, OrderStatus[]> = {
      PENDING_PAYMENT: ['PAYMENT_CONFIRMED', 'CANCELLED'],
      PAYMENT_CONFIRMED: ['CONFIRMED', 'CANCELLED', 'REFUNDED'],
      CONFIRMED: ['PREPARING', 'CANCELLED'],
      PREPARING: ['READY_FOR_PICKUP', 'CANCELLED'],
      READY_FOR_PICKUP: ['PICKED_UP', 'CANCELLED'],
      PICKED_UP: ['OUT_FOR_DELIVERY'],
      OUT_FOR_DELIVERY: ['DELIVERED'],
      DELIVERED: [],
      CANCELLED: ['REFUNDED'],
      REFUNDED: [],
    };

    const allowed = validTransitions[order.status] || [];
    if (!allowed.includes(targetStatus)) {
      return {
        success: false,
        error: `Invalid state transition from ${order.status} to ${targetStatus}`,
      };
    }

    order.status = targetStatus;
    order.updatedAt = new Date().toISOString();
    return { success: true, order };
  }

  cancelOrder(orderId: string, userId: string): { success: boolean; order?: Order; error?: string } {
    const order = this.getOrderById(orderId, userId);
    if (!order) return { success: false, error: 'Order not found' };

    if (['OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'].includes(order.status)) {
      return { success: false, error: `Order in ${order.status} cannot be cancelled` };
    }

    order.status = 'CANCELLED';
    order.updatedAt = new Date().toISOString();
    return { success: true, order };
  }

  // --- Profile Operations ---
  getProfile(userId: string): any {
    return this.userProfiles.get(userId) || {
      id: userId,
      authUserId: userId,
      firstName: 'Student',
      lastName: 'User',
      phone: '+919876543210',
      email: 'student@kidsg.in',
      role: 'CUSTOMER',
      onboardingCompleted: true,
      selectedClass: 'Class 7',
      selectedSchool: 'School',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
  }

  updateProfile(userId: string, updates: any): any {
    const existing = this.getProfile(userId);
    const updated = { ...existing, ...updates, updatedAt: new Date().toISOString() };
    this.userProfiles.set(userId, updated);
    return updated;
  }

  // --- Support Tickets ---
  createSupportTicket(userId: string, subject: string, message: string, orderId?: string): SupportTicket {
    const ticket: SupportTicket = {
      id: `tkt_${randomUUID().substring(0, 8)}`,
      ticketNumber: `KG-SUP-${Math.floor(10000 + Math.random() * 90000)}`,
      userId,
      orderId,
      subject,
      message,
      status: 'OPEN',
      priority: 'NORMAL',
      createdAt: new Date().toISOString(),
    };
    this.tickets.unshift(ticket);
    return ticket;
  }

  getSupportTickets(userId: string): SupportTicket[] {
    return this.tickets.filter(t => t.userId === userId || t.userId === 'user_dev_default');
  }

  getSupportTicketById(id: string, userId: string): SupportTicket | undefined {
    return this.tickets.find(t => (t.id === id || t.ticketNumber === id) && (t.userId === userId || t.userId === 'user_dev_default'));
  }
}

export const db = new KidsGDatabase();
