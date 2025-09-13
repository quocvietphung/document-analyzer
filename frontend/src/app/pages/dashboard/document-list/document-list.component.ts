import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../../services/api.service';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-document-list',
  standalone: true,
  templateUrl: './document-list.component.html',
  styleUrls: ['./document-list.component.scss'],
  imports: [MatCardModule, MatListModule, MatButtonModule, MatIconModule]
})
export class DocumentListComponent implements OnInit {
  documents: any[] = [];

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (user?.id) {
      this.apiService.getUserDocuments(user.id).subscribe({
        next: (res: any) => {
          this.documents = res.documents || [];
        },
        error: (err) => {
          console.error('Failed to fetch documents:', err);
        }
      });
    }
  }

  viewDocument(id: string) {
    window.open(`/api/documents/view?documentId=${id}&userId=${this.getUserId()}`, '_blank');
  }

  deleteDocument(id: string) {
    this.apiService.deleteDocument(id, this.getUserId()).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== id);
      },
      error: (err) => {
        console.error('Delete failed:', err);
      }
    });
  }

  private getUserId(): string {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user?.id || '';
  }
}
