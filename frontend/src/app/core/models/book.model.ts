export interface Book {
  id: number;
  title: string;
  author: string;
  isbn: string;
  description: string;
  categoryId: number;
  categoryName: string;
  totalCopies: number;
  availableCopies: number;
  publishedDate: string;
  coverImageUrl?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BookRequest {
  title: string;
  author: string;
  isbn: string;
  description: string;
  categoryId: number;
  totalCopies: number;
  publishedDate: string;
}

export interface Category {
  id: number;
  name: string;
  description: string;
  active: boolean;
  createdAt: string;
  librarianName?: string;
  librarianId?: number;
  totalBooks: number;
  availableBooks: number;
}

export interface CategoryRequest {
  name: string;
  description: string;
}

export interface BookSearchParams {
  query?: string;
  categoryId?: number;
  author?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface BookStats {
  totalBooks: number;
  availableBooks: number;
  borrowedBooks: number;
  overdueBooks: number;
  mostPopularBooks: PopularBook[];
}

export interface PopularBook {
  bookId: number;
  title: string;
  author: string;
  borrowCount: number;
}

export interface BookAccess {
  bookId: number;
  secureUrl: string;
  expiresIn: string;
  message: string;
}

export interface BookMetadata {
  bookId: number;
  title: string;
  author: string;
  fileExists: boolean;
  fileSize: number;
  formattedFileSize: string;
  hasPreview: boolean;
  hasCover: boolean;
}