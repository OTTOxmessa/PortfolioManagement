package com.example.portfolio.service;

import com.example.portfolio.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(String username, String email, String rawPassword);

    AuthResponse login(String email, String rawPassword);
}
