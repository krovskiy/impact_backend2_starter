package com.impact.ecommerce.services;

import com.impact.ecommerce.dtos.auth.*;
import com.impact.ecommerce.entities.*;
import com.impact.ecommerce.repositories.UserRepository;
import com.impact.ecommerce.repositories.RefreshTokenRepository;
import com.impact.ecommerce.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokens;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenRepository refreshTokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokens = refreshTokens;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must fit in 72 UTF-8 bytes");
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hashPassword(request.password()));
        user.setRole(Role.USER); // Never accept a role from public registration.
        users.saveAndFlush(user);
        return response(user); // 201 plus a token: required by the existing frontend's register form.
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(this::badCredentials);
        if (!passwordMatches(request.password(), user.getPasswordHash())) throw badCredentials();
        return response(user);
    }

    // Lesson 4 L4-1: the service looks up the current account; the controller only routes HTTP.
    @Transactional(readOnly = true)
    public MeResponse me(String email) {
        User user = users.findByEmail(email).orElseThrow(this::badCredentials);
        return new MeResponse(user.getEmail(), user.getRole().name());
    }

    private String hashPassword(String rawPassword) {
        // Answer Lesson 2 L2-3: return the BCrypt hash using passwordEncoder.
        // Never return rawPassword.
        return passwordEncoder.encode(rawPassword);
    }

    private boolean passwordMatches(String rawPassword, String storedHash) {
        // Answer Lesson 2 L2-4: compare rawPassword with storedHash using passwordEncoder.
        // Do not hash again and compare strings: BCrypt uses a random salt.
        return passwordEncoder.matches(rawPassword, storedHash);
    }

    private AuthResponse response(User user) {
        RefreshToken refresh = new RefreshToken(UUID.randomUUID().toString(), user, Instant.now().plusSeconds(30L * 24 * 60 * 60));
        refreshTokens.save(refresh);
        return new AuthResponse(jwtService.generateToken(user), user.getId(), user.getEmail(), user.getRole().name(), refresh.getToken());
    }

    // Lesson 11: lock the row so concurrent requests cannot both rotate the same token.
    @Transactional
    public AuthResponse refresh(String token) {
        RefreshToken existing = refreshTokens.findLockedByToken(token).orElseThrow(this::invalidRefresh);
        if (existing.isRevoked() || !existing.getExpiresAt().isAfter(Instant.now())) throw invalidRefresh();
        existing.revoke();
        return response(existing.getUser());
    }

    @Transactional
    public void logout(String token) {
        refreshTokens.findLockedByToken(token).ifPresent(RefreshToken::revoke);
    }

    private ResponseStatusException invalidRefresh() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
    }

    private ResponseStatusException badCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
