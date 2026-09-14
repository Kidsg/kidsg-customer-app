import { Router } from 'express';
import { env } from '../config/env.js';

const router = Router();

router.get('/health', (_req, res) => {
  res.json({
    success: true,
    service: 'kidsG-api',
    status: 'ok',
    environment: env.NODE_ENV,
    appEnv: env.APP_ENV,
  });
});

export default router;
