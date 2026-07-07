package br.com.unipsi.chatbot.controller;

import br.com.unipsi.chatbot.dto.ChatMessageRequest;
import br.com.unipsi.chatbot.dto.ChatMessageResponse;
import br.com.unipsi.chatbot.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint público — sem autenticação. Ver {@code SecurityConfig} (permitAll para /api/chatbot/**).
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/message")
    public ChatMessageResponse enviarMensagem(HttpServletRequest request, @RequestBody @Valid ChatMessageRequest pedido) {
        return chatbotService.processarMensagem(extrairIp(request), pedido);
    }

    private String extrairIp(HttpServletRequest request) {
        String encaminhadoPor = request.getHeader("X-Forwarded-For");
        if (encaminhadoPor != null && !encaminhadoPor.isBlank()) {
            return encaminhadoPor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
