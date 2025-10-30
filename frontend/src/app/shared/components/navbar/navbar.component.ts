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
      background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%) !important;
      box-shadow: 0 4px 20px rgba(44, 62, 80, 0.15);
      backdrop-filter: blur(10px);
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      height: 70px !important;
      min-height: 70px !important;
      max-height: 70px !important;
    }

    .navbar-container {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      max-width: 1400px;
      margin: 0 auto;
      padding: 0 2rem;
      height: 70px;
      box-sizing: border-box;
    }

    .navbar-brand {
      display: flex;
      align-items: center;
    }

    .brand-link {
      display: flex;
      align-items: center;
      text-decoration: none;
      color: white !important;
      font-size: 1.8rem;
      font-weight: 700;
      letter-spacing: -0.5px;
      transition: all 0.3s ease;
    }

    .brand-link:hover {
      transform: translateY(-1px);
      color: #e67e22 !important;
    }

    .brand-link mat-icon {
      font-size: 2rem;
      width: 2rem;
      height: 2rem;
      margin-right: 0.75rem;
      color: #e67e22;
    }

    .brand-text {
      background: linear-gradient(135deg, #ffffff 0%, #ecf0f1 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .navbar-nav {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      text-decoration: none;
      color: rgba(255, 255, 255, 0.9) !important;
      padding: 0.75rem 1.25rem;
      border-radius: 12px;
      transition: all 0.3s ease;
      font-weight: 500;
      font-size: 0.95rem;
      position: relative;
      overflow: hidden;
    }

    .nav-link::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(135deg, rgba(230, 126, 34, 0.1) 0%, rgba(241, 196, 15, 0.1) 100%);
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    .nav-link:hover::before {
      opacity: 1;
    }

    .nav-link:hover {
      color: white !important;
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(230, 126, 34, 0.2);
    }

    .nav-link.active {
      background: linear-gradient(135deg, #e67e22 0%, #f39c12 100%);
      color: white !important;
      box-shadow: 0 4px 15px rgba(230, 126, 34, 0.3);
    }

    .nav-link mat-icon {
      font-size: 1.2rem;
      width: 1.2rem;
      height: 1.2rem;
    }

    .navbar-user {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .notification-btn {
      background: rgba(255, 255, 255, 0.1) !important;
      color: white !important;
      border-radius: 12px !important;
      width: 48px !important;
      height: 48px !important;
      transition: all 0.3s ease;
    }

    .notification-btn:hover {
      background: rgba(230, 126, 34, 0.2) !important;
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(230, 126, 34, 0.2);
    }

    .user-menu-btn {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      background: rgba(255, 255, 255, 0.1) !important;
      color: white !important;
      border-radius: 25px !important;
      padding: 0.5rem 1.25rem !important;
      transition: all 0.3s ease;
      font-weight: 500;
    }

    .user-menu-btn:hover {
      background: rgba(230, 126, 34, 0.2) !important;
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(230, 126, 34, 0.2);
    }

    .user-name {
      font-size: 0.95rem;
      font-weight: 600;
    }

    .auth-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-left: 0.5rem;
      padding: 0.75rem 1.5rem !important;
      border-radius: 12px !important;
      font-weight: 600 !important;
      text-transform: none !important;
      transition: all 0.3s ease;
    }

    .auth-btn:first-of-type {
      background: rgba(255, 255, 255, 0.1) !important;
      color: white !important;
    }

    .auth-btn:first-of-type:hover {
      background: rgba(255, 255, 255, 0.2) !important;
      transform: translateY(-2px);
    }

    .auth-btn:last-of-type {
      background: linear-gradient(135deg, #e67e22 0%, #f39c12 100%) !important;
      color: white !important;
      box-shadow: 0 4px 15px rgba(230, 126, 34, 0.3);
    }

    .auth-btn:last-of-type:hover {
      background: linear-gradient(135deg, #d35400 0%, #e67e22 100%) !important;
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(230, 126, 34, 0.4);
    }

    .mobile-menu-btn {
      background: rgba(255, 255, 255, 0.1) !important;
      color: white !important;
      border-radius: 12px !important;
      width: 48px !important;
      height: 48px !important;
    }

    /* Menu Styling */
    ::ng-deep .mat-mdc-menu-panel {
      background: white !important;
      border-radius: 16px !important;
      box-shadow: 0 20px 60px rgba(44, 62, 80, 0.15) !important;
      border: 1px solid rgba(44, 62, 80, 0.1) !important;
      margin-top: 8px !important;
      min-width: 220px !important;
    }

    ::ng-deep .mat-mdc-menu-item {
      padding: 12px 20px !important;
      font-size: 0.95rem !important;
      font-weight: 500 !important;
      color: #2c3e50 !important;
      transition: all 0.3s ease !important;
      border-radius: 8px !important;
      margin: 4px 8px !important;
    }

    ::ng-deep .mat-mdc-menu-item:hover {
      background: linear-gradient(135deg, #e67e22 0%, #f39c12 100%) !important;
      color: white !important;
      transform: translateX(4px);
    }

    ::ng-deep .mat-mdc-menu-item mat-icon {
      margin-right: 12px !important;
      color: inherit !important;
    }

    /* Badge Styling */
    ::ng-deep .mat-badge-content {
      background: #e74c3c !important;
      color: white !important;
      font-weight: 600 !important;
      font-size: 0.75rem !important;
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .navbar-container {
        padding: 0 1rem;
      }
      
      .navbar-nav {
        display: none;
      }
      
      .user-name {
        display: none;
      }
      
      .auth-btn span {
        display: none;
      }

      .auth-btn {
        padding: 0.75rem !important;
        min-width: 48px !important;
      }

      .brand-link {
        font-size: 1.5rem;
      }

      .brand-link mat-icon {
        font-size: 1.75rem;
        width: 1.75rem;
        height: 1.75rem;
        margin-right: 0.5rem;
      }
    }

    @media (max-width: 480px) {
      .navbar-container {
        padding: 0 0.75rem;
      }

      .navbar-user {
        gap: 0.5rem;
      }
    }

    /* Ensure navbar stays on top */
    :host {
      display: block;
      position: relative;
      z-index: 1000;
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