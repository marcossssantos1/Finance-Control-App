package com.financeapp.dto;

public record LoginResponse(
        String token,
        String tipo
) {
    public static LoginResponse of(String token) {
        return new LoginResponse(token, "Bearer");
    }
}
