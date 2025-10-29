import { Routes } from '@angular/router';

export const subscriptionRoutes: Routes = [
  {
    path: '',
    redirectTo: 'plans',
    pathMatch: 'full'
  },
  {
    path: 'plans',
    loadComponent: () => import('./subscription-plans/subscription-plans.component').then(m => m.SubscriptionPlansComponent)
  },
  {
    path: 'upgrade',
    loadComponent: () => import('./subscription-upgrade/subscription-upgrade.component').then(m => m.SubscriptionUpgradeComponent)
  },
  {
    path: 'history',
    loadComponent: () => import('./subscription-history/subscription-history.component').then(m => m.SubscriptionHistoryComponent)
  },
  {
    path: 'payment-history',
    loadComponent: () => import('./payment-history/payment-history.component').then(m => m.PaymentHistoryComponent)
  }
];