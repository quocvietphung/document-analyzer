import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Skip adding token for login and register endpoints
  if (req.url.includes('/api/auth/login') || req.url.includes('/api/users/create')) {
    return next(req);
  }

  // Get token from localStorage (browser only)
  if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
    const token = localStorage.getItem('accessToken');
    
    if (token) {
      // Clone the request and add Authorization header
      const clonedReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      return next(clonedReq);
    }
  }

  return next(req);
};
