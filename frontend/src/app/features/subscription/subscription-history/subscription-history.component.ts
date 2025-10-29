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
import { Subject, takeUntil } from 'rxjs';

import { SubscriptionService } from '@core/services/subscription.service';
import { Subscription, SubscriptionType } from '@core/models/subscription.model';

@Component({
  selector: 'app-subscription-history',
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
    MatChipsModule
  ],
  template: `
    <div class="subscription-history-container">
      <div class="header">
        <button mat-icon-button routerLink="/subscription/plans" class="back-button">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <div class="header-content">
          <h1>Subscription History</h1>
          <p>View your subscription changes and billing history</p>
        </div>
      </div>

      <div class="loading-container" *ngIf="loading">
        <mat-spinner></mat-spinner>
        <p>Loading subscription history...</p>
      </div>

      <div class="content" *ngIf="!loading">
        <mat-card class="current-subscription-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon color="primary">card_membership</mat-icon>
              Current Subscription
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="current-subscription" *ngIf="currentSubscription">
              <div class="subscription-info">
                <mat-chip [color]="currentSubscription.type === 'PREMIUM' ? 'accent' : 'primary'" selected>
                  {{ currentSubscription.type === 'PREMIUM' ? 'Premium' : 'Free' }}
                </mat-chip>
                <div class="dates">
                  <p><strong>Started:</strong> {{ currentSubscription.startDate | date:'mediumDate' }}</p>
                  <p><strong>{{ currentSubscription.type === 'PREMIUM' ? 'Expires' : 'Active since' }}:</strong> 
                     {{ currentSubscription.endDate | date:'mediumDate' }}</p>
                </div>
              </div>
              <div class="subscription-actions" *ngIf="currentSubscription.type === 'FREE'">
                <button mat-raised-button color="accent" routerLink="/subscription/upgrade">
                  <mat-icon>upgrade</mat-icon>
                  Upgrade to Premium
                </button>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="history-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon color="primary">history</mat-icon>
              Subscription History
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="no-data" *ngIf="subscriptions.length === 0">
              <mat-icon>inbox</mat-icon>
              <p>No subscription history found</p>
            </div>

            <div class="table-container" *ngIf="subscriptions.length > 0">
              <table mat-table [dataSource]="subscriptions" class="subscription-table">
                <!-- Type Column -->
                <ng-container matColumnDef="type">
                  <th mat-header-cell *matHeaderCellDef>Plan</th>
                  <td mat-cell *matCellDef="let subscription">
                    <mat-chip 
                      [color]="subscription.type === 'PREMIUM' ? 'accent' : 'primary'" 
                      selected>
                      {{ subscription.type === 'PREMIUM' ? 'Premium' : 'Free' }}
                    </mat-chip>
                  </td>
                </ng-container>

                <!-- Start Date Column -->
                <ng-container matColumnDef="startDate">
                  <th mat-header-cell *matHeaderCellDef>Start Date</th>
                  <td mat-cell *matCellDef="let subscription">
                    {{ subscription.startDate | date:'mediumDate' }}
                  </td>
                </ng-container>

                <!-- End Date Column -->
                <ng-container matColumnDef="endDate">
                  <th mat-header-cell *matHeaderCellDef>End Date</th>
                  <td mat-cell *matCellDef="let subscription">
                    {{ subscription.endDate | date:'mediumDate' }}
                  </td>
                </ng-container>

                <!-- Status Column -->
                <ng-container matColumnDef="status">
                  <th mat-header-cell *matHeaderCellDef>Status</th>
                  <td mat-cell *matCellDef="let subscription">
                    <mat-chip 
                      [color]="getStatusColor(subscription)"
                      selected>
                      {{ getStatusText(subscription) }}
                    </mat-chip>
                  </td>
                </ng-container>

                <!-- Duration Column -->
                <ng-container matColumnDef="duration">
                  <th mat-header-cell *matHeaderCellDef>Duration</th>
                  <td mat-cell *matCellDef="let subscription">
                    {{ calculateDuration(subscription.startDate, subscription.endDate) }} days
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
              <button mat-raised-button color="primary" routerLink="/subscription/payment-history">
                <mat-icon>receipt</mat-icon>
                View Payment History
              </button>
              <button mat-button routerLink="/subscription/plans">
                <mat-icon>compare</mat-icon>
                Compare Plans
              </button>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .subscription-history-container {
      max-width: 1000px;
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

    .current-subscription-card,
    .history-card,
    .actions-card {
      margin-bottom: 2rem;
    }

    .current-subscription {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
    }

    .subscription-info {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .dates p {
      margin: 0.25rem 0;
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

    .subscription-table {
      width: 100%;
    }

    .action-buttons {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
    }

    @media (max-width: 768px) {
      .subscription-history-container {
        padding: 1rem;
      }

      .header {
        flex-direction: column;
        align-items: flex-start;
      }

      .back-button {
        margin-bottom: 1rem;
      }

      .current-subscription {
        flex-direction: column;
        align-items: flex-start;
      }

      .action-buttons {
        flex-direction: column;
      }

      .action-buttons button {
        width: 100%;
      }
    }
  `]
})
export class SubscriptionHistoryComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  subscriptions: Subscription[] = [];
  currentSubscription: Subscription | null = null;
  loading = true;
  
  displayedColumns: string[] = ['type', 'startDate', 'endDate', 'status', 'duration'];
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;

  constructor(private subscriptionService: SubscriptionService) {}

  ngOnInit(): void {
    this.loadCurrentSubscription();
    this.loadSubscriptionHistory();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadCurrentSubscription(): void {
    this.subscriptionService.getCurrentSubscription()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (subscription) => {
          this.currentSubscription = subscription;
        },
        error: (error) => {
          console.error('Failed to load current subscription:', error);
        }
      });
  }

  private loadSubscriptionHistory(): void {
    this.subscriptionService.getSubscriptionHistory(this.currentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.subscriptions = response.content;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: (error) => {
          console.error('Failed to load subscription history:', error);
          this.loading = false;
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadSubscriptionHistory();
  }

  getStatusColor(subscription: Subscription): string {
    const now = new Date();
    const endDate = new Date(subscription.endDate);
    
    if (endDate > now) {
      return 'primary'; // Active
    } else {
      return 'warn'; // Expired
    }
  }

  getStatusText(subscription: Subscription): string {
    const now = new Date();
    const endDate = new Date(subscription.endDate);
    
    if (endDate > now) {
      return 'Active';
    } else {
      return 'Expired';
    }
  }

  calculateDuration(startDate: string, endDate: string): number {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = Math.abs(end.getTime() - start.getTime());
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }
}