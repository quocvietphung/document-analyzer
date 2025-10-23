package ai.document.analyzer.service;

import ai.document.analyzer.config.JwtConfig;
import ai.document.analyzer.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private JwtConfig jwtConfig;

    private JwtUtil jwtUtil;

    private final String testUserId = "test-user-123";
    private final String testEmail = "test@example.com";
    private final String testSecret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256";

    @BeforeEach
    void setUp() {
        lenient().when(jwtConfig.getSecret()).thenReturn(testSecret);
        lenient().when(jwtConfig.getExpiration()).thenReturn(3600000L); // 1 hour
        lenient().when(jwtConfig.getRefreshExpiration()).thenReturn(86400000L); // 24 hours
        
        jwtUtil = new JwtUtil(jwtConfig);
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken(testUserId, testEmail);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUserId() {
        String token = jwtUtil.generateToken(testUserId, testEmail);
        String extractedUserId = jwtUtil.extractUserId(token);
        
        assertEquals(testUserId, extractedUserId);
    }

    @Test
    void testExtractEmail() {
        String token = jwtUtil.generateToken(testUserId, testEmail);
        String extractedEmail = jwtUtil.extractEmail(token);
        
        assertEquals(testEmail, extractedEmail);
    }

    @Test
    void testValidateToken() {
        String token = jwtUtil.generateToken(testUserId, testEmail);
        
        assertTrue(jwtUtil.validateToken(token, testUserId));
    }

    @Test
    void testValidateTokenWithWrongUserId() {
        String token = jwtUtil.generateToken(testUserId, testEmail);
        
        assertFalse(jwtUtil.validateToken(token, "wrong-user-id"));
    }

    @Test
    void testGenerateRefreshToken() {
        String refreshToken = jwtUtil.generateRefreshToken(testUserId, testEmail);
        
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        
        String extractedUserId = jwtUtil.extractUserId(refreshToken);
        assertEquals(testUserId, extractedUserId);
    }

    @Test
    void testValidateTokenGeneric() {
        String token = jwtUtil.generateToken(testUserId, testEmail);
        
        assertTrue(jwtUtil.validateToken(token));
    }
}
