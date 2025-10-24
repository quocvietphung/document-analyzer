import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: true,
  imports: [
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ]
})
export class LoginComponent {
  email: string = '';
  password: string = '';

  constructor(private apiService: ApiService, private router: Router) {}

  onLogin() {
    this.apiService.login(this.email, this.password).subscribe({
      next: (res: any) => {
        // Store JWT tokens from response
        localStorage.setItem('accessToken', res.accessToken);
        localStorage.setItem('refreshToken', res.refreshToken);
        // Note: userId is now extracted from JWT on the backend
        // We can decode the token on frontend if needed, but not storing separately

        this.router.navigate(['/documents']);
      },
      error: (err) => {
        alert('Login failed: ' + (err.error?.message || err.message));
      }
    });
  }

  goToRegister() {
    this.router.navigate(['/register']);
  }
}
