package br.com.unipsi.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        String sessionId, @NotBlank @Size(max = 2000) String mensagem, @Size(max = 200) String contato) {
}
