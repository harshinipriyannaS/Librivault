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
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Subject, takeUntil, interval } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { BorrowService } from '@core/services/borrow.service';
import { BookService } from '@core/services/book.service';
import { BorrowRequest, BorrowRecord, Fine, RequestStatus, BorrowStatus, FineStatus } from '@core/models/borrow.model';

@Component({
  selector: 'app-my-borrows',
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
    MatBadgeModule,
    MatProgressBarModule
  ],
  template: `
    <div class="my-borrows-container">
      <div class="page-header">
        <h1>My Borrowed Books</h1>
        <p>Manage your book borrowing history and current loans</p>
      </div>

      <!-- Quick Stats -->
      <div class="stats-grid">
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="warn">pending</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ pendingRequests.length }}</h3>
                <p>Pending Requests</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="primary">library_books</mat-icon>
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
                <mat-icon color="accent">schedule</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ overdueBorrows.length }}</h3>
                <p>Overdue Books</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="warn">payment</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ unpaidFines.length }}</h3>
                <p>Outstanding Fines</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Borrowing Tabs -->
      <mat-card class="borrowing-card">
        <mat-tab-group>
          <!-- Active Borrows -->
          <mat-tab>
            <ng-template mat-tab-label>
              <span matBadge="{{ activeBorrows.length }}" matBadgeColor="primary" [matBadgeHidden]="activeBorrows.length === 0">
                Active Borrows
              </span>
            </ng-template>
            <div class="tab-content">
              <div class="tab-header">
                <h2>Currently Borrowed Books</h2>
                <button mat-button (click)="refreshData()">
                  <mat-icon>refresh</mat-icon>
                  Refresh
                </button>
              </div>

              <div class="borrows-list" *ngIf="activeBorrows.length > 0; else noBorrows">
                <div class="borrow-item" 
                     *ngFor="let borrow of activeBorrows"
                     [class.overdue]="isOverdue(borrow)">
                  <div class="book-info">
                    <div class="book-cover">
                      <mat-icon>book</mat-icon>
                    </div>
                    <div class="book-details">
                      <h3>{{ borrow.bookTitle }}</h3>
                      <p>by {{ borrow.bookAuthor }}</p>
                      <span class="category-chip">{{ borrow.categoryName }}</span>
                    </div>
                  </div>

                  <div class="borrow-details">
                    <div class="date-info">
                      <div class="date-item">
                        <mat-icon>event</mat-icon>
                        <span>Borrowed: {{ borrow.borrowedAt | date:'short' }}</span>
                      </div>
                      <div class="date-item" [class.overdue-date]="isOverdue(borrow)">
                        <mat-icon>schedule</mat-icon>
                        <span>Due: {{ borrow.dueDate | date:'short' }}</span>
                        <mat-icon *ngIf="isOverdue(borrow)" class="warning-icon">warning</mat-icon>
                      </div>
                    </div>

                    <!-- Countdown Timer -->
                    <div class="countdown" *ngIf="!isOverdue(borrow)">
                      <mat-progress-bar 
                        mode="determinate" 
                        [value]="getTimeProgress(borrow)"
                        [color]="getProgressColor(borrow)">
                      </mat-progress-bar>
                      <small>{{ getTimeRemaining(borrow) }}</small>
                    </div>

                    <div class="overdue-notice" *ngIf="isOverdue(borrow)">
                      <mat-icon color="warn">error</mat-icon>
                      <span>{{ getDaysOverdue(borrow) }} days overdue</span>
                    </div>
                  </div>

                  <div class="borrow-actions">
                    <button mat-button 
                            [routerLink]="['/books', borrow.bookId]"
                            color="primary">
                      <mat-icon>visibility</mat-icon>
                      View Book
                    </button>
                    
                    <button mat-button 
                            (click)="readBook(borrow)"
                            color="primary"
                            [disabled]="isLoadingAccess">
                      <mat-icon>menu_book</mat-icon>
                      Read Now
                    </button>

                    <button mat-raised-button 
                            color="accent"
                            (click)="returnBook(borrow)"
                            [disabled]="isReturning">
                      <mat-spinner *ngIf="isReturning" diameter="20"></mat-spinner>
                      <mat-icon *ngIf="!isReturning">keyboard_return</mat-icon>
                      <span *ngIf="!isReturning">Return</span>
                    </button>
                  </div>
                </div>
              </div>

              <ng-template #noBorrows>
                <div class="empty-state">
                  <mat-icon>library_books</mat-icon>
                  <h3>No active borrows</h3>
                  <p>You don't have any books currently borrowed.</p>
                  <button mat-raised-button color="primary" routerLink="/books">
                    <mat-icon>search</mat-icon>
                    Browse Books
                  </button>
                </div>
              </ng-template>
            </div>
          </mat-tab>

          <!-- Pending Requests -->
          <mat-tab>
            <ng-template mat-tab-label>
              <span matBadge="{{ pendingRequests.length }}" matBadgeColor="warn" [matBadgeHidden]="pendingRequests.length === 0">
                Pending Requests
              </span>
            </ng-template>
            <div class="tab-content">
              <div class="tab-header">
                <h2>Borrow Requests Awaiting Approval</h2>
              </div>

              <div class="requests-list" *ngIf="pendingRequests.length > 0; else noRequests">
                <div class="request-item" *ngFor="let request of pendingRequests">
                  <div class="book-info">
                    <div class="book-cover">
                      <mat-icon>book</mat-icon>
                    </div>
                    <div class="book-details">
                      <h3>{{ request.bookTitle }}</h3>
                      <p>by {{ request.bookAuthor }}</p>
                      <span class="category-chip">{{ request.categoryName }}</span>
                    </div>
                  </div>

                  <div class="request-details">
                    <div class="request-info">
                      <div class="info-item">
                        <mat-icon>schedule</mat-icon>
                        <span>Requested: {{ request.requestedAt | date:'short' }}</span>
                      </div>
                      <div class="status-chip pending">
                        <mat-icon>hourglass_empty</mat-icon>
                        <span>{{ request.status }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="request-actions">
                    <button mat-button [routerLink]="['/books', request.bookId]">
                      <mat-icon>visibility</mat-icon>
                      View Book
                    </button>
                  </div>
                </div>
              </div>

              <ng-template #noRequests>
                <div class="empty-state">
                  <mat-icon>inbox</mat-icon>
                  <h3>No pending requests</h3>
                  <p>You don't have any pending borrow requests.</p>
                </div>
              </ng-template>
            </div>
          </mat-tab>

          <!-- Borrow History -->
          <mat-tab label="History">
            <div class="tab-content">
              <div class="tab-header">
                <h2>Borrowing History</h2>
              </div>

              <div class="history-list" *ngIf="borrowHistory.length > 0; else noHistory">
                <div class="history-item" *ngFor="let record of borrowHistory">
                  <div class="book-info">
                    <div class="book-cover">
                      <mat-icon>book</mat-icon>
                    </div>
                    <div class="book-details">
                      <h3>{{ record.bookTitle }}</h3>
                      <p>by {{ record.bookAuthor }}</p>
                      <span class="category-chip">{{ record.categoryName }}</span>
                    </div>
                  </div>

                  <div class="history-details">
                    <div class="date-range">
                      <span>{{ record.borrowedAt | date:'short' }} - {{ record.returnedAt | date:'short' }}</span>
                    </div>
                    <div class="status-info">
                      <span [class]="getStatusClass(record.status)">{{ record.status }}</span>
                      <span *ngIf="record.creditsEarned > 0" class="credits-earned">
                        +{{ record.creditsEarned }} credits
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <ng-template #noHistory>
                <div class="empty-state">
                  <mat-icon>history</mat-icon>
                  <h3>No borrowing history</h3>
                  <p>Your borrowing history will appear here.</p>
                </div>
              </ng-template>
            </div>
          </mat-tab>

          <!-- Fines -->
          <mat-tab>
            <ng-template mat-tab-label>
              <span matBadge="{{ unpaidFines.length }}" matBadgeColor="warn" [matBadgeHidden]="unpaidFines.length === 0">
                Fines
              </span>
            </ng-template>
            <div class="tab-content">
              <div class="tab-header">
                <h2>Outstanding Fines</h2>
              </div>

              <div class="fines-list" *ngIf="allFines.length > 0; else noFines">
                <div class="fine-item" *ngFor="let fine of allFines">
                  <div class="fine-info">
                    <div class="book-details">
                      <h4>{{ fine.bookTitle }}</h4>
                      <p>by {{ fine.bookAuthor }}</p>
                    </div>
                    <div class="fine-details">
                      <div class="fine-amount">
                        <span class="amount">\${{ fine.amount.toFixed(2) }}</span>
                        <small>{{ fine.overdueDays }} days overdue</small>
                      </div>
                      <div class="fine-status">
                        <span [class]="getFineStatusClass(fine.status)">{{ fine.status }}</span>
                        <small>{{ fine.createdAt | date:'short' }}</small>
                      </div>
                    </div>
                  </div>

                  <div class="fine-actions" *ngIf="fine.status === 'PENDING'">
                    <button mat-raised-button 
                            color="primary"
                            (click)="payFine(fine)"
                            [disabled]="isPayingFine">
                      <mat-icon>payment</mat-icon>
                      Pay Fine
                    </button>
                  </div>
                </div>
              </div>

              <ng-template #noFines>
                <div class="empty-state">
                  <mat-icon>check_circle</mat-icon>
                  <h3>No outstanding fines</h3>
                  <p>You don't have any fines to pay.</p>
                </div>
              </ng-template>
            </div>
          </mat-tab>
        </mat-tab-group>
      </mat-card>
    </div>
  `,
  styles: [`
    .my-borrows-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .page-header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .page-header h1 {
      color: #2c3e50;
      font-size: 2.5rem;
      margin-bottom: 0.5rem;
    }

    .page-header p {
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
      transition: transform 0.2s ease;
    }

    .stat-card:hover {
      transform: translateY(-2px);
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
      font-size: 1.5rem;
      font-weight: 600;
      color: #2c3e50;
    }

    .stat-info p {
      margin: 0;
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .borrowing-card {
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    .tab-content {
      padding: 2rem;
    }

    .tab-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .tab-header h2 {
      color: #2c3e50;
      margin: 0;
    }

    .borrows-list,
    .requests-list,
    .history-list,
    .fines-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .borrow-item,
    .request-item,
    .history-item,
    .fine-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 1.5rem;
      border: 1px solid #ecf0f1;
      border-radius: 8px;
      background: #fafafa;
      transition: all 0.2s ease;
    }

    .borrow-item.overdue {
      border-color: #e74c3c;
      background: #fdf2f2;
    }

    .book-info {
      display: flex;
      align-items: center;
      gap: 1rem;
      flex: 1;
    }

    .book-cover {
      width: 60px;
      height: 80px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(45deg, #f0f0f0, #e0e0e0);
      border-radius: 4px;
    }

    .book-cover mat-icon {
      font-size: 2rem;
      width: 2rem;
      height: 2rem;
      color: #666;
    }

    .book-details h3,
    .book-details h4 {
      margin: 0 0 0.25rem 0;
      color: #2c3e50;
      font-size: 1.1rem;
    }

    .book-details p {
      margin: 0 0 0.5rem 0;
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .category-chip {
      background-color: #3498db;
      color: white;
      padding: 0.25rem 0.5rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 500;
    }

    .borrow-details,
    .request-details,
    .history-details {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      flex: 1;
      margin: 0 1rem;
    }

    .date-info {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .date-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.9rem;
      color: #7f8c8d;
    }

    .date-item.overdue-date {
      color: #e74c3c;
      font-weight: 500;
    }

    .date-item mat-icon {
      font-size: 1rem;
      width: 1rem;
      height: 1rem;
    }

    .warning-icon {
      color: #e74c3c;
      font-size: 1rem;
      width: 1rem;
      height: 1rem;
    }

    .countdown {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .countdown small {
      color: #7f8c8d;
      font-size: 0.8rem;
    }

    .overdue-notice {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #e74c3c;
      font-weight: 500;
      font-size: 0.9rem;
    }

    .status-chip {
      display: flex;
      align-items: center;
      gap: 0.25rem;
      padding: 0.25rem 0.5rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 500;
    }

    .status-chip.pending {
      background-color: #f39c12;
      color: white;
    }

    .borrow-actions,
    .request-actions,
    .fine-actions {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .credits-earned {
      color: #27ae60;
      font-weight: 500;
      font-size: 0.8rem;
    }

    .fine-info {
      display: flex;
      align-items: center;
      gap: 2rem;
      flex: 1;
    }

    .fine-details {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .fine-amount .amount {
      font-size: 1.2rem;
      font-weight: 600;
      color: #e74c3c;
    }

    .fine-amount small,
    .fine-status small {
      color: #95a5a6;
      font-size: 0.8rem;
    }

    .status-active {
      color: #27ae60;
    }

    .status-returned {
      color: #3498db;
    }

    .status-overdue {
      color: #e74c3c;
    }

    .fine-status-pending {
      color: #e74c3c;
      font-weight: 500;
    }

    .fine-status-paid {
      color: #27ae60;
      font-weight: 500;
    }

    .fine-status-waived {
      color: #3498db;
      font-weight: 500;
    }

    .empty-state {
      text-align: center;
      padding: 4rem 2rem;
      color: #7f8c8d;
    }

    .empty-state mat-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      margin-bottom: 1rem;
      color: #bdc3c7;
    }

    .empty-state h3 {
      color: #2c3e50;
      margin-bottom: 0.5rem;
    }

    @media (max-width: 768px) {
      .my-borrows-container {
        padding: 1rem;
      }

      .page-header h1 {
        font-size: 2rem;
      }

      .stats-grid {
        grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      }

      .tab-content {
        padding: 1rem;
      }

      .tab-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
      }

      .borrow-item,
      .request-item,
      .history-item,
      .fine-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
      }

      .book-info {
        width: 100%;
      }

      .borrow-details,
      .request-details,
      .history-details {
        width: 100%;
        margin: 0;
      }

      .borrow-actions,
      .request-actions,
      .fine-actions {
        flex-direction: row;
        width: 100%;
        justify-content: flex-start;
      }
    }
  `]
})
export class MyBorrowsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  pendingRequests: BorrowRequest[] = [];
  activeBorrows: BorrowRecord[] = [];
  borrowHistory: BorrowRecord[] = [];
  allFines: Fine[] = [];
  
  isReturning = false;
  isPayingFine = false;
  isLoadingAccess = false;

  constructor(
    private borrowService: BorrowService,
    private bookService: BookService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadData();
    
    // Auto-refresh every 30 seconds
    interval(30000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.refreshData());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadData(): void {
    this.loadBorrowRequests();
    this.loadBorrowRecords();
    this.loadFines();
  }

  private loadBorrowRequests(): void {
    this.borrowService.getMyBorrowRequests(0, 50).subscribe({
      next: (response) => {
        this.pendingRequests = response.content.filter(req => req.status === RequestStatus.PENDING);
      },
      error: (error) => console.error('Error loading borrow requests:', error)
    });
  }

  private loadBorrowRecords(): void {
    this.borrowService.getMyBorrowRecords(0, 100).subscribe({
      next: (response) => {
        const records = response.content;
        this.activeBorrows = records.filter(record => 
          record.status === BorrowStatus.ACTIVE || record.status === BorrowStatus.OVERDUE
        );
        this.borrowHistory = records.filter(record => record.status === BorrowStatus.RETURNED);
      },
      error: (error) => console.error('Error loading borrow records:', error)
    });
  }

  private loadFines(): void {
    this.borrowService.getMyFines(0, 50).subscribe({
      next: (response) => {
        this.allFines = response.content;
      },
      error: (error) => console.error('Error loading fines:', error)
    });
  }

  refreshData(): void {
    this.loadData();
  }

  returnBook(borrow: BorrowRecord): void {
    this.isReturning = true;
    this.borrowService.returnBook(borrow.id).subscribe({
      next: () => {
        this.toastr.success(`Successfully returned "${borrow.bookTitle}"`, 'Book Returned');
        this.loadBorrowRecords();
        this.isReturning = false;
      },
      error: (error) => {
        this.isReturning = false;
      }
    });
  }

  readBook(borrow: BorrowRecord): void {
    this.isLoadingAccess = true;
    this.bookService.getBookAccess(borrow.bookId).subscribe({
      next: (access) => {
        window.open(access.secureUrl, '_blank');
        this.isLoadingAccess = false;
      },
      error: (error) => {
        this.toastr.error('Unable to access book content', 'Access Error');
        this.isLoadingAccess = false;
      }
    });
  }

  payFine(fine: Fine): void {
    this.isPayingFine = true;
    this.borrowService.payFine(fine.id).subscribe({
      next: () => {
        this.toastr.success(`Fine of $${fine.amount.toFixed(2)} has been paid`, 'Fine Paid');
        this.loadFines();
        this.isPayingFine = false;
      },
      error: (error) => {
        this.isPayingFine = false;
      }
    });
  }

  isOverdue(borrow: BorrowRecord): boolean {
    const dueDate = new Date(borrow.dueDate);
    const now = new Date();
    return now > dueDate && borrow.status === BorrowStatus.ACTIVE;
  }

  getDaysOverdue(borrow: BorrowRecord): number {
    const dueDate = new Date(borrow.dueDate);
    const now = new Date();
    const diffTime = now.getTime() - dueDate.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  getTimeRemaining(borrow: BorrowRecord): string {
    const dueDate = new Date(borrow.dueDate);
    const now = new Date();
    const diffTime = dueDate.getTime() - now.getTime();
    
    if (diffTime <= 0) return 'Overdue';
    
    const days = Math.floor(diffTime / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diffTime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    
    if (days > 0) {
      return `${days} day${days > 1 ? 's' : ''} remaining`;
    } else {
      return `${hours} hour${hours > 1 ? 's' : ''} remaining`;
    }
  }

  getTimeProgress(borrow: BorrowRecord): number {
    const borrowDate = new Date(borrow.borrowedAt);
    const dueDate = new Date(borrow.dueDate);
    const now = new Date();
    
    const totalTime = dueDate.getTime() - borrowDate.getTime();
    const elapsedTime = now.getTime() - borrowDate.getTime();
    
    return Math.min(100, Math.max(0, (elapsedTime / totalTime) * 100));
  }

  getProgressColor(borrow: BorrowRecord): string {
    const progress = this.getTimeProgress(borrow);
    if (progress > 80) return 'warn';
    if (progress > 60) return 'accent';
    return 'primary';
  }

  getStatusClass(status: BorrowStatus): string {
    switch (status) {
      case BorrowStatus.ACTIVE:
        return 'status-active';
      case BorrowStatus.RETURNED:
        return 'status-returned';
      case BorrowStatus.OVERDUE:
        return 'status-overdue';
      default:
        return '';
    }
  }

  getFineStatusClass(status: FineStatus): string {
    switch (status) {
      case FineStatus.PENDING:
        return 'fine-status-pending';
      case FineStatus.PAID:
        return 'fine-status-paid';
      case FineStatus.WAIVED:
        return 'fine-status-waived';
      default:
        return '';
    }
  }

  get overdueBorrows(): BorrowRecord[] {
    return this.activeBorrows.filter(borrow => this.isOverdue(borrow));
  }

  get unpaidFines(): Fine[] {
    return this.allFines.filter(fine => fine.status === FineStatus.PENDING);
  }
}