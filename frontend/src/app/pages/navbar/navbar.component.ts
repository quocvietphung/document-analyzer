import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  standalone: true,
  imports: [MatButtonModule, MatIconModule, NgIf]
})
export class NavbarComponent {
  isCollapsed = false;

  constructor(private router: Router) {}

  toggleMenu() {
    this.isCollapsed = !this.isCollapsed;
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
