export interface RequestLog {
  requestId: string;
  endpoint: string;
  method: string;
  status?: number;
  durationMs?: number;
  userId?: string;
  ip?: string;
}

export class Logger {
  private static sanitize(obj: any): any {
    if (!obj || typeof obj !== 'object') return obj;
    const sanitized = { ...obj };
    const sensitiveKeys = ['password', 'otp', 'secret', 'key', 'token', 'authorization', 'card'];
    for (const k of Object.keys(sanitized)) {
      if (sensitiveKeys.some(s => k.toLowerCase().includes(s))) {
        sanitized[k] = '[REDACTED]';
      } else if (typeof sanitized[k] === 'object') {
        sanitized[k] = Logger.sanitize(sanitized[k]);
      }
    }
    return sanitized;
  }

  static info(message: string, context?: Record<string, any>): void {
    const timestamp = new Date().toISOString();
    const cleanContext = context ? Logger.sanitize(context) : undefined;
    console.log(JSON.stringify({ level: 'INFO', timestamp, message, ...cleanContext }));
  }

  static warn(message: string, context?: Record<string, any>): void {
    const timestamp = new Date().toISOString();
    const cleanContext = context ? Logger.sanitize(context) : undefined;
    console.warn(JSON.stringify({ level: 'WARN', timestamp, message, ...cleanContext }));
  }

  static error(message: string, error?: any, context?: Record<string, any>): void {
    const timestamp = new Date().toISOString();
    const cleanContext = context ? Logger.sanitize(context) : undefined;
    console.error(JSON.stringify({
      level: 'ERROR',
      timestamp,
      message,
      errorMessage: error?.message || String(error),
      stack: process.env.NODE_ENV === 'development' ? error?.stack : undefined,
      ...cleanContext,
    }));
  }
}
