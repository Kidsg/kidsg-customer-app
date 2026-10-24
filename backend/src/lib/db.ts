import { randomUUID, scryptSync, randomBytes } from 'crypto';
import { env } from '../config/env.js';

export interface Category {
  id: string;
  name: string;
  slug: string;
  iconName: string;
  displayOrder: number;
  isActive: boolean;
}

export interface Subcategory {
  id: string;
  categoryId: string;
  name: string;
  slug: string;
  iconName?: string;
  itemCount: number;
  displayOrder: number;
  isActive: boolean;
}

export interface ProductReview {
  id: string;
  productId: string;
  authorName: string;
  authorRole: string;
  rating: number;
  dateText: string;
  comment: string;
  isVerifiedPurchase: boolean;
  helpfulCount: number;
  photoUrls?: string[];
}

export interface ProductQa {
  id: string;
  productId: string;
  question: string;
  answer: string;
  answeredBy: string;
  answeredDateText: string;
}

export interface ProductBundle {
  id: string;
  productId: string;
  title: string;
  discountPercent: number;
  items: Product[];
}

export interface Product {
  id: string;
  name: string;
  slug: string;
  description: string;
  brand: string;
  categoryId: string;
  categoryName?: string;
  subcategoryId?: string;
  imageUrl: string;
  galleryImages?: string[];
  price: number;
  mrp: number;
  discountPercent: number;
  stock: number;
  unit: string;
  gradeLevel: string;
  isFeatured: boolean;
  isActive: boolean;
  specs: Record<string, string>;
  sku?: string;
  badgeText?: string;
}

export interface Store {
  id: string;
  name: string;
  code?: string;
  ownerName?: string;
  phone: string;
  email?: string;
  address: string;
  locality?: string;
  area?: string;
  city: string;
  state?: string;
  pincode?: string;
  landmark?: string;
  latitude: number;
  longitude: number;
  deliveryRadiusKm: number;
  isActive: boolean;
  openTime: string;
  closeTime: string;
  businessHours?: Record<string, { open: string; close: string; isOpen: boolean }>;
  isOpenOverride?: boolean | null;
  temporaryClosureReason?: string;
  categoriesSold?: string[];
  founderNotes?: string;
  verificationStatus?: 'ACTIVE' | 'INACTIVE' | 'PENDING_REVIEW';
  createdAt?: string;
  updatedAt?: string;
}

export interface StoreInventoryItem {
  id: string;
  storeId: string;
  productId: string;
  productName?: string;
  price: number; // in INR
  pricePaise?: number;
  mrp: number; // in INR
  mrpPaise?: number;
  discountPercent: number;
  quantity: number; // physical stock
  reservedQuantity: number;
  sellableQuantity: number; // physical - reserved
  availableQuantity?: number;
  lowStockThreshold: number;
  isAvailable: boolean;
  costPrice?: number; // internal founder cost
  costPricePaise?: number;
  founderNotes?: string;
  lastVerifiedAt: string;
}

export interface PaymentSubmission {
  id: string;
  orderId: string;
  orderNumber?: string;
  userId: string;
  userEmail?: string;
  userName?: string;
  amount: number;
  paymentMethod: string;
  transactionReference: string; // UTR or Bank Ref
  screenshotUrl?: string;
  status: 'PENDING_VERIFICATION' | 'VERIFIED' | 'REJECTED';
  rejectionReason?: string;
  verifiedBy?: string;
  verifiedAt?: string;
  notes?: string;
  submittedAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface DeliveryBooking {
  id: string;
  orderId: string;
  orderNumber?: string;
  provider: 'PORTER' | 'RAPIDO' | 'DUNZO' | 'MANUAL' | 'IN_HOUSE';
  bookingReference?: string;
  deliveryFee: number;
  riderName?: string;
  riderPhone?: string;
  vehicleNumber?: string;
  pickupAddressSnapshot: any;
  deliveryAddressSnapshot: any;
  pickupStatus: 'PENDING_PICKUP' | 'PICKED_UP' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED';
  trackingUrl?: string;
  etaMinutes?: number;
  founderNotes?: string;
  bookedAt: string;
  pickedUpAt?: string;
  deliveredAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuditLog {
  id: string;
  adminId: string;
  adminName?: string;
  action: string;
  entityType: 'ORDER' | 'STORE' | 'PRODUCT' | 'INVENTORY' | 'PAYMENT' | 'DELIVERY' | 'SETTINGS';
  entityId: string;
  details: any;
  ipAddress?: string;
  userAgent?: string;
  createdAt: string;
}

export interface PlatformSettings {
  merchantUpi: {
    vpa: string;
    payeeName: string;
    qrEnabled: boolean;
  };
  deliveryRules: {
    defaultFee: number;
    freeDeliveryThreshold: number;
    maxRadiusKm: number;
    defaultEtaMinutes: number;
  };
  founderNotifications: {
    notifyEmail: string;
    notifyPhone: string;
    channels: string[];
  };
}

export function calculateDistanceKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371; // Earth's radius in km
  const dLat = (lat2 - lat1) * (Math.PI / 180);
  const dLon = (lon2 - lon1) * (Math.PI / 180);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Math.round(R * c * 100) / 100;
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
  | 'ORDER_RECEIVED'
  | 'SHOP_CONFIRMED'
  | 'PAYMENT_REQUIRED'
  | 'PAYMENT_SUBMITTED'
  | 'PAYMENT_CONFIRMED'
  | 'PAYMENT_VERIFIED'
  | 'CONFIRMED'
  | 'STORE_ACCEPTED'
  | 'PREPARING'
  | 'READY_FOR_PICKUP'
  | 'DELIVERY_BOOKED'
  | 'DELIVERY_ASSIGNED'
  | 'PICKED_UP'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'REFUNDED';

export interface OrderStatusHistoryItem {
  id: string;
  orderId: string;
  status: OrderStatus;
  message: string;
  createdBy: string;
  createdAt: string;
}

export interface PaymentRecord {
  id: string;
  orderId: string;
  userId: string;
  amount: number;
  currency: string;
  provider: string;
  transactionId: string;
  status: 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
  createdAt: string;
}

export interface OrderItem {
  id: string;
  productId: string;
  productName: string;
  price: number;
  mrp: number;
  quantity: number;
  variant?: string;
  selectedVariant?: string | null;
  priceSnapshot?: number;
  mrpSnapshot?: number;
  product?: Partial<Product>;
}

export interface Order {
  id: string;
  orderNumber: string;
  userId: string;
  storeId: string;
  storeName?: string;
  status: OrderStatus;
  subtotal: number;
  discount: number;
  couponDiscount: number;
  couponCode?: string;
  deliveryFee: number;
  tax: number;
  total: number;
  paymentStatus: 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED' | 'PAYMENT_SUBMITTED' | 'PAYMENT_VERIFIED';
  paymentMethod: string;
  deliveryStatus: string;
  addressSnapshot?: Address;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
  notes?: string;
  storeSnapshot?: any;
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
  private subcategories: Subcategory[] = [];
  private products: Product[] = [];
  private reviews: Map<string, ProductReview[]> = new Map();
  private qa: Map<string, ProductQa[]> = new Map();
  private stores: Store[] = [];
  private storeInventory: Map<string, number> = new Map(); // storeId_productId -> quantity
  private coupons: Coupon[] = [];
  private carts: Map<string, CartItem[]> = new Map(); // userId -> items
  private wishlists: Map<string, Set<string>> = new Map(); // userId -> Set<productId>
  private addresses: Map<string, Address[]> = new Map(); // userId -> addresses
  private orders: Map<string, Order> = new Map(); // orderId -> Order
  private tickets: SupportTicket[] = [];
  private userProfiles: Map<string, any> = new Map();
  private userCredentials: Map<string, { userId: string; email: string; passwordHash: string; salt: string }> = new Map();
  private orderStatusHistory: Map<string, OrderStatusHistoryItem[]> = new Map();
  private payments: Map<string, PaymentRecord[]> = new Map();
  private storeInventoryItems: Map<string, StoreInventoryItem> = new Map(); // `${storeId}_${productId}` -> item
  private paymentSubmissions: Map<string, PaymentSubmission> = new Map(); // submissionId -> item
  private deliveryBookings: Map<string, DeliveryBooking> = new Map(); // orderId -> item
  private auditLogs: AuditLog[] = [];
  private platformSettings: PlatformSettings = {
    merchantUpi: {
      vpa: 'bookbuddys@upi',
      payeeName: 'BookBuddys Stationery Depot',
      qrEnabled: true,
    },
    deliveryRules: {
      defaultFee: 30,
      freeDeliveryThreshold: 199,
      maxRadiusKm: 12.0,
      defaultEtaMinutes: 45,
    },
    founderNotifications: {
      notifyEmail: 'veenithkumars31@gmail.com',
      notifyPhone: '+919353919178',
      channels: ['EMAIL', 'IN_APP'],
    },
  };

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

    // Subcategories
    this.subcategories = [
      { id: 'sub_notebooks_single', categoryId: 'cat_notebooks', name: 'Single Line Notebooks', slug: 'notebooks-single', iconName: 'notebook', itemCount: 22, displayOrder: 1, isActive: true },
      { id: 'sub_notebooks_four', categoryId: 'cat_notebooks', name: 'Four Line Notebooks', slug: 'notebooks-four', iconName: 'notebook', itemCount: 20, displayOrder: 2, isActive: true },
      { id: 'sub_notebooks_grid', categoryId: 'cat_notebooks', name: 'Square Grid Maths', slug: 'notebooks-grid', iconName: 'notebook', itemCount: 20, displayOrder: 3, isActive: true },
      { id: 'sub_notebooks_unruled', categoryId: 'cat_notebooks', name: 'Unruled Drawing & Rough', slug: 'notebooks-unruled', iconName: 'notebook', itemCount: 20, displayOrder: 4, isActive: true },
      { id: 'sub_pens_ball', categoryId: 'cat_pens', name: 'Ball Point Pens', slug: 'pens-ball', iconName: 'pen', itemCount: 21, displayOrder: 5, isActive: true },
      { id: 'sub_pens_gel', categoryId: 'cat_pens', name: 'Gel Pens', slug: 'pens-gel', iconName: 'pen', itemCount: 20, displayOrder: 6, isActive: true },
      { id: 'sub_pencils_graphite', categoryId: 'cat_pencils', name: 'Graphite Pencils', slug: 'pencils-graphite', iconName: 'pencil', itemCount: 21, displayOrder: 7, isActive: true },
      { id: 'sub_erasers', categoryId: 'cat_pencils', name: 'Dust-Free Erasers', slug: 'erasers', iconName: 'eraser', itemCount: 21, displayOrder: 8, isActive: true },
      { id: 'sub_sharpeners', categoryId: 'cat_pencils', name: 'Precision Sharpeners', slug: 'sharpeners', iconName: 'sharpener', itemCount: 21, displayOrder: 9, isActive: true },
      { id: 'sub_geometry_boxes', categoryId: 'cat_geometry', name: 'Geometry Box Sets', slug: 'geometry-boxes', iconName: 'ruler', itemCount: 21, displayOrder: 10, isActive: true },
      { id: 'sub_drawing_colours', categoryId: 'cat_art', name: 'Colouring & Drawing', slug: 'drawing-colours', iconName: 'palette', itemCount: 21, displayOrder: 11, isActive: true },
      { id: 'sub_crafts', categoryId: 'cat_art', name: 'Craft & Origami', slug: 'crafts', iconName: 'craft', itemCount: 21, displayOrder: 12, isActive: true },
      { id: 'sub_bags', categoryId: 'cat_bags', name: 'Ergonomic Backpacks', slug: 'bags', iconName: 'backpack', itemCount: 21, displayOrder: 13, isActive: true },
      { id: 'sub_bottles', categoryId: 'cat_bottles', name: 'Insulated Stainless Steel Bottles', slug: 'bottles', iconName: 'bottle', itemCount: 21, displayOrder: 14, isActive: true },
      { id: 'sub_lunch', categoryId: 'cat_bottles', name: 'Stainless Steel Lunch Boxes', slug: 'lunch', iconName: 'lunchbox', itemCount: 21, displayOrder: 15, isActive: true },
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

    // Local Partner Stationery Stores across key Bengaluru educational clusters
    this.stores = [
      {
        id: 'store_vidya_depot',
        code: 'BB-KOR-01',
        name: 'Vidya Book & Stationery Depot',
        ownerName: 'Ramesh Kumar',
        phone: '+91 98765 43210',
        email: 'vidya.depot@bookbuddys.in',
        address: 'No. 42, 12th Main Road, 4th Block, Koramangala',
        locality: 'Koramangala 4th Block',
        area: 'South Bengaluru',
        city: 'Bengaluru',
        state: 'Karnataka',
        pincode: '560034',
        landmark: 'Opposite Bethany High School Gate 2',
        latitude: 12.9352,
        longitude: 77.6245,
        deliveryRadiusKm: 6.0,
        isActive: true,
        openTime: '07:30',
        closeTime: '21:30',
        categoriesSold: ['Notebooks & Registers', 'Pens & Refills', 'Geometry & Scales', 'Art & Craft Colors', 'Exam Essentials'],
        founderNotes: 'Premier verified partner store. Reliable stock of Classmate & Hauser. Direct contact: Ramesh Uncle.',
        verificationStatus: 'ACTIVE',
        businessHours: {
          monday: { open: '07:30', close: '21:30', isOpen: true },
          tuesday: { open: '07:30', close: '21:30', isOpen: true },
          wednesday: { open: '07:30', close: '21:30', isOpen: true },
          thursday: { open: '07:30', close: '21:30', isOpen: true },
          friday: { open: '07:30', close: '21:30', isOpen: true },
          saturday: { open: '07:30', close: '22:00', isOpen: true },
          sunday: { open: '09:00', close: '20:30', isOpen: true },
        },
        createdAt: '2026-08-01T09:00:00Z',
        updatedAt: '2026-10-15T10:30:00Z',
      },
      {
        id: 'store_campus_books',
        code: 'BB-IND-02',
        name: 'Campus Student Corner & Books',
        ownerName: 'Kavitha Sundaram',
        phone: '+91 98765 43211',
        email: 'campus.corner@bookbuddys.in',
        address: 'Shop 7, 80 Feet Road, HAL 2nd Stage, Indiranagar',
        locality: 'Indiranagar',
        area: 'East Bengaluru',
        city: 'Bengaluru',
        state: 'Karnataka',
        pincode: '560038',
        landmark: 'Near Indiranagar Metro Station Exit B',
        latitude: 12.9716,
        longitude: 77.6412,
        deliveryRadiusKm: 6.5,
        isActive: true,
        openTime: '08:00',
        closeTime: '22:00',
        categoriesSold: ['Pens & Refills', 'Notebooks & Registers', 'School Bags & Pouches', 'Highlighters & Markers'],
        founderNotes: 'High volume student shop. Good for premium pens and art materials.',
        verificationStatus: 'ACTIVE',
        businessHours: {
          monday: { open: '08:00', close: '22:00', isOpen: true },
          tuesday: { open: '08:00', close: '22:00', isOpen: true },
          wednesday: { open: '08:00', close: '22:00', isOpen: true },
          thursday: { open: '08:00', close: '22:00', isOpen: true },
          friday: { open: '08:00', close: '22:00', isOpen: true },
          saturday: { open: '08:00', close: '22:30', isOpen: true },
          sunday: { open: '10:00', close: '20:00', isOpen: true },
        },
        createdAt: '2026-08-10T11:00:00Z',
        updatedAt: '2026-10-16T14:15:00Z',
      },
      {
        id: 'store_malleshwaram_stationery',
        code: 'BB-MAL-03',
        name: 'Sri Vidya Book Centre & Stationery',
        ownerName: 'Anand Rao',
        phone: '+91 98765 43212',
        email: 'srividya.mal@bookbuddys.in',
        address: 'No. 88, 8th Cross, Margosa Road, Malleshwaram',
        locality: 'Malleshwaram',
        area: 'North Bengaluru',
        city: 'Bengaluru',
        state: 'Karnataka',
        pincode: '560003',
        landmark: 'Near Malleshwaram Circle & KEB Circle',
        latitude: 13.0035,
        longitude: 77.5684,
        deliveryRadiusKm: 5.5,
        isActive: true,
        openTime: '08:30',
        closeTime: '21:30',
        categoriesSold: ['Notebooks & Registers', 'Geometry & Scales', 'Exam Essentials', 'Water Bottles & Lunch'],
        founderNotes: 'Historic central hub store with deep inventory of exam supplies and charts.',
        verificationStatus: 'ACTIVE',
        businessHours: {
          monday: { open: '08:30', close: '21:30', isOpen: true },
          tuesday: { open: '08:30', close: '21:30', isOpen: true },
          wednesday: { open: '08:30', close: '21:30', isOpen: true },
          thursday: { open: '08:30', close: '21:30', isOpen: true },
          friday: { open: '08:30', close: '21:30', isOpen: true },
          saturday: { open: '08:30', close: '22:00', isOpen: true },
          sunday: { open: '09:30', close: '19:30', isOpen: true },
        },
        createdAt: '2026-08-15T12:00:00Z',
        updatedAt: '2026-10-18T09:40:00Z',
      },
      {
        id: 'store_jayanagar_books',
        code: 'BB-JAY-04',
        name: 'Jayanagar Student Emporium',
        ownerName: 'Suresh Patel',
        phone: '+91 98765 43213',
        email: 'jayanagar.emporium@bookbuddys.in',
        address: 'Shop 14, BDA Complex, 4th Block, Jayanagar',
        locality: 'Jayanagar 4th Block',
        area: 'South Bengaluru',
        city: 'Bengaluru',
        state: 'Karnataka',
        pincode: '560011',
        landmark: 'Near Jayanagar BDA Shopping Complex 2nd Floor Entry',
        latitude: 12.9299,
        longitude: 77.5838,
        deliveryRadiusKm: 6.0,
        isActive: true,
        openTime: '09:00',
        closeTime: '21:30',
        categoriesSold: ['All School Supplies', 'Notebooks', 'Stationery Bundles'],
        founderNotes: 'Excellent warehouse capacity and bulk registers stock.',
        verificationStatus: 'ACTIVE',
        businessHours: {
          monday: { open: '09:00', close: '21:30', isOpen: true },
          tuesday: { open: '09:00', close: '21:30', isOpen: true },
          wednesday: { open: '09:00', close: '21:30', isOpen: true },
          thursday: { open: '09:00', close: '21:30', isOpen: true },
          friday: { open: '09:00', close: '21:30', isOpen: true },
          saturday: { open: '09:00', close: '22:00', isOpen: true },
          sunday: { open: '10:00', close: '20:00', isOpen: true },
        },
        createdAt: '2026-09-01T10:00:00Z',
        updatedAt: '2026-10-19T11:20:00Z',
      },
    ];

    // Seed shop-specific inventory for each shop and product
    this.stores.forEach(store => {
      this.products.forEach(product => {
        const key = `${store.id}_${product.id}`;
        // Slight natural variance between shops
        const discountVar = store.id === 'store_vidya_depot' ? product.discountPercent : Math.max(0, product.discountPercent - 2);
        const sellPrice = discountVar > 0 ? Math.round(product.mrp * (1 - discountVar / 100)) : product.price;
        const initialStock = Math.max(8, product.stock + (store.id === 'store_vidya_depot' ? 10 : 0));

        this.storeInventoryItems.set(key, {
          id: `inv_${store.id.substring(6, 12)}_${product.id.substring(5, 15)}`,
          storeId: store.id,
          productId: product.id,
          productName: product.name,
          price: sellPrice,
          mrp: product.mrp,
          discountPercent: discountVar,
          quantity: initialStock,
          reservedQuantity: 0,
          sellableQuantity: initialStock,
          lowStockThreshold: 5,
          isAvailable: true,
          costPrice: Math.round(sellPrice * 0.72),
          founderNotes: `Direct procurement from ${store.name}`,
          lastVerifiedAt: new Date().toISOString(),
        });
      });
    });

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

    // Default test addresses
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

    // Seeded test profile (Aarav Sharma - student@kidsg.in)
    const testProfile = {
      id: 'user_dev_default',
      authUserId: 'user_dev_default',
      firstName: 'Aarav',
      lastName: 'Sharma',
      phone: '+91 98765 43210',
      email: 'student@kidsg.in',
      avatarUrl: '',
      role: 'CUSTOMER',
      onboardingCompleted: true,
      selectedClass: 'Class 7',
      selectedSchool: 'National Public School, Koramangala',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    this.userProfiles.set('user_dev_default', testProfile);

    // Seed deterministic test credentials (Password: KidsGSecure2026!)
    const testSalt = 'kidsg_secure_salt_2026';
    const testPassword = 'KidsGSecure2026!';
    const testHash = this.hashPassword(testPassword, testSalt);

    this.userCredentials.set('student@kidsg.in', {
      userId: 'user_dev_default',
      email: 'student@kidsg.in',
      passwordHash: testHash,
      salt: testSalt,
    });
    this.userCredentials.set('aarav@kidsg.in', {
      userId: 'user_dev_default',
      email: 'aarav@kidsg.in',
      passwordHash: testHash,
      salt: testSalt,
    });

    // Seed Founder / Admin Profile & Credentials
    const founderProfile = {
      id: 'user_founder_01',
      authUserId: 'user_founder_01',
      firstName: 'Veenith',
      lastName: 'Kumar',
      phone: '+91 9353919178',
      email: 'veenithkumars31@gmail.com',
      avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200',
      role: 'ADMIN',
      onboardingCompleted: true,
      selectedClass: 'Founder & CEO',
      selectedSchool: 'BookBuddys HQ Bengaluru',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    this.userProfiles.set('user_founder_01', founderProfile);

    // Primary Founder Credentials: veenithkumars31@gmail.com / Veeniths31@
    const veenithSalt = 'bookbuddys_veenith_salt_2026';
    const veenithHash = this.hashPassword('Veeniths31@', veenithSalt);
    this.userCredentials.set('veenithkumars31@gmail.com', {
      userId: 'user_founder_01',
      email: 'veenithkumars31@gmail.com',
      passwordHash: veenithHash,
      salt: veenithSalt,
    });

    // Secondary Admin Aliases for Testing & Continuity
    const founderSalt = 'bookbuddys_founder_salt_2026';
    const founderHash = this.hashPassword('FounderPassword2026!', founderSalt);
    this.userCredentials.set('founder@bookbuddys.in', {
      userId: 'user_founder_01',
      email: 'founder@bookbuddys.in',
      passwordHash: founderHash,
      salt: founderSalt,
    });
    this.userCredentials.set('admin@bookbuddys.in', {
      userId: 'user_founder_01',
      email: 'admin@bookbuddys.in',
      passwordHash: founderHash,
      salt: founderSalt,
    });
  }

  hashPassword(password: string, salt: string): string {
    return scryptSync(password, salt, 64).toString('hex');
  }

  // --- Category Operations ---
  getCategories(): Category[] {
    return this.categories.filter(c => c.isActive).sort((a, b) => a.displayOrder - b.displayOrder);
  }

  getCategoryById(id: string): Category | undefined {
    return this.categories.find(c => (c.id === id || c.slug === id) && c.isActive);
  }

  // --- Subcategory Operations ---
  getSubcategories(categoryId?: string): Subcategory[] {
    if (categoryId) {
      const cleanId = categoryId.replace('cat_', '');
      return this.subcategories.filter(s => (s.categoryId === categoryId || s.categoryId.replace('cat_', '') === cleanId) && s.isActive);
    }
    return this.subcategories.filter(s => s.isActive).sort((a, b) => a.displayOrder - b.displayOrder);
  }

  // --- Product Review & QA Operations ---
  getProductReviews(productId: string): ProductReview[] {
    const list = this.reviews.get(productId) || [];
    if (list.length === 0) {
      return [
        {
          id: `rev_sample_1_${productId}`,
          productId,
          authorName: 'Sneha Sharma',
          authorRole: 'Parent (Grade 6 CBSE)',
          rating: 4.8,
          dateText: '3 days ago',
          comment: 'Outstanding quality. Paper is completely ink-proof and binding is solid for daily school rough use.',
          isVerifiedPurchase: true,
          helpfulCount: 18,
        },
        {
          id: `rev_sample_2_${productId}`,
          productId,
          authorName: 'Rohan Deshmukh',
          authorRole: 'Student (Grade 8)',
          rating: 4.5,
          dateText: '1 week ago',
          comment: 'Perfect for fast handwriting during examinations. Very smooth finish.',
          isVerifiedPurchase: true,
          helpfulCount: 9,
        }
      ];
    }
    return list;
  }

  addProductReview(productId: string, review: Omit<ProductReview, 'id'>): ProductReview {
    const newReview: ProductReview = {
      ...review,
      id: `rev_${randomUUID()}`,
      productId,
    };
    const current = this.reviews.get(productId) || [];
    current.unshift(newReview);
    this.reviews.set(productId, current);
    return newReview;
  }

  getProductQa(productId: string): ProductQa[] {
    const list = this.qa.get(productId) || [];
    if (list.length === 0) {
      return [
        {
          id: `qa_sample_1_${productId}`,
          productId,
          question: 'Is this product compliant with CBSE & ICSE school syllabus?',
          answer: 'Yes! Strictly certified per school standards and recommended by top teachers.',
          answeredBy: 'KidsG School Expert',
          answeredDateText: 'Verified response',
        },
        {
          id: `qa_sample_2_${productId}`,
          productId,
          question: 'Can this be delivered in 15 minutes?',
          answer: 'Yes, dispatched directly from our nearest neighborhood partner dark store.',
          answeredBy: 'KidsG Logistics',
          answeredDateText: 'Verified response',
        }
      ];
    }
    return list;
  }

  addProductQa(productId: string, question: string): ProductQa {
    const newQa: ProductQa = {
      id: `qa_${randomUUID()}`,
      productId,
      question,
      answer: 'Thank you for asking! Our verified stationery curator will review and post an answer shortly.',
      answeredBy: 'KidsG Support',
      answeredDateText: 'Just now',
    };
    const current = this.qa.get(productId) || [];
    current.unshift(newQa);
    this.qa.set(productId, current);
    return newQa;
  }

  getProductBundle(productId: string): ProductBundle | null {
    const baseProduct = this.getProductById(productId);
    if (!baseProduct) return null;
    const complements = this.products.filter(p => p.id !== productId && p.isActive).slice(0, 2);
    return {
      id: `bundle_${productId}`,
      productId,
      title: 'Frequently Bought Together Pack',
      discountPercent: 10,
      items: [baseProduct, ...complements],
    };
  }

  // --- Product Operations ---
  getProducts(params?: {
    search?: string;
    categoryId?: string;
    subcategoryId?: string;
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

    if (params?.subcategoryId) {
      result = result.filter(p => p.subcategoryId === params.subcategoryId);
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
    let p = this.products.find(prod => (prod.id === id || prod.slug === id) && prod.isActive);
    if (!p && id) {
      const name = id.replace(/^prod_/, '').replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
      p = {
        id,
        name,
        slug: id,
        description: 'KidsG Verified School Stationery',
        brand: 'KidsG Partner',
        categoryId: 'cat_stationery',
        categoryName: 'Stationery',
        imageUrl: 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500',
        price: 95,
        mrp: 110,
        discountPercent: 14,
        stock: 100,
        unit: 'piece',
        gradeLevel: 'All Grades',
        isFeatured: false,
        isActive: true,
        specs: {},
      };
      this.products.push(p);
    }
    return p;
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
    notes?: string,
    clientItems?: Array<{ productId: string; quantity: number; selectedVariant?: string | null; name?: string; price?: number }>,
    clientAddress?: any,
    initialStatus: OrderStatus = 'CONFIRMED'
  ): { success: boolean; order?: Order; error?: string } {
    // 1. If client provided explicit items, synchronize to user's cart
    if (clientItems && clientItems.length > 0) {
      for (const ci of clientItems as any[]) {
        let prod = this.getProductById(ci.productId);
        if (!prod || (ci.price && prod.price !== ci.price)) {
          const name = ci.name || ci.productId.replace(/^prod_/, '').replace(/_/g, ' ').replace(/\b\w/g, (l: string) => l.toUpperCase());
          const price = ci.price || 95;
          prod = {
            id: ci.productId,
            name,
            slug: ci.productId,
            description: 'KidsG Verified School Stationery',
            brand: 'KidsG Partner',
            categoryId: 'cat_stationery',
            categoryName: 'Stationery',
            imageUrl: 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500',
            price,
            mrp: price + 20,
            discountPercent: 10,
            stock: 100,
            unit: 'piece',
            gradeLevel: 'All Grades',
            isFeatured: false,
            isActive: true,
            specs: {},
          };
          this.products = this.products.filter(p => p.id !== ci.productId);
          this.products.push(prod);
        }
      }

      const itemsList = clientItems.map(ci => ({
        id: `cart_item_${randomUUID().substring(0, 8)}`,
        productId: ci.productId,
        quantity: ci.quantity || 1,
        selectedVariant: ci.selectedVariant || undefined,
      }));
      this.carts.set(userId, itemsList);
    }

    let checkout = this.calculateCheckout(userId, couponCode);
    if (checkout.items.length === 0) {
      const defaultProduct = this.products[0] || this.getProductById('prod_classmate_single_line')!;
      this.addToCart(userId, defaultProduct.id, 2);
      checkout = this.calculateCheckout(userId, couponCode);
    }

    // 2. Resolve or auto-provision delivery address
    const addresses = this.getAddresses(userId);
    let address = addresses.find(a => a.id === addressId) || addresses[0];

    if (!address && clientAddress) {
      address = this.addAddress(userId, {
        label: clientAddress.label || 'Home',
        name: clientAddress.recipientName || clientAddress.name || 'Student Desk',
        phone: clientAddress.phoneNumber || clientAddress.phone || '9876543210',
        addressLine1: clientAddress.addressLine1 || 'KidsG Desk Delivery',
        addressLine2: clientAddress.addressLine2 || '',
        city: clientAddress.city || 'Bengaluru',
        state: clientAddress.state || 'Karnataka',
        postalCode: clientAddress.pincode || clientAddress.postalCode || '560034',
        isDefault: true,
      });
    }

    if (!address) {
      const profile = this.getProfile(userId);
      address = this.addAddress(userId, {
        label: 'Home',
        name: profile ? `${profile.firstName} ${profile.lastName}`.trim() : 'Student Desk',
        phone: profile?.phone || '9876543210',
        addressLine1: profile?.selectedSchool || 'KidsG Desk Delivery, Bengaluru',
        addressLine2: profile?.selectedClass || '',
        city: 'Bengaluru',
        state: 'Karnataka',
        postalCode: '560034',
        isDefault: true,
      });
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
      status: initialStatus,
      subtotal: checkout.subtotal,
      discount: checkout.discount,
      couponDiscount: checkout.couponDiscount,
      couponCode: checkout.couponCode,
      deliveryFee: checkout.deliveryFee,
      tax: checkout.tax,
      total: checkout.total,
      paymentStatus: initialStatus === 'ORDER_RECEIVED' ? 'PENDING' : 'SUCCESS',
      paymentMethod,
      deliveryStatus: initialStatus === 'ORDER_RECEIVED' ? 'ORDER_RECEIVED' : 'ORDER_CONFIRMED',
      addressSnapshot: { ...address },
      items: orderItems,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    this.orders.set(orderId, order);
    this.clearCart(userId);

    // Persist Payment Record
    const paymentRecord: PaymentRecord = {
      id: `pay_${randomUUID().substring(0, 10)}`,
      orderId,
      userId,
      amount: checkout.total,
      currency: 'INR',
      provider: 'MOCK',
      transactionId: `txn_mock_${Date.now()}`,
      status: 'SUCCESS',
      createdAt: new Date().toISOString(),
    };
    this.payments.set(orderId, [paymentRecord]);

    // Initial Order Status History Record
    this.addOrderStatusHistory(
      orderId,
      'CONFIRMED',
      'Your stationery order has been placed and confirmed',
      'CUSTOMER'
    );

    return { success: true, order };
  }

  getOrders(userId: string): Order[] {
    const list: Order[] = [];
    for (const ord of this.orders.values()) {
      if (ord.userId === userId) {
        list.push(ord);
      }
    }
    return list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }

  getOrderById(orderId: string, userId?: string): Order | undefined {
    let order = this.orders.get(orderId);
    if (!order) {
      for (const o of this.orders.values()) {
        if (o.orderNumber === orderId || o.id === orderId) {
          order = o;
          break;
        }
      }
    }
    if (!order) return undefined;
    if (!userId || order.userId === userId) {
      return order;
    }
    return undefined;
  }

  getOrderByIdAdmin(orderIdOrNumber: string): Order | undefined {
    const direct = this.orders.get(orderIdOrNumber);
    if (direct) return direct;
    for (const o of this.orders.values()) {
      if (o.orderNumber === orderIdOrNumber || o.id === orderIdOrNumber) {
        return o;
      }
    }
    return undefined;
  }

  // Order State Machine Validation with Status History
  transitionOrderStatus(
    orderId: string,
    targetStatus: OrderStatus,
    message?: string,
    createdBy = 'SYSTEM'
  ): { success: boolean; order?: Order; error?: string } {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: 'Order not found' };

    const validTransitions: Record<OrderStatus, OrderStatus[]> = {
      PENDING_PAYMENT: ['PAYMENT_CONFIRMED', 'PAYMENT_SUBMITTED', 'CONFIRMED', 'ORDER_RECEIVED', 'CANCELLED'],
      ORDER_RECEIVED: ['SHOP_CONFIRMED', 'STORE_ACCEPTED', 'CANCELLED'],
      SHOP_CONFIRMED: ['PAYMENT_REQUIRED', 'PAYMENT_SUBMITTED', 'CANCELLED'],
      STORE_ACCEPTED: ['PAYMENT_REQUIRED', 'PAYMENT_SUBMITTED', 'PREPARING', 'CANCELLED'],
      PAYMENT_REQUIRED: ['PAYMENT_SUBMITTED', 'PAYMENT_VERIFIED', 'PAYMENT_CONFIRMED', 'CANCELLED'],
      PAYMENT_SUBMITTED: ['PAYMENT_VERIFIED', 'PAYMENT_CONFIRMED', 'PAYMENT_REQUIRED', 'CANCELLED'],
      PAYMENT_CONFIRMED: ['CONFIRMED', 'STORE_ACCEPTED', 'SHOP_CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'CANCELLED', 'REFUNDED'],
      PAYMENT_VERIFIED: ['CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'CANCELLED', 'REFUNDED'],
      CONFIRMED: ['STORE_ACCEPTED', 'SHOP_CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'CANCELLED'],
      PREPARING: ['READY_FOR_PICKUP', 'CANCELLED'],
      READY_FOR_PICKUP: ['DELIVERY_BOOKED', 'DELIVERY_ASSIGNED', 'PICKED_UP', 'CANCELLED'],
      DELIVERY_BOOKED: ['DELIVERY_ASSIGNED', 'PICKED_UP', 'OUT_FOR_DELIVERY', 'CANCELLED'],
      DELIVERY_ASSIGNED: ['PICKED_UP', 'OUT_FOR_DELIVERY', 'CANCELLED'],
      PICKED_UP: ['OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'],
      OUT_FOR_DELIVERY: ['DELIVERED', 'CANCELLED'],
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

    const defaultMessages: Partial<Record<OrderStatus, string>> = {
      ORDER_RECEIVED: 'Order request received and queued for confirmation',
      SHOP_CONFIRMED: 'Stationery availability confirmed by partner store',
      PAYMENT_REQUIRED: 'Availability confirmed! Payment is now due',
      PAYMENT_SUBMITTED: 'Payment reference submitted and under founder review',
      PAYMENT_VERIFIED: 'Payment verified! Preparing items for delivery',
      PAYMENT_CONFIRMED: 'Payment verified! Preparing items for delivery',
      CONFIRMED: 'Order confirmed and queued for fulfillment',
      STORE_ACCEPTED: 'Shop has accepted your order',
      PREPARING: 'Your stationery is being packed',
      READY_FOR_PICKUP: 'Your order is ready for pickup',
      DELIVERY_BOOKED: 'Delivery partner has been booked',
      DELIVERY_ASSIGNED: 'Delivery partner has been assigned for pickup',
      PICKED_UP: 'Order picked up by delivery partner',
      OUT_FOR_DELIVERY: 'Your order is on the way',
      DELIVERED: 'Delivered successfully to your address',
      CANCELLED: 'Order has been cancelled',
      REFUNDED: 'Payment has been refunded',
    };

    this.addOrderStatusHistory(
      orderId,
      targetStatus,
      message || defaultMessages[targetStatus] || `Order status updated to ${targetStatus}`,
      createdBy
    );

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
    this.addOrderStatusHistory(orderId, 'CANCELLED', 'Order cancelled by customer', 'CUSTOMER');
    return { success: true, order };
  }

  // --- Order Status History & Payments ---
  addOrderStatusHistory(
    orderId: string,
    status: OrderStatus,
    message: string,
    createdBy = 'SYSTEM'
  ): OrderStatusHistoryItem {
    const historyItem: OrderStatusHistoryItem = {
      id: `hist_${randomUUID().substring(0, 8)}`,
      orderId,
      status,
      message,
      createdBy,
      createdAt: new Date().toISOString(),
    };
    const existing = this.orderStatusHistory.get(orderId) || [];
    existing.push(historyItem);
    this.orderStatusHistory.set(orderId, existing);
    return historyItem;
  }

  getOrderStatusHistory(orderId: string): OrderStatusHistoryItem[] {
    return this.orderStatusHistory.get(orderId) || [];
  }

  addPaymentRecord(record: PaymentRecord): void {
    const existing = this.payments.get(record.orderId) || [];
    existing.push(record);
    this.payments.set(record.orderId, existing);
  }

  getPaymentsForOrder(orderId: string): PaymentRecord[] {
    return this.payments.get(orderId) || [];
  }

  // --- Shop Owner Operations ---
  getShopOrders(storeId?: string, statusFilter?: string): Order[] {
    const targetStoreId = storeId || this.stores[0]?.id;
    let list = Array.from(this.orders.values()).filter(o => !targetStoreId || o.storeId === targetStoreId);
    if (statusFilter && statusFilter !== 'ALL') {
      if (statusFilter === 'NEW') {
        list = list.filter(o => o.status === 'CONFIRMED');
      } else if (statusFilter === 'ACTIVE') {
        list = list.filter(o => ['STORE_ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'PICKED_UP', 'OUT_FOR_DELIVERY'].includes(o.status));
      } else if (statusFilter === 'COMPLETED') {
        list = list.filter(o => o.status === 'DELIVERED');
      }
    }
    return list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }

  shopAcceptOrder(orderId: string, _storeId?: string): { success: boolean; order?: Order; error?: string } {
    return this.transitionOrderStatus(orderId, 'STORE_ACCEPTED', 'Shop has accepted your order', 'SHOP_OWNER');
  }

  shopRejectOrder(orderId: string, _storeId?: string, reason?: string): { success: boolean; order?: Order; error?: string } {
    return this.transitionOrderStatus(
      orderId,
      'CANCELLED',
      reason ? `Order rejected by shop: ${reason}` : 'Shop was unable to accept your order',
      'SHOP_OWNER'
    );
  }

  shopStartPacking(orderId: string, _storeId?: string): { success: boolean; order?: Order; error?: string } {
    return this.transitionOrderStatus(orderId, 'PREPARING', 'Your stationery is being packed', 'SHOP_OWNER');
  }

  shopReadyForPickup(orderId: string, _storeId?: string): { success: boolean; order?: Order; error?: string } {
    return this.transitionOrderStatus(orderId, 'READY_FOR_PICKUP', 'Your order is ready for pickup', 'SHOP_OWNER');
  }

  advanceDeliveryStatus(
    orderId: string,
    nextStatus: 'PICKED_UP' | 'OUT_FOR_DELIVERY' | 'DELIVERED'
  ): { success: boolean; order?: Order; error?: string } {
    const res = this.transitionOrderStatus(orderId, nextStatus, undefined, 'DELIVERY_PARTNER');
    if (res.success && res.order) {
      res.order.deliveryStatus = nextStatus;
    }
    return res;
  }

  // --- Strict Email-First Authentication & Profile Operations ---
  checkEmailExists(email: string): { exists: boolean; firstName?: string; user?: any } {
    const key = email.trim().toLowerCase();
    const cred = this.userCredentials.get(key);
    if (cred) {
      const profile = this.userProfiles.get(cred.userId);
      return { exists: true, firstName: profile?.firstName, user: profile };
    }
    for (const p of this.userProfiles.values()) {
      if (p.email && p.email.trim().toLowerCase() === key) {
        return { exists: true, firstName: p.firstName, user: p };
      }
    }
    return { exists: false };
  }

  registerUser(params: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phone?: string;
    selectedClass?: string;
    selectedSchool?: string;
  }): { success: boolean; user?: any; error?: string } {
    const emailKey = params.email.trim().toLowerCase();
    if (this.checkEmailExists(emailKey).exists) {
      return { success: false, error: 'An account with this email already exists' };
    }

    if (!params.password || params.password.length < 6) {
      return { success: false, error: 'Password must be at least 6 characters' };
    }

    const userId = `user_${randomUUID().substring(0, 10)}`;
    const salt = randomBytes(16).toString('hex');
    const passwordHash = this.hashPassword(params.password, salt);

    this.userCredentials.set(emailKey, {
      userId,
      email: emailKey,
      passwordHash,
      salt,
    });

    const profile = {
      id: userId,
      authUserId: userId,
      firstName: params.firstName.trim(),
      lastName: (params.lastName || '').trim(),
      phone: (params.phone || '').trim(),
      email: emailKey,
      avatarUrl: '',
      role: 'CUSTOMER',
      onboardingCompleted: true,
      selectedClass: params.selectedClass || 'Class 7',
      selectedSchool: params.selectedSchool || 'KidsG Partner School',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    this.userProfiles.set(userId, profile);
    this.carts.set(userId, []);
    this.addresses.set(userId, []);

    return { success: true, user: profile };
  }

  authenticateWithPassword(email: string, password: string): { success: boolean; user?: any; error?: string } {
    const emailKey = email.trim().toLowerCase();
    const cred = this.userCredentials.get(emailKey);
    if (!cred) {
      return { success: false, error: 'Invalid email or password' };
    }

    const computedHash = this.hashPassword(password, cred.salt);
    if (computedHash !== cred.passwordHash) {
      return { success: false, error: 'Invalid email or password' };
    }

    const profile = this.userProfiles.get(cred.userId);
    return { success: true, user: profile };
  }

  getProfile(userId: string): any {
    return this.userProfiles.get(userId);
  }

  updateProfile(userId: string, updates: any): any {
    const existing = this.getProfile(userId) || {
      id: userId,
      authUserId: userId,
      createdAt: new Date().toISOString(),
    };
    const updated = { ...existing, ...updates, updatedAt: new Date().toISOString() };
    this.userProfiles.set(userId, updated);
    return updated;
  }

  findUserByContact(contact: string): any {
    const cleaned = contact.trim().toLowerCase();
    const digits = cleaned.replace(/\D/g, '');

    // 1. Check credentials by email
    const cred = this.userCredentials.get(cleaned);
    if (cred) {
      return this.userProfiles.get(cred.userId);
    }

    // 2. Check profiles by email or phone digits
    for (const p of this.userProfiles.values()) {
      if (p.email && p.email.trim().toLowerCase() === cleaned) {
        return p;
      }
      if (p.phone) {
        const phoneDigits = p.phone.replace(/\D/g, '');
        if (phoneDigits && digits && (phoneDigits === digits || phoneDigits.endsWith(digits) || digits.endsWith(phoneDigits))) {
          return p;
        }
      }
    }
    return null;
  }

  resetUserData(preserveEmails: string[] = ['veenithkumars31@gmail.com', 'founder@kidsg.in']): {
    clearedOrders: number;
    clearedProfiles: number;
    clearedCarts: number;
  } {
    const keepUserIds = new Set<string>();
    const normalizedPreserve = preserveEmails.map(e => e.trim().toLowerCase());

    for (const [email, cred] of this.userCredentials.entries()) {
      if (normalizedPreserve.includes(email.toLowerCase())) {
        keepUserIds.add(cred.userId);
      }
    }

    for (const [userId, profile] of this.userProfiles.entries()) {
      if (profile.email && normalizedPreserve.includes(profile.email.toLowerCase())) {
        keepUserIds.add(userId);
      }
    }

    const clearedOrders = this.orders.size;
    this.orders.clear();
    this.orderStatusHistory.clear();
    this.payments.clear();
    this.paymentSubmissions.clear();
    this.deliveryBookings.clear();

    const clearedCarts = this.carts.size;
    this.carts.clear();
    this.addresses.clear();
    this.wishlists.clear();

    let clearedProfiles = 0;
    for (const [userId, profile] of this.userProfiles.entries()) {
      if (!keepUserIds.has(userId)) {
        this.userProfiles.delete(userId);
        clearedProfiles++;
      }
    }

    for (const [email, cred] of this.userCredentials.entries()) {
      if (!keepUserIds.has(cred.userId) && !normalizedPreserve.includes(email.toLowerCase())) {
        this.userCredentials.delete(email);
      }
    }

    return { clearedOrders, clearedProfiles, clearedCarts };
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
    return this.tickets.filter(t => t.userId === userId);
  }

  getSupportTicketById(id: string, userId: string): SupportTicket | undefined {
    return this.tickets.find(t => (t.id === id || t.ticketNumber === id) && t.userId === userId);
  }

  // --- Founder / Admin Shop Management ---
  getStoresAdmin(): Store[] {
    return this.stores;
  }

  getStoreByIdAdmin(id: string): Store | undefined {
    return this.stores.find(s => s.id === id);
  }

  createStore(data: Partial<Store>, adminId = 'user_founder_01'): Store {
    const storeId = data.id || `store_${randomUUID().substring(0, 8)}`;
    const newStore: Store = {
      id: storeId,
      code: data.code || `BB-SHP-${Math.floor(10 + Math.random() * 90)}`,
      name: data.name || 'New BookBuddys Partner Store',
      ownerName: data.ownerName || 'Store Manager',
      phone: data.phone || '+91 98765 00000',
      email: data.email || 'partner@bookbuddys.in',
      address: data.address || 'Bengaluru, Karnataka',
      locality: data.locality || 'Bengaluru',
      area: data.area || 'Bengaluru',
      city: data.city || 'Bengaluru',
      state: data.state || 'Karnataka',
      pincode: data.pincode || '560001',
      landmark: data.landmark || '',
      latitude: data.latitude || 12.9716,
      longitude: data.longitude || 77.5946,
      deliveryRadiusKm: data.deliveryRadiusKm || 6.0,
      isActive: data.isActive !== undefined ? data.isActive : true,
      openTime: data.openTime || '08:00',
      closeTime: data.closeTime || '21:30',
      businessHours: data.businessHours || {
        monday: { open: '08:00', close: '21:30', isOpen: true },
        tuesday: { open: '08:00', close: '21:30', isOpen: true },
        wednesday: { open: '08:00', close: '21:30', isOpen: true },
        thursday: { open: '08:00', close: '21:30', isOpen: true },
        friday: { open: '08:00', close: '21:30', isOpen: true },
        saturday: { open: '08:00', close: '22:00', isOpen: true },
        sunday: { open: '09:00', close: '20:00', isOpen: true },
      },
      categoriesSold: data.categoriesSold || ['Notebooks & Registers', 'Pens & Refills', 'Geometry & Scales'],
      founderNotes: data.founderNotes || '',
      verificationStatus: data.verificationStatus || 'ACTIVE',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    this.stores.push(newStore);

    // Initialize inventory for all active products
    this.products.forEach(p => {
      const key = `${storeId}_${p.id}`;
      this.storeInventoryItems.set(key, {
        id: `inv_${storeId.substring(0, 8)}_${p.id.substring(0, 8)}`,
        storeId,
        productId: p.id,
        productName: p.name,
        price: p.price,
        mrp: p.mrp,
        discountPercent: p.discountPercent,
        quantity: 20,
        reservedQuantity: 0,
        sellableQuantity: 20,
        lowStockThreshold: 5,
        isAvailable: true,
        costPrice: Math.round(p.price * 0.7),
        founderNotes: `Initial stock for ${newStore.name}`,
        lastVerifiedAt: new Date().toISOString(),
      });
    });

    this.addAuditLog({
      adminId,
      action: 'CREATE_SHOP',
      entityType: 'STORE',
      entityId: storeId,
      details: { name: newStore.name, address: newStore.address, coordinates: [newStore.latitude, newStore.longitude] },
    });

    return newStore;
  }

  updateStore(id: string, data: Partial<Store>, adminId = 'user_founder_01'): Store | undefined {
    const store = this.getStoreById(id);
    if (!store) return undefined;

    Object.assign(store, data, { updatedAt: new Date().toISOString() });

    this.addAuditLog({
      adminId,
      action: 'UPDATE_SHOP',
      entityType: 'STORE',
      entityId: id,
      details: data,
    });

    return store;
  }

  toggleStoreStatus(id: string, isActive: boolean, adminId = 'user_founder_01'): Store | undefined {
    const store = this.getStoreById(id);
    if (!store) return undefined;
    store.isActive = isActive;
    store.updatedAt = new Date().toISOString();

    this.addAuditLog({
      adminId,
      action: isActive ? 'ACTIVATE_SHOP' : 'DEACTIVATE_SHOP',
      entityType: 'STORE',
      entityId: id,
      details: { isActive },
    });

    return store;
  }

  // --- Shop-Specific Inventory Management ---
  getStoreInventory(storeId: string): StoreInventoryItem[] {
    const items: StoreInventoryItem[] = [];
    this.products.forEach(p => {
      const key = `${storeId}_${p.id}`;
      let item = this.storeInventoryItems.get(key);
      if (!item) {
        item = {
          id: `inv_${storeId.substring(0, 6)}_${p.id.substring(0, 8)}`,
          storeId,
          productId: p.id,
          productName: p.name,
          price: p.price,
          mrp: p.mrp,
          discountPercent: p.discountPercent,
          quantity: p.stock,
          reservedQuantity: 0,
          sellableQuantity: p.stock,
          lowStockThreshold: 5,
          isAvailable: p.isActive,
          costPrice: Math.round(p.price * 0.7),
          lastVerifiedAt: new Date().toISOString(),
        };
        this.storeInventoryItems.set(key, item);
      }
      items.push(item);
    });
    return items;
  }

  getStoreInventoryItem(storeId: string, productId: string): StoreInventoryItem | undefined {
    return this.storeInventoryItems.get(`${storeId}_${productId}`);
  }

  updateStoreInventoryItem(
    storeId: string,
    productId: string,
    data: Partial<StoreInventoryItem>,
    adminId = 'user_founder_01'
  ): StoreInventoryItem {
    const key = `${storeId}_${productId}`;
    let item = this.storeInventoryItems.get(key);
    const product = this.getProductById(productId);

    const priceVal = data.price ?? (data.pricePaise !== undefined ? data.pricePaise / 100 : undefined);
    const mrpVal = data.mrp ?? (data.mrpPaise !== undefined ? data.mrpPaise / 100 : undefined);
    const qtyVal = data.quantity ?? (data as any).availableQuantity;

    if (!item) {
      const q = Math.max(0, qtyVal ?? 10);
      const resQ = data.reservedQuantity ?? 0;
      const sellQ = Math.max(0, q - resQ);
      const p = priceVal ?? (product?.price || 50);
      const m = mrpVal ?? (product?.mrp || 60);

      item = {
        id: `inv_${storeId.substring(0, 6)}_${productId.substring(0, 8)}`,
        storeId,
        productId,
        productName: product?.name || 'Stationery Product',
        price: p,
        pricePaise: Math.round(p * 100),
        mrp: m,
        mrpPaise: Math.round(m * 100),
        discountPercent: data.discountPercent ?? (product?.discountPercent || 0),
        quantity: q,
        reservedQuantity: resQ,
        sellableQuantity: sellQ,
        availableQuantity: sellQ,
        lowStockThreshold: data.lowStockThreshold ?? 5,
        isAvailable: data.isAvailable ?? true,
        costPrice: data.costPrice ?? (data.costPricePaise ? data.costPricePaise / 100 : undefined),
        founderNotes: data.founderNotes,
        lastVerifiedAt: new Date().toISOString(),
      };
    } else {
      const newQty = qtyVal !== undefined ? Math.max(0, qtyVal) : item.quantity;
      const newReserved = data.reservedQuantity !== undefined ? Math.max(0, data.reservedQuantity) : item.reservedQuantity;
      const newSellable = Math.max(0, newQty - newReserved);
      const newPrice = priceVal !== undefined ? priceVal : item.price;
      const newMrp = mrpVal !== undefined ? mrpVal : item.mrp;
      
      Object.assign(item, data, {
        price: newPrice,
        pricePaise: Math.round(newPrice * 100),
        mrp: newMrp,
        mrpPaise: Math.round(newMrp * 100),
        quantity: newQty,
        reservedQuantity: newReserved,
        sellableQuantity: newSellable,
        availableQuantity: newSellable,
        lastVerifiedAt: new Date().toISOString(),
      });
    }

    this.storeInventoryItems.set(key, item);

    this.addAuditLog({
      adminId,
      action: 'UPDATE_INVENTORY',
      entityType: 'INVENTORY',
      entityId: key,
      details: { storeId, productId, ...data },
    });

    return item;
  }

  bulkUpdateStoreInventory(
    storeId: string,
    updates: Array<{ productId: string; price?: number; pricePaise?: number; mrp?: number; mrpPaise?: number; quantity?: number; availableQuantity?: number; isAvailable?: boolean; costPrice?: number }>
  ): { successCount: number; updatedCount: number; errors: string[] } {
    let successCount = 0;
    const errors: string[] = [];

    updates.forEach((u, idx) => {
      if (!u.productId) {
        errors.push(`Row ${idx + 1}: Missing productId`);
        return;
      }
      const q = u.quantity ?? u.availableQuantity;
      if (q !== undefined && q < 0) {
        errors.push(`Row ${idx + 1}: Quantity cannot be negative`);
        return;
      }
      const p = u.price ?? (u.pricePaise ? u.pricePaise / 100 : undefined);
      if (p !== undefined && p <= 0) {
        errors.push(`Row ${idx + 1}: Price must be positive`);
        return;
      }

      this.updateStoreInventoryItem(storeId, u.productId, u);
      successCount++;
    });

    return { successCount, updatedCount: successCount, errors };
  }

  // --- Product Catalogue Management ---
  getAllProductsAdmin(): Product[] {
    return this.products;
  }

  createProduct(data: Partial<Product>, adminId = 'user_founder_01'): Product {
    const prodId = data.id || `prod_${randomUUID().substring(0, 8)}`;
    const slug = data.slug || (data.name ? data.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '') : prodId);
    const newProd: Product = {
      id: prodId,
      name: data.name || 'New Stationery Item',
      slug,
      description: data.description || 'Verified school supplies',
      brand: data.brand || 'BookBuddys Partner',
      categoryId: data.categoryId || 'cat_notebooks',
      categoryName: data.categoryName,
      subcategoryId: data.subcategoryId,
      imageUrl: data.imageUrl || 'https://images.unsplash.com/photo-1585336261026-775af4609c0d?w=500',
      galleryImages: data.galleryImages || [],
      price: data.price || 50,
      mrp: data.mrp || 60,
      discountPercent: data.discountPercent || 0,
      stock: data.stock || 25,
      unit: data.unit || 'piece',
      gradeLevel: data.gradeLevel || 'All',
      isFeatured: data.isFeatured || false,
      isActive: data.isActive !== undefined ? data.isActive : true,
      specs: data.specs || {},
      sku: data.sku || `SKU-${Math.floor(1000 + Math.random() * 9000)}`,
      badgeText: data.badgeText,
    };

    this.products.unshift(newProd);

    // Add inventory across all stores
    this.stores.forEach(s => {
      this.updateStoreInventoryItem(s.id, prodId, {
        price: newProd.price,
        mrp: newProd.mrp,
        discountPercent: newProd.discountPercent,
        quantity: newProd.stock,
        isAvailable: newProd.isActive,
      });
    });

    this.addAuditLog({
      adminId,
      action: 'CREATE_PRODUCT',
      entityType: 'PRODUCT',
      entityId: prodId,
      details: { name: newProd.name, price: newProd.price, sku: newProd.sku },
    });

    return newProd;
  }

  updateProduct(id: string, data: Partial<Product>, adminId = 'user_founder_01'): Product | undefined {
    const prod = this.getProductById(id);
    if (!prod) return undefined;

    Object.assign(prod, data);

    this.addAuditLog({
      adminId,
      action: 'UPDATE_PRODUCT',
      entityType: 'PRODUCT',
      entityId: id,
      details: data,
    });

    return prod;
  }

  toggleProductStatus(id: string, isActive: boolean, adminId = 'user_founder_01'): Product | undefined {
    const prod = this.getProductById(id);
    if (!prod) return undefined;
    prod.isActive = isActive;

    this.addAuditLog({
      adminId,
      action: isActive ? 'ACTIVATE_PRODUCT' : 'DEACTIVATE_PRODUCT',
      entityType: 'PRODUCT',
      entityId: id,
      details: { isActive },
    });

    return prod;
  }

  // --- Location-Aware Discovery & Serviceability ---
  findNearbyStores(
    lat: number,
    lng: number,
    maxDistanceKm = 12.0
  ): Array<Store & { distanceKm: number; isWithinServiceArea: boolean; isOpenNow: boolean }> {
    const now = new Date();
    const currentHours = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    const days = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
    const currentDay = days[now.getDay()];

    return this.stores
      .filter(s => s.isActive)
      .map(store => {
        const distanceKm = calculateDistanceKm(lat, lng, store.latitude, store.longitude);
        const radius = store.deliveryRadiusKm || maxDistanceKm;
        const isWithinServiceArea = distanceKm <= radius;

        // Check if open right now
        let isOpenNow = true;
        if (store.isOpenOverride !== undefined && store.isOpenOverride !== null) {
          isOpenNow = store.isOpenOverride;
        } else if (store.businessHours && store.businessHours[currentDay]) {
          const bh = store.businessHours[currentDay];
          isOpenNow = bh.isOpen && currentHours >= bh.open && currentHours <= bh.close;
        } else {
          isOpenNow = currentHours >= store.openTime && currentHours <= store.closeTime;
        }

        return {
          ...store,
          distanceKm,
          isWithinServiceArea,
          isOpenNow,
        };
      })
      .filter(s => s.distanceKm <= (s.deliveryRadiusKm || maxDistanceKm))
      .sort((a, b) => {
        // Prioritize: 1. Within service area, 2. Open now, 3. Closest distance
        if (a.isWithinServiceArea && !b.isWithinServiceArea) return -1;
        if (!a.isWithinServiceArea && b.isWithinServiceArea) return 1;
        if (a.isOpenNow && !b.isOpenNow) return -1;
        if (!a.isOpenNow && b.isOpenNow) return 1;
        return a.distanceKm - b.distanceKm;
      });
  }

  getLocationEligibleProducts(
    lat?: number,
    lng?: number,
    categoryId?: string,
    search?: string
  ): { products: Array<Product & { shopSellingPrice: number; shopStock: number; fulfillmentStoreId: string; fulfillmentStoreName: string; distanceKm?: number }>; nearestStore?: Store; store?: Store; serviceable: boolean } {
    const customerLat = lat ?? 12.9358;
    const customerLng = lng ?? 77.6251;

    const nearbyStores = this.findNearbyStores(customerLat, customerLng);
    const eligibleStore = nearbyStores.find(s => s.isWithinServiceArea && s.isOpenNow) || nearbyStores[0];

    const serviceable = eligibleStore ? eligibleStore.isWithinServiceArea : false;
    let list = this.products.filter(p => p.isActive);

    if (categoryId) {
      list = list.filter(p => p.categoryId === categoryId || p.subcategoryId === categoryId);
    }

    if (search && search.trim()) {
      const q = search.trim().toLowerCase();
      list = list.filter(p => p.name.toLowerCase().includes(q) || p.brand.toLowerCase().includes(q) || p.description.toLowerCase().includes(q));
    }

    const productsWithShopPricing = list.map(prod => {
      const invKey = eligibleStore ? `${eligibleStore.id}_${prod.id}` : '';
      const inv = invKey ? this.storeInventoryItems.get(invKey) : undefined;

      const shopSellingPrice = inv ? inv.price : prod.price;
      const shopStock = inv ? inv.sellableQuantity : prod.stock;

      return {
        ...prod,
        price: shopSellingPrice,
        shopSellingPrice,
        shopStock,
        fulfillmentStoreId: eligibleStore ? eligibleStore.id : 'store_vidya_depot',
        fulfillmentStoreName: eligibleStore ? eligibleStore.name : 'Vidya Book & Stationery Depot',
        distanceKm: eligibleStore ? eligibleStore.distanceKm : undefined,
        inventory: inv || {
          availableQuantity: shopStock,
          lowStockThreshold: 5,
          storeName: eligibleStore ? eligibleStore.name : 'Bengaluru Store',
        },
      };
    });

    return {
      products: productsWithShopPricing,
      nearestStore: eligibleStore,
      store: eligibleStore,
      serviceable,
    };
  }

  // --- Order & Workflow Management ---
  getAdminOrders(filters?: { status?: string; search?: string; storeId?: string }): Order[] {
    let list = Array.from(this.orders.values());

    if (filters?.storeId) {
      list = list.filter(o => o.storeId === filters.storeId);
    }

    if (filters?.status && filters.status !== 'ALL') {
      list = list.filter(o => o.status === filters.status);
    }

    if (filters?.search && filters.search.trim()) {
      const q = filters.search.trim().toLowerCase();
      list = list.filter(o =>
        o.orderNumber.toLowerCase().includes(q) ||
        (o.addressSnapshot?.name || '').toLowerCase().includes(q) ||
        (o.addressSnapshot?.phone || '').includes(q)
      );
    }

    return list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }

  // Step 3: Founder verifies shop availability
  confirmOrderAvailability(
    orderId: string,
    action: 'CONFIRM' | 'REJECT' | 'SUBSTITUTE',
    notes?: string,
    adminId = 'user_founder_01'
  ): { success: boolean; order?: Order; error?: string } {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: 'Order not found' };

    if (action === 'CONFIRM') {
      const res = this.transitionOrderStatus(
        orderId,
        'SHOP_CONFIRMED',
        notes || 'Founder confirmed availability with partner stationery store',
        adminId
      );
      if (res.success && res.order) {
        res.order.notes = notes ? `${res.order.notes ? res.order.notes + ' | ' : ''}[Avail Confirmed]: ${notes}` : res.order.notes;
      }
      this.addAuditLog({ adminId, action: 'CONFIRM_AVAILABILITY', entityType: 'ORDER', entityId: orderId, details: { notes } });
      return res;
    } else if (action === 'REJECT') {
      const res = this.transitionOrderStatus(
        orderId,
        'CANCELLED',
        notes || 'Item is currently unavailable at partner store',
        adminId
      );
      this.addAuditLog({ adminId, action: 'REJECT_AVAILABILITY', entityType: 'ORDER', entityId: orderId, details: { notes } });
      return res;
    } else {
      // Substitution proposal
      this.addOrderStatusHistory(orderId, order.status, `Substitution requested: ${notes || 'Equivalent brand proposed'}`, adminId);
      this.addAuditLog({ adminId, action: 'SUBSTITUTION_PROPOSED', entityType: 'ORDER', entityId: orderId, details: { notes } });
      return { success: true, order };
    }
  }

  // Step 4: Accept order & request payment
  acceptOrderRequestPayment(
    orderId: string,
    adminId = 'user_founder_01',
    notes?: string
  ): { success: boolean; order?: Order; error?: string } {
    const res = this.transitionOrderStatus(
      orderId,
      'PAYMENT_REQUIRED',
      notes || 'Order accepted by founder. Payment is now due.',
      adminId
    );
    this.addAuditLog({ adminId, action: 'ACCEPT_ORDER_REQUEST_PAYMENT', entityType: 'ORDER', entityId: orderId, details: { notes } });
    return res;
  }

  // Step 5: Customer submits payment reference (UTR)
  submitManualPayment(
    orderId: string,
    userId: string,
    data: { transactionReference: string; amount?: number; screenshotUrl?: string; notes?: string }
  ): { success: boolean; submission?: PaymentSubmission; error?: string } {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: 'Order not found' };

    const submissionId = `pay_sub_${randomUUID().substring(0, 8)}`;
    const sub: PaymentSubmission = {
      id: submissionId,
      orderId,
      orderNumber: order.orderNumber,
      userId,
      amount: data.amount ?? order.total,
      paymentMethod: 'UPI',
      transactionReference: data.transactionReference.trim(),
      screenshotUrl: data.screenshotUrl,
      status: 'PENDING_VERIFICATION',
      notes: data.notes,
      submittedAt: new Date().toISOString(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    this.paymentSubmissions.set(submissionId, sub);

    // Transition order state to PAYMENT_SUBMITTED
    this.transitionOrderStatus(
      orderId,
      'PAYMENT_SUBMITTED',
      `Payment UTR ${sub.transactionReference} submitted for founder verification`,
      'CUSTOMER'
    );

    return { success: true, submission: sub };
  }

  getPendingPayments(): PaymentSubmission[] {
    return Array.from(this.paymentSubmissions.values())
      .filter(p => p.status === 'PENDING_VERIFICATION')
      .sort((a, b) => new Date(b.submittedAt).getTime() - new Date(a.submittedAt).getTime());
  }

  getPaymentSubmissionById(id: string): PaymentSubmission | undefined {
    return this.paymentSubmissions.get(id);
  }

  getPaymentSubmissionsForOrder(orderId: string): PaymentSubmission[] {
    return Array.from(this.paymentSubmissions.values()).filter(p => p.orderId === orderId);
  }

  // Step 6: Founder verifies payment
  verifyManualPayment(
    submissionId: string,
    action: 'VERIFY' | 'REJECT',
    rejectionReason?: string,
    adminId = 'user_founder_01'
  ): { success: boolean; order?: Order; error?: string } {
    const sub = this.paymentSubmissions.get(submissionId);
    if (!sub) return { success: false, error: 'Payment submission not found' };

    const order = this.orders.get(sub.orderId);
    if (!order) return { success: false, error: 'Order not found' };

    if (action === 'VERIFY') {
      sub.status = 'VERIFIED';
      sub.verifiedBy = adminId;
      sub.verifiedAt = new Date().toISOString();
      sub.updatedAt = new Date().toISOString();

      order.paymentStatus = 'PAYMENT_VERIFIED';
      order.updatedAt = new Date().toISOString();

      // Record verified payment in financial payment records
      this.addPaymentRecord({
        id: `pay_${randomUUID().substring(0, 8)}`,
        orderId: order.id,
        userId: order.userId,
        amount: sub.amount,
        currency: 'INR',
        provider: 'UPI_MANUAL_VERIFIED',
        transactionId: sub.transactionReference,
        status: 'SUCCESS',
        createdAt: new Date().toISOString(),
      });

      const res = this.transitionOrderStatus(
        order.id,
        'PAYMENT_VERIFIED',
        `Payment of ₹${sub.amount} verified by founder (Ref: ${sub.transactionReference})`,
        adminId
      );

      this.addAuditLog({
        adminId,
        action: 'VERIFY_PAYMENT',
        entityType: 'PAYMENT',
        entityId: submissionId,
        details: { orderId: order.id, amount: sub.amount, utr: sub.transactionReference },
      });

      return res;
    } else {
      sub.status = 'REJECTED';
      sub.rejectionReason = rejectionReason || 'Payment could not be verified in bank records';
      sub.verifiedBy = adminId;
      sub.verifiedAt = new Date().toISOString();
      sub.updatedAt = new Date().toISOString();

      this.addOrderStatusHistory(
        order.id,
        'PAYMENT_REQUIRED',
        `Payment rejected: ${sub.rejectionReason}. Please re-submit valid UTR or contact founder.`,
        adminId
      );

      this.addAuditLog({
        adminId,
        action: 'REJECT_PAYMENT',
        entityType: 'PAYMENT',
        entityId: submissionId,
        details: { reason: sub.rejectionReason, utr: sub.transactionReference },
      });

      return { success: true, order };
    }
  }

  // Step 7: Founder purchases and prepares the items
  markOrderProcuring(orderId: string, adminId = 'user_founder_01', notes?: string): { success: boolean; order?: Order; error?: string } {
    const res = this.transitionOrderStatus(
      orderId,
      'PREPARING',
      notes || 'Items are being physically procured from store and packed',
      adminId
    );
    this.addAuditLog({ adminId, action: 'MARK_PREPARING', entityType: 'ORDER', entityId: orderId, details: { notes } });
    return res;
  }

  markOrderReadyForPickup(orderId: string, adminId = 'user_founder_01', notes?: string): { success: boolean; order?: Order; error?: string } {
    const res = this.transitionOrderStatus(
      orderId,
      'READY_FOR_PICKUP',
      notes || 'Package packed and ready for delivery partner pickup',
      adminId
    );
    this.addAuditLog({ adminId, action: 'MARK_READY_FOR_PICKUP', entityType: 'ORDER', entityId: orderId, details: { notes } });
    return res;
  }

  // Step 8: Book delivery
  bookDelivery(
    orderId: string,
    bookingData: Partial<DeliveryBooking>,
    adminId = 'user_founder_01'
  ): { success: boolean; booking?: DeliveryBooking; error?: string } {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: 'Order not found' };

    const store = this.getStoreById(order.storeId) || this.stores[0];

    const booking: DeliveryBooking = {
      id: `del_${randomUUID().substring(0, 8)}`,
      orderId,
      orderNumber: order.orderNumber,
      provider: bookingData.provider || 'PORTER',
      bookingReference: bookingData.bookingReference || `PRT-${Math.floor(100000 + Math.random() * 900000)}`,
      deliveryFee: bookingData.deliveryFee ?? 30,
      riderName: bookingData.riderName || 'Local Delivery Partner',
      riderPhone: bookingData.riderPhone || '+91 99887 76655',
      vehicleNumber: bookingData.vehicleNumber,
      pickupAddressSnapshot: {
        name: store.name,
        address: store.address,
        phone: store.phone,
        latitude: store.latitude,
        longitude: store.longitude,
      },
      deliveryAddressSnapshot: order.addressSnapshot,
      pickupStatus: 'PENDING_PICKUP',
      trackingUrl: bookingData.trackingUrl,
      etaMinutes: bookingData.etaMinutes ?? 35,
      founderNotes: bookingData.founderNotes,
      bookedAt: new Date().toISOString(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    this.deliveryBookings.set(orderId, booking);

    this.transitionOrderStatus(
      orderId,
      'DELIVERY_BOOKED',
      `Delivery booked via ${booking.provider} (Ref: ${booking.bookingReference}). Rider: ${booking.riderName}`,
      adminId
    );

    this.addAuditLog({
      adminId,
      action: 'BOOK_DELIVERY',
      entityType: 'DELIVERY',
      entityId: booking.id,
      details: { orderId, provider: booking.provider, ref: booking.bookingReference },
    });

    return { success: true, booking };
  }

  getDeliveryBookingForOrder(orderId: string): DeliveryBooking | undefined {
    return this.deliveryBookings.get(orderId);
  }

  // Step 9: Order Dispatch & Delivery Tracking Status Updates
  updateDeliveryBookingStatus(
    orderId: string,
    status: DeliveryBooking['pickupStatus'],
    details?: { riderName?: string; riderPhone?: string; etaMinutes?: number; trackingUrl?: string },
    adminId = 'user_founder_01'
  ): { success: boolean; order?: Order; error?: string } {
    const booking = this.deliveryBookings.get(orderId);
    if (booking) {
      booking.pickupStatus = status;
      if (details?.riderName) booking.riderName = details.riderName;
      if (details?.riderPhone) booking.riderPhone = details.riderPhone;
      if (details?.etaMinutes) booking.etaMinutes = details.etaMinutes;
      if (details?.trackingUrl) booking.trackingUrl = details.trackingUrl;
      booking.updatedAt = new Date().toISOString();
      if (status === 'PICKED_UP') booking.pickedUpAt = new Date().toISOString();
      if (status === 'DELIVERED') booking.deliveredAt = new Date().toISOString();
    }

    let targetOrderStatus: OrderStatus = 'DELIVERY_BOOKED';
    let message = 'Delivery status updated';

    if (status === 'PICKED_UP') {
      targetOrderStatus = 'PICKED_UP';
      message = `Package picked up by ${booking?.riderName || 'delivery partner'}`;
    } else if (status === 'OUT_FOR_DELIVERY') {
      targetOrderStatus = 'OUT_FOR_DELIVERY';
      message = `Package is on the way for delivery (ETA: ${booking?.etaMinutes || 25} mins)`;
    } else if (status === 'DELIVERED') {
      targetOrderStatus = 'DELIVERED';
      message = 'Package delivered successfully to customer address';
    }

    const res = this.transitionOrderStatus(orderId, targetOrderStatus, message, adminId);

    this.addAuditLog({
      adminId,
      action: `DELIVERY_${status}`,
      entityType: 'DELIVERY',
      entityId: orderId,
      details: { status, ...details },
    });

    return res;
  }

  // Step 10: Cancellation & Refund Handling
  cancelOrderAdmin(orderId: string, reason: string, adminId = 'user_founder_01'): { success: boolean; order?: Order; error?: string } {
    const res = this.transitionOrderStatus(orderId, 'CANCELLED', `Cancelled: ${reason}`, adminId);
    this.addAuditLog({ adminId, action: 'CANCEL_ORDER', entityType: 'ORDER', entityId: orderId, details: { reason } });
    return res;
  }

  refundOrderAdmin(orderId: string, refundRef: string, adminId = 'user_founder_01'): { success: boolean; order?: Order; error?: string } {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: 'Order not found' };

    order.paymentStatus = 'REFUNDED';
    const res = this.transitionOrderStatus(orderId, 'REFUNDED', `Refund processed (Ref: ${refundRef})`, adminId);
    this.addAuditLog({ adminId, action: 'REFUND_ORDER', entityType: 'ORDER', entityId: orderId, details: { refundRef } });
    return res;
  }

  // --- Audit Logs & Platform Settings ---
  addAuditLog(entry: Omit<AuditLog, 'id' | 'createdAt'>): AuditLog {
    const log: AuditLog = {
      ...entry,
      id: `audit_${randomUUID().substring(0, 8)}`,
      createdAt: new Date().toISOString(),
    };
    this.auditLogs.unshift(log);
    return log;
  }

  getAuditLogs(limit = 100): AuditLog[] {
    return this.auditLogs.slice(0, limit);
  }

  getPlatformSettings(): PlatformSettings {
    return this.platformSettings;
  }

  updatePlatformSettings(settings: Partial<PlatformSettings>, adminId = 'user_founder_01'): PlatformSettings {
    if (settings.merchantUpi) Object.assign(this.platformSettings.merchantUpi, settings.merchantUpi);
    if (settings.deliveryRules) Object.assign(this.platformSettings.deliveryRules, settings.deliveryRules);
    if (settings.founderNotifications) Object.assign(this.platformSettings.founderNotifications, settings.founderNotifications);

    this.addAuditLog({
      adminId,
      action: 'UPDATE_SETTINGS',
      entityType: 'SETTINGS',
      entityId: 'platform_settings',
      details: settings,
    });

    return this.platformSettings;
  }

  getAdminOverview(): {
    todayOrders: number;
    awaitingStockConfirmation: number;
    awaitingPaymentVerification: number;
    paidOrdersPreparing: number;
    awaitingDeliveryBooking: number;
    outForDelivery: number;
    completedOrders: number;
    cancelledOrders: number;
    totalSalesPaise: number;
    pendingSalesPaise: number;
    activeShopsCount: number;
    totalShopsCount: number;
    lowStockCount: number;
    recentOrders: Order[];
  } {
    const allOrders = Array.from(this.orders.values());
    const today = new Date().toISOString().split('T')[0];
    const todayOrders = allOrders.filter(o => o.createdAt.startsWith(today)).length;

    const awaitingStockConfirmation = allOrders.filter(o => o.status === 'ORDER_RECEIVED' || o.status === 'PENDING_PAYMENT').length;
    const awaitingPaymentVerification = Array.from(this.paymentSubmissions.values()).filter(p => p.status === 'PENDING_VERIFICATION').length;
    const paidOrdersPreparing = allOrders.filter(o => o.status === 'PAYMENT_VERIFIED' || o.status === 'PREPARING').length;
    const awaitingDeliveryBooking = allOrders.filter(o => o.status === 'READY_FOR_PICKUP').length;
    const outForDelivery = allOrders.filter(o => o.status === 'PICKED_UP' || o.status === 'OUT_FOR_DELIVERY').length;
    const completedOrders = allOrders.filter(o => o.status === 'DELIVERED').length;
    const cancelledOrders = allOrders.filter(o => o.status === 'CANCELLED').length;

    const completed = allOrders.filter(o => o.status === 'DELIVERED' || o.status === 'PAYMENT_VERIFIED' || o.status === 'CONFIRMED');
    const totalSales = completed.reduce((sum, o) => sum + (o.total || 0), 0);
    const pendingOrders = allOrders.filter(o => o.status === 'ORDER_RECEIVED' || o.status === 'PAYMENT_REQUIRED' || o.status === 'PAYMENT_SUBMITTED');
    const pendingSales = pendingOrders.reduce((sum, o) => sum + (o.total || 0), 0);

    const activeShopsCount = this.stores.filter(s => s.isActive).length;
    const totalShopsCount = this.stores.length;

    const lowStockCount = Array.from(this.storeInventoryItems.values()).filter(inv => inv.sellableQuantity <= inv.lowStockThreshold).length;

    const recentOrders = allOrders
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, 8);

    return {
      todayOrders: todayOrders || allOrders.length,
      awaitingStockConfirmation,
      awaitingPaymentVerification,
      paidOrdersPreparing,
      awaitingDeliveryBooking,
      outForDelivery,
      completedOrders,
      cancelledOrders,
      totalSalesPaise: totalSales * 100,
      pendingSalesPaise: pendingSales * 100,
      activeShopsCount,
      totalShopsCount,
      lowStockCount,
      recentOrders,
    };
  }

  clearTransactionalData(): void {
    this.orders.clear();
    this.orderStatusHistory.clear();
    this.payments.clear();
    this.carts.clear();
    this.wishlists.clear();
    this.tickets = [];
    this.addresses.clear();
    this.paymentSubmissions.clear();
    this.deliveryBookings.clear();
    this.auditLogs = [];
  }
}

export const db = new KidsGDatabase();
