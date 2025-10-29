import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { Subject, takeUntil } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '@core/services/auth.service';
import { UserService } from '@core/services/user.service';
import { User, UpdateProfileRequest, ChangePasswordRequest } from '@core/models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ],
  template: `
    <div class="profile-container">
      <div class="profile-header">
        <h1>Profile Settings</h1>
        <p>Manage your account information and preferences</p>
      </div>

      <mat-card class="profile-card">
        <mat-tab-group>
          <!-- Profile Information Tab -->
          <mat-tab label="Profile Information">
            <div class="tab-content">
              <form [formGroup]="profileForm" (ngSubmit)="updateProfile()" class="profile-form">
                <div class="form-row">
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>First Name</mat-label>
                    <input matInput formControlName="firstName" placeholder="Enter first name">
                    <mat-error *ngIf="profileForm.get('firstName')?.hasError('required')">
                      First name is required
                    </mat-error>
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>Last Name</mat-label>
                    <input matInput formControlName="lastName" placeholder="Enter last name">
                    <mat-error *ngIf="profileForm.get('lastName')?.hasError('required')">
                      Last name is required
                    </mat-error>
                  </mat-form-field>
                </div>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Email</mat-label>
                  <input matInput type="email" formControlName="email" placeholder="Enter email">
                  <mat-icon matSuffix>email</mat-icon>
                  <mat-error *ngIf="profileForm.get('email')?.hasError('required')">
                    Email is required
                  </mat-error>
                  <mat-error *ngIf="profileForm.get('email')?.hasError('email')">
                    Please enter a valid email
                  </mat-error>
                </mat-form-field>

                <div class="form-actions">
                  <button mat-raised-button 
                          color="primary" 
                          type="submit"
                          [disabled]="profileForm.invalid || isUpdatingProfile">
                    <mat-spinner *ngIf="isUpdatingProfile" diameter="20"></mat-spinner>
                    <span *ngIf="!isUpdatingProfile">Update Profile</span>
                  </button>
                </div>
              </form>
            </div>
          </mat-tab>

          <!-- Change Password Tab -->
          <mat-tab label="Change Password">
            <div class="tab-content">
              <form [formGroup]="passwordForm" (ngSubmit)="changePassword()" class="password-form">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Current Password</mat-label>
                  <input matInput 
                         [type]="hideCurrentPassword ? 'password' : 'text'"
                         formControlName="currentPassword"
                         placeholder="Enter current password">
                  <button mat-icon-button 
                          matSuffix 
                          type="button"
                          (click)="hideCurrentPassword = !hideCurrentPassword">
                    <mat-icon>{{hideCurrentPassword ? 'visibility_off' : 'visibility'}}</mat-icon>
                  </button>
                  <mat-error *ngIf="passwordForm.get('currentPassword')?.hasError('required')">
                    Current password is required
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>New Password</mat-label>
                  <input matInput 
                         [type]="hideNewPassword ? 'password' : 'text'"
                         formControlName="newPassword"
                         placeholder="Enter new password">
                  <button mat-icon-button 
                          matSuffix 
                          type="button"
                          (click)="hideNewPassword = !hideNewPassword">
                    <mat-icon>{{hideNewPassword ? 'visibility_off' : 'visibility'}}</mat-icon>
                  </button>
                  <mat-error *ngIf="passwordForm.get('newPassword')?.hasError('required')">
                    New password is required
                  </mat-error>
                  <mat-error *ngIf="passwordForm.get('newPassword')?.hasError('minlength')">
                    Password must be at least 6 characters
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Confirm New Password</mat-label>
                  <input matInput 
                         [type]="hideConfirmPassword ? 'password' : 'text'"
                         formControlName="confirmPassword"
                         placeholder="Confirm new password">
                  <button mat-icon-button 
                          matSuffix 
                          type="button"
                          (click)="hideConfirmPassword = !hideConfirmPassword">
                    <mat-icon>{{hideConfirmPassword ? 'visibility_off' : 'visibility'}}</mat-icon>
                  </button>
                  <mat-error *ngIf="passwordForm.get('confirmPassword')?.hasError('required')">
                    Please confirm your new password
                  </mat-error>
                  <mat-error *ngIf="passwordForm.hasError('passwordMismatch') && passwordForm.get('confirmPassword')?.touched">
                    Passwords do not match
                  </mat-error>
                </mat-form-field>

                <div class="form-actions">
                  <button mat-raised-button 
                          color="primary" 
                          type="submit"
                          [disabled]="passwordForm.invalid || isChangingPassword">
                    <mat-spinner *ngIf="isChangingPassword" diameter="20"></mat-spinner>
                    <span *ngIf="!isChangingPassword">Change Password</span>
                  </button>
                </div>
              </form>
            </div>
          </mat-tab>

          <!-- Account Information Tab -->
          <mat-tab label="Account Information">
            <div class="tab-content">
              <div class="account-info" *ngIf="currentUser">
                <div class="info-section">
                  <h3>Account Details</h3>
                  <div class="info-grid">
                    <div class="info-item">
                      <label>User ID</label>
                      <span>{{ currentUser.id }}</span>
                    </div>
                    <div class="info-item">
                      <label>Role</label>
                      <span class="role-badge">{{ currentUser.role }}</span>
                    </div>
                    <div class="info-item">
                      <label>Account Status</label>
                      <span [class]="currentUser.active ? 'status-active' : 'status-inactive'">
                        {{ currentUser.active ? 'Active' : 'Inactive' }}
                      </span>
                    </div>
                    <div class="info-item">
                      <label>Reader Credits</label>
                      <span class="credits">{{ currentUser.readerCredits }}</span>
                    </div>
                    <div class="info-item">
                      <label>Member Since</label>
                      <span>{{ currentUser.createdAt | date:'mediumDate' }}</span>
                    </div>
                    <div class="info-item">
                      <label>Last Updated</label>
                      <span>{{ currentUser.updatedAt | date:'medium' }}</span>
                    </div>
                  </div>
                </div>

                <mat-divider></mat-divider>

                <div class="info-section">
                  <h3>Account Actions</h3>
                  <div class="action-buttons">
                    <button mat-button color="primary" (click)="refreshProfile()">
                      <mat-icon>refresh</mat-icon>
                      Refresh Profile
                    </button>
                    <button mat-button color="warn" (click)="downloadData()">
                      <mat-icon>download</mat-icon>
                      Download My Data
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </mat-tab>
        </mat-tab-group>
      </mat-card>
    </div>
  `,
  styles: [`
    .profile-container {
      max-width: 800px;
      margin: 0 auto;
      padding: 2rem;
    }

    .profile-header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .profile-header h1 {
      color: #2c3e50;
      font-size: 2.5rem;
      margin-bottom: 0.5rem;
    }

    .profile-header p {
      color: #7f8c8d;
      font-size: 1.1rem;
    }

    .profile-card {
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    .tab-content {
      padding: 2rem;
    }

    .profile-form,
    .password-form {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .form-row {
      display: flex;
      gap: 1rem;
    }

    .full-width {
      width: 100%;
    }

    .half-width {
      flex: 1;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 1rem;
    }

    .account-info {
      display: flex;
      flex-direction: column;
      gap: 2rem;
    }

    .info-section h3 {
      color: #2c3e50;
      margin-bottom: 1rem;
      font-size: 1.3rem;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
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
      font-size: 1rem;
    }

    .role-badge {
      background-color: #3498db;
      color: white;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-size: 0.8rem;
      font-weight: 500;
      width: fit-content;
    }

    .status-active {
      color: #27ae60;
      font-weight: 500;
    }

    .status-inactive {
      color: #e74c3c;
      font-weight: 500;
    }

    .credits {
      color: #f39c12;
      font-weight: 600;
    }

    .action-buttons {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .action-buttons button {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    mat-spinner {
      margin-right: 0.5rem;
    }

    @media (max-width: 768px) {
      .profile-container {
        padding: 1rem;
      }

      .profile-header h1 {
        font-size: 2rem;
      }

      .tab-content {
        padding: 1rem;
      }

      .form-row {
        flex-direction: column;
      }

      .info-grid {
        grid-template-columns: 1fr;
      }

      .action-buttons {
        flex-direction: column;
      }
    }
  `]
})
export class ProfileComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  currentUser: User | null = null;
  profileForm: FormGroup;
  passwordForm: FormGroup;
  
  hideCurrentPassword = true;
  hideNewPassword = true;
  hideConfirmPassword = true;
  
  isUpdatingProfile = false;
  isChangingPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private toastr: ToastrService
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        this.currentUser = user;
        if (user) {
          this.profileForm.patchValue({
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email
          });
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');
    
    if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    return null;
  }

  updateProfile(): void {
    if (this.profileForm.valid && !this.isUpdatingProfile) {
      this.isUpdatingProfile = true;
      const profileData: UpdateProfileRequest = this.profileForm.value;

      this.userService.updateProfile(profileData).subscribe({
        next: (user) => {
          this.toastr.success('Profile updated successfully!', 'Success');
          // Update the current user in auth service
          this.authService.loadUserProfile().subscribe();
        },
        error: (error) => {
          this.isUpdatingProfile = false;
        },
        complete: () => {
          this.isUpdatingProfile = false;
        }
      });
    }
  }

  changePassword(): void {
    if (this.passwordForm.valid && !this.isChangingPassword) {
      this.isChangingPassword = true;
      const { confirmPassword, ...passwordData } = this.passwordForm.value;
      const request: ChangePasswordRequest = passwordData;

      this.userService.changePassword(request).subscribe({
        next: () => {
          this.toastr.success('Password changed successfully!', 'Success');
          this.passwordForm.reset();
        },
        error: (error) => {
          this.isChangingPassword = false;
        },
        complete: () => {
          this.isChangingPassword = false;
        }
      });
    }
  }

  refreshProfile(): void {
    this.authService.loadUserProfile().subscribe({
      next: () => {
        this.toastr.success('Profile refreshed!', 'Success');
      },
      error: (error) => {
        this.toastr.error('Failed to refresh profile', 'Error');
      }
    });
  }

  downloadData(): void {
    // Placeholder for data download functionality
    this.toastr.info('Data download feature coming soon!', 'Info');
  }
}