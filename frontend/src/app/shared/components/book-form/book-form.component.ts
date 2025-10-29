import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { Subject, takeUntil } from 'rxjs';
import { BookService } from '@core/services/book.service';
import { Book, BookRequest, Category } from '@core/models/book.model';

@Component({
  selector: 'app-book-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ],
  template: `
    <mat-card class="book-form-card">
      <mat-card-header>
        <mat-card-title>
          <mat-icon>{{ isEditMode ? 'edit' : 'add' }}</mat-icon>
          {{ isEditMode ? 'Edit Book' : 'Add New Book' }}
        </mat-card-title>
        <mat-card-subtitle>
          {{ isEditMode ? 'Update book information' : 'Add a new book to the library' }}
        </mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        <form [formGroup]="bookForm" (ngSubmit)="onSubmit()" class="book-form">
          <!-- Basic Information -->
          <div class="form-section">
            <h3>Basic Information</h3>
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Title *</mat-label>
              <input matInput formControlName="title" placeholder="Enter book title">
              <mat-icon matSuffix>title</mat-icon>
              <mat-error *ngIf="bookForm.get('title')?.hasError('required')">
                Title is required
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Author *</mat-label>
              <input matInput formControlName="author" placeholder="Enter author name">
              <mat-icon matSuffix>person</mat-icon>
              <mat-error *ngIf="bookForm.get('author')?.hasError('required')">
                Author is required
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>ISBN</mat-label>
              <input matInput formControlName="isbn" placeholder="Enter ISBN">
              <mat-icon matSuffix>qr_code</mat-icon>
              <mat-error *ngIf="bookForm.get('isbn')?.hasError('pattern')">
                Please enter a valid ISBN
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Category *</mat-label>
              <mat-select formControlName="categoryId">
                <mat-option *ngFor="let category of categories" [value]="category.id">
                  {{ category.name }}
                </mat-option>
              </mat-select>
              <mat-error *ngIf="bookForm.get('categoryId')?.hasError('required')">
                Category is required
              </mat-error>
            </mat-form-field>
          </div>

          <mat-divider class="section-divider"></mat-divider>

          <!-- Publication Details -->
          <div class="form-section">
            <h3>Publication Details</h3>
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Published Date *</mat-label>
              <input matInput 
                     [matDatepicker]="picker" 
                     formControlName="publishedDate"
                     placeholder="Select publication date">
              <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
              <mat-datepicker #picker></mat-datepicker>
              <mat-error *ngIf="bookForm.get('publishedDate')?.hasError('required')">
                Published date is required
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Total Copies *</mat-label>
              <input matInput 
                     type="number" 
                     formControlName="totalCopies" 
                     placeholder="Enter number of copies"
                     min="1">
              <mat-icon matSuffix>library_books</mat-icon>
              <mat-error *ngIf="bookForm.get('totalCopies')?.hasError('required')">
                Total copies is required
              </mat-error>
              <mat-error *ngIf="bookForm.get('totalCopies')?.hasError('min')">
                Must have at least 1 copy
              </mat-error>
            </mat-form-field>
          </div>

          <mat-divider class="section-divider"></mat-divider>

          <!-- Description -->
          <div class="form-section">
            <h3>Description</h3>
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Description</mat-label>
              <textarea matInput 
                        formControlName="description" 
                        placeholder="Enter book description"
                        rows="4"
                        maxlength="1000"></textarea>
              <mat-hint align="end">{{ getDescriptionLength() }}/1000</mat-hint>
            </mat-form-field>
          </div>

          <mat-divider class="section-divider"></mat-divider>

          <!-- File Upload Section -->
          <div class="form-section" *ngIf="isEditMode && book">
            <h3>File Management</h3>
            
            <div class="file-upload-section">
              <div class="upload-item">
                <div class="upload-info">
                  <mat-icon>upload_file</mat-icon>
                  <div>
                    <h4>Book File (PDF)</h4>
                    <p>Upload the main book file in PDF format</p>
                  </div>
                </div>
                <div class="upload-actions">
                  <input #bookFileInput 
                         type="file" 
                         accept=".pdf"
                         (change)="onBookFileSelected($event)"
                         style="display: none;">
                  <button mat-button 
                          type="button"
                          (click)="bookFileInput.click()"
                          [disabled]="isUploadingFile">
                    <mat-spinner *ngIf="isUploadingFile" diameter="20"></mat-spinner>
                    <mat-icon *ngIf="!isUploadingFile">upload</mat-icon>
                    <span *ngIf="!isUploadingFile">Upload</span>
                  </button>
                </div>
              </div>

              <div class="upload-item">
                <div class="upload-info">
                  <mat-icon>image</mat-icon>
                  <div>
                    <h4>Cover Image</h4>
                    <p>Upload a cover image for the book</p>
                  </div>
                </div>
                <div class="upload-actions">
                  <input #coverFileInput 
                         type="file" 
                         accept="image/*"
                         (change)="onCoverFileSelected($event)"
                         style="display: none;">
                  <button mat-button 
                          type="button"
                          (click)="coverFileInput.click()"
                          [disabled]="isUploadingCover">
                    <mat-spinner *ngIf="isUploadingCover" diameter="20"></mat-spinner>
                    <mat-icon *ngIf="!isUploadingCover">upload</mat-icon>
                    <span *ngIf="!isUploadingCover">Upload</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Form Actions -->
          <div class="form-actions">
            <button mat-button 
                    type="button" 
                    (click)="onCancel()">
              <mat-icon>cancel</mat-icon>
              Cancel
            </button>
            
            <button mat-raised-button 
                    color="primary" 
                    type="submit"
                    [disabled]="bookForm.invalid || isSubmitting">
              <mat-spinner *ngIf="isSubmitting" diameter="20"></mat-spinner>
              <mat-icon *ngIf="!isSubmitting">{{ isEditMode ? 'save' : 'add' }}</mat-icon>
              <span *ngIf="!isSubmitting">{{ isEditMode ? 'Update Book' : 'Add Book' }}</span>
            </button>
          </div>
        </form>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .book-form-card {
      max-width: 800px;
      margin: 0 auto;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    .book-form {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .form-section {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .form-section h3 {
      color: #2c3e50;
      margin: 0;
      font-size: 1.2rem;
      font-weight: 600;
    }

    .full-width {
      width: 100%;
    }

    .section-divider {
      margin: 1rem 0;
    }

    .file-upload-section {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .upload-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem;
      border: 1px solid #ecf0f1;
      border-radius: 8px;
      background: #fafafa;
    }

    .upload-info {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .upload-info mat-icon {
      font-size: 2rem;
      width: 2rem;
      height: 2rem;
      color: #3498db;
    }

    .upload-info h4 {
      margin: 0 0 0.25rem 0;
      color: #2c3e50;
      font-size: 1rem;
    }

    .upload-info p {
      margin: 0;
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .upload-actions {
      display: flex;
      gap: 0.5rem;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
      margin-top: 2rem;
      padding-top: 1rem;
      border-top: 1px solid #ecf0f1;
    }

    .form-actions button {
      min-width: 120px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    mat-spinner {
      margin-right: 0.5rem;
    }

    @media (max-width: 768px) {
      .book-form-card {
        margin: 1rem;
        max-width: none;
      }

      .upload-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
      }

      .form-actions {
        flex-direction: column-reverse;
      }

      .form-actions button {
        width: 100%;
      }
    }
  `]
})
export class BookFormComponent implements OnInit, OnDestroy {
  @Input() book: Book | null = null;
  @Input() isEditMode = false;
  @Output() bookSaved = new EventEmitter<Book>();
  @Output() cancelled = new EventEmitter<void>();

  private destroy$ = new Subject<void>();
  
  bookForm: FormGroup;
  categories: Category[] = [];
  
  isSubmitting = false;
  isUploadingFile = false;
  isUploadingCover = false;

  constructor(
    private fb: FormBuilder,
    private bookService: BookService
  ) {
    this.bookForm = this.fb.group({
      title: ['', [Validators.required]],
      author: ['', [Validators.required]],
      isbn: ['', [Validators.pattern(/^(?:\d{9}[\dX]|\d{13})$/)]],
      description: [''],
      categoryId: ['', [Validators.required]],
      totalCopies: [1, [Validators.required, Validators.min(1)]],
      publishedDate: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
    
    if (this.isEditMode && this.book) {
      this.populateForm();
    }
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

  private populateForm(): void {
    if (this.book) {
      this.bookForm.patchValue({
        title: this.book.title,
        author: this.book.author,
        isbn: this.book.isbn,
        description: this.book.description,
        categoryId: this.book.categoryId,
        totalCopies: this.book.totalCopies,
        publishedDate: new Date(this.book.publishedDate)
      });
    }
  }

  onSubmit(): void {
    if (this.bookForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      const formValue = this.bookForm.value;
      
      // Format the date
      const bookRequest: BookRequest = {
        ...formValue,
        publishedDate: formValue.publishedDate.toISOString().split('T')[0]
      };

      const operation = this.isEditMode && this.book
        ? this.bookService.updateBook(this.book.id, bookRequest)
        : this.bookService.createBook(bookRequest);

      operation.subscribe({
        next: (book) => {
          this.bookSaved.emit(book);
          this.isSubmitting = false;
        },
        error: (error) => {
          console.error('Error saving book:', error);
          this.isSubmitting = false;
        }
      });
    }
  }

  onCancel(): void {
    this.cancelled.emit();
  }

  onBookFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0 && this.book) {
      const file = input.files[0];
      this.isUploadingFile = true;
      
      this.bookService.uploadBookFile(this.book.id, file).subscribe({
        next: () => {
          this.isUploadingFile = false;
          // You might want to emit an event or show a success message
        },
        error: (error) => {
          console.error('Error uploading book file:', error);
          this.isUploadingFile = false;
        }
      });
    }
  }

  onCoverFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0 && this.book) {
      const file = input.files[0];
      this.isUploadingCover = true;
      
      this.bookService.uploadBookCover(this.book.id, file).subscribe({
        next: () => {
          this.isUploadingCover = false;
          // You might want to emit an event or show a success message
        },
        error: (error) => {
          console.error('Error uploading cover:', error);
          this.isUploadingCover = false;
        }
      });
    }
  }

  getDescriptionLength(): number {
    return this.bookForm.get('description')?.value?.length || 0;
  }
}