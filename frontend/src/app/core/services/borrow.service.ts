import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { 
  BorrowRequest, 
  BorrowRecord, 
  Fine, 
  BorrowStats,
  CreateBorrowRequestRequest,
  ReviewBorrowRequestRequest,
  WaiveFineRequest
} from '../models/borrow.model';
import { PagedResponse } from './book.service';

@Injectable({
  providedIn: 'root'
})
export class BorrowService {
  private readonly API_URL = `${environment.apiUrl}/borrowing`;

  constructor(private http: HttpClient) {}

  // Borrow Request operations
  createBorrowRequest(request: CreateBorrowRequestRequest): Observable<BorrowRequest> {
    return this.http.post<BorrowRequest>(`${this.API_URL}/requests`, request);
  }

  getMyBorrowRequests(page: number = 0, size: number = 20): Observable<PagedResponse<BorrowRequest>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<BorrowRequest>>(`${this.API_URL}/requests/my`, { params });
  }

  getPendingBorrowRequests(page: number = 0, size: number = 20): Observable<PagedResponse<BorrowRequest>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<BorrowRequest>>(`${this.API_URL}/requests/pending`, { params });
  }

  getAllBorrowRequests(page: number = 0, size: number = 20): Observable<PagedResponse<BorrowRequest>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<BorrowRequest>>(`${this.API_URL}/requests`, { params });
  }

  approveBorrowRequest(requestId: number, review: ReviewBorrowRequestRequest): Observable<BorrowRequest> {
    return this.http.post<BorrowRequest>(`${this.API_URL}/requests/${requestId}/approve`, review);
  }

  declineBorrowRequest(requestId: number, review: ReviewBorrowRequestRequest): Observable<BorrowRequest> {
    return this.http.post<BorrowRequest>(`${this.API_URL}/requests/${requestId}/decline`, review);
  }

  // Borrow Record operations
  getMyBorrowRecords(page: number = 0, size: number = 20): Observable<PagedResponse<BorrowRecord>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<BorrowRecord>>(`${this.API_URL}/records/my`, { params });
  }

  getAllBorrowRecords(page: number = 0, size: number = 20): Observable<PagedResponse<BorrowRecord>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<BorrowRecord>>(`${this.API_URL}/records`, { params });
  }

  getOverdueBorrowRecords(page: number = 0, size: number = 20): Observable<PagedResponse<BorrowRecord>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<BorrowRecord>>(`${this.API_URL}/records/overdue`, { params });
  }

  returnBook(recordId: number): Observable<BorrowRecord> {
    return this.http.post<BorrowRecord>(`${this.API_URL}/records/${recordId}/return`, {});
  }

  // Fine operations
  getMyFines(page: number = 0, size: number = 20): Observable<PagedResponse<Fine>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<Fine>>(`${this.API_URL}/fines/my`, { params });
  }

  getAllFines(page: number = 0, size: number = 20): Observable<PagedResponse<Fine>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<Fine>>(`${this.API_URL}/fines`, { params });
  }

  payFine(fineId: number): Observable<Fine> {
    return this.http.post<Fine>(`${this.API_URL}/fines/${fineId}/pay`, {});
  }

  waiveFine(fineId: number, request: WaiveFineRequest): Observable<Fine> {
    return this.http.post<Fine>(`${this.API_URL}/fines/${fineId}/waive`, request);
  }

  // Statistics
  getBorrowStats(): Observable<BorrowStats> {
    return this.http.get<BorrowStats>(`${this.API_URL}/stats`);
  }
}