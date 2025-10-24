import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { CommonModule } from '@angular/common';

// Angular Material
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

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
    MatIconModule,
    MatSidenavModule,
    MatToolbarModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatSnackBarModule
  ]
})
export class DocumentManagement implements OnInit {
  documents: any[] = [];

  // Viewer state
  viewerOpen = false;
  viewerName = '';
  viewerUrl: SafeResourceUrl | null = null;
  private viewerObjectUrl: string | null = null;
  loadingViewer = false;

  // Analyze state
  analyzeOpen = false;
  analyzing = false;
  analyzeResult: any = null;

  constructor(
    private apiService: ApiService,
    private router: Router,
    private sanitizer: DomSanitizer,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadDocuments();
  }

  // ==== Load documents ====
  loadDocuments(): void {
    this.apiService.getUserDocuments().subscribe({
      next: (res) => (this.documents = res.documents || []),
      error: (err) => {
        console.error('❌ Failed to fetch documents:', err);
        this.snack.open('Failed to load documents', 'Close', { duration: 3000 });
      }
    });
  }

  // ==== Upload document ====
  onFileSelected(event: any): void {
    const file: File = event.target.files?.[0];
    if (file) {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('type', 'GENERAL');
      // userId is now read from JWT token on backend

      this.apiService.uploadDocument(formData).subscribe({
        next: () => this.loadDocuments(),
        error: (err) => {
          console.error('❌ Upload failed:', err);
          this.snack.open('Upload failed', 'Close', { duration: 3000 });
        }
      });
    }
    event.target.value = null;
  }

  // ==== Delete document ====
  deleteDocument(docId: string): void {
    this.apiService.deleteDocument(docId).subscribe({
      next: () => (this.documents = this.documents.filter(d => d.id !== docId)),
      error: (err) => {
        console.error('❌ Delete failed:', err);
        this.snack.open('Delete failed', 'Close', { duration: 3000 });
      }
    });
  }

  // ==== Open viewer ====
  viewDocument(doc: { id: string; fileName: string }): void {
    this.loadingViewer = true;
    this.viewerName = doc.fileName || 'Document';

    this.apiService.viewDocument(doc.id).subscribe({
      next: (res) => {
        const blob = res.body as Blob;
        const url = URL.createObjectURL(blob);
        this.viewerObjectUrl = url;
        this.viewerUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
        this.viewerOpen = true;
        this.loadingViewer = false;
      },
      error: (err) => {
        console.error('❌ Open PDF failed:', err);
        this.loadingViewer = false;
        this.snack.open('Could not open PDF', 'Close', { duration: 3000 });
      }
    });
  }

  downloadCurrent(): void {
    if (!this.viewerObjectUrl) return;
    const a = document.createElement('a');
    a.href = this.viewerObjectUrl;
    a.download = this.viewerName || 'document.pdf';
    a.click();
  }

  openInNewTab(): void {
    if (!this.viewerObjectUrl) return;
    window.open(this.viewerObjectUrl, '_blank');
  }

  closeViewer(): void {
    this.viewerOpen = false;
  }

  // ==== Analyze document ====
  analyzeDocument(doc: { id: string; fileName: string }): void {
    this.analyzing = true;
    this.analyzeOpen = true;

    this.apiService.viewDocument(doc.id).subscribe({
      next: (res) => {
        const blob = res.body as Blob;
        const file = new File([blob], doc.fileName, { type: blob.type });

        this.apiService.analyzeDocument(file).subscribe({
          next: (res) => {
            console.log("✅ Raw Azure result:", res);
            this.analyzeResult = this.mapInvoiceResult(res.analyzeResult);
            this.analyzing = false;
          },
          error: (err) => {
            this.analyzing = false;
            console.error("❌ Analyze failed:", err);
            this.snack.open(err.error?.message || 'Analyze failed', 'Close', { duration: 3000 });
          }
        });
      },
      error: (err) => {
        this.analyzing = false;
        console.error('❌ Could not fetch file:', err);
        this.snack.open('Could not fetch file', 'Close', { duration: 3000 });
      }
    });
  }

  // Map Azure JSON → UI model
  private mapInvoiceResult(result: any) {
    const doc = result?.documents?.[0] || {};
    const fields = doc.fields || {};

    return {
      CustomerName: fields.CustomerName?.content || '-',
      InvoiceDate: fields.InvoiceDate?.valueDate || fields.InvoiceDate?.content || '-',
      InvoiceTotal: fields.InvoiceTotal?.valueNumber || fields.InvoiceTotal?.content || '-',
      Items: (fields.Items?.valueArray || []).map((item: any) => ({
        Description: item.valueObject?.Description?.content || '-',
        Quantity: item.valueObject?.Quantity?.valueNumber || item.valueObject?.Quantity?.content || '-',
        UnitPrice: item.valueObject?.UnitPrice?.valueNumber || item.valueObject?.UnitPrice?.content || '-',
        Amount: item.valueObject?.Amount?.valueNumber || item.valueObject?.Amount?.content || '-'
      }))
    };
  }
}
