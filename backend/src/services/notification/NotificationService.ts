import { randomUUID } from 'crypto';

export interface NotificationItem {
  id: string;
  userId: string;
  type: string;
  title: string;
  message: string;
  data?: Record<string, any>;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationService {
  send(userId: string, type: string, title: string, message: string, data?: Record<string, any>): Promise<NotificationItem>;
  getNotifications(userId: string): Promise<NotificationItem[]>;
  markAsRead(userId: string, notificationId: string): Promise<boolean>;
  markAllAsRead(userId: string): Promise<boolean>;
}

export class MockNotificationService implements NotificationService {
  private static notifications: NotificationItem[] = [
    {
      id: 'notif_welcome',
      userId: 'user_dev_default',
      type: 'SYSTEM',
      title: 'Welcome to KidsG! 🎒',
      message: 'Small Supplies. Big Futures. Explore school essentials delivered in 15 mins!',
      isRead: false,
      createdAt: new Date(Date.now() - 3600000).toISOString(),
    },
    {
      id: 'notif_coupon',
      userId: 'user_dev_default',
      type: 'PROMOTION',
      title: 'Flat ₹50 OFF for your school day! ✨',
      message: 'Use coupon KIDSG50 on your first order above ₹199.',
      isRead: false,
      createdAt: new Date(Date.now() - 7200000).toISOString(),
    }
  ];

  async send(userId: string, type: string, title: string, message: string, data?: Record<string, any>): Promise<NotificationItem> {
    const item: NotificationItem = {
      id: `notif_${randomUUID().substring(0, 10)}`,
      userId,
      type,
      title,
      message,
      data,
      isRead: false,
      createdAt: new Date().toISOString(),
    };
    MockNotificationService.notifications.unshift(item);
    return item;
  }

  async getNotifications(userId: string): Promise<NotificationItem[]> {
    return MockNotificationService.notifications.filter(n => n.userId === userId || n.userId === 'user_dev_default');
  }

  async markAsRead(userId: string, notificationId: string): Promise<boolean> {
    const item = MockNotificationService.notifications.find(n => n.id === notificationId && (n.userId === userId || n.userId === 'user_dev_default'));
    if (item) {
      item.isRead = true;
      return true;
    }
    return false;
  }

  async markAllAsRead(userId: string): Promise<boolean> {
    MockNotificationService.notifications.forEach(n => {
      if (n.userId === userId || n.userId === 'user_dev_default') {
        n.isRead = true;
      }
    });
    return true;
  }
}

export function getNotificationService(): NotificationService {
  return new MockNotificationService();
}
