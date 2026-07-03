package br.com.unipsi.auth.dto;

public record AuthResponse(String accessToken, String refreshToken, String role, String nome) {
}
