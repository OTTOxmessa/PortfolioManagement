package com.example.portfolio.service.impl;

import com.example.portfolio.domain.entity.User;
import com.example.portfolio.dto.response.AuthResponse;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.repository.UserRepository;
import com.example.portfolio.security.JwtUtil;
import com.example.portfolio.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Username นี้ถูกใช้ไปแล้ว");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email นี้ถูกใช้ไปแล้ว");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword)) // ไม่เก็บรหัสผ่านดิบเด็ดขาด
                .role("USER")
                .build();
        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail());
        return new AuthResponse(token, saved.getId(), saved.getUsername(), saved.getEmail());
    }

    @Override
    public AuthResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบบัญชีอีเมลนี้"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalStateException("รหัสผ่านไม่ถูกต้อง");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }
}
