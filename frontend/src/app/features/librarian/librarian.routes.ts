import { Routes } from '@angular/router';

export const librarianRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./librarian-dashboard/librarian-dashboard.component').then(m => m.LibrarianDashboardComponent)
  }
];