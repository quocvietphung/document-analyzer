import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/users/login`, { email, password });
  }

  register(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/users/create`, data);
  }

  getUserDocuments(userId: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/documents/getUserDocuments`, {
      params: { userId }
    });
  }

  deleteDocument(documentId: string, userId: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/documents/delete`, {
      params: { documentId, userId },
      responseType: 'text'
    });
  }

  uploadDocument(data: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/documents/upload`, data);
  }
}
