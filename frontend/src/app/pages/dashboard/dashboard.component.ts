import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  imports: [MatCardModule, MatButtonModule]
})
export class DashboardComponent {
  constructor(private router: Router) {}

  goToDocuments() {
    this.router.navigate(['/documents']);
  }

  uploadDocument() {
    this.router.navigate(['/upload']);
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
