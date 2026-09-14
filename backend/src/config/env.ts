import { z } from 'zod';
import dotenv from 'dotenv';
import path from 'path';

// Load environment variables from .env.local if present, else .env
dotenv.config({ path: path.resolve(process.cwd(), '.env.local') });
dotenv.config({ path: path.resolve(process.cwd(), '.env') });

const envSchema = z.object({
  NODE_ENV: z.enum(['development', 'test', 'production']).default('development'),
  APP_ENV: z.enum(['DEV', 'STAGING', 'PRODUCTION']).default('DEV'),
  PORT: z.coerce.number().default(3000),
  API_BASE_URL: z.string().default('http://localhost:3000'),

  // Supabase
  SUPABASE_URL: z.string().default('https://mock.supabase.co'),
  SUPABASE_PUBLISHABLE_KEY: z.string().default(''),
  SUPABASE_ANON_KEY: z.string().default(''),
  SUPABASE_SECRET_KEY: z.string().default(''),
  SUPABASE_SERVICE_ROLE_KEY: z.string().default(''),
  JWT_SECRET: z.string().default('kidsg_development_jwt_secret_must_be_changed_in_prod'),

  // Email & Resend
  EMAIL_PROVIDER: z.enum(['resend', 'mock']).default('resend'),
  RESEND_API_KEY: z.string().optional(),
  RESEND_FROM_EMAIL: z.string().default('REPLACE_ME'),
  RESEND_FROM_NAME: z.string().default('KidsG'),

  // Providers
  OTP_PROVIDER: z.enum(['supabase', 'mock', 'msg91', 'twilio', 'twofactor']).default('supabase'),
  OTP_API_URL: z.string().optional(),
  OTP_API_KEY: z.string().optional(),

  PAYMENT_PROVIDER: z.enum(['mock', 'razorpay', 'cashfree', 'phonepe']).default('mock'),
  RAZORPAY_KEY_ID: z.string().optional(),
  RAZORPAY_KEY_SECRET: z.string().optional(),
  RAZORPAY_WEBHOOK_SECRET: z.string().optional(),

  MAPS_PROVIDER: z.enum(['mock', 'google']).default('mock'),
  GOOGLE_MAPS_API_KEY: z.string().optional(),

  DELIVERY_PROVIDER: z.enum(['mock', 'shadowfax', 'dunzo', 'porter']).default('mock'),
  NOTIFICATION_PROVIDER: z.enum(['mock', 'fcm', 'onesignal']).default('mock'),
  NOTIFICATION_API_KEY: z.string().optional(),

  // Authoritative Business Rules
  FREE_DELIVERY_THRESHOLD: z.coerce.number().default(199),
  DEFAULT_DELIVERY_FEE: z.coerce.number().default(30),
  MINIMUM_ORDER_VALUE: z.coerce.number().default(0),
});

export type Env = z.infer<typeof envSchema>;

function parseEnv(): Env {
  // Normalize key aliases
  const rawEnv = {
    ...process.env,
    SUPABASE_ANON_KEY: process.env.SUPABASE_ANON_KEY || process.env.SUPABASE_PUBLISHABLE_KEY || 'mock_anon_key',
    SUPABASE_PUBLISHABLE_KEY: process.env.SUPABASE_PUBLISHABLE_KEY || process.env.SUPABASE_ANON_KEY || 'mock_anon_key',
    SUPABASE_SERVICE_ROLE_KEY: process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_SECRET_KEY || 'mock_service_role_key',
    SUPABASE_SECRET_KEY: process.env.SUPABASE_SECRET_KEY || process.env.SUPABASE_SERVICE_ROLE_KEY || 'mock_service_role_key',
  };

  const result = envSchema.safeParse(rawEnv);
  if (!result.success) {
    console.error('❌ Invalid environment variables:', result.error.format());
    if (process.env.NODE_ENV === 'production') {
      throw new Error('Missing or invalid environment configuration in production');
    }
  }
  return result.success ? result.data : (envSchema.parse({}) as Env);
}

export const env = parseEnv();
