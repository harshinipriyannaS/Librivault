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
import { MatMenuModule } from '@angular/material/menu';
import { Subject, takeUntil } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

import { SubscriptionService } from '@core/services/subscription.service';
import { Payment, PaymentStatus } from '@core/models/subscription.model';

@Component({
  selector: 'app-payment-history',
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
    MatMenuModule
  ],
  template: `
    <div class="payment-history-container">
      <div class="header">
        <button mat-icon-button routerLink="/subscription/history" class="back-button">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <div class="header-content">
          <h1>Payment History</h1>
          <p>View all your payment transactions and receipts</p>
        </div>
      </div>

      <div class="loading-container" *ngIf="loading">
        <mat-spinner></mat-spinner>
        <p>Loading payment history...</p>
      </div>

      <div class="content" *ngIf="!loading">
        <mat-card class="summary-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon color="primary">account_balance_wallet</mat-icon>
              Payment Summary
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="summary-stats">
              <div class="stat-item">
                <span class="stat-value">{{ totalPayments }}</span>
                <span class="stat-label">Total Payments</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">\${{ totalAmount.toFixed(2) }}</span>
                <span class="stat-label">Total Spent</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ successfulPayments }}</span>
                <span class="stat-label">Successful</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="payments-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon color="primary">receipt</mat-icon>
              Payment Transactions
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="no-data" *ngIf="payments.length === 0">
              <mat-icon>inbox</mat-icon>
              <p>No payment history found</p>
              <button mat-raised-button color="accent" routerLink="/subscription/upgrade">
                <mat-icon>upgrade</mat-icon>
                Upgrade to Premium
              </button>
            </div>

            <div class="table-container" *ngIf="payments.length > 0">
              <table mat-table [dataSource]="payments" class="payments-table">
                <!-- Date Column -->
                <ng-container matColumnDef="date">
                  <th mat-header-cell *matHeaderCellDef>Date</th>
                  <td mat-cell *matCellDef="let payment">
                    {{ payment.createdAt | date:'medium' }}
                  </td>
                </ng-container>

                <!-- Description Column -->
                <ng-container matColumnDef="description">
                  <th mat-header-cell *matHeaderCellDef>Description</th>
                  <td mat-cell *matCellDef="let payment">
                    <div class="payment-description">
                      <span class="description-text">{{ getPaymentDescription(payment) }}</span>
                      <span class="payment-id">ID: {{ payment.id }}</span>
                    </div>
                  </td>
                </ng-container>

                <!-- Amount Column -->
                <ng-container matColumnDef="amount">
                  <th mat-header-cell *matHeaderCellDef>Amount</th>
                  <td mat-cell *matCellDef="let payment">
                    <span class="amount">
                      \${{ payment.amount.toFixed(2) }} {{ payment.currency.toUpperCase() }}
                    </span>
                  </td>
                </ng-container>

                <!-- Status Column -->
                <ng-container matColumnDef="status">
                  <th mat-header-cell *matHeaderCellDef>Status</th>
                  <td mat-cell *matCellDef="let payment">
                    <mat-chip 
                      [color]="getStatusColor(payment.status)"
                      selected>
                      <mat-icon>{{ getStatusIcon(payment.status) }}</mat-icon>
                      {{ getStatusText(payment.status) }}
                    </mat-chip>
                  </td>
                </ng-container>

                <!-- Actions Column -->
                <ng-container matColumnDef="actions">
                  <th mat-header-cell *matHeaderCellDef>Actions</th>
                  <td mat-cell *matCellDef="let payment">
                    <button 
                      mat-icon-button 
                      [matMenuTriggerFor]="actionMenu"
                      [disabled]="payment.status !== 'SUCCEEDED'">
                      <mat-icon>more_vert</mat-icon>
                    </button>
                    <mat-menu #actionMenu="matMenu">
                      <button mat-menu-item (click)="downloadReceipt(payment.id)">
                        <mat-icon>download</mat-icon>
                        <span>Download Receipt</span>
                      </button>
                      <button mat-menu-item (click)="viewReceiptOnline(payment.id)">
                        <mat-icon>open_in_new</mat-icon>
                        <span>View Receipt Online</span>
                      </button>
                    </mat-menu>
                  </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
              </table>

              <mat-paginator
                [length]="totalElements"
                [pageSize]="pageSize"
                [pageSizeOptions]="[5, 10, 20]"
                [pageIndex]="currentPage"
                (page)="onPageChange($event)"
                showFirstLastButtons>
              </mat-paginator>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="actions-card">
          <mat-card-content>
            <div class="action-buttons">
              <button mat-raised-button color="primary" routerLink="/subscription/history">
                <mat-icon>history</mat-icon>
                Subscription History
              </button>
              <button mat-button routerLink="/subscription/plans">
                <mat-icon>compare</mat-icon>
                View Plans
              </button>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .payment-history-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .header {
      display: flex;
      align-items: center;
      margin-bottom: 2rem;
    }

    .back-button {
      margin-right: 1rem;
    }

    .header-content h1 {
      margin: 0;
      color: #2c3e50;
    }

    .header-content p {
      margin: 0.5rem 0 0 0;
      color: #7f8c8d;
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

    .summary-card,
    .payments-card,
    .actions-card {
      margin-bottom: 2rem;
    }

    .summary-stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 2rem;
      text-align: center;
    }

    .stat-item {
      display: flex;
      flex-direction: column;
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

    .no-data {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem;
      color: #7f8c8d;
    }

    .no-data mat-icon {
      font-size: 3rem;
      width: 3rem;
      height: 3rem;
      margin-bottom: 1rem;
    }

    .table-container {
      overflow-x: auto;
    }

    .payments-table {
      width: 100%;
    }

    .payment-description {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .description-text {
      font-weight: 500;
    }

    .payment-id {
      font-size: 0.8rem;
      color: #7f8c8d;
    }

    .amount {
      font-weight: 600;
      color: #2c3e50;
    }

    .mat-chip {
      display: flex;
      align-items: center;
      gap: 0.25rem;
    }

    .action-buttons {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
    }

    @media (max-width: 768px) {
      .payment-history-container {
        padding: 1rem;
      }

      .header {
        flex-direction: column;
        align-items: flex-start;
      }

      .back-button {
        margin-bottom: 1rem;
      }

      .summary-stats {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .action-buttons {
        flex-direction: column;
      }

      .action-buttons button {
        width: 100%;
      }

      .displayedColumns {
        /* Hide some columns on mobile */
      }
    }
  `]
})
export class PaymentHistoryComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  payments: Payment[] = [];
  loading = true;
  
  displayedColumns: string[] = ['date', 'description', 'amount', 'status', 'actions'];
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  
  // Summary stats
  totalPayments = 0;
  totalAmount = 0;
  successfulPayments = 0;

  constructor(
    private subscriptionService: SubscriptionService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadPaymentHistory();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadPaymentHistory(): void {
    this.subscriptionService.getPaymentHistory(this.currentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.payments = response.content;
          this.totalElements = response.totalElements;
          this.calculateSummaryStats();
          this.loading = false;
        },
        error: (error) => {
          console.error('Failed to load payment history:', error);
          this.loading = false;
        }
      });
  }

  private calculateSummaryStats(): void {
    this.totalPayments = this.payments.length;
    this.totalAmount = this.payments.reduce((sum, payment) => sum + payment.amount, 0);
    this.successfulPayments = this.payments.filter(p => p.status === PaymentStatus.SUCCEEDED).length;
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPaymentHistory();
  }

  getPaymentDescription(payment: Payment): string {
    switch (payment.subscriptionType) {
      case 'PREMIUM':
        return 'Premium Subscription';
      case 'FREE':
        return 'Free Plan (No charge)';
      default:
        return 'Subscription Payment';
    }
  }

  getStatusColor(status: PaymentStatus): string {
    switch (status) {
      case PaymentStatus.SUCCEEDED:
        return 'primary';
      case PaymentStatus.PENDING:
        return 'accent';
      case PaymentStatus.FAILED:
        return 'warn';
      default:
        return '';
    }
  }

  getStatusIcon(status: PaymentStatus): string {
    switch (status) {
      case PaymentStatus.SUCCEEDED:
        return 'check_circle';
      case PaymentStatus.PENDING:
        return 'schedule';
      case PaymentStatus.FAILED:
        return 'error';
      default:
        return 'help';
    }
  }

  getStatusText(status: PaymentStatus): string {
    switch (status) {
      case PaymentStatus.SUCCEEDED:
        return 'Completed';
      case PaymentStatus.PENDING:
        return 'Pending';
      case PaymentStatus.FAILED:
        return 'Failed';
      default:
        return 'Unknown';
    }
  }

  downloadReceipt(paymentId: number): void {
    this.subscriptionService.getReceiptUrl(paymentId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          // Create a temporary link to download the receipt
          const link = document.createElement('a');
          link.href = response.receiptUrl;
          link.download = `receipt-${paymentId}.pdf`;
          link.click();
          this.toastr.success('Receipt download started');
        },
        error: (error) => {
          console.error('Failed to download receipt:', error);
          this.toastr.error('Failed to download receipt');
        }
      });
  }

  viewReceiptOnline(paymentId: number): void {
    this.subscriptionService.getReceiptUrl(paymentId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          window.open(response.receiptUrl, '_blank');
        },
        error: (error) => {
          console.error('Failed to view receipt:', error);
          this.toastr.error('Failed to view receipt');
        }
      });
  }
}