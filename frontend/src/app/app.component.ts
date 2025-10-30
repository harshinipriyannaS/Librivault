import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { FooterComponent } from './shared/components/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent, FooterComponent],
  template: `
    <div class="app-container">
      <app-navbar></app-navbar>
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
      <app-footer></app-footer>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    
    .main-content {
      flex: 1;
      padding-top: 80px; /* Account for fixed navbar height (70px + 10px buffer) */
      min-height: calc(100vh - 80px);
    }
    
    @media (max-width: 768px) {
      .main-content {
        padding-top: 70px; /* Mobile navbar height */
        min-height: calc(100vh - 70px);
      }
    }
  `]
})
export class AppComponent {
  title = 'LibriVault - Digital Library';
}