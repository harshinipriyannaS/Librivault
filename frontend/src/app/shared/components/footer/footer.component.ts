import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '@environments/environment';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  template: `
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-content">
          <!-- Brand Section -->
          <div class="footer-section">
            <div class="footer-brand">
              <mat-icon>library_books</mat-icon>
              <span class="brand-text">{{ appName }}</span>
            </div>
            <p class="footer-description">
              Your digital library for accessing and managing books online.
              Discover, borrow, and read your favorite books anytime, anywhere.
            </p>
          </div>

          <!-- Quick Links -->
          <div class="footer-section">
            <h4 class="footer-title">Quick Links</h4>
            <ul class="footer-links">
              <li><a routerLink="/books">Browse Books</a></li>
              <li><a routerLink="/about">About Us</a></li>
              <li><a routerLink="/contact">Contact</a></li>
              <li><a routerLink="/auth/register">Join Now</a></li>
            </ul>
          </div>

          <!-- Services -->
          <div class="footer-section">
            <h4 class="footer-title">Services</h4>
            <ul class="footer-links">
              <li><a href="#" (click)="$event.preventDefault()">Book Borrowing</a></li>
              <li><a href="#" (click)="$event.preventDefault()">Digital Reading</a></li>
              <li><a href="#" (click)="$event.preventDefault()">Premium Subscriptions</a></li>
              <li><a href="#" (click)="$event.preventDefault()">Book Recommendations</a></li>
            </ul>
          </div>

          <!-- Contact Info -->
          <div class="footer-section">
            <h4 class="footer-title">Contact Info</h4>
            <div class="contact-info">
              <div class="contact-item">
                <mat-icon>email</mat-icon>
                <span>support&#64;librivault.com</span>
              </div>
              <div class="contact-item">
                <mat-icon>phone</mat-icon>
                <span>+1 (555) 123-4567</span>
              </div>
              <div class="contact-item">
                <mat-icon>location_on</mat-icon>
                <span>123 Library St, Book City, BC 12345</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer Bottom -->
        <div class="footer-bottom">
          <div class="footer-bottom-content">
            <div class="copyright">
              <p>&copy; {{ currentYear }} {{ appName }}. All rights reserved.</p>
            </div>
            <div class="footer-links-bottom">
              <a href="#" (click)="$event.preventDefault()">Privacy Policy</a>
              <a href="#" (click)="$event.preventDefault()">Terms of Service</a>
              <a href="#" (click)="$event.preventDefault()">Cookie Policy</a>
            </div>
            <div class="version">
              <span>v{{ version }}</span>
            </div>
          </div>
        </div>
      </div>
    </footer>
  `,
  styles: [`
    .footer {
      background-color: #2c3e50;
      color: #ecf0f1;
      margin-top: auto;
    }

    .footer-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 1rem;
    }

    .footer-content {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 2rem;
      padding: 3rem 0 2rem;
    }

    .footer-section {
      display: flex;
      flex-direction: column;
    }

    .footer-brand {
      display: flex;
      align-items: center;
      margin-bottom: 1rem;
      font-size: 1.5rem;
      font-weight: 600;
    }

    .brand-text {
      margin-left: 0.5rem;
    }

    .footer-description {
      color: #bdc3c7;
      line-height: 1.6;
      margin: 0;
    }

    .footer-title {
      color: #3498db;
      margin-bottom: 1rem;
      font-size: 1.1rem;
      font-weight: 600;
    }

    .footer-links {
      list-style: none;
      padding: 0;
      margin: 0;
    }

    .footer-links li {
      margin-bottom: 0.5rem;
    }

    .footer-links a {
      color: #bdc3c7;
      text-decoration: none;
      transition: color 0.3s ease;
    }

    .footer-links a:hover {
      color: #3498db;
    }

    .contact-info {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .contact-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #bdc3c7;
    }

    .contact-item mat-icon {
      font-size: 1.2rem;
      width: 1.2rem;
      height: 1.2rem;
      color: #3498db;
    }

    .footer-bottom {
      border-top: 1px solid #34495e;
      padding: 1.5rem 0;
    }

    .footer-bottom-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
    }

    .copyright p {
      margin: 0;
      color: #bdc3c7;
    }

    .footer-links-bottom {
      display: flex;
      gap: 1.5rem;
    }

    .footer-links-bottom a {
      color: #bdc3c7;
      text-decoration: none;
      font-size: 0.9rem;
      transition: color 0.3s ease;
    }

    .footer-links-bottom a:hover {
      color: #3498db;
    }

    .version {
      color: #7f8c8d;
      font-size: 0.8rem;
    }

    @media (max-width: 768px) {
      .footer-content {
        grid-template-columns: 1fr;
        gap: 1.5rem;
        padding: 2rem 0 1.5rem;
      }

      .footer-bottom-content {
        flex-direction: column;
        text-align: center;
        gap: 0.5rem;
      }

      .footer-links-bottom {
        flex-wrap: wrap;
        justify-content: center;
      }
    }

    @media (max-width: 480px) {
      .footer-links-bottom {
        flex-direction: column;
        gap: 0.5rem;
      }
    }
  `]
})
export class FooterComponent {
  appName = environment.appName;
  version = environment.version;
  currentYear = new Date().getFullYear();
}