import { Request, Response, NextFunction } from 'express';
import { sendError } from '../lib/response.js';
import { supabaseAdmin } from '../lib/supabase.js';

export type UserRole = 'CUSTOMER' | 'ADMIN' | 'PARTNER' | 'DELIVERY_PARTNER';

export interface AuthenticatedUser {
  id: string;
  authUserId?: string;
  email?: string;
  phone?: string;
  role: UserRole;
  firstName?: string;
  lastName?: string;
}

declare global {
  namespace Express {
    interface Request {
      user?: AuthenticatedUser;
    }
  }
}

export function extractBearerToken(req: Request): string | null {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return null;
  }
  return authHeader.substring(7).trim();
}

export async function parseToken(token: string): Promise<AuthenticatedUser | null> {
  // 1. Development & Mock Tokens
  if (token.startsWith('dev-token-') || token.startsWith('mock-token-') || token === 'dev_token') {
    const userId = token.replace('dev-token-', '').replace('mock-token-', '');
    const isMockAdmin = token.includes('admin');
    return {
      id: userId || 'user_dev_default',
      authUserId: userId || 'user_dev_default',
      phone: '+919876543210',
      email: isMockAdmin ? 'admin@kidsg.in' : 'student@kidsg.in',
      role: isMockAdmin ? 'ADMIN' : 'CUSTOMER',
      firstName: isMockAdmin ? 'KidsG' : 'Aarav',
      lastName: isMockAdmin ? 'Admin' : 'Sharma',
    };
  }

  // 2. Supabase Auth Token verification
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
      role: (authUser.user_metadata?.role as UserRole) || 'CUSTOMER',
      firstName: authUser.user_metadata?.first_name || '',
      lastName: authUser.user_metadata?.last_name || '',
    };
  } catch {
    return null;
  }
}

export function requireAuth() {
  return async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    const token = extractBearerToken(req);
    if (!token) {
      sendError(res, 'Authentication required', 'UNAUTHORIZED', 401);
      return;
    }

    const user = await parseToken(token);
    if (!user) {
      sendError(res, 'Invalid or expired session token', 'UNAUTHORIZED', 401);
      return;
    }

    req.user = user;
    next();
  };
}

export function optionalAuth() {
  return async (req: Request, _res: Response, next: NextFunction): Promise<void> => {
    const token = extractBearerToken(req);
    if (token) {
      const user = await parseToken(token);
      if (user) {
        req.user = user;
      }
    }
    next();
  };
}

export function requireRole(...allowedRoles: UserRole[]) {
  return async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    const token = extractBearerToken(req);
    if (!token) {
      sendError(res, 'Authentication required', 'UNAUTHORIZED', 401);
      return;
    }

    const user = await parseToken(token);
    if (!user) {
      sendError(res, 'Invalid or expired session token', 'UNAUTHORIZED', 401);
      return;
    }

    if (!allowedRoles.includes(user.role)) {
      sendError(res, 'You do not have permission to perform this action', 'FORBIDDEN', 403);
      return;
    }

    req.user = user;
    next();
  };
}
