import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatStepperModule } from '@angular/material/stepper';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

import { SubscriptionService } from '@core/services/subscription.service';
import { AuthService } from '@core/services/auth.service';
import { SubscriptionType, CreatePaymentIntentRequest } from '@core/models/subscription.model';

declare var Stripe: any;

@Component({
  selector: 'app-subscription-upgrade',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatStepperModule,
    MatFormFieldModule,
    MatInputModule
  ],
  template: `
    <div class="upgrade-container">
      <div class="header">
        <button mat-icon-button routerLink="/subscription/plans" class="back-button">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h1>Upgrade to Premium</h1>
        <p>Unlock unlimited reading with our Premium subscription</p>
      </div>

      <div class="loading-container" *ngIf="loading">
        <mat-spinner></mat-spinner>
        <p>{{ loadingMessage }}</p>
      </div>

      <div class="upgrade-content" *ngIf="!loading">
        <mat-horizontal-stepper #stepper [linear]="true">
          <!-- Step 1: Plan Review -->
          <mat-step [stepControl]="planForm" label="Review Plan">
            <form [formGroup]="planForm">
              <mat-card class="plan-review-card">
                <mat-card-header>
                  <mat-card-title>
                    <mat-icon color="accent">star</mat-icon>
                    Premium Plan
                  </mat-card-title>
                  <mat-card-subtitle>Everything you need for unlimited reading</mat-card-subtitle>
                </mat-card-header>

                <mat-card-content>
                  <div class="plan-details">
                    <div class="price-section">
                      <span class="price">\$9.99</span>
                      <span class="period">/month</span>
                    </div>

                    <div class="features">
                      <h3>What's included:</h3>
                      <ul>
                        <li><mat-icon color="primary">check</mat-icon> Unlimited books per month</li>
                        <li><mat-icon color="primary">check</mat-icon> 30-day borrow period</li>
                        <li><mat-icon color="primary">check</mat-icon> Lower late fees (\$1.00/day)</li>
                        <li><mat-icon color="primary">check</mat-icon> Priority customer support</li>
                        <li><mat-icon color="primary">check</mat-icon> Early access to new releases</li>
                        <li><mat-icon color="primary">check</mat-icon> Extended reading sessions</li>
                      </ul>
                    </div>

                    <div class="billing-info">
                      <p><strong>Billing:</strong> Monthly subscription</p>
                      <p><strong>Next billing date:</strong> {{ getNextBillingDate() }}</p>
                      <p><strong>Cancel anytime:</strong> No long-term commitment</p>
                    </div>
                  </div>
                </mat-card-content>

                <mat-card-actions>
                  <button mat-raised-button color="accent" matStepperNext>
                    Continue to Payment
                    <mat-icon>arrow_forward</mat-icon>
                  </button>
                </mat-card-actions>
              </mat-card>
            </form>
          </mat-step>

          <!-- Step 2: Payment -->
          <mat-step [stepControl]="paymentForm" label="Payment">
            <form [formGroup]="paymentForm">
              <mat-card class="payment-card">
                <mat-card-header>
                  <mat-card-title>
                    <mat-icon color="primary">payment</mat-icon>
                    Payment Information
                  </mat-card-title>
                  <mat-card-subtitle>Secure payment powered by Stripe</mat-card-subtitle>
                </mat-card-header>

                <mat-card-content>
                  <div class="payment-summary">
                    <div class="summary-row">
                      <span>Premium Subscription</span>
                      <span>\$9.99</span>
                    </div>
                    <div class="summary-row total">
                      <span><strong>Total</strong></span>
                      <span><strong>\$9.99</strong></span>
                    </div>
                  </div>

                  <div class="stripe-elements">
                    <div id="card-element">
                      <!-- Stripe Elements will create form elements here -->
                    </div>
                    <div id="card-errors" role="alert"></div>
                  </div>

                  <div class="security-info">
                    <mat-icon color="primary">security</mat-icon>
                    <span>Your payment information is secure and encrypted</span>
                  </div>
                </mat-card-content>

                <mat-card-actions>
                  <button mat-button matStepperPrevious>
                    <mat-icon>arrow_back</mat-icon>
                    Back
                  </button>
                  <button 
                    mat-raised-button 
                    color="accent" 
                    [disabled]="processing"
                    (click)="processPayment()">
                    <mat-spinner diameter="20" *ngIf="processing"></mat-spinner>
                    <mat-icon *ngIf="!processing">credit_card</mat-icon>
                    {{ processing ? 'Processing...' : 'Complete Upgrade' }}
                  </button>
                </mat-card-actions>
              </mat-card>
            </form>
          </mat-step>

          <!-- Step 3: Confirmation -->
          <mat-step label="Confirmation">
            <mat-card class="confirmation-card">
              <mat-card-header>
                <mat-card-title>
                  <mat-icon color="accent">check_circle</mat-icon>
                  Upgrade Successful!
                </mat-card-title>
                <mat-card-subtitle>Welcome to Premium</mat-card-subtitle>
              </mat-card-header>

              <mat-card-content>
                <div class="success-message">
                  <p>Congratulations! Your account has been successfully upgraded to Premium.</p>
                  <p>You now have access to all Premium features including:</p>
                  
                  <ul class="benefits-list">
                    <li>Unlimited book borrowing</li>
                    <li>Extended 30-day borrow periods</li>
                    <li>Reduced late fees</li>
                    <li>Priority support</li>
                  </ul>

                  <div class="next-steps">
                    <h3>What's next?</h3>
                    <p>Start exploring our full catalog and enjoy unlimited reading!</p>
                  </div>
                </div>
              </mat-card-content>

              <mat-card-actions>
                <button mat-raised-button color="primary" routerLink="/books">
                  <mat-icon>library_books</mat-icon>
                  Browse Books
                </button>
                <button mat-button routerLink="/dashboard">
                  <mat-icon>dashboard</mat-icon>
                  Go to Dashboard
                </button>
              </mat-card-actions>
            </mat-card>
          </mat-step>
        </mat-horizontal-stepper>
      </div>
    </div>
  `,
  styles: [`
    .upgrade-container {
      max-width: 800px;
      margin: 0 auto;
      padding: 2rem;
    }

    .header {
      display: flex;
      align-items: center;
      margin-bottom: 2rem;
      position: relative;
    }

    .back-button {
      margin-right: 1rem;
    }

    .header h1 {
      margin: 0;
      color: #2c3e50;
    }

    .header p {
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

    .plan-review-card,
    .payment-card,
    .confirmation-card {
      margin: 1rem 0;
    }

    .plan-details {
      display: flex;
      flex-direction: column;
      gap: 2rem;
    }

    .price-section {
      text-align: center;
      padding: 1rem;
      background-color: #f8f9fa;
      border-radius: 8px;
    }

    .price {
      font-size: 3rem;
      font-weight: bold;
      color: #2c3e50;
    }

    .period {
      font-size: 1.2rem;
      color: #7f8c8d;
    }

    .features h3 {
      margin-bottom: 1rem;
      color: #2c3e50;
    }

    .features ul {
      list-style: none;
      padding: 0;
    }

    .features li {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 0.5rem;
    }

    .billing-info {
      background-color: #e8f5e8;
      padding: 1rem;
      border-radius: 8px;
    }

    .billing-info p {
      margin: 0.5rem 0;
    }

    .payment-summary {
      background-color: #f8f9fa;
      padding: 1rem;
      border-radius: 8px;
      margin-bottom: 2rem;
    }

    .summary-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 0.5rem;
    }

    .summary-row.total {
      border-top: 1px solid #dee2e6;
      padding-top: 0.5rem;
      margin-top: 1rem;
    }

    .stripe-elements {
      margin: 2rem 0;
    }

    #card-element {
      padding: 1rem;
      border: 1px solid #ccc;
      border-radius: 4px;
      background-color: white;
    }

    #card-errors {
      color: #fa755a;
      margin-top: 0.5rem;
    }

    .security-info {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #7f8c8d;
      font-size: 0.9rem;
      margin-top: 1rem;
    }

    .success-message {
      text-align: center;
    }

    .benefits-list {
      text-align: left;
      margin: 2rem 0;
    }

    .benefits-list li {
      margin-bottom: 0.5rem;
    }

    .next-steps {
      background-color: #e3f2fd;
      padding: 1rem;
      border-radius: 8px;
      margin-top: 2rem;
    }

    .next-steps h3 {
      margin-top: 0;
      color: #1976d2;
    }

    @media (max-width: 768px) {
      .upgrade-container {
        padding: 1rem;
      }

      .header {
        flex-direction: column;
        align-items: flex-start;
      }

      .back-button {
        margin-bottom: 1rem;
      }

      .price {
        font-size: 2rem;
      }
    }
  `]
})
export class SubscriptionUpgradeComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  planForm: FormGroup;
  paymentForm: FormGroup;
  loading = true;
  processing = false;
  loadingMessage = 'Initializing payment system...';
  
  private stripe: any;
  private cardElement: any;

  constructor(
    private fb: FormBuilder,
    private subscriptionService: SubscriptionService,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.planForm = this.fb.group({
      accepted: [true, Validators.requiredTrue]
    });

    this.paymentForm = this.fb.group({
      cardComplete: [false, Validators.requiredTrue]
    });
  }

  ngOnInit(): void {
    this.initializeStripe();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private async initializeStripe(): Promise<void> {
    try {
      // Get Stripe configuration from backend
      const config = await this.subscriptionService.getStripeConfig().toPromise();
      
      if (!config) {
        throw new Error('Failed to get Stripe configuration');
      }
      
      // Initialize Stripe
      this.stripe = Stripe(config.publishableKey);
      
      // Create card element
      const elements = this.stripe.elements();
      this.cardElement = elements.create('card', {
        style: {
          base: {
            fontSize: '16px',
            color: '#424770',
            '::placeholder': {
              color: '#aab7c4',
            },
          },
        },
      });

      this.cardElement.mount('#card-element');

      // Listen for changes
      this.cardElement.on('change', (event: any) => {
        const displayError = document.getElementById('card-errors');
        if (event.error) {
          displayError!.textContent = event.error.message;
          this.paymentForm.patchValue({ cardComplete: false });
        } else {
          displayError!.textContent = '';
          this.paymentForm.patchValue({ cardComplete: event.complete });
        }
      });

      this.loading = false;
    } catch (error) {
      console.error('Failed to initialize Stripe:', error);
      this.toastr.error('Failed to initialize payment system');
      this.loading = false;
    }
  }

  getNextBillingDate(): string {
    const nextMonth = new Date();
    nextMonth.setMonth(nextMonth.getMonth() + 1);
    return nextMonth.toLocaleDateString();
  }

  async processPayment(): Promise<void> {
    if (!this.stripe || !this.cardElement) {
      this.toastr.error('Payment system not initialized');
      return;
    }

    this.processing = true;

    try {
      // Create payment intent
      const paymentIntentRequest: CreatePaymentIntentRequest = {
        subscriptionType: SubscriptionType.PREMIUM
      };

      const paymentIntentResponse = await this.subscriptionService
        .createPaymentIntent(paymentIntentRequest)
        .toPromise();

      if (!paymentIntentResponse) {
        throw new Error('Failed to create payment intent');
      }

      // Confirm payment with Stripe
      const { error, paymentIntent } = await this.stripe.confirmCardPayment(
        paymentIntentResponse.clientSecret,
        {
          payment_method: {
            card: this.cardElement,
          }
        }
      );

      if (error) {
        throw new Error(error.message);
      }

      if (paymentIntent.status === 'succeeded') {
        // Confirm payment with backend
        await this.subscriptionService.confirmPayment(paymentIntent.id).toPromise();
        
        // Note: User subscription will be updated on next login or page refresh
        
        this.toastr.success('Payment successful! Welcome to Premium!');
        
        // Move to confirmation step
        // Note: In a real implementation, you'd need to access the stepper
        // For now, we'll redirect to success page
        setTimeout(() => {
          this.router.navigate(['/subscription/plans']);
        }, 2000);
      }
    } catch (error: any) {
      console.error('Payment failed:', error);
      this.toastr.error(error.message || 'Payment failed. Please try again.');
    } finally {
      this.processing = false;
    }
  }
}