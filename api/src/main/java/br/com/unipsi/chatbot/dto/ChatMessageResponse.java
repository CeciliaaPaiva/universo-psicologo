package br.com.unipsi.chatbot.dto;

import java.util.List;

public record ChatMessageResponse(
        String sessionId,
        String resposta,
        boolean crise,
        boolean profissionalAcionado,
        List<ContatoEmergencia> contatosEmergencia,
        boolean sugerirMarketplace) {

    public record ContatoEmergencia(String label, String url) {
    }
}
