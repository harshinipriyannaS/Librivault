import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '@core/services/auth.service';
import { NotificationService } from '@core/services/notification.service';
import { User, UserRole } from '@core/models/user.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatBadgeModule,
    MatDividerModule
  ],
  template: `
    <mat-toolbar color="primary" class="navbar">
      <div class="navbar-container">
        <!-- Logo and Brand -->
        <div class="navbar-brand">
          <a routerLink="/" class="brand-link">
            <mat-icon>library_books</mat-icon>
            <span class="brand-text">LibriVault</span>
          </a>
        </div>

        <!-- Navigation Links -->
        <nav class="navbar-nav" *ngIf="!isMobile">
          <a routerLink="/books" routerLinkActive="active" class="nav-link">
            <mat-icon>book</mat-icon>
            <span>Books</span>
          </a>
          
          <a routerLink="/about" routerLinkActive="active" class="nav-link">
            <mat-icon>info</mat-icon>
            <span>About</span>
          </a>
          
          <a routerLink="/contact" routerLinkActive="active" class="nav-link">
            <mat-icon>contact_mail</mat-icon>
            <span>Contact</span>
          </a>
        </nav>

        <!-- User Menu -->
        <div class="navbar-user">
          <ng-container *ngIf="isAuthenticated; else guestMenu">
            <!-- Notifications -->
            <button mat-icon-button routerLink="/notifications" class="notification-btn">
              <mat-icon [matBadge]="unreadCount > 0 ? unreadCount : null" matBadgeColor="warn">notifications</mat-icon>
            </button>

            <!-- User Menu -->
            <button mat-button [matMenuTriggerFor]="userMenu" class="user-menu-btn">
              <mat-icon>account_circle</mat-icon>
              <span class="user-name">{{ currentUser?.firstName }}</span>
              <mat-icon>arrow_drop_down</mat-icon>
            </button>

            <mat-menu #userMenu="matMenu">
              <button mat-menu-item routerLink="/dashboard">
                <mat-icon>dashboard</mat-icon>
                <span>Dashboard</span>
              </button>
              
              <button mat-menu-item routerLink="/profile">
                <mat-icon>person</mat-icon>
                <span>Profile</span>
              </button>

              <button mat-menu-item routerLink="/subscription/plans">
                <mat-icon>card_membership</mat-icon>
                <span>Subscription</span>
              </button>

              <button mat-menu-item routerLink="/subscription/payment-history">
                <mat-icon>receipt</mat-icon>
                <span>Payment History</span>
              </button>

              <mat-divider></mat-divider>

              <!-- Role-specific menu items -->
              <ng-container *ngIf="isAdmin">
                <button mat-menu-item routerLink="/admin">
                  <mat-icon>admin_panel_settings</mat-icon>
                  <span>Admin Panel</span>
                </button>
              </ng-container>

              <ng-container *ngIf="isLibrarian || isAdmin">
                <button mat-menu-item routerLink="/librarian">
                  <mat-icon>local_library</mat-icon>
                  <span>Librarian Panel</span>
                </button>
              </ng-container>

              <mat-divider></mat-divider>

              <button mat-menu-item (click)="logout()">
                <mat-icon>logout</mat-icon>
                <span>Logout</span>
              </button>
            </mat-menu>
          </ng-container>

          <ng-template #guestMenu>
            <a mat-button routerLink="/auth/login" class="auth-btn">
              <mat-icon>login</mat-icon>
              <span>Login</span>
            </a>
            <a mat-raised-button color="accent" routerLink="/auth/register" class="auth-btn">
              <mat-icon>person_add</mat-icon>
              <span>Register</span>
            </a>
          </ng-template>

          <!-- Mobile Menu Toggle -->
          <button mat-icon-button *ngIf="isMobile" [matMenuTriggerFor]="mobileMenu" class="mobile-menu-btn">
            <mat-icon>menu</mat-icon>
          </button>

          <mat-menu #mobileMenu="matMenu">
            <button mat-menu-item routerLink="/books">
              <mat-icon>book</mat-icon>
              <span>Books</span>
            </button>
            <button mat-menu-item routerLink="/about">
              <mat-icon>info</mat-icon>
              <span>About</span>
            </button>
            <button mat-menu-item routerLink="/contact">
              <mat-icon>contact_mail</mat-icon>
              <span>Contact</span>
            </button>
          </mat-menu>
        </div>
      </div>
    </mat-toolbar>
  `,
  styles: [`
    .navbar {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      z-index: 1000;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .navbar-container {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 1rem;
    }

    .navbar-brand {
      display: flex;
      align-items: center;
    }

    .brand-link {
      display: flex;
      align-items: center;
      text-decoration: none;
      color: inherit;
      font-size: 1.5rem;
      font-weight: 500;
    }

    .brand-text {
      margin-left: 0.5rem;
    }

    .navbar-nav {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      text-decoration: none;
      color: inherit;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      transition: background-color 0.3s ease;
    }

    .nav-link:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }

    .nav-link.active {
      background-color: rgba(255, 255, 255, 0.2);
    }

    .navbar-user {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .user-menu-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .user-name {
      margin: 0 0.5rem;
    }

    .auth-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-left: 0.5rem;
    }

    .notification-btn {
      margin-right: 0.5rem;
    }

    @media (max-width: 768px) {
      .navbar-nav {
        display: none;
      }
      
      .user-name {
        display: none;
      }
      
      .auth-btn span {
        display: none;
      }
    }
  `]
})
export class NavbarComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  currentUser: User | null = null;
  isAuthenticated = false;
  isMobile = false;
  unreadCount = 0;

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Subscribe to authentication state
    this.authService.isAuthenticated$
      .pipe(takeUntil(this.destroy$))
      .subscribe(isAuth => this.isAuthenticated = isAuth);

    // Subscribe to current user
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => this.currentUser = user);

    // Subscribe to notification count
    this.notificationService.unreadCount$
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => this.unreadCount = count);

    // Check if mobile
    this.checkMobile();
    window.addEventListener('resize', () => this.checkMobile());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private checkMobile(): void {
    this.isMobile = window.innerWidth < 768;
  }

  get isAdmin(): boolean {
    return this.currentUser?.role === UserRole.ADMIN;
  }

  get isLibrarian(): boolean {
    return this.currentUser?.role === UserRole.LIBRARIAN;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}