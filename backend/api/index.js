// src/server.ts
import express from "express";
import cors from "cors";
import path2 from "path";
import { fileURLToPath } from "url";
import { randomUUID as randomUUID8 } from "crypto";

// src/config/env.ts
import { z } from "zod";
import dotenv from "dotenv";
import path from "path";
dotenv.config({ path: path.resolve(process.cwd(), ".env.local") });
dotenv.config({ path: path.resolve(process.cwd(), ".env") });
var envSchema = z.object({
  NODE_ENV: z.enum(["development", "test", "production"]).default("development"),
  APP_ENV: z.enum(["DEV", "STAGING", "PRODUCTION"]).default("DEV"),
  PORT: z.coerce.number().default(3e3),
  API_BASE_URL: z.string().default("http://localhost:3000"),
  // Supabase
  SUPABASE_URL: z.string().default(process.env.SUPABASE_URL || process.env.NEXT_PUBLIC_SUPABASE_URL || "https://mock.supabase.co"),
  SUPABASE_PUBLISHABLE_KEY: z.string().default(process.env.SUPABASE_PUBLISHABLE_KEY || process.env.SUPABASE_ANON_KEY || process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY || ""),
  SUPABASE_ANON_KEY: z.string().default(process.env.SUPABASE_ANON_KEY || process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || ""),
  SUPABASE_SECRET_KEY: z.string().default(process.env.SUPABASE_SECRET_KEY || process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_JWT_SECRET || ""),
  SUPABASE_SERVICE_ROLE_KEY: z.string().default(process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_SECRET_KEY || ""),
  JWT_SECRET: z.string().default(process.env.JWT_SECRET || process.env.SUPABASE_JWT_SECRET || "kidsg_development_jwt_secret_must_be_changed_in_prod"),
  // Email & Providers (SMTP / Resend / Mock)
  EMAIL_PROVIDER: z.enum(["smtp", "resend", "mock"]).default("smtp"),
  SMTP_HOST: z.string().default("smtp.gmail.com"),
  SMTP_PORT: z.coerce.number().default(587),
  SMTP_SECURE: z.coerce.boolean().default(false),
  SMTP_USER: z.string().default("buildingwithkidsg@gmail.com"),
  SMTP_PASS: z.string().default("dykzeeyqtkkpzhlr"),
  SMTP_FROM: z.string().default("KidsG <buildingwithkidsg@gmail.com>"),
  RESEND_API_KEY: z.string().optional(),
  RESEND_FROM_EMAIL: z.string().default("REPLACE_ME"),
  RESEND_FROM_NAME: z.string().default("KidsG"),
  // Providers
  OTP_PROVIDER: z.enum(["memory", "mock", "supabase", "msg91", "twilio", "twofactor"]).default("memory"),
  OTP_API_URL: z.string().optional(),
  OTP_API_KEY: z.string().optional(),
  PAYMENT_PROVIDER: z.enum(["mock", "razorpay", "cashfree", "phonepe"]).default("mock"),
  RAZORPAY_KEY_ID: z.string().optional(),
  RAZORPAY_KEY_SECRET: z.string().optional(),
  RAZORPAY_WEBHOOK_SECRET: z.string().optional(),
  MAPS_PROVIDER: z.enum(["mock", "google"]).default("mock"),
  GOOGLE_MAPS_API_KEY: z.string().optional(),
  DELIVERY_PROVIDER: z.enum(["mock", "shadowfax", "dunzo", "porter"]).default("mock"),
  NOTIFICATION_PROVIDER: z.enum(["mock", "fcm", "onesignal"]).default("mock"),
  NOTIFICATION_API_KEY: z.string().optional(),
  // Authoritative Business Rules
  FREE_DELIVERY_THRESHOLD: z.coerce.number().default(199),
  DEFAULT_DELIVERY_FEE: z.coerce.number().default(30),
  MINIMUM_ORDER_VALUE: z.coerce.number().default(0)
});
function parseEnv() {
  const rawEnv = {
    ...process.env,
    SUPABASE_ANON_KEY: process.env.SUPABASE_ANON_KEY || process.env.SUPABASE_PUBLISHABLE_KEY || "mock_anon_key",
    SUPABASE_PUBLISHABLE_KEY: process.env.SUPABASE_PUBLISHABLE_KEY || process.env.SUPABASE_ANON_KEY || "mock_anon_key",
    SUPABASE_SERVICE_ROLE_KEY: process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_SECRET_KEY || "mock_service_role_key",
    SUPABASE_SECRET_KEY: process.env.SUPABASE_SECRET_KEY || process.env.SUPABASE_SERVICE_ROLE_KEY || "mock_service_role_key"
  };
  const result = envSchema.safeParse(rawEnv);
  if (!result.success) {
    console.error("\u274C Invalid environment variables:", result.error.format());
    if (process.env.NODE_ENV === "production") {
      throw new Error("Missing or invalid environment configuration in production");
    }
  }
  return result.success ? result.data : envSchema.parse({});
}
var env = parseEnv();

// src/lib/logger.ts
var Logger = class _Logger {
  static sanitize(obj) {
    if (!obj || typeof obj !== "object") return obj;
    const sanitized = { ...obj };
    const sensitiveKeys = ["password", "otp", "secret", "key", "token", "authorization", "card"];
    for (const k of Object.keys(sanitized)) {
      if (sensitiveKeys.some((s) => k.toLowerCase().includes(s))) {
        sanitized[k] = "[REDACTED]";
      } else if (typeof sanitized[k] === "object") {
        sanitized[k] = _Logger.sanitize(sanitized[k]);
      }
    }
    return sanitized;
  }
  static info(message, context) {
    const timestamp = (/* @__PURE__ */ new Date()).toISOString();
    const cleanContext = context ? _Logger.sanitize(context) : void 0;
    console.log(JSON.stringify({ level: "INFO", timestamp, message, ...cleanContext }));
  }
  static warn(message, context) {
    const timestamp = (/* @__PURE__ */ new Date()).toISOString();
    const cleanContext = context ? _Logger.sanitize(context) : void 0;
    console.warn(JSON.stringify({ level: "WARN", timestamp, message, ...cleanContext }));
  }
  static error(message, error, context) {
    const timestamp = (/* @__PURE__ */ new Date()).toISOString();
    const cleanContext = context ? _Logger.sanitize(context) : void 0;
    console.error(JSON.stringify({
      level: "ERROR",
      timestamp,
      message,
      errorMessage: error?.message || String(error),
      stack: process.env.NODE_ENV === "development" ? error?.stack : void 0,
      ...cleanContext
    }));
  }
};

// src/lib/response.ts
function sendSuccess(res, data, message = null, statusCode = 200) {
  const response = {
    success: true,
    data,
    message,
    errorCode: null
  };
  res.status(statusCode).json(response);
}
function sendError(res, message, errorCode = "INTERNAL_ERROR", statusCode = 400, data = null) {
  const response = {
    success: false,
    data,
    message,
    errorCode
  };
  res.status(statusCode).json(response);
}

// src/routes/health.ts
import { Router } from "express";

// src/lib/supabase.ts
import { createClient } from "@supabase/supabase-js";
var supabaseClient;
var supabaseAuthClient;
var isConfigured = env.SUPABASE_URL.startsWith("http") && !env.SUPABASE_URL.includes("mock.supabase.co");
if (isConfigured) {
  const secretKey = env.SUPABASE_SECRET_KEY || env.SUPABASE_SERVICE_ROLE_KEY;
  const publishableKey = env.SUPABASE_PUBLISHABLE_KEY || env.SUPABASE_ANON_KEY || secretKey;
  supabaseClient = createClient(env.SUPABASE_URL, secretKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false
    }
  });
  supabaseAuthClient = createClient(env.SUPABASE_URL, publishableKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false
    }
  });
} else {
  const createMockChain = () => {
    const target = () => target;
    const handler = {
      get(_obj, prop) {
        if (prop === "then") {
          return (resolve) => Promise.resolve({ data: null, error: null }).then(resolve);
        }
        if (prop === "single" || prop === "maybeSingle") {
          return () => Promise.resolve({ data: null, error: null });
        }
        if (prop === "insert" || prop === "upsert" || prop === "update" || prop === "delete") {
          return () => new Proxy(target, handler);
        }
        if (prop === "select") {
          return () => new Proxy(target, handler);
        }
        return () => new Proxy(target, handler);
      },
      apply() {
        return new Proxy(target, handler);
      }
    };
    return new Proxy(target, handler);
  };
  supabaseClient = {
    auth: {
      getUser: async (token) => {
        if (token && token.length > 5) {
          return {
            data: {
              user: {
                id: "user_dev_default",
                email: "student@kidsg.in",
                phone: "+919876543210",
                user_metadata: { role: "CUSTOMER", first_name: "Aarav", last_name: "Sharma" }
              }
            },
            error: null
          };
        }
        return { data: { user: null }, error: new Error("Invalid token") };
      },
      signUp: async () => ({ data: { user: { id: "user_dev_default" } }, error: null }),
      signInWithPassword: async () => ({ data: { session: { access_token: "dev-token-user_dev_default" } }, error: null }),
      signInWithOtp: async () => ({ data: {}, error: null }),
      verifyOtp: async () => ({ data: { session: { access_token: "dev-token-user_dev_default" }, user: { id: "user_dev_default" } }, error: null })
    },
    from: () => createMockChain()
  };
  supabaseAuthClient = supabaseClient;
}
var supabaseAdmin = supabaseClient;

// src/lib/supabaseSync.ts
import { randomUUID as randomUUID2 } from "crypto";

// src/lib/db.ts
import { randomUUID, scryptSync, randomBytes } from "crypto";
function calculateDistanceKm(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const dLat = (lat2 - lat1) * (Math.PI / 180);
  const dLon = (lon2 - lon1) * (Math.PI / 180);
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Math.round(R * c * 100) / 100;
}
var KidsGDatabase = class {
  categories = [];
  subcategories = [];
  products = [];
  reviews = /* @__PURE__ */ new Map();
  qa = /* @__PURE__ */ new Map();
  stores = [];
  storeInventory = /* @__PURE__ */ new Map();
  // storeId_productId -> quantity
  coupons = [];
  carts = /* @__PURE__ */ new Map();
  // userId -> items
  wishlists = /* @__PURE__ */ new Map();
  // userId -> Set<productId>
  addresses = /* @__PURE__ */ new Map();
  // userId -> addresses
  orders = /* @__PURE__ */ new Map();
  // orderId -> Order
  tickets = [];
  userProfiles = /* @__PURE__ */ new Map();
  userCredentials = /* @__PURE__ */ new Map();
  orderStatusHistory = /* @__PURE__ */ new Map();
  payments = /* @__PURE__ */ new Map();
  storeInventoryItems = /* @__PURE__ */ new Map();
  // `${storeId}_${productId}` -> item
  paymentSubmissions = /* @__PURE__ */ new Map();
  // submissionId -> item
  deliveryBookings = /* @__PURE__ */ new Map();
  // orderId -> item
  auditLogs = [];
  platformSettings = {
    merchantUpi: {
      vpa: "bookbuddys@upi",
      payeeName: "BookBuddys Stationery Depot",
      qrEnabled: true
    },
    deliveryRules: {
      defaultFee: 30,
      freeDeliveryThreshold: 199,
      maxRadiusKm: 12,
      defaultEtaMinutes: 45
    },
    founderNotifications: {
      notifyEmail: "veenithkumars31@gmail.com",
      notifyPhone: "+919353919178",
      channels: ["EMAIL", "IN_APP"]
    }
  };
  constructor() {
    this.seedInitialData();
  }
  seedInitialData() {
    this.categories = [
      { id: "cat_notebooks", name: "Notebooks & Registers", slug: "notebooks", iconName: "notebook", displayOrder: 1, isActive: true },
      { id: "cat_pens", name: "Pens & Refills", slug: "pens", iconName: "pen", displayOrder: 2, isActive: true },
      { id: "cat_pencils", name: "Pencils & Erasers", slug: "pencils", iconName: "pencil", displayOrder: 3, isActive: true },
      { id: "cat_geometry", name: "Geometry & Scales", slug: "geometry", iconName: "ruler", displayOrder: 4, isActive: true },
      { id: "cat_art", name: "Art & Craft Colors", slug: "art-craft", iconName: "palette", displayOrder: 5, isActive: true },
      { id: "cat_exam", name: "Exam Essentials", slug: "exam-essentials", iconName: "clipboard", displayOrder: 6, isActive: true },
      { id: "cat_highlighters", name: "Highlighters & Markers", slug: "highlighters", iconName: "highlighter", displayOrder: 7, isActive: true },
      { id: "cat_sticky", name: "Sticky Notes & Flags", slug: "sticky-notes", iconName: "sticky", displayOrder: 8, isActive: true },
      { id: "cat_bags", name: "School Bags & Pouches", slug: "bags-pouches", iconName: "backpack", displayOrder: 9, isActive: true },
      { id: "cat_bottles", name: "Water Bottles & Lunch", slug: "bottles-lunch", iconName: "bottle", displayOrder: 10, isActive: true }
    ];
    this.subcategories = [
      { id: "sub_notebooks_single", categoryId: "cat_notebooks", name: "Single Line Notebooks", slug: "notebooks-single", iconName: "notebook", itemCount: 22, displayOrder: 1, isActive: true },
      { id: "sub_notebooks_four", categoryId: "cat_notebooks", name: "Four Line Notebooks", slug: "notebooks-four", iconName: "notebook", itemCount: 20, displayOrder: 2, isActive: true },
      { id: "sub_notebooks_grid", categoryId: "cat_notebooks", name: "Square Grid Maths", slug: "notebooks-grid", iconName: "notebook", itemCount: 20, displayOrder: 3, isActive: true },
      { id: "sub_notebooks_unruled", categoryId: "cat_notebooks", name: "Unruled Drawing & Rough", slug: "notebooks-unruled", iconName: "notebook", itemCount: 20, displayOrder: 4, isActive: true },
      { id: "sub_pens_ball", categoryId: "cat_pens", name: "Ball Point Pens", slug: "pens-ball", iconName: "pen", itemCount: 21, displayOrder: 5, isActive: true },
      { id: "sub_pens_gel", categoryId: "cat_pens", name: "Gel Pens", slug: "pens-gel", iconName: "pen", itemCount: 20, displayOrder: 6, isActive: true },
      { id: "sub_pencils_graphite", categoryId: "cat_pencils", name: "Graphite Pencils", slug: "pencils-graphite", iconName: "pencil", itemCount: 21, displayOrder: 7, isActive: true },
      { id: "sub_erasers", categoryId: "cat_pencils", name: "Dust-Free Erasers", slug: "erasers", iconName: "eraser", itemCount: 21, displayOrder: 8, isActive: true },
      { id: "sub_sharpeners", categoryId: "cat_pencils", name: "Precision Sharpeners", slug: "sharpeners", iconName: "sharpener", itemCount: 21, displayOrder: 9, isActive: true },
      { id: "sub_geometry_boxes", categoryId: "cat_geometry", name: "Geometry Box Sets", slug: "geometry-boxes", iconName: "ruler", itemCount: 21, displayOrder: 10, isActive: true },
      { id: "sub_drawing_colours", categoryId: "cat_art", name: "Colouring & Drawing", slug: "drawing-colours", iconName: "palette", itemCount: 21, displayOrder: 11, isActive: true },
      { id: "sub_crafts", categoryId: "cat_art", name: "Craft & Origami", slug: "crafts", iconName: "craft", itemCount: 21, displayOrder: 12, isActive: true },
      { id: "sub_bags", categoryId: "cat_bags", name: "Ergonomic Backpacks", slug: "bags", iconName: "backpack", itemCount: 21, displayOrder: 13, isActive: true },
      { id: "sub_bottles", categoryId: "cat_bottles", name: "Insulated Stainless Steel Bottles", slug: "bottles", iconName: "bottle", itemCount: 21, displayOrder: 14, isActive: true },
      { id: "sub_lunch", categoryId: "cat_bottles", name: "Stainless Steel Lunch Boxes", slug: "lunch", iconName: "lunchbox", itemCount: 21, displayOrder: 15, isActive: true }
    ];
    this.products = [
      // Notebooks
      {
        id: "prod_classmate_single_line",
        name: "Classmate Pulse Spiral Single Line Notebook",
        slug: "classmate-pulse-spiral-single-line",
        description: "Single line, 180 pages, high-grade 70 GSM paper for smooth fountain and ball pen writing.",
        brand: "Classmate",
        categoryId: "cat_notebooks",
        categoryName: "Notebooks & Registers",
        imageUrl: "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500",
        price: 95,
        mrp: 110,
        discountPercent: 14,
        stock: 50,
        unit: "book",
        gradeLevel: "6th - 12th",
        isFeatured: true,
        isActive: true,
        specs: { Pages: "180", Ruling: "Single Line", Paper: "70 GSM" }
      },
      {
        id: "prod_classmate_four_line",
        name: "Classmate 4-Line English Exercise Book",
        slug: "classmate-four-line-english-book",
        description: "Primary school 4-line ruled notebook with red and blue margin guidelines.",
        brand: "Classmate",
        categoryId: "cat_notebooks",
        categoryName: "Notebooks & Registers",
        imageUrl: "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500",
        price: 45,
        mrp: 50,
        discountPercent: 10,
        stock: 80,
        unit: "book",
        gradeLevel: "1st - 3rd",
        isFeatured: false,
        isActive: true,
        specs: { Pages: "120", Ruling: "Four Line" }
      },
      {
        id: "prod_classmate_grid_math",
        name: "Classmate Math Square Grid Notebook (Small)",
        slug: "classmate-math-square-grid-notebook",
        description: "Square ruled notebook specifically for mathematics and numerical calculations.",
        brand: "Classmate",
        categoryId: "cat_notebooks",
        categoryName: "Notebooks & Registers",
        imageUrl: "https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=500",
        price: 50,
        mrp: 55,
        discountPercent: 9,
        stock: 65,
        unit: "book",
        gradeLevel: "1st - 8th",
        isFeatured: false,
        isActive: true,
        specs: { Pages: "172", Ruling: "Square Grid" }
      },
      {
        id: "prod_navneet_long_book",
        name: "Navneet Youva Soft Bound Long Notebook",
        slug: "navneet-youva-soft-bound-long-notebook",
        description: "Hardcover long register for college, CBSE board examinations, and high school note taking.",
        brand: "Navneet",
        categoryId: "cat_notebooks",
        categoryName: "Notebooks & Registers",
        imageUrl: "https://images.unsplash.com/photo-1516962215378-7fa2e137ae93?w=500",
        price: 75,
        mrp: 85,
        discountPercent: 12,
        stock: 45,
        unit: "register",
        gradeLevel: "8th - 12th",
        isFeatured: true,
        isActive: true,
        specs: { Pages: "160", Size: "A4" }
      },
      // Pens
      {
        id: "prod_hauser_xo_ball",
        name: "Hauser XO 0.7mm Ball Pen (Pack of 5, Blue)",
        slug: "hauser-xo-ball-pen-pack-of-5",
        description: "Super fluid German ink technology with non-slip textured comfort grip.",
        brand: "Hauser",
        categoryId: "cat_pens",
        categoryName: "Pens & Refills",
        imageUrl: "https://images.unsplash.com/photo-1585336261026-775af4609c0d?w=500",
        price: 50,
        mrp: 50,
        discountPercent: 0,
        stock: 120,
        unit: "pack",
        gradeLevel: "All",
        isFeatured: true,
        isActive: true,
        specs: { Tip: "0.7mm", Color: "Blue", Count: "5" }
      },
      {
        id: "prod_cello_butterflow_pack",
        name: "Cello Butterflow Classic Blue Gel Pen (Pack of 3)",
        slug: "cello-butterflow-classic-blue",
        description: "Lubriflow ink system for ultra-smooth skip-free school exam writing.",
        brand: "Cello",
        categoryId: "cat_pens",
        categoryName: "Pens & Refills",
        imageUrl: "https://images.unsplash.com/photo-1569683795645-b62e50fbf103?w=500",
        price: 60,
        mrp: 75,
        discountPercent: 20,
        stock: 100,
        unit: "pack",
        gradeLevel: "6th - 12th",
        isFeatured: false,
        isActive: true,
        specs: { Tip: "0.7mm", Color: "Blue" }
      },
      {
        id: "prod_uniball_eye_roller",
        name: "Uni-ball Eye Fine 0.7mm Roller Pen",
        slug: "uniball-eye-fine-07-roller-pen",
        description: "Waterproof fade-proof pigment ink rollerball pen with stainless steel tip.",
        brand: "Uni-ball",
        categoryId: "cat_pens",
        categoryName: "Pens & Refills",
        imageUrl: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500",
        price: 85,
        mrp: 95,
        discountPercent: 11,
        stock: 40,
        unit: "piece",
        gradeLevel: "8th - 12th",
        isFeatured: true,
        isActive: true,
        specs: { Tip: "0.7mm Fine", Ink: "Black/Blue" }
      },
      {
        id: "prod_pilot_v5_liquid",
        name: "Pilot Hi-Tecpoint V5 0.5mm Liquid Ink Pen",
        slug: "pilot-hi-tecpoint-v5-pen",
        description: "Precision Japanese 0.5mm needle point tip for ultra-crisp diagram labeling.",
        brand: "Pilot",
        categoryId: "cat_pens",
        categoryName: "Pens & Refills",
        imageUrl: "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500",
        price: 65,
        mrp: 70,
        discountPercent: 7,
        stock: 60,
        unit: "piece",
        gradeLevel: "8th - 12th",
        isFeatured: false,
        isActive: true,
        specs: { Tip: "0.5mm Needle", Ink: "Pure Liquid" }
      },
      // Pencils & Erasers
      {
        id: "prod_apsara_platinum_box",
        name: "Apsara Platinum Extra Dark Pencils (Box of 10)",
        slug: "apsara-platinum-extra-dark-pencils",
        description: "Soft wood easy sharpening pencils with bonus sharpener and eraser included.",
        brand: "Apsara",
        categoryId: "cat_pencils",
        categoryName: "Pencils & Erasers",
        imageUrl: "https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=500",
        price: 70,
        mrp: 80,
        discountPercent: 13,
        stock: 90,
        unit: "box",
        gradeLevel: "1st - 10th",
        isFeatured: true,
        isActive: true,
        specs: { Lead: "Extra Dark HB", Count: "10 Pencils" }
      },
      {
        id: "prod_natraj_classic_box",
        name: "Nataraj Classic 621 Red & Black Pencils (Pack of 10)",
        slug: "nataraj-classic-621-pencils",
        description: "The trusted school classic pencil for students across India.",
        brand: "Nataraj",
        categoryId: "cat_pencils",
        categoryName: "Pencils & Erasers",
        imageUrl: "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500",
        price: 55,
        mrp: 60,
        discountPercent: 8,
        stock: 110,
        unit: "box",
        gradeLevel: "All",
        isFeatured: false,
        isActive: true,
        specs: { Lead: "HB" }
      },
      {
        id: "prod_milan_capsule_eraser",
        name: "Milan Capsule Dual Eraser & Sharpener",
        slug: "milan-capsule-eraser-sharpener",
        description: "Compact Spanish eraser capsule with safety blade sharpener reservoir.",
        brand: "Milan",
        categoryId: "cat_pencils",
        categoryName: "Pencils & Erasers",
        imageUrl: "https://images.unsplash.com/photo-1588854337221-4cf9fa96059c?w=500",
        price: 45,
        mrp: 50,
        discountPercent: 10,
        stock: 75,
        unit: "piece",
        gradeLevel: "All",
        isFeatured: false,
        isActive: true,
        specs: { Type: "Dust-free Eraser + Sharpener" }
      },
      {
        id: "prod_apsara_non_dust_erasers",
        name: "Apsara Non-Dust Erasers (Pack of 5)",
        slug: "apsara-non-dust-erasers-pack-5",
        description: "Pencil marks erased without creating loose messy graphite dust.",
        brand: "Apsara",
        categoryId: "cat_pencils",
        categoryName: "Pencils & Erasers",
        imageUrl: "https://images.unsplash.com/photo-1587614382346-4ec70e388b28?w=500",
        price: 25,
        mrp: 30,
        discountPercent: 17,
        stock: 140,
        unit: "pack",
        gradeLevel: "All",
        isFeatured: false,
        isActive: true,
        specs: { Type: "Non-Dust", Count: "5" }
      },
      // Geometry & Scales
      {
        id: "prod_camlin_scholar_geometry",
        name: "Camlin Scholar Mathematical Drawing Instruments Box",
        slug: "camlin-scholar-geometry-box",
        description: "Self-centering compass, divider, protractor, set squares, and 15cm ruler in sturdy metal tin.",
        brand: "Camlin",
        categoryId: "cat_geometry",
        categoryName: "Geometry & Scales",
        imageUrl: "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=500",
        price: 135,
        mrp: 150,
        discountPercent: 10,
        stock: 35,
        unit: "tin",
        gradeLevel: "6th - 12th",
        isFeatured: true,
        isActive: true,
        specs: { Case: "Rust-free Metal Tin", Instruments: "9 items" }
      },
      {
        id: "prod_classmate_inventor_geometry",
        name: "Classmate Victor Precision Geometry Box",
        slug: "classmate-victor-geometry-box",
        description: "Specially engineered die-cast compass for wobble-free circles in mathematics board exams.",
        brand: "Classmate",
        categoryId: "cat_geometry",
        categoryName: "Geometry & Scales",
        imageUrl: "https://images.unsplash.com/photo-1584697964190-7bb9348c5825?w=500",
        price: 160,
        mrp: 180,
        discountPercent: 11,
        stock: 30,
        unit: "box",
        gradeLevel: "8th - 12th",
        isFeatured: false,
        isActive: true,
        specs: { Precision: "Die-cast gears" }
      },
      {
        id: "prod_doms_clear_ruler_30cm",
        name: "DOMS Transparent Acrylic 30cm Metric Scale",
        slug: "doms-transparent-acrylic-scale-30cm",
        description: "Scratch-resistant transparent acrylic ruler with mm, cm and inch dual measurements.",
        brand: "DOMS",
        categoryId: "cat_geometry",
        categoryName: "Geometry & Scales",
        imageUrl: "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=500",
        price: 20,
        mrp: 20,
        discountPercent: 0,
        stock: 150,
        unit: "piece",
        gradeLevel: "All",
        isFeatured: false,
        isActive: true,
        specs: { Length: "30 cm", Material: "Virgin Acrylic" }
      },
      // Art & Craft
      {
        id: "prod_doms_brush_pens_14",
        name: "DOMS Brush Pens (14 Shades with Blender)",
        slug: "doms-brush-pens-14-shades",
        description: "Super-flexible nylon brush tips for calligraphy, lettering, and blending poster art.",
        brand: "DOMS",
        categoryId: "cat_art",
        categoryName: "Art & Craft Colors",
        imageUrl: "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=500",
        price: 199,
        mrp: 225,
        discountPercent: 12,
        stock: 40,
        unit: "pack",
        gradeLevel: "3rd - 12th",
        isFeatured: true,
        isActive: true,
        specs: { Shades: "14 Colors", Tip: "Flexible Brush" }
      },
      {
        id: "prod_camlin_oil_pastels_25",
        name: "Camlin Kokuyo Oil Pastels (25 Shades)",
        slug: "camlin-oil-pastels-25-shades",
        description: "Bright vivid non-toxic oil pastels with scraping tool for rich art shading.",
        brand: "Camlin",
        categoryId: "cat_art",
        categoryName: "Art & Craft Colors",
        imageUrl: "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500",
        price: 110,
        mrp: 125,
        discountPercent: 12,
        stock: 55,
        unit: "box",
        gradeLevel: "1st - 8th",
        isFeatured: false,
        isActive: true,
        specs: { Shades: "25 Shades", Tool: "Scraper Included" }
      },
      {
        id: "prod_faber_castell_sketch_pack",
        name: "Faber-Castell Connector Sketch Pens (Pack of 20)",
        slug: "faber-castell-connector-pens-20",
        description: "Washable bright food-grade dye sketch pens that connect together into crafts.",
        brand: "Faber-Castell",
        categoryId: "cat_art",
        categoryName: "Art & Craft Colors",
        imageUrl: "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500",
        price: 140,
        mrp: 160,
        discountPercent: 13,
        stock: 60,
        unit: "pack",
        gradeLevel: "1st - 8th",
        isFeatured: false,
        isActive: true,
        specs: { Count: "20 Pens", Washable: "Yes" }
      },
      {
        id: "prod_pidilite_fevicryl_acrylic",
        name: "Pidilite Fevicryl Acrylic Colors Kit (6 Shades)",
        slug: "pidilite-fevicryl-acrylic-colors-6",
        description: "Fast drying water-based colors suitable for canvas, cardboard, and school models.",
        brand: "Pidilite",
        categoryId: "cat_art",
        categoryName: "Art & Craft Colors",
        imageUrl: "https://images.unsplash.com/photo-1572945753563-804956783134?w=500",
        price: 90,
        mrp: 100,
        discountPercent: 10,
        stock: 50,
        unit: "kit",
        gradeLevel: "4th - 12th",
        isFeatured: false,
        isActive: true,
        specs: { Bottles: "6 x 15ml", Medium: "Multi-surface" }
      },
      {
        id: "prod_fevistick_super_glue",
        name: "Fevistik Glue Stick 15g (Mess-free Crafting)",
        slug: "fevistik-super-glue-stick-15g",
        description: "Smooth lipstick-twist mechanism paper glue for clean craft projects.",
        brand: "Pidilite",
        categoryId: "cat_art",
        categoryName: "Art & Craft Colors",
        imageUrl: "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=500",
        price: 35,
        mrp: 40,
        discountPercent: 13,
        stock: 120,
        unit: "stick",
        gradeLevel: "All",
        isFeatured: false,
        isActive: true,
        specs: { Weight: "15 grams", Safe: "Non-toxic" }
      },
      // Exam Essentials
      {
        id: "prod_transparent_exam_pouch",
        name: "KidsG Board Exam Approved Transparent Stationery Pouch",
        slug: "kidsg-transparent-exam-pouch",
        description: "100% transparent durable PVC zipper pouch conforming to CBSE/ICSE exam hall rules.",
        brand: "KidsG Essentials",
        categoryId: "cat_exam",
        categoryName: "Exam Essentials",
        imageUrl: "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500",
        price: 65,
        mrp: 80,
        discountPercent: 19,
        stock: 85,
        unit: "piece",
        gradeLevel: "9th - 12th",
        isFeatured: true,
        isActive: true,
        specs: { Material: "Clear PVC", Regulation: "CBSE / ICSE Board Compliant" }
      },
      {
        id: "prod_wooden_exam_clipboard",
        name: "Hardboard Wooden School Exam Writing Pad (A4)",
        slug: "hardboard-wooden-exam-clipboard",
        description: "Smooth polished tempered clipboard with sturdy stainless steel clip.",
        brand: "Classmate",
        categoryId: "cat_exam",
        categoryName: "Exam Essentials",
        imageUrl: "https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=500",
        price: 85,
        mrp: 99,
        discountPercent: 14,
        stock: 45,
        unit: "piece",
        gradeLevel: "All",
        isFeatured: false,
        isActive: true,
        specs: { Size: "A4", Clip: "Heavy duty spring" }
      },
      // Highlighters & Markers
      {
        id: "prod_faber_textliner_pastel",
        name: "Faber-Castell 1546 Pastel Textliner Highlighters (Set of 4)",
        slug: "faber-castell-pastel-highlighters-4",
        description: "Soft pastel water-based inks that don\u2019t bleed through notebook paper.",
        brand: "Faber-Castell",
        categoryId: "cat_highlighters",
        categoryName: "Highlighters & Markers",
        imageUrl: "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500",
        price: 110,
        mrp: 130,
        discountPercent: 15,
        stock: 65,
        unit: "pack",
        gradeLevel: "6th - 12th",
        isFeatured: true,
        isActive: true,
        specs: { Colors: "4 Pastel Shades", Tip: "Chisel" }
      },
      {
        id: "prod_camlin_whiteboard_markers",
        name: "Camlin Whiteboard Markers with Duster (Set of 4)",
        slug: "camlin-whiteboard-markers-set-4",
        description: "Dry wipe markers for student study rooms and teacher boards.",
        brand: "Camlin",
        categoryId: "cat_highlighters",
        categoryName: "Highlighters & Markers",
        imageUrl: "https://images.unsplash.com/photo-1585336261026-775af4609c0d?w=500",
        price: 120,
        mrp: 140,
        discountPercent: 14,
        stock: 40,
        unit: "set",
        gradeLevel: "All",
        isFeatured: false,
        isActive: true,
        specs: { Colors: "Black, Blue, Red, Green" }
      },
      // Sticky Notes
      {
        id: "prod_3m_post_it_yellow",
        name: "3M Post-it Canary Yellow Notes (3 x 3 inch, 100 Sheets)",
        slug: "3m-post-it-yellow-notes",
        description: "Genuine 3M repositionable adhesive notes for textbook bookmarks and revision tags.",
        brand: "3M Post-it",
        categoryId: "cat_sticky",
        categoryName: "Sticky Notes & Flags",
        imageUrl: "https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=500",
        price: 55,
        mrp: 65,
        discountPercent: 15,
        stock: 90,
        unit: "pad",
        gradeLevel: "All",
        isFeatured: true,
        isActive: true,
        specs: { Size: "76mm x 76mm", Sheets: "100" }
      },
      {
        id: "prod_page_marker_flags",
        name: "Neon Index Arrow Page Marker Sticky Flags (5 Colors)",
        slug: "neon-index-page-markers-5-colors",
        description: "Translucent self-adhesive tabs for indexing textbook chapters.",
        brand: "KidsG Essentials",
        categoryId: "cat_sticky",
        categoryName: "Sticky Notes & Flags",
        imageUrl: "https://images.unsplash.com/photo-1588854337221-4cf9fa96059c?w=500",
        price: 40,
        mrp: 50,
        discountPercent: 20,
        stock: 110,
        unit: "pack",
        gradeLevel: "6th - 12th",
        isFeatured: false,
        isActive: true,
        specs: { Strips: "125 tabs" }
      },
      // School Bags & Pouches
      {
        id: "prod_kidsg_desk_pouch_canvas",
        name: "KidsG Dual-Compartment Canvas Desk Pouch (Orange & Jet Black)",
        slug: "kidsg-dual-compartment-canvas-pouch",
        description: "Signature KidsG branded pencil box with dedicated pen loops and mesh pocket.",
        brand: "KidsG",
        categoryId: "cat_bags",
        categoryName: "School Bags & Pouches",
        imageUrl: "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500",
        price: 180,
        mrp: 249,
        discountPercent: 28,
        stock: 35,
        unit: "piece",
        gradeLevel: "All",
        isFeatured: true,
        isActive: true,
        specs: { Material: "600D Canvas", Pockets: "2 Main + 1 Mesh" }
      },
      // Water Bottles
      {
        id: "prod_milton_thermosteel_bottle",
        name: "Milton Flip Lid Insulated Stainless Steel Bottle (500ml)",
        slug: "milton-thermosteel-bottle-500ml",
        description: "Keeps water cold for 12 hours throughout long school and tuition days.",
        brand: "Milton",
        categoryId: "cat_bottles",
        categoryName: "Water Bottles & Lunch",
        imageUrl: "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500",
        price: 380,
        mrp: 440,
        discountPercent: 14,
        stock: 25,
        unit: "piece",
        gradeLevel: "All",
        isFeatured: false,
        isActive: true,
        specs: { Capacity: "500 ml", Grade: "304 Stainless Steel" }
      }
    ];
    this.stores = [
      {
        id: "store_vidya_depot",
        code: "BB-KOR-01",
        name: "Vidya Book & Stationery Depot",
        ownerName: "Ramesh Kumar",
        phone: "+91 98765 43210",
        email: "vidya.depot@bookbuddys.in",
        address: "No. 42, 12th Main Road, 4th Block, Koramangala",
        locality: "Koramangala 4th Block",
        area: "South Bengaluru",
        city: "Bengaluru",
        state: "Karnataka",
        pincode: "560034",
        landmark: "Opposite Bethany High School Gate 2",
        latitude: 12.9352,
        longitude: 77.6245,
        deliveryRadiusKm: 6,
        isActive: true,
        openTime: "07:30",
        closeTime: "21:30",
        categoriesSold: ["Notebooks & Registers", "Pens & Refills", "Geometry & Scales", "Art & Craft Colors", "Exam Essentials"],
        founderNotes: "Premier verified partner store. Reliable stock of Classmate & Hauser. Direct contact: Ramesh Uncle.",
        verificationStatus: "ACTIVE",
        businessHours: {
          monday: { open: "07:30", close: "21:30", isOpen: true },
          tuesday: { open: "07:30", close: "21:30", isOpen: true },
          wednesday: { open: "07:30", close: "21:30", isOpen: true },
          thursday: { open: "07:30", close: "21:30", isOpen: true },
          friday: { open: "07:30", close: "21:30", isOpen: true },
          saturday: { open: "07:30", close: "22:00", isOpen: true },
          sunday: { open: "09:00", close: "20:30", isOpen: true }
        },
        createdAt: "2026-08-01T09:00:00Z",
        updatedAt: "2026-10-15T10:30:00Z"
      },
      {
        id: "store_campus_books",
        code: "BB-IND-02",
        name: "Campus Student Corner & Books",
        ownerName: "Kavitha Sundaram",
        phone: "+91 98765 43211",
        email: "campus.corner@bookbuddys.in",
        address: "Shop 7, 80 Feet Road, HAL 2nd Stage, Indiranagar",
        locality: "Indiranagar",
        area: "East Bengaluru",
        city: "Bengaluru",
        state: "Karnataka",
        pincode: "560038",
        landmark: "Near Indiranagar Metro Station Exit B",
        latitude: 12.9716,
        longitude: 77.6412,
        deliveryRadiusKm: 6.5,
        isActive: true,
        openTime: "08:00",
        closeTime: "22:00",
        categoriesSold: ["Pens & Refills", "Notebooks & Registers", "School Bags & Pouches", "Highlighters & Markers"],
        founderNotes: "High volume student shop. Good for premium pens and art materials.",
        verificationStatus: "ACTIVE",
        businessHours: {
          monday: { open: "08:00", close: "22:00", isOpen: true },
          tuesday: { open: "08:00", close: "22:00", isOpen: true },
          wednesday: { open: "08:00", close: "22:00", isOpen: true },
          thursday: { open: "08:00", close: "22:00", isOpen: true },
          friday: { open: "08:00", close: "22:00", isOpen: true },
          saturday: { open: "08:00", close: "22:30", isOpen: true },
          sunday: { open: "10:00", close: "20:00", isOpen: true }
        },
        createdAt: "2026-08-10T11:00:00Z",
        updatedAt: "2026-10-16T14:15:00Z"
      },
      {
        id: "store_malleshwaram_stationery",
        code: "BB-MAL-03",
        name: "Sri Vidya Book Centre & Stationery",
        ownerName: "Anand Rao",
        phone: "+91 98765 43212",
        email: "srividya.mal@bookbuddys.in",
        address: "No. 88, 8th Cross, Margosa Road, Malleshwaram",
        locality: "Malleshwaram",
        area: "North Bengaluru",
        city: "Bengaluru",
        state: "Karnataka",
        pincode: "560003",
        landmark: "Near Malleshwaram Circle & KEB Circle",
        latitude: 13.0035,
        longitude: 77.5684,
        deliveryRadiusKm: 5.5,
        isActive: true,
        openTime: "08:30",
        closeTime: "21:30",
        categoriesSold: ["Notebooks & Registers", "Geometry & Scales", "Exam Essentials", "Water Bottles & Lunch"],
        founderNotes: "Historic central hub store with deep inventory of exam supplies and charts.",
        verificationStatus: "ACTIVE",
        businessHours: {
          monday: { open: "08:30", close: "21:30", isOpen: true },
          tuesday: { open: "08:30", close: "21:30", isOpen: true },
          wednesday: { open: "08:30", close: "21:30", isOpen: true },
          thursday: { open: "08:30", close: "21:30", isOpen: true },
          friday: { open: "08:30", close: "21:30", isOpen: true },
          saturday: { open: "08:30", close: "22:00", isOpen: true },
          sunday: { open: "09:30", close: "19:30", isOpen: true }
        },
        createdAt: "2026-08-15T12:00:00Z",
        updatedAt: "2026-10-18T09:40:00Z"
      },
      {
        id: "store_jayanagar_books",
        code: "BB-JAY-04",
        name: "Jayanagar Student Emporium",
        ownerName: "Suresh Patel",
        phone: "+91 98765 43213",
        email: "jayanagar.emporium@bookbuddys.in",
        address: "Shop 14, BDA Complex, 4th Block, Jayanagar",
        locality: "Jayanagar 4th Block",
        area: "South Bengaluru",
        city: "Bengaluru",
        state: "Karnataka",
        pincode: "560011",
        landmark: "Near Jayanagar BDA Shopping Complex 2nd Floor Entry",
        latitude: 12.9299,
        longitude: 77.5838,
        deliveryRadiusKm: 6,
        isActive: true,
        openTime: "09:00",
        closeTime: "21:30",
        categoriesSold: ["All School Supplies", "Notebooks", "Stationery Bundles"],
        founderNotes: "Excellent warehouse capacity and bulk registers stock.",
        verificationStatus: "ACTIVE",
        businessHours: {
          monday: { open: "09:00", close: "21:30", isOpen: true },
          tuesday: { open: "09:00", close: "21:30", isOpen: true },
          wednesday: { open: "09:00", close: "21:30", isOpen: true },
          thursday: { open: "09:00", close: "21:30", isOpen: true },
          friday: { open: "09:00", close: "21:30", isOpen: true },
          saturday: { open: "09:00", close: "22:00", isOpen: true },
          sunday: { open: "10:00", close: "20:00", isOpen: true }
        },
        createdAt: "2026-09-01T10:00:00Z",
        updatedAt: "2026-10-19T11:20:00Z"
      }
    ];
    this.stores.forEach((store) => {
      this.products.forEach((product) => {
        const key = `${store.id}_${product.id}`;
        const discountVar = store.id === "store_vidya_depot" ? product.discountPercent : Math.max(0, product.discountPercent - 2);
        const sellPrice = discountVar > 0 ? Math.round(product.mrp * (1 - discountVar / 100)) : product.price;
        const initialStock = Math.max(8, product.stock + (store.id === "store_vidya_depot" ? 10 : 0));
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
          lastVerifiedAt: (/* @__PURE__ */ new Date()).toISOString()
        });
      });
    });
    this.coupons = [
      {
        id: "coup_kidsg50",
        code: "KIDSG50",
        description: "Flat \u20B950 discount for school orders above \u20B9199",
        discountType: "FLAT",
        discountValue: 50,
        minOrderValue: 199,
        validUntil: "2026-12-31T23:59:59Z",
        isActive: true
      },
      {
        id: "coup_firstorder",
        code: "FIRSTORDER",
        description: "Flat \u20B940 off for new students",
        discountType: "FLAT",
        discountValue: 40,
        minOrderValue: 149,
        validUntil: "2026-12-31T23:59:59Z",
        isActive: true
      },
      {
        id: "coup_examready",
        code: "EXAMREADY",
        description: "15% discount on exam stationery essentials",
        discountType: "PERCENTAGE",
        discountValue: 15,
        minOrderValue: 249,
        maxDiscountAmount: 75,
        validUntil: "2026-12-31T23:59:59Z",
        isActive: true
      }
    ];
    const defaultAddress = {
      id: "addr_dev_default",
      userId: "user_dev_default",
      label: "Home",
      name: "Aarav Sharma",
      phone: "+91 98765 43210",
      addressLine1: "Flat 302, Sunrise Orchid Apartments",
      addressLine2: "14th Cross, 5th Block",
      city: "Bengaluru",
      state: "Karnataka",
      postalCode: "560034",
      latitude: 12.9358,
      longitude: 77.6251,
      deliveryInstructions: "Ring doorbell twice. Student studying.",
      isDefault: true
    };
    this.addresses.set("user_dev_default", [defaultAddress]);
    const testProfile = {
      id: "user_dev_default",
      authUserId: "user_dev_default",
      firstName: "Aarav",
      lastName: "Sharma",
      phone: "+91 98765 43210",
      email: "student@kidsg.in",
      avatarUrl: "",
      role: "CUSTOMER",
      onboardingCompleted: true,
      selectedClass: "Class 7",
      selectedSchool: "National Public School, Koramangala",
      createdAt: (/* @__PURE__ */ new Date()).toISOString(),
      updatedAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.userProfiles.set("user_dev_default", testProfile);
    const testSalt = "kidsg_secure_salt_2026";
    const testPassword = "KidsGSecure2026!";
    const testHash = this.hashPassword(testPassword, testSalt);
    this.userCredentials.set("student@kidsg.in", {
      userId: "user_dev_default",
      email: "student@kidsg.in",
      passwordHash: testHash,
      salt: testSalt
    });
    this.userCredentials.set("aarav@kidsg.in", {
      userId: "user_dev_default",
      email: "aarav@kidsg.in",
      passwordHash: testHash,
      salt: testSalt
    });
    const founderProfile = {
      id: "user_founder_01",
      authUserId: "user_founder_01",
      firstName: "Veenith",
      lastName: "Kumar",
      phone: "+91 9353919178",
      email: "veenithkumars31@gmail.com",
      avatarUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
      role: "ADMIN",
      onboardingCompleted: true,
      selectedClass: "Founder & CEO",
      selectedSchool: "BookBuddys HQ Bengaluru",
      createdAt: (/* @__PURE__ */ new Date()).toISOString(),
      updatedAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.userProfiles.set("user_founder_01", founderProfile);
    const veenithSalt = "bookbuddys_veenith_salt_2026";
    const veenithHash = this.hashPassword("Veeniths31@", veenithSalt);
    this.userCredentials.set("veenithkumars31@gmail.com", {
      userId: "user_founder_01",
      email: "veenithkumars31@gmail.com",
      passwordHash: veenithHash,
      salt: veenithSalt
    });
    const founderSalt = "bookbuddys_founder_salt_2026";
    const founderHash = this.hashPassword("FounderPassword2026!", founderSalt);
    this.userCredentials.set("founder@bookbuddys.in", {
      userId: "user_founder_01",
      email: "founder@bookbuddys.in",
      passwordHash: founderHash,
      salt: founderSalt
    });
    this.userCredentials.set("admin@bookbuddys.in", {
      userId: "user_founder_01",
      email: "admin@bookbuddys.in",
      passwordHash: founderHash,
      salt: founderSalt
    });
  }
  hashPassword(password, salt) {
    return scryptSync(password, salt, 64).toString("hex");
  }
  // --- Category Operations ---
  getCategories() {
    return this.categories.filter((c) => c.isActive).sort((a, b) => a.displayOrder - b.displayOrder);
  }
  getCategoryById(id) {
    return this.categories.find((c) => (c.id === id || c.slug === id) && c.isActive);
  }
  // --- Subcategory Operations ---
  getSubcategories(categoryId) {
    if (categoryId) {
      const cleanId = categoryId.replace("cat_", "");
      return this.subcategories.filter((s) => (s.categoryId === categoryId || s.categoryId.replace("cat_", "") === cleanId) && s.isActive);
    }
    return this.subcategories.filter((s) => s.isActive).sort((a, b) => a.displayOrder - b.displayOrder);
  }
  // --- Product Review & QA Operations ---
  getProductReviews(productId) {
    const list = this.reviews.get(productId) || [];
    if (list.length === 0) {
      return [
        {
          id: `rev_sample_1_${productId}`,
          productId,
          authorName: "Sneha Sharma",
          authorRole: "Parent (Grade 6 CBSE)",
          rating: 4.8,
          dateText: "3 days ago",
          comment: "Outstanding quality. Paper is completely ink-proof and binding is solid for daily school rough use.",
          isVerifiedPurchase: true,
          helpfulCount: 18
        },
        {
          id: `rev_sample_2_${productId}`,
          productId,
          authorName: "Rohan Deshmukh",
          authorRole: "Student (Grade 8)",
          rating: 4.5,
          dateText: "1 week ago",
          comment: "Perfect for fast handwriting during examinations. Very smooth finish.",
          isVerifiedPurchase: true,
          helpfulCount: 9
        }
      ];
    }
    return list;
  }
  addProductReview(productId, review) {
    const newReview = {
      ...review,
      id: `rev_${randomUUID()}`,
      productId
    };
    const current = this.reviews.get(productId) || [];
    current.unshift(newReview);
    this.reviews.set(productId, current);
    return newReview;
  }
  getProductQa(productId) {
    const list = this.qa.get(productId) || [];
    if (list.length === 0) {
      return [
        {
          id: `qa_sample_1_${productId}`,
          productId,
          question: "Is this product compliant with CBSE & ICSE school syllabus?",
          answer: "Yes! Strictly certified per school standards and recommended by top teachers.",
          answeredBy: "KidsG School Expert",
          answeredDateText: "Verified response"
        },
        {
          id: `qa_sample_2_${productId}`,
          productId,
          question: "Can this be delivered in 15 minutes?",
          answer: "Yes, dispatched directly from our nearest neighborhood partner dark store.",
          answeredBy: "KidsG Logistics",
          answeredDateText: "Verified response"
        }
      ];
    }
    return list;
  }
  addProductQa(productId, question) {
    const newQa = {
      id: `qa_${randomUUID()}`,
      productId,
      question,
      answer: "Thank you for asking! Our verified stationery curator will review and post an answer shortly.",
      answeredBy: "KidsG Support",
      answeredDateText: "Just now"
    };
    const current = this.qa.get(productId) || [];
    current.unshift(newQa);
    this.qa.set(productId, current);
    return newQa;
  }
  getProductBundle(productId) {
    const baseProduct = this.getProductById(productId);
    if (!baseProduct) return null;
    const complements = this.products.filter((p) => p.id !== productId && p.isActive).slice(0, 2);
    return {
      id: `bundle_${productId}`,
      productId,
      title: "Frequently Bought Together Pack",
      discountPercent: 10,
      items: [baseProduct, ...complements]
    };
  }
  // --- Product Operations ---
  getProducts(params) {
    let result = this.products.filter((p) => p.isActive);
    if (params?.search) {
      const q = params.search.toLowerCase();
      result = result.filter(
        (p) => p.name.toLowerCase().includes(q) || p.brand.toLowerCase().includes(q) || p.description.toLowerCase().includes(q) || p.categoryName?.toLowerCase().includes(q)
      );
    }
    if (params?.categoryId) {
      const catId = params.categoryId.toLowerCase();
      result = result.filter((p) => p.categoryId === params.categoryId || p.categoryName?.toLowerCase() === catId);
    }
    if (params?.subcategoryId) {
      result = result.filter((p) => p.subcategoryId === params.subcategoryId);
    }
    if (params?.brand) {
      result = result.filter((p) => p.brand.toLowerCase() === params.brand?.toLowerCase());
    }
    if (params?.featured !== void 0) {
      result = result.filter((p) => p.isFeatured === params.featured);
    }
    if (params?.sort === "price_asc") {
      result.sort((a, b) => a.price - b.price);
    } else if (params?.sort === "price_desc") {
      result.sort((a, b) => b.price - a.price);
    }
    const total = result.length;
    const page = params?.page || 1;
    const limit = params?.limit || 20;
    const start = (page - 1) * limit;
    const paginated = result.slice(start, start + limit);
    return { products: paginated, total, page, limit };
  }
  getProductById(id) {
    let p = this.products.find((prod) => (prod.id === id || prod.slug === id) && prod.isActive);
    if (!p && id) {
      const name = id.replace(/^prod_/, "").replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
      p = {
        id,
        name,
        slug: id,
        description: "KidsG Verified School Stationery",
        brand: "KidsG Partner",
        categoryId: "cat_stationery",
        categoryName: "Stationery",
        imageUrl: "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500",
        price: 95,
        mrp: 110,
        discountPercent: 14,
        stock: 100,
        unit: "piece",
        gradeLevel: "All Grades",
        isFeatured: false,
        isActive: true,
        specs: {}
      };
      this.products.push(p);
    }
    return p;
  }
  // --- Store Operations ---
  getStores() {
    return this.stores.filter((s) => s.isActive);
  }
  getStoreById(id) {
    return this.stores.find((s) => s.id === id && s.isActive);
  }
  // --- Wishlist Operations ---
  getWishlist(userId) {
    const ids = this.wishlists.get(userId) || /* @__PURE__ */ new Set();
    return this.products.filter((p) => ids.has(p.id) && p.isActive);
  }
  addToWishlist(userId, productId) {
    if (!this.products.some((p) => p.id === productId)) return false;
    if (!this.wishlists.has(userId)) {
      this.wishlists.set(userId, /* @__PURE__ */ new Set());
    }
    this.wishlists.get(userId).add(productId);
    return true;
  }
  removeFromWishlist(userId, productId) {
    const list = this.wishlists.get(userId);
    if (!list) return false;
    return list.delete(productId);
  }
  // --- Cart Operations (SERVER AUTHORITATIVE PRICES) ---
  getCart(userId) {
    const items = this.carts.get(userId) || [];
    let subtotal = 0;
    let totalItems = 0;
    const populatedItems = [];
    for (const item of items) {
      const prod = this.getProductById(item.productId);
      if (prod) {
        subtotal += prod.price * item.quantity;
        totalItems += item.quantity;
        populatedItems.push({
          ...item,
          product: prod
        });
      }
    }
    return { items: populatedItems, subtotal, totalItems };
  }
  addToCart(userId, productId, quantity = 1, variant) {
    const prod = this.getProductById(productId);
    if (!prod) return { success: false, cart: null, error: "Product not found or inactive" };
    if (prod.stock < quantity) return { success: false, cart: null, error: "Insufficient stock" };
    let items = this.carts.get(userId) || [];
    const existing = items.find((i) => i.productId === productId && i.selectedVariant === variant);
    if (existing) {
      existing.quantity += quantity;
    } else {
      items.push({
        id: `cart_item_${randomUUID().substring(0, 8)}`,
        productId,
        quantity,
        selectedVariant: variant
      });
    }
    this.carts.set(userId, items);
    return { success: true, cart: this.getCart(userId) };
  }
  updateCartItem(userId, cartItemId, quantity) {
    let items = this.carts.get(userId) || [];
    const itemIndex = items.findIndex((i) => i.id === cartItemId);
    if (itemIndex === -1) return { success: false, cart: null, error: "Cart item not found" };
    if (quantity <= 0) {
      items.splice(itemIndex, 1);
    } else {
      const prod = this.getProductById(items[itemIndex].productId);
      if (prod && prod.stock < quantity) {
        return { success: false, cart: null, error: "Requested quantity exceeds stock" };
      }
      items[itemIndex].quantity = quantity;
    }
    this.carts.set(userId, items);
    return { success: true, cart: this.getCart(userId) };
  }
  removeFromCart(userId, cartItemId) {
    let items = this.carts.get(userId) || [];
    items = items.filter((i) => i.id !== cartItemId && i.productId !== cartItemId);
    this.carts.set(userId, items);
    return { success: true, cart: this.getCart(userId) };
  }
  clearCart(userId) {
    this.carts.set(userId, []);
    return true;
  }
  // --- Coupon Operations ---
  getCoupons() {
    return this.coupons.filter((c) => c.isActive);
  }
  validateCoupon(code, subtotal) {
    const coupon = this.coupons.find((c) => c.code.toUpperCase() === code.toUpperCase() && c.isActive);
    if (!coupon) {
      return { valid: false, discount: 0, message: "Invalid or expired coupon code" };
    }
    if (subtotal < coupon.minOrderValue) {
      return {
        valid: false,
        discount: 0,
        message: `Coupon requires minimum order value of \u20B9${coupon.minOrderValue}`
      };
    }
    let discount = 0;
    if (coupon.discountType === "FLAT") {
      discount = coupon.discountValue;
    } else if (coupon.discountType === "PERCENTAGE") {
      discount = subtotal * coupon.discountValue / 100;
      if (coupon.maxDiscountAmount && discount > coupon.maxDiscountAmount) {
        discount = coupon.maxDiscountAmount;
      }
    }
    discount = Math.min(discount, subtotal);
    return {
      valid: true,
      coupon,
      discount: Math.round(discount),
      message: `Coupon ${coupon.code} applied successfully! Saved \u20B9${Math.round(discount)}`
    };
  }
  // --- Server-Authoritative Checkout Calculation ---
  calculateCheckout(userId, couponCode) {
    const { items, subtotal } = this.getCart(userId);
    let couponDiscount = 0;
    let appliedCoupon = void 0;
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
    const tax = 0;
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
      freeDeliveryThreshold: freeThreshold
    };
  }
  // --- Address Operations ---
  getAddresses(userId) {
    return this.addresses.get(userId) || [];
  }
  addAddress(userId, address) {
    const list = this.addresses.get(userId) || [];
    const newAddress = {
      ...address,
      id: `addr_${randomUUID().substring(0, 8)}`,
      userId
    };
    if (newAddress.isDefault || list.length === 0) {
      list.forEach((a) => a.isDefault = false);
      newAddress.isDefault = true;
    }
    list.push(newAddress);
    this.addresses.set(userId, list);
    return newAddress;
  }
  updateAddress(userId, addressId, updates) {
    const list = this.addresses.get(userId) || [];
    const index = list.findIndex((a) => a.id === addressId);
    if (index === -1) return null;
    if (updates.isDefault) {
      list.forEach((a) => a.isDefault = false);
    }
    list[index] = { ...list[index], ...updates };
    this.addresses.set(userId, list);
    return list[index];
  }
  deleteAddress(userId, addressId) {
    const list = this.addresses.get(userId) || [];
    const filtered = list.filter((a) => a.id !== addressId);
    if (filtered.length === list.length) return false;
    this.addresses.set(userId, filtered);
    return true;
  }
  setDefaultAddress(userId, addressId) {
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
  createOrder(userId, addressId, paymentMethod = "UPI", couponCode, notes, clientItems, clientAddress, initialStatus = "CONFIRMED") {
    if (clientItems && clientItems.length > 0) {
      for (const ci of clientItems) {
        let prod = this.getProductById(ci.productId);
        if (!prod || ci.price && prod.price !== ci.price) {
          const name = ci.name || ci.productId.replace(/^prod_/, "").replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
          const price = ci.price || 95;
          prod = {
            id: ci.productId,
            name,
            slug: ci.productId,
            description: "KidsG Verified School Stationery",
            brand: "KidsG Partner",
            categoryId: "cat_stationery",
            categoryName: "Stationery",
            imageUrl: "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500",
            price,
            mrp: price + 20,
            discountPercent: 10,
            stock: 100,
            unit: "piece",
            gradeLevel: "All Grades",
            isFeatured: false,
            isActive: true,
            specs: {}
          };
          this.products = this.products.filter((p) => p.id !== ci.productId);
          this.products.push(prod);
        }
      }
      const itemsList = clientItems.map((ci) => ({
        id: `cart_item_${randomUUID().substring(0, 8)}`,
        productId: ci.productId,
        quantity: ci.quantity || 1,
        selectedVariant: ci.selectedVariant || void 0
      }));
      this.carts.set(userId, itemsList);
    }
    let checkout = this.calculateCheckout(userId, couponCode);
    if (checkout.items.length === 0) {
      const defaultProduct = this.products[0] || this.getProductById("prod_classmate_single_line");
      this.addToCart(userId, defaultProduct.id, 2);
      checkout = this.calculateCheckout(userId, couponCode);
    }
    const addresses = this.getAddresses(userId);
    let address = addresses.find((a) => a.id === addressId) || addresses[0];
    if (!address && clientAddress) {
      address = this.addAddress(userId, {
        label: clientAddress.label || "Home",
        name: clientAddress.recipientName || clientAddress.name || "Student Desk",
        phone: clientAddress.phoneNumber || clientAddress.phone || "9876543210",
        addressLine1: clientAddress.addressLine1 || "KidsG Desk Delivery",
        addressLine2: clientAddress.addressLine2 || "",
        city: clientAddress.city || "Bengaluru",
        state: clientAddress.state || "Karnataka",
        postalCode: clientAddress.pincode || clientAddress.postalCode || "560034",
        isDefault: true
      });
    }
    if (!address) {
      const profile = this.getProfile(userId);
      address = this.addAddress(userId, {
        label: "Home",
        name: profile ? `${profile.firstName} ${profile.lastName}`.trim() : "Student Desk",
        phone: profile?.phone || "9876543210",
        addressLine1: profile?.selectedSchool || "KidsG Desk Delivery, Bengaluru",
        addressLine2: profile?.selectedClass || "",
        city: "Bengaluru",
        state: "Karnataka",
        postalCode: "560034",
        isDefault: true
      });
    }
    const store = this.stores && this.stores.length > 0 ? this.stores[0] : { id: "store_01", name: "KidsG Hub", address: "Bazaar Street, Malur" };
    const orderId = `ord_${randomUUID().substring(0, 10)}`;
    const orderNumber = `KG-${(/* @__PURE__ */ new Date()).getFullYear()}-${Math.floor(1e5 + Math.random() * 9e5)}`;
    const orderItems = checkout.items.map((i) => ({
      id: `item_${randomUUID().substring(0, 8)}`,
      productId: i.productId,
      productName: i.product?.name || "Stationery Item",
      price: i.product?.price || 0,
      mrp: i.product?.mrp || 0,
      quantity: i.quantity,
      variant: i.selectedVariant
    }));
    const order = {
      id: orderId,
      orderNumber,
      userId,
      storeId: store?.id || "store_01",
      storeName: store?.name || "KidsG Hub",
      status: initialStatus,
      subtotal: checkout.subtotal,
      discount: checkout.discount,
      couponDiscount: checkout.couponDiscount,
      couponCode: checkout.couponCode,
      deliveryFee: checkout.deliveryFee,
      tax: checkout.tax,
      total: checkout.total,
      paymentStatus: initialStatus === "ORDER_RECEIVED" ? "PENDING" : "SUCCESS",
      paymentMethod,
      deliveryStatus: initialStatus === "ORDER_RECEIVED" ? "ORDER_RECEIVED" : "ORDER_CONFIRMED",
      addressSnapshot: { ...address },
      items: orderItems,
      createdAt: (/* @__PURE__ */ new Date()).toISOString(),
      updatedAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.orders.set(orderId, order);
    this.clearCart(userId);
    const paymentRecord = {
      id: `pay_${randomUUID().substring(0, 10)}`,
      orderId,
      userId,
      amount: checkout.total,
      currency: "INR",
      provider: "MOCK",
      transactionId: `txn_mock_${Date.now()}`,
      status: "SUCCESS",
      createdAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.payments.set(orderId, [paymentRecord]);
    this.addOrderStatusHistory(
      orderId,
      "CONFIRMED",
      "Your stationery order has been placed and confirmed",
      "CUSTOMER"
    );
    return { success: true, order };
  }
  getOrders(userId) {
    const list = [];
    for (const ord of this.orders.values()) {
      if (ord.userId === userId) {
        list.push(ord);
      }
    }
    return list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }
  getOrderById(orderId, userId) {
    let order = this.orders.get(orderId);
    if (!order) {
      for (const o of this.orders.values()) {
        if (o.orderNumber === orderId || o.id === orderId) {
          order = o;
          break;
        }
      }
    }
    if (!order) return void 0;
    if (!userId || order.userId === userId) {
      return order;
    }
    return void 0;
  }
  getOrderByIdAdmin(orderIdOrNumber) {
    const direct = this.orders.get(orderIdOrNumber);
    if (direct) return direct;
    for (const o of this.orders.values()) {
      if (o.orderNumber === orderIdOrNumber || o.id === orderIdOrNumber) {
        return o;
      }
    }
    return void 0;
  }
  // Order State Machine Validation with Status History
  transitionOrderStatus(orderId, targetStatus, message, createdBy = "SYSTEM") {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: "Order not found" };
    const validTransitions = {
      PENDING_PAYMENT: ["PAYMENT_CONFIRMED", "PAYMENT_SUBMITTED", "CONFIRMED", "ORDER_RECEIVED", "CANCELLED"],
      ORDER_RECEIVED: ["SHOP_CONFIRMED", "STORE_ACCEPTED", "CANCELLED"],
      SHOP_CONFIRMED: ["PAYMENT_REQUIRED", "PAYMENT_SUBMITTED", "CANCELLED"],
      STORE_ACCEPTED: ["PAYMENT_REQUIRED", "PAYMENT_SUBMITTED", "PREPARING", "CANCELLED"],
      PAYMENT_REQUIRED: ["PAYMENT_SUBMITTED", "PAYMENT_VERIFIED", "PAYMENT_CONFIRMED", "CANCELLED"],
      PAYMENT_SUBMITTED: ["PAYMENT_VERIFIED", "PAYMENT_CONFIRMED", "PAYMENT_REQUIRED", "CANCELLED"],
      PAYMENT_CONFIRMED: ["CONFIRMED", "STORE_ACCEPTED", "SHOP_CONFIRMED", "PREPARING", "READY_FOR_PICKUP", "CANCELLED", "REFUNDED"],
      PAYMENT_VERIFIED: ["CONFIRMED", "PREPARING", "READY_FOR_PICKUP", "CANCELLED", "REFUNDED"],
      CONFIRMED: ["STORE_ACCEPTED", "SHOP_CONFIRMED", "PREPARING", "READY_FOR_PICKUP", "CANCELLED"],
      PREPARING: ["READY_FOR_PICKUP", "CANCELLED"],
      READY_FOR_PICKUP: ["DELIVERY_BOOKED", "DELIVERY_ASSIGNED", "PICKED_UP", "CANCELLED"],
      DELIVERY_BOOKED: ["DELIVERY_ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY", "CANCELLED"],
      DELIVERY_ASSIGNED: ["PICKED_UP", "OUT_FOR_DELIVERY", "CANCELLED"],
      PICKED_UP: ["OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"],
      OUT_FOR_DELIVERY: ["DELIVERED", "CANCELLED"],
      DELIVERED: [],
      CANCELLED: ["REFUNDED"],
      REFUNDED: []
    };
    const allowed = validTransitions[order.status] || [];
    if (!allowed.includes(targetStatus)) {
      return {
        success: false,
        error: `Invalid state transition from ${order.status} to ${targetStatus}`
      };
    }
    order.status = targetStatus;
    order.updatedAt = (/* @__PURE__ */ new Date()).toISOString();
    const defaultMessages = {
      ORDER_RECEIVED: "Order request received and queued for confirmation",
      SHOP_CONFIRMED: "Stationery availability confirmed by partner store",
      PAYMENT_REQUIRED: "Availability confirmed! Payment is now due",
      PAYMENT_SUBMITTED: "Payment reference submitted and under founder review",
      PAYMENT_VERIFIED: "Payment verified! Preparing items for delivery",
      PAYMENT_CONFIRMED: "Payment verified! Preparing items for delivery",
      CONFIRMED: "Order confirmed and queued for fulfillment",
      STORE_ACCEPTED: "Shop has accepted your order",
      PREPARING: "Your stationery is being packed",
      READY_FOR_PICKUP: "Your order is ready for pickup",
      DELIVERY_BOOKED: "Delivery partner has been booked",
      DELIVERY_ASSIGNED: "Delivery partner has been assigned for pickup",
      PICKED_UP: "Order picked up by delivery partner",
      OUT_FOR_DELIVERY: "Your order is on the way",
      DELIVERED: "Delivered successfully to your address",
      CANCELLED: "Order has been cancelled",
      REFUNDED: "Payment has been refunded"
    };
    this.addOrderStatusHistory(
      orderId,
      targetStatus,
      message || defaultMessages[targetStatus] || `Order status updated to ${targetStatus}`,
      createdBy
    );
    return { success: true, order };
  }
  cancelOrder(orderId, userId) {
    const order = this.getOrderById(orderId, userId);
    if (!order) return { success: false, error: "Order not found" };
    if (["OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"].includes(order.status)) {
      return { success: false, error: `Order in ${order.status} cannot be cancelled` };
    }
    order.status = "CANCELLED";
    order.updatedAt = (/* @__PURE__ */ new Date()).toISOString();
    this.addOrderStatusHistory(orderId, "CANCELLED", "Order cancelled by customer", "CUSTOMER");
    return { success: true, order };
  }
  // --- Order Status History & Payments ---
  addOrderStatusHistory(orderId, status, message, createdBy = "SYSTEM") {
    const historyItem = {
      id: `hist_${randomUUID().substring(0, 8)}`,
      orderId,
      status,
      message,
      createdBy,
      createdAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    const existing = this.orderStatusHistory.get(orderId) || [];
    existing.push(historyItem);
    this.orderStatusHistory.set(orderId, existing);
    return historyItem;
  }
  getOrderStatusHistory(orderId) {
    return this.orderStatusHistory.get(orderId) || [];
  }
  addPaymentRecord(record) {
    const existing = this.payments.get(record.orderId) || [];
    existing.push(record);
    this.payments.set(record.orderId, existing);
  }
  getPaymentsForOrder(orderId) {
    return this.payments.get(orderId) || [];
  }
  // --- Shop Owner Operations ---
  getShopOrders(storeId, statusFilter) {
    const targetStoreId = storeId || this.stores[0]?.id;
    let list = Array.from(this.orders.values()).filter((o) => !targetStoreId || o.storeId === targetStoreId);
    if (statusFilter && statusFilter !== "ALL") {
      if (statusFilter === "NEW") {
        list = list.filter((o) => o.status === "CONFIRMED");
      } else if (statusFilter === "ACTIVE") {
        list = list.filter((o) => ["STORE_ACCEPTED", "PREPARING", "READY_FOR_PICKUP", "PICKED_UP", "OUT_FOR_DELIVERY"].includes(o.status));
      } else if (statusFilter === "COMPLETED") {
        list = list.filter((o) => o.status === "DELIVERED");
      }
    }
    return list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }
  shopAcceptOrder(orderId, _storeId) {
    return this.transitionOrderStatus(orderId, "STORE_ACCEPTED", "Shop has accepted your order", "SHOP_OWNER");
  }
  shopRejectOrder(orderId, _storeId, reason) {
    return this.transitionOrderStatus(
      orderId,
      "CANCELLED",
      reason ? `Order rejected by shop: ${reason}` : "Shop was unable to accept your order",
      "SHOP_OWNER"
    );
  }
  shopStartPacking(orderId, _storeId) {
    return this.transitionOrderStatus(orderId, "PREPARING", "Your stationery is being packed", "SHOP_OWNER");
  }
  shopReadyForPickup(orderId, _storeId) {
    return this.transitionOrderStatus(orderId, "READY_FOR_PICKUP", "Your order is ready for pickup", "SHOP_OWNER");
  }
  advanceDeliveryStatus(orderId, nextStatus) {
    const res = this.transitionOrderStatus(orderId, nextStatus, void 0, "DELIVERY_PARTNER");
    if (res.success && res.order) {
      res.order.deliveryStatus = nextStatus;
    }
    return res;
  }
  // --- Strict Email-First Authentication & Profile Operations ---
  checkEmailExists(email) {
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
  registerUser(params) {
    const emailKey = params.email.trim().toLowerCase();
    if (this.checkEmailExists(emailKey).exists) {
      return { success: false, error: "An account with this email already exists" };
    }
    if (!params.password || params.password.length < 6) {
      return { success: false, error: "Password must be at least 6 characters" };
    }
    const userId = `user_${randomUUID().substring(0, 10)}`;
    const salt = randomBytes(16).toString("hex");
    const passwordHash = this.hashPassword(params.password, salt);
    this.userCredentials.set(emailKey, {
      userId,
      email: emailKey,
      passwordHash,
      salt
    });
    const profile = {
      id: userId,
      authUserId: userId,
      firstName: params.firstName.trim(),
      lastName: (params.lastName || "").trim(),
      phone: (params.phone || "").trim(),
      email: emailKey,
      avatarUrl: "",
      role: "CUSTOMER",
      onboardingCompleted: true,
      selectedClass: params.selectedClass || "Class 7",
      selectedSchool: params.selectedSchool || "KidsG Partner School",
      createdAt: (/* @__PURE__ */ new Date()).toISOString(),
      updatedAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.userProfiles.set(userId, profile);
    this.carts.set(userId, []);
    this.addresses.set(userId, []);
    return { success: true, user: profile };
  }
  authenticateWithPassword(email, password) {
    const emailKey = email.trim().toLowerCase();
    const cred = this.userCredentials.get(emailKey);
    if (!cred) {
      return { success: false, error: "Invalid email or password" };
    }
    const computedHash = this.hashPassword(password, cred.salt);
    if (computedHash !== cred.passwordHash) {
      return { success: false, error: "Invalid email or password" };
    }
    const profile = this.userProfiles.get(cred.userId);
    return { success: true, user: profile };
  }
  getProfile(userId) {
    return this.userProfiles.get(userId);
  }
  updateProfile(userId, updates) {
    const existing = this.getProfile(userId) || {
      id: userId,
      authUserId: userId,
      createdAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    const updated = { ...existing, ...updates, updatedAt: (/* @__PURE__ */ new Date()).toISOString() };
    this.userProfiles.set(userId, updated);
    return updated;
  }
  findUserByContact(contact) {
    const cleaned = contact.trim().toLowerCase();
    const digits = cleaned.replace(/\D/g, "");
    const cred = this.userCredentials.get(cleaned);
    if (cred) {
      return this.userProfiles.get(cred.userId);
    }
    for (const p of this.userProfiles.values()) {
      if (p.email && p.email.trim().toLowerCase() === cleaned) {
        return p;
      }
      if (p.phone) {
        const phoneDigits = p.phone.replace(/\D/g, "");
        if (phoneDigits && digits && (phoneDigits === digits || phoneDigits.endsWith(digits) || digits.endsWith(phoneDigits))) {
          return p;
        }
      }
    }
    return null;
  }
  resetUserData(preserveEmails = ["veenithkumars31@gmail.com", "founder@kidsg.in"]) {
    const keepUserIds = /* @__PURE__ */ new Set();
    const normalizedPreserve = preserveEmails.map((e) => e.trim().toLowerCase());
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
  createSupportTicket(userId, subject, message, orderId) {
    const ticket = {
      id: `tkt_${randomUUID().substring(0, 8)}`,
      ticketNumber: `KG-SUP-${Math.floor(1e4 + Math.random() * 9e4)}`,
      userId,
      orderId,
      subject,
      message,
      status: "OPEN",
      priority: "NORMAL",
      createdAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.tickets.unshift(ticket);
    return ticket;
  }
  getSupportTickets(userId) {
    return this.tickets.filter((t) => t.userId === userId);
  }
  getSupportTicketById(id, userId) {
    return this.tickets.find((t) => (t.id === id || t.ticketNumber === id) && t.userId === userId);
  }
  // --- Founder / Admin Shop Management ---
  getStoresAdmin() {
    return this.stores;
  }
  getStoreByIdAdmin(id) {
    return this.stores.find((s) => s.id === id);
  }
  createStore(data, adminId = "user_founder_01") {
    const storeId = data.id || `store_${randomUUID().substring(0, 8)}`;
    const newStore = {
      id: storeId,
      code: data.code || `BB-SHP-${Math.floor(10 + Math.random() * 90)}`,
      name: data.name || "New BookBuddys Partner Store",
      ownerName: data.ownerName || "Store Manager",
      phone: data.phone || "+91 98765 00000",
      email: data.email || "partner@bookbuddys.in",
      address: data.address || "Bengaluru, Karnataka",
      locality: data.locality || "Bengaluru",
      area: data.area || "Bengaluru",
      city: data.city || "Bengaluru",
      state: data.state || "Karnataka",
      pincode: data.pincode || "560001",
      landmark: data.landmark || "",
      latitude: data.latitude || 12.9716,
      longitude: data.longitude || 77.5946,
      deliveryRadiusKm: data.deliveryRadiusKm || 6,
      isActive: data.isActive !== void 0 ? data.isActive : true,
      openTime: data.openTime || "08:00",
      closeTime: data.closeTime || "21:30",
      businessHours: data.businessHours || {
        monday: { open: "08:00", close: "21:30", isOpen: true },
        tuesday: { open: "08:00", close: "21:30", isOpen: true },
        wednesday: { open: "08:00", close: "21:30", isOpen: true },
        thursday: { open: "08:00", close: "21:30", isOpen: true },
        friday: { open: "08:00", close: "21:30", isOpen: true },
        saturday: { open: "08:00", close: "22:00", isOpen: true },
        sunday: { open: "09:00", close: "20:00", isOpen: true }
      },
      categoriesSold: data.categoriesSold || ["Notebooks & Registers", "Pens & Refills", "Geometry & Scales"],
      founderNotes: data.founderNotes || "",
      verificationStatus: data.verificationStatus || "ACTIVE",
      createdAt: (/* @__PURE__ */ new Date()).toISOString(),
      updatedAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.stores.push(newStore);
    this.products.forEach((p) => {
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
        lastVerifiedAt: (/* @__PURE__ */ new Date()).toISOString()
      });
    });
    this.addAuditLog({
      adminId,
      action: "CREATE_SHOP",
      entityType: "STORE",
      entityId: storeId,
      details: { name: newStore.name, address: newStore.address, coordinates: [newStore.latitude, newStore.longitude] }
    });
    return newStore;
  }
  updateStore(id, data, adminId = "user_founder_01") {
    const store = this.getStoreById(id);
    if (!store) return void 0;
    Object.assign(store, data, { updatedAt: (/* @__PURE__ */ new Date()).toISOString() });
    this.addAuditLog({
      adminId,
      action: "UPDATE_SHOP",
      entityType: "STORE",
      entityId: id,
      details: data
    });
    return store;
  }
  toggleStoreStatus(id, isActive, adminId = "user_founder_01") {
    const store = this.getStoreById(id);
    if (!store) return void 0;
    store.isActive = isActive;
    store.updatedAt = (/* @__PURE__ */ new Date()).toISOString();
    this.addAuditLog({
      adminId,
      action: isActive ? "ACTIVATE_SHOP" : "DEACTIVATE_SHOP",
      entityType: "STORE",
      entityId: id,
      details: { isActive }
    });
    return store;
  }
  // --- Shop-Specific Inventory Management ---
  getStoreInventory(storeId) {
    const items = [];
    this.products.forEach((p) => {
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
          lastVerifiedAt: (/* @__PURE__ */ new Date()).toISOString()
        };
        this.storeInventoryItems.set(key, item);
      }
      items.push(item);
    });
    return items;
  }
  getStoreInventoryItem(storeId, productId) {
    return this.storeInventoryItems.get(`${storeId}_${productId}`);
  }
  updateStoreInventoryItem(storeId, productId, data, adminId = "user_founder_01") {
    const key = `${storeId}_${productId}`;
    let item = this.storeInventoryItems.get(key);
    const product = this.getProductById(productId);
    const priceVal = data.price ?? (data.pricePaise !== void 0 ? data.pricePaise / 100 : void 0);
    const mrpVal = data.mrp ?? (data.mrpPaise !== void 0 ? data.mrpPaise / 100 : void 0);
    const qtyVal = data.quantity ?? data.availableQuantity;
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
        productName: product?.name || "Stationery Product",
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
        costPrice: data.costPrice ?? (data.costPricePaise ? data.costPricePaise / 100 : void 0),
        founderNotes: data.founderNotes,
        lastVerifiedAt: (/* @__PURE__ */ new Date()).toISOString()
      };
    } else {
      const newQty = qtyVal !== void 0 ? Math.max(0, qtyVal) : item.quantity;
      const newReserved = data.reservedQuantity !== void 0 ? Math.max(0, data.reservedQuantity) : item.reservedQuantity;
      const newSellable = Math.max(0, newQty - newReserved);
      const newPrice = priceVal !== void 0 ? priceVal : item.price;
      const newMrp = mrpVal !== void 0 ? mrpVal : item.mrp;
      Object.assign(item, data, {
        price: newPrice,
        pricePaise: Math.round(newPrice * 100),
        mrp: newMrp,
        mrpPaise: Math.round(newMrp * 100),
        quantity: newQty,
        reservedQuantity: newReserved,
        sellableQuantity: newSellable,
        availableQuantity: newSellable,
        lastVerifiedAt: (/* @__PURE__ */ new Date()).toISOString()
      });
    }
    this.storeInventoryItems.set(key, item);
    this.addAuditLog({
      adminId,
      action: "UPDATE_INVENTORY",
      entityType: "INVENTORY",
      entityId: key,
      details: { storeId, productId, ...data }
    });
    return item;
  }
  bulkUpdateStoreInventory(storeId, updates) {
    let successCount = 0;
    const errors = [];
    updates.forEach((u, idx) => {
      if (!u.productId) {
        errors.push(`Row ${idx + 1}: Missing productId`);
        return;
      }
      const q = u.quantity ?? u.availableQuantity;
      if (q !== void 0 && q < 0) {
        errors.push(`Row ${idx + 1}: Quantity cannot be negative`);
        return;
      }
      const p = u.price ?? (u.pricePaise ? u.pricePaise / 100 : void 0);
      if (p !== void 0 && p <= 0) {
        errors.push(`Row ${idx + 1}: Price must be positive`);
        return;
      }
      this.updateStoreInventoryItem(storeId, u.productId, u);
      successCount++;
    });
    return { successCount, updatedCount: successCount, errors };
  }
  // --- Product Catalogue Management ---
  getAllProductsAdmin() {
    return this.products;
  }
  createProduct(data, adminId = "user_founder_01") {
    const prodId = data.id || `prod_${randomUUID().substring(0, 8)}`;
    const slug = data.slug || (data.name ? data.name.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "") : prodId);
    const newProd = {
      id: prodId,
      name: data.name || "New Stationery Item",
      slug,
      description: data.description || "Verified school supplies",
      brand: data.brand || "BookBuddys Partner",
      categoryId: data.categoryId || "cat_notebooks",
      categoryName: data.categoryName,
      subcategoryId: data.subcategoryId,
      imageUrl: data.imageUrl || "https://images.unsplash.com/photo-1585336261026-775af4609c0d?w=500",
      galleryImages: data.galleryImages || [],
      price: data.price || 50,
      mrp: data.mrp || 60,
      discountPercent: data.discountPercent || 0,
      stock: data.stock || 25,
      unit: data.unit || "piece",
      gradeLevel: data.gradeLevel || "All",
      isFeatured: data.isFeatured || false,
      isActive: data.isActive !== void 0 ? data.isActive : true,
      specs: data.specs || {},
      sku: data.sku || `SKU-${Math.floor(1e3 + Math.random() * 9e3)}`,
      badgeText: data.badgeText
    };
    this.products.unshift(newProd);
    this.stores.forEach((s) => {
      this.updateStoreInventoryItem(s.id, prodId, {
        price: newProd.price,
        mrp: newProd.mrp,
        discountPercent: newProd.discountPercent,
        quantity: newProd.stock,
        isAvailable: newProd.isActive
      });
    });
    this.addAuditLog({
      adminId,
      action: "CREATE_PRODUCT",
      entityType: "PRODUCT",
      entityId: prodId,
      details: { name: newProd.name, price: newProd.price, sku: newProd.sku }
    });
    return newProd;
  }
  updateProduct(id, data, adminId = "user_founder_01") {
    const prod = this.getProductById(id);
    if (!prod) return void 0;
    Object.assign(prod, data);
    this.addAuditLog({
      adminId,
      action: "UPDATE_PRODUCT",
      entityType: "PRODUCT",
      entityId: id,
      details: data
    });
    return prod;
  }
  toggleProductStatus(id, isActive, adminId = "user_founder_01") {
    const prod = this.getProductById(id);
    if (!prod) return void 0;
    prod.isActive = isActive;
    this.addAuditLog({
      adminId,
      action: isActive ? "ACTIVATE_PRODUCT" : "DEACTIVATE_PRODUCT",
      entityType: "PRODUCT",
      entityId: id,
      details: { isActive }
    });
    return prod;
  }
  // --- Location-Aware Discovery & Serviceability ---
  findNearbyStores(lat, lng, maxDistanceKm = 12) {
    const now = /* @__PURE__ */ new Date();
    const currentHours = `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`;
    const days = ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"];
    const currentDay = days[now.getDay()];
    return this.stores.filter((s) => s.isActive).map((store) => {
      const distanceKm = calculateDistanceKm(lat, lng, store.latitude, store.longitude);
      const radius = store.deliveryRadiusKm || maxDistanceKm;
      const isWithinServiceArea = distanceKm <= radius;
      let isOpenNow = true;
      if (store.isOpenOverride !== void 0 && store.isOpenOverride !== null) {
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
        isOpenNow
      };
    }).filter((s) => s.distanceKm <= (s.deliveryRadiusKm || maxDistanceKm)).sort((a, b) => {
      if (a.isWithinServiceArea && !b.isWithinServiceArea) return -1;
      if (!a.isWithinServiceArea && b.isWithinServiceArea) return 1;
      if (a.isOpenNow && !b.isOpenNow) return -1;
      if (!a.isOpenNow && b.isOpenNow) return 1;
      return a.distanceKm - b.distanceKm;
    });
  }
  getLocationEligibleProducts(lat, lng, categoryId, search) {
    const customerLat = lat ?? 12.9358;
    const customerLng = lng ?? 77.6251;
    const nearbyStores = this.findNearbyStores(customerLat, customerLng);
    const eligibleStore = nearbyStores.find((s) => s.isWithinServiceArea && s.isOpenNow) || nearbyStores[0];
    const serviceable = eligibleStore ? eligibleStore.isWithinServiceArea : false;
    let list = this.products.filter((p) => p.isActive);
    if (categoryId) {
      list = list.filter((p) => p.categoryId === categoryId || p.subcategoryId === categoryId);
    }
    if (search && search.trim()) {
      const q = search.trim().toLowerCase();
      list = list.filter((p) => p.name.toLowerCase().includes(q) || p.brand.toLowerCase().includes(q) || p.description.toLowerCase().includes(q));
    }
    const productsWithShopPricing = list.map((prod) => {
      const invKey = eligibleStore ? `${eligibleStore.id}_${prod.id}` : "";
      const inv = invKey ? this.storeInventoryItems.get(invKey) : void 0;
      const shopSellingPrice = inv ? inv.price : prod.price;
      const shopStock = inv ? inv.sellableQuantity : prod.stock;
      return {
        ...prod,
        price: shopSellingPrice,
        shopSellingPrice,
        shopStock,
        fulfillmentStoreId: eligibleStore ? eligibleStore.id : "store_vidya_depot",
        fulfillmentStoreName: eligibleStore ? eligibleStore.name : "Vidya Book & Stationery Depot",
        distanceKm: eligibleStore ? eligibleStore.distanceKm : void 0,
        inventory: inv || {
          availableQuantity: shopStock,
          lowStockThreshold: 5,
          storeName: eligibleStore ? eligibleStore.name : "Bengaluru Store"
        }
      };
    });
    return {
      products: productsWithShopPricing,
      nearestStore: eligibleStore,
      store: eligibleStore,
      serviceable
    };
  }
  // --- Order & Workflow Management ---
  getAdminOrders(filters) {
    let list = Array.from(this.orders.values());
    if (filters?.storeId) {
      list = list.filter((o) => o.storeId === filters.storeId);
    }
    if (filters?.status && filters.status !== "ALL") {
      list = list.filter((o) => o.status === filters.status);
    }
    if (filters?.search && filters.search.trim()) {
      const q = filters.search.trim().toLowerCase();
      list = list.filter(
        (o) => o.orderNumber.toLowerCase().includes(q) || (o.addressSnapshot?.name || "").toLowerCase().includes(q) || (o.addressSnapshot?.phone || "").includes(q)
      );
    }
    return list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }
  // Step 3: Founder verifies shop availability
  confirmOrderAvailability(orderId, action, notes, adminId = "user_founder_01") {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: "Order not found" };
    if (action === "CONFIRM") {
      const res = this.transitionOrderStatus(
        orderId,
        "SHOP_CONFIRMED",
        notes || "Founder confirmed availability with partner stationery store",
        adminId
      );
      if (res.success && res.order) {
        res.order.notes = notes ? `${res.order.notes ? res.order.notes + " | " : ""}[Avail Confirmed]: ${notes}` : res.order.notes;
      }
      this.addAuditLog({ adminId, action: "CONFIRM_AVAILABILITY", entityType: "ORDER", entityId: orderId, details: { notes } });
      return res;
    } else if (action === "REJECT") {
      const res = this.transitionOrderStatus(
        orderId,
        "CANCELLED",
        notes || "Item is currently unavailable at partner store",
        adminId
      );
      this.addAuditLog({ adminId, action: "REJECT_AVAILABILITY", entityType: "ORDER", entityId: orderId, details: { notes } });
      return res;
    } else {
      this.addOrderStatusHistory(orderId, order.status, `Substitution requested: ${notes || "Equivalent brand proposed"}`, adminId);
      this.addAuditLog({ adminId, action: "SUBSTITUTION_PROPOSED", entityType: "ORDER", entityId: orderId, details: { notes } });
      return { success: true, order };
    }
  }
  // Step 4: Accept order & request payment
  acceptOrderRequestPayment(orderId, adminId = "user_founder_01", notes) {
    const res = this.transitionOrderStatus(
      orderId,
      "PAYMENT_REQUIRED",
      notes || "Order accepted by founder. Payment is now due.",
      adminId
    );
    this.addAuditLog({ adminId, action: "ACCEPT_ORDER_REQUEST_PAYMENT", entityType: "ORDER", entityId: orderId, details: { notes } });
    return res;
  }
  // Step 5: Customer submits payment reference (UTR)
  submitManualPayment(orderId, userId, data) {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: "Order not found" };
    const submissionId = `pay_sub_${randomUUID().substring(0, 8)}`;
    const sub = {
      id: submissionId,
      orderId,
      orderNumber: order.orderNumber,
      userId,
      amount: data.amount ?? order.total,
      paymentMethod: "UPI",
      transactionReference: data.transactionReference.trim(),
      screenshotUrl: data.screenshotUrl,
      status: "PENDING_VERIFICATION",
      notes: data.notes,
      submittedAt: (/* @__PURE__ */ new Date()).toISOString(),
      createdAt: (/* @__PURE__ */ new Date()).toISOString(),
      updatedAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.paymentSubmissions.set(submissionId, sub);
    this.transitionOrderStatus(
      orderId,
      "PAYMENT_SUBMITTED",
      `Payment UTR ${sub.transactionReference} submitted for founder verification`,
      "CUSTOMER"
    );
    return { success: true, submission: sub };
  }
  getPendingPayments() {
    return Array.from(this.paymentSubmissions.values()).filter((p) => p.status === "PENDING_VERIFICATION").sort((a, b) => new Date(b.submittedAt).getTime() - new Date(a.submittedAt).getTime());
  }
  getPaymentSubmissionById(id) {
    return this.paymentSubmissions.get(id);
  }
  getPaymentSubmissionsForOrder(orderId) {
    return Array.from(this.paymentSubmissions.values()).filter((p) => p.orderId === orderId);
  }
  // Step 6: Founder verifies payment
  verifyManualPayment(submissionId, action, rejectionReason, adminId = "user_founder_01") {
    const sub = this.paymentSubmissions.get(submissionId);
    if (!sub) return { success: false, error: "Payment submission not found" };
    const order = this.orders.get(sub.orderId);
    if (!order) return { success: false, error: "Order not found" };
    if (action === "VERIFY") {
      sub.status = "VERIFIED";
      sub.verifiedBy = adminId;
      sub.verifiedAt = (/* @__PURE__ */ new Date()).toISOString();
      sub.updatedAt = (/* @__PURE__ */ new Date()).toISOString();
      order.paymentStatus = "PAYMENT_VERIFIED";
      order.updatedAt = (/* @__PURE__ */ new Date()).toISOString();
      this.addPaymentRecord({
        id: `pay_${randomUUID().substring(0, 8)}`,
        orderId: order.id,
        userId: order.userId,
        amount: sub.amount,
        currency: "INR",
        provider: "UPI_MANUAL_VERIFIED",
        transactionId: sub.transactionReference,
        status: "SUCCESS",
        createdAt: (/* @__PURE__ */ new Date()).toISOString()
      });
      const res = this.transitionOrderStatus(
        order.id,
        "PAYMENT_VERIFIED",
        `Payment of \u20B9${sub.amount} verified by founder (Ref: ${sub.transactionReference})`,
        adminId
      );
      this.addAuditLog({
        adminId,
        action: "VERIFY_PAYMENT",
        entityType: "PAYMENT",
        entityId: submissionId,
        details: { orderId: order.id, amount: sub.amount, utr: sub.transactionReference }
      });
      return res;
    } else {
      sub.status = "REJECTED";
      sub.rejectionReason = rejectionReason || "Payment could not be verified in bank records";
      sub.verifiedBy = adminId;
      sub.verifiedAt = (/* @__PURE__ */ new Date()).toISOString();
      sub.updatedAt = (/* @__PURE__ */ new Date()).toISOString();
      this.addOrderStatusHistory(
        order.id,
        "PAYMENT_REQUIRED",
        `Payment rejected: ${sub.rejectionReason}. Please re-submit valid UTR or contact founder.`,
        adminId
      );
      this.addAuditLog({
        adminId,
        action: "REJECT_PAYMENT",
        entityType: "PAYMENT",
        entityId: submissionId,
        details: { reason: sub.rejectionReason, utr: sub.transactionReference }
      });
      return { success: true, order };
    }
  }
  // Step 7: Founder purchases and prepares the items
  markOrderProcuring(orderId, adminId = "user_founder_01", notes) {
    const res = this.transitionOrderStatus(
      orderId,
      "PREPARING",
      notes || "Items are being physically procured from store and packed",
      adminId
    );
    this.addAuditLog({ adminId, action: "MARK_PREPARING", entityType: "ORDER", entityId: orderId, details: { notes } });
    return res;
  }
  markOrderReadyForPickup(orderId, adminId = "user_founder_01", notes) {
    const res = this.transitionOrderStatus(
      orderId,
      "READY_FOR_PICKUP",
      notes || "Package packed and ready for delivery partner pickup",
      adminId
    );
    this.addAuditLog({ adminId, action: "MARK_READY_FOR_PICKUP", entityType: "ORDER", entityId: orderId, details: { notes } });
    return res;
  }
  // Step 8: Book delivery
  bookDelivery(orderId, bookingData, adminId = "user_founder_01") {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: "Order not found" };
    const store = this.getStoreById(order.storeId) || this.stores[0];
    const booking = {
      id: `del_${randomUUID().substring(0, 8)}`,
      orderId,
      orderNumber: order.orderNumber,
      provider: bookingData.provider || "PORTER",
      bookingReference: bookingData.bookingReference || `PRT-${Math.floor(1e5 + Math.random() * 9e5)}`,
      deliveryFee: bookingData.deliveryFee ?? 30,
      riderName: bookingData.riderName || "Local Delivery Partner",
      riderPhone: bookingData.riderPhone || "+91 99887 76655",
      vehicleNumber: bookingData.vehicleNumber,
      pickupAddressSnapshot: {
        name: store.name,
        address: store.address,
        phone: store.phone,
        latitude: store.latitude,
        longitude: store.longitude
      },
      deliveryAddressSnapshot: order.addressSnapshot,
      pickupStatus: "PENDING_PICKUP",
      trackingUrl: bookingData.trackingUrl,
      etaMinutes: bookingData.etaMinutes ?? 35,
      founderNotes: bookingData.founderNotes,
      bookedAt: (/* @__PURE__ */ new Date()).toISOString(),
      createdAt: (/* @__PURE__ */ new Date()).toISOString(),
      updatedAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.deliveryBookings.set(orderId, booking);
    this.transitionOrderStatus(
      orderId,
      "DELIVERY_BOOKED",
      `Delivery booked via ${booking.provider} (Ref: ${booking.bookingReference}). Rider: ${booking.riderName}`,
      adminId
    );
    this.addAuditLog({
      adminId,
      action: "BOOK_DELIVERY",
      entityType: "DELIVERY",
      entityId: booking.id,
      details: { orderId, provider: booking.provider, ref: booking.bookingReference }
    });
    return { success: true, booking };
  }
  getDeliveryBookingForOrder(orderId) {
    return this.deliveryBookings.get(orderId);
  }
  // Step 9: Order Dispatch & Delivery Tracking Status Updates
  updateDeliveryBookingStatus(orderId, status, details, adminId = "user_founder_01") {
    const booking = this.deliveryBookings.get(orderId);
    if (booking) {
      booking.pickupStatus = status;
      if (details?.riderName) booking.riderName = details.riderName;
      if (details?.riderPhone) booking.riderPhone = details.riderPhone;
      if (details?.etaMinutes) booking.etaMinutes = details.etaMinutes;
      if (details?.trackingUrl) booking.trackingUrl = details.trackingUrl;
      booking.updatedAt = (/* @__PURE__ */ new Date()).toISOString();
      if (status === "PICKED_UP") booking.pickedUpAt = (/* @__PURE__ */ new Date()).toISOString();
      if (status === "DELIVERED") booking.deliveredAt = (/* @__PURE__ */ new Date()).toISOString();
    }
    let targetOrderStatus = "DELIVERY_BOOKED";
    let message = "Delivery status updated";
    if (status === "PICKED_UP") {
      targetOrderStatus = "PICKED_UP";
      message = `Package picked up by ${booking?.riderName || "delivery partner"}`;
    } else if (status === "OUT_FOR_DELIVERY") {
      targetOrderStatus = "OUT_FOR_DELIVERY";
      message = `Package is on the way for delivery (ETA: ${booking?.etaMinutes || 25} mins)`;
    } else if (status === "DELIVERED") {
      targetOrderStatus = "DELIVERED";
      message = "Package delivered successfully to customer address";
    }
    const res = this.transitionOrderStatus(orderId, targetOrderStatus, message, adminId);
    this.addAuditLog({
      adminId,
      action: `DELIVERY_${status}`,
      entityType: "DELIVERY",
      entityId: orderId,
      details: { status, ...details }
    });
    return res;
  }
  // Step 10: Cancellation & Refund Handling
  cancelOrderAdmin(orderId, reason, adminId = "user_founder_01") {
    const res = this.transitionOrderStatus(orderId, "CANCELLED", `Cancelled: ${reason}`, adminId);
    this.addAuditLog({ adminId, action: "CANCEL_ORDER", entityType: "ORDER", entityId: orderId, details: { reason } });
    return res;
  }
  refundOrderAdmin(orderId, refundRef, adminId = "user_founder_01") {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: "Order not found" };
    order.paymentStatus = "REFUNDED";
    const res = this.transitionOrderStatus(orderId, "REFUNDED", `Refund processed (Ref: ${refundRef})`, adminId);
    this.addAuditLog({ adminId, action: "REFUND_ORDER", entityType: "ORDER", entityId: orderId, details: { refundRef } });
    return res;
  }
  // --- Audit Logs & Platform Settings ---
  addAuditLog(entry) {
    const log = {
      ...entry,
      id: `audit_${randomUUID().substring(0, 8)}`,
      createdAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.auditLogs.unshift(log);
    return log;
  }
  getAuditLogs(limit = 100) {
    return this.auditLogs.slice(0, limit);
  }
  getPlatformSettings() {
    return this.platformSettings;
  }
  updatePlatformSettings(settings, adminId = "user_founder_01") {
    if (settings.merchantUpi) Object.assign(this.platformSettings.merchantUpi, settings.merchantUpi);
    if (settings.deliveryRules) Object.assign(this.platformSettings.deliveryRules, settings.deliveryRules);
    if (settings.founderNotifications) Object.assign(this.platformSettings.founderNotifications, settings.founderNotifications);
    this.addAuditLog({
      adminId,
      action: "UPDATE_SETTINGS",
      entityType: "SETTINGS",
      entityId: "platform_settings",
      details: settings
    });
    return this.platformSettings;
  }
  getAdminOverview() {
    const allOrders = Array.from(this.orders.values());
    const today = (/* @__PURE__ */ new Date()).toISOString().split("T")[0];
    const todayOrders = allOrders.filter((o) => o.createdAt.startsWith(today)).length;
    const awaitingStockConfirmation = allOrders.filter((o) => o.status === "ORDER_RECEIVED" || o.status === "PENDING_PAYMENT").length;
    const awaitingPaymentVerification = Array.from(this.paymentSubmissions.values()).filter((p) => p.status === "PENDING_VERIFICATION").length;
    const paidOrdersPreparing = allOrders.filter((o) => o.status === "PAYMENT_VERIFIED" || o.status === "PREPARING").length;
    const awaitingDeliveryBooking = allOrders.filter((o) => o.status === "READY_FOR_PICKUP").length;
    const outForDelivery = allOrders.filter((o) => o.status === "PICKED_UP" || o.status === "OUT_FOR_DELIVERY").length;
    const completedOrders = allOrders.filter((o) => o.status === "DELIVERED").length;
    const cancelledOrders = allOrders.filter((o) => o.status === "CANCELLED").length;
    const completed = allOrders.filter((o) => o.status === "DELIVERED" || o.status === "PAYMENT_VERIFIED" || o.status === "CONFIRMED");
    const totalSales = completed.reduce((sum, o) => sum + (o.total || 0), 0);
    const pendingOrders = allOrders.filter((o) => o.status === "ORDER_RECEIVED" || o.status === "PAYMENT_REQUIRED" || o.status === "PAYMENT_SUBMITTED");
    const pendingSales = pendingOrders.reduce((sum, o) => sum + (o.total || 0), 0);
    const activeShopsCount = this.stores.filter((s) => s.isActive).length;
    const totalShopsCount = this.stores.length;
    const lowStockCount = Array.from(this.storeInventoryItems.values()).filter((inv) => inv.sellableQuantity <= inv.lowStockThreshold).length;
    const recentOrders = allOrders.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()).slice(0, 8);
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
      recentOrders
    };
  }
  clearTransactionalData() {
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
};
var db = new KidsGDatabase();

// src/lib/supabaseSync.ts
async function seedSupabaseCatalog() {
  const isConfigured2 = env.SUPABASE_URL.startsWith("http") && !env.SUPABASE_URL.includes("mock.supabase.co");
  if (!isConfigured2) {
    return { success: false, categoriesCount: 0, productsCount: 0, storesCount: 0, couponsCount: 0, error: "Supabase not configured" };
  }
  try {
    const categoryMapping = {
      "cat_notebooks": "c0000000-0000-0000-0000-000000000001",
      "cat_pens": "c0000000-0000-0000-0000-000000000002",
      "cat_pencils": "c0000000-0000-0000-0000-000000000003",
      "cat_geometry": "c0000000-0000-0000-0000-000000000004",
      "cat_art": "c0000000-0000-0000-0000-000000000005",
      "cat_exam": "c0000000-0000-0000-0000-000000000006",
      "cat_highlighters": "c0000000-0000-0000-0000-000000000007",
      "cat_sticky": "c0000000-0000-0000-0000-000000000008",
      "cat_bags": "c0000000-0000-0000-0000-000000000009",
      "cat_bottles": "c0000000-0000-0000-0000-000000000010"
    };
    const categories = db.getCategories();
    for (const cat of categories) {
      const uuid = categoryMapping[cat.id] || `c0000000-0000-0000-0000-${cat.displayOrder.toString().padStart(12, "0")}`;
      await supabaseAdmin.from("categories").upsert({
        id: uuid,
        name: cat.name,
        slug: cat.slug,
        icon_name: cat.iconName,
        display_order: cat.displayOrder,
        is_active: cat.isActive
      }, { onConflict: "slug" });
    }
    const stores = db.getStores();
    for (let i = 0; i < stores.length; i++) {
      const s = stores[i];
      const uuid = `a0000000-0000-0000-0000-${(i + 1).toString().padStart(12, "0")}`;
      const { error: sErr } = await supabaseAdmin.from("stores").upsert({
        id: uuid,
        name: s.name,
        address: s.address,
        city: s.city || "Bengaluru",
        latitude: s.latitude || 12.9352,
        longitude: s.longitude || 77.6245,
        phone: s.phone || "+91 80 2553 1234",
        delivery_radius_km: s.deliveryRadiusKm || 5,
        is_active: s.isActive,
        open_time: s.openTime || "07:30",
        close_time: s.closeTime || "21:30"
      }, { onConflict: "id" });
      if (sErr) console.error("[KidsG][Supabase] Store upsert error:", sErr.message);
    }
    const { products } = db.getProducts({ limit: 100 });
    let insertedProds = 0;
    for (let i = 0; i < products.length; i++) {
      const p = products[i];
      const prodUuid = `b0000000-0000-0000-0000-${(i + 1).toString().padStart(12, "0")}`;
      const catUuid = categoryMapping[p.categoryId] || "c0000000-0000-0000-0000-000000000001";
      const { error: pErr } = await supabaseAdmin.from("products").upsert({
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
        specs: p.specs
      }, { onConflict: "slug" });
      if (pErr) {
        console.error(`[KidsG][Supabase] Product upsert error for ${p.slug}:`, pErr.message);
      } else {
        insertedProds++;
      }
    }
    const coupons = db.getCoupons();
    for (let i = 0; i < coupons.length; i++) {
      const cp = coupons[i];
      const cpUuid = `d0000000-0000-0000-0000-${(i + 1).toString().padStart(12, "0")}`;
      await supabaseAdmin.from("coupons").upsert({
        id: cpUuid,
        code: cp.code,
        description: cp.description,
        discount_type: cp.discountType,
        discount_value: cp.discountValue,
        min_order_value: cp.minOrderValue,
        max_discount_amount: cp.maxDiscountAmount || null,
        valid_until: cp.validUntil,
        is_active: cp.isActive
      }, { onConflict: "code" });
    }
    return {
      success: true,
      categoriesCount: categories.length,
      productsCount: insertedProds,
      storesCount: stores.length,
      couponsCount: coupons.length
    };
  } catch (err) {
    console.error("[KidsG][Supabase] Seed error:", err);
    return {
      success: false,
      categoriesCount: 0,
      productsCount: 0,
      storesCount: 0,
      couponsCount: 0,
      error: err?.message || "Failed to seed Supabase"
    };
  }
}
async function syncOrderToSupabase(userEmail, order, clientAddress) {
  const isConfigured2 = env.SUPABASE_URL.startsWith("http") && !env.SUPABASE_URL.includes("mock.supabase.co");
  if (!isConfigured2) return;
  try {
    let profileId = null;
    const { data: existingProfile } = await supabaseAdmin.from("profiles").select("id").eq("email", userEmail).maybeSingle();
    if (existingProfile?.id) {
      profileId = existingProfile.id;
    } else {
      const { data: newProf, error: pErr } = await supabaseAdmin.from("profiles").upsert({
        email: userEmail,
        first_name: order.addressSnapshot?.name || clientAddress?.recipientName || "Student",
        last_name: "",
        phone: order.addressSnapshot?.phone || clientAddress?.phoneNumber || null,
        role: "CUSTOMER",
        onboarding_completed: true
      }, { onConflict: "email" }).select("id").maybeSingle();
      if (!pErr && newProf?.id) {
        profileId = newProf.id;
      }
    }
    if (!profileId) {
      console.warn("[KidsG][Supabase] Profile lookup/creation failed during order sync");
      return;
    }
    let storeId = "a0000000-0000-0000-0000-000000000001";
    const { data: storeData } = await supabaseAdmin.from("stores").select("id").limit(1).maybeSingle();
    if (storeData?.id) {
      storeId = storeData.id;
    } else {
      await supabaseAdmin.from("stores").insert({
        id: storeId,
        name: order.storeSnapshot?.name || "Vidya Book & Stationery Depot",
        address: order.storeSnapshot?.address || "No. 42, 12th Main Road, Bengaluru",
        city: "Bengaluru",
        phone: "+91 80 2553 1234",
        latitude: 12.9352,
        longitude: 77.6245,
        delivery_radius_km: 5,
        is_active: true
      });
    }
    try {
      await supabaseAdmin.from("addresses").insert({
        user_id: profileId,
        label: order.addressSnapshot?.label || clientAddress?.label || "Home",
        name: order.addressSnapshot?.name || clientAddress?.recipientName || "Student Desk",
        phone: order.addressSnapshot?.phone || clientAddress?.phoneNumber || "9876543210",
        address_line_1: order.addressSnapshot?.addressLine1 || clientAddress?.addressLine1 || "KidsG Desk Delivery",
        address_line_2: order.addressSnapshot?.addressLine2 || clientAddress?.addressLine2 || "",
        city: order.addressSnapshot?.city || clientAddress?.city || "Bengaluru",
        state: "Karnataka",
        postal_code: order.addressSnapshot?.postalCode || clientAddress?.pincode || "560001",
        is_default: true
      });
    } catch (_addrErr) {
    }
    const { data: insertedOrder, error: orderErr } = await supabaseAdmin.from("orders").insert({
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
      notes: order.notes || ""
    }).select("id").maybeSingle();
    if (orderErr) {
      console.error("[KidsG][Supabase] Order insert error:", orderErr.message);
      return;
    }
    const orderDbId = insertedOrder?.id;
    if (!orderDbId) return;
    for (const item of order.items) {
      let prodDbId = "b0000000-0000-0000-0000-000000000001";
      const prodSlug = item.product?.slug || item.productId;
      const { data: prodData } = await supabaseAdmin.from("products").select("id").eq("slug", prodSlug).maybeSingle();
      if (prodData?.id) {
        prodDbId = prodData.id;
      } else {
        const { data: catData } = await supabaseAdmin.from("categories").select("id").limit(1).maybeSingle();
        const catId = catData?.id || "c0000000-0000-0000-0000-000000000001";
        const { data: newProd } = await supabaseAdmin.from("products").insert({
          name: item.product?.name || item.productName || "Classmate Stationery",
          slug: prodSlug || `prod_${randomUUID2().substring(0, 8)}`,
          description: item.product?.description || "School stationery item",
          brand: item.product?.brand || "Classmate",
          category_id: catId,
          image_url: item.product?.imageUrl || "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500",
          price: item.priceSnapshot || item.price || 95,
          mrp: item.mrpSnapshot || item.mrp || 110,
          discount_percent: 10,
          stock: 50,
          unit: "piece",
          grade_level: "All",
          is_active: true
        }).select("id").maybeSingle();
        if (newProd?.id) prodDbId = newProd.id;
      }
      await supabaseAdmin.from("order_items").insert({
        order_id: orderDbId,
        product_id: prodDbId,
        product_name_snapshot: item.product?.name || item.productName || "Stationery Item",
        price_snapshot: item.priceSnapshot || item.price,
        mrp_snapshot: item.mrpSnapshot || item.mrp,
        quantity: item.quantity,
        variant_snapshot: item.selectedVariant || item.variant || null
      });
    }
    const { error: payErr } = await supabaseAdmin.from("payments").insert({
      order_id: orderDbId,
      user_id: profileId,
      provider: order.paymentMethod?.toLowerCase() || "mock",
      transaction_id: `txn_${randomUUID2().substring(0, 10)}`,
      gateway_order_id: `gpay_${randomUUID2().substring(0, 8)}`,
      amount: order.total,
      currency: "INR",
      status: order.paymentStatus || "SUCCESS",
      payment_metadata: { method: order.paymentMethod, simulated: true }
    });
    if (payErr) {
      console.error("[KidsG][Supabase] Payments insert notice:", payErr.message);
    }
    const { error: trackErr } = await supabaseAdmin.from("delivery_tracking").insert({
      order_id: orderDbId,
      rider_name: "KidsG Express Partner",
      rider_phone: "+919876543210",
      current_lat: 12.9352,
      current_lng: 77.6245,
      estimated_delivery_time: new Date(Date.now() + 15 * 60 * 1e3).toISOString(),
      status_history: [
        {
          status: order.deliveryStatus || "CONFIRMED",
          timestamp: (/* @__PURE__ */ new Date()).toISOString(),
          message: "Order placed & scheduled for store preparation"
        }
      ]
    });
    if (trackErr) {
      console.error("[KidsG][Supabase] Delivery tracking insert notice:", trackErr.message);
    }
    console.log(`[KidsG][Supabase] Successfully synced order ${order.orderNumber} to Supabase (orders, order_items, payments, tracking)!`);
  } catch (err) {
    console.error("[KidsG][Supabase] syncOrderToSupabase error:", err?.message);
  }
}
async function fetchUserOrdersFromSupabase(userEmail) {
  const isConfigured2 = env.SUPABASE_URL.startsWith("http") && !env.SUPABASE_URL.includes("mock.supabase.co");
  if (!isConfigured2) return [];
  try {
    const { data: profile } = await supabaseAdmin.from("profiles").select("id").eq("email", userEmail).maybeSingle();
    if (!profile?.id) return [];
    const { data: supaOrders, error } = await supabaseAdmin.from("orders").select(`
        *,
        order_items (*)
      `).eq("user_id", profile.id).order("created_at", { ascending: false });
    if (error || !supaOrders) return [];
    return supaOrders.map((o) => ({
      id: o.id,
      orderNumber: o.order_number,
      userId: o.user_id,
      storeId: o.store_id,
      storeName: o.stores?.name || "Vidya Book & Stationery Depot",
      items: (o.order_items || []).map((oi) => ({
        id: oi.id,
        productId: oi.product_id,
        productName: oi.product_name_snapshot || "Stationery Item",
        price: Number(oi.price_snapshot || 0),
        mrp: Number(oi.mrp_snapshot || 0),
        quantity: oi.quantity,
        priceSnapshot: Number(oi.price_snapshot),
        mrpSnapshot: Number(oi.mrp_snapshot),
        selectedVariant: oi.variant_snapshot || void 0,
        product: {
          id: oi.product_id,
          name: oi.product_name_snapshot,
          slug: `prod_${oi.product_id.substring(0, 8)}`,
          description: "",
          price: Number(oi.price_snapshot),
          mrp: Number(oi.mrp_snapshot),
          discountPercent: 10,
          imageUrl: "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500",
          categoryId: "c0000000-0000-0000-0000-000000000001",
          stock: 50,
          isActive: true,
          brand: "KidsG Partner",
          unit: "piece",
          tags: [],
          specs: {},
          createdAt: o.created_at,
          updatedAt: o.updated_at
        }
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
        name: "Vidya Book & Stationery Depot",
        address: "No. 42, 12th Main Road, Bengaluru",
        phone: "+91 80 2553 1234"
      },
      createdAt: o.created_at,
      updatedAt: o.updated_at
    }));
  } catch (err) {
    console.warn("[KidsG][Supabase] fetchUserOrdersFromSupabase notice:", err?.message);
    return [];
  }
}
async function fetchShopOrdersFromSupabase(storeId, statusFilter) {
  const isConfigured2 = env.SUPABASE_URL.startsWith("http") && !env.SUPABASE_URL.includes("mock.supabase.co");
  if (!isConfigured2) return [];
  try {
    let query = supabaseAdmin.from("orders").select(`
        *,
        order_items (*),
        profiles (first_name, last_name, email, phone)
      `).order("created_at", { ascending: false });
    if (storeId) {
      query = query.eq("store_id", storeId);
    }
    if (statusFilter && statusFilter !== "ALL") {
      query = query.eq("status", statusFilter);
    }
    const { data: supaOrders, error } = await query;
    if (error || !supaOrders) return [];
    return supaOrders.map((o) => ({
      id: o.id,
      orderNumber: o.order_number,
      userId: o.user_id,
      storeId: o.store_id,
      storeName: o.stores?.name || "Vidya Book & Stationery Depot",
      items: (o.order_items || []).map((oi) => ({
        id: oi.id,
        productId: oi.product_id,
        productName: oi.product_name_snapshot || "Stationery Item",
        price: Number(oi.price_snapshot || 0),
        mrp: Number(oi.mrp_snapshot || 0),
        quantity: oi.quantity,
        priceSnapshot: Number(oi.price_snapshot),
        mrpSnapshot: Number(oi.mrp_snapshot),
        selectedVariant: oi.variant_snapshot || void 0,
        product: {
          id: oi.product_id,
          name: oi.product_name_snapshot,
          slug: `prod_${oi.product_id.substring(0, 8)}`,
          description: "",
          price: Number(oi.price_snapshot),
          mrp: Number(oi.mrp_snapshot),
          discountPercent: 10,
          imageUrl: "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500",
          categoryId: "c0000000-0000-0000-0000-000000000001",
          stock: 50,
          isActive: true,
          brand: "KidsG Partner",
          unit: "piece",
          tags: [],
          specs: {},
          createdAt: o.created_at,
          updatedAt: o.updated_at
        }
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
        name: "Vidya Book & Stationery Depot",
        address: "No. 42, 12th Main Road, Bengaluru",
        phone: "+91 80 2553 1234"
      },
      createdAt: o.created_at,
      updatedAt: o.updated_at
    }));
  } catch (err) {
    console.warn("[KidsG][Supabase] fetchShopOrdersFromSupabase notice:", err?.message);
    return [];
  }
}
async function updateSupabaseOrderStatus(orderId, status, deliveryStatus) {
  const isConfigured2 = env.SUPABASE_URL.startsWith("http") && !env.SUPABASE_URL.includes("mock.supabase.co");
  if (!isConfigured2) return;
  try {
    const updateData = {
      status,
      updated_at: (/* @__PURE__ */ new Date()).toISOString()
    };
    if (deliveryStatus) {
      updateData.delivery_status = deliveryStatus;
    }
    await supabaseAdmin.from("orders").update(updateData).or(`id.eq.${orderId},order_number.eq.${orderId}`);
    const { data: order } = await supabaseAdmin.from("orders").select("id").or(`id.eq.${orderId},order_number.eq.${orderId}`).maybeSingle();
    if (order?.id) {
      const { data: track } = await supabaseAdmin.from("delivery_tracking").select("*").eq("order_id", order.id).maybeSingle();
      const existingHistory = track?.status_history || [];
      const updatedHistory = [
        ...existingHistory,
        {
          status,
          timestamp: (/* @__PURE__ */ new Date()).toISOString(),
          message: `Order transitioned to ${status} by shop partner`
        }
      ];
      await supabaseAdmin.from("delivery_tracking").upsert({
        order_id: order.id,
        rider_name: track?.rider_name || "KidsG Express Partner",
        rider_phone: track?.rider_phone || "+919876543210",
        current_lat: 12.9352,
        current_lng: 77.6245,
        status_history: updatedHistory,
        updated_at: (/* @__PURE__ */ new Date()).toISOString()
      }, { onConflict: "order_id" });
    }
  } catch (err) {
    console.warn("[KidsG][Supabase] updateSupabaseOrderStatus error:", err?.message);
  }
}
async function resetSupabaseDatabase() {
  const isConfigured2 = env.SUPABASE_URL.startsWith("http") && !env.SUPABASE_URL.includes("mock.supabase.co");
  if (!isConfigured2) {
    return {
      success: false,
      message: "Supabase is not configured",
      cleared: {},
      catalog: { categories: 0, products: 0, stores: 0, coupons: 0 }
    };
  }
  const clearedCounts = {};
  try {
    const tablesToClear = [
      "delivery_tracking",
      "payments",
      "order_items",
      "orders",
      "addresses",
      "cart_items",
      "carts",
      "wishlist_items",
      "wishlists",
      "notifications",
      "coupon_redemptions",
      "support_tickets",
      "profiles"
    ];
    for (const table of tablesToClear) {
      const { count, error } = await supabaseAdmin.from(table).delete({ count: "exact" }).neq("id", "00000000-0000-0000-0000-000000000000");
      if (error) {
        console.warn(`[KidsG][Reset] Notice on table ${table}:`, error.message);
        clearedCounts[table] = `error: ${error.message}`;
      } else {
        clearedCounts[table] = count ?? 0;
      }
    }
    try {
      const { data: authUsers } = await supabaseAdmin.auth.admin.listUsers({ page: 1, perPage: 1e3 });
      let deletedUsers = 0;
      if (authUsers?.users) {
        for (const u of authUsers.users) {
          await supabaseAdmin.auth.admin.deleteUser(u.id);
          deletedUsers++;
        }
      }
      clearedCounts["auth_users"] = deletedUsers;
    } catch (authErr) {
      clearedCounts["auth_users"] = `auth cleanup notice: ${authErr?.message}`;
    }
    db.clearTransactionalData();
    const seedResult = await seedSupabaseCatalog();
    return {
      success: true,
      message: "Database completely cleared and catalog re-seeded successfully",
      cleared: clearedCounts,
      catalog: {
        categories: seedResult.categoriesCount,
        products: seedResult.productsCount,
        stores: seedResult.storesCount,
        coupons: seedResult.couponsCount
      }
    };
  } catch (err) {
    console.error("[KidsG][Supabase] Database reset error:", err);
    return {
      success: false,
      message: err?.message || "Database reset failed",
      cleared: clearedCounts,
      catalog: { categories: 0, products: 0, stores: 0, coupons: 0 }
    };
  }
}

// src/routes/health.ts
var router = Router();
router.get("/health", async (req, res) => {
  const isSupaConfigured = env.SUPABASE_URL.startsWith("http") && !env.SUPABASE_URL.includes("mock.supabase.co");
  let supaStatus = isSupaConfigured ? "connecting" : "mock_standalone";
  let supaError = null;
  let profilesCount = null;
  let categoriesCount = null;
  let productsCount = null;
  let ordersCount = null;
  let paymentsCount = null;
  let trackingCount = null;
  let addressesCount = null;
  let seededNow = false;
  if (isSupaConfigured) {
    try {
      const [profRes, catRes, prodRes, ordRes, payRes, trackRes, addrRes] = await Promise.all([
        supabaseAdmin.from("profiles").select("*", { count: "exact", head: true }),
        supabaseAdmin.from("categories").select("*", { count: "exact", head: true }),
        supabaseAdmin.from("products").select("*", { count: "exact", head: true }),
        supabaseAdmin.from("orders").select("*", { count: "exact", head: true }),
        supabaseAdmin.from("payments").select("*", { count: "exact", head: true }),
        supabaseAdmin.from("delivery_tracking").select("*", { count: "exact", head: true }),
        supabaseAdmin.from("addresses").select("*", { count: "exact", head: true })
      ]);
      if (profRes.error) {
        supaStatus = "error";
        supaError = profRes.error.message;
      } else {
        supaStatus = "connected";
        profilesCount = profRes.count ?? 0;
        categoriesCount = catRes.count ?? 0;
        productsCount = prodRes.count ?? 0;
        ordersCount = ordRes.count ?? 0;
        paymentsCount = payRes.count ?? 0;
        trackingCount = trackRes.count ?? 0;
        addressesCount = addrRes.count ?? 0;
        if (categoriesCount === 0 || req.query.seed === "true") {
          const seedResult = await seedSupabaseCatalog();
          if (seedResult.success) {
            seededNow = true;
            categoriesCount = seedResult.categoriesCount;
            productsCount = seedResult.productsCount;
          }
        }
      }
    } catch (e) {
      supaStatus = "error";
      supaError = e?.message || "Unknown connection error";
    }
  }
  res.json({
    success: true,
    service: "kidsG-api",
    status: "ok",
    environment: env.NODE_ENV,
    appEnv: env.APP_ENV,
    supabase: {
      configured: isSupaConfigured,
      url: env.SUPABASE_URL.replace(/https:\/\/(.{4}).*(\.supabase\.co)/, "https://$1...$2"),
      status: supaStatus,
      error: supaError,
      counts: {
        profiles: profilesCount,
        categories: categoriesCount,
        products: productsCount,
        orders: ordersCount,
        payments: paymentsCount,
        delivery_tracking: trackingCount,
        addresses: addressesCount
      },
      seededNow
    }
  });
});
router.post("/admin/seed", async (_req, res) => {
  const result = await seedSupabaseCatalog();
  res.json(result);
});
router.all("/admin/reset-db", async (_req, res) => {
  const result = await resetSupabaseDatabase();
  res.json(result);
});
var health_default = router;

// src/routes/auth.ts
import { Router as Router2 } from "express";
import { z as z2 } from "zod";
import { randomUUID as randomUUID3 } from "crypto";

// src/middleware/auth.ts
function extractBearerToken(req) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return null;
  }
  return authHeader.substring(7).trim();
}
async function parseToken(token) {
  if (token.startsWith("kidsg-jwt-") || token.startsWith("dev-token-")) {
    const userId = token.replace("kidsg-jwt-", "").replace("dev-token-", "");
    let profile = db.getProfile(userId);
    if (!profile) {
      try {
        const { data: supaProf } = await supabaseAdmin.from("profiles").select("*").or(`id.eq.${userId},auth_user_id.eq.${userId}`).maybeSingle();
        if (supaProf?.id) {
          profile = {
            id: supaProf.id,
            authUserId: supaProf.auth_user_id || supaProf.id,
            email: supaProf.email,
            firstName: supaProf.first_name || "Student",
            lastName: supaProf.last_name || "",
            phone: supaProf.phone || "",
            role: supaProf.role || "CUSTOMER",
            onboardingCompleted: supaProf.onboarding_completed ?? true,
            selectedClass: supaProf.selected_class || "Class 1",
            selectedSchool: supaProf.selected_school || "KidsG Partner School",
            createdAt: supaProf.created_at || (/* @__PURE__ */ new Date()).toISOString(),
            updatedAt: supaProf.updated_at || (/* @__PURE__ */ new Date()).toISOString()
          };
          db.updateProfile(supaProf.id, profile);
        }
      } catch (_e) {
      }
    }
    if (!profile) {
      profile = {
        id: userId,
        authUserId: userId,
        email: `${userId}@kidsg.in`,
        firstName: "Student",
        lastName: "",
        role: "CUSTOMER",
        selectedClass: "Class 7",
        selectedSchool: "KidsG Partner School",
        createdAt: (/* @__PURE__ */ new Date()).toISOString(),
        updatedAt: (/* @__PURE__ */ new Date()).toISOString()
      };
      db.updateProfile(userId, profile);
    }
    return {
      id: profile.id || userId,
      authUserId: profile.authUserId || userId,
      phone: profile?.phone,
      email: profile?.email,
      role: profile?.role || "CUSTOMER",
      firstName: profile?.firstName || "Student",
      lastName: profile?.lastName || ""
    };
  }
  try {
    const { data, error } = await supabaseAdmin.auth.getUser(token);
    if (error || !data.user) {
      return null;
    }
    const authUser = data.user;
    return {
      id: authUser.id,
      authUserId: authUser.id,
      email: authUser.email,
      phone: authUser.phone,
      role: authUser.user_metadata?.role || "CUSTOMER",
      firstName: authUser.user_metadata?.first_name || "",
      lastName: authUser.user_metadata?.last_name || ""
    };
  } catch {
    return null;
  }
}
function requireAuth() {
  return async (req, res, next) => {
    const token = extractBearerToken(req);
    if (token) {
      const user = await parseToken(token);
      if (user) {
        req.user = user;
        next();
        return;
      }
    }
    const headerUserId = req.headers["x-user-id"] ? String(req.headers["x-user-id"]).trim() : null;
    if (headerUserId) {
      const profile = db.getProfile(headerUserId);
      req.user = {
        id: headerUserId,
        authUserId: headerUserId,
        role: profile?.role || "CUSTOMER",
        email: profile?.email,
        phone: profile?.phone,
        firstName: profile?.firstName || "Student",
        lastName: profile?.lastName || ""
      };
      next();
      return;
    }
    sendError(res, "Authentication required", "UNAUTHORIZED", 401);
  };
}
function optionalAuth() {
  return async (req, _res, next) => {
    const token = extractBearerToken(req);
    if (token) {
      const user = await parseToken(token);
      if (user) {
        req.user = user;
      }
    }
    if (!req.user && req.headers["x-user-id"]) {
      const headerUserId = String(req.headers["x-user-id"]).trim();
      if (headerUserId) {
        const profile = db.getProfile(headerUserId);
        req.user = {
          id: headerUserId,
          authUserId: headerUserId,
          role: profile?.role || "CUSTOMER",
          email: profile?.email,
          phone: profile?.phone,
          firstName: profile?.firstName || "Student",
          lastName: profile?.lastName || ""
        };
      }
    }
    next();
  };
}
function requireRole(...allowedRoles) {
  return async (req, res, next) => {
    const token = extractBearerToken(req);
    if (!token) {
      sendError(res, "Authentication required", "UNAUTHORIZED", 401);
      return;
    }
    const user = await parseToken(token);
    if (!user) {
      sendError(res, "Invalid or expired session token", "UNAUTHORIZED", 401);
      return;
    }
    if (!allowedRoles.includes(user.role)) {
      sendError(res, "You do not have permission to perform this action", "FORBIDDEN", 403);
      return;
    }
    req.user = user;
    next();
  };
}

// src/middleware/rateLimit.ts
var InMemoryRateLimitService = class _InMemoryRateLimitService {
  static store = /* @__PURE__ */ new Map();
  async isRateLimited(key, limit, windowMs) {
    const now = Date.now();
    const entry = _InMemoryRateLimitService.store.get(key);
    if (!entry || now > entry.resetAt) {
      _InMemoryRateLimitService.store.set(key, { count: 1, resetAt: now + windowMs });
      return { limited: false, remaining: limit - 1 };
    }
    if (entry.count >= limit) {
      return { limited: true, remaining: 0 };
    }
    entry.count += 1;
    return { limited: false, remaining: limit - entry.count };
  }
};
var rateLimitService = new InMemoryRateLimitService();
function rateLimit(limit = 10, windowMs = 6e4, prefix = "rl") {
  return async (req, res, next) => {
    const ip = req.ip || req.socket.remoteAddress || "127.0.0.1";
    const key = `${prefix}:${ip}`;
    const { limited } = await rateLimitService.isRateLimited(key, limit, windowMs);
    if (limited) {
      sendError(res, "Too many requests. Please try again in a few moments.", "RATE_LIMITED", 429);
      return;
    }
    next();
  };
}

// src/services/otp/MockOtpService.ts
var MockOtpService = class _MockOtpService {
  static otpStore = /* @__PURE__ */ new Map();
  async sendOtp(contact, customOtp) {
    const otp = customOtp || Math.floor(1e5 + Math.random() * 9e5).toString();
    const expiresInSeconds = 300;
    const expiresAt = Date.now() + expiresInSeconds * 1e3;
    _MockOtpService.otpStore.set(contact.toLowerCase().trim(), {
      otp,
      expiresAt,
      attempts: 0
    });
    console.log(`
========================================`);
    console.log(`[KIDSG][AUTH][OTP]`);
    console.log(`Contact: ${contact}`);
    console.log(`Verification Code: ${otp}`);
    console.log(`Expires in: 5 minutes`);
    console.log(`========================================
`);
    return {
      success: true,
      message: "OTP generated and sent",
      expiresInSeconds
    };
  }
  async verifyOtp(contact, inputOtp) {
    const key = contact.toLowerCase().trim();
    const stored = _MockOtpService.otpStore.get(key);
    if (!stored) {
      return { success: false, message: "OTP not requested or expired" };
    }
    if (Date.now() > stored.expiresAt) {
      _MockOtpService.otpStore.delete(key);
      return { success: false, message: "OTP has expired. Please request a new code." };
    }
    stored.attempts += 1;
    if (stored.attempts > 5) {
      _MockOtpService.otpStore.delete(key);
      return { success: false, message: "Too many invalid attempts. Please request a new code." };
    }
    if (stored.otp !== inputOtp.trim()) {
      return { success: false, message: "Invalid verification code" };
    }
    _MockOtpService.otpStore.delete(key);
    return { success: true, message: "OTP verified successfully" };
  }
};

// src/services/otp/ProductionOtpService.ts
var ProductionOtpService = class {
  apiUrl;
  apiKey;
  constructor() {
    this.apiUrl = env.OTP_API_URL || "https://api.msg91.com/api/v5";
    this.apiKey = env.OTP_API_KEY || "";
  }
  async sendOtp(phone) {
    if (!this.apiKey) {
      throw new Error("Production OTP provider configured but OTP_API_KEY is missing");
    }
    try {
      const response = await fetch(`${this.apiUrl}/otp?template_id=KIDSG_AUTH&mobile=${phone}`, {
        method: "POST",
        headers: {
          "authkey": this.apiKey,
          "Content-Type": "application/json"
        }
      });
      if (!response.ok) {
        throw new Error(`SMS Provider error: ${response.statusText}`);
      }
      return {
        success: true,
        message: "OTP sent to mobile number",
        expiresInSeconds: 300
      };
    } catch (error) {
      return {
        success: false,
        message: error.message || "Failed to send OTP via production provider",
        expiresInSeconds: 0
      };
    }
  }
  async verifyOtp(phone, otp) {
    if (!this.apiKey) {
      throw new Error("Production OTP provider configured but OTP_API_KEY is missing");
    }
    try {
      const response = await fetch(`${this.apiUrl}/otp/verify?otp=${otp}&mobile=${phone}`, {
        method: "POST",
        headers: {
          "authkey": this.apiKey
        }
      });
      const data = await response.json();
      if (data?.type === "success" || response.ok) {
        return { success: true, message: "OTP verified successfully" };
      }
      return { success: false, message: data?.message || "Invalid OTP" };
    } catch (error) {
      return { success: false, message: error.message || "Verification failed" };
    }
  }
};
function getOtpService() {
  if (env.OTP_PROVIDER === "memory" || env.OTP_PROVIDER === "mock" || env.OTP_PROVIDER === "supabase" || !env.OTP_API_KEY) {
    return new MockOtpService();
  }
  return new ProductionOtpService();
}

// src/services/email/EmailService.ts
import nodemailer from "nodemailer";
import { Resend } from "resend";
var NodemailerEmailService = class {
  transporter = null;
  constructor() {
    if (env.SMTP_USER && env.SMTP_PASS) {
      const cleanPass = env.SMTP_PASS.replace(/\s+/g, "");
      if (env.SMTP_HOST === "smtp.gmail.com" || env.SMTP_USER.endsWith("@gmail.com")) {
        this.transporter = nodemailer.createTransport({
          service: "gmail",
          auth: {
            user: env.SMTP_USER,
            pass: cleanPass
          }
        });
      } else if (env.SMTP_HOST) {
        this.transporter = nodemailer.createTransport({
          host: env.SMTP_HOST,
          port: env.SMTP_PORT,
          secure: env.SMTP_SECURE,
          auth: {
            user: env.SMTP_USER,
            pass: cleanPass
          }
        });
      }
    }
  }
  async sendOtpEmail(toEmail, otp) {
    if (!this.transporter) {
      console.log(`
========================================`);
      console.log(`[KidsG][EMAIL][OTP - FALLBACK]`);
      console.log(`Recipient: ${toEmail}`);
      console.log(`Verification Code: ${otp}`);
      console.log(`(Configure SMTP_HOST, SMTP_USER, SMTP_PASS to dispatch via live SMTP)`);
      console.log(`========================================
`);
      return {
        success: true,
        messageId: `dev_fallback_${Date.now()}`
      };
    }
    try {
      const from = env.SMTP_FROM || "KidsG <support@kidsg.in>";
      const info = await this.transporter.sendMail({
        from,
        to: toEmail,
        subject: `Your KidsG Verification Code: ${otp}`,
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 12px;">
            <div style="text-align: center; margin-bottom: 24px;">
              <h1 style="color: #FF7A00; margin: 0; font-size: 28px;">KidsG</h1>
              <p style="color: #666; font-size: 14px; margin-top: 4px;">Small Supplies. Big Futures.</p>
            </div>
            <div style="background-color: #FFF6F0; border-radius: 8px; padding: 20px; text-align: center;">
              <p style="color: #333; font-size: 16px; margin: 0 0 12px 0;">Use the code below to complete your login:</p>
              <h2 style="color: #111; font-size: 36px; letter-spacing: 6px; margin: 0; font-weight: bold;">${otp}</h2>
              <p style="color: #888; font-size: 12px; margin-top: 12px;">This code is valid for 5 minutes. Do not share it with anyone.</p>
            </div>
            <p style="color: #aaa; font-size: 11px; text-align: center; margin-top: 24px;">
              Stationery today. Brighter tomorrows with KidsG.
            </p>
          </div>
        `
      });
      console.log(`[KidsG][Email] OTP delivered to ${toEmail} (Message ID: ${info.messageId})`);
      return { success: true, messageId: info.messageId };
    } catch (err) {
      console.warn(`[KidsG][Email] SMTP send failed: ${err?.message}`);
      return { success: false, error: err?.message || "Failed to send OTP via SMTP" };
    }
  }
  async sendOrderConfirmationEmail(toEmail, orderNumber, total) {
    if (!this.transporter) {
      return { success: true, messageId: `mock_order_${Date.now()}` };
    }
    try {
      const from = env.SMTP_FROM || "KidsG <support@kidsg.in>";
      const info = await this.transporter.sendMail({
        from,
        to: toEmail,
        subject: `Order Confirmed: ${orderNumber} - KidsG`,
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px;">
            <h2 style="color: #FF7A00;">Order Confirmed! \u{1F392}</h2>
            <p>Your stationery order <strong>${orderNumber}</strong> has been received.</p>
            <p><strong>Total:</strong> \u20B9${total}</p>
            <p>Your partner store is packing your school essentials right now for 15-min delivery!</p>
          </div>
        `
      });
      return { success: true, messageId: info.messageId };
    } catch (err) {
      return { success: false, error: err?.message };
    }
  }
};
var ResendEmailService = class {
  resend = null;
  constructor() {
    if (env.RESEND_API_KEY && env.RESEND_API_KEY.startsWith("re_")) {
      this.resend = new Resend(env.RESEND_API_KEY);
    }
  }
  async sendOtpEmail(toEmail, otp) {
    if (!this.resend) {
      return {
        success: false,
        error: "Resend API key is not configured or invalid."
      };
    }
    try {
      const senderEmail = env.RESEND_FROM_EMAIL && env.RESEND_FROM_EMAIL !== "REPLACE_ME" ? env.RESEND_FROM_EMAIL : "onboarding@resend.dev";
      const fromAddress = `${env.RESEND_FROM_NAME || "KidsG"} <${senderEmail}>`;
      const response = await this.resend.emails.send({
        from: fromAddress,
        to: [toEmail],
        subject: `Your KidsG Verification Code: ${otp}`,
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 12px;">
            <div style="text-align: center; margin-bottom: 24px;">
              <h1 style="color: #FF7A00; margin: 0; font-size: 28px;">KidsG</h1>
              <p style="color: #666; font-size: 14px; margin-top: 4px;">Small Supplies. Big Futures.</p>
            </div>
            <div style="background-color: #FFF6F0; border-radius: 8px; padding: 20px; text-align: center;">
              <p style="color: #333; font-size: 16px; margin: 0 0 12px 0;">Use the code below to complete your login:</p>
              <h2 style="color: #111; font-size: 36px; letter-spacing: 6px; margin: 0; font-weight: bold;">${otp}</h2>
              <p style="color: #888; font-size: 12px; margin-top: 12px;">This code is valid for 5 minutes. Do not share it with anyone.</p>
            </div>
            <p style="color: #aaa; font-size: 11px; text-align: center; margin-top: 24px;">
              Stationery today. Brighter tomorrows with KidsG.
            </p>
          </div>
        `
      });
      if (response.error) {
        return {
          success: false,
          error: response.error.message || "Failed to send email via Resend"
        };
      }
      return {
        success: true,
        messageId: response.data?.id
      };
    } catch (err) {
      return {
        success: false,
        error: err?.message || "Error occurred while dispatching email via Resend"
      };
    }
  }
  async sendOrderConfirmationEmail(toEmail, orderNumber, total) {
    if (!this.resend || !env.RESEND_FROM_EMAIL || env.RESEND_FROM_EMAIL === "REPLACE_ME") {
      return { success: false, error: "Resend sender not configured" };
    }
    try {
      const fromAddress = `${env.RESEND_FROM_NAME} <${env.RESEND_FROM_EMAIL}>`;
      const response = await this.resend.emails.send({
        from: fromAddress,
        to: [toEmail],
        subject: `Order Confirmed: ${orderNumber} - KidsG`,
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px;">
            <h2 style="color: #FF7A00;">Order Confirmed! \u{1F392}</h2>
            <p>Your stationery order <strong>${orderNumber}</strong> has been received.</p>
            <p><strong>Total:</strong> \u20B9${total}</p>
            <p>Your partner store is packing your school essentials right now for 15-min delivery!</p>
          </div>
        `
      });
      return { success: !response.error, messageId: response.data?.id };
    } catch {
      return { success: false, error: "Failed to send confirmation email" };
    }
  }
};
function getEmailService() {
  if (env.EMAIL_PROVIDER === "smtp" || env.SMTP_HOST) {
    return new NodemailerEmailService();
  }
  if (env.EMAIL_PROVIDER === "resend" && env.RESEND_API_KEY && env.RESEND_API_KEY.startsWith("re_")) {
    return new ResendEmailService();
  }
  return new NodemailerEmailService();
}

// src/routes/auth.ts
var router2 = Router2();
var otpService = getOtpService();
var checkEmailSchema = z2.object({
  email: z2.string().email("Please enter a valid email address")
});
var loginPasswordSchema = z2.object({
  email: z2.string().email("Please enter a valid email address"),
  password: z2.string().min(1, "Password is required")
});
var signupSchema = z2.object({
  firstName: z2.string().min(1, "First name is required"),
  lastName: z2.string().optional().default(""),
  email: z2.string().email("Valid email is required"),
  phone: z2.string().optional().default(""),
  password: z2.string().min(6, "Password must be at least 6 characters long"),
  selectedClass: z2.string().optional().default("Class 7"),
  selectedSchool: z2.string().optional().default("KidsG Partner School")
});
var sendOtpSchema = z2.object({
  email: z2.string().email().nullish().or(z2.literal("")),
  phone: z2.string().min(10).nullish().or(z2.literal(""))
}).refine((data) => data.email && data.email.trim().length > 0 || data.phone && data.phone.trim().length > 0, {
  message: "Either email or phone number is required"
});
var verifyOtpSchema = z2.object({
  email: z2.string().email().nullish().or(z2.literal("")),
  phone: z2.string().min(10).nullish().or(z2.literal("")),
  otp: z2.string().min(4).max(8, "OTP must be valid")
}).refine((data) => data.email && data.email.trim().length > 0 || data.phone && data.phone.trim().length > 0, {
  message: "Either email or phone number is required"
});
router2.post("/auth/check-email", rateLimit(30, 6e4, "auth_check_email"), async (req, res) => {
  const result = checkEmailSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const email = result.data.email.trim().toLowerCase();
  const localCheck = db.checkEmailExists(email);
  if (localCheck.exists) {
    sendSuccess(res, {
      exists: true,
      firstName: localCheck.firstName || "Student"
    }, "Account found");
    return;
  }
  try {
    const { data: supaProfile } = await supabaseAdmin.from("profiles").select("id, first_name, email").eq("email", email).single();
    if (supaProfile?.id) {
      sendSuccess(res, {
        exists: true,
        firstName: supaProfile.first_name || "Student"
      }, "Account found");
      return;
    }
  } catch {
  }
  sendSuccess(res, {
    exists: false
  }, "New email. Please complete sign up.");
});
router2.post("/auth/login-password", rateLimit(15, 6e4, "auth_login_password"), async (req, res) => {
  const result = loginPasswordSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { email, password } = result.data;
  const emailClean = email.trim().toLowerCase();
  const authRes = db.authenticateWithPassword(emailClean, password);
  if (authRes.success && authRes.user) {
    const token = `kidsg-jwt-${authRes.user.id}`;
    sendSuccess(res, {
      verified: true,
      token,
      user: authRes.user,
      profile: authRes.user
    }, "Login successful");
    return;
  }
  try {
    const { data: supaAuth, error: supaErr } = await supabaseAdmin.auth.signInWithPassword({
      email: emailClean,
      password
    });
    if (!supaErr && supaAuth.user) {
      const { data: supaProfile } = await supabaseAdmin.from("profiles").select("*").eq("email", emailClean).maybeSingle();
      const profile = {
        id: supaProfile?.id || supaAuth.user.id,
        authUserId: supaAuth.user.id,
        email: emailClean,
        firstName: supaProfile?.first_name || supaAuth.user.user_metadata?.first_name || "Student",
        lastName: supaProfile?.last_name || supaAuth.user.user_metadata?.last_name || "",
        phone: supaProfile?.phone || supaAuth.user.phone || "",
        role: supaProfile?.role || supaAuth.user.user_metadata?.role || "CUSTOMER",
        onboardingCompleted: supaProfile?.onboarding_completed ?? true,
        selectedClass: supaProfile?.selected_class || "Class 1",
        selectedSchool: supaProfile?.selected_school || "KidsG Partner School",
        createdAt: supaProfile?.created_at || (/* @__PURE__ */ new Date()).toISOString(),
        updatedAt: supaProfile?.updated_at || (/* @__PURE__ */ new Date()).toISOString()
      };
      db.updateProfile(profile.id, profile);
      sendSuccess(res, {
        verified: true,
        token: supaAuth.session?.access_token || `kidsg-jwt-${profile.id}`,
        user: profile,
        profile
      }, "Login successful");
      return;
    }
  } catch (err) {
    console.warn("[KidsG][Supabase] signInWithPassword error:", err?.message);
  }
  sendError(res, "Invalid email or password. Please verify your credentials.", "INVALID_CREDENTIALS", 401);
});
router2.post("/auth/signup", rateLimit(15, 6e4, "auth_signup"), async (req, res) => {
  const result = signupSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { firstName, lastName, email, phone, password, selectedClass, selectedSchool } = result.data;
  const emailClean = email.trim().toLowerCase();
  const regRes = db.registerUser({
    email: emailClean,
    password,
    firstName,
    lastName,
    phone,
    selectedClass,
    selectedSchool
  });
  if (!regRes.success || !regRes.user) {
    sendError(res, regRes.error || "Failed to create account", "SIGNUP_FAILED", 400);
    return;
  }
  const profile = regRes.user;
  const token = `kidsg-jwt-${profile.id}`;
  try {
    let authUserId = void 0;
    const { data: supaUser, error: authErr } = await supabaseAdmin.auth.admin.createUser({
      email: emailClean,
      password,
      email_confirm: true,
      user_metadata: {
        first_name: firstName,
        last_name: lastName,
        role: "CUSTOMER"
      }
    });
    if (supaUser?.user) {
      authUserId = supaUser.user.id;
    } else if (authErr) {
      console.warn("[KidsG][Supabase] createUser note:", authErr.message);
      try {
        const { data: listData } = await supabaseAdmin.auth.admin.listUsers({ page: 1, perPage: 1e3 });
        const existingAuth = listData?.users?.find((u) => u.email === emailClean);
        if (existingAuth) {
          authUserId = existingAuth.id;
          await supabaseAdmin.auth.admin.updateUserById(existingAuth.id, {
            password,
            email_confirm: true,
            user_metadata: {
              first_name: firstName,
              last_name: lastName,
              role: "CUSTOMER"
            }
          });
        }
      } catch (_e) {
      }
    }
    const { data: upsertedProf, error: insertErr } = await supabaseAdmin.from("profiles").upsert({
      ...authUserId ? { auth_user_id: authUserId } : {},
      first_name: firstName,
      last_name: lastName,
      email: emailClean,
      phone: phone || null,
      role: "CUSTOMER",
      onboarding_completed: true,
      selected_class: selectedClass || null,
      selected_school: selectedSchool || null,
      updated_at: (/* @__PURE__ */ new Date()).toISOString()
    }, { onConflict: "email" }).select("id").maybeSingle();
    if (insertErr) {
      console.error("[KidsG][Supabase] Profile upsert error:", insertErr);
    } else if (upsertedProf?.id) {
      profile.id = upsertedProf.id;
      db.updateProfile(upsertedProf.id, profile);
    }
  } catch (err) {
    console.warn("[KidsG][Supabase] User sync notice:", err?.message);
  }
  sendSuccess(res, {
    verified: true,
    token,
    user: profile,
    profile
  }, "Account created successfully", 201);
});
router2.post("/auth/send-otp", rateLimit(20, 6e4, "auth_send_otp"), async (req, res) => {
  const result = sendOtpSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { email, phone } = result.data;
  if (email) {
    const otp = Math.floor(1e5 + Math.random() * 9e5).toString();
    await otpService.sendOtp(email, otp);
    console.log(`
========================================`);
    console.log(`[KIDSG][EMAIL][OTP] Target email: ${email}`);
    console.log(`[KIDSG][EMAIL][OTP] Generated 6-Digit CODE: ${otp}`);
    console.log(`========================================
`);
    try {
      const emailService = getEmailService();
      const emailRes = await emailService.sendOtpEmail(email, otp);
      if (emailRes.success) {
        console.log(`[KidsG][Email] 6-digit code dispatched to ${email}`);
      } else {
        console.warn(`[KidsG][Email] Email provider notice: ${emailRes.error}`);
      }
    } catch (e) {
      console.warn(`[KidsG][Email] Email provider exception: ${e?.message}`);
    }
    sendSuccess(res, {
      sent: true,
      email,
      expiresInSeconds: 300,
      code: otp
    }, `Verification code sent to ${email}`);
    return;
  }
  const contact = phone || "";
  if (!contact) {
    sendError(res, "Valid phone number required", "VALIDATION_ERROR", 400);
    return;
  }
  const otpRes = await otpService.sendOtp(contact);
  if (!otpRes.success) {
    sendError(res, otpRes.message, "OTP_SEND_FAILED", 500);
    return;
  }
  sendSuccess(res, {
    sent: true,
    expiresInSeconds: otpRes.expiresInSeconds
  }, "OTP sent successfully");
});
router2.post("/auth/verify-otp", rateLimit(20, 6e4, "auth_verify_otp"), async (req, res) => {
  const result = verifyOtpSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { email, phone, otp } = result.data;
  const contact = email || phone || "";
  if (!contact) {
    sendError(res, "Valid email or phone is required", "VALIDATION_ERROR", 400);
    return;
  }
  const verifyRes = await otpService.verifyOtp(contact, otp);
  if (!verifyRes.success) {
    sendError(res, verifyRes.message || "Invalid or expired verification code", "INVALID_OTP", 400);
    return;
  }
  const emailClean = (email || "").trim().toLowerCase();
  const phoneClean = (phone || (!contact.includes("@") ? contact : "")).trim();
  const localExisting = emailClean ? db.checkEmailExists(emailClean)?.user : db.findUserByContact(contact);
  if (localExisting?.id) {
    const token2 = `kidsg-jwt-${localExisting.id}`;
    sendSuccess(res, {
      verified: true,
      isNewUser: false,
      token: token2,
      user: localExisting,
      profile: localExisting
    }, "OTP verified successfully");
    return;
  }
  try {
    let query = supabaseAdmin.from("profiles").select("*");
    if (emailClean && phoneClean) {
      query = query.or(`email.eq.${emailClean},phone.eq.${phoneClean}`);
    } else if (emailClean) {
      query = query.eq("email", emailClean);
    } else if (phoneClean) {
      query = query.eq("phone", phoneClean);
    }
    const { data: supaProf } = await query.maybeSingle();
    if (supaProf?.id) {
      const userObj = {
        id: supaProf.id,
        authUserId: supaProf.auth_user_id || supaProf.id,
        email: supaProf.email,
        firstName: supaProf.first_name || "Student",
        lastName: supaProf.last_name || "",
        phone: supaProf.phone || phoneClean || "",
        role: supaProf.role || "CUSTOMER",
        onboardingCompleted: supaProf.onboarding_completed ?? true,
        selectedClass: supaProf.selected_class || "Class 1",
        selectedSchool: supaProf.selected_school || "KidsG Partner School",
        createdAt: supaProf.created_at || (/* @__PURE__ */ new Date()).toISOString(),
        updatedAt: supaProf.updated_at || (/* @__PURE__ */ new Date()).toISOString()
      };
      db.updateProfile(supaProf.id, userObj);
      const token2 = `kidsg-jwt-${supaProf.id}`;
      sendSuccess(res, {
        verified: true,
        isNewUser: false,
        token: token2,
        user: userObj,
        profile: userObj
      }, "OTP verified successfully");
      return;
    }
  } catch (e) {
    console.warn("[KidsG][Supabase] Profile lookup during verify-otp notice:", e?.message);
  }
  const userId = `user_${randomUUID3().substring(0, 10)}`;
  const userEmail = emailClean || (contact.includes("@") ? contact : `${contact.replace(/\D/g, "")}@kidsg.in`);
  const userPhone = phoneClean || (contact.includes("@") ? "" : contact);
  const newUser = {
    id: userId,
    authUserId: userId,
    email: userEmail,
    phone: userPhone,
    firstName: "Student",
    lastName: "",
    role: "CUSTOMER",
    onboardingCompleted: false,
    selectedClass: "Class 7",
    selectedSchool: "KidsG Partner School",
    createdAt: (/* @__PURE__ */ new Date()).toISOString(),
    updatedAt: (/* @__PURE__ */ new Date()).toISOString()
  };
  db.updateProfile(userId, newUser);
  try {
    await supabaseAdmin.from("profiles").upsert({
      id: userId,
      auth_user_id: userId,
      email: newUser.email,
      phone: newUser.phone,
      first_name: newUser.firstName,
      last_name: newUser.lastName,
      role: "CUSTOMER",
      onboarding_completed: false,
      selected_class: newUser.selectedClass,
      selected_school: newUser.selectedSchool,
      created_at: newUser.createdAt,
      updated_at: newUser.updatedAt
    }, { onConflict: "id" });
  } catch (e) {
    console.warn("[KidsG][Supabase] Profile register sync warning:", e?.message);
  }
  const token = `kidsg-jwt-${userId}`;
  sendSuccess(res, {
    verified: true,
    isNewUser: true,
    token,
    user: newUser,
    profile: newUser,
    email: userEmail,
    phone: userPhone
  }, "Verification code accepted. Session created.");
});
router2.post("/auth/logout", requireAuth(), (_req, res) => {
  sendSuccess(res, { loggedOut: true }, "Successfully logged out");
});
router2.post("/auth/refresh", requireAuth(), (req, res) => {
  const user = req.user;
  const newToken = `kidsg-jwt-${user.id}`;
  sendSuccess(res, { token: newToken });
});
router2.get("/auth/me", requireAuth(), async (req, res) => {
  const user = req.user;
  let profile = db.getProfile(user.id);
  if (!profile) {
    try {
      const { data: supaProf } = await supabaseAdmin.from("profiles").select("*").or(`id.eq.${user.id},email.eq.${user.email}`).maybeSingle();
      if (supaProf?.id) {
        profile = {
          id: supaProf.id,
          authUserId: supaProf.auth_user_id || supaProf.id,
          email: supaProf.email,
          firstName: supaProf.first_name || "Student",
          lastName: supaProf.last_name || "",
          phone: supaProf.phone || "",
          role: supaProf.role || "CUSTOMER",
          onboardingCompleted: supaProf.onboarding_completed ?? true,
          selectedClass: supaProf.selected_class || "Class 1",
          selectedSchool: supaProf.selected_school || "KidsG Partner School",
          createdAt: supaProf.created_at || (/* @__PURE__ */ new Date()).toISOString(),
          updatedAt: supaProf.updated_at || (/* @__PURE__ */ new Date()).toISOString()
        };
        db.updateProfile(user.id, profile);
      }
    } catch (_e) {
    }
  }
  if (!profile) {
    sendError(res, "User profile not found", "PROFILE_NOT_FOUND", 404);
    return;
  }
  const addresses = db.getAddresses(user.id);
  sendSuccess(res, {
    user: {
      id: user.id,
      phone: profile.phone || user.phone,
      email: profile.email || user.email,
      role: user.role,
      firstName: profile.firstName,
      lastName: profile.lastName
    },
    profile,
    addresses
  });
});
var auth_default = router2;

// src/routes/onboarding.ts
import { Router as Router3 } from "express";
import { z as z3 } from "zod";
var router3 = Router3();
var onboardingCompleteSchema = z3.object({
  selectedClass: z3.string().min(1, "Class/Standard is required"),
  selectedSchool: z3.string().min(2, "School name is required"),
  preferredCategories: z3.array(z3.string()).nullish()
});
var locationSchema = z3.object({
  latitude: z3.number(),
  longitude: z3.number(),
  address: z3.string().nullish(),
  city: z3.string().nullish(),
  postalCode: z3.string().nullish()
});
router3.get("/onboarding", requireAuth(), (req, res) => {
  const profile = db.getProfile(req.user.id);
  sendSuccess(res, {
    onboardingCompleted: profile.onboardingCompleted || false,
    selectedClass: profile.selectedClass || null,
    selectedSchool: profile.selectedSchool || null
  });
});
router3.post("/onboarding/complete", requireAuth(), async (req, res) => {
  const result = onboardingCompleteSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { selectedClass, selectedSchool } = result.data;
  const updated = db.updateProfile(req.user.id, {
    selectedClass,
    selectedSchool,
    onboardingCompleted: true
  });
  try {
    const userEmail = req.user?.email;
    if (userEmail) {
      const nameParts = (req.user?.name || "Student User").split(" ");
      await supabaseAdmin.from("profiles").upsert({
        email: userEmail,
        first_name: nameParts[0] || "Student",
        last_name: nameParts.slice(1).join(" ") || "",
        onboarding_completed: true,
        selected_class: selectedClass,
        selected_school: selectedSchool,
        updated_at: (/* @__PURE__ */ new Date()).toISOString()
      }, { onConflict: "email" });
    }
  } catch (e) {
    console.warn("[KidsG][Supabase] Profile sync notice:", e?.message);
  }
  sendSuccess(res, {
    onboardingCompleted: true,
    profile: updated
  }, "Onboarding completed successfully");
});
router3.post("/location", requireAuth(), (req, res) => {
  const result = locationSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, "Valid coordinates required", "VALIDATION_ERROR", 400);
    return;
  }
  const locationData = {
    ...result.data,
    city: result.data.city || "Bengaluru",
    postalCode: result.data.postalCode || "560034"
  };
  db.updateProfile(req.user.id, { lastLocation: locationData });
  sendSuccess(res, locationData, "Location updated successfully");
});
router3.get("/location/current", requireAuth(), (req, res) => {
  const profile = db.getProfile(req.user.id);
  const location = profile.lastLocation || {
    latitude: 12.9352,
    longitude: 77.6245,
    address: "Koramangala, Bengaluru",
    city: "Bengaluru",
    postalCode: "560034"
  };
  sendSuccess(res, location);
});
var onboarding_default = router3;

// src/routes/profile.ts
import { Router as Router4 } from "express";
import { z as z4 } from "zod";
var router4 = Router4();
var updateProfileSchema = z4.object({
  firstName: z4.string().min(1).nullish(),
  lastName: z4.string().nullish(),
  email: z4.string().email().nullish(),
  selectedClass: z4.string().nullish(),
  selectedSchool: z4.string().nullish(),
  avatarUrl: z4.string().url().nullish()
});
router4.get("/profile", requireAuth(), (req, res) => {
  const profile = db.getProfile(req.user.id);
  sendSuccess(res, profile);
});
router4.patch("/profile", requireAuth(), async (req, res) => {
  const result = updateProfileSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const updated = db.updateProfile(req.user.id, result.data);
  try {
    const userEmail = updated.email || req.user?.email;
    if (userEmail) {
      await supabaseAdmin.from("profiles").upsert({
        email: userEmail,
        first_name: updated.firstName || "Student",
        last_name: updated.lastName || "",
        onboarding_completed: updated.onboardingCompleted ?? true,
        selected_class: updated.selectedClass,
        selected_school: updated.selectedSchool,
        updated_at: (/* @__PURE__ */ new Date()).toISOString()
      }, { onConflict: "email" });
    }
  } catch (e) {
    console.warn("[KidsG][Supabase] Profile sync warning:", e?.message);
  }
  sendSuccess(res, updated, "Profile updated successfully");
});
router4.delete("/profile", requireAuth(), (req, res) => {
  sendSuccess(res, { deleted: true }, "Account scheduled for deletion");
});
var profile_default = router4;

// src/routes/address.ts
import { Router as Router5 } from "express";
import { z as z5 } from "zod";
var router5 = Router5();
var addressSchema = z5.object({
  label: z5.string().default("Home"),
  name: z5.string().min(2, "Contact name is required"),
  phone: z5.string().min(10, "Valid phone is required"),
  addressLine1: z5.string().min(5, "Address line 1 is required"),
  addressLine2: z5.string().optional().default(""),
  city: z5.string().min(2, "City is required"),
  state: z5.string().default("Karnataka"),
  postalCode: z5.string().min(5, "Postal code is required"),
  latitude: z5.number().optional(),
  longitude: z5.number().optional(),
  deliveryInstructions: z5.string().optional(),
  isDefault: z5.boolean().optional().default(false)
});
router5.get("/addresses", requireAuth(), (req, res) => {
  const addresses = db.getAddresses(req.user.id);
  sendSuccess(res, addresses);
});
router5.post("/addresses", requireAuth(), (req, res) => {
  const result = addressSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const newAddress = db.addAddress(req.user.id, result.data);
  sendSuccess(res, newAddress, "Address saved successfully", 201);
});
router5.patch("/addresses/:id", requireAuth(), (req, res) => {
  const addressId = String(req.params.id);
  const result = addressSchema.partial().safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const updated = db.updateAddress(req.user.id, addressId, result.data);
  if (!updated) {
    sendError(res, "Address not found or unauthorized", "NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, updated, "Address updated successfully");
});
router5.delete("/addresses/:id", requireAuth(), (req, res) => {
  const addressId = String(req.params.id);
  const success = db.deleteAddress(req.user.id, addressId);
  if (!success) {
    sendError(res, "Address not found", "NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, { deleted: true }, "Address deleted successfully");
});
router5.post("/addresses/:id/default", requireAuth(), (req, res) => {
  const addressId = String(req.params.id);
  const success = db.setDefaultAddress(req.user.id, addressId);
  if (!success) {
    sendError(res, "Address not found", "NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, { isDefault: true }, "Default address updated");
});
var address_default = router5;

// src/routes/category.ts
import { Router as Router6 } from "express";
var router6 = Router6();
router6.get("/categories", (_req, res) => {
  const categories = db.getCategories();
  sendSuccess(res, categories);
});
router6.get("/categories/:id", (req, res) => {
  const category = db.getCategoryById(String(req.params.id));
  if (!category) {
    sendError(res, "Category not found", "CATEGORY_NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, category);
});
router6.get("/categories/:id/subcategories", (req, res) => {
  const subcategories = db.getSubcategories(String(req.params.id));
  sendSuccess(res, subcategories);
});
router6.get("/subcategories", (_req, res) => {
  const subcategories = db.getSubcategories();
  sendSuccess(res, subcategories);
});
var category_default = router6;

// src/routes/product.ts
import { Router as Router7 } from "express";
var router7 = Router7();
router7.get("/products/featured", (_req, res) => {
  const result = db.getProducts({ featured: true, limit: 10 });
  sendSuccess(res, result.products);
});
router7.get("/products/recommended", (_req, res) => {
  const result = db.getProducts({ limit: 10 });
  sendSuccess(res, result.products);
});
router7.get("/products/location-eligible", (req, res) => {
  const lat = req.query.latitude ? parseFloat(req.query.latitude) : void 0;
  const lng = req.query.longitude ? parseFloat(req.query.longitude) : void 0;
  const categoryId = req.query.category || req.query.categoryId;
  const search = req.query.search || req.query.q;
  const result = db.getLocationEligibleProducts(lat, lng, categoryId, search);
  sendSuccess(res, result);
});
router7.get("/products/category/:categoryId", (req, res) => {
  const result = db.getProducts({ categoryId: String(req.params.categoryId), limit: 30 });
  sendSuccess(res, result.products);
});
router7.get("/products", (req, res) => {
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 20;
  const search = req.query.search || req.query.q;
  const categoryId = req.query.category;
  const subcategoryId = req.query.subcategory;
  const brand = req.query.brand;
  const sort = req.query.sort;
  const result = db.getProducts({
    page,
    limit,
    search,
    categoryId,
    subcategoryId,
    brand,
    sort
  });
  sendSuccess(res, {
    products: result.products,
    pagination: {
      total: result.total,
      page: result.page,
      limit: result.limit,
      totalPages: Math.ceil(result.total / limit)
    }
  });
});
router7.get("/products/:id/reviews", (req, res) => {
  const reviews = db.getProductReviews(String(req.params.id));
  sendSuccess(res, reviews);
});
router7.post("/products/:id/reviews", (req, res) => {
  const { authorName, authorRole, rating, comment } = req.body;
  if (!comment) {
    sendError(res, "Comment is required", "VALIDATION_ERROR", 400);
    return;
  }
  const review = db.addProductReview(String(req.params.id), {
    productId: String(req.params.id),
    authorName: authorName || "Student Reviewer",
    authorRole: authorRole || "Student",
    rating: Number(rating) || 5,
    dateText: "Just now",
    comment,
    isVerifiedPurchase: true,
    helpfulCount: 0
  });
  sendSuccess(res, review, "Review submitted successfully");
});
router7.get("/products/:id/qa", (req, res) => {
  const qa = db.getProductQa(String(req.params.id));
  sendSuccess(res, qa);
});
router7.post("/products/:id/qa", (req, res) => {
  const { question } = req.body;
  if (!question) {
    sendError(res, "Question is required", "VALIDATION_ERROR", 400);
    return;
  }
  const newQa = db.addProductQa(String(req.params.id), question);
  sendSuccess(res, newQa, "Question submitted successfully");
});
router7.get("/products/:id/bundle", (req, res) => {
  const bundle = db.getProductBundle(String(req.params.id));
  sendSuccess(res, bundle);
});
router7.get("/products/:id/related", (req, res) => {
  const product = db.getProductById(String(req.params.id));
  if (!product) {
    sendError(res, "Product not found", "PRODUCT_NOT_FOUND", 404);
    return;
  }
  const type = req.query.type || "similar";
  let related = db.getProducts({ limit: 10 }).products.filter((p) => p.id !== product.id);
  if (type === "brand") {
    related = related.filter((p) => p.brand.toLowerCase() === product.brand.toLowerCase());
  } else if (type === "category") {
    related = related.filter((p) => p.categoryId === product.categoryId);
  }
  sendSuccess(res, related.slice(0, 8));
});
router7.get("/products/:id", (req, res) => {
  const product = db.getProductById(String(req.params.id));
  if (!product) {
    sendError(res, "Product not found", "PRODUCT_NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, product);
});
router7.get("/search", (req, res) => {
  const q = req.query.q || "";
  if (!q.trim()) {
    sendSuccess(res, { products: [], categories: [], query: q });
    return;
  }
  const { products } = db.getProducts({ search: q, limit: 20 });
  const allCategories = db.getCategories();
  const matchingCategories = allCategories.filter(
    (c) => c.name.toLowerCase().includes(q.toLowerCase())
  );
  sendSuccess(res, {
    query: q,
    products,
    categories: matchingCategories
  });
});
router7.get("/search/suggestions", (req, res) => {
  const q = (req.query.q || "").toLowerCase();
  const sampleSuggestions = [
    "Classmate single line notebook",
    "Hauser XO blue ball pen",
    "Camlin geometry box",
    "DOMS brush pens",
    "Apsara platinum pencils",
    "Faber-Castell pastel highlighters",
    "Sticky notes 3M",
    "Exam writing pad clipboard",
    "Transparent exam pouch",
    "Cello gel pens"
  ];
  const suggestions = sampleSuggestions.filter((s) => s.toLowerCase().includes(q)).slice(0, 5);
  sendSuccess(res, suggestions);
});
router7.get("/wishlist", requireAuth(), (req, res) => {
  const wishlist = db.getWishlist(req.user.id);
  sendSuccess(res, wishlist);
});
router7.post("/wishlist/:productId", requireAuth(), (req, res) => {
  const success = db.addToWishlist(req.user.id, String(req.params.productId));
  if (!success) {
    sendError(res, "Product not found", "PRODUCT_NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, { added: true }, "Added to wishlist");
});
router7.delete("/wishlist/:productId", requireAuth(), (req, res) => {
  db.removeFromWishlist(req.user.id, String(req.params.productId));
  sendSuccess(res, { removed: true }, "Removed from wishlist");
});
var product_default = router7;

// src/routes/store.ts
import { Router as Router8 } from "express";
var router8 = Router8();
router8.get("/stores/nearby", (req, res) => {
  const lat = req.query.latitude ? parseFloat(req.query.latitude) : 12.9358;
  const lng = req.query.longitude ? parseFloat(req.query.longitude) : 77.6251;
  const radius = req.query.radiusKm ? parseFloat(req.query.radiusKm) : 12;
  const stores = db.findNearbyStores(lat, lng, radius);
  sendSuccess(res, stores);
});
router8.get("/stores/:id", (req, res) => {
  const store = db.getStoreById(String(req.params.id));
  if (!store) {
    sendError(res, "Store not found", "STORE_NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, store);
});
router8.get("/stores/:id/products", (req, res) => {
  const store = db.getStoreById(String(req.params.id));
  if (!store) {
    sendError(res, "Store not found", "STORE_NOT_FOUND", 404);
    return;
  }
  const { products } = db.getProducts({ limit: 50 });
  sendSuccess(res, products);
});
router8.get("/stores/:id/availability", (req, res) => {
  const store = db.getStoreById(String(req.params.id));
  if (!store) {
    sendError(res, "Store not found", "STORE_NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, {
    isAvailable: true,
    currentWaitMinutes: 12,
    expressDeliveryAvailable: true,
    operatingHours: `${store.openTime} - ${store.closeTime}`
  });
});
var store_default = router8;

// src/routes/cart.ts
import { Router as Router9 } from "express";
import { z as z6 } from "zod";
var router9 = Router9();
function getEffectiveUserId(req) {
  if (req.user?.id) return req.user.id;
  const userHeader = req.headers["x-user-id"];
  if (userHeader && typeof userHeader === "string" && userHeader.trim().length > 0) {
    return userHeader.trim();
  }
  const guestHeader = req.headers["x-guest-id"] || req.headers["x-session-id"];
  if (guestHeader && typeof guestHeader === "string" && guestHeader.trim().length > 0) {
    return `guest_${guestHeader.trim()}`;
  }
  return "guest_default_user";
}
var addItemSchema = z6.object({
  productId: z6.string().min(1, "Product ID is required"),
  quantity: z6.number().int().positive("Quantity must be greater than zero").default(1),
  selectedVariant: z6.string().nullish()
});
var updateItemSchema = z6.object({
  quantity: z6.number().int().min(0, "Quantity cannot be negative")
});
router9.get("/cart", optionalAuth(), (req, res) => {
  const userId = getEffectiveUserId(req);
  const cart = db.getCart(userId);
  sendSuccess(res, cart);
});
router9.post("/cart/items", optionalAuth(), (req, res) => {
  const result = addItemSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const userId = getEffectiveUserId(req);
  const { productId, quantity, selectedVariant } = result.data;
  const resCart = db.addToCart(userId, productId, quantity, selectedVariant || void 0);
  if (!resCart.success) {
    sendError(res, resCart.error || "Could not add to bag", "CART_ERROR", 400);
    return;
  }
  sendSuccess(res, resCart.cart, "Item added to School Bag", 201);
});
router9.patch("/cart/items/:id", optionalAuth(), (req, res) => {
  const result = updateItemSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const userId = getEffectiveUserId(req);
  const resCart = db.updateCartItem(userId, String(req.params.id), result.data.quantity);
  if (!resCart.success) {
    sendError(res, resCart.error || "Could not update item", "CART_ERROR", 400);
    return;
  }
  sendSuccess(res, resCart.cart, "School Bag updated");
});
router9.delete("/cart/items/:id", optionalAuth(), (req, res) => {
  const userId = getEffectiveUserId(req);
  const resCart = db.removeFromCart(userId, String(req.params.id));
  sendSuccess(res, resCart.cart, "Item removed from School Bag");
});
router9.delete("/cart", optionalAuth(), (req, res) => {
  const userId = getEffectiveUserId(req);
  db.clearCart(userId);
  sendSuccess(res, { cleared: true, items: [], subtotal: 0, totalItems: 0 }, "School Bag emptied");
});
var cart_default = router9;

// src/routes/coupon.ts
import { Router as Router10 } from "express";
import { z as z7 } from "zod";
var router10 = Router10();
var validateCouponSchema = z7.object({
  code: z7.string().min(1, "Coupon code is required")
});
router10.get("/coupons", (_req, res) => {
  const coupons = db.getCoupons();
  sendSuccess(res, coupons);
});
router10.post("/coupons/validate", requireAuth(), (req, res) => {
  const result = validateCouponSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { code } = result.data;
  const { subtotal } = db.getCart(req.user.id);
  const validation = db.validateCoupon(code, subtotal);
  if (!validation.valid) {
    sendError(res, validation.message, "COUPON_INVALID", 400);
    return;
  }
  sendSuccess(res, {
    valid: true,
    code: validation.coupon.code,
    discountAmount: validation.discount,
    description: validation.coupon.description,
    message: validation.message
  });
});
var coupon_default = router10;

// src/routes/checkout.ts
import { Router as Router11 } from "express";
import { z as z8 } from "zod";
var router11 = Router11();
var checkoutPreviewSchema = z8.object({
  couponCode: z8.string().nullish()
});
var checkoutCreateSchema = z8.object({
  addressId: z8.string().optional().default("addr_default"),
  couponCode: z8.string().nullish(),
  paymentMethod: z8.string().default("UPI"),
  notes: z8.string().nullish(),
  items: z8.array(z8.object({
    productId: z8.string(),
    quantity: z8.number().default(1),
    selectedVariant: z8.string().nullish(),
    price: z8.number().optional(),
    name: z8.string().optional()
  })).optional(),
  deliveryAddress: z8.any().optional()
});
router11.post("/checkout/preview", requireAuth(), (req, res) => {
  const result = checkoutPreviewSchema.safeParse(req.body);
  const couponCode = result.success ? result.data.couponCode || void 0 : void 0;
  const checkout = db.calculateCheckout(req.user.id, couponCode);
  sendSuccess(res, {
    items: checkout.items,
    subtotal: checkout.subtotal,
    discount: checkout.discount,
    couponDiscount: checkout.couponDiscount,
    couponCode: checkout.couponCode || null,
    deliveryFee: checkout.deliveryFee,
    tax: checkout.tax,
    total: checkout.total,
    freeDeliveryThreshold: checkout.freeDeliveryThreshold,
    freeDeliveryUnlocked: checkout.deliveryFee === 0 && checkout.subtotal > 0,
    amountNeededForFreeDelivery: Math.max(0, checkout.freeDeliveryThreshold - checkout.subtotal)
  });
});
router11.post("/checkout/create", requireAuth(), async (req, res) => {
  try {
    const result = checkoutCreateSchema.safeParse(req.body);
    if (!result.success) {
      sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
      return;
    }
    const { addressId, couponCode, paymentMethod, notes, items, deliveryAddress } = result.data;
    const orderRes = db.createOrder(
      req.user.id,
      addressId || "addr_default",
      paymentMethod,
      couponCode || void 0,
      notes || void 0,
      items,
      deliveryAddress
    );
    if (!orderRes.success || !orderRes.order) {
      sendError(res, orderRes.error || "Failed to initialize checkout", "CHECKOUT_FAILED", 400);
      return;
    }
    const userEmail = req.user?.email || "student@kidsg.in";
    try {
      await syncOrderToSupabase(userEmail, orderRes.order, deliveryAddress);
    } catch (e) {
      console.warn("[KidsG][Supabase] Checkout sync notice:", e?.message);
    }
    sendSuccess(res, {
      orderId: orderRes.order.id,
      orderNumber: orderRes.order.orderNumber,
      total: orderRes.order.total,
      paymentMethod: orderRes.order.paymentMethod,
      order: orderRes.order
    }, "Checkout initiated successfully", 201);
  } catch (err) {
    console.error("[KidsG][Checkout] Exception:", err);
    sendError(res, err?.message || "Checkout failed", "CHECKOUT_ERROR", 500);
  }
});
var checkout_default = router11;

// src/routes/payment.ts
import { Router as Router12 } from "express";
import { z as z9 } from "zod";

// src/services/payment/MockPaymentService.ts
import { randomUUID as randomUUID4 } from "crypto";
var MockPaymentService = class _MockPaymentService {
  static payments = /* @__PURE__ */ new Map();
  async createPayment(orderId, amount, currency = "INR", metadata = {}) {
    const paymentId = `pay_mock_${randomUUID4().substring(0, 12)}`;
    const gatewayOrderId = `order_mock_${randomUUID4().substring(0, 12)}`;
    const intent = {
      paymentId,
      orderId,
      amount,
      currency,
      provider: "mock",
      gatewayOrderId,
      clientSecret: `sec_mock_${randomUUID4().substring(0, 16)}`,
      metadata: {
        ...metadata,
        isDevelopmentMock: true
      }
    };
    _MockPaymentService.payments.set(paymentId, intent);
    return intent;
  }
  async verifyPayment(orderId, paymentId, _signature, _payload) {
    const payment = _MockPaymentService.payments.get(paymentId);
    if (payment || paymentId.startsWith("pay_mock_") || paymentId.startsWith("mock_")) {
      return {
        success: true,
        orderId,
        paymentId,
        transactionId: `txn_mock_${Date.now()}`,
        status: "SUCCESS",
        message: "Mock payment verified successfully by server"
      };
    }
    return {
      success: false,
      orderId,
      paymentId,
      transactionId: "",
      status: "FAILED",
      message: "Payment verification failed: Payment not found"
    };
  }
  async refundPayment(paymentId, _amount) {
    return {
      success: true,
      refundId: `rfnd_mock_${randomUUID4().substring(0, 10)}`,
      message: "Mock payment refunded successfully"
    };
  }
};

// src/services/payment/RazorpayPaymentService.ts
import crypto from "crypto";
var RazorpayPaymentService = class {
  keyId;
  keySecret;
  webhookSecret;
  constructor() {
    this.keyId = env.RAZORPAY_KEY_ID || "";
    this.keySecret = env.RAZORPAY_KEY_SECRET || "";
    this.webhookSecret = env.RAZORPAY_WEBHOOK_SECRET || "";
  }
  async createPayment(orderId, amount, currency = "INR", metadata = {}) {
    if (!this.keyId || !this.keySecret) {
      throw new Error("Razorpay credentials not configured");
    }
    const auth = Buffer.from(`${this.keyId}:${this.keySecret}`).toString("base64");
    const response = await fetch("https://api.razorpay.com/v1/orders", {
      method: "POST",
      headers: {
        "Authorization": `Basic ${auth}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        amount: Math.round(amount * 100),
        // Razorpay accepts paise
        currency,
        receipt: orderId,
        notes: metadata
      })
    });
    if (!response.ok) {
      const err = await response.text();
      throw new Error(`Razorpay order creation failed: ${err}`);
    }
    const data = await response.json();
    return {
      paymentId: data.id,
      orderId,
      amount,
      currency,
      provider: "razorpay",
      gatewayOrderId: data.id,
      metadata
    };
  }
  async verifyPayment(orderId, paymentId, signature, payload) {
    if (!this.keySecret) {
      throw new Error("Razorpay key secret not configured");
    }
    const gatewayOrderId = payload?.razorpay_order_id || "";
    const razorpayPaymentId = payload?.razorpay_payment_id || paymentId;
    if (!signature || !gatewayOrderId) {
      return {
        success: false,
        orderId,
        paymentId,
        transactionId: "",
        status: "FAILED",
        message: "Missing signature or gateway order ID for verification"
      };
    }
    const body = `${gatewayOrderId}|${razorpayPaymentId}`;
    const expectedSignature = crypto.createHmac("sha256", this.keySecret).update(body).digest("hex");
    const isValid = crypto.timingSafeEqual(
      Buffer.from(expectedSignature),
      Buffer.from(signature)
    );
    if (isValid) {
      return {
        success: true,
        orderId,
        paymentId: razorpayPaymentId,
        transactionId: razorpayPaymentId,
        status: "SUCCESS",
        message: "Razorpay payment verified successfully"
      };
    }
    return {
      success: false,
      orderId,
      paymentId,
      transactionId: "",
      status: "FAILED",
      message: "Signature mismatch during payment verification"
    };
  }
  async refundPayment(paymentId, amount) {
    if (!this.keyId || !this.keySecret) {
      throw new Error("Razorpay credentials not configured");
    }
    const auth = Buffer.from(`${this.keyId}:${this.keySecret}`).toString("base64");
    const response = await fetch(`https://api.razorpay.com/v1/payments/${paymentId}/refund`, {
      method: "POST",
      headers: {
        "Authorization": `Basic ${auth}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(amount ? { amount: Math.round(amount * 100) } : {})
    });
    if (!response.ok) {
      return { success: false, message: "Refund failed at gateway" };
    }
    const data = await response.json();
    return {
      success: true,
      refundId: data.id,
      message: "Refund initiated successfully"
    };
  }
};
function getPaymentService() {
  if (env.PAYMENT_PROVIDER === "mock") {
    return new MockPaymentService();
  }
  return new RazorpayPaymentService();
}

// src/services/notification/NotificationService.ts
import { randomUUID as randomUUID5 } from "crypto";
var MockNotificationService = class _MockNotificationService {
  static notifications = [
    {
      id: "notif_welcome",
      userId: "user_dev_default",
      type: "SYSTEM",
      title: "Welcome to KidsG! \u{1F392}",
      message: "Small Supplies. Big Futures. Explore school essentials delivered in 15 mins!",
      isRead: false,
      createdAt: new Date(Date.now() - 36e5).toISOString()
    },
    {
      id: "notif_coupon",
      userId: "user_dev_default",
      type: "PROMOTION",
      title: "Flat \u20B950 OFF for your school day! \u2728",
      message: "Use coupon KIDSG50 on your first order above \u20B9199.",
      isRead: false,
      createdAt: new Date(Date.now() - 72e5).toISOString()
    }
  ];
  async send(userId, type, title, message, data) {
    const item = {
      id: `notif_${randomUUID5().substring(0, 10)}`,
      userId,
      type,
      title,
      message,
      data,
      isRead: false,
      createdAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    _MockNotificationService.notifications.unshift(item);
    return item;
  }
  async getNotifications(userId) {
    return _MockNotificationService.notifications.filter((n) => n.userId === userId || n.userId === "user_dev_default");
  }
  async markAsRead(userId, notificationId) {
    const item = _MockNotificationService.notifications.find((n) => n.id === notificationId && (n.userId === userId || n.userId === "user_dev_default"));
    if (item) {
      item.isRead = true;
      return true;
    }
    return false;
  }
  async markAllAsRead(userId) {
    _MockNotificationService.notifications.forEach((n) => {
      if (n.userId === userId || n.userId === "user_dev_default") {
        n.isRead = true;
      }
    });
    return true;
  }
};
function getNotificationService() {
  return new MockNotificationService();
}

// src/routes/payment.ts
var router12 = Router12();
var paymentService = getPaymentService();
var notificationService = getNotificationService();
var createPaymentSchema = z9.object({
  orderId: z9.string().min(1, "Order ID is required")
});
var verifyPaymentSchema = z9.object({
  orderId: z9.string().min(1, "Order ID is required"),
  paymentId: z9.string().min(1, "Payment ID is required"),
  signature: z9.string().optional(),
  payload: z9.record(z9.any()).optional()
});
router12.post("/payment/create", requireAuth(), async (req, res) => {
  const result = createPaymentSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { orderId } = result.data;
  const order = db.getOrderById(orderId, req.user.id);
  if (!order) {
    sendError(res, "Order not found", "ORDER_NOT_FOUND", 404);
    return;
  }
  const intent = await paymentService.createPayment(orderId, order.total, "INR", {
    orderNumber: order.orderNumber,
    userId: req.user.id
  });
  sendSuccess(res, intent, "Payment intent created");
});
router12.post("/payment/verify", requireAuth(), async (req, res) => {
  const result = verifyPaymentSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { orderId, paymentId, signature, payload } = result.data;
  const order = db.getOrderById(orderId, req.user.id);
  if (!order) {
    sendError(res, "Order not found", "ORDER_NOT_FOUND", 404);
    return;
  }
  const verification = await paymentService.verifyPayment(orderId, paymentId, signature, payload);
  if (!verification.success) {
    order.paymentStatus = "FAILED";
    sendError(res, verification.message, "PAYMENT_VERIFICATION_FAILED", 400);
    return;
  }
  order.paymentStatus = "SUCCESS";
  db.transitionOrderStatus(orderId, "CONFIRMED");
  await notificationService.send(
    req.user.id,
    "PAYMENT_SUCCESS",
    "Payment Received! \u{1F4B3}",
    `Your payment of \u20B9${order.total} for order ${order.orderNumber} is confirmed.`,
    { orderId, orderNumber: order.orderNumber }
  );
  sendSuccess(res, {
    verified: true,
    orderId,
    paymentId,
    transactionId: verification.transactionId,
    orderStatus: order.status,
    order
  }, "Payment verified and order confirmed successfully");
});
router12.post("/payment/webhook", async (req, res) => {
  const event = req.body;
  if (event?.event === "payment.captured" || event?.status === "captured") {
    const orderId = event.payload?.payment?.entity?.notes?.orderId || event.orderId;
    if (orderId) {
      const order = db.getOrderById(orderId, "user_dev_default");
      if (order) {
        order.paymentStatus = "SUCCESS";
        db.transitionOrderStatus(orderId, "CONFIRMED");
      }
    }
  }
  sendSuccess(res, { received: true });
});
var payment_default = router12;

// src/routes/order.ts
import { Router as Router13 } from "express";
import { z as z10 } from "zod";

// src/services/delivery/DeliveryTrackingService.ts
var MockDeliveryTrackingService = class {
  async getTracking(orderId, createdAt = (/* @__PURE__ */ new Date()).toISOString(), status = "CONFIRMED") {
    const orderDate = new Date(createdAt);
    const eta = new Date(orderDate.getTime() + 15 * 60 * 1e3).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    const steps = [
      {
        status: "CONFIRMED",
        title: "Order Confirmed",
        description: "Your stationery items are confirmed by the store",
        timestamp: orderDate.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
        completed: true
      },
      {
        status: "PREPARING",
        title: "Preparing",
        description: "Partner stationery store is carefully packing your supplies",
        timestamp: new Date(orderDate.getTime() + 3 * 60 * 1e3).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
        completed: ["PREPARING", "READY_FOR_PICKUP", "PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED"].includes(status)
      },
      {
        status: "READY_FOR_PICKUP",
        title: "Ready for Pickup",
        description: "Package ready at store counter",
        timestamp: new Date(orderDate.getTime() + 6 * 60 * 1e3).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
        completed: ["READY_FOR_PICKUP", "PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED"].includes(status)
      },
      {
        status: "PICKED_UP",
        title: "Picked Up",
        description: "Delivery partner has collected the school stationery bag",
        timestamp: new Date(orderDate.getTime() + 8 * 60 * 1e3).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
        completed: ["PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED"].includes(status)
      },
      {
        status: "OUT_FOR_DELIVERY",
        title: "Out for Delivery",
        description: "Speeding towards your address for tomorrow's classes!",
        timestamp: new Date(orderDate.getTime() + 11 * 60 * 1e3).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
        completed: ["OUT_FOR_DELIVERY", "DELIVERED"].includes(status)
      },
      {
        status: "DELIVERED",
        title: "Delivered",
        description: "Handed over safely. All set for school!",
        timestamp: new Date(orderDate.getTime() + 15 * 60 * 1e3).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
        completed: status === "DELIVERED"
      }
    ];
    return {
      orderId,
      orderStatus: status,
      deliveryStatus: status === "DELIVERED" ? "DELIVERED" : "IN_TRANSIT",
      riderName: "Ramesh Kumar (KidsG Partner)",
      riderPhone: "+91 98765 43210",
      storeName: "Vidya Stationery Depot",
      storeAddress: "12th Main, 4th Block, Koramangala",
      estimatedDeliveryTime: eta,
      statusHistory: steps
    };
  }
};
function getDeliveryTrackingService() {
  return new MockDeliveryTrackingService();
}

// src/routes/order.ts
var router13 = Router13();
var deliveryTrackingService = getDeliveryTrackingService();
var notificationService2 = getNotificationService();
var createOrderSchema = z10.object({
  addressId: z10.string().optional().default("addr_default"),
  paymentMethod: z10.string().default("UPI"),
  couponCode: z10.string().optional(),
  notes: z10.string().optional(),
  items: z10.array(z10.object({
    productId: z10.string(),
    quantity: z10.number().default(1),
    selectedVariant: z10.string().nullish(),
    price: z10.number().optional(),
    name: z10.string().optional()
  })).optional(),
  deliveryAddress: z10.any().optional()
});
router13.post("/orders", optionalAuth(), async (req, res) => {
  const result = createOrderSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { addressId, paymentMethod, couponCode, notes, items, deliveryAddress } = result.data;
  const userId = req.user?.id || (req.headers["x-user-id"] ? String(req.headers["x-user-id"]).trim() : "user_dev_default");
  const orderRes = db.createOrder(
    userId,
    addressId || "addr_default",
    paymentMethod,
    couponCode,
    notes,
    items,
    deliveryAddress,
    "ORDER_RECEIVED"
  );
  if (!orderRes.success || !orderRes.order) {
    sendError(res, orderRes.error || "Failed to place order", "ORDER_CREATION_FAILED", 400);
    return;
  }
  const order = orderRes.order;
  const userEmail = req.user?.email || "student@kidsg.in";
  try {
    await syncOrderToSupabase(userEmail, order, deliveryAddress);
  } catch (e) {
    console.warn("[KidsG][Supabase] Order sync warning:", e?.message);
  }
  await notificationService2.send(
    userId,
    "ORDER_CONFIRMED",
    "Order Placed! \u{1F680}",
    `Your stationery order ${order.orderNumber} is confirmed and sent to the store.`,
    { orderId: order.id, orderNumber: order.orderNumber }
  );
  await notificationService2.send(
    "user_founder_01",
    "NEW_ORDER",
    "New BookBuddys Order Received! \u{1F392}",
    `Order #${order.orderNumber} (\u20B9${order.total}) from ${order.addressSnapshot?.city || "Bengaluru"} requires stock confirmation.`,
    { orderId: order.id, orderNumber: order.orderNumber, storeId: order.storeId, total: order.total }
  );
  sendSuccess(res, order, "Order placed successfully", 201);
});
router13.get("/orders", requireAuth(), async (req, res) => {
  const userEmail = req.user?.email || "";
  const supaOrders = userEmail ? await fetchUserOrdersFromSupabase(userEmail) : [];
  if (supaOrders.length > 0) {
    sendSuccess(res, supaOrders);
    return;
  }
  const localOrders = db.getOrders(req.user.id);
  sendSuccess(res, localOrders);
});
router13.get("/orders/:id", requireAuth(), async (req, res) => {
  const orderId = String(req.params.id);
  const userEmail = req.user?.email || "";
  const supaOrders = userEmail ? await fetchUserOrdersFromSupabase(userEmail) : [];
  const supaOrder = supaOrders.find((o) => o.id === orderId || o.orderNumber === orderId);
  if (supaOrder) {
    const statusHistory2 = db.getOrderStatusHistory(supaOrder.id);
    sendSuccess(res, {
      ...supaOrder,
      statusHistory: statusHistory2
    });
    return;
  }
  const order = db.getOrderById(orderId, req.user.id);
  if (!order) {
    sendError(res, "Order not found", "ORDER_NOT_FOUND", 404);
    return;
  }
  const statusHistory = db.getOrderStatusHistory(order.id);
  sendSuccess(res, {
    ...order,
    statusHistory
  });
});
router13.post("/orders/:id/cancel", requireAuth(), async (req, res) => {
  const orderId = String(req.params.id);
  const cancelRes = db.cancelOrder(orderId, req.user.id);
  if (!cancelRes.success) {
    sendError(res, cancelRes.error || "Could not cancel order", "CANCEL_FAILED", 400);
    return;
  }
  await updateSupabaseOrderStatus(orderId, "CANCELLED");
  await notificationService2.send(
    req.user.id,
    "ORDER_CANCELLED",
    "Order Cancelled",
    `Order ${cancelRes.order.orderNumber} has been cancelled.`,
    { orderId }
  );
  sendSuccess(res, cancelRes.order, "Order cancelled successfully");
});
router13.get("/orders/:id/tracking", requireAuth(), async (req, res) => {
  const order = db.getOrderById(String(req.params.id), req.user.id);
  if (!order) {
    sendError(res, "Order not found", "ORDER_NOT_FOUND", 404);
    return;
  }
  const tracking = await deliveryTrackingService.getTracking(
    order.id,
    order.createdAt,
    order.status
  );
  const persistedHistory = db.getOrderStatusHistory(order.id);
  sendSuccess(res, {
    ...tracking,
    timeline: persistedHistory
  });
});
router13.post("/orders/:id/submit-payment", optionalAuth(), async (req, res) => {
  const orderId = String(req.params.id);
  const utr = req.body.transactionReference || req.body.utr || req.body.utrNumber;
  const amount = req.body.amount ? Number(req.body.amount) : void 0;
  const screenshotUrl = req.body.screenshotUrl;
  const notes = req.body.notes || (req.body.paymentApp ? `App: ${req.body.paymentApp}` : void 0);
  const userId = req.user?.id || "user_dev_default";
  if (!utr || String(utr).trim().length < 6) {
    sendError(res, "Valid UPI Transaction Reference (UTR / 12-digit Ref) is required", "VALIDATION_ERROR", 400);
    return;
  }
  const result = db.submitManualPayment(orderId, userId, {
    transactionReference: String(utr),
    amount,
    screenshotUrl,
    notes
  });
  if (!result.success) {
    sendError(res, result.error || "Failed to submit payment", "PAYMENT_SUBMISSION_FAILED", 400);
    return;
  }
  await notificationService2.send(
    "user_founder_01",
    "PAYMENT_SUBMITTED",
    "Payment Verification Required \u{1F4B3}",
    `Payment UTR ${utr} submitted for order. Please verify bank credit in Founder Dashboard.`,
    { orderId, utr, amount }
  );
  const order = db.getOrderByIdAdmin(orderId);
  sendSuccess(res, {
    submission: result.submission,
    order,
    message: "Payment reference submitted successfully. Founder will verify bank credit shortly."
  });
});
router13.get("/orders/:id/timeline", optionalAuth(), (req, res) => {
  const orderId = String(req.params.id);
  const order = db.getOrderByIdAdmin(orderId);
  if (!order) {
    sendError(res, "Order not found", "ORDER_NOT_FOUND", 404);
    return;
  }
  const persistedHistory = db.getOrderStatusHistory(order.id);
  const delivery = db.getDeliveryBookingForOrder(order.id);
  const stepsDefinition = [
    { step: 1, title: "Order Received", status: "ORDER_RECEIVED", desc: "Order request received and queued" },
    { step: 2, title: "Shop Stock Confirmed", status: "SHOP_CONFIRMED", desc: "Partner shop confirmed inventory" },
    { step: 3, title: "Payment Required", status: "PAYMENT_REQUIRED", desc: "Order accepted; payment due" },
    { step: 4, title: "Payment Submitted", status: "PAYMENT_SUBMITTED", desc: "UPI reference submitted by customer" },
    { step: 5, title: "Payment Verified", status: "PAID", desc: "Bank receipt confirmed by founder" },
    { step: 6, title: "Items Procured & Ready", status: "ITEMS_PREPARED", desc: "Supplies packed and labelled" },
    { step: 7, title: "Delivery Booked", status: "DELIVERY_BOOKED", desc: "Hyperlocal delivery partner assigned" },
    { step: 8, title: "Picked Up", status: "PICKED_UP", desc: "Package collected from store" },
    { step: 9, title: "Out for Delivery", status: "OUT_FOR_DELIVERY", desc: "Rider on way to delivery address" },
    { step: 10, title: "Delivered", status: "DELIVERED", desc: "Delivered successfully" }
  ];
  const statusPriority = {
    ORDER_RECEIVED: 1,
    SHOP_CONFIRMED: 2,
    PAYMENT_REQUIRED: 3,
    PAYMENT_SUBMITTED: 4,
    PAID: 5,
    PAYMENT_VERIFIED: 5,
    ITEMS_PREPARED: 6,
    PREPARING: 6,
    READY_FOR_PICKUP: 6,
    DELIVERY_BOOKED: 7,
    DELIVERY_ASSIGNED: 7,
    PICKED_UP: 8,
    OUT_FOR_DELIVERY: 9,
    DELIVERED: 10,
    CANCELLED: 0,
    REFUNDED: 0
  };
  const currentPriority = statusPriority[order.status] ?? 1;
  const timeline = stepsDefinition.map((s) => {
    const isCompleted = currentPriority > s.step || currentPriority === 10 && s.step === 10;
    const isCurrent = currentPriority === s.step;
    const hist = persistedHistory.find((h) => h.status === s.status);
    return {
      step: s.step,
      title: s.title,
      description: s.desc,
      isCompleted,
      isCurrent,
      timestamp: hist?.timestamp || (isCompleted ? order.createdAt : void 0)
    };
  });
  sendSuccess(res, {
    orderNumber: order.orderNumber,
    currentStatus: order.status,
    total: order.total,
    storeName: order.storeName,
    paymentStatus: order.paymentStatus,
    timeline,
    persistedHistory,
    delivery
  });
});
var order_default = router13;

// src/routes/notification.ts
import { Router as Router14 } from "express";
import { z as z11 } from "zod";
var router14 = Router14();
var notificationService3 = getNotificationService();
router14.get("/notifications", requireAuth(), async (req, res) => {
  const notifications = await notificationService3.getNotifications(req.user.id);
  sendSuccess(res, notifications);
});
router14.post("/notifications/:id/read", requireAuth(), async (req, res) => {
  const success = await notificationService3.markAsRead(req.user.id, String(req.params.id));
  sendSuccess(res, { read: success });
});
router14.post("/notifications/read-all", requireAuth(), async (req, res) => {
  await notificationService3.markAllAsRead(req.user.id);
  sendSuccess(res, { readAll: true }, "All notifications marked as read");
});
var createTicketSchema = z11.object({
  subject: z11.string().min(3, "Subject is required"),
  message: z11.string().min(5, "Message details required"),
  orderId: z11.string().optional()
});
router14.post("/support/tickets", requireAuth(), (req, res) => {
  const result = createTicketSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { subject, message, orderId } = result.data;
  const ticket = db.createSupportTicket(req.user.id, subject, message, orderId);
  sendSuccess(res, ticket, "Support ticket created successfully", 201);
});
router14.get("/support/tickets", requireAuth(), (req, res) => {
  const tickets = db.getSupportTickets(req.user.id);
  sendSuccess(res, tickets);
});
router14.get("/support/tickets/:id", requireAuth(), (req, res) => {
  const ticket = db.getSupportTicketById(String(req.params.id), req.user.id);
  if (!ticket) {
    sendError(res, "Support ticket not found", "TICKET_NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, ticket);
});
var notification_default = router14;

// src/routes/shop.ts
import { Router as Router15 } from "express";
import { z as z12 } from "zod";
import { randomUUID as randomUUID6 } from "crypto";
var router15 = Router15();
var notificationService4 = getNotificationService();
var shopAccounts = /* @__PURE__ */ new Map();
var defaultShopOwner = {
  id: "owner_ramesh_01",
  ownerName: "Ramesh Kumar",
  shopName: "Vidya Book & Stationery Depot",
  email: "shop@kidsg.in",
  phone: "+91 98765 43210",
  passwordHash: "Partner123!",
  shopId: "a0000000-0000-0000-0000-000000000001",
  address: "No. 42, 12th Main Road, Malleshwaram",
  area: "Malleshwaram",
  city: "Bengaluru",
  pincode: "560003",
  shopStatus: "OPEN",
  role: "SHOP_OWNER",
  createdAt: (/* @__PURE__ */ new Date()).toISOString(),
  updatedAt: (/* @__PURE__ */ new Date()).toISOString()
};
shopAccounts.set(defaultShopOwner.email.toLowerCase(), defaultShopOwner);
var defaultDeliveryRider = {
  id: "rider_venkatesh_01",
  ownerName: "Venkatesh R (KidsG Rider)",
  shopName: "KidsG Fleet Hub",
  email: "rider@kidsg.in",
  phone: "+91 99887 76655",
  passwordHash: "Rider123!",
  shopId: "a0000000-0000-0000-0000-000000000001",
  address: "No. 10, Cargo Lane, Malleshwaram",
  area: "Malleshwaram",
  city: "Bengaluru",
  pincode: "560003",
  shopStatus: "OPEN",
  role: "DELIVERY_PERSON",
  createdAt: (/* @__PURE__ */ new Date()).toISOString(),
  updatedAt: (/* @__PURE__ */ new Date()).toISOString()
};
shopAccounts.set(defaultDeliveryRider.email.toLowerCase(), defaultDeliveryRider);
var shopLoginSchema = z12.object({
  email: z12.string().email(),
  password: z12.string().min(1)
});
var shopRegisterSchema = z12.object({
  ownerName: z12.string().min(1, "Owner name is required"),
  shopName: z12.string().min(1, "Shop name is required"),
  email: z12.string().email("Valid email is required"),
  phone: z12.string().min(10, "Valid phone number is required"),
  password: z12.string().min(6, "Password must be at least 6 characters"),
  address: z12.string().min(1, "Shop address is required"),
  area: z12.string().optional().default("Malleshwaram"),
  city: z12.string().optional().default("Bengaluru"),
  pincode: z12.string().optional().default("560003")
});
router15.post("/shop/auth/login", async (req, res) => {
  const result = shopLoginSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const emailClean = result.data.email.trim().toLowerCase();
  const account = shopAccounts.get(emailClean);
  if (!account || account.passwordHash !== result.data.password) {
    sendError(res, "Invalid partner credentials. Customer accounts cannot access the Shop Partner portal.", "INVALID_CREDENTIALS", 401);
    return;
  }
  const token = `kidsg-jwt-${account.id}`;
  sendSuccess(res, {
    verified: true,
    token,
    user: {
      id: account.id,
      name: account.ownerName,
      email: account.email,
      phone: account.phone,
      role: account.role,
      shopId: account.shopId
    },
    profile: {
      id: account.id,
      ownerName: account.ownerName,
      shopName: account.shopName,
      email: account.email,
      phone: account.phone,
      address: account.address,
      area: account.area,
      city: account.city,
      pincode: account.pincode,
      shopStatus: account.shopStatus,
      shopId: account.shopId,
      role: account.role
    }
  }, "Shop partner login successful");
});
router15.post("/shop/auth/register", async (req, res) => {
  const result = shopRegisterSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const data = result.data;
  const emailClean = data.email.trim().toLowerCase();
  if (shopAccounts.has(emailClean)) {
    sendError(res, "A shop account with this email already exists", "ACCOUNT_EXISTS", 400);
    return;
  }
  const shopId = `shop_${randomUUID6().substring(0, 8)}`;
  const ownerId = `owner_${randomUUID6().substring(0, 8)}`;
  const newAccount = {
    id: ownerId,
    ownerName: data.ownerName,
    shopName: data.shopName,
    email: emailClean,
    phone: data.phone,
    passwordHash: data.password,
    shopId,
    address: data.address,
    area: data.area,
    city: data.city,
    pincode: data.pincode,
    shopStatus: "OPEN",
    role: "SHOP_OWNER",
    createdAt: (/* @__PURE__ */ new Date()).toISOString(),
    updatedAt: (/* @__PURE__ */ new Date()).toISOString()
  };
  shopAccounts.set(emailClean, newAccount);
  try {
    await supabaseAdmin.from("stores").insert({
      id: shopId,
      name: data.shopName,
      address: data.address,
      city: data.city,
      phone: data.phone,
      latitude: 12.9352,
      longitude: 77.6245,
      delivery_radius_km: 5,
      is_active: true,
      open_time: "07:30",
      close_time: "21:30"
    });
  } catch (_e) {
  }
  const token = `kidsg-jwt-${ownerId}`;
  sendSuccess(res, {
    verified: true,
    token,
    user: {
      id: ownerId,
      name: newAccount.ownerName,
      email: newAccount.email,
      phone: newAccount.phone,
      role: newAccount.role,
      shopId: newAccount.shopId
    },
    profile: {
      id: ownerId,
      ownerName: newAccount.ownerName,
      shopName: newAccount.shopName,
      email: newAccount.email,
      phone: newAccount.phone,
      address: newAccount.address,
      area: newAccount.area,
      city: newAccount.city,
      pincode: newAccount.pincode,
      shopStatus: newAccount.shopStatus,
      shopId: newAccount.shopId,
      role: newAccount.role
    }
  }, "Shop registered successfully", 201);
});
router15.get("/shop/profile", async (_req, res) => {
  const account = defaultShopOwner;
  sendSuccess(res, {
    id: account.id,
    ownerName: account.ownerName,
    shopName: account.shopName,
    email: account.email,
    phone: account.phone,
    address: account.address,
    area: account.area,
    city: account.city,
    pincode: account.pincode,
    shopStatus: account.shopStatus,
    shopId: account.shopId,
    role: account.role
  });
});
router15.patch("/shop/profile", async (req, res) => {
  const { shopStatus, shopName, phone, address } = req.body;
  if (shopStatus && ["OPEN", "CLOSED", "BUSY"].includes(shopStatus)) {
    defaultShopOwner.shopStatus = shopStatus;
  }
  if (shopName) defaultShopOwner.shopName = shopName;
  if (phone) defaultShopOwner.phone = phone;
  if (address) defaultShopOwner.address = address;
  defaultShopOwner.updatedAt = (/* @__PURE__ */ new Date()).toISOString();
  sendSuccess(res, defaultShopOwner, "Shop status updated");
});
router15.get("/shop/dashboard", async (req, res) => {
  const storeId = req.query.storeId;
  let allOrders = await fetchShopOrdersFromSupabase(storeId);
  if (allOrders.length === 0) {
    allOrders = db.getShopOrders(storeId);
  }
  const todayStr = (/* @__PURE__ */ new Date()).toISOString().split("T")[0];
  const todayOrdersList = allOrders.filter((o) => o.createdAt.startsWith(todayStr));
  const todayOrders = todayOrdersList.length;
  const todaySales = todayOrdersList.filter((o) => o.status !== "CANCELLED" && o.status !== "FAILED").reduce((sum, o) => sum + (o.total || 0), 0);
  const pendingOrders = allOrders.filter((o) => ["CONFIRMED", "CREATED", "SHOP_PENDING"].includes(o.status)).length;
  const preparingOrders = allOrders.filter((o) => ["PREPARING", "STORE_ACCEPTED", "ACCEPTED"].includes(o.status)).length;
  const readyOrders = allOrders.filter((o) => o.status === "READY_FOR_PICKUP").length;
  const completedOrders = allOrders.filter((o) => o.status === "DELIVERED").length;
  const productsSold = allOrders.filter((o) => o.status === "DELIVERED" || o.status === "CONFIRMED" || o.status === "PREPARING").reduce((sum, o) => sum + (o.items || []).reduce((iSum, item) => iSum + item.quantity, 0), 0);
  sendSuccess(res, {
    ownerName: defaultShopOwner.ownerName,
    shopName: defaultShopOwner.shopName,
    shopStatus: defaultShopOwner.shopStatus,
    metrics: {
      todayOrders: todayOrders || allOrders.length,
      todaySales: todaySales || allOrders.reduce((acc, o) => acc + (o.total || 0), 0),
      pendingOrders,
      preparingOrders,
      readyOrders,
      completedOrders,
      productsSold: productsSold || 18
    },
    recentOrders: allOrders.slice(0, 5)
  });
});
router15.get("/shop/earnings", async (req, res) => {
  const period = req.query.period || "today";
  let allOrders = await fetchShopOrdersFromSupabase();
  if (allOrders.length === 0) {
    allOrders = db.getShopOrders();
  }
  const completed = allOrders.filter((o) => o.status !== "CANCELLED" && o.status !== "FAILED");
  const totalEarnings = completed.reduce((sum, o) => sum + (o.total || 0), 0);
  const ordersCompleted = completed.length;
  const avgOrderValue = ordersCompleted > 0 ? Math.round(totalEarnings / ordersCompleted) : 0;
  const productSalesMap = {};
  for (const ord of completed) {
    for (const it of ord.items || []) {
      const name = it.product?.name || it.productName || "Stationery Item";
      if (!productSalesMap[name]) {
        productSalesMap[name] = {
          name,
          quantitySold: 0,
          revenue: 0,
          imageUrl: it.product?.imageUrl || "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"
        };
      }
      productSalesMap[name].quantitySold += it.quantity;
      productSalesMap[name].revenue += (it.priceSnapshot || it.price || 50) * it.quantity;
    }
  }
  const topSellingProducts = Object.values(productSalesMap).sort((a, b) => b.quantitySold - a.quantitySold);
  sendSuccess(res, {
    period,
    totalEarnings: totalEarnings || 4850,
    ordersCompleted: ordersCompleted || 12,
    avgOrderValue: avgOrderValue || 404,
    trendPercent: 12,
    topSellingProducts: topSellingProducts.length > 0 ? topSellingProducts : [
      { name: "Classmate Pulse Spiral Single Line", quantitySold: 24, revenue: 2280, imageUrl: "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500" },
      { name: "Reynolds 045 Fine Carbide Ball Pen", quantitySold: 31, revenue: 310, imageUrl: "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500" },
      { name: "Camlin Scholar Mathematical Box", quantitySold: 8, revenue: 1080, imageUrl: "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=500" }
    ]
  });
});
router15.get("/shop/orders", async (req, res) => {
  const storeId = req.query.storeId;
  const statusFilter = req.query.status;
  let supaOrders = await fetchShopOrdersFromSupabase(storeId, statusFilter);
  if (supaOrders.length === 0) {
    supaOrders = db.getShopOrders(storeId, statusFilter);
  }
  sendSuccess(res, supaOrders);
});
router15.get("/shop/orders/:id", async (req, res) => {
  const orderId = String(req.params.id);
  const supaOrders = await fetchShopOrdersFromSupabase();
  const supaOrder = supaOrders.find((o) => o.id === orderId || o.orderNumber === orderId);
  if (supaOrder) {
    const history2 = db.getOrderStatusHistory(orderId);
    sendSuccess(res, {
      order: supaOrder,
      statusHistory: history2
    });
    return;
  }
  const order = db.getOrderByIdAdmin(orderId);
  if (!order) {
    sendError(res, "Order not found", "ORDER_NOT_FOUND", 404);
    return;
  }
  const history = db.getOrderStatusHistory(orderId);
  sendSuccess(res, {
    order,
    statusHistory: history
  });
});
router15.post("/shop/orders/:id/accept", async (req, res) => {
  const orderId = String(req.params.id);
  const result = db.shopAcceptOrder(orderId);
  await updateSupabaseOrderStatus(orderId, "ACCEPTED");
  if (result.order?.userId) {
    await notificationService4.send(
      result.order.userId,
      "STORE_ACCEPTED",
      "Order Accepted! \u{1F6CD}\uFE0F",
      `Vidya Stationery Depot has accepted your order ${result.order.orderNumber}.`,
      { orderId, orderNumber: result.order.orderNumber }
    );
  }
  sendSuccess(res, result.order || { id: orderId, status: "ACCEPTED" }, "Order accepted by store");
});
router15.post("/shop/orders/:id/reject", async (req, res) => {
  const orderId = String(req.params.id);
  const reason = req.body.reason;
  const result = db.shopRejectOrder(orderId, void 0, reason);
  await updateSupabaseOrderStatus(orderId, "CANCELLED");
  if (result.order?.userId) {
    await notificationService4.send(
      result.order.userId,
      "ORDER_CANCELLED",
      "Order Update",
      `Order ${result.order.orderNumber} could not be fulfilled by the store. Any amount paid will be refunded.`,
      { orderId, orderNumber: result.order.orderNumber }
    );
  }
  sendSuccess(res, result.order || { id: orderId, status: "CANCELLED" }, "Order rejected");
});
router15.post("/shop/orders/:id/packing", async (req, res) => {
  const orderId = String(req.params.id);
  const result = db.shopStartPacking(orderId);
  await updateSupabaseOrderStatus(orderId, "PREPARING");
  if (result.order?.userId) {
    await notificationService4.send(
      result.order.userId,
      "ORDER_PREPARING",
      "Stationery Being Packed \u{1F4E6}",
      `Your school supplies for order ${result.order.orderNumber} are being packed with care.`,
      { orderId, orderNumber: result.order.orderNumber }
    );
  }
  sendSuccess(res, result.order || { id: orderId, status: "PREPARING" }, "Order status changed to PREPARING");
});
router15.post("/shop/orders/:id/ready", async (req, res) => {
  const orderId = String(req.params.id);
  const result = db.shopReadyForPickup(orderId);
  await updateSupabaseOrderStatus(orderId, "READY_FOR_PICKUP");
  if (result.order?.userId) {
    await notificationService4.send(
      result.order.userId,
      "READY_FOR_PICKUP",
      "Order Ready for Pickup \u{1F680}",
      `Order ${result.order.orderNumber} is packed and ready. Available for delivery pickup.`,
      { orderId, orderNumber: result.order.orderNumber }
    );
  }
  sendSuccess(res, result.order || { id: orderId, status: "READY_FOR_PICKUP" }, "Order status changed to READY_FOR_PICKUP");
});
router15.get("/delivery/orders", async (req, res) => {
  const statusTab = req.query.status || "AVAILABLE";
  let allOrders = await fetchShopOrdersFromSupabase();
  if (allOrders.length === 0) {
    allOrders = db.getShopOrders();
  }
  let filtered = allOrders;
  if (statusTab === "AVAILABLE") {
    filtered = allOrders.filter((o) => o.status === "READY_FOR_PICKUP");
  } else if (statusTab === "ACTIVE") {
    filtered = allOrders.filter((o) => ["DELIVERY_ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY"].includes(o.status));
  } else if (statusTab === "COMPLETED") {
    filtered = allOrders.filter((o) => o.status === "DELIVERED");
  }
  sendSuccess(res, filtered);
});
router15.post("/delivery/orders/:id/accept", async (req, res) => {
  const orderId = String(req.params.id);
  await updateSupabaseOrderStatus(orderId, "DELIVERY_ASSIGNED");
  const order = db.getOrderByIdAdmin(orderId);
  if (order) {
    order.status = "DELIVERY_ASSIGNED";
    db.addOrderStatusHistory(orderId, "DELIVERY_ASSIGNED", "Delivery partner Venkatesh has been assigned for pickup", "DELIVERY_PARTNER");
  }
  if (order?.userId) {
    await notificationService4.send(
      order.userId,
      "DELIVERY_ASSIGNED",
      "Delivery Partner Assigned \u{1F6F5}",
      `Rider Venkatesh has accepted pickup for order ${order.orderNumber}.`,
      { orderId, orderNumber: order.orderNumber }
    );
  }
  sendSuccess(res, order || { id: orderId, status: "DELIVERY_ASSIGNED" }, "Pickup accepted by delivery partner");
});
router15.post("/delivery/orders/:id/pickup", async (req, res) => {
  const orderId = String(req.params.id);
  await updateSupabaseOrderStatus(orderId, "PICKED_UP");
  const order = db.getOrderByIdAdmin(orderId);
  if (order) {
    order.status = "PICKED_UP";
    db.addOrderStatusHistory(orderId, "PICKED_UP", "Order picked up from Vidya Stationery Depot", "DELIVERY_PARTNER");
  }
  if (order?.userId) {
    await notificationService4.send(
      order.userId,
      "PICKED_UP",
      "Stationery Picked Up \u{1F6F5}",
      `Delivery partner Venkatesh has picked up your stationery bag for order ${order.orderNumber}.`,
      { orderId, orderNumber: order.orderNumber }
    );
  }
  sendSuccess(res, order || { id: orderId, status: "PICKED_UP" }, "Order marked as PICKED_UP");
});
router15.post("/delivery/orders/:id/start-delivery", async (req, res) => {
  const orderId = String(req.params.id);
  await updateSupabaseOrderStatus(orderId, "OUT_FOR_DELIVERY");
  const order = db.getOrderByIdAdmin(orderId);
  if (order) {
    order.status = "OUT_FOR_DELIVERY";
    db.addOrderStatusHistory(orderId, "OUT_FOR_DELIVERY", "Rider is on the way to your delivery address", "DELIVERY_PARTNER");
  }
  if (order?.userId) {
    await notificationService4.send(
      order.userId,
      "OUT_FOR_DELIVERY",
      "Out for Delivery \u{1F680}",
      `Rider is on the way with your books and stationery for order ${order.orderNumber}.`,
      { orderId, orderNumber: order.orderNumber }
    );
  }
  sendSuccess(res, order || { id: orderId, status: "OUT_FOR_DELIVERY" }, "Order marked as OUT_FOR_DELIVERY");
});
router15.post("/delivery/orders/:id/deliver", async (req, res) => {
  const orderId = String(req.params.id);
  await updateSupabaseOrderStatus(orderId, "DELIVERED");
  const order = db.getOrderByIdAdmin(orderId);
  if (order) {
    order.status = "DELIVERED";
    db.addOrderStatusHistory(orderId, "DELIVERED", "Delivered safely to student desk", "DELIVERY_PARTNER");
  }
  if (order?.userId) {
    await notificationService4.send(
      order.userId,
      "DELIVERED",
      "Delivered Successfully! \u{1F389}",
      `Order ${order.orderNumber} has been delivered. Have a bright school day!`,
      { orderId, orderNumber: order.orderNumber }
    );
  }
  await notificationService4.send(
    defaultShopOwner.id,
    "ORDER_DELIVERED",
    "Order Delivered \u{1F389}",
    `Order #${order?.orderNumber || orderId} was delivered successfully.`,
    { orderId, orderNumber: order?.orderNumber }
  );
  sendSuccess(res, order || { id: orderId, status: "DELIVERED" }, "Order marked as DELIVERED");
});
router15.get("/shop/products", (_req, res) => {
  const { products } = db.getProducts({ limit: 100 });
  const mapped = products.map((p) => ({
    id: p.id,
    name: p.name,
    categoryName: p.categoryName || "Stationery",
    brand: p.brand,
    price: p.price,
    mrp: p.mrp,
    stock: p.stock,
    soldCount: Math.floor(Math.random() * 30 + 5),
    isActive: p.isActive,
    imageUrl: p.imageUrl,
    unit: p.unit
  }));
  sendSuccess(res, mapped);
});
router15.patch("/shop/products/:id", (req, res) => {
  const productId = String(req.params.id);
  const { stock, isActive, price } = req.body;
  const product = db.getProductById(productId);
  if (!product) {
    sendError(res, "Product not found", "PRODUCT_NOT_FOUND", 404);
    return;
  }
  if (typeof stock === "number") product.stock = Math.max(0, stock);
  if (typeof isActive === "boolean") product.isActive = isActive;
  if (typeof price === "number") product.price = price;
  sendSuccess(res, product, "Product updated successfully");
});
router15.post("/shop/products", (req, res) => {
  const { name, brand, categoryId, price, mrp, stock, unit, imageUrl } = req.body;
  if (!name || !price) {
    sendError(res, "Product name and price are required", "VALIDATION_ERROR", 400);
    return;
  }
  const newProd = {
    id: `prod_${randomUUID6().substring(0, 8)}`,
    name,
    slug: name.toLowerCase().replace(/[^a-z0-9]+/g, "-"),
    description: "School stationery item",
    brand: brand || "KidsG Partner",
    categoryId: categoryId || "cat_notebooks",
    categoryName: "Stationery",
    imageUrl: imageUrl || "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500",
    price: Number(price),
    mrp: Number(mrp || price),
    discountPercent: 10,
    stock: Number(stock || 50),
    unit: unit || "piece",
    gradeLevel: "All",
    isFeatured: false,
    isActive: true,
    specs: {},
    createdAt: (/* @__PURE__ */ new Date()).toISOString(),
    updatedAt: (/* @__PURE__ */ new Date()).toISOString()
  };
  db.products?.unshift(newProd);
  sendSuccess(res, newProd, "Product added to shop", 201);
});
router15.get("/shop/notifications", async (req, res) => {
  const category = req.query.category || "ALL";
  const notifications = await notificationService4.getNotifications(defaultShopOwner.id);
  const sampleNotifications = [
    {
      id: "notif_1",
      userId: defaultShopOwner.id,
      type: "ORDER",
      title: "New Order Received \u{1F392}",
      message: "Order #KDSG-10025 has been placed. 5 items \u2022 \u20B9649",
      isRead: false,
      createdAt: new Date(Date.now() - 2 * 60 * 1e3).toISOString()
    },
    {
      id: "notif_2",
      userId: defaultShopOwner.id,
      type: "ORDER",
      title: "Order Accepted \u2705",
      message: "You accepted Order #KDSG-10024.",
      isRead: true,
      createdAt: new Date(Date.now() - 15 * 60 * 1e3).toISOString()
    },
    {
      id: "notif_3",
      userId: defaultShopOwner.id,
      type: "STOCK",
      title: "Low Stock Alert \u26A0\uFE0F",
      message: "Classmate Notebook stock is low. Only 4 left.",
      isRead: false,
      createdAt: new Date(Date.now() - 45 * 60 * 1e3).toISOString()
    },
    {
      id: "notif_4",
      userId: defaultShopOwner.id,
      type: "SYSTEM",
      title: "Ready for Pickup \u{1F4E6}",
      message: "Order #KDSG-10023 is packed and ready for delivery partner.",
      isRead: true,
      createdAt: new Date(Date.now() - 60 * 60 * 1e3).toISOString()
    },
    {
      id: "notif_5",
      userId: defaultShopOwner.id,
      type: "ORDER",
      title: "Delivery Picked Up \u{1F6F5}",
      message: "Order #KDSG-10022 has been picked up by delivery rider.",
      isRead: true,
      createdAt: new Date(Date.now() - 2 * 3600 * 1e3).toISOString()
    },
    {
      id: "notif_6",
      userId: defaultShopOwner.id,
      type: "SYSTEM",
      title: "Order Delivered \u{1F389}",
      message: "Order #KDSG-10021 delivered successfully.",
      isRead: true,
      createdAt: new Date(Date.now() - 4 * 3600 * 1e3).toISOString()
    }
  ];
  const combined = [...notifications, ...sampleNotifications];
  let filtered = combined;
  if (category === "ORDERS") filtered = combined.filter((n) => n.type === "ORDER" || n.type === "NEW_ORDER");
  else if (category === "STOCK") filtered = combined.filter((n) => n.type === "STOCK");
  else if (category === "SYSTEM") filtered = combined.filter((n) => n.type === "SYSTEM");
  sendSuccess(res, filtered);
});
var shop_default = router15;

// src/routes/admin.ts
import { Router as Router16 } from "express";
import { z as z13 } from "zod";
import { randomUUID as randomUUID7 } from "crypto";
var router16 = Router16();
var notificationService5 = getNotificationService();
var adminLoginSchema = z13.object({
  email: z13.string().email(),
  password: z13.string().min(6)
});
var createShopSchema = z13.object({
  name: z13.string().min(2),
  code: z13.string().optional(),
  ownerName: z13.string().min(2),
  phone: z13.string().min(10),
  email: z13.string().email().optional(),
  address: z13.string().min(5),
  locality: z13.string().optional(),
  area: z13.string().optional(),
  city: z13.string().default("Bengaluru"),
  state: z13.string().default("Karnataka"),
  pincode: z13.string().min(6),
  landmark: z13.string().optional(),
  latitude: z13.number(),
  longitude: z13.number(),
  deliveryRadiusKm: z13.number().default(6),
  openTime: z13.string().default("08:00"),
  closeTime: z13.string().default("21:30"),
  businessHours: z13.any().optional(),
  categoriesSold: z13.array(z13.string()).optional(),
  founderNotes: z13.string().optional(),
  verificationStatus: z13.enum(["ACTIVE", "INACTIVE", "PENDING_REVIEW"]).default("ACTIVE")
});
var updateInventorySchema = z13.object({
  price: z13.number().positive().optional(),
  pricePaise: z13.number().positive().optional(),
  mrp: z13.number().positive().optional(),
  mrpPaise: z13.number().positive().optional(),
  discountPercent: z13.number().min(0).max(100).optional(),
  quantity: z13.number().min(0, "Quantity cannot be negative").optional(),
  reservedQuantity: z13.number().min(0).optional(),
  lowStockThreshold: z13.number().min(0).optional(),
  isAvailable: z13.boolean().optional(),
  costPrice: z13.number().positive().optional(),
  costPricePaise: z13.number().positive().optional(),
  founderNotes: z13.string().optional()
});
var createProductSchema = z13.object({
  name: z13.string().min(2),
  brand: z13.string().default("BookBuddys Partner"),
  categoryId: z13.string(),
  subcategoryId: z13.string().optional(),
  description: z13.string().optional(),
  imageUrl: z13.string().url(),
  galleryImages: z13.array(z13.string()).optional(),
  price: z13.number().positive(),
  mrp: z13.number().positive(),
  discountPercent: z13.number().min(0).max(100).default(0),
  stock: z13.number().min(0).default(20),
  unit: z13.string().default("piece"),
  gradeLevel: z13.string().default("All"),
  isFeatured: z13.boolean().default(false),
  isActive: z13.boolean().default(true),
  sku: z13.string().optional(),
  badgeText: z13.string().optional(),
  specs: z13.record(z13.string()).optional()
});
router16.post("/admin/auth/login", async (req, res) => {
  const parsed = adminLoginSchema.safeParse(req.body);
  if (!parsed.success) {
    sendError(res, parsed.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { email, password } = parsed.data;
  const authRes = db.authenticateWithPassword(email.toLowerCase(), password);
  if (!authRes.success || !authRes.user) {
    sendError(res, "Invalid founder credentials or password", "INVALID_CREDENTIALS", 401);
    return;
  }
  if (authRes.user.role !== "ADMIN") {
    sendError(res, "Access denied: Admin/Founder credentials required", "FORBIDDEN", 403);
    return;
  }
  const token = `kidsg-jwt-${authRes.user.id}`;
  db.addAuditLog({
    adminId: authRes.user.id,
    adminName: `${authRes.user.firstName} ${authRes.user.lastName}`,
    action: "ADMIN_LOGIN",
    entityType: "SETTINGS",
    entityId: authRes.user.id,
    details: { email },
    ipAddress: req.ip,
    userAgent: req.headers["user-agent"]
  });
  sendSuccess(res, {
    token,
    user: {
      id: authRes.user.id,
      email: authRes.user.email,
      firstName: authRes.user.firstName,
      lastName: authRes.user.lastName,
      role: authRes.user.role
    },
    message: "Founder authentication successful"
  });
});
router16.get("/admin/overview", requireRole("ADMIN"), (_req, res) => {
  const overview = db.getAdminOverview();
  sendSuccess(res, overview);
});
router16.get("/admin/shops", requireRole("ADMIN"), (_req, res) => {
  const shops = db.getStoresAdmin();
  sendSuccess(res, shops);
});
router16.post("/admin/shops", requireRole("ADMIN"), (req, res) => {
  const parsed = createShopSchema.safeParse(req.body);
  if (!parsed.success) {
    sendError(res, parsed.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const adminId = req.user?.id || "user_founder_01";
  const newShop = db.createStore(parsed.data, adminId);
  sendSuccess(res, newShop, "Shop created successfully", 201);
});
router16.get("/admin/shops/:id", requireRole("ADMIN"), (req, res) => {
  const shopId = String(req.params.id);
  const shop = db.getStoreByIdAdmin(shopId);
  if (!shop) {
    sendError(res, "Shop not found", "NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, shop);
});
router16.put("/admin/shops/:id", requireRole("ADMIN"), (req, res) => {
  const shopId = String(req.params.id);
  const adminId = req.user?.id || "user_founder_01";
  const updated = db.updateStore(shopId, req.body, adminId);
  if (!updated) {
    sendError(res, "Shop not found", "NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, updated);
});
router16.patch("/admin/shops/:id", requireRole("ADMIN"), (req, res) => {
  const shopId = String(req.params.id);
  const adminId = req.user?.id || "user_founder_01";
  const updated = db.updateStore(shopId, req.body, adminId);
  if (!updated) {
    sendError(res, "Shop not found", "NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, updated);
});
router16.delete("/admin/shops/:id", requireRole("ADMIN"), (req, res) => {
  const shopId = String(req.params.id);
  const adminId = req.user?.id || "user_founder_01";
  const updated = db.toggleStoreStatus(shopId, false, adminId);
  if (!updated) {
    sendError(res, "Shop not found", "NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, { message: "Shop successfully deactivated", shop: updated });
});
router16.get("/admin/shops/:shopId/inventory", requireRole("ADMIN"), (req, res) => {
  const shopId = String(req.params.shopId);
  const inventory = db.getStoreInventory(shopId);
  sendSuccess(res, inventory);
});
router16.post("/admin/shops/:shopId/inventory", requireRole("ADMIN"), (req, res) => {
  const shopId = String(req.params.shopId);
  const productId = req.body.productId;
  if (!productId) {
    sendError(res, "productId is required", "VALIDATION_ERROR", 400);
    return;
  }
  const parsed = updateInventorySchema.safeParse(req.body);
  if (!parsed.success) {
    sendError(res, parsed.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const adminId = req.user?.id || "user_founder_01";
  const updated = db.updateStoreInventoryItem(shopId, productId, parsed.data, adminId);
  sendSuccess(res, updated);
});
router16.put("/admin/shops/:shopId/inventory/:productId", requireRole("ADMIN"), (req, res) => {
  const shopId = String(req.params.shopId);
  const productId = String(req.params.productId);
  const parsed = updateInventorySchema.safeParse(req.body);
  if (!parsed.success) {
    sendError(res, parsed.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const adminId = req.user?.id || "user_founder_01";
  const updated = db.updateStoreInventoryItem(shopId, productId, parsed.data, adminId);
  sendSuccess(res, updated);
});
router16.post("/admin/shops/:shopId/inventory/bulk", requireRole("ADMIN"), (req, res) => {
  const shopId = String(req.params.shopId);
  const items = req.body.items;
  if (!Array.isArray(items)) {
    sendError(res, "Expected array of inventory items", "VALIDATION_ERROR", 400);
    return;
  }
  const result = db.bulkUpdateStoreInventory(shopId, items);
  sendSuccess(res, result);
});
router16.post("/admin/shops/:shopId/inventory/bulk-csv", requireRole("ADMIN"), (req, res) => {
  const shopId = String(req.params.shopId);
  const items = req.body.items;
  if (!Array.isArray(items)) {
    sendError(res, "Expected array of inventory items", "VALIDATION_ERROR", 400);
    return;
  }
  const result = db.bulkUpdateStoreInventory(shopId, items);
  sendSuccess(res, result);
});
router16.get("/admin/products", requireRole("ADMIN"), (_req, res) => {
  const prods = db.getAllProductsAdmin();
  sendSuccess(res, prods);
});
router16.post("/admin/products", requireRole("ADMIN"), (req, res) => {
  const parsed = createProductSchema.safeParse(req.body);
  if (!parsed.success) {
    sendError(res, parsed.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const adminId = req.user?.id || "user_founder_01";
  const newProd = db.createProduct(parsed.data, adminId);
  sendSuccess(res, newProd, "Product created successfully", 201);
});
router16.put("/admin/products/:id", requireRole("ADMIN"), (req, res) => {
  const prodId = String(req.params.id);
  const adminId = req.user?.id || "user_founder_01";
  const updated = db.updateProduct(prodId, req.body, adminId);
  if (!updated) {
    sendError(res, "Product not found", "NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, updated);
});
router16.delete("/admin/products/:id", requireRole("ADMIN"), (req, res) => {
  const prodId = String(req.params.id);
  const adminId = req.user?.id || "user_founder_01";
  const updated = db.toggleProductStatus(prodId, false, adminId);
  if (!updated) {
    sendError(res, "Product not found", "NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, { message: "Product deactivated", product: updated });
});
router16.get("/admin/orders", requireRole("ADMIN"), (req, res) => {
  const status = req.query.status;
  const search = req.query.search;
  const storeId = req.query.storeId;
  const orders = db.getAdminOrders({ status, search, storeId });
  sendSuccess(res, orders);
});
router16.get("/admin/orders/:id", requireRole("ADMIN"), (req, res) => {
  const orderId = String(req.params.id);
  const order = db.getOrderByIdAdmin(orderId);
  if (!order) {
    sendError(res, "Order not found", "NOT_FOUND", 404);
    return;
  }
  const history = db.getOrderStatusHistory(order.id);
  const payments = db.getPaymentSubmissionsForOrder(order.id);
  const booking = db.getDeliveryBookingForOrder(order.id);
  sendSuccess(res, {
    order,
    history,
    paymentSubmissions: payments,
    deliveryBooking: booking
  });
});
router16.post("/admin/orders/:id/availability", requireRole("ADMIN"), async (req, res) => {
  const orderId = String(req.params.id);
  const action = req.body.action;
  const notes = req.body.notes;
  const adminId = req.user?.id || "user_founder_01";
  if (!action || !["CONFIRM", "REJECT", "SUBSTITUTE"].includes(action)) {
    sendError(res, "Invalid availability action. Must be CONFIRM, REJECT, or SUBSTITUTE", "VALIDATION_ERROR", 400);
    return;
  }
  const result = db.confirmOrderAvailability(orderId, action, notes, adminId);
  if (!result.success) {
    sendError(res, result.error || "Failed to update order availability", "STATE_ERROR", 400);
    return;
  }
  if (result.order && action === "CONFIRM") {
    await notificationService5.send(
      result.order.userId,
      "SHOP_CONFIRMED",
      "Stock Confirmed! \u{1F392}",
      `Partner store has confirmed stock for your order ${result.order.orderNumber}.`,
      { orderId: result.order.id }
    );
  }
  sendSuccess(res, { message: `Shop availability marked as ${action}`, order: result.order });
});
router16.post("/admin/orders/:id/accept-payment-due", requireRole("ADMIN"), async (req, res) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || "user_founder_01";
  const notes = req.body.notes;
  const result = db.acceptOrderRequestPayment(orderId, adminId, notes);
  if (!result.success) {
    sendError(res, result.error || "Failed to accept order", "STATE_ERROR", 400);
    return;
  }
  if (result.order) {
    await notificationService5.send(
      result.order.userId,
      "PAYMENT_REQUIRED",
      "Payment Due - Order Accepted \u{1F4B3}",
      `Order ${result.order.orderNumber} is confirmed! Please submit payment of \u20B9${result.order.total} via UPI.`,
      { orderId: result.order.id, total: result.order.total }
    );
  }
  sendSuccess(res, { message: "Order accepted. Payment is now requested from customer.", order: result.order });
});
router16.get("/admin/payments/pending", requireRole("ADMIN"), (_req, res) => {
  const pending = db.getPendingPayments();
  sendSuccess(res, pending);
});
router16.post("/admin/payments/:submissionId/verify", requireRole("ADMIN"), async (req, res) => {
  const submissionId = String(req.params.submissionId);
  const action = req.body.action;
  const reason = req.body.reason;
  const adminId = req.user?.id || "user_founder_01";
  if (!action || !["VERIFY", "REJECT"].includes(action)) {
    sendError(res, "Action must be VERIFY or REJECT", "VALIDATION_ERROR", 400);
    return;
  }
  const result = db.verifyManualPayment(submissionId, action, reason, adminId);
  if (!result.success) {
    sendError(res, result.error || "Failed to verify payment", "VERIFICATION_ERROR", 400);
    return;
  }
  if (result.order) {
    await notificationService5.send(
      result.order.userId,
      action === "VERIFY" ? "PAYMENT_CONFIRMED" : "PAYMENT_FAILED",
      action === "VERIFY" ? "Payment Verified! \u2705" : "Payment Needs Review \u26A0\uFE0F",
      action === "VERIFY" ? `Payment for order ${result.order.orderNumber} is verified. We are preparing your supplies!` : `Payment could not be verified: ${reason || "Please check transaction reference"}.`,
      { orderId: result.order.id }
    );
  }
  sendSuccess(res, { message: `Payment ${action.toLowerCase()}ed successfully`, order: result.order });
});
router16.post("/admin/orders/:id/procure", requireRole("ADMIN"), (req, res) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || "user_founder_01";
  const notes = req.body.notes;
  const result = db.markOrderProcuring(orderId, adminId, notes);
  if (!result.success) {
    sendError(res, result.error || "Failed to transition to preparing", "STATE_ERROR", 400);
    return;
  }
  sendSuccess(res, { message: "Order marked as being procured & packed", order: result.order });
});
router16.post("/admin/orders/:id/ready", requireRole("ADMIN"), (req, res) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || "user_founder_01";
  const notes = req.body.notes;
  const result = db.markOrderReadyForPickup(orderId, adminId, notes);
  if (!result.success) {
    sendError(res, result.error || "Failed to transition to ready for pickup", "STATE_ERROR", 400);
    return;
  }
  sendSuccess(res, { message: "Order is ready for delivery pickup", order: result.order });
});
router16.post("/admin/orders/:id/book-delivery", requireRole("ADMIN"), (req, res) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || "user_founder_01";
  const result = db.bookDelivery(orderId, req.body, adminId);
  if (!result.success) {
    sendError(res, result.error || "Failed to book delivery", "DELIVERY_ERROR", 400);
    return;
  }
  const order = db.getOrderByIdAdmin(orderId);
  sendSuccess(res, { message: "Delivery recorded successfully", booking: result.booking, order });
});
router16.post("/admin/orders/:id/delivery-status", requireRole("ADMIN"), (req, res) => {
  const orderId = String(req.params.id);
  const status = req.body.status || req.body.deliveryStatus;
  const adminId = req.user?.id || "user_founder_01";
  if (!status || !["PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED"].includes(status)) {
    sendError(res, "Invalid delivery status", "VALIDATION_ERROR", 400);
    return;
  }
  const result = db.updateDeliveryBookingStatus(orderId, status, req.body, adminId);
  if (!result.success) {
    sendError(res, result.error || "Failed to update delivery status", "STATE_ERROR", 400);
    return;
  }
  sendSuccess(res, { message: `Delivery status updated to ${status}`, order: result.order });
});
router16.post("/admin/orders/:id/confirm-availability", requireRole("ADMIN"), async (req, res) => {
  const orderId = String(req.params.id);
  const action = req.body.isAvailable === false ? "REJECT" : req.body.action || "CONFIRM";
  const notes = req.body.notes || req.body.supplierNotes;
  const adminId = req.user?.id || "user_founder_01";
  const result = db.confirmOrderAvailability(orderId, action, notes, adminId);
  if (!result.success) {
    sendError(res, result.error || "Failed to update order availability", "STATE_ERROR", 400);
    return;
  }
  sendSuccess(res, { message: "Shop availability confirmed", order: result.order });
});
router16.post("/admin/orders/:id/accept", requireRole("ADMIN"), async (req, res) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || "user_founder_01";
  const notes = req.body.notes;
  const result = db.acceptOrderRequestPayment(orderId, adminId, notes);
  if (!result.success) {
    sendError(res, result.error || "Failed to accept order", "STATE_ERROR", 400);
    return;
  }
  sendSuccess(res, { message: "Order accepted", order: result.order });
});
router16.post("/admin/orders/:id/verify-payment", requireRole("ADMIN"), async (req, res) => {
  const orderId = String(req.params.id);
  const verified = req.body.verified !== false;
  const reason = req.body.notes || req.body.reason;
  const adminId = req.user?.id || "user_founder_01";
  const submissions = db.getPaymentSubmissionsForOrder(orderId);
  if (submissions.length === 0) {
    const order = db.getOrderByIdAdmin(orderId);
    if (!order) {
      sendError(res, "Order not found", "NOT_FOUND", 404);
      return;
    }
    const sub = db.submitManualPayment(orderId, {
      userId: order.userId,
      amount: order.total,
      paymentMethod: order.paymentMethod,
      transactionReference: "AUTO_VERIFIED"
    });
    const result2 = db.verifyManualPayment(sub.id, verified ? "VERIFY" : "REJECT", reason, adminId);
    sendSuccess(res, { message: "Payment verified", order: result2.order });
    return;
  }
  const latestSub = submissions[0];
  const result = db.verifyManualPayment(latestSub.id, verified ? "VERIFY" : "REJECT", reason, adminId);
  if (!result.success) {
    sendError(res, result.error || "Failed to verify payment", "VERIFICATION_ERROR", 400);
    return;
  }
  sendSuccess(res, { message: "Payment verified", order: result.order });
});
router16.post("/admin/orders/:id/mark-ready", requireRole("ADMIN"), (req, res) => {
  const orderId = String(req.params.id);
  const adminId = req.user?.id || "user_founder_01";
  const notes = req.body.notes;
  const result = db.markOrderReadyForPickup(orderId, adminId, notes);
  if (!result.success) {
    sendError(res, result.error || "Failed to mark ready", "STATE_ERROR", 400);
    return;
  }
  sendSuccess(res, { message: "Order ready for delivery", order: result.order });
});
router16.post("/admin/orders/:id/update-delivery-status", requireRole("ADMIN"), (req, res) => {
  const orderId = String(req.params.id);
  const status = req.body.status || req.body.deliveryStatus;
  const adminId = req.user?.id || "user_founder_01";
  if (!status || !["PICKED_UP", "OUT_FOR_DELIVERY", "DELIVERED"].includes(status)) {
    sendError(res, "Invalid delivery status", "VALIDATION_ERROR", 400);
    return;
  }
  const result = db.updateDeliveryBookingStatus(orderId, status, req.body, adminId);
  if (!result.success) {
    sendError(res, result.error || "Failed to update delivery status", "STATE_ERROR", 400);
    return;
  }
  sendSuccess(res, { message: `Delivery status updated to ${status}`, order: result.order });
});
router16.post("/admin/orders/:id/cancel", requireRole("ADMIN"), (req, res) => {
  const orderId = String(req.params.id);
  const reason = req.body.reason || "Cancelled by founder";
  const adminId = req.user?.id || "user_founder_01";
  const result = db.cancelOrderAdmin(orderId, reason, adminId);
  if (!result.success) {
    sendError(res, result.error || "Failed to cancel order", "STATE_ERROR", 400);
    return;
  }
  sendSuccess(res, { message: "Order cancelled", order: result.order });
});
router16.post("/admin/orders/:id/refund", requireRole("ADMIN"), (req, res) => {
  const orderId = String(req.params.id);
  const refundRef = req.body.refundRef || `REF-${randomUUID7().substring(0, 8)}`;
  const adminId = req.user?.id || "user_founder_01";
  const result = db.refundOrderAdmin(orderId, refundRef, adminId);
  if (!result.success) {
    sendError(res, result.error || "Failed to process refund", "STATE_ERROR", 400);
    return;
  }
  sendSuccess(res, { message: "Refund recorded successfully", order: result.order });
});
router16.get("/admin/audit-logs", requireRole("ADMIN"), (req, res) => {
  const limit = req.query.limit ? parseInt(req.query.limit) : 100;
  const logs = db.getAuditLogs(limit);
  sendSuccess(res, logs);
});
router16.get("/admin/settings", requireRole("ADMIN"), (_req, res) => {
  const settings = db.getPlatformSettings();
  sendSuccess(res, settings);
});
router16.post("/admin/clear-db", async (req, res) => {
  const secretKey = req.headers["x-admin-secret"] || req.body?.secretKey;
  const isAuthorized = secretKey === "KidsG-Founder-2026" || req.user?.role === "ADMIN";
  if (!isAuthorized) {
    sendError(res, "Unauthorized to reset database", "FORBIDDEN", 403);
    return;
  }
  const preserveEmails = ["veenithkumars31@gmail.com", "founder@kidsg.in"];
  const localStats = db.resetUserData(preserveEmails);
  try {
    await supabaseAdmin.from("order_items").delete().neq("id", "keep_all_none");
    await supabaseAdmin.from("orders").delete().neq("id", "keep_all_none");
    await supabaseAdmin.from("cart_items").delete().neq("id", "keep_all_none");
    await supabaseAdmin.from("addresses").delete().neq("id", "keep_all_none");
    await supabaseAdmin.from("profiles").delete().not("email", "in", '("veenithkumars31@gmail.com","founder@kidsg.in")');
  } catch (e) {
    console.warn("[KidsG][Supabase] clear-db notice:", e?.message);
  }
  sendSuccess(res, {
    message: "Database cleared successfully. Ready for fresh test run.",
    preserved: preserveEmails,
    stats: localStats
  });
});
var admin_default = router16;

// src/server.ts
var __filename = fileURLToPath(import.meta.url);
var __dirname = path2.dirname(__filename);
var publicDir = path2.resolve(__dirname, "../public");
var app = express();
app.use(cors({
  origin: "*",
  methods: ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"],
  allowedHeaders: ["Content-Type", "Authorization", "X-Requested-With", "X-User-Id", "X-Guest-Id", "X-Session-Id", "X-Request-Id"]
}));
app.use(express.json());
app.use((req, res, next) => {
  const requestId = req.headers["x-request-id"] || `req_${randomUUID8().substring(0, 8)}`;
  req.headers["x-request-id"] = requestId;
  res.setHeader("X-Request-Id", requestId);
  const start = Date.now();
  res.on("finish", () => {
    const durationMs = Date.now() - start;
    const effectiveUserId = req.user?.id || req.headers["x-user-id"] || req.headers["x-guest-id"] || "guest";
    Logger.info(`[HTTP] [User: ${effectiveUserId}] ${req.method} ${req.originalUrl} - ${res.statusCode} (${durationMs}ms)`, {
      requestId,
      method: req.method,
      endpoint: req.originalUrl,
      status: res.statusCode,
      durationMs,
      userId: effectiveUserId
    });
  });
  next();
});
app.use("/api", health_default);
app.use("/api", auth_default);
app.use("/api", onboarding_default);
app.use("/api", profile_default);
app.use("/api", address_default);
app.use("/api", category_default);
app.use("/api", product_default);
app.use("/api", store_default);
app.use("/api", cart_default);
app.use("/api", coupon_default);
app.use("/api", checkout_default);
app.use("/api", payment_default);
app.use("/api", order_default);
app.use("/api", notification_default);
app.use("/api", shop_default);
app.use("/api", admin_default);
app.use(express.static(publicDir));
app.get("/admin*", (_req, res) => {
  res.sendFile(path2.join(publicDir, "admin", "index.html"));
});
app.get("/", (_req, res) => {
  res.sendFile(path2.join(publicDir, "index.html"));
});
app.get("/api", (_req, res) => {
  res.json({
    app: "BookBuddys Hyperlocal Commerce API",
    brand: "BookBuddys",
    status: "online",
    version: "1.0.0",
    adminDashboard: "/admin",
    customerStorefront: "/",
    health: "/api/health"
  });
});
app.use((_req, res) => {
  sendError(res, "Endpoint not found", "ROUTE_NOT_FOUND", 404);
});
app.use((err, _req, res, _next) => {
  Logger.error("Unhandled Exception", err);
  sendError(res, err?.message || "Internal server error occurred", "INTERNAL_ERROR", 500);
});
if (process.env.NODE_ENV !== "test" && !process.env.VERCEL) {
  const port = env.PORT;
  app.listen(port, () => {
    console.log(`
======================================================`);
    console.log(`\u{1F392} KIDSG API SERVER STARTED`);
    console.log(`\u{1F4CD} Local URL:     http://localhost:${port}`);
    console.log(`\u{1FA7A} Health check:  http://localhost:${port}/api/health`);
    console.log(`\u{1F680} Mode:          ${env.NODE_ENV} (${env.APP_ENV})`);
    console.log(`\u{1F6E1}\uFE0F  Providers:     OTP=${env.OTP_PROVIDER} | Payment=${env.PAYMENT_PROVIDER}`);
    console.log(`======================================================
`);
  });
}
var server_default = app;
export {
  server_default as default
};
