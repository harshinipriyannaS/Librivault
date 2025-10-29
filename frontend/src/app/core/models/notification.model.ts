export interface Notification {
  id: number;
  userId: number;
  type: NotificationType;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
  updatedAt: string;
}

export enum NotificationType {
  BORROW_REQUEST_APPROVED = 'BORROW_REQUEST_APPROVED',
  BORROW_REQUEST_DECLINED = 'BORROW_REQUEST_DECLINED',
  BOOK_DUE_REMINDER = 'BOOK_DUE_REMINDER',
  BOOK_OVERDUE = 'BOOK_OVERDUE',
  FINE_GENERATED = 'FINE_GENERATED',
  SUBSCRIPTION_EXPIRING = 'SUBSCRIPTION_EXPIRING',
  SUBSCRIPTION_EXPIRED = 'SUBSCRIPTION_EXPIRED',
  PAYMENT_SUCCESSFUL = 'PAYMENT_SUCCESSFUL',
  PAYMENT_FAILED = 'PAYMENT_FAILED'
}

export interface NotificationStats {
  totalNotifications: number;
  unreadNotifications: number;
  notificationsByType: { [key: string]: number };
}