import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  standalone: true,
  imports: []
})
export class DashboardComponent implements OnInit {
  documents: any[] = [];
  user: any;

  constructor(private apiService: ApiService, private router: Router) {}

  ngOnInit(): void {
    this.user = JSON.parse(localStorage.getItem('user') || '{}');
  }

  loadDocuments(): void {
    if (!this.user?.id) return;
    this.apiService.getUserDocuments(this.user.id).subscribe({
      next: (res) => {
        this.documents = res.documents || [];
      },
      error: (err) => {
        console.error('Failed to fetch documents:', err);
      }
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file && this.user?.id) {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('type', 'GENERAL');
      formData.append('userId', this.user.id);

      this.apiService.uploadDocument(formData).subscribe({
        next: () => this.loadDocuments(),
        error: (err) => {
          console.error('Upload failed:', err);
        }
      });
    }
  }

  deleteDocument(docId: string): void {
    this.apiService.deleteDocument(docId, this.user.id).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== docId);
      },
      error: (err) => {
        console.error('Delete failed:', err);
      }
    });
  }

  logout(): void {
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }
}
