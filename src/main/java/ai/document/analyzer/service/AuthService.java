package ai.document.analyzer.service;

import ai.document.analyzer.dto.JwtResponseDto;
import ai.document.analyzer.dto.LoginRequestDto;
import ai.document.analyzer.model.User;
import ai.document.analyzer.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticate user and return JWT tokens
     */
    public JwtResponseDto login(LoginRequestDto loginRequest) {
        // Find user by email
        User user = userService.findUserByEmail(loginRequest.getEmail());
        
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Check if user is active
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("User account is not active");
        }

        // Generate tokens
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        return JwtResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600000L) // 1 hour in milliseconds
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    public JwtResponseDto refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String userId = jwtUtil.extractUserId(refreshToken);
        String email = jwtUtil.extractEmail(refreshToken);

        User user = userService.findUserById(userId);
        if (user == null || !user.getIsActive()) {
            throw new IllegalArgumentException("User not found or inactive");
        }

        String newAccessToken = jwtUtil.generateToken(userId, email);

        return JwtResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Return the same refresh token
                .expiresIn(3600000L)
                .build();
    }
}
