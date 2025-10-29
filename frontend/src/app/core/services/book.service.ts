import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { 
  Book, 
  BookRequest, 
  BookSearchParams, 
  BookStats, 
  BookAccess,
  BookMetadata,
  Category,
  CategoryRequest
} from '../models/book.model';

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private readonly API_URL = `${environment.apiUrl}/books`;
  private readonly CATEGORY_URL = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) {}

  // Book operations
  searchBooks(params: BookSearchParams): Observable<PagedResponse<Book>> {
    let httpParams = new HttpParams();
    
    if (params.query) httpParams = httpParams.set('query', params.query);
    if (params.categoryId) httpParams = httpParams.set('categoryId', params.categoryId.toString());
    if (params.author) httpParams = httpParams.set('author', params.author);
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
    if (params.sortBy) httpParams = httpParams.set('sortBy', params.sortBy);
    if (params.sortDirection) httpParams = httpParams.set('sortDirection', params.sortDirection);

    return this.http.get<PagedResponse<Book>>(`${this.API_URL}/search`, { params: httpParams });
  }

  getAllBooks(page: number = 0, size: number = 20): Observable<PagedResponse<Book>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<Book>>(this.API_URL, { params });
  }

  getBookById(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.API_URL}/${id}`);
  }

  createBook(book: BookRequest): Observable<Book> {
    return this.http.post<Book>(this.API_URL, book);
  }

  updateBook(id: number, book: BookRequest): Observable<Book> {
    return this.http.put<Book>(`${this.API_URL}/${id}`, book);
  }

  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  uploadBookFile(bookId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.API_URL}/${bookId}/upload`, formData);
  }

  uploadBookCover(bookId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('cover', file);
    return this.http.post(`${this.API_URL}/${bookId}/cover`, formData);
  }

  getBookAccess(bookId: number): Observable<BookAccess> {
    return this.http.get<BookAccess>(`${this.API_URL}/${bookId}/access`);
  }

  getBookPreview(bookId: number): Observable<BookAccess> {
    return this.http.get<BookAccess>(`${this.API_URL}/${bookId}/preview`);
  }

  getBookMetadata(bookId: number): Observable<BookMetadata> {
    return this.http.get<BookMetadata>(`${this.API_URL}/${bookId}/metadata`);
  }

  getBookStats(): Observable<BookStats> {
    return this.http.get<BookStats>(`${this.API_URL}/stats`);
  }

  // Category operations
  getAllCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.CATEGORY_URL);
  }

  getCategoryById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.CATEGORY_URL}/${id}`);
  }

  createCategory(category: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.CATEGORY_URL, category);
  }

  updateCategory(id: number, category: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.CATEGORY_URL}/${id}`, category);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.CATEGORY_URL}/${id}`);
  }

  assignLibrarianToCategory(categoryId: number, librarianId: number): Observable<Category> {
    return this.http.post<Category>(`${this.CATEGORY_URL}/${categoryId}/assign/${librarianId}`, {});
  }

  getLibrarianCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.CATEGORY_URL}/my-categories`);
  }
}