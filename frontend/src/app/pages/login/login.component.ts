import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email: string = '';
  password: string = '';

  constructor(private apiService: ApiService, private router: Router) {}

  onLogin() {
    this.apiService.login(this.email, this.password).subscribe({
      next: (res: any) => {
        // lưu user info/token vào localStorage
        localStorage.setItem('user', JSON.stringify(res.user));
        localStorage.setItem('token', 'fake-token'); // sau này thay JWT
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        alert('Login failed: ' + err.error.message);
      }
    });
  }
}
