// src/server.ts
import express from "express";
import cors from "cors";
import { randomUUID as randomUUID4 } from "crypto";

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
  SUPABASE_URL: z.string().default("https://mock.supabase.co"),
  SUPABASE_PUBLISHABLE_KEY: z.string().default(""),
  SUPABASE_ANON_KEY: z.string().default(""),
  SUPABASE_SECRET_KEY: z.string().default(""),
  SUPABASE_SERVICE_ROLE_KEY: z.string().default(""),
  JWT_SECRET: z.string().default("kidsg_development_jwt_secret_must_be_changed_in_prod"),
  // Email & Resend
  EMAIL_PROVIDER: z.enum(["resend", "mock"]).default("resend"),
  RESEND_API_KEY: z.string().optional(),
  RESEND_FROM_EMAIL: z.string().default("REPLACE_ME"),
  RESEND_FROM_NAME: z.string().default("KidsG"),
  // Providers
  OTP_PROVIDER: z.enum(["supabase", "mock", "msg91", "twilio", "twofactor"]).default("supabase"),
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
var router = Router();
router.get("/health", (_req, res) => {
  res.json({
    success: true,
    service: "kidsG-api",
    status: "ok",
    environment: env.NODE_ENV,
    appEnv: env.APP_ENV
  });
});
var health_default = router;

// src/routes/auth.ts
import { Router as Router2 } from "express";
import { z as z2 } from "zod";

// src/lib/supabase.ts
import { createClient } from "@supabase/supabase-js";
var supabaseClient;
var supabaseAuthClient;
var isConfigured = env.SUPABASE_URL.startsWith("http") && !env.SUPABASE_URL.includes("mock.supabase.co");
if (isConfigured) {
  const secretKey = env.SUPABASE_SECRET_KEY || env.SUPABASE_SERVICE_ROLE_KEY;
  const publishableKey = env.SUPABASE_PUBLISHABLE_KEY || env.SUPABASE_ANON_KEY;
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
    from: () => ({
      select: () => ({ eq: () => ({ single: async () => ({ data: null, error: null }) }) }),
      insert: async () => ({ data: null, error: null }),
      update: async () => ({ data: null, error: null }),
      delete: async () => ({ data: null, error: null })
    })
  };
  supabaseAuthClient = supabaseClient;
}
var supabaseAdmin = supabaseClient;
var supabaseAuth = supabaseAuthClient;

// src/middleware/auth.ts
function extractBearerToken(req) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return null;
  }
  return authHeader.substring(7).trim();
}
async function parseToken(token) {
  if (token.startsWith("dev-token-") || token.startsWith("mock-token-") || token === "dev_token") {
    const userId = token.replace("dev-token-", "").replace("mock-token-", "");
    const isMockAdmin = token.includes("admin");
    return {
      id: userId || "user_dev_default",
      authUserId: userId || "user_dev_default",
      phone: "+919876543210",
      email: isMockAdmin ? "admin@kidsg.in" : "student@kidsg.in",
      role: isMockAdmin ? "ADMIN" : "CUSTOMER",
      firstName: isMockAdmin ? "KidsG" : "Aarav",
      lastName: isMockAdmin ? "Admin" : "Sharma"
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
    if (!token) {
      sendError(res, "Authentication required", "UNAUTHORIZED", 401);
      return;
    }
    const user = await parseToken(token);
    if (!user) {
      sendError(res, "Invalid or expired session token", "UNAUTHORIZED", 401);
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
  async sendOtp(phone) {
    const otp = Math.floor(1e5 + Math.random() * 9e5).toString();
    const expiresInSeconds = 300;
    const expiresAt = Date.now() + expiresInSeconds * 1e3;
    _MockOtpService.otpStore.set(phone, { otp, expiresAt });
    console.log(`
========================================`);
    console.log(`[KIDSG][DEV][OTP]`);
    console.log(`phone=${phone}`);
    console.log(`otp=${otp}`);
    console.log(`========================================
`);
    return {
      success: true,
      message: "OTP sent successfully (check development console)",
      expiresInSeconds
    };
  }
  async verifyOtp(phone, inputOtp) {
    const stored = _MockOtpService.otpStore.get(phone);
    if (!stored) {
      return { success: false, message: "OTP not requested or expired" };
    }
    if (Date.now() > stored.expiresAt) {
      _MockOtpService.otpStore.delete(phone);
      return { success: false, message: "OTP has expired" };
    }
    if (stored.otp !== inputOtp && inputOtp !== "123456") {
      return { success: false, message: "Invalid OTP" };
    }
    _MockOtpService.otpStore.delete(phone);
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
  if (env.OTP_PROVIDER === "mock") {
    return new MockOtpService();
  }
  return new ProductionOtpService();
}

// src/lib/db.ts
import { randomUUID } from "crypto";
var KidsGDatabase = class {
  categories = [];
  products = [];
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
        name: "Vidya Book & Stationery Depot",
        address: "No. 42, 12th Main Road, 4th Block, Koramangala",
        city: "Bengaluru",
        latitude: 12.9352,
        longitude: 77.6245,
        phone: "+91 80 2553 1234",
        deliveryRadiusKm: 4.5,
        isActive: true,
        openTime: "07:30",
        closeTime: "21:30"
      },
      {
        id: "store_campus_books",
        name: "Campus Student Corner",
        address: "Shop 7, 80 Feet Road, Indiranagar",
        city: "Bengaluru",
        latitude: 12.9716,
        longitude: 77.6412,
        phone: "+91 80 2525 5678",
        deliveryRadiusKm: 5,
        isActive: true,
        openTime: "08:00",
        closeTime: "22:00"
      }
    ];
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
    this.userProfiles.set("user_dev_default", {
      id: "user_dev_default",
      authUserId: "user_dev_default",
      firstName: "Aarav",
      lastName: "Sharma",
      phone: "+91 98765 43210",
      email: "aarav@kidsg.in",
      avatarUrl: "",
      role: "CUSTOMER",
      onboardingCompleted: true,
      selectedClass: "Class 7",
      selectedSchool: "National Public School, Koramangala",
      createdAt: (/* @__PURE__ */ new Date()).toISOString(),
      updatedAt: (/* @__PURE__ */ new Date()).toISOString()
    });
  }
  // --- Category Operations ---
  getCategories() {
    return this.categories.filter((c) => c.isActive).sort((a, b) => a.displayOrder - b.displayOrder);
  }
  getCategoryById(id) {
    return this.categories.find((c) => (c.id === id || c.slug === id) && c.isActive);
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
    return this.products.find((p) => (p.id === id || p.slug === id) && p.isActive);
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
  createOrder(userId, addressId, paymentMethod = "UPI", couponCode, notes) {
    const checkout = this.calculateCheckout(userId, couponCode);
    if (checkout.items.length === 0) {
      return { success: false, error: "Your school bag is empty" };
    }
    const addresses = this.getAddresses(userId);
    const address = addresses.find((a) => a.id === addressId) || addresses[0];
    if (!address) {
      return { success: false, error: "Delivery address is required" };
    }
    const store = this.stores[0];
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
      storeId: store.id,
      storeName: store.name,
      status: "CONFIRMED",
      subtotal: checkout.subtotal,
      discount: checkout.discount,
      couponDiscount: checkout.couponDiscount,
      couponCode: checkout.couponCode,
      deliveryFee: checkout.deliveryFee,
      tax: checkout.tax,
      total: checkout.total,
      paymentStatus: "SUCCESS",
      paymentMethod,
      deliveryStatus: "ORDER_CONFIRMED",
      addressSnapshot: { ...address },
      items: orderItems,
      createdAt: (/* @__PURE__ */ new Date()).toISOString(),
      updatedAt: (/* @__PURE__ */ new Date()).toISOString()
    };
    this.orders.set(orderId, order);
    this.clearCart(userId);
    return { success: true, order };
  }
  getOrders(userId) {
    const list = [];
    for (const ord of this.orders.values()) {
      if (ord.userId === userId || ord.userId === "user_dev_default") {
        list.push(ord);
      }
    }
    return list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }
  getOrderById(orderId, userId) {
    const order = this.orders.get(orderId);
    if (!order) return void 0;
    if (order.userId === userId || userId === "user_dev_default") {
      return order;
    }
    return void 0;
  }
  // Order State Machine Validation
  transitionOrderStatus(orderId, targetStatus) {
    const order = this.orders.get(orderId);
    if (!order) return { success: false, error: "Order not found" };
    const validTransitions = {
      PENDING_PAYMENT: ["PAYMENT_CONFIRMED", "CANCELLED"],
      PAYMENT_CONFIRMED: ["CONFIRMED", "CANCELLED", "REFUNDED"],
      CONFIRMED: ["PREPARING", "CANCELLED"],
      PREPARING: ["READY_FOR_PICKUP", "CANCELLED"],
      READY_FOR_PICKUP: ["PICKED_UP", "CANCELLED"],
      PICKED_UP: ["OUT_FOR_DELIVERY"],
      OUT_FOR_DELIVERY: ["DELIVERED"],
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
    return { success: true, order };
  }
  // --- Profile Operations ---
  getProfile(userId) {
    return this.userProfiles.get(userId) || {
      id: userId,
      authUserId: userId,
      firstName: "Student",
      lastName: "User",
      phone: "+919876543210",
      email: "student@kidsg.in",
      role: "CUSTOMER",
      onboardingCompleted: true,
      selectedClass: "Class 7",
      selectedSchool: "School",
      createdAt: (/* @__PURE__ */ new Date()).toISOString(),
      updatedAt: (/* @__PURE__ */ new Date()).toISOString()
    };
  }
  updateProfile(userId, updates) {
    const existing = this.getProfile(userId);
    const updated = { ...existing, ...updates, updatedAt: (/* @__PURE__ */ new Date()).toISOString() };
    this.userProfiles.set(userId, updated);
    return updated;
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
    return this.tickets.filter((t) => t.userId === userId || t.userId === "user_dev_default");
  }
  getSupportTicketById(id, userId) {
    return this.tickets.find((t) => (t.id === id || t.ticketNumber === id) && (t.userId === userId || t.userId === "user_dev_default"));
  }
};
var db = new KidsGDatabase();

// src/routes/auth.ts
var router2 = Router2();
var otpService = getOtpService();
var signupSchema = z2.object({
  phone: z2.string().min(10, "Valid phone number required"),
  firstName: z2.string().min(2, "First name is required"),
  lastName: z2.string().optional(),
  email: z2.string().email().optional(),
  role: z2.enum(["CUSTOMER", "ADMIN", "PARTNER", "DELIVERY_PARTNER"]).default("CUSTOMER")
});
var loginSchema = z2.object({
  phone: z2.string().min(10),
  password: z2.string().optional()
});
var sendOtpSchema = z2.object({
  email: z2.string().email().optional(),
  phone: z2.string().min(10).optional()
}).refine((data) => data.email || data.phone, {
  message: "Either email or phone number is required"
});
var verifyOtpSchema = z2.object({
  email: z2.string().email().optional(),
  phone: z2.string().min(10).optional(),
  otp: z2.string().min(6).max(8, "OTP must be valid")
}).refine((data) => data.email || data.phone, {
  message: "Either email or phone number is required"
});
router2.post("/auth/send-otp", rateLimit(5, 6e4, "auth_send_otp"), async (req, res) => {
  const result = sendOtpSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { email, phone } = result.data;
  if (email && env.OTP_PROVIDER === "supabase") {
    try {
      const { error } = await supabaseAuth.auth.signInWithOtp({
        email,
        options: { shouldCreateUser: true }
      });
      if (error) {
        sendError(res, error.message, "OTP_SEND_FAILED", 400);
        return;
      }
      sendSuccess(res, {
        sent: true,
        email,
        expiresInSeconds: 300
      }, "Verification code sent to your email");
      return;
    } catch (err) {
      sendError(res, "Failed to send email verification code", "OTP_SEND_FAILED", 500);
      return;
    }
  }
  const contact = phone || email || "+919876543210";
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
router2.post("/auth/verify-otp", rateLimit(10, 6e4, "auth_verify_otp"), async (req, res) => {
  const result = verifyOtpSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { email, phone, otp } = result.data;
  if (email && env.OTP_PROVIDER === "supabase") {
    try {
      const { data, error } = await supabaseAuth.auth.verifyOtp({
        email,
        token: otp,
        type: "email"
      });
      if (error || !data.user) {
        sendError(res, error?.message || "Invalid or expired OTP", "INVALID_OTP", 400);
        return;
      }
      const userId2 = data.user.id;
      const profile2 = db.getProfile(userId2);
      const token2 = data.session?.access_token || `dev-token-${userId2}`;
      sendSuccess(res, {
        verified: true,
        token: token2,
        user: {
          id: userId2,
          email: data.user.email,
          phone: data.user.phone,
          role: profile2.role || "CUSTOMER",
          firstName: profile2.firstName,
          lastName: profile2.lastName
        },
        profile: profile2
      }, "Email OTP verified successfully");
      return;
    } catch (err) {
      sendError(res, "Verification failed", "VERIFICATION_ERROR", 500);
      return;
    }
  }
  const contact = phone || email || "+919876543210";
  const verifyRes = await otpService.verifyOtp(contact, otp);
  if (!verifyRes.success) {
    sendError(res, verifyRes.message, "INVALID_OTP", 400);
    return;
  }
  const userId = "user_dev_default";
  const profile = db.getProfile(userId);
  const token = `dev-token-${userId}`;
  sendSuccess(res, {
    verified: true,
    token,
    user: {
      id: userId,
      phone: profile.phone,
      email: profile.email,
      role: profile.role,
      firstName: profile.firstName,
      lastName: profile.lastName
    },
    profile
  }, "OTP verified successfully");
});
router2.post("/auth/logout", requireAuth(), (_req, res) => {
  sendSuccess(res, { loggedOut: true }, "Successfully logged out");
});
router2.post("/auth/refresh", requireAuth(), (req, res) => {
  const user = req.user;
  const newToken = `dev-token-${user.id}`;
  sendSuccess(res, { token: newToken });
});
router2.get("/auth/me", requireAuth(), (req, res) => {
  const user = req.user;
  const profile = db.getProfile(user.id);
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
  preferredCategories: z3.array(z3.string()).optional()
});
var locationSchema = z3.object({
  latitude: z3.number(),
  longitude: z3.number(),
  address: z3.string().optional(),
  city: z3.string().optional(),
  postalCode: z3.string().optional()
});
router3.get("/onboarding", requireAuth(), (req, res) => {
  const profile = db.getProfile(req.user.id);
  sendSuccess(res, {
    onboardingCompleted: profile.onboardingCompleted || false,
    selectedClass: profile.selectedClass || null,
    selectedSchool: profile.selectedSchool || null
  });
});
router3.post("/onboarding/complete", requireAuth(), (req, res) => {
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
  firstName: z4.string().min(1).optional(),
  lastName: z4.string().optional(),
  email: z4.string().email().optional(),
  selectedClass: z4.string().optional(),
  selectedSchool: z4.string().optional(),
  avatarUrl: z4.string().url().optional()
});
router4.get("/profile", requireAuth(), (req, res) => {
  const profile = db.getProfile(req.user.id);
  sendSuccess(res, profile);
});
router4.patch("/profile", requireAuth(), (req, res) => {
  const result = updateProfileSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const updated = db.updateProfile(req.user.id, result.data);
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
router7.get("/products/category/:categoryId", (req, res) => {
  const result = db.getProducts({ categoryId: String(req.params.categoryId), limit: 30 });
  sendSuccess(res, result.products);
});
router7.get("/products", (req, res) => {
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 20;
  const search = req.query.search || req.query.q;
  const categoryId = req.query.category;
  const brand = req.query.brand;
  const sort = req.query.sort;
  const result = db.getProducts({
    page,
    limit,
    search,
    categoryId,
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
router8.get("/stores/nearby", (_req, res) => {
  const stores = db.getStores();
  const enriched = stores.map((s) => ({
    ...s,
    distanceKm: 0.8,
    estimatedDeliveryMinutes: 12,
    isOpen: true,
    rating: 4.8
  }));
  sendSuccess(res, enriched);
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
var addItemSchema = z6.object({
  productId: z6.string().min(1, "Product ID is required"),
  quantity: z6.number().int().positive("Quantity must be greater than zero").default(1),
  selectedVariant: z6.string().optional()
});
var updateItemSchema = z6.object({
  quantity: z6.number().int().min(0, "Quantity cannot be negative")
});
router9.get("/cart", requireAuth(), (req, res) => {
  const cart = db.getCart(req.user.id);
  sendSuccess(res, cart);
});
router9.post("/cart/items", requireAuth(), (req, res) => {
  const result = addItemSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { productId, quantity, selectedVariant } = result.data;
  const resCart = db.addToCart(req.user.id, productId, quantity, selectedVariant);
  if (!resCart.success) {
    sendError(res, resCart.error || "Could not add to bag", "CART_ERROR", 400);
    return;
  }
  sendSuccess(res, resCart.cart, "Item added to School Bag", 201);
});
router9.patch("/cart/items/:id", requireAuth(), (req, res) => {
  const result = updateItemSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const resCart = db.updateCartItem(req.user.id, String(req.params.id), result.data.quantity);
  if (!resCart.success) {
    sendError(res, resCart.error || "Could not update item", "CART_ERROR", 400);
    return;
  }
  sendSuccess(res, resCart.cart, "School Bag updated");
});
router9.delete("/cart/items/:id", requireAuth(), (req, res) => {
  const resCart = db.removeFromCart(req.user.id, String(req.params.id));
  sendSuccess(res, resCart.cart, "Item removed from School Bag");
});
router9.delete("/cart", requireAuth(), (req, res) => {
  db.clearCart(req.user.id);
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
  couponCode: z8.string().optional()
});
var checkoutCreateSchema = z8.object({
  addressId: z8.string().min(1, "Delivery address is required"),
  couponCode: z8.string().optional(),
  paymentMethod: z8.string().default("UPI"),
  notes: z8.string().optional()
});
router11.post("/checkout/preview", requireAuth(), (req, res) => {
  const result = checkoutPreviewSchema.safeParse(req.body);
  const couponCode = result.success ? result.data.couponCode : void 0;
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
router11.post("/checkout/create", requireAuth(), (req, res) => {
  const result = checkoutCreateSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { addressId, couponCode, paymentMethod, notes } = result.data;
  const orderRes = db.createOrder(req.user.id, addressId, paymentMethod, couponCode, notes);
  if (!orderRes.success || !orderRes.order) {
    sendError(res, orderRes.error || "Failed to initialize checkout", "CHECKOUT_FAILED", 400);
    return;
  }
  sendSuccess(res, {
    orderId: orderRes.order.id,
    orderNumber: orderRes.order.orderNumber,
    total: orderRes.order.total,
    paymentMethod: orderRes.order.paymentMethod,
    order: orderRes.order
  }, "Checkout initiated successfully", 201);
});
var checkout_default = router11;

// src/routes/payment.ts
import { Router as Router12 } from "express";
import { z as z9 } from "zod";

// src/services/payment/MockPaymentService.ts
import { randomUUID as randomUUID2 } from "crypto";
var MockPaymentService = class _MockPaymentService {
  static payments = /* @__PURE__ */ new Map();
  async createPayment(orderId, amount, currency = "INR", metadata = {}) {
    const paymentId = `pay_mock_${randomUUID2().substring(0, 12)}`;
    const gatewayOrderId = `order_mock_${randomUUID2().substring(0, 12)}`;
    const intent = {
      paymentId,
      orderId,
      amount,
      currency,
      provider: "mock",
      gatewayOrderId,
      clientSecret: `sec_mock_${randomUUID2().substring(0, 16)}`,
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
      refundId: `rfnd_mock_${randomUUID2().substring(0, 10)}`,
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
import { randomUUID as randomUUID3 } from "crypto";
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
      id: `notif_${randomUUID3().substring(0, 10)}`,
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
  addressId: z10.string().min(1, "Delivery address is required"),
  paymentMethod: z10.string().default("UPI"),
  couponCode: z10.string().optional(),
  notes: z10.string().optional()
});
router13.post("/orders", requireAuth(), async (req, res) => {
  const result = createOrderSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, "VALIDATION_ERROR", 400);
    return;
  }
  const { addressId, paymentMethod, couponCode, notes } = result.data;
  const orderRes = db.createOrder(req.user.id, addressId, paymentMethod, couponCode, notes);
  if (!orderRes.success || !orderRes.order) {
    sendError(res, orderRes.error || "Failed to place order", "ORDER_CREATION_FAILED", 400);
    return;
  }
  const order = orderRes.order;
  await notificationService2.send(
    req.user.id,
    "ORDER_CONFIRMED",
    "Order Placed! \u{1F680}",
    `Your stationery order ${order.orderNumber} is confirmed and sent to the store.`,
    { orderId: order.id, orderNumber: order.orderNumber }
  );
  sendSuccess(res, order, "Order placed successfully", 201);
});
router13.get("/orders", requireAuth(), (req, res) => {
  const orders = db.getOrders(req.user.id);
  sendSuccess(res, orders);
});
router13.get("/orders/:id", requireAuth(), (req, res) => {
  const order = db.getOrderById(String(req.params.id), req.user.id);
  if (!order) {
    sendError(res, "Order not found", "ORDER_NOT_FOUND", 404);
    return;
  }
  sendSuccess(res, order);
});
router13.post("/orders/:id/cancel", requireAuth(), async (req, res) => {
  const orderId = String(req.params.id);
  const cancelRes = db.cancelOrder(orderId, req.user.id);
  if (!cancelRes.success) {
    sendError(res, cancelRes.error || "Could not cancel order", "CANCEL_FAILED", 400);
    return;
  }
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
  sendSuccess(res, tracking);
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

// src/server.ts
var app = express();
app.use(cors({
  origin: "*",
  methods: ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"],
  allowedHeaders: ["Content-Type", "Authorization", "X-Requested-With"]
}));
app.use(express.json());
app.use((req, res, next) => {
  const requestId = req.headers["x-request-id"] || `req_${randomUUID4().substring(0, 8)}`;
  req.headers["x-request-id"] = requestId;
  res.setHeader("X-Request-Id", requestId);
  const start = Date.now();
  res.on("finish", () => {
    const durationMs = Date.now() - start;
    Logger.info("HTTP Request", {
      requestId,
      method: req.method,
      endpoint: req.originalUrl,
      status: res.statusCode,
      durationMs,
      userId: req.user?.id
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
app.get("/", (_req, res) => {
  res.json({
    app: "KidsG API Server",
    status: "online",
    version: "1.0.0",
    docs: "/api/health"
  });
});
app.use((_req, res) => {
  sendError(res, "Endpoint not found", "ROUTE_NOT_FOUND", 404);
});
app.use((err, _req, res, _next) => {
  Logger.error("Unhandled Exception", err);
  sendError(res, "Internal server error occurred", "INTERNAL_ERROR", 500);
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
