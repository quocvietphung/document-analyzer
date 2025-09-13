import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

// Angular Material
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatListModule, MatIconModule]
})
export class DashboardComponent implements OnInit {
  documents: any[] = [];
  userId: string | null = null;

  constructor(private apiService: ApiService, private router: Router) {}

  ngOnInit(): void {
    this.userId = localStorage.getItem('userId'); // ✅ dùng userId đã lưu riêng
    if (this.userId) {
      this.loadDocuments();
    }
  }

  loadDocuments(): void {
    if (!this.userId) return;
    this.apiService.getUserDocuments(this.userId).subscribe({
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
    if (file && this.userId) {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('type', 'GENERAL');
      formData.append('userId', this.userId);

      this.apiService.uploadDocument(formData).subscribe({
        next: () => this.loadDocuments(),
        error: (err) => console.error('Upload failed:', err)
      });
    }
  }

  deleteDocument(docId: string): void {
    if (!this.userId) return;
    this.apiService.deleteDocument(docId, this.userId).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== docId);
      },
      error: (err) => console.error('Delete failed:', err)
    });
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
