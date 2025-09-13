import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';

// Angular Material
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
  standalone: true,
  imports: [
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule
  ]
})
export class RegisterComponent {
  firstName = '';
  lastName = '';
  email = '';
  phoneNumber = '';
  password = '';

  // role mặc định
  role = 'USER';

  // terms
  termsAndConditionsAccepted = false;
  privacyPolicyAccepted = false;

  constructor(private apiService: ApiService, private router: Router) {}

  onRegister() {
    const request = {
      firstName: this.firstName,
      lastName: this.lastName,
      phoneNumber: this.phoneNumber,
      email: this.email,
      password: this.password,
      role: this.role,
      termsAndConditionsAccepted: this.termsAndConditionsAccepted,
      privacyPolicyAccepted: this.privacyPolicyAccepted
    };

    this.apiService.register(request).subscribe({
      next: () => {
        alert('Registration successful 🎉');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        alert('Registration failed: ' + (err.error?.message || err.message));
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
