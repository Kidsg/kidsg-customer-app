import { Router } from 'express';
import { env } from '../config/env.js';
import { supabaseAdmin } from '../lib/supabase.js';

const router = Router();

router.get('/health', async (_req, res) => {
  const isSupaConfigured = env.SUPABASE_URL.startsWith('http') && !env.SUPABASE_URL.includes('mock.supabase.co');
  let supaStatus = isSupaConfigured ? 'connecting' : 'mock_standalone';
  let supaError: string | null = null;
  let profilesCount: number | null = null;

  if (isSupaConfigured) {
    try {
      const { count, error } = await supabaseAdmin
        .from('profiles')
        .select('*', { count: 'exact', head: true });
      if (error) {
        supaStatus = 'error';
        supaError = error.message;
      } else {
        supaStatus = 'connected';
        profilesCount = count ?? 0;
      }
    } catch (e: any) {
      supaStatus = 'error';
      supaError = e?.message || 'Unknown connection error';
    }
  }

  res.json({
    success: true,
    service: 'kidsG-api',
    status: 'ok',
    environment: env.NODE_ENV,
    appEnv: env.APP_ENV,
    supabase: {
      configured: isSupaConfigured,
      url: env.SUPABASE_URL.replace(/https:\/\/(.{4}).*(\.supabase\.co)/, 'https://$1...$2'),
      status: supaStatus,
      error: supaError,
      profilesCount,
    }
  });
});

export default router;
