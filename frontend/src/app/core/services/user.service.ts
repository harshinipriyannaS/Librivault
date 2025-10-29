import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { 
  User, 
  UpdateProfileRequest, 
  ChangePasswordRequest,
  UserStats
} from '../models/user.model';
import { PagedResponse } from './book.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly API_URL = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  // Profile operations
  getProfile(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/profile`);
  }

  updateProfile(profile: UpdateProfileRequest): Observable<User> {
    return this.http.put<User>(`${this.API_URL}/profile`, profile);
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/change-password`, request);
  }

  // Admin operations
  getAllUsers(page: number = 0, size: number = 20): Observable<PagedResponse<User>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<User>>(this.API_URL, { params });
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/${id}`);
  }

  updateUser(id: number, user: UpdateProfileRequest): Observable<User> {
    return this.http.put<User>(`${this.API_URL}/${id}`, user);
  }

  deactivateUser(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${id}/deactivate`, {});
  }

  activateUser(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${id}/activate`, {});
  }

  assignRole(userId: number, role: string): Observable<User> {
    return this.http.post<User>(`${this.API_URL}/${userId}/assign-role`, { role });
  }

  searchUsers(query: string, page: number = 0, size: number = 20): Observable<PagedResponse<User>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<PagedResponse<User>>(`${this.API_URL}/search`, { params });
  }

  getUserStats(): Observable<UserStats> {
    return this.http.get<UserStats>(`${this.API_URL}/stats`);
  }

  // Librarian operations
  getLibrarians(): Observable<User[]> {
    return this.http.get<User[]>(`${this.API_URL}/librarians`);
  }
}