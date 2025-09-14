import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  standalone: true,
  imports: [MatButtonModule, MatIconModule]
})
export class NavbarComponent {
  constructor(private router: Router) {}

  toggleMenu() {
    console.log('Menu clicked (sau này mở sidebar)');
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
