package br.com.unipsi.chatbot.domain;

/**
 * {@code role} segue o vocabulário da API do Gemini: "user" ou "model".
 */
public record ChatMessage(String role, String conteudo) {
}
