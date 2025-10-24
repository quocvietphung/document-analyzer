# JWT Authentication Testing Guide

This document provides instructions for testing the JWT authentication refactoring.

## Overview

The application has been refactored to use JWT authentication as the primary login mechanism while keeping Keycloak configuration for future SSO integration.

## Changes Summary

### Backend
- Removed duplicate `/api/users/login` endpoint
- Added `/api/users/me` endpoint for authenticated user info
- All document endpoints now use JWT for authentication
- Keycloak is disabled but configuration is preserved

### Frontend
- Login now uses `/api/auth/login`
- JWT tokens stored in localStorage (accessToken, refreshToken)
- Auth interceptor automatically adds Authorization header
- All document operations no longer require userId parameter

## Testing Steps

### 1. Backend Testing

#### Start the Backend
```bash
# Set environment variables (or use .env file)
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export JWT_SECRET=your-production-secret-key-must-be-at-least-32-characters-long-and-random
export JWT_EXPIRATION_MS=3600000
export JWT_REFRESH_EXPIRATION_MS=86400000

# Build and run
mvn clean package -DskipTests
mvn spring-boot:run
```

#### Test Endpoints

**1. Register a new user**
```bash
curl -X POST http://localhost:8080/api/users/create \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "Test123!", "firstName": "Test", "lastName": "User"}'
```

**2. Login with JWT**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "Test123!"}'
```

Expected response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600000
}
```

**3. Access authenticated endpoint**
```bash
# Replace YOUR_ACCESS_TOKEN with the token from step 2
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Expected: Returns user details

**4. Try accessing without token (should fail)**
```bash
curl -X GET http://localhost:8080/api/users/me
```

Expected: 401 Unauthorized

**5. Test document endpoints**
```bash
# Upload document (requires authentication)
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -F "file=@/path/to/document.pdf" \
  -F "type=GENERAL"

# Get user documents (requires authentication)
curl -X GET http://localhost:8080/api/documents/getUserDocuments \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**6. Refresh token**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN"}'
```

### 2. Frontend Testing

#### Start the Frontend
```bash
cd frontend
npm install
npm run start
```

#### Manual Testing Checklist

1. **Login Flow**
   - [ ] Navigate to http://localhost:4200/login
   - [ ] Enter credentials and click Login
   - [ ] Verify redirect to /documents
   - [ ] Open browser DevTools > Application > Local Storage
   - [ ] Verify `accessToken` and `refreshToken` are stored

2. **Document Management**
   - [ ] Upload a document (observe network tab - should include Authorization header)
   - [ ] View document list (should fetch without userId parameter)
   - [ ] Click to view a document
   - [ ] Delete a document
   - [ ] Verify all requests include `Authorization: Bearer <token>` header

3. **Auth Guard**
   - [ ] Logout (clears localStorage)
   - [ ] Try accessing /documents directly
   - [ ] Verify redirect to /login

4. **Error Handling**
   - [ ] Clear localStorage manually
   - [ ] Try to access any document endpoint
   - [ ] Verify 401 error and appropriate handling

### 3. Integration Testing

#### End-to-End Flow
1. Register new user → Success
2. Login → Receive JWT tokens
3. Upload document → Success (authenticated)
4. View documents → Success (authenticated)
5. View document → Success (authenticated)
6. Analyze document → Success (authenticated)
7. Delete document → Success (authenticated)
8. Logout → Tokens cleared
9. Try to access documents → 401 Unauthorized

### 4. Security Verification

✅ **CodeQL Scan**: 0 vulnerabilities found
✅ **Code Review**: All feedback addressed

**Security Checklist**:
- [ ] JWT secret is properly configured (32+ characters)
- [ ] Tokens stored only in localStorage (not cookies to avoid CSRF)
- [ ] Authorization header included in all authenticated requests
- [ ] Unauthenticated requests return 401
- [ ] Password is hashed with BCrypt
- [ ] JWT tokens have proper expiration times

## Expected Behavior

### Authentication Flow
1. User registers via `/api/users/create`
2. User logs in via `/api/auth/login` → receives `accessToken` and `refreshToken`
3. Frontend stores tokens in localStorage
4. Auth interceptor adds `Authorization: Bearer <token>` to all requests
5. Backend validates JWT on each request via `JwtAuthenticationFilter`
6. Backend extracts userId from JWT and uses it for authorization

### Document Operations
- All document operations (upload, view, delete, analyze) use JWT for authentication
- No need to pass userId as query parameter
- Backend extracts userId from SecurityContext
- Users can only access their own documents

### Keycloak
- Keycloak configuration is present but disabled (`keycloak.enabled=false`)
- Can be enabled later for SSO by updating configuration

## Troubleshooting

### Backend Issues
- **401 Unauthorized**: Check if JWT token is valid and not expired
- **403 Forbidden**: User may not have permission for the resource
- **500 Error**: Check server logs for details

### Frontend Issues
- **Token not stored**: Check browser console for errors
- **Requests missing token**: Verify auth interceptor is registered in app.config.ts
- **Redirect loops**: Check auth guard logic

### Database Issues
- Ensure MySQL is running and accessible
- Check database connection settings in application.yml or .env

## Next Steps

After successful testing:
1. Deploy to staging environment
2. Update production environment variables
3. Monitor authentication metrics
4. Plan Keycloak SSO integration if needed

## Notes

- All endpoints under `/api/auth/**` and `/api/users/create` are public
- All other endpoints require authentication
- JWT tokens are stateless - no session storage needed
- Refresh token can be used to get new access token without re-login
