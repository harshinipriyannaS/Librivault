import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { 
  Subscription, 
  Payment, 
  CreatePaymentIntentRequest,
  PaymentIntentResponse,
  SubscriptionPlan,
  SubscriptionStats
} from '../models/subscription.model';
import { PagedResponse } from './book.service';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly API_URL = `${environment.apiUrl}/subscriptions`;
  private readonly PAYMENT_URL = `${environment.apiUrl}/payments`;

  constructor(private http: HttpClient) {}

  // Subscription operations
  getCurrentSubscription(): Observable<Subscription> {
    return this.http.get<Subscription>(`${this.API_URL}/current`);
  }

  getSubscriptionHistory(page: number = 0, size: number = 20): Observable<PagedResponse<Subscription>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<Subscription>>(`${this.API_URL}/history`, { params });
  }

  getAvailablePlans(): Observable<SubscriptionPlan[]> {
    return this.http.get<SubscriptionPlan[]>(`${this.API_URL}/plans`);
  }

  // Payment operations
  createPaymentIntent(request: CreatePaymentIntentRequest): Observable<PaymentIntentResponse> {
    return this.http.post<PaymentIntentResponse>(`${this.PAYMENT_URL}/create-intent`, request);
  }

  confirmPayment(paymentIntentId: string): Observable<Payment> {
    return this.http.post<Payment>(`${this.PAYMENT_URL}/confirm`, { paymentIntentId });
  }

  getPaymentHistory(page: number = 0, size: number = 20): Observable<PagedResponse<Payment>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<Payment>>(`${this.PAYMENT_URL}/history`, { params });
  }

  downloadReceipt(paymentId: number): Observable<Blob> {
    return this.http.get(`${this.PAYMENT_URL}/${paymentId}/receipt`, { 
      responseType: 'blob' 
    });
  }

  // Admin operations
  getAllPayments(page: number = 0, size: number = 20): Observable<PagedResponse<Payment>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<Payment>>(`${this.PAYMENT_URL}/admin/all`, { params });
  }

  getSubscriptionStats(): Observable<SubscriptionStats> {
    return this.http.get<SubscriptionStats>(`${this.API_URL}/stats`);
  }

  // Stripe configuration
  getStripeConfig(): Observable<{ publishableKey: string }> {
    return this.http.get<{ publishableKey: string }>(`${this.PAYMENT_URL}/config`);
  }

  // Receipt operations
  getReceiptUrl(paymentId: number): Observable<{ receiptUrl: string }> {
    return this.http.get<{ receiptUrl: string }>(`${this.PAYMENT_URL}/${paymentId}/receipt`);
  }
}