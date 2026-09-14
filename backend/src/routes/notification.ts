import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { sendSuccess, sendError } from '../lib/response.js';
import { requireAuth } from '../middleware/auth.js';
import { getNotificationService } from '../services/notification/NotificationService.js';
import { db } from '../lib/db.js';

const router = Router();
const notificationService = getNotificationService();

// GET /api/notifications
router.get('/notifications', requireAuth(), async (req: Request, res: Response) => {
  const notifications = await notificationService.getNotifications(req.user!.id);
  sendSuccess(res, notifications);
});

// POST /api/notifications/:id/read
router.post('/notifications/:id/read', requireAuth(), async (req: Request, res: Response) => {
  const success = await notificationService.markAsRead(req.user!.id, String(req.params.id));
  sendSuccess(res, { read: success });
});

// POST /api/notifications/read-all
router.post('/notifications/read-all', requireAuth(), async (req: Request, res: Response) => {
  await notificationService.markAllAsRead(req.user!.id);
  sendSuccess(res, { readAll: true }, 'All notifications marked as read');
});

// --- Support Ticket Endpoints ---
const createTicketSchema = z.object({
  subject: z.string().min(3, 'Subject is required'),
  message: z.string().min(5, 'Message details required'),
  orderId: z.string().optional(),
});

// POST /api/support/tickets
router.post('/support/tickets', requireAuth(), (req: Request, res: Response) => {
  const result = createTicketSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.errors[0].message, 'VALIDATION_ERROR', 400);
    return;
  }

  const { subject, message, orderId } = result.data;
  const ticket = db.createSupportTicket(req.user!.id, subject, message, orderId);
  sendSuccess(res, ticket, 'Support ticket created successfully', 201);
});

// GET /api/support/tickets
router.get('/support/tickets', requireAuth(), (req: Request, res: Response) => {
  const tickets = db.getSupportTickets(req.user!.id);
  sendSuccess(res, tickets);
});

// GET /api/support/tickets/:id
router.get('/support/tickets/:id', requireAuth(), (req: Request, res: Response) => {
  const ticket = db.getSupportTicketById(String(req.params.id), req.user!.id);
  if (!ticket) {
    sendError(res, 'Support ticket not found', 'TICKET_NOT_FOUND', 404);
    return;
  }
  sendSuccess(res, ticket);
});

export default router;
