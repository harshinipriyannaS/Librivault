export interface Subscription {
  id: number;
  userId: number;
  type: SubscriptionType;
  startDate: string;
  endDate: string;
  bookLimit: number;
  durationDays: number;
  dailyFineAmount: number;
  price: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Payment {
  id: number;
  userId: number;
  stripePaymentIntentId: string;
  subscriptionType: SubscriptionType;
  amount: number;
  currency: string;
  status: PaymentStatus;
  receiptUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export enum SubscriptionType {
  FREE = 'FREE',
  PREMIUM = 'PREMIUM'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  SUCCEEDED = 'SUCCEEDED',
  FAILED = 'FAILED',
  CANCELED = 'CANCELED'
}

export interface CreatePaymentIntentRequest {
  subscriptionType: SubscriptionType;
}

export interface PaymentIntentResponse {
  clientSecret: string;
  amount: number;
  currency: string;
}

export interface SubscriptionPlan {
  type: SubscriptionType;
  name: string;
  price: number;
  bookLimit: number;
  durationDays: number;
  dailyFineAmount: number;
  features: string[];
  popular?: boolean;
}

export interface SubscriptionStats {
  totalRevenue: number;
  monthlyRevenue: number;
  subscriptionsByType: { [key: string]: number };
  recentPayments: Payment[];
}