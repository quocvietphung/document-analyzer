import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { DocumentManagement } from './pages/document-management/document-management';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'documents', component: DocumentManagement, canActivate: [authGuard] },
  { path: '', redirectTo: 'login', pathMatch: 'full' }
];
