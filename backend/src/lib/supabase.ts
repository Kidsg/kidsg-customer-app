import { createClient, SupabaseClient } from '@supabase/supabase-js';
import { env } from '../config/env.js';

let supabaseClient: SupabaseClient;
let supabaseAuthClient: SupabaseClient;

// Check if valid URL and keys are provided (ignore mock or non-existent template domains)
const isConfigured = 
  Boolean(env.SUPABASE_URL) &&
  env.SUPABASE_URL.startsWith('http') && 
  !env.SUPABASE_URL.includes('mock.supabase.co') &&
  !env.SUPABASE_URL.includes('ogglmiewsswgkyjajnrg') &&
  Boolean(env.SUPABASE_SECRET_KEY || env.SUPABASE_SERVICE_ROLE_KEY);

if (isConfigured) {
  const secretKey = env.SUPABASE_SECRET_KEY || env.SUPABASE_SERVICE_ROLE_KEY;
  const publishableKey = env.SUPABASE_PUBLISHABLE_KEY || env.SUPABASE_ANON_KEY || secretKey;

  supabaseClient = createClient(env.SUPABASE_URL, secretKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false,
    },
    global: {
      fetch: (url, options) => {
        return fetch(url, {
          ...options,
          signal: AbortSignal.timeout(3000),
        });
      },
    },
  });

  supabaseAuthClient = createClient(env.SUPABASE_URL, publishableKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false,
    },
    global: {
      fetch: (url, options) => {
        return fetch(url, {
          ...options,
          signal: AbortSignal.timeout(3000),
        });
      },
    },
  });
} else {
  // Safe mock client builder that supports any chainable query without throwing
  const createMockChain = (): any => {
    const target: any = () => target;
    const handler: ProxyHandler<any> = {
      get(_obj, prop) {
        if (prop === 'then') {
          return (resolve: any) => Promise.resolve({ data: null, error: null }).then(resolve);
        }
        if (prop === 'single' || prop === 'maybeSingle') {
          return () => Promise.resolve({ data: null, error: null });
        }
        if (prop === 'insert' || prop === 'upsert' || prop === 'update' || prop === 'delete') {
          return () => new Proxy(target, handler);
        }
        if (prop === 'select') {
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
    from: () => createMockChain(),
  } as unknown as SupabaseClient;

  supabaseAuthClient = supabaseClient;
}

export const supabaseAdmin = supabaseClient;
export const supabaseAuth = supabaseAuthClient;
