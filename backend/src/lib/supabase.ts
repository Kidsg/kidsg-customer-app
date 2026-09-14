import { createClient, SupabaseClient } from '@supabase/supabase-js';
import { env } from '../config/env.js';

let supabaseClient: SupabaseClient;
let supabaseAuthClient: SupabaseClient;

// Check if valid URL is provided
const isConfigured = env.SUPABASE_URL.startsWith('http') && !env.SUPABASE_URL.includes('mock.supabase.co');

if (isConfigured) {
  const secretKey = env.SUPABASE_SECRET_KEY || env.SUPABASE_SERVICE_ROLE_KEY;
  const publishableKey = env.SUPABASE_PUBLISHABLE_KEY || env.SUPABASE_ANON_KEY;

  supabaseClient = createClient(env.SUPABASE_URL, secretKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false,
    },
  });

  supabaseAuthClient = createClient(env.SUPABASE_URL, publishableKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false,
    },
  });
} else {
  // Safe mock client for development/testing when Supabase project is not yet provisioned
  supabaseClient = {
    auth: {
      getUser: async (token: string) => {
        if (token && token.length > 5) {
          return {
            data: {
              user: {
                id: 'user_dev_default',
                email: 'student@kidsg.in',
                phone: '+919876543210',
                user_metadata: { role: 'CUSTOMER', first_name: 'Aarav', last_name: 'Sharma' },
              },
            },
            error: null,
          } as any;
        }
        return { data: { user: null }, error: new Error('Invalid token') } as any;
      },
      signUp: async () => ({ data: { user: { id: 'user_dev_default' } }, error: null } as any),
      signInWithPassword: async () => ({ data: { session: { access_token: 'dev-token-user_dev_default' } }, error: null } as any),
      signInWithOtp: async () => ({ data: {}, error: null } as any),
      verifyOtp: async () => ({ data: { session: { access_token: 'dev-token-user_dev_default' }, user: { id: 'user_dev_default' } }, error: null } as any),
    },
    from: () => ({
      select: () => ({ eq: () => ({ single: async () => ({ data: null, error: null }) }) }),
      insert: async () => ({ data: null, error: null }),
      update: async () => ({ data: null, error: null }),
      delete: async () => ({ data: null, error: null }),
    }),
  } as unknown as SupabaseClient;

  supabaseAuthClient = supabaseClient;
}

export const supabaseAdmin = supabaseClient;
export const supabaseAuth = supabaseAuthClient;
