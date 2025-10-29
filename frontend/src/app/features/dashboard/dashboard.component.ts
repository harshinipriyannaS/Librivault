import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '@core/services/auth.service';
import { BorrowService } from '@core/services/borrow.service';
import { NotificationService } from '@core/services/notification.service';
import { User, UserRole } from '@core/models/user.model';
import { BorrowRecord } from '@core/models/borrow.model';
import { Notification } from '@core/models/notification.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatGridListModule,
    MatProgressBarModule
  ],
  template: `
    <div class="dashboard-container">
      <div class="dashboard-header">
        <h1>Welcome back, {{ currentUser?.firstName }}!</h1>
        <p class="dashboard-subtitle">Here's what's happening with your library account</p>
      </div>

      <!-- Quick Stats Cards -->
      <div class="stats-grid">
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="primary">book</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ activeBorrows.length }}</h3>
                <p>Active Borrows</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="accent">star</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ currentUser?.readerCredits || 0 }}</h3>
                <p>Reader Credits</p>
              </div>
            </div>
          </mat-card-content>
          <mat-card-actions>
            <button mat-button color="accent" routerLink="/dashboard/credits">
              <mat-icon>visibility</mat-icon>
              View Details
            </button>
          </mat-card-actions>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="warn">notifications</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ unreadNotifications }}</h3>
                <p>Notifications</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card" *ngIf="isAdmin || isLibrarian">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="primary">admin_panel_settings</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ currentUser?.role }}</h3>
                <p>Role</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Main Content Grid -->
      <div class="content-grid">
        <!-- Active Borrows -->
        <mat-card class="content-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>library_books</mat-icon>
              Active Borrows
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div *ngIf="activeBorrows.length === 0" class="empty-state">
              <mat-icon>book_online</mat-icon>
              <p>No active borrows</p>
              <a mat-button routerLink="/books" color="primary">Browse Books</a>
            </div>
            <div *ngFor="let borrow of activeBorrows.slice(0, 3)" class="borrow-item">
              <div class="borrow-info">
                <h4>{{ borrow.bookTitle }}</h4>
                <p>by {{ borrow.bookAuthor }}</p>
                <p class="due-date">Due: {{ borrow.dueDate | date:'short' }}</p>
              </div>
              <div class="borrow-actions">
                <button mat-icon-button color="primary" title="Return Book">
                  <mat-icon>keyboard_return</mat-icon>
                </button>
              </div>
            </div>
            <div *ngIf="activeBorrows.length > 3" class="view-all">
              <a mat-button routerLink="/dashboard/borrows">View All Borrows</a>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Recent Notifications -->
        <mat-card class="content-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>notifications</mat-icon>
              Recent Notifications
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div *ngIf="recentNotifications.length === 0" class="empty-state">
              <mat-icon>notifications_none</mat-icon>
              <p>No notifications</p>
            </div>
            <div *ngFor="let notification of recentNotifications.slice(0, 3)" class="notification-item">
              <div class="notification-content">
                <h4>{{ notification.title }}</h4>
                <p>{{ notification.message }}</p>
                <small>{{ notification.createdAt | date:'short' }}</small>
              </div>
              <mat-icon *ngIf="!notification.read" class="unread-indicator" color="accent">fiber_manual_record</mat-icon>
            </div>
            <div *ngIf="recentNotifications.length > 3" class="view-all">
              <a mat-button routerLink="/notifications">View All Notifications</a>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Quick Actions -->
        <mat-card class="content-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>flash_on</mat-icon>
              Quick Actions
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="quick-actions">
              <a mat-raised-button color="primary" routerLink="/books">
                <mat-icon>search</mat-icon>
                Browse Books
              </a>
              
              <a mat-button routerLink="/profile">
                <mat-icon>person</mat-icon>
                Edit Profile
              </a>
              
              <a mat-button routerLink="/notifications">
                <mat-icon>notifications</mat-icon>
                View Notifications
              </a>

              <!-- Role-specific actions -->
              <ng-container *ngIf="isAdmin">
                <a mat-button color="accent" routerLink="/admin">
                  <mat-icon>admin_panel_settings</mat-icon>
                  Admin Panel
                </a>
              </ng-container>

              <ng-container *ngIf="isLibrarian">
                <a mat-button color="accent" routerLink="/librarian">
                  <mat-icon>local_library</mat-icon>
                  Librarian Panel
                </a>
              </ng-container>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Subscription Info (for readers) -->
        <mat-card class="content-card" *ngIf="isReader">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>card_membership</mat-icon>
              Subscription
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="subscription-info">
              <div class="subscription-type">
                <h3>Free Plan</h3>
                <p>2 books per month</p>
              </div>
              <div class="subscription-usage">
                <p>Books this month: {{ activeBorrows.length }}/2</p>
                <mat-progress-bar 
                  mode="determinate" 
                  [value]="(activeBorrows.length / 2) * 100">
                </mat-progress-bar>
              </div>
              <div class="subscription-actions">
                <a mat-raised-button color="accent" routerLink="/subscription/upgrade">
                  <mat-icon>upgrade</mat-icon>
                  Upgrade to Premium
                </a>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .dashboard-header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .dashboard-header h1 {
      color: #2c3e50;
      font-size: 2.5rem;
      margin-bottom: 0.5rem;
    }

    .dashboard-subtitle {
      color: #7f8c8d;
      font-size: 1.1rem;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin-bottom: 2rem;
    }

    .stat-card {
      transition: transform 0.2s ease, box-shadow 0.2s ease;
    }

    .stat-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .stat-icon mat-icon {
      font-size: 2rem;
      width: 2rem;
      height: 2rem;
    }

    .stat-info h3 {
      margin: 0;
      font-size: 1.8rem;
      font-weight: 600;
      color: #2c3e50;
    }

    .stat-info p {
      margin: 0;
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .content-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
      gap: 1.5rem;
    }

    .content-card {
      height: fit-content;
    }

    .content-card mat-card-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #2c3e50;
    }

    .empty-state {
      text-align: center;
      padding: 2rem;
      color: #7f8c8d;
    }

    .empty-state mat-icon {
      font-size: 3rem;
      width: 3rem;
      height: 3rem;
      margin-bottom: 1rem;
    }

    .borrow-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem 0;
      border-bottom: 1px solid #ecf0f1;
    }

    .borrow-item:last-child {
      border-bottom: none;
    }

    .borrow-info h4 {
      margin: 0 0 0.25rem 0;
      color: #2c3e50;
    }

    .borrow-info p {
      margin: 0;
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .due-date {
      color: #e74c3c !important;
      font-weight: 500;
    }

    .notification-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 1rem 0;
      border-bottom: 1px solid #ecf0f1;
    }

    .notification-item:last-child {
      border-bottom: none;
    }

    .notification-content h4 {
      margin: 0 0 0.25rem 0;
      color: #2c3e50;
      font-size: 1rem;
    }

    .notification-content p {
      margin: 0 0 0.25rem 0;
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .notification-content small {
      color: #95a5a6;
    }

    .unread-indicator {
      font-size: 0.8rem;
      width: 0.8rem;
      height: 0.8rem;
    }

    .quick-actions {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .quick-actions a,
    .quick-actions button {
      justify-content: flex-start;
      text-align: left;
    }

    .subscription-info {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .subscription-type h3 {
      margin: 0;
      color: #2c3e50;
    }

    .subscription-type p {
      margin: 0;
      color: #7f8c8d;
    }

    .subscription-usage p {
      margin: 0 0 0.5rem 0;
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .view-all {
      text-align: center;
      padding-top: 1rem;
      border-top: 1px solid #ecf0f1;
      margin-top: 1rem;
    }

    @media (max-width: 768px) {
      .dashboard-container {
        padding: 1rem;
      }

      .dashboard-header h1 {
        font-size: 2rem;
      }

      .stats-grid {
        grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      }

      .content-grid {
        grid-template-columns: 1fr;
      }

      .stat-content {
        flex-direction: column;
        text-align: center;
      }

      .borrow-item,
      .notification-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
      }
    }
  `]
})
export class DashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  currentUser: User | null = null;
  activeBorrows: BorrowRecord[] = [];
  recentNotifications: Notification[] = [];
  unreadNotifications = 0;

  constructor(
    private authService: AuthService,
    private borrowService: BorrowService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    // Get current user
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        this.currentUser = user;
        if (user) {
          this.loadDashboardData();
        }
      });

    // Get unread notification count
    this.notificationService.unreadCount$
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => this.unreadNotifications = count);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadDashboardData(): void {
    // Load active borrows
    this.borrowService.getMyBorrowRecords(0, 10).subscribe({
      next: (response) => {
        this.activeBorrows = response.content.filter(borrow => borrow.status === 'ACTIVE');
      },
      error: (error) => {
        console.error('Error loading borrows:', error);
      }
    });

    // Load recent notifications
    this.notificationService.getMyNotifications(0, 10).subscribe({
      next: (response) => {
        this.recentNotifications = response.content;
      },
      error: (error) => {
        console.error('Error loading notifications:', error);
      }
    });
  }

  get isAdmin(): boolean {
    return this.currentUser?.role === UserRole.ADMIN;
  }

  get isLibrarian(): boolean {
    return this.currentUser?.role === UserRole.LIBRARIAN;
  }

  get isReader(): boolean {
    return this.currentUser?.role === UserRole.READER;
  }
}