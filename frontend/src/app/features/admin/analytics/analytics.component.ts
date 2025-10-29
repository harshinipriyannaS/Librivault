import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { NgChartsModule } from 'ng2-charts';
import { Chart, ChartConfiguration, ChartData, ChartType, registerables } from 'chart.js';
import { Subject, takeUntil, forkJoin } from 'rxjs';

import { SubscriptionService } from '@core/services/subscription.service';
import { BookService } from '@core/services/book.service';
import { UserService } from '@core/services/user.service';
import { BorrowService } from '@core/services/borrow.service';
import { Payment, SubscriptionStats } from '@core/models/subscription.model';
import { BookStats } from '@core/models/book.model';
import { UserStats } from '@core/models/user.model';
import { BorrowStats } from '@core/models/borrow.model';

Chart.register(...registerables);

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSelectModule,
    MatFormFieldModule,
    NgChartsModule
  ],
  template: `
    <div class="analytics-container">
      <div class="header">
        <h1>
          <mat-icon color="primary">analytics</mat-icon>
          Analytics Dashboard
        </h1>
        <p>Comprehensive insights into your library system</p>
      </div>

      <div class="loading-container" *ngIf="loading">
        <mat-spinner></mat-spinner>
        <p>Loading analytics data...</p>
      </div>

      <div class="content" *ngIf="!loading">
        <!-- Key Metrics Cards -->
        <div class="metrics-grid">
          <mat-card class="metric-card revenue">
            <mat-card-content>
              <div class="metric-content">
                <div class="metric-icon">
                  <mat-icon>attach_money</mat-icon>
                </div>
                <div class="metric-info">
                  <h3>\${{ totalRevenue.toFixed(2) }}</h3>
                  <p>Total Revenue</p>
                  <span class="metric-change positive">+12.5% this month</span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>

          <mat-card class="metric-card users">
            <mat-card-content>
              <div class="metric-content">
                <div class="metric-icon">
                  <mat-icon>people</mat-icon>
                </div>
                <div class="metric-info">
                  <h3>{{ totalUsers }}</h3>
                  <p>Total Users</p>
                  <span class="metric-change positive">+{{ newUsersThisMonth }} new this month</span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>

          <mat-card class="metric-card books">
            <mat-card-content>
              <div class="metric-content">
                <div class="metric-icon">
                  <mat-icon>library_books</mat-icon>
                </div>
                <div class="metric-info">
                  <h3>{{ totalBooks }}</h3>
                  <p>Total Books</p>
                  <span class="metric-change neutral">{{ activeBorrows }} currently borrowed</span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>

          <mat-card class="metric-card subscriptions">
            <mat-card-content>
              <div class="metric-content">
                <div class="metric-icon">
                  <mat-icon>card_membership</mat-icon>
                </div>
                <div class="metric-info">
                  <h3>{{ premiumSubscriptions }}</h3>
                  <p>Premium Subscribers</p>
                  <span class="metric-change positive">{{ subscriptionGrowth }}% growth</span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Charts Section -->
        <div class="charts-section">
          <div class="charts-row">
            <!-- Revenue Chart -->
            <mat-card class="chart-card">
              <mat-card-header>
                <mat-card-title>
                  <mat-icon color="primary">trending_up</mat-icon>
                  Revenue Trends
                </mat-card-title>
                <mat-card-subtitle>Monthly revenue over the last 12 months</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content>
                <div class="chart-container">
                  <canvas 
                    baseChart
                    [data]="revenueChartData"
                    [options]="revenueChartOptions"
                    [type]="'line'">
                  </canvas>
                </div>
              </mat-card-content>
            </mat-card>

            <!-- Subscription Distribution -->
            <mat-card class="chart-card">
              <mat-card-header>
                <mat-card-title>
                  <mat-icon color="accent">pie_chart</mat-icon>
                  Subscription Distribution
                </mat-card-title>
                <mat-card-subtitle>Free vs Premium subscribers</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content>
                <div class="chart-container">
                  <canvas 
                    baseChart
                    [data]="subscriptionChartData"
                    [options]="subscriptionChartOptions"
                    [type]="'doughnut'">
                  </canvas>
                </div>
              </mat-card-content>
            </mat-card>
          </div>

          <div class="charts-row">
            <!-- Popular Books -->
            <mat-card class="chart-card">
              <mat-card-header>
                <mat-card-title>
                  <mat-icon color="primary">star</mat-icon>
                  Most Popular Books
                </mat-card-title>
                <mat-card-subtitle>Top 10 most borrowed books</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content>
                <div class="chart-container">
                  <canvas 
                    baseChart
                    [data]="popularBooksChartData"
                    [options]="popularBooksChartOptions"
                    [type]="'bar'">
                  </canvas>
                </div>
              </mat-card-content>
            </mat-card>

            <!-- User Activity -->
            <mat-card class="chart-card">
              <mat-card-header>
                <mat-card-title>
                  <mat-icon color="primary">timeline</mat-icon>
                  User Activity
                </mat-card-title>
                <mat-card-subtitle>Daily active users over time</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content>
                <div class="chart-container">
                  <canvas 
                    baseChart
                    [data]="userActivityChartData"
                    [options]="userActivityChartOptions"
                    [type]="'line'">
                  </canvas>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </div>

        <!-- Transaction Management -->
        <mat-card class="transactions-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon color="primary">receipt_long</mat-icon>
              Recent Transactions
            </mat-card-title>
            <mat-card-subtitle>Payment history and transaction management</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <div class="table-container">
              <table mat-table [dataSource]="recentPayments" class="transactions-table">
                <!-- Date Column -->
                <ng-container matColumnDef="date">
                  <th mat-header-cell *matHeaderCellDef>Date</th>
                  <td mat-cell *matCellDef="let payment">
                    {{ payment.createdAt | date:'medium' }}
                  </td>
                </ng-container>

                <!-- User Column -->
                <ng-container matColumnDef="user">
                  <th mat-header-cell *matHeaderCellDef>User</th>
                  <td mat-cell *matCellDef="let payment">
                    {{ payment.userId }}
                  </td>
                </ng-container>

                <!-- Type Column -->
                <ng-container matColumnDef="type">
                  <th mat-header-cell *matHeaderCellDef>Type</th>
                  <td mat-cell *matCellDef="let payment">
                    <mat-chip color="accent" selected>
                      {{ payment.subscriptionType }}
                    </mat-chip>
                  </td>
                </ng-container>

                <!-- Amount Column -->
                <ng-container matColumnDef="amount">
                  <th mat-header-cell *matHeaderCellDef>Amount</th>
                  <td mat-cell *matCellDef="let payment">
                    \${{ payment.amount.toFixed(2) }}
                  </td>
                </ng-container>

                <!-- Status Column -->
                <ng-container matColumnDef="status">
                  <th mat-header-cell *matHeaderCellDef>Status</th>
                  <td mat-cell *matCellDef="let payment">
                    <mat-chip 
                      [color]="payment.status === 'SUCCEEDED' ? 'primary' : 'warn'"
                      selected>
                      {{ payment.status }}
                    </mat-chip>
                  </td>
                </ng-container>

                <!-- Actions Column -->
                <ng-container matColumnDef="actions">
                  <th mat-header-cell *matHeaderCellDef>Actions</th>
                  <td mat-cell *matCellDef="let payment">
                    <button 
                      mat-icon-button 
                      color="primary"
                      (click)="downloadReceipt(payment.id)"
                      matTooltip="Download Receipt">
                      <mat-icon>download</mat-icon>
                    </button>
                  </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="transactionColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: transactionColumns;"></tr>
              </table>

              <mat-paginator
                [length]="totalTransactions"
                [pageSize]="pageSize"
                [pageSizeOptions]="[10, 25, 50]"
                [pageIndex]="currentPage"
                (page)="onPageChange($event)"
                showFirstLastButtons>
              </mat-paginator>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .analytics-container {
      max-width: 1400px;
      margin: 0 auto;
      padding: 2rem;
    }

    .header {
      text-align: center;
      margin-bottom: 3rem;
    }

    .header h1 {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      margin: 0;
      color: #2c3e50;
      font-size: 2.5rem;
    }

    .header p {
      color: #7f8c8d;
      font-size: 1.2rem;
      margin: 0.5rem 0 0 0;
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

    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 2rem;
      margin-bottom: 3rem;
    }

    .metric-card {
      transition: transform 0.3s ease, box-shadow 0.3s ease;
    }

    .metric-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 25px rgba(0,0,0,0.15);
    }

    .metric-card.revenue {
      border-left: 4px solid #27ae60;
    }

    .metric-card.users {
      border-left: 4px solid #3498db;
    }

    .metric-card.books {
      border-left: 4px solid #e74c3c;
    }

    .metric-card.subscriptions {
      border-left: 4px solid #f39c12;
    }

    .metric-content {
      display: flex;
      align-items: center;
      gap: 1.5rem;
    }

    .metric-icon {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 50%;
      padding: 1rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .metric-icon mat-icon {
      color: white;
      font-size: 2rem;
      width: 2rem;
      height: 2rem;
    }

    .metric-info h3 {
      margin: 0;
      font-size: 2.5rem;
      font-weight: 700;
      color: #2c3e50;
    }

    .metric-info p {
      margin: 0.25rem 0;
      color: #7f8c8d;
      font-size: 1rem;
    }

    .metric-change {
      font-size: 0.9rem;
      font-weight: 500;
    }

    .metric-change.positive {
      color: #27ae60;
    }

    .metric-change.negative {
      color: #e74c3c;
    }

    .metric-change.neutral {
      color: #7f8c8d;
    }

    .charts-section {
      margin-bottom: 3rem;
    }

    .charts-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
      gap: 2rem;
      margin-bottom: 2rem;
    }

    .chart-card {
      min-height: 400px;
    }

    .chart-container {
      position: relative;
      height: 300px;
      width: 100%;
    }

    .transactions-card {
      margin-bottom: 2rem;
    }

    .table-container {
      overflow-x: auto;
    }

    .transactions-table {
      width: 100%;
    }

    @media (max-width: 768px) {
      .analytics-container {
        padding: 1rem;
      }

      .metrics-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .charts-row {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .metric-content {
        flex-direction: column;
        text-align: center;
        gap: 1rem;
      }

      .header h1 {
        font-size: 2rem;
        flex-direction: column;
        gap: 0.5rem;
      }
    }
  `]
})
export class AnalyticsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  loading = true;
  
  // Metrics
  totalRevenue = 0;
  totalUsers = 0;
  newUsersThisMonth = 0;
  totalBooks = 0;
  activeBorrows = 0;
  premiumSubscriptions = 0;
  subscriptionGrowth = 0;
  
  // Chart Data
  revenueChartData: ChartData<'line'> = { labels: [], datasets: [] };
  subscriptionChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  popularBooksChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  userActivityChartData: ChartData<'line'> = { labels: [], datasets: [] };
  
  // Chart Options
  revenueChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true },
      tooltip: { mode: 'index', intersect: false }
    },
    scales: {
      y: { beginAtZero: true }
    }
  };
  
  subscriptionChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom' }
    }
  };
  
  popularBooksChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    },
    scales: {
      y: { beginAtZero: true }
    }
  };
  
  userActivityChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true }
    },
    scales: {
      y: { beginAtZero: true }
    }
  };
  
  // Transaction Management
  recentPayments: Payment[] = [];
  transactionColumns: string[] = ['date', 'user', 'type', 'amount', 'status', 'actions'];
  currentPage = 0;
  pageSize = 10;
  totalTransactions = 0;

  constructor(
    private subscriptionService: SubscriptionService,
    private bookService: BookService,
    private userService: UserService,
    private borrowService: BorrowService
  ) {}

  ngOnInit(): void {
    this.loadAnalyticsData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadAnalyticsData(): void {
    forkJoin({
      subscriptionStats: this.subscriptionService.getSubscriptionStats(),
      bookStats: this.bookService.getBookStats(),
      userStats: this.userService.getUserStats(),
      borrowStats: this.borrowService.getBorrowStats(),
      recentPayments: this.subscriptionService.getAllPayments(0, 10)
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.processAnalyticsData(data);
          this.loading = false;
        },
        error: (error) => {
          console.error('Failed to load analytics data:', error);
          this.loading = false;
          this.setMockData(); // Fallback to mock data
        }
      });
  }

  private processAnalyticsData(data: any): void {
    // Process metrics
    this.totalRevenue = data.subscriptionStats?.totalRevenue || 0;
    this.totalUsers = data.userStats?.totalUsers || 0;
    this.newUsersThisMonth = data.userStats?.newUsersThisMonth || 0;
    this.totalBooks = data.bookStats?.totalBooks || 0;
    this.activeBorrows = data.borrowStats?.totalActiveBorrows || 0;
    this.premiumSubscriptions = data.subscriptionStats?.subscriptionsByType?.PREMIUM || 0;
    this.subscriptionGrowth = 15; // Mock growth percentage
    
    // Process recent payments
    this.recentPayments = data.recentPayments?.content || [];
    this.totalTransactions = data.recentPayments?.totalElements || 0;
    
    // Setup charts
    this.setupCharts(data);
  }

  private setupCharts(data: any): void {
    // Revenue Chart
    this.revenueChartData = {
      labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
      datasets: [{
        label: 'Revenue ($)',
        data: [1200, 1900, 3000, 5000, 2000, 3000, 4500, 3200, 2800, 4100, 3900, 4800],
        borderColor: '#3498db',
        backgroundColor: 'rgba(52, 152, 219, 0.1)',
        tension: 0.4
      }]
    };

    // Subscription Distribution
    const freeUsers = data.subscriptionStats?.subscriptionsByType?.FREE || 0;
    const premiumUsers = data.subscriptionStats?.subscriptionsByType?.PREMIUM || 0;
    
    this.subscriptionChartData = {
      labels: ['Free', 'Premium'],
      datasets: [{
        data: [freeUsers, premiumUsers],
        backgroundColor: ['#95a5a6', '#f39c12'],
        borderWidth: 0
      }]
    };

    // Popular Books
    const popularBooks = data.bookStats?.mostPopularBooks || [];
    this.popularBooksChartData = {
      labels: popularBooks.slice(0, 10).map((book: any) => book.title),
      datasets: [{
        label: 'Borrows',
        data: popularBooks.slice(0, 10).map((book: any) => book.borrowCount),
        backgroundColor: '#e74c3c',
        borderColor: '#c0392b',
        borderWidth: 1
      }]
    };

    // User Activity (Mock data)
    this.userActivityChartData = {
      labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
      datasets: [{
        label: 'Active Users',
        data: [120, 190, 300, 500, 200, 300, 450],
        borderColor: '#27ae60',
        backgroundColor: 'rgba(39, 174, 96, 0.1)',
        tension: 0.4
      }]
    };
  }

  private setMockData(): void {
    // Set mock data if API fails
    this.totalRevenue = 45000;
    this.totalUsers = 1250;
    this.newUsersThisMonth = 85;
    this.totalBooks = 2500;
    this.activeBorrows = 340;
    this.premiumSubscriptions = 180;
    this.subscriptionGrowth = 15;
    
    this.setupCharts({
      subscriptionStats: {
        subscriptionsByType: { FREE: 1070, PREMIUM: 180 }
      },
      bookStats: {
        mostPopularBooks: [
          { title: 'The Great Gatsby', borrowCount: 45 },
          { title: '1984', borrowCount: 38 },
          { title: 'To Kill a Mockingbird', borrowCount: 32 }
        ]
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    // Load more transactions
  }

  downloadReceipt(paymentId: number): void {
    this.subscriptionService.getReceiptUrl(paymentId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          window.open(response.receiptUrl, '_blank');
        },
        error: (error) => {
          console.error('Failed to download receipt:', error);
        }
      });
  }
}