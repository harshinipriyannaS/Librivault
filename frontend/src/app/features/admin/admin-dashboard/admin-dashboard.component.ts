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
import { Subject, takeUntil } from 'rxjs';
import { BookService } from '@core/services/book.service';
import { UserService } from '@core/services/user.service';
import { BorrowService } from '@core/services/borrow.service';
import { Book, Category, BookStats } from '@core/models/book.model';
import { User, UserStats } from '@core/models/user.model';
import { BorrowStats } from '@core/models/borrow.model';

@Component({
  selector: 'app-admin-dashboard',
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
    MatChipsModule
  ],
  template: `
    <div class="admin-dashboard-container">
      <div class="dashboard-header">
        <div class="header-content">
          <h1>Admin Dashboard</h1>
          <p>Manage your digital library system</p>
        </div>
        <div class="header-actions">
          <button mat-raised-button color="accent" routerLink="/admin/analytics">
            <mat-icon>analytics</mat-icon>
            Advanced Analytics
          </button>
        </div>
      </div>

      <!-- Quick Stats -->
      <div class="stats-grid">
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="primary">library_books</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ bookStats?.totalBooks || 0 }}</h3>
                <p>Total Books</p>
                <small>{{ bookStats?.availableBooks || 0 }} available</small>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="accent">people</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ userStats?.totalUsers || 0 }}</h3>
                <p>Total Users</p>
                <small>{{ userStats?.activeUsers || 0 }} active</small>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon color="warn">assignment</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ borrowStats?.totalActiveBorrows || 0 }}</h3>
                <p>Active Borrows</p>
                <small>{{ borrowStats?.totalOverdueBooks || 0 }} overdue</small>
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
                <h3>{{ categories.length }}</h3>
                <p>Categories</p>
                <small>{{ activeCategories }} active</small>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Management Tabs -->
      <mat-card class="management-card">
        <mat-tab-group>
          <!-- Book Management -->
          <mat-tab label="Book Management">
            <div class="tab-content">
              <div class="tab-header">
                <h2>Book Management</h2>
                <div class="tab-actions">
                  <button mat-raised-button color="primary" routerLink="/admin/books/add">
                    <mat-icon>add</mat-icon>
                    Add New Book
                  </button>
                  <button mat-button routerLink="/admin/books">
                    <mat-icon>list</mat-icon>
                    View All Books
                  </button>
                </div>
              </div>

              <div class="recent-books" *ngIf="recentBooks.length > 0">
                <h3>Recently Added Books</h3>
                <div class="books-list">
                  <div class="book-item" *ngFor="let book of recentBooks">
                    <div class="book-info">
                      <h4>{{ book.title }}</h4>
                      <p>by {{ book.author }}</p>
                      <span class="category-chip">{{ book.categoryName }}</span>
                    </div>
                    <div class="book-status">
                      <span [class]="book.active ? 'status-active' : 'status-inactive'">
                        {{ book.active ? 'Active' : 'Inactive' }}
                      </span>
                      <small>{{ book.createdAt | date:'short' }}</small>
                    </div>
                    <div class="book-actions">
                      <button mat-icon-button [routerLink]="['/admin/books/edit', book.id]">
                        <mat-icon>edit</mat-icon>
                      </button>
                      <button mat-icon-button [routerLink]="['/books', book.id]">
                        <mat-icon>visibility</mat-icon>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </mat-tab>

          <!-- Category Management -->
          <mat-tab label="Categories">
            <div class="tab-content">
              <div class="tab-header">
                <h2>Category Management</h2>
                <div class="tab-actions">
                  <button mat-raised-button color="primary" routerLink="/admin/categories/add">
                    <mat-icon>add</mat-icon>
                    Add Category
                  </button>
                  <button mat-button routerLink="/admin/categories">
                    <mat-icon>list</mat-icon>
                    Manage Categories
                  </button>
                </div>
              </div>

              <div class="categories-grid">
                <mat-card class="category-card" *ngFor="let category of categories.slice(0, 6)">
                  <mat-card-content>
                    <div class="category-header">
                      <h4>{{ category.name }}</h4>
                      <span [class]="category.active ? 'status-active' : 'status-inactive'">
                        {{ category.active ? 'Active' : 'Inactive' }}
                      </span>
                    </div>
                    <p class="category-description">{{ category.description }}</p>
                    <div class="category-stats">
                      <span>{{ category.totalBooks }} books</span>
                      <span>{{ category.availableBooks }} available</span>
                    </div>
                    <div class="category-librarian" *ngIf="category.librarianName">
                      <small>Managed by: {{ category.librarianName }}</small>
                    </div>
                  </mat-card-content>
                  <mat-card-actions>
                    <button mat-button [routerLink]="['/admin/categories/edit', category.id]">
                      <mat-icon>edit</mat-icon>
                      Edit
                    </button>
                  </mat-card-actions>
                </mat-card>
              </div>
            </div>
          </mat-tab>

          <!-- User Management -->
          <mat-tab label="Users">
            <div class="tab-content">
              <div class="tab-header">
                <h2>User Management</h2>
                <div class="tab-actions">
                  <button mat-button routerLink="/admin/users">
                    <mat-icon>people</mat-icon>
                    Manage Users
                  </button>
                  <button mat-button routerLink="/admin/librarians">
                    <mat-icon>local_library</mat-icon>
                    Manage Librarians
                  </button>
                </div>
              </div>

              <div class="user-stats-grid">
                <div class="user-stat-item" *ngFor="let role of userRoleStats">
                  <div class="role-info">
                    <h4>{{ role.name }}</h4>
                    <span class="role-count">{{ role.count }}</span>
                  </div>
                  <mat-icon [color]="role.color">{{ role.icon }}</mat-icon>
                </div>
              </div>

              <div class="recent-users" *ngIf="recentUsers.length > 0">
                <h3>Recently Registered Users</h3>
                <div class="users-list">
                  <div class="user-item" *ngFor="let user of recentUsers">
                    <div class="user-info">
                      <h4>{{ user.firstName }} {{ user.lastName }}</h4>
                      <p>{{ user.email }}</p>
                      <span class="role-chip">{{ user.role }}</span>
                    </div>
                    <div class="user-status">
                      <span [class]="user.active ? 'status-active' : 'status-inactive'">
                        {{ user.active ? 'Active' : 'Inactive' }}
                      </span>
                      <small>{{ user.createdAt | date:'short' }}</small>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </mat-tab>

          <!-- System Overview -->
          <mat-tab label="System">
            <div class="tab-content">
              <div class="tab-header">
                <h2>System Overview</h2>
              </div>

              <div class="system-stats">
                <mat-card class="system-card">
                  <mat-card-header>
                    <mat-card-title>Popular Books</mat-card-title>
                  </mat-card-header>
                  <mat-card-content>
                    <div class="popular-books" *ngIf="bookStats?.mostPopularBooks">
                      <div class="popular-book" *ngFor="let book of (bookStats?.mostPopularBooks || []).slice(0, 5)">
                        <span class="book-title">{{ book.title }}</span>
                        <span class="borrow-count">{{ book.borrowCount }} borrows</span>
                      </div>
                    </div>
                  </mat-card-content>
                </mat-card>

                <mat-card class="system-card">
                  <mat-card-header>
                    <mat-card-title>Active Readers</mat-card-title>
                  </mat-card-header>
                  <mat-card-content>
                    <div class="active-readers" *ngIf="borrowStats?.mostActiveReaders">
                      <div class="active-reader" *ngFor="let reader of (borrowStats?.mostActiveReaders || []).slice(0, 5)">
                        <span class="reader-name">{{ reader.readerName }}</span>
                        <span class="borrow-count">{{ reader.borrowCount }} books</span>
                      </div>
                    </div>
                  </mat-card-content>
                </mat-card>
              </div>
            </div>
          </mat-tab>
        </mat-tab-group>
      </mat-card>
    </div>
  `,
  styles: [`
    .admin-dashboard-container {
      max-width: 1400px;
      margin: 0 auto;
      padding: 2rem;
    }

    .dashboard-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
      flex-wrap: wrap;
      gap: 1rem;
    }

    .header-content {
      text-align: left;
    }

    .dashboard-header h1 {
      color: #2c3e50;
      font-size: 2.5rem;
      margin: 0 0 0.5rem 0;
    }

    .dashboard-header p {
      color: #7f8c8d;
      font-size: 1.1rem;
      margin: 0;
    }

    .header-actions {
      display: flex;
      gap: 1rem;
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
      flex-wrap: wrap;
    }

    .recent-books h3,
    .recent-users h3 {
      color: #2c3e50;
      margin-bottom: 1rem;
    }

    .books-list,
    .users-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .book-item,
    .user-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 1rem;
      border: 1px solid #ecf0f1;
      border-radius: 8px;
      background: #fafafa;
    }

    .book-info h4,
    .user-info h4 {
      margin: 0 0 0.25rem 0;
      color: #2c3e50;
    }

    .book-info p,
    .user-info p {
      margin: 0;
      color: #7f8c8d;
      font-size: 0.9rem;
    }

    .category-chip,
    .role-chip {
      background-color: #3498db;
      color: white;
      padding: 0.25rem 0.5rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 500;
    }

    .book-status,
    .user-status {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 0.25rem;
    }

    .status-active {
      color: #27ae60;
      font-weight: 500;
    }

    .status-inactive {
      color: #e74c3c;
      font-weight: 500;
    }

    .book-actions {
      display: flex;
      gap: 0.5rem;
    }

    .categories-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 1.5rem;
    }

    .category-card {
      height: 100%;
    }

    .category-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.5rem;
    }

    .category-header h4 {
      margin: 0;
      color: #2c3e50;
    }

    .category-description {
      color: #7f8c8d;
      font-size: 0.9rem;
      margin-bottom: 1rem;
    }

    .category-stats {
      display: flex;
      gap: 1rem;
      margin-bottom: 0.5rem;
    }

    .category-stats span {
      color: #3498db;
      font-size: 0.85rem;
      font-weight: 500;
    }

    .category-librarian small {
      color: #95a5a6;
    }

    .user-stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin-bottom: 2rem;
    }

    .user-stat-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem;
      border: 1px solid #ecf0f1;
      border-radius: 8px;
      background: #fafafa;
    }

    .role-info h4 {
      margin: 0;
      color: #2c3e50;
    }

    .role-count {
      font-size: 1.5rem;
      font-weight: 600;
      color: #3498db;
    }

    .system-stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 1.5rem;
    }

    .system-card {
      height: fit-content;
    }

    .popular-books,
    .active-readers {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .popular-book,
    .active-reader {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.5rem 0;
      border-bottom: 1px solid #ecf0f1;
    }

    .popular-book:last-child,
    .active-reader:last-child {
      border-bottom: none;
    }

    .book-title,
    .reader-name {
      color: #2c3e50;
      font-weight: 500;
    }

    .borrow-count {
      color: #3498db;
      font-weight: 600;
      font-size: 0.9rem;
    }

    @media (max-width: 768px) {
      .admin-dashboard-container {
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

      .book-item,
      .user-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
      }

      .categories-grid {
        grid-template-columns: 1fr;
      }

      .system-stats {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  bookStats: BookStats | null = null;
  userStats: UserStats | null = null;
  borrowStats: BorrowStats | null = null;
  
  categories: Category[] = [];
  recentBooks: Book[] = [];
  recentUsers: User[] = [];
  
  userRoleStats = [
    { name: 'Admins', count: 0, icon: 'admin_panel_settings', color: 'warn' },
    { name: 'Librarians', count: 0, icon: 'local_library', color: 'accent' },
    { name: 'Readers', count: 0, icon: 'people', color: 'primary' }
  ];

  constructor(
    private bookService: BookService,
    private userService: UserService,
    private borrowService: BorrowService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadDashboardData(): void {
    // Load book stats
    this.bookService.getBookStats().subscribe({
      next: (stats) => this.bookStats = stats,
      error: (error) => console.error('Error loading book stats:', error)
    });

    // Load user stats
    this.userService.getUserStats().subscribe({
      next: (stats) => {
        this.userStats = stats;
        this.updateUserRoleStats(stats);
      },
      error: (error) => console.error('Error loading user stats:', error)
    });

    // Load borrow stats
    this.borrowService.getBorrowStats().subscribe({
      next: (stats) => this.borrowStats = stats,
      error: (error) => console.error('Error loading borrow stats:', error)
    });

    // Load categories
    this.bookService.getAllCategories().subscribe({
      next: (categories) => this.categories = categories,
      error: (error) => console.error('Error loading categories:', error)
    });

    // Load recent books
    this.bookService.getAllBooks(0, 5).subscribe({
      next: (response) => this.recentBooks = response.content,
      error: (error) => console.error('Error loading recent books:', error)
    });

    // Load recent users
    this.userService.getAllUsers(0, 5).subscribe({
      next: (response) => this.recentUsers = response.content,
      error: (error) => console.error('Error loading recent users:', error)
    });
  }

  private updateUserRoleStats(stats: UserStats): void {
    this.userRoleStats[0].count = stats.usersByRole['ADMIN'] || 0;
    this.userRoleStats[1].count = stats.usersByRole['LIBRARIAN'] || 0;
    this.userRoleStats[2].count = stats.usersByRole['READER'] || 0;
  }

  get activeCategories(): number {
    return this.categories.filter(cat => cat.active).length;
  }
}