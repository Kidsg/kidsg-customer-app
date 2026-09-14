import { Request, Response, NextFunction } from 'express';
import { sendError } from '../lib/response.js';

export interface RateLimitService {
  isRateLimited(key: string, limit: number, windowMs: number): Promise<{ limited: boolean; remaining: number }>;
}

export class InMemoryRateLimitService implements RateLimitService {
  private static store: Map<string, { count: number; resetAt: number }> = new Map();

  async isRateLimited(key: string, limit: number, windowMs: number): Promise<{ limited: boolean; remaining: number }> {
    const now = Date.now();
    const entry = InMemoryRateLimitService.store.get(key);

    if (!entry || now > entry.resetAt) {
      InMemoryRateLimitService.store.set(key, { count: 1, resetAt: now + windowMs });
      return { limited: false, remaining: limit - 1 };
    }

    if (entry.count >= limit) {
      return { limited: true, remaining: 0 };
    }

    entry.count += 1;
    return { limited: false, remaining: limit - entry.count };
  }
}

const rateLimitService: RateLimitService = new InMemoryRateLimitService();

export function rateLimit(limit = 10, windowMs = 60000, prefix = 'rl') {
  return async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    const ip = req.ip || req.socket.remoteAddress || '127.0.0.1';
    const key = `${prefix}:${ip}`;

    const { limited } = await rateLimitService.isRateLimited(key, limit, windowMs);
    if (limited) {
      sendError(res, 'Too many requests. Please try again in a few moments.', 'RATE_LIMITED', 429);
      return;
    }

    next();
  };
}
