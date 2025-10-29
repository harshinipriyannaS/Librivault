import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { AuthService } from '@core/services/auth.service';
import { BookService } from '@core/services/book.service';
import { Book } from '@core/models/book.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule
  ],
  template: `
    <div class="home-container">
      <!-- Hero Section -->
      <section class="hero-section">
        <div class="hero-content">
          <div class="hero-text">
            <h1 class="hero-title">Welcome to LibriVault</h1>
            <p class="hero-subtitle">
              Your digital gateway to thousands of books. Discover, borrow, and read 
              your favorite titles from anywhere, anytime.
            </p>
            <div class="hero-actions">
              <ng-container *ngIf="!isAuthenticated">
                <a mat-raised-button color="primary" routerLink="/auth/register" class="cta-button">
                  <mat-icon>person_add</mat-icon>
                  Get Started
                </a>
                <a mat-button routerLink="/books" class="secondary-button">
                  <mat-icon>library_books</mat-icon>
                  Browse Books
                </a>
              </ng-container>
              <ng-container *ngIf="isAuthenticated">
                <a mat-raised-button color="primary" routerLink="/dashboard" class="cta-button">
                  <mat-icon>dashboard</mat-icon>
                  Go to Dashboard
                </a>
                <a mat-button routerLink="/books" class="secondary-button">
                  <mat-icon>library_books</mat-icon>
                  Browse Books
                </a>
              </ng-container>
            </div>
          </div>
          <div class="hero-image">
            <mat-icon class="hero-icon">auto_stories</mat-icon>
          </div>
        </div>
      </section>

      <!-- Features Section -->
      <section class="features-section">
        <div class="section-container">
          <h2 class="section-title">Why Choose LibriVault?</h2>
          <div class="features-grid">
            <div class="feature-card">
              <mat-card>
                <mat-card-content>
                  <div class="feature-icon">
                    <mat-icon color="primary">cloud_download</mat-icon>
                  </div>
                  <h3>Digital Access</h3>
                  <p>Access your books instantly from any device. No waiting, no physical limitations.</p>
                </mat-card-content>
              </mat-card>
            </div>

            <div class="feature-card">
              <mat-card>
                <mat-card-content>
                  <div class="feature-icon">
                    <mat-icon color="primary">schedule</mat-icon>
                  </div>
                  <h3>Flexible Borrowing</h3>
                  <p>Borrow books for up to 14 days with easy renewal options. Return early to earn credits.</p>
                </mat-card-content>
              </mat-card>
            </div>

            <div class="feature-card">
              <mat-card>
                <mat-card-content>
                  <div class="feature-icon">
                    <mat-icon color="primary">star</mat-icon>
                  </div>
                  <h3>Premium Features</h3>
                  <p>Upgrade to Premium for unlimited borrowing, priority access, and exclusive content.</p>
                </mat-card-content>
              </mat-card>
            </div>

            <div class="feature-card">
              <mat-card>
                <mat-card-content>
                  <div class="feature-icon">
                    <mat-icon color="primary">search</mat-icon>
                  </div>
                  <h3>Smart Search</h3>
                  <p>Find books by title, author, category, or ISBN. Advanced filters help you discover new favorites.</p>
                </mat-card-content>
              </mat-card>
            </div>

            <div class="feature-card">
              <mat-card>
                <mat-card-content>
                  <div class="feature-icon">
                    <mat-icon color="primary">notifications</mat-icon>
                  </div>
                  <h3>Smart Notifications</h3>
                  <p>Get reminders for due dates, new arrivals, and subscription updates via email and dashboard.</p>
                </mat-card-content>
              </mat-card>
            </div>

            <div class="feature-card">
              <mat-card>
                <mat-card-content>
                  <div class="feature-icon">
                    <mat-icon color="primary">security</mat-icon>
                  </div>
                  <h3>Secure & Reliable</h3>
                  <p>Your data is protected with enterprise-grade security. Enjoy uninterrupted access to your library.</p>
                </mat-card-content>
              </mat-card>
            </div>
          </div>
        </div>
      </section>

      <!-- Popular Books Section -->
      <section class="popular-books-section" *ngIf="popularBooks.length > 0">
        <div class="section-container">
          <h2 class="section-title">Popular Books</h2>
          <div class="books-grid">
            <div class="book-card" *ngFor="let book of popularBooks">
              <mat-card>
                <div class="book-cover">
                  <mat-icon>book</mat-icon>
                </div>
                <mat-card-content>
                  <h4 class="book-title">{{ book.title }}</h4>
                  <p class="book-author">by {{ book.author }}</p>
                  <p class="book-category">{{ book.categoryName }}</p>
                </mat-card-content>
                <mat-card-actions>
                  <a mat-button routerLink="/books/{{ book.id }}" color="primary">
                    View Details
                  </a>
                </mat-card-actions>
              </mat-card>
            </div>
          </div>
          <div class="section-actions">
            <a mat-button routerLink="/books" color="primary">
              View All Books
              <mat-icon>arrow_forward</mat-icon>
            </a>
          </div>
        </div>
      </section>

      <!-- CTA Section -->
      <section class="cta-section" *ngIf="!isAuthenticated">
        <div class="cta-content">
          <h2>Ready to Start Reading?</h2>
          <p>Join thousands of readers who have discovered their next favorite book with LibriVault.</p>
          <div class="cta-actions">
            <a mat-raised-button color="accent" routerLink="/auth/register" class="cta-button">
              <mat-icon>person_add</mat-icon>
              Sign Up Now
            </a>
            <a mat-button routerLink="/auth/login" class="secondary-button">
              <mat-icon>login</mat-icon>
              Already have an account?
            </a>
          </div>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .home-container {
      min-height: calc(100vh - 128px);
    }

    /* Hero Section */
    .hero-section {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 4rem 2rem;
      min-height: 500px;
      display: flex;
      align-items: center;
    }

    .hero-content {
      max-width: 1200px;
      margin: 0 auto;
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 3rem;
      align-items: center;
    }

    .hero-title {
      font-size: 3rem;
      font-weight: 700;
      margin-bottom: 1rem;
      line-height: 1.2;
    }

    .hero-subtitle {
      font-size: 1.2rem;
      line-height: 1.6;
      margin-bottom: 2rem;
      opacity: 0.9;
    }

    .hero-actions {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .cta-button, .secondary-button {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1.5rem;
    }

    .hero-image {
      display: flex;
      justify-content: center;
      align-items: center;
    }

    .hero-icon {
      font-size: 12rem;
      width: 12rem;
      height: 12rem;
      opacity: 0.8;
    }

    /* Sections */
    .section-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 2rem;
    }

    .section-title {
      text-align: center;
      font-size: 2.5rem;
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 3rem;
    }

    /* Features Section */
    .features-section {
      padding: 4rem 0;
      background-color: #f8f9fa;
    }

    .features-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 2rem;
    }

    .feature-card mat-card {
      height: 100%;
      text-align: center;
      transition: transform 0.3s ease, box-shadow 0.3s ease;
    }

    .feature-card mat-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 25px rgba(0,0,0,0.15);
    }

    .feature-icon {
      margin-bottom: 1rem;
    }

    .feature-icon mat-icon {
      font-size: 3rem;
      width: 3rem;
      height: 3rem;
    }

    .feature-card h3 {
      color: #2c3e50;
      margin-bottom: 1rem;
      font-size: 1.3rem;
    }

    .feature-card p {
      color: #7f8c8d;
      line-height: 1.6;
    }

    /* Popular Books Section */
    .popular-books-section {
      padding: 4rem 0;
    }

    .books-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 2rem;
      margin-bottom: 2rem;
    }

    .book-card mat-card {
      height: 100%;
      display: flex;
      flex-direction: column;
    }

    .book-cover {
      height: 150px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(45deg, #f0f0f0, #e0e0e0);
    }

    .book-cover mat-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      color: #666;
    }

    .book-title {
      font-weight: 600;
      margin: 0.5rem 0;
      color: #2c3e50;
    }

    .book-author {
      color: #7f8c8d;
      margin: 0.25rem 0;
    }

    .book-category {
      color: #3498db;
      font-size: 0.9rem;
      margin: 0.25rem 0;
    }

    .section-actions {
      text-align: center;
      margin-top: 2rem;
    }

    /* CTA Section */
    .cta-section {
      background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
      color: white;
      padding: 4rem 2rem;
      text-align: center;
    }

    .cta-content h2 {
      font-size: 2.5rem;
      margin-bottom: 1rem;
    }

    .cta-content p {
      font-size: 1.2rem;
      margin-bottom: 2rem;
      opacity: 0.9;
    }

    .cta-actions {
      display: flex;
      gap: 1rem;
      justify-content: center;
      flex-wrap: wrap;
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .hero-content {
        grid-template-columns: 1fr;
        text-align: center;
      }

      .hero-title {
        font-size: 2rem;
      }

      .hero-icon {
        font-size: 8rem;
        width: 8rem;
        height: 8rem;
      }

      .section-title {
        font-size: 2rem;
      }

      .features-grid {
        grid-template-columns: 1fr;
      }

      .books-grid {
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      }

      .cta-content h2 {
        font-size: 2rem;
      }

      .cta-actions {
        flex-direction: column;
        align-items: center;
      }
    }
  `]
})
export class HomeComponent implements OnInit {
  isAuthenticated = false;
  popularBooks: Book[] = [];

  constructor(
    private authService: AuthService,
    private bookService: BookService
  ) {}

  ngOnInit(): void {
    this.isAuthenticated = this.authService.isAuthenticated();
    this.loadPopularBooks();
  }

  private loadPopularBooks(): void {
    // Load first few books as popular books for demo
    this.bookService.getAllBooks(0, 6).subscribe({
      next: (response) => {
        this.popularBooks = response.content;
      },
      error: (error) => {
        console.error('Error loading popular books:', error);
      }
    });
  }
}