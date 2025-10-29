import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { Subject, takeUntil } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { BookService } from '@core/services/book.service';
import { BorrowService } from '@core/services/borrow.service';
import { AuthService } from '@core/services/auth.service';
import { Book, Category } from '@core/models/book.model';
import { BorrowRequest, BorrowRecord, RequestStatus, BorrowStatus } from '@core/models/borrow.model';
import { User } from '@core/models/user.model';

@Component({
  selector: 'app-librarian-dashboard',
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
    MatBadgeModule
  ],
  template: `
    <div class="librarian-dashboard-container">
      <div class="dashboard-header">
        <h1>Librarian Dashboard</h1>
        <p>Manage book borrowing and your assigned categories</p>
      </div>

      <!-- Quick Stats -->
      <div class="stats-grid">
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="warn">pending_actions</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ pendingRequests.length }}</h3>
                <p>Pending Requests</p>
                <small>Awaiting approval</small>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="primary">assignment</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ activeBorrows.length }}</h3>
                <p>Active Borrows</p>
                <small>Currently borrowed</small>
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
                <small>Need attention</small>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="primary">category</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ myCategories.length }}</h3>
                <p>My Categories</p>
                <small>Assigned to me</small>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Management Tabs -->
      <mat-card class="management-card">
        <mat-tab-group>
          <!-- Borrow Requests -->
          <mat-tab>
            <ng-template mat-tab-label>
              <span matBadge="{{ pendingRequests.length }}" matBadgeColor="warn" [matBadgeHidden]="pendingRequests.length === 0">
                Borrow Requests
              </span>
            </ng-template>
            <div class="tab-content">
              <div class="tab-header">
                <h2>Pending Borrow Requests</h2>
                <button mat-button (click)="refreshRequests()">
                  <mat-icon>refresh</mat-icon>
                  Refresh
                </button>
              </div>

              <div class="requests-list" *ngIf="pendingRequests.length > 0; else noRequests">
                <div class="request-item" *ngFor="let request of pendingRequests">
                  <div class="request-info">
                    <div class="book-details">
                      <h4>{{ request.bookTitle }}</h4>
                      <p>by {{ request.bookAuthor }}</p>
                      <span class="category-chip">{{ request.categoryName }}</span>
                    </div>
                    <div class="reader-details">
                      <h5>{{ request.readerName }}</h5>
                      <p>{{ request.readerEmail }}</p>
                      <small>Requested: {{ request.requestedAt | date:'short' }}</small>
                    </div>
                  </div>
                  <div class="request-actions">
                    <button mat-raised-button 
                            color="primary"
                            (click)="approveRequest(request)"
                            [disabled]="isProcessingRequest">
                      <mat-icon>check</mat-icon>
                      Approve
                    </button>
                    <button mat-button 
                            color="warn"
                            (click)="declineRequest(request)"
                            [disabled]="isProcessingRequest">
                      <mat-icon>close</mat-icon>
                      Decline
                    </button>
                  </div>
                </div>
              </div>

              <ng-template #noRequests>
                <div class="empty-state">
                  <mat-icon>inbox</mat-icon>
                  <h3>No pending requests</h3>
                  <p>All borrow requests have been processed.</p>
                </div>
              </ng-template>
            </div>
          </mat-tab>

          <!-- Active Borrows -->
          <mat-tab>
            <ng-template mat-tab-label>
              <span matBadge="{{ overdueBorrows.length }}" matBadgeColor="warn" [matBadgeHidden]="overdueBorrows.length === 0">
                Active Borrows
              </span>
            </ng-template>
            <div class="tab-content">
              <div class="tab-header">
                <h2>Active Borrows</h2>
                <button mat-button (click)="refreshBorrows()">
                  <mat-icon>refresh</mat-icon>
                  Refresh
                </button>
              </div>

              <div class="borrows-list" *ngIf="activeBorrows.length > 0; else noBorrows">
                <div class="borrow-item" 
                     *ngFor="let borrow of activeBorrows"
                     [class.overdue]="isOverdue(borrow)">
                  <div class="borrow-info">
                    <div class="book-details">
                      <h4>{{ borrow.bookTitle }}</h4>
                      <p>by {{ borrow.bookAuthor }}</p>
                      <span class="category-chip">{{ borrow.categoryName }}</span>
                    </div>
                    <div class="reader-details">
                      <h5>{{ borrow.readerName }}</h5>
                      <p>{{ borrow.readerEmail }}</p>
                    </div>
                    <div class="borrow-dates">
                      <div class="date-info">
                        <small>Borrowed: {{ borrow.borrowedAt | date:'short' }}</small>
                      </div>
                      <div class="date-info" [class.overdue-date]="isOverdue(borrow)">
                        <small>Due: {{ borrow.dueDate | date:'short' }}</small>
                        <mat-icon *ngIf="isOverdue(borrow)" class="overdue-icon">warning</mat-icon>
                      </div>
                    </div>
                  </div>
                  <div class="borrow-status">
                    <span [class]="getBorrowStatusClass(borrow.status)">
                      {{ borrow.status }}
                    </span>
                    <button mat-button 
                            color="primary"
                            (click)="processReturn(borrow)"
                            [disabled]="isProcessingReturn">
                      <mat-icon>keyboard_return</mat-icon>
                      Process Return
                    </button>
                  </div>
                </div>
              </div>

              <ng-template #noBorrows>
                <div class="empty-state">
                  <mat-icon>library_books</mat-icon>
                  <h3>No active borrows</h3>
                  <p>No books are currently borrowed.</p>
                </div>
              </ng-template>
            </div>
          </mat-tab>

          <!-- My Categories -->
          <mat-tab label="My Categories">
            <div class="tab-content">
              <div class="tab-header">
                <h2>My Assigned Categories</h2>
                <div class="tab-actions">
                  <button mat-raised-button color="primary" routerLink="/librarian/books/add">
                    <mat-icon>add</mat-icon>
                    Add Book
                  </button>
                </div>
              </div>

              <div class="categories-grid" *ngIf="myCategories.length > 0; else noCategories">
                <mat-card class="category-card" *ngFor="let category of myCategories">
                  <mat-card-header>
                    <mat-card-title>{{ category.name }}</mat-card-title>
                    <mat-card-subtitle>{{ category.description }}</mat-card-subtitle>
                  </mat-card-header>
                  <mat-card-content>
                    <div class="category-stats">
                      <div class="stat-item">
                        <mat-icon>library_books</mat-icon>
                        <span>{{ category.totalBooks }} total books</span>
                      </div>
                      <div class="stat-item">
                        <mat-icon>check_circle</mat-icon>
                        <span>{{ category.availableBooks }} available</span>
                      </div>
                    </div>
                  </mat-card-content>
                  <mat-card-actions>
                    <button mat-button [routerLink]="['/librarian/categories', category.id, 'books']">
                      <mat-icon>list</mat-icon>
                      Manage Books
                    </button>
                    <button mat-button [routerLink]="['/books']" [queryParams]="{categoryId: category.id}">
                      <mat-icon>visibility</mat-icon>
                      View Books
                    </button>
                  </mat-card-actions>
                </mat-card>
              </div>

              <ng-template #noCategories>
                <div class="empty-state">
                  <mat-icon>category</mat-icon>
                  <h3>No categories assigned</h3>
                  <p>You haven't been assigned to manage any categories yet.</p>
                </div>
              </ng-template>
            </div>
          </mat-tab>
        </mat-tab-group>
      </mat-card>
    </div>
  `,
  styles: [`
    .librarian-dashboard-container {
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

    .dashboard-header p {
      color: #7f8c8d;
      font-size: 1.1rem;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1.5rem;
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
      font-size: 2.5rem;
      width: 2.5rem;
      height: 2.5rem;
    }

    .stat-info h3 {
      margin: 0;
      font-size: 2rem;
      font-weight: 600;
      color: #2c3e50;
    }

    .stat-info p {
      margin: 0;
      color: #7f8c8d;
      font-weight: 500;
    }

    .stat-info small {
      color: #95a5a6;
      font-size: 0.8rem;
    }

    .management-card {
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
      flex-wrap: wrap;
      gap: 1rem;
    }

    .tab-header h2 {
      color: #2c3e50;
      margin: 0;
    }

    .tab-actions {
      display: flex;
      gap: 1rem;
    }

    .requests-list,
    .borrows-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .request-item,
    .borrow-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      border: 1px solid #ecf0f1;
      border-radius: 8px;
      background: #fafafa;
      transition: border-color 0.2s ease;
    }

    .borrow-item.overdue {
      border-color: #e74c3c;
      background: #fdf2f2;
    }

    .request-info,
    .borrow-info {
      display: flex;
      gap: 2rem;
      flex: 1;
    }

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

    .reader-details h5 {
      margin: 0 0 0.25rem 0;
      color: #2c3e50;
      font-size: 1rem;
    }

    .reader-details p {
      margin: 0 0 0.25rem 0;
      color: #7f8c8d;
      font-size: 0.85rem;
    }

    .reader-details small {
      color: #95a5a6;
      font-size: 0.8rem;
    }

    .category-chip {
      background-color: #3498db;
      color: white;
      padding: 0.25rem 0.5rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 500;
    }

    .request-actions,
    .borrow-status {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      align-items: flex-end;
    }

    .borrow-dates {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .date-info {
      display: flex;
      align-items: center;
      gap: 0.25rem;
    }

    .date-info.overdue-date {
      color: #e74c3c;
    }

    .overdue-icon {
      font-size: 1rem;
      width: 1rem;
      height: 1rem;
      color: #e74c3c;
    }

    .status-active {
      color: #27ae60;
      font-weight: 500;
    }

    .status-overdue {
      color: #e74c3c;
      font-weight: 500;
    }

    .categories-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 1.5rem;
    }

    .category-card {
      height: 100%;
    }

    .category-stats {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .stat-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .stat-item mat-icon {
      font-size: 1.2rem;
      width: 1.2rem;
      height: 1.2rem;
      color: #3498db;
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
      .librarian-dashboard-container {
        padding: 1rem;
      }

      .dashboard-header h1 {
        font-size: 2rem;
      }

      .stats-grid {
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      }

      .tab-content {
        padding: 1rem;
      }

      .tab-header {
        flex-direction: column;
        align-items: flex-start;
      }

      .request-item,
      .borrow-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
      }

      .request-info,
      .borrow-info {
        flex-direction: column;
        gap: 1rem;
        width: 100%;
      }

      .request-actions,
      .borrow-status {
        flex-direction: row;
        width: 100%;
        justify-content: flex-start;
      }

      .categories-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class LibrarianDashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  currentUser: User | null = null;
  pendingRequests: BorrowRequest[] = [];
  activeBorrows: BorrowRecord[] = [];
  overdueBorrows: BorrowRecord[] = [];
  myCategories: Category[] = [];
  
  isProcessingRequest = false;
  isProcessingReturn = false;

  constructor(
    private bookService: BookService,
    private borrowService: BorrowService,
    private authService: AuthService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        this.currentUser = user;
        if (user) {
          this.loadDashboardData();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadDashboardData(): void {
    this.loadPendingRequests();
    this.loadActiveBorrows();
    this.loadMyCategories();
  }

  private loadPendingRequests(): void {
    this.borrowService.getPendingBorrowRequests(0, 50).subscribe({
      next: (response) => {
        this.pendingRequests = response.content;
      },
      error: (error) => {
        console.error('Error loading pending requests:', error);
      }
    });
  }

  private loadActiveBorrows(): void {
    this.borrowService.getAllBorrowRecords(0, 100).subscribe({
      next: (response) => {
        this.activeBorrows = response.content.filter(borrow => 
          borrow.status === BorrowStatus.ACTIVE || borrow.status === BorrowStatus.OVERDUE
        );
        this.overdueBorrows = this.activeBorrows.filter(borrow => this.isOverdue(borrow));
      },
      error: (error) => {
        console.error('Error loading active borrows:', error);
      }
    });
  }

  private loadMyCategories(): void {
    this.bookService.getLibrarianCategories().subscribe({
      next: (categories) => {
        this.myCategories = categories;
      },
      error: (error) => {
        console.error('Error loading my categories:', error);
      }
    });
  }

  approveRequest(request: BorrowRequest): void {
    this.isProcessingRequest = true;
    this.borrowService.approveBorrowRequest(request.id, {}).subscribe({
      next: () => {
        this.toastr.success(`Approved borrow request for "${request.bookTitle}"`, 'Request Approved');
        this.loadPendingRequests();
        this.loadActiveBorrows();
        this.isProcessingRequest = false;
      },
      error: (error) => {
        this.isProcessingRequest = false;
      }
    });
  }

  declineRequest(request: BorrowRequest): void {
    this.isProcessingRequest = true;
    this.borrowService.declineBorrowRequest(request.id, { notes: 'Declined by librarian' }).subscribe({
      next: () => {
        this.toastr.success(`Declined borrow request for "${request.bookTitle}"`, 'Request Declined');
        this.loadPendingRequests();
        this.isProcessingRequest = false;
      },
      error: (error) => {
        this.isProcessingRequest = false;
      }
    });
  }

  processReturn(borrow: BorrowRecord): void {
    this.isProcessingReturn = true;
    this.borrowService.returnBook(borrow.id).subscribe({
      next: () => {
        this.toastr.success(`Processed return for "${borrow.bookTitle}"`, 'Book Returned');
        this.loadActiveBorrows();
        this.isProcessingReturn = false;
      },
      error: (error) => {
        this.isProcessingReturn = false;
      }
    });
  }

  refreshRequests(): void {
    this.loadPendingRequests();
  }

  refreshBorrows(): void {
    this.loadActiveBorrows();
  }

  isOverdue(borrow: BorrowRecord): boolean {
    const dueDate = new Date(borrow.dueDate);
    const now = new Date();
    return now > dueDate && borrow.status === BorrowStatus.ACTIVE;
  }

  getBorrowStatusClass(status: BorrowStatus): string {
    switch (status) {
      case BorrowStatus.ACTIVE:
        return 'status-active';
      case BorrowStatus.OVERDUE:
        return 'status-overdue';
      default:
        return '';
    }
  }
}