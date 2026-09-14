import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';
import { randomUUID } from 'crypto';
import { env } from './config/env.js';
import { Logger } from './lib/logger.js';
import { sendError } from './lib/response.js';

// Route Handlers
import healthRoutes from './routes/health.js';
import authRoutes from './routes/auth.js';
import onboardingRoutes from './routes/onboarding.js';
import profileRoutes from './routes/profile.js';
import addressRoutes from './routes/address.js';
import categoryRoutes from './routes/category.js';
import productRoutes from './routes/product.js';
import storeRoutes from './routes/store.js';
import cartRoutes from './routes/cart.js';
import couponRoutes from './routes/coupon.js';
import checkoutRoutes from './routes/checkout.js';
import paymentRoutes from './routes/payment.js';
import orderRoutes from './routes/order.js';
import notificationRoutes from './routes/notification.js';

const app = express();

// Middlewares
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PATCH', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With'],
}));
app.use(express.json());

// Request ID and Observability Logging Middleware
app.use((req: Request, res: Response, next: NextFunction) => {
  const requestId = req.headers['x-request-id'] as string || `req_${randomUUID().substring(0, 8)}`;
  req.headers['x-request-id'] = requestId;
  res.setHeader('X-Request-Id', requestId);

  const start = Date.now();
  res.on('finish', () => {
    const durationMs = Date.now() - start;
    Logger.info('HTTP Request', {
      requestId,
      method: req.method,
      endpoint: req.originalUrl,
      status: res.statusCode,
      durationMs,
      userId: req.user?.id,
    });
  });

  next();
});

// API Routes
app.use('/api', healthRoutes);
app.use('/api', authRoutes);
app.use('/api', onboardingRoutes);
app.use('/api', profileRoutes);
app.use('/api', addressRoutes);
app.use('/api', categoryRoutes);
app.use('/api', productRoutes);
app.use('/api', storeRoutes);
app.use('/api', cartRoutes);
app.use('/api', couponRoutes);
app.use('/api', checkoutRoutes);
app.use('/api', paymentRoutes);
app.use('/api', orderRoutes);
app.use('/api', notificationRoutes);

// Root Health Fallback
app.get('/', (_req: Request, res: Response) => {
  res.json({
    app: 'KidsG API Server',
    status: 'online',
    version: '1.0.0',
    docs: '/api/health',
  });
});

// 404 Handler
app.use((_req: Request, res: Response) => {
  sendError(res, 'Endpoint not found', 'ROUTE_NOT_FOUND', 404);
});

// Global Error Handler
app.use((err: any, _req: Request, res: Response, _next: NextFunction) => {
  Logger.error('Unhandled Exception', err);
  sendError(res, 'Internal server error occurred', 'INTERNAL_ERROR', 500);
});

// Start Server locally
if (process.env.NODE_ENV !== 'test' && !process.env.VERCEL) {
  const port = env.PORT;
  app.listen(port, () => {
    console.log(`\n======================================================`);
    console.log(`🎒 KIDSG API SERVER STARTED`);
    console.log(`📍 Local URL:     http://localhost:${port}`);
    console.log(`🩺 Health check:  http://localhost:${port}/api/health`);
    console.log(`🚀 Mode:          ${env.NODE_ENV} (${env.APP_ENV})`);
    console.log(`🛡️  Providers:     OTP=${env.OTP_PROVIDER} | Payment=${env.PAYMENT_PROVIDER}`);
    console.log(`======================================================\n`);
  });
}

export default app;
