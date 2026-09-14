import { Response } from 'express';

export interface ApiResponse<T = any> {
  success: boolean;
  data: T | null;
  message: string | null;
  errorCode: string | null;
}

export function sendSuccess<T>(res: Response, data: T, message: string | null = null, statusCode = 200): void {
  const response: ApiResponse<T> = {
    success: true,
    data,
    message,
    errorCode: null,
  };
  res.status(statusCode).json(response);
}

export function sendError(
  res: Response,
  message: string,
  errorCode = 'INTERNAL_ERROR',
  statusCode = 400,
  data: any = null
): void {
  const response: ApiResponse = {
    success: false,
    data,
    message,
    errorCode,
  };
  res.status(statusCode).json(response);
}
