import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subject, takeUntil } from 'rxjs';

import { SubscriptionService } from '@core/services/subscription.service';
import { AuthService } from '@core/services/auth.service';
import { SubscriptionPlan, SubscriptionType } from '@core/models/subscription.model';
import { User } from '@core/models/user.model';

@Component({
  selector: 'app-subscription-plans',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="subscription-plans-container">
      <div class="header">
        <h1>Choose Your Plan</h1>
        <p>Select the perfect subscription plan for your reading needs</p>
      </div>

      <div class="loading-container" *ngIf="loading">
        <mat-spinner></mat-spinner>
        <p>Loading subscription plans...</p>
      </div>

      <div class="plans-grid" *ngIf="!loading">
        <mat-card 
          class="plan-card" 
          [class.current-plan]="currentSubscription?.type === plan.type"
          *ngFor="let plan of plans">
          
          <mat-card-header>
            <mat-card-title class="plan-title">
              <mat-icon [color]="plan.type === 'PREMIUM' ? 'accent' : 'primary'">
                {{ plan.type === 'PREMIUM' ? 'star' : 'book' }}
              </mat-icon>
              {{ plan.name }}
            </mat-card-title>
            <mat-card-subtitle>
              <span class="price">\${{ plan.price }}</span>
              <span class="period">/month</span>
            </mat-card-subtitle>
          </mat-card-header>

          <mat-card-content>
            <div class="features-list">
              <div class="feature" *ngFor="let feature of plan.features">
                <mat-icon color="primary">check_circle</mat-icon>
                <span>{{ feature }}</span>
              </div>
            </div>

            <div class="limits">
              <div class="limit-item">
                <strong>Books per month:</strong> 
                {{ plan.bookLimit === -1 ? 'Unlimited' : plan.bookLimit }}
              </div>
              <div class="limit-item">
                <strong>Borrow duration:</strong> 
                {{ plan.durationDays }} days
              </div>
              <div class="limit-item">
                <strong>Fine per day:</strong> 
                \${{ plan.dailyFineAmount }}
              </div>
            </div>
          </mat-card-content>

          <mat-card-actions>
            <div class="action-buttons">
              <mat-chip 
                *ngIf="currentSubscription?.type === plan.type"
                color="accent"
                selected>
                Current Plan
              </mat-chip>
              
              <button 
                mat-raised-button 
                [color]="plan.type === 'PREMIUM' ? 'accent' : 'primary'"
                [disabled]="currentSubscription?.type === plan.type"
                [routerLink]="plan.type === 'PREMIUM' ? '/subscription/upgrade' : null"
                *ngIf="plan.type === 'PREMIUM' && currentSubscription?.type !== 'PREMIUM'">
                <mat-icon>upgrade</mat-icon>
                Upgrade Now
              </button>

              <button 
                mat-stroked-button 
                color="primary"
                disabled
                *ngIf="plan.type === 'FREE'">
                <mat-icon>check</mat-icon>
                Default Plan
              </button>
            </div>
          </mat-card-actions>
        </mat-card>
      </div>

      <div class="comparison-section" *ngIf="!loading">
        <h2>Plan Comparison</h2>
        <div class="comparison-table">
          <table>
            <thead>
              <tr>
                <th>Feature</th>
                <th>Free</th>
                <th>Premium</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Books per month</td>
                <td>2</td>
                <td>Unlimited</td>
              </tr>
              <tr>
                <td>Borrow duration</td>
                <td>14 days</td>
                <td>30 days</td>
              </tr>
              <tr>
                <td>Late fee per day</td>
                <td>$2.00</td>
                <td>$1.00</td>
              </tr>
              <tr>
                <td>Priority support</td>
                <td><mat-icon color="warn">close</mat-icon></td>
                <td><mat-icon color="primary">check</mat-icon></td>
              </tr>
              <tr>
                <td>Early access to new books</td>
                <td><mat-icon color="warn">close</mat-icon></td>
                <td><mat-icon color="primary">check</mat-icon></td>
              </tr>
              <tr>
                <td>Extended reading time</td>
                <td><mat-icon color="warn">close</mat-icon></td>
                <td><mat-icon color="primary">check</mat-icon></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .subscription-plans-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .header {
      text-align: center;
      margin-bottom: 3rem;
    }

    .header h1 {
      font-size: 2.5rem;
      margin-bottom: 1rem;
      color: #2c3e50;
    }

    .header p {
      font-size: 1.2rem;
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

    .plans-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
      gap: 2rem;
      margin-bottom: 4rem;
    }

    .plan-card {
      position: relative;
      transition: transform 0.3s ease, box-shadow 0.3s ease;
    }

    .plan-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 25px rgba(0,0,0,0.15);
    }

    .plan-card.current-plan {
      border: 2px solid #ff4081;
      box-shadow: 0 4px 20px rgba(255, 64, 129, 0.2);
    }

    .plan-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1.5rem;
    }

    .price {
      font-size: 2rem;
      font-weight: bold;
      color: #2c3e50;
    }

    .period {
      color: #7f8c8d;
      font-size: 1rem;
    }

    .features-list {
      margin: 1.5rem 0;
    }

    .feature {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 0.5rem;
    }

    .limits {
      background-color: #f8f9fa;
      padding: 1rem;
      border-radius: 8px;
      margin-top: 1rem;
    }

    .limit-item {
      margin-bottom: 0.5rem;
    }

    .limit-item:last-child {
      margin-bottom: 0;
    }

    .action-buttons {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      width: 100%;
    }

    .comparison-section {
      margin-top: 4rem;
    }

    .comparison-section h2 {
      text-align: center;
      margin-bottom: 2rem;
      color: #2c3e50;
    }

    .comparison-table {
      overflow-x: auto;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      background: white;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }

    th, td {
      padding: 1rem;
      text-align: left;
      border-bottom: 1px solid #ecf0f1;
    }

    th {
      background-color: #3498db;
      color: white;
      font-weight: 600;
    }

    tr:hover {
      background-color: #f8f9fa;
    }

    @media (max-width: 768px) {
      .subscription-plans-container {
        padding: 1rem;
      }

      .plans-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .header h1 {
        font-size: 2rem;
      }

      table {
        font-size: 0.9rem;
      }

      th, td {
        padding: 0.5rem;
      }
    }
  `]
})
export class SubscriptionPlansComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  plans: SubscriptionPlan[] = [];
  currentUser: User | null = null;
  currentSubscription: any = null;
  loading = true;

  constructor(
    private subscriptionService: SubscriptionService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadCurrentSubscription();
    this.loadSubscriptionPlans();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadCurrentUser(): void {
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        this.currentUser = user;
      });
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
          // Set default free subscription if API fails
          this.currentSubscription = { type: 'FREE' };
        }
      });
  }

  private loadSubscriptionPlans(): void {
    this.subscriptionService.getAvailablePlans()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (plans) => {
          this.plans = plans;
          this.loading = false;
        },
        error: (error) => {
          console.error('Failed to load subscription plans:', error);
          this.loading = false;
          // Set default plans if API fails
          this.setDefaultPlans();
        }
      });
  }

  private setDefaultPlans(): void {
    this.plans = [
      {
        type: SubscriptionType.FREE,
        name: 'Free Plan',
        price: 0,
        bookLimit: 2,
        durationDays: 14,
        dailyFineAmount: 2.00,
        features: [
          '2 books per month',
          '14-day borrow period',
          'Basic book catalog access',
          'Standard support'
        ]
      },
      {
        type: SubscriptionType.PREMIUM,
        name: 'Premium Plan',
        price: 9.99,
        bookLimit: -1,
        durationDays: 30,
        dailyFineAmount: 1.00,
        features: [
          'Unlimited books per month',
          '30-day borrow period',
          'Full catalog access',
          'Priority support',
          'Early access to new releases',
          'Extended reading sessions',
          'Lower late fees'
        ]
      }
    ];
  }
}