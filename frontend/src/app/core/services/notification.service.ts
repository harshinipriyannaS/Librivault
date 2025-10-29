import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '@environments/environment';
import { 
  Notification, 
  NotificationStats 
} from '../models/notification.model';
import { PagedResponse } from './book.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly API_URL = `${environment.apiUrl}/notifications`;
  
  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUnreadCount();
  }

  // Notification operations
  getMyNotifications(page: number = 0, size: number = 20): Observable<PagedResponse<Notification>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<Notification>>(`${this.API_URL}/my`, { params });
  }

  getUnreadNotifications(page: number = 0, size: number = 20): Observable<PagedResponse<Notification>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<Notification>>(`${this.API_URL}/unread`, { params });
  }

  markAsRead(notificationId: number): Observable<Notification> {
    return this.http.post<Notification>(`${this.API_URL}/${notificationId}/read`, {})
      .pipe(
        tap(() => this.loadUnreadCount())
      );
  }

  markAllAsRead(): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/mark-all-read`, {})
      .pipe(
        tap(() => this.loadUnreadCount())
      );
  }

  deleteNotification(notificationId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${notificationId}`)
      .pipe(
        tap(() => this.loadUnreadCount())
      );
  }

  getNotificationStats(): Observable<NotificationStats> {
    return this.http.get<NotificationStats>(`${this.API_URL}/stats`);
  }

  private loadUnreadCount(): void {
    this.http.get<{ count: number }>(`${this.API_URL}/unread-count`, {
      headers: { 'X-Skip-Loading': 'true' }
    }).subscribe({
      next: (response) => this.unreadCountSubject.next(response.count),
      error: () => this.unreadCountSubject.next(0)
    });
  }

  markAsUnread(notificationId: number): Observable<Notification> {
    return this.http.post<Notification>(`${this.API_URL}/${notificationId}/unread`, {})
      .pipe(
        tap(() => this.loadUnreadCount())
      );
  }

  // Public method to refresh unread count
  refreshUnreadCount(): void {
    this.loadUnreadCount();
  }
}