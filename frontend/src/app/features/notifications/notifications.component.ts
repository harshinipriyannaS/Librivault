import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subject, takeUntil } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

import { NotificationService } from '@core/services/notification.service';
import { Notification, NotificationType } from '@core/models/notification.model';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatBadgeModule,
    MatTooltipModule
  ],
  template: `
    <div class="notifications-container">
      <div class="header">
        <h1>
          <mat-icon color="primary">notifications</mat-icon>
          Notifications
        </h1>
        <div class="header-actions">
          <button 
            mat-button 
            color="primary" 
            (click)="markAllAsRead()"
            [disabled]="unreadCount === 0 || loading">
            <mat-icon>done_all</mat-icon>
            Mark All Read
          </button>
          <button 
            mat-icon-button 
            [matMenuTriggerFor]="filterMenu"
            matTooltip="Filter notifications">
            <mat-icon>filter_list</mat-icon>
          </button>
          <mat-menu #filterMenu="matMenu">
            <button mat-menu-item (click)="setFilter('all')">
              <mat-icon>notifications</mat-icon>
              All Notifications
            </button>
            <button mat-menu-item (click)="setFilter('unread')">
              <mat-icon>mark_email_unread</mat-icon>
              Unread Only
            </button>
            <button mat-menu-item (click)="setFilter('BOOK_DUE_REMINDER')">
              <mat-icon>schedule</mat-icon>
              Due Soon
            </button>
            <button mat-menu-item (click)="setFilter('BOOK_OVERDUE')">
              <mat-icon>warning</mat-icon>
              Overdue
            </button>
            <button mat-menu-item (click)="setFilter('PAYMENT_SUCCESSFUL')">
              <mat-icon>payment</mat-icon>
              Payments
            </button>
          </mat-menu>
        </div>
      </div>

      <div class="loading-container" *ngIf="loading">
        <mat-spinner></mat-spinner>
        <p>Loading notifications...</p>
      </div>

      <div class="content" *ngIf="!loading">
        <!-- Summary Card -->
        <mat-card class="summary-card">
          <mat-card-content>
            <div class="summary-stats">
              <div class="stat-item">
                <mat-icon color="primary">notifications</mat-icon>
                <div class="stat-info">
                  <span class="stat-value">{{ totalNotifications }}</span>
                  <span class="stat-label">Total</span>
                </div>
              </div>
              <div class="stat-item">
                <mat-icon color="accent">mark_email_unread</mat-icon>
                <div class="stat-info">
                  <span class="stat-value">{{ unreadCount }}</span>
                  <span class="stat-label">Unread</span>
                </div>
              </div>
              <div class="stat-item">
                <mat-icon color="warn">warning</mat-icon>
                <div class="stat-info">
                  <span class="stat-value">{{ urgentCount }}</span>
                  <span class="stat-label">Urgent</span>
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Notifications List -->
        <div class="notifications-list">
          <div class="no-notifications" *ngIf="filteredNotifications.length === 0">
            <mat-icon>inbox</mat-icon>
            <h3>No notifications</h3>
            <p *ngIf="currentFilter === 'all'">You're all caught up! No notifications to show.</p>
            <p *ngIf="currentFilter === 'unread'">No unread notifications.</p>
            <p *ngIf="currentFilter !== 'all' && currentFilter !== 'unread'">
              No notifications of this type.
            </p>
          </div>

          <mat-card 
            class="notification-card" 
            *ngFor="let notification of filteredNotifications"
            [class.unread]="!notification.read"
            [class.urgent]="isUrgent(notification.type)">
            
            <mat-card-content>
              <div class="notification-content">
                <div class="notification-icon">
                  <mat-icon [color]="getNotificationColor(notification.type)">
                    {{ getNotificationIcon(notification.type) }}
                  </mat-icon>
                </div>
                
                <div class="notification-details">
                  <div class="notification-header">
                    <h3 class="notification-title">{{ notification.title }}</h3>
                    <div class="notification-meta">
                      <mat-chip 
                        [color]="getNotificationColor(notification.type)"
                        selected
                        class="type-chip">
                        {{ getNotificationTypeLabel(notification.type) }}
                      </mat-chip>
                      <span class="notification-time">
                        {{ notification.createdAt | date:'medium' }}
                      </span>
                    </div>
                  </div>
                  
                  <p class="notification-message">{{ notification.message }}</p>
                  
                  <div class="notification-actions" *ngIf="hasActions(notification)">
                    <button 
                      mat-button 
                      color="primary"
                      *ngIf="notification.type === 'BOOK_DUE_REMINDER' || notification.type === 'BOOK_OVERDUE'"
                      routerLink="/dashboard/my-borrows">
                      <mat-icon>book</mat-icon>
                      View Books
                    </button>
                    <button 
                      mat-button 
                      color="primary"
                      *ngIf="notification.type === 'PAYMENT_SUCCESSFUL' || notification.type === 'PAYMENT_FAILED'"
                      routerLink="/subscription/payment-history">
                      <mat-icon>receipt</mat-icon>
                      View Payments
                    </button>
                  </div>
                </div>
                
                <div class="notification-controls">
                  <button 
                    mat-icon-button 
                    *ngIf="!notification.read"
                    (click)="markAsRead(notification.id)"
                    matTooltip="Mark as read">
                    <mat-icon>done</mat-icon>
                  </button>
                  <button 
                    mat-icon-button 
                    [matMenuTriggerFor]="notificationMenu"
                    matTooltip="More options">
                    <mat-icon>more_vert</mat-icon>
                  </button>
                  <mat-menu #notificationMenu="matMenu">
                    <button 
                      mat-menu-item 
                      *ngIf="!notification.read"
                      (click)="markAsRead(notification.id)">
                      <mat-icon>done</mat-icon>
                      Mark as read
                    </button>
                    <button 
                      mat-menu-item 
                      *ngIf="notification.read"
                      (click)="markAsUnread(notification.id)">
                      <mat-icon>mark_email_unread</mat-icon>
                      Mark as unread
                    </button>
                    <button 
                      mat-menu-item 
                      (click)="deleteNotification(notification.id)">
                      <mat-icon>delete</mat-icon>
                      Delete
                    </button>
                  </mat-menu>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Load More Button -->
        <div class="load-more" *ngIf="hasMoreNotifications">
          <button 
            mat-raised-button 
            color="primary" 
            (click)="loadMoreNotifications()"
            [disabled]="loadingMore">
            <mat-spinner diameter="20" *ngIf="loadingMore"></mat-spinner>
            <mat-icon *ngIf="!loadingMore">expand_more</mat-icon>
            {{ loadingMore ? 'Loading...' : 'Load More' }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .notifications-container {
      max-width: 1000px;
      margin: 0 auto;
      padding: 2rem;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .header h1 {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin: 0;
      color: #2c3e50;
    }

    .header-actions {
      display: flex;
      gap: 1rem;
      align-items: center;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem;
    }

    .loading-container p {
      margin-top: 1rem;
      color: #7f8c8d;
    }

    .summary-card {
      margin-bottom: 2rem;
    }

    .summary-stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 2rem;
    }

    .stat-item {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .stat-info {
      display: flex;
      flex-direction: column;
    }

    .stat-value {
      font-size: 1.5rem;
      font-weight: bold;
      color: #2c3e50;
    }

    .stat-label {
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .notifications-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .no-notifications {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem;
      color: #7f8c8d;
      text-align: center;
    }

    .no-notifications mat-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      margin-bottom: 1rem;
    }

    .notification-card {
      transition: all 0.3s ease;
      cursor: pointer;
    }

    .notification-card:hover {
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    .notification-card.unread {
      border-left: 4px solid #3498db;
      background-color: #f8f9fa;
    }

    .notification-card.urgent {
      border-left: 4px solid #e74c3c;
    }

    .notification-content {
      display: flex;
      gap: 1rem;
      align-items: flex-start;
    }

    .notification-icon {
      flex-shrink: 0;
    }

    .notification-details {
      flex: 1;
    }

    .notification-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 0.5rem;
      gap: 1rem;
    }

    .notification-title {
      margin: 0;
      color: #2c3e50;
      font-size: 1.1rem;
    }

    .notification-meta {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 0.5rem;
    }

    .type-chip {
      font-size: 0.8rem;
    }

    .notification-time {
      color: #7f8c8d;
      font-size: 0.8rem;
    }

    .notification-message {
      color: #555;
      margin: 0.5rem 0;
      line-height: 1.5;
    }

    .notification-actions {
      display: flex;
      gap: 0.5rem;
      margin-top: 1rem;
    }

    .notification-controls {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .load-more {
      text-align: center;
      margin-top: 2rem;
    }

    @media (max-width: 768px) {
      .notifications-container {
        padding: 1rem;
      }

      .header {
        flex-direction: column;
        gap: 1rem;
        align-items: stretch;
      }

      .header-actions {
        justify-content: space-between;
      }

      .summary-stats {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .notification-content {
        flex-direction: column;
        gap: 0.5rem;
      }

      .notification-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
      }

      .notification-meta {
        flex-direction: row;
        align-items: center;
      }

      .notification-controls {
        flex-direction: row;
        justify-content: flex-end;
      }
    }
  `]
})
export class NotificationsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  notifications: Notification[] = [];
  filteredNotifications: Notification[] = [];
  loading = true;
  loadingMore = false;
  currentFilter = 'all';
  
  // Pagination
  currentPage = 0;
  pageSize = 20;
  hasMoreNotifications = false;
  
  // Statistics
  totalNotifications = 0;
  unreadCount = 0;
  urgentCount = 0;

  constructor(
    private notificationService: NotificationService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadNotifications(): void {
    this.notificationService.getMyNotifications(this.currentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          if (this.currentPage === 0) {
            this.notifications = response.content;
          } else {
            this.notifications = [...this.notifications, ...response.content];
          }
          
          this.hasMoreNotifications = !response.last;
          this.updateStatistics();
          this.applyFilter();
          this.loading = false;
          this.loadingMore = false;
        },
        error: (error) => {
          console.error('Failed to load notifications:', error);
          this.loading = false;
          this.loadingMore = false;
        }
      });
  }

  private updateStatistics(): void {
    this.totalNotifications = this.notifications.length;
    this.unreadCount = this.notifications.filter(n => !n.read).length;
    this.urgentCount = this.notifications.filter(n => this.isUrgent(n.type)).length;
  }

  private applyFilter(): void {
    switch (this.currentFilter) {
      case 'unread':
        this.filteredNotifications = this.notifications.filter(n => !n.read);
        break;
      case 'all':
        this.filteredNotifications = [...this.notifications];
        break;
      default:
        this.filteredNotifications = this.notifications.filter(n => n.type === this.currentFilter);
    }
  }

  setFilter(filter: string): void {
    this.currentFilter = filter;
    this.applyFilter();
  }

  loadMoreNotifications(): void {
    this.loadingMore = true;
    this.currentPage++;
    this.loadNotifications();
  }

  markAsRead(notificationId: number): void {
    this.notificationService.markAsRead(notificationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const notification = this.notifications.find(n => n.id === notificationId);
          if (notification) {
            notification.read = true;
            this.updateStatistics();
            this.applyFilter();
          }
        },
        error: (error) => {
          console.error('Failed to mark notification as read:', error);
          this.toastr.error('Failed to mark notification as read');
        }
      });
  }

  markAsUnread(notificationId: number): void {
    this.notificationService.markAsUnread(notificationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const notification = this.notifications.find(n => n.id === notificationId);
          if (notification) {
            notification.read = false;
            this.updateStatistics();
            this.applyFilter();
          }
        },
        error: (error) => {
          console.error('Failed to mark notification as unread:', error);
          this.toastr.error('Failed to mark notification as unread');
        }
      });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notifications.forEach(n => n.read = true);
          this.updateStatistics();
          this.applyFilter();
          this.toastr.success('All notifications marked as read');
        },
        error: (error) => {
          console.error('Failed to mark all notifications as read:', error);
          this.toastr.error('Failed to mark all notifications as read');
        }
      });
  }

  deleteNotification(notificationId: number): void {
    this.notificationService.deleteNotification(notificationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notifications = this.notifications.filter(n => n.id !== notificationId);
          this.updateStatistics();
          this.applyFilter();
          this.toastr.success('Notification deleted');
        },
        error: (error) => {
          console.error('Failed to delete notification:', error);
          this.toastr.error('Failed to delete notification');
        }
      });
  }

  getNotificationIcon(type: NotificationType): string {
    switch (type) {
      case NotificationType.BOOK_DUE_REMINDER:
        return 'schedule';
      case NotificationType.BOOK_OVERDUE:
        return 'warning';
      case NotificationType.FINE_GENERATED:
        return 'account_balance_wallet';
      case NotificationType.SUBSCRIPTION_EXPIRING:
        return 'card_membership';
      case NotificationType.SUBSCRIPTION_EXPIRED:
        return 'error';
      case NotificationType.PAYMENT_SUCCESSFUL:
        return 'check_circle';
      case NotificationType.PAYMENT_FAILED:
        return 'error';
      default:
        return 'notifications';
    }
  }

  getNotificationColor(type: NotificationType): string {
    switch (type) {
      case NotificationType.BOOK_DUE_REMINDER:
        return 'accent';
      case NotificationType.BOOK_OVERDUE:
      case NotificationType.FINE_GENERATED:
      case NotificationType.SUBSCRIPTION_EXPIRED:
      case NotificationType.PAYMENT_FAILED:
        return 'warn';
      case NotificationType.PAYMENT_SUCCESSFUL:
        return 'primary';
      default:
        return 'primary';
    }
  }

  getNotificationTypeLabel(type: NotificationType): string {
    switch (type) {
      case NotificationType.BOOK_DUE_REMINDER:
        return 'Due Soon';
      case NotificationType.BOOK_OVERDUE:
        return 'Overdue';
      case NotificationType.FINE_GENERATED:
        return 'Fine';
      case NotificationType.SUBSCRIPTION_EXPIRING:
        return 'Subscription';
      case NotificationType.SUBSCRIPTION_EXPIRED:
        return 'Expired';
      case NotificationType.PAYMENT_SUCCESSFUL:
        return 'Payment';
      case NotificationType.PAYMENT_FAILED:
        return 'Payment Failed';
      default:
        return 'Notification';
    }
  }

  isUrgent(type: NotificationType): boolean {
    return [
      NotificationType.BOOK_OVERDUE,
      NotificationType.FINE_GENERATED,
      NotificationType.SUBSCRIPTION_EXPIRED,
      NotificationType.PAYMENT_FAILED
    ].includes(type);
  }

  hasActions(notification: Notification): boolean {
    return [
      NotificationType.BOOK_DUE_REMINDER,
      NotificationType.BOOK_OVERDUE,
      NotificationType.PAYMENT_SUCCESSFUL,
      NotificationType.PAYMENT_FAILED
    ].includes(notification.type);
  }
}