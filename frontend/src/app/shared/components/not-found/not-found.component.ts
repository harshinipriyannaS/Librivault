import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, RouterModule, MatButtonModule, MatIconModule],
  template: `
    <div class="not-found-container">
      <div class="not-found-content">
        <div class="error-icon">
          <mat-icon>error_outline</mat-icon>
        </div>
        
        <h1 class="error-code">404</h1>
        
        <h2 class="error-title">Page Not Found</h2>
        
        <p class="error-message">
          The page you're looking for doesn't exist or has been moved.
          Don't worry, let's get you back on track!
        </p>
        
        <div class="action-buttons">
          <a mat-raised-button color="primary" routerLink="/">
            <mat-icon>home</mat-icon>
            Go Home
          </a>
          
          <a mat-button routerLink="/books">
            <mat-icon>library_books</mat-icon>
            Browse Books
          </a>
          
          <button mat-button (click)="goBack()">
            <mat-icon>arrow_back</mat-icon>
            Go Back
          </button>
        </div>
        
        <div class="suggestions">
          <h3>You might be looking for:</h3>
          <ul>
            <li><a routerLink="/books">Book Catalog</a></li>
            <li><a routerLink="/about">About Us</a></li>
            <li><a routerLink="/contact">Contact Support</a></li>
            <li><a routerLink="/auth/login">Login</a></li>
          </ul>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .not-found-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: calc(100vh - 128px);
      padding: 2rem;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    }

    .not-found-content {
      text-align: center;
      max-width: 600px;
      background: white;
      padding: 3rem;
      border-radius: 12px;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    }

    .error-icon {
      margin-bottom: 1rem;
    }

    .error-icon mat-icon {
      font-size: 4rem;
      width: 4rem;
      height: 4rem;
      color: #ff6b6b;
    }

    .error-code {
      font-size: 6rem;
      font-weight: 700;
      color: #2c3e50;
      margin: 0;
      line-height: 1;
    }

    .error-title {
      font-size: 2rem;
      color: #34495e;
      margin: 1rem 0;
      font-weight: 600;
    }

    .error-message {
      font-size: 1.1rem;
      color: #7f8c8d;
      line-height: 1.6;
      margin-bottom: 2rem;
    }

    .action-buttons {
      display: flex;
      gap: 1rem;
      justify-content: center;
      flex-wrap: wrap;
      margin-bottom: 2rem;
    }

    .action-buttons a,
    .action-buttons button {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .suggestions {
      border-top: 1px solid #ecf0f1;
      padding-top: 2rem;
      margin-top: 2rem;
    }

    .suggestions h3 {
      color: #2c3e50;
      margin-bottom: 1rem;
      font-size: 1.2rem;
    }

    .suggestions ul {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 1rem;
    }

    .suggestions li {
      margin: 0;
    }

    .suggestions a {
      color: #3498db;
      text-decoration: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      transition: all 0.3s ease;
      border: 1px solid #3498db;
    }

    .suggestions a:hover {
      background-color: #3498db;
      color: white;
    }

    @media (max-width: 768px) {
      .not-found-container {
        padding: 1rem;
      }

      .not-found-content {
        padding: 2rem 1.5rem;
      }

      .error-code {
        font-size: 4rem;
      }

      .error-title {
        font-size: 1.5rem;
      }

      .action-buttons {
        flex-direction: column;
        align-items: center;
      }

      .suggestions ul {
        flex-direction: column;
        align-items: center;
      }
    }
  `]
})
export class NotFoundComponent {
  goBack(): void {
    window.history.back();
  }
}