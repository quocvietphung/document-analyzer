import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { CommonModule } from '@angular/common';

// Angular Material
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-document-management',
  templateUrl: './document-management.html',
  styleUrls: ['./document-management.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatListModule,
    MatIconModule
  ]
})
export class DocumentManagement implements OnInit {
  documents: any[] = [];
  userId: string | null = null;

  constructor(
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (typeof window !== 'undefined') {
      this.userId = localStorage.getItem('userId');
      console.log('📌 Loaded userId from localStorage:', this.userId);
    }

    if (this.userId) {
      this.loadDocuments();
    }
  }

  loadDocuments(): void {
    if (!this.userId) return;

    console.log('📥 Fetching documents for userId:', this.userId);
    this.apiService.getUserDocuments(this.userId).subscribe({
      next: (res) => {
        console.log('✅ Documents loaded:', res);
        this.documents = res.documents || [];
      },
      error: (err) => {
        console.error('❌ Failed to fetch documents:', err);
      }
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file && this.userId) {
      console.log('⬆️ Uploading file for userId:', this.userId);

      const formData = new FormData();
      formData.append('file', file);
      formData.append('type', 'GENERAL');
      formData.append('userId', this.userId);

      this.apiService.uploadDocument(formData).subscribe({
        next: () => {
          console.log('✅ Upload successful');
          this.loadDocuments();
        },
        error: (err) => {
          console.error('❌ Upload failed:', err);
        }
      });
    }
  }

  deleteDocument(docId: string): void {
    if (!this.userId) return;

    console.log('🗑️ Deleting document:', docId, 'for userId:', this.userId);
    this.apiService.deleteDocument(docId, this.userId).subscribe({
      next: () => {
        console.log('✅ Document deleted:', docId);
        this.documents = this.documents.filter(d => d.id !== docId);
      },
      error: (err) => {
        console.error('❌ Delete failed:', err);
      }
    });
  }

  logout(): void {
    console.log('🚪 Logging out, clearing localStorage');
    if (typeof window !== 'undefined') {
      localStorage.clear();
    }
    this.router.navigate(['/login']);
  }
}
