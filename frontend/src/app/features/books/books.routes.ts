import { Routes } from '@angular/router';

export const booksRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./book-list/book-list.component').then(m => m.BookListComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./book-detail/book-detail.component').then(m => m.BookDetailComponent)
  }
];