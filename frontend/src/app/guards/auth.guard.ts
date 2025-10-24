import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  // Check for JWT access token in browser localStorage
  const token = (typeof window !== 'undefined')
    ? localStorage.getItem('accessToken')
    : null;

  if (token) {
    return true;
  } else {
    router.navigate(['/login']);
    return false;
  }
};
