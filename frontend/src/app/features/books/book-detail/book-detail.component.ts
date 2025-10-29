import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { Subject, takeUntil } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { BookService } from '@core/services/book.service';
import { BorrowService } from '@core/services/borrow.service';
import { AuthService } from '@core/services/auth.service';
import { Book, BookMetadata } from '@core/models/book.model';
import { CreateBorrowRequestRequest } from '@core/models/borrow.model';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ],
  template: `
    <div class="book-detail-container">
      <!-- Loading State -->
      <div class="loading-container" *ngIf="isLoading">
        <mat-spinner></mat-spinner>
        <p>Loading book details...</p>
      </div>

      <!-- Book Details -->
      <div class="book-detail-content" *ngIf="!isLoading && book">
        <!-- Back Button -->
        <div class="navigation">
          <button mat-button (click)="goBack()" class="back-btn">
            <mat-icon>arrow_back</mat-icon>
            Back to Catalog
          </button>
        </div>

        <div class="book-detail-grid">
          <!-- Book Cover and Actions -->
          <div class="book-cover-section">
            <mat-card class="cover-card">
              <div class="book-cover">
                <img *ngIf="book.coverImageUrl; else defaultCover" 
                     [src]="book.coverImageUrl" 
                     [alt]="book.title"
                     class="cover-image">
                <ng-template #defaultCover>
                  <mat-icon class="default-cover-icon">book</mat-icon>
                </ng-template>
              </div>
              
              <!-- Availability Status -->
              <div class="availability-status" 
                   [class.available]="book.availableCopies > 0"
                   [class.unavailable]="book.availableCopies === 0">
                <mat-icon>{{ book.availableCopies > 0 ? 'check_circle' : 'cancel' }}</mat-icon>
                <span>{{ book.availableCopies > 0 ? 'Available' : 'Currently Unavailable' }}</span>
              </div>

              <!-- Action Buttons -->
              <div class="action-buttons">
                <button mat-raised-button 
                        color="primary"
                        *ngIf="isAuthenticated && book.availableCopies > 0"
                        (click)="borrowBook()"
                        [disabled]="isBorrowing"
                        class="primary-action">
                  <mat-spinner *ngIf="isBorrowing" diameter="20"></mat-spinner>
                  <mat-icon *ngIf="!isBorrowing">add_shopping_cart</mat-icon>
                  <span *ngIf="!isBorrowing">Borrow Book</span>
                </button>

                <button mat-button 
                        color="primary"
                        (click)="previewBook()"
                        [disabled]="isLoadingPreview"
                        class="secondary-action">
                  <mat-spinner *ngIf="isLoadingPreview" diameter="20"></mat-spinner>
                  <mat-icon *ngIf="!isLoadingPreview">preview</mat-icon>
                  <span *ngIf="!isLoadingPreview">Preview</span>
                </button>

                <button mat-button 
                        *ngIf="isAuthenticated && hasFullAccess"
                        (click)="readBook()"
                        [disabled]="isLoadingAccess"
                        class="secondary-action">
                  <mat-spinner *ngIf="isLoadingAccess" diameter="20"></mat-spinner>
                  <mat-icon *ngIf="!isLoadingAccess">menu_book</mat-icon>
                  <span *ngIf="!isLoadingAccess">Read Now</span>
                </button>
              </div>
            </mat-card>
          </div>

          <!-- Book Information -->
          <div class="book-info-section">
            <mat-card class="info-card">
              <mat-card-header>
                <mat-card-title class="book-title">{{ book.title }}</mat-card-title>
                <mat-card-subtitle class="book-author">by {{ book.author }}</mat-card-subtitle>
              </mat-card-header>

              <mat-card-content>
                <!-- Book Metadata -->
                <div class="book-metadata">
                  <div class="metadata-item">
                    <mat-icon>category</mat-icon>
                    <span>{{ book.categoryName }}</span>
                  </div>
                  
                  <div class="metadata-item">
                    <mat-icon>calendar_today</mat-icon>
                    <span>Published {{ book.publishedDate | date:'mediumDate' }}</span>
                  </div>
                  
                  <div class="metadata-item">
                    <mat-icon>library_books</mat-icon>
                    <span>{{ book.availableCopies }} of {{ book.totalCopies }} copies available</span>
                  </div>
                  
                  <div class="metadata-item" *ngIf="book.isbn">
                    <mat-icon>qr_code</mat-icon>
                    <span>ISBN: {{ book.isbn }}</span>
                  </div>
                </div>

                <mat-divider class="section-divider"></mat-divider>

                <!-- Description -->
                <div class="book-description" *ngIf="book.description">
                  <h3>Description</h3>
                  <p>{{ book.description }}</p>
                </div>

                <!-- File Information -->
                <div class="file-info" *ngIf="bookMetadata">
                  <h3>File Information</h3>
                  <div class="file-details">
                    <div class="file-item">
                      <mat-icon>insert_drive_file</mat-icon>
                      <span>File Size: {{ bookMetadata.formattedFileSize }}</span>
                    </div>
                    <div class="file-item" *ngIf="bookMetadata.hasPreview">
                      <mat-icon>visibility</mat-icon>
                      <span>Preview Available</span>
                    </div>
                    <div class="file-item" *ngIf="bookMetadata.hasCover">
                      <mat-icon>image</mat-icon>
                      <span>Cover Image Available</span>
                    </div>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </div>

        <!-- Additional Information -->
        <mat-card class="additional-info">
          <mat-card-header>
            <mat-card-title>Additional Information</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-grid">
              <div class="info-item">
                <label>Book ID</label>
                <span>{{ book.id }}</span>
              </div>
              <div class="info-item">
                <label>Added to Library</label>
                <span>{{ book.createdAt | date:'mediumDate' }}</span>
              </div>
              <div class="info-item">
                <label>Last Updated</label>
                <span>{{ book.updatedAt | date:'mediumDate' }}</span>
              </div>
              <div class="info-item">
                <label>Status</label>
                <span [class]="book.active ? 'status-active' : 'status-inactive'">
                  {{ book.active ? 'Active' : 'Inactive' }}
                </span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Error State -->
      <div class="error-state" *ngIf="!isLoading && !book">
        <mat-icon class="error-icon">error_outline</mat-icon>
        <h2>Book Not Found</h2>
        <p>The book you're looking for doesn't exist or has been removed.</p>
        <button mat-raised-button color="primary" routerLink="/books">
          <mat-icon>arrow_back</mat-icon>
          Back to Catalog
        </button>
      </div>
    </div>
  `,
  styles: [`
    .book-detail-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem;
      color: #7f8c8d;
    }

    .loading-container mat-spinner {
      margin-bottom: 1rem;
    }

    .navigation {
      margin-bottom: 2rem;
    }

    .back-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .book-detail-grid {
      display: grid;
      grid-template-columns: 350px 1fr;
      gap: 2rem;
      margin-bottom: 2rem;
    }

    .cover-card {
      position: sticky;
      top: 2rem;
    }

    .book-cover {
      height: 400px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(45deg, #f8f9fa, #e9ecef);
      margin-bottom: 1rem;
    }

    .cover-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .default-cover-icon {
      font-size: 6rem;
      width: 6rem;
      height: 6rem;
      color: #6c757d;
    }

    .availability-status {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem;
      border-radius: 4px;
      margin-bottom: 1rem;
      font-weight: 500;
    }

    .availability-status.available {
      background-color: #d4edda;
      color: #155724;
    }

    .availability-status.unavailable {
      background-color: #f8d7da;
      color: #721c24;
    }

    .action-buttons {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .primary-action,
    .secondary-action {
      width: 100%;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
    }

    .info-card {
      height: fit-content;
    }

    .book-title {
      font-size: 2rem;
      line-height: 1.3;
      color: #2c3e50;
      margin-bottom: 0.5rem;
    }

    .book-author {
      font-size: 1.2rem;
      color: #7f8c8d;
      font-style: italic;
    }

    .book-metadata {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin: 1.5rem 0;
    }

    .metadata-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      color: #2c3e50;
    }

    .metadata-item mat-icon {
      color: #3498db;
      font-size: 1.2rem;
      width: 1.2rem;
      height: 1.2rem;
    }

    .section-divider {
      margin: 1.5rem 0;
    }

    .book-description h3,
    .file-info h3 {
      color: #2c3e50;
      margin-bottom: 1rem;
      font-size: 1.3rem;
    }

    .book-description p {
      color: #34495e;
      line-height: 1.6;
      font-size: 1rem;
    }

    .file-details {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .file-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .file-item mat-icon {
      font-size: 1rem;
      width: 1rem;
      height: 1rem;
    }

    .additional-info {
      margin-top: 2rem;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .info-item label {
      font-weight: 500;
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .info-item span {
      color: #2c3e50;
    }

    .status-active {
      color: #27ae60;
      font-weight: 500;
    }

    .status-inactive {
      color: #e74c3c;
      font-weight: 500;
    }

    .error-state {
      text-align: center;
      padding: 4rem 2rem;
      color: #7f8c8d;
    }

    .error-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      margin-bottom: 1rem;
      color: #e74c3c;
    }

    .error-state h2 {
      color: #2c3e50;
      margin-bottom: 1rem;
    }

    .error-state p {
      margin-bottom: 2rem;
      max-width: 400px;
      margin-left: auto;
      margin-right: auto;
    }

    @media (max-width: 768px) {
      .book-detail-container {
        padding: 1rem;
      }

      .book-detail-grid {
        grid-template-columns: 1fr;
        gap: 1.5rem;
      }

      .cover-card {
        position: static;
      }

      .book-cover {
        height: 300px;
      }

      .book-title {
        font-size: 1.5rem;
      }

      .info-grid {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 480px) {
      .book-cover {
        height: 250px;
      }

      .default-cover-icon {
        font-size: 4rem;
        width: 4rem;
        height: 4rem;
      }
    }
  `]
})
export class BookDetailComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  book: Book | null = null;
  bookMetadata: BookMetadata | null = null;
  
  isLoading = false;
  isBorrowing = false;
  isLoadingPreview = false;
  isLoadingAccess = false;
  isAuthenticated = false;
  hasFullAccess = false; // This would be determined by user's borrow status

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService,
    private borrowService: BorrowService,
    private authService: AuthService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.isAuthenticated = this.authService.isAuthenticated();
    
    this.route.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const bookId = +params['id'];
        if (bookId) {
          this.loadBookDetails(bookId);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadBookDetails(bookId: number): void {
    this.isLoading = true;
    
    // Load book details
    this.bookService.getBookById(bookId).subscribe({
      next: (book) => {
        this.book = book;
        this.loadBookMetadata(bookId);
      },
      error: (error) => {
        console.error('Error loading book:', error);
        this.isLoading = false;
      }
    });
  }

  private loadBookMetadata(bookId: number): void {
    this.bookService.getBookMetadata(bookId).subscribe({
      next: (metadata) => {
        this.bookMetadata = metadata;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading book metadata:', error);
        this.isLoading = false;
      }
    });
  }

  borrowBook(): void {
    if (!this.book || !this.isAuthenticated) {
      return;
    }

    this.isBorrowing = true;
    const request: CreateBorrowRequestRequest = { bookId: this.book.id };

    this.borrowService.createBorrowRequest(request).subscribe({
      next: (borrowRequest) => {
        this.toastr.success(
          `Borrow request for "${this.book!.title}" has been submitted for approval`,
          'Request Submitted'
        );
        this.isBorrowing = false;
      },
      error: (error) => {
        this.isBorrowing = false;
      }
    });
  }

  previewBook(): void {
    if (!this.book) return;

    this.isLoadingPreview = true;
    this.bookService.getBookPreview(this.book.id).subscribe({
      next: (access) => {
        window.open(access.secureUrl, '_blank');
        this.isLoadingPreview = false;
      },
      error: (error) => {
        this.toastr.error('Preview not available for this book', 'Error');
        this.isLoadingPreview = false;
      }
    });
  }

  readBook(): void {
    if (!this.book) return;

    this.isLoadingAccess = true;
    this.bookService.getBookAccess(this.book.id).subscribe({
      next: (access) => {
        window.open(access.secureUrl, '_blank');
        this.isLoadingAccess = false;
      },
      error: (error) => {
        this.toastr.error('You need to borrow this book first to read it', 'Access Denied');
        this.isLoadingAccess = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/books']);
  }
}