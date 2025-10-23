import { Injectable } from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/login`, { email, password });
  }

  register(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/users/create`, data);
  }

  getUserDocuments(): Observable<any> {
    return this.http.get(`${this.baseUrl}/documents/getUserDocuments`);
  }

  deleteDocument(documentId: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/documents/${documentId}`, {
      responseType: 'text'
    });
  }

  uploadDocument(data: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/documents/upload`, data);
  }

  viewDocument(documentId: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}/documents/${documentId}/view`, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  analyzeDocument(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any>(`${this.baseUrl}/documents/analyze`, formData);
  }
}
