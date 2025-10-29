import { Routes } from '@angular/router';

export const dashboardRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'credits',
    loadComponent: () => import('./credit-balance/credit-balance.component').then(m => m.CreditBalanceComponent)
  },
  {
    path: 'my-borrows',
    loadComponent: () => import('./my-borrows/my-borrows.component').then(m => m.MyBorrowsComponent)
  }
];