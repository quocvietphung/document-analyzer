import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  // Chỉ dùng localStorage nếu đang chạy trong browser
  const token = (typeof window !== 'undefined')
    ? localStorage.getItem('token')
    : null;

  if (token) {
    return true;
  } else {
    router.navigate(['/login']);
    return false;
  }
};
