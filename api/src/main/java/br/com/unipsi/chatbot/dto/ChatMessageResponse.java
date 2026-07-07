package br.com.unipsi.chatbot.dto;

import java.util.List;

public record ChatMessageResponse(
        String sessionId,
        String resposta,
        boolean crise,
        boolean plantaoAcionado,
        List<String> contatosEmergencia,
        boolean sugerirMarketplace) {
}
