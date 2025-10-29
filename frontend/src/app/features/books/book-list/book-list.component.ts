import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { BookService } from '@core/services/book.service';
import { BorrowService } from '@core/services/borrow.service';
import { AuthService } from '@core/services/auth.service';
import { Book, Category, BookSearchParams } from '@core/models/book.model';
import { CreateBorrowRequestRequest } from '@core/models/borrow.model';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatBadgeModule
  ],
  template: `
    <div class="book-list-container">
      <!-- Header -->
      <div class="page-header">
        <h1>Book Catalog</h1>
        <p>Discover and borrow from our extensive digital library</p>
      </div>

      <!-- Search and Filters -->
      <mat-card class="search-card">
        <mat-card-content>
          <form [formGroup]="searchForm" class="search-form">
            <div class="search-row">
              <mat-form-field appearance="outline" class="search-field">
                <mat-label>Search books...</mat-label>
                <input matInput 
                       formControlName="query"
                       placeholder="Title, author, or ISBN">
                <mat-icon matSuffix>search</mat-icon>
              </mat-form-field>

              <mat-form-field appearance="outline" class="filter-field">
                <mat-label>Category</mat-label>
                <mat-select formControlName="categoryId">
                  <mat-option value="">All Categories</mat-option>
                  <mat-option *ngFor="let category of categories" [value]="category.id">
                    {{ category.name }}
                  </mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline" class="filter-field">
                <mat-label>Sort By</mat-label>
                <mat-select formControlName="sortBy">
                  <mat-option value="title">Title</mat-option>
                  <mat-option value="author">Author</mat-option>
                  <mat-option value="publishedDate">Published Date</mat-option>
                  <mat-option value="createdAt">Recently Added</mat-option>
                </mat-select>
              </mat-form-field>

              <button mat-raised-button 
                      color="primary" 
                      type="button"
                      (click)="clearFilters()"
                      class="clear-btn">
                <mat-icon>clear</mat-icon>
                Clear
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>

      <!-- Active Filters -->
      <div class="active-filters" *ngIf="hasActiveFilters()">
        <mat-chip-listbox>
          <mat-chip *ngIf="searchForm.get('query')?.value" 
                    (removed)="removeFilter('query')"
                    removable>
            Search: "{{ searchForm.get('query')?.value }}"
            <mat-icon matChipRemove>cancel</mat-icon>
          </mat-chip>
          <mat-chip *ngIf="searchForm.get('categoryId')?.value" 
                    (removed)="removeFilter('categoryId')"
                    removable>
            Category: {{ getCategoryName(searchForm.get('categoryId')?.value) }}
            <mat-icon matChipRemove>cancel</mat-icon>
          </mat-chip>
        </mat-chip-listbox>
      </div>

      <!-- Results Info -->
      <div class="results-info" *ngIf="!isLoading">
        <p>
          Showing {{ books.length }} of {{ totalBooks }} books
          <span *ngIf="searchForm.get('query')?.value">
            for "{{ searchForm.get('query')?.value }}"
          </span>
        </p>
      </div>

      <!-- Loading Spinner -->
      <div class="loading-container" *ngIf="isLoading">
        <mat-spinner></mat-spinner>
        <p>Loading books...</p>
      </div>

      <!-- Books Grid -->
      <div class="books-grid" *ngIf="!isLoading">
        <div class="book-card" *ngFor="let book of books">
          <mat-card>
            <div class="book-cover">
              <img *ngIf="book.coverImageUrl; else defaultCover" 
                   [src]="book.coverImageUrl" 
                   [alt]="book.title"
                   class="cover-image">
              <ng-template #defaultCover>
                <mat-icon class="default-cover-icon">book</mat-icon>
              </ng-template>
              
              <!-- Availability Badge -->
              <div class="availability-badge" 
                   [class.available]="book.availableCopies > 0"
                   [class.unavailable]="book.availableCopies === 0">
                {{ book.availableCopies > 0 ? 'Available' : 'Unavailable' }}
              </div>
            </div>

            <mat-card-content class="book-info">
              <h3 class="book-title">{{ book.title }}</h3>
              <p class="book-author">by {{ book.author }}</p>
              <p class="book-category">{{ book.categoryName }}</p>
              
              <div class="book-meta">
                <span class="copies-info">
                  <mat-icon>library_books</mat-icon>
                  {{ book.availableCopies }}/{{ book.totalCopies }} available
                </span>
                <span class="published-date">
                  <mat-icon>calendar_today</mat-icon>
                  {{ book.publishedDate | date:'yyyy' }}
                </span>
              </div>

              <p class="book-description" *ngIf="book.description">
                {{ book.description | slice:0:100 }}
                <span *ngIf="book.description.length > 100">...</span>
              </p>
            </mat-card-content>

            <mat-card-actions class="book-actions">
              <button mat-button 
                      color="primary"
                      [routerLink]="['/books', book.id]">
                <mat-icon>visibility</mat-icon>
                View Details
              </button>
              
              <button mat-raised-button 
                      color="primary"
                      *ngIf="isAuthenticated && book.availableCopies > 0"
                      (click)="borrowBook(book)"
                      [disabled]="isBorrowing">
                <mat-icon>add_shopping_cart</mat-icon>
                Borrow
              </button>
              
              <button mat-button 
                      *ngIf="book.availableCopies > 0"
                      (click)="previewBook(book)">
                <mat-icon>preview</mat-icon>
                Preview
              </button>
            </mat-card-actions>
          </mat-card>
        </div>
      </div>

      <!-- Empty State -->
      <div class="empty-state" *ngIf="!isLoading && books.length === 0">
        <mat-icon class="empty-icon">search_off</mat-icon>
        <h2>No books found</h2>
        <p *ngIf="hasActiveFilters()">
          Try adjusting your search criteria or clearing filters.
        </p>
        <p *ngIf="!hasActiveFilters()">
          No books are currently available in the catalog.
        </p>
        <button mat-raised-button color="primary" (click)="clearFilters()">
          <mat-icon>refresh</mat-icon>
          Reset Search
        </button>
      </div>

      <!-- Pagination -->
      <mat-paginator *ngIf="!isLoading && books.length > 0"
                     [length]="totalBooks"
                     [pageSize]="pageSize"
                     [pageIndex]="currentPage"
                     [pageSizeOptions]="[12, 24, 48]"
                     (page)="onPageChange($event)"
                     showFirstLastButtons>
      </mat-paginator>
    </div>
  `,
  styles: [`
    .book-list-container {
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

    .search-card {
      margin-bottom: 1rem;
    }

    .search-form {
      width: 100%;
    }

    .search-row {
      display: flex;
      gap: 1rem;
      align-items: flex-start;
    }

    .search-field {
      flex: 2;
    }

    .filter-field {
      flex: 1;
      min-width: 150px;
    }

    .clear-btn {
      height: 56px;
      min-width: 100px;
    }

    .active-filters {
      margin-bottom: 1rem;
    }

    .results-info {
      margin-bottom: 1rem;
      color: #7f8c8d;
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

    .books-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2rem;
    }

    .book-card mat-card {
      height: 100%;
      display: flex;
      flex-direction: column;
      transition: transform 0.2s ease, box-shadow 0.2s ease;
    }

    .book-card mat-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 25px rgba(0,0,0,0.15);
    }

    .book-cover {
      position: relative;
      height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(45deg, #f8f9fa, #e9ecef);
      overflow: hidden;
    }

    .cover-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .default-cover-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      color: #6c757d;
    }

    .availability-badge {
      position: absolute;
      top: 0.5rem;
      right: 0.5rem;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
    }

    .availability-badge.available {
      background-color: #d4edda;
      color: #155724;
    }

    .availability-badge.unavailable {
      background-color: #f8d7da;
      color: #721c24;
    }

    .book-info {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    .book-title {
      font-size: 1.2rem;
      font-weight: 600;
      color: #2c3e50;
      margin: 0 0 0.5rem 0;
      line-height: 1.3;
    }

    .book-author {
      color: #7f8c8d;
      margin: 0 0 0.25rem 0;
      font-style: italic;
    }

    .book-category {
      color: #3498db;
      font-size: 0.9rem;
      margin: 0 0 0.75rem 0;
      font-weight: 500;
    }

    .book-meta {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      margin-bottom: 0.75rem;
    }

    .book-meta span {
      display: flex;
      align-items: center;
      gap: 0.25rem;
      font-size: 0.85rem;
      color: #6c757d;
    }

    .book-meta mat-icon {
      font-size: 1rem;
      width: 1rem;
      height: 1rem;
    }

    .book-description {
      color: #6c757d;
      font-size: 0.9rem;
      line-height: 1.4;
      margin-top: auto;
    }

    .book-actions {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
      padding: 1rem;
    }

    .book-actions button {
      flex: 1;
      min-width: 120px;
    }

    .empty-state {
      text-align: center;
      padding: 4rem 2rem;
      color: #7f8c8d;
    }

    .empty-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      margin-bottom: 1rem;
      color: #bdc3c7;
    }

    .empty-state h2 {
      color: #2c3e50;
      margin-bottom: 1rem;
    }

    .empty-state p {
      margin-bottom: 1.5rem;
      max-width: 400px;
      margin-left: auto;
      margin-right: auto;
    }

    @media (max-width: 768px) {
      .book-list-container {
        padding: 1rem;
      }

      .page-header h1 {
        font-size: 2rem;
      }

      .search-row {
        flex-direction: column;
      }

      .search-field,
      .filter-field {
        flex: none;
        width: 100%;
      }

      .books-grid {
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        gap: 1rem;
      }

      .book-actions {
        flex-direction: column;
      }

      .book-actions button {
        min-width: auto;
      }
    }

    @media (max-width: 480px) {
      .books-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class BookListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  searchForm: FormGroup;
  books: Book[] = [];
  categories: Category[] = [];
  
  isLoading = false;
  isBorrowing = false;
  isAuthenticated = false;
  
  // Pagination
  currentPage = 0;
  pageSize = 12;
  totalBooks = 0;

  constructor(
    private fb: FormBuilder,
    private bookService: BookService,
    private borrowService: BorrowService,
    private authService: AuthService,
    private toastr: ToastrService
  ) {
    this.searchForm = this.fb.group({
      query: [''],
      categoryId: [''],
      sortBy: ['title'],
      sortDirection: ['asc']
    });
  }

  ngOnInit(): void {
    this.isAuthenticated = this.authService.isAuthenticated();
    
    // Load initial data
    this.loadCategories();
    this.loadBooks();

    // Setup search form subscription
    this.searchForm.valueChanges
      .pipe(
        takeUntil(this.destroy$),
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.currentPage = 0;
        this.loadBooks();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadCategories(): void {
    this.bookService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories.filter(cat => cat.active);
      },
      error: (error) => {
        console.error('Error loading categories:', error);
      }
    });
  }

  private loadBooks(): void {
    this.isLoading = true;
    const searchParams: BookSearchParams = {
      ...this.searchForm.value,
      page: this.currentPage,
      size: this.pageSize
    };

    // Remove empty values
    Object.keys(searchParams).forEach(key => {
      if (searchParams[key as keyof BookSearchParams] === '' || 
          searchParams[key as keyof BookSearchParams] === null) {
        delete searchParams[key as keyof BookSearchParams];
      }
    });

    this.bookService.searchBooks(searchParams).subscribe({
      next: (response) => {
        this.books = response.content;
        this.totalBooks = response.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading books:', error);
        this.isLoading = false;
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadBooks();
  }

  clearFilters(): void {
    this.searchForm.reset({
      query: '',
      categoryId: '',
      sortBy: 'title',
      sortDirection: 'asc'
    });
  }

  removeFilter(filterName: string): void {
    this.searchForm.patchValue({ [filterName]: '' });
  }

  hasActiveFilters(): boolean {
    const values = this.searchForm.value;
    return values.query || values.categoryId;
  }

  getCategoryName(categoryId: number): string {
    const category = this.categories.find(cat => cat.id === categoryId);
    return category ? category.name : '';
  }

  borrowBook(book: Book): void {
    if (!this.isAuthenticated) {
      this.toastr.warning('Please login to borrow books', 'Authentication Required');
      return;
    }

    this.isBorrowing = true;
    const request: CreateBorrowRequestRequest = { bookId: book.id };

    this.borrowService.createBorrowRequest(request).subscribe({
      next: (borrowRequest) => {
        this.toastr.success(
          `Borrow request for "${book.title}" has been submitted for approval`,
          'Request Submitted'
        );
        this.isBorrowing = false;
      },
      error: (error) => {
        this.isBorrowing = false;
      }
    });
  }

  previewBook(book: Book): void {
    this.bookService.getBookPreview(book.id).subscribe({
      next: (access) => {
        window.open(access.secureUrl, '_blank');
      },
      error: (error) => {
        this.toastr.error('Preview not available for this book', 'Error');
      }
    });
  }
}