import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container mt-4">
      <h1>About LibriVault</h1>
      <p>About page component - Coming soon!</p>
    </div>
  `
})
export class AboutComponent {}