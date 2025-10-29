import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subject, takeUntil, forkJoin } from 'rxjs';

import { AuthService } from '@core/services/auth.service';
import { BorrowService } from '@core/services/borrow.service';
import { User } from '@core/models/user.model';
import { BorrowRecord } from '@core/models/borrow.model';

interface CreditTransaction {
  id: number;
  type: 'EARNED' | 'USED';
  amount: number;
  description: string;
  bookTitle: string;
  date: string;
  borrowRecordId?: number;
}

@Component({
  selector: 'app-credit-balance',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTooltipModule
  ],
  template: `
    <div class="credit-balance-container">
      <div class="header">
        <h2>
          <mat-icon color="accent">stars</mat-icon>
          Reader Credits
        </h2>
        <p>Earn credits by returning books early and use them for extended borrowing</p>
      </div>

      <div class="loading-container" *ngIf="loading">
        <mat-spinner></mat-spinner>
        <p>Loading credit information...</p>
      </div>

      <div class="content" *ngIf="!loading">
        <!-- Current Balance Card -->
        <mat-card class="balance-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon color="accent">account_balance_wallet</mat-icon>
              Current Balance
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="balance-display">
              <div class="balance-amount">
                <span class="credits-value">{{ currentUser?.readerCredits || 0 }}</span>
                <span class="credits-label">Credits</span>
              </div>
              <div class="balance-info">
                <div class="info-item">
                  <mat-icon>info</mat-icon>
                  <span>1 credit = 1 day extension</span>
                </div>
                <div class="info-item">
                  <mat-icon>schedule</mat-icon>
                  <span>Earn credits by returning books early</span>
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Credit Statistics -->
        <mat-card class="stats-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon color="primary">analytics</mat-icon>
              Credit Statistics
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ totalCreditsEarned }}</div>
                <div class="stat-label">Total Earned</div>
                <mat-icon color="primary">trending_up</mat-icon>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ totalCreditsUsed }}</div>
                <div class="stat-label">Total Used</div>
                <mat-icon color="accent">trending_down</mat-icon>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ booksReturnedEarly }}</div>
                <div class="stat-label">Early Returns</div>
                <mat-icon color="primary">schedule</mat-icon>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- How Credits Work -->
        <mat-card class="info-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon color="primary">help</mat-icon>
              How Credits Work
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-sections">
              <div class="info-section">
                <h3>
                  <mat-icon color="primary">add_circle</mat-icon>
                  Earning Credits
                </h3>
                <ul>
                  <li>Return books before the due date to earn credits</li>
                  <li>1 credit earned for each day returned early</li>
                  <li>Maximum 7 credits per book</li>
                  <li>Credits are automatically added to your balance</li>
                </ul>
              </div>
              
              <div class="info-section">
                <h3>
                  <mat-icon color="accent">remove_circle</mat-icon>
                  Using Credits
                </h3>
                <ul>
                  <li>Use credits to extend borrowing periods</li>
                  <li>1 credit = 1 day extension</li>
                  <li>Credits can be used before or during borrowing</li>
                  <li>No expiration date on credits</li>
                </ul>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Recent Credit Activity -->
        <mat-card class="activity-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon color="primary">history</mat-icon>
              Recent Credit Activity
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="no-activity" *ngIf="creditTransactions.length === 0">
              <mat-icon>inbox</mat-icon>
              <p>No credit activity yet</p>
              <p class="hint">Start borrowing and returning books to earn credits!</p>
            </div>

            <div class="activity-list" *ngIf="creditTransactions.length > 0">
              <div 
                class="activity-item" 
                *ngFor="let transaction of creditTransactions"
                [class.earned]="transaction.type === 'EARNED'"
                [class.used]="transaction.type === 'USED'">
                
                <div class="activity-icon">
                  <mat-icon [color]="transaction.type === 'EARNED' ? 'primary' : 'accent'">
                    {{ transaction.type === 'EARNED' ? 'add_circle' : 'remove_circle' }}
                  </mat-icon>
                </div>
                
                <div class="activity-details">
                  <div class="activity-description">{{ transaction.description }}</div>
                  <div class="activity-book">{{ transaction.bookTitle }}</div>
                  <div class="activity-date">{{ transaction.date | date:'medium' }}</div>
                </div>
                
                <div class="activity-amount">
                  <mat-chip 
                    [color]="transaction.type === 'EARNED' ? 'primary' : 'accent'"
                    selected>
                    {{ transaction.type === 'EARNED' ? '+' : '-' }}{{ transaction.amount }}
                  </mat-chip>
                </div>
              </div>
            </div>

            <div class="activity-actions" *ngIf="creditTransactions.length > 5">
              <button mat-button color="primary" routerLink="/dashboard/my-borrows">
                <mat-icon>visibility</mat-icon>
                View All Borrow History
              </button>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .credit-balance-container {
      max-width: 1000px;
      margin: 0 auto;
      padding: 1rem;
    }

    .header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .header h2 {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      margin: 0;
      color: #2c3e50;
    }

    .header p {
      color: #7f8c8d;
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

    .balance-card,
    .stats-card,
    .info-card,
    .activity-card {
      margin-bottom: 2rem;
    }

    .balance-display {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 2rem;
    }

    .balance-amount {
      text-align: center;
    }

    .credits-value {
      font-size: 4rem;
      font-weight: bold;
      color: #f39c12;
      display: block;
    }

    .credits-label {
      font-size: 1.2rem;
      color: #7f8c8d;
    }

    .balance-info {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      width: 100%;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #7f8c8d;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 2rem;
      text-align: center;
    }

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
    }

    .stat-value {
      font-size: 2rem;
      font-weight: bold;
      color: #2c3e50;
    }

    .stat-label {
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .info-sections {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 2rem;
    }

    .info-section h3 {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 1rem;
      color: #2c3e50;
    }

    .info-section ul {
      list-style: none;
      padding: 0;
    }

    .info-section li {
      margin-bottom: 0.5rem;
      padding-left: 1rem;
      position: relative;
    }

    .info-section li::before {
      content: '•';
      color: #3498db;
      position: absolute;
      left: 0;
    }

    .no-activity {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem;
      color: #7f8c8d;
    }

    .no-activity mat-icon {
      font-size: 3rem;
      width: 3rem;
      height: 3rem;
      margin-bottom: 1rem;
    }

    .hint {
      font-size: 0.9rem;
      margin-top: 0.5rem;
    }

    .activity-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .activity-item {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1rem;
      border-radius: 8px;
      background-color: #f8f9fa;
      transition: background-color 0.3s ease;
    }

    .activity-item:hover {
      background-color: #e9ecef;
    }

    .activity-item.earned {
      border-left: 4px solid #27ae60;
    }

    .activity-item.used {
      border-left: 4px solid #e74c3c;
    }

    .activity-details {
      flex: 1;
    }

    .activity-description {
      font-weight: 500;
      color: #2c3e50;
    }

    .activity-book {
      color: #7f8c8d;
      font-size: 0.9rem;
      margin: 0.25rem 0;
    }

    .activity-date {
      color: #95a5a6;
      font-size: 0.8rem;
    }

    .activity-actions {
      text-align: center;
      margin-top: 2rem;
      padding-top: 1rem;
      border-top: 1px solid #ecf0f1;
    }

    @media (max-width: 768px) {
      .credit-balance-container {
        padding: 0.5rem;
      }

      .balance-display {
        gap: 1rem;
      }

      .credits-value {
        font-size: 3rem;
      }

      .stats-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .info-sections {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .activity-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
      }

      .activity-amount {
        align-self: flex-end;
      }
    }
  `]
})
export class CreditBalanceComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  currentUser: User | null = null;
  creditTransactions: CreditTransaction[] = [];
  loading = true;
  
  // Statistics
  totalCreditsEarned = 0;
  totalCreditsUsed = 0;
  booksReturnedEarly = 0;

  constructor(
    private authService: AuthService,
    private borrowService: BorrowService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadData(): void {
    forkJoin({
      user: this.authService.currentUser$,
      borrowHistory: this.borrowService.getMyBorrowRecords(0, 50)
    }).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ user, borrowHistory }) => {
          this.currentUser = user;
          this.processBorrowHistory(borrowHistory.content);
          this.loading = false;
        },
        error: (error) => {
          console.error('Failed to load credit data:', error);
          this.loading = false;
        }
      });
  }

  private processBorrowHistory(borrowRecords: BorrowRecord[]): void {
    this.creditTransactions = [];
    this.totalCreditsEarned = 0;
    this.totalCreditsUsed = 0;
    this.booksReturnedEarly = 0;

    borrowRecords.forEach(record => {
      // Count credits earned
      if (record.creditsEarned > 0) {
        this.totalCreditsEarned += record.creditsEarned;
        this.booksReturnedEarly++;
        
        this.creditTransactions.push({
          id: record.id,
          type: 'EARNED',
          amount: record.creditsEarned,
          description: 'Early return bonus',
          bookTitle: record.bookTitle,
          date: record.returnedAt || record.borrowedAt,
          borrowRecordId: record.id
        });
      }

      // Count credits used
      if (record.usedCredit) {
        this.totalCreditsUsed += 1; // Assuming 1 credit used per extension
        
        this.creditTransactions.push({
          id: record.id,
          type: 'USED',
          amount: 1,
          description: 'Extended borrowing period',
          bookTitle: record.bookTitle,
          date: record.borrowedAt,
          borrowRecordId: record.id
        });
      }
    });

    // Sort transactions by date (most recent first)
    this.creditTransactions.sort((a, b) => 
      new Date(b.date).getTime() - new Date(a.date).getTime()
    );

    // Limit to recent transactions for display
    this.creditTransactions = this.creditTransactions.slice(0, 10);
  }
}