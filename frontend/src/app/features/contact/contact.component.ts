import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container mt-4">
      <h1>Contact Us</h1>
      <p>Contact page component - Coming soon!</p>
    </div>
  `
})
export class ContactComponent {}