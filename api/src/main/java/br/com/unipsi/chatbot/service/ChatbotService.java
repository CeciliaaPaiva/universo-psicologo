package br.com.unipsi.chatbot.service;

import br.com.unipsi.agenda.service.AgendaService;
import br.com.unipsi.chatbot.domain.ChatMessage;
import br.com.unipsi.chatbot.domain.RateLimitExcedidoException;
import br.com.unipsi.chatbot.domain.StatusTriagem;
import br.com.unipsi.chatbot.dto.ChatMessageRequest;
import br.com.unipsi.chatbot.dto.ChatMessageResponse;
import br.com.unipsi.chatbot.dto.ChatMessageResponse.ContatoEmergencia;
import br.com.unipsi.notificacao.service.EmailService;
import br.com.unipsi.plantao.service.PlantaoService;
import br.com.unipsi.usuario.domain.Psicologo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Orquestra uma mensagem do chatbot: rate limit -> histórico (Redis) -> Gemini -> classificação
 * de crise -> aciona plantão ou retorna contatos de emergência. Não é {@code @Transactional}
 * (o acionamento do plantão via {@link PlantaoService} gerencia sua própria transação de leitura;
 * não faz sentido segurar uma transação de banco durante a chamada HTTP ao Gemini).
 *
 * <p>Busca ampliada (ajuste 07/07/2026 — ver atas/2026-07-07-alinhamento-sprint-4.md): quando
 * ninguém está de plantão hoje, busca também o psicólogo aprovado com a próxima disponibilidade
 * mais próxima na agenda antes de cair no fallback de contatos de emergência.
 */
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private static final List<ContatoEmergencia> CONTATOS_EMERGENCIA = List.of(
            new ContatoEmergencia("CVV — conversar no chat", "https://cvv.org.br/chat/"),
            new ContatoEmergencia("CVV — ligar (188)", "tel:188"),
            new ContatoEmergencia("SAMU (192)", "tel:192"));

    private final ChatRateLimitService rateLimitService;
    private final ConversacaoStateService conversacaoStateService;
    private final GeminiClient geminiClient;
    private final CriseDetectorService criseDetectorService;
    private final PlantaoService plantaoService;
    private final AgendaService agendaService;
    private final EmailService emailService;

    public ChatMessageResponse processarMensagem(String ip, ChatMessageRequest pedido) {
        if (rateLimitService.excedeuLimite(ip)) {
            throw new RateLimitExcedidoException("Muitas mensagens em pouco tempo. Aguarde um minuto e tente novamente.");
        }

        String sessionId = (pedido.sessionId() == null || pedido.sessionId().isBlank())
                ? UUID.randomUUID().toString()
                : pedido.sessionId();

        List<ChatMessage> historico = new ArrayList<>(conversacaoStateService.obterHistorico(sessionId));
        historico.add(new ChatMessage("user", pedido.mensagem()));

        String respostaTexto = geminiClient.gerarResposta(historico);
        historico.add(new ChatMessage("model", respostaTexto));
        conversacaoStateService.salvarHistorico(sessionId, historico);

        StatusTriagem status = criseDetectorService.classificar(respostaTexto);
        if (status == StatusTriagem.CRISE) {
            return tratarCrise(sessionId, respostaTexto, pedido.contato());
        }

        return new ChatMessageResponse(sessionId, respostaTexto, false, false, List.of(), true);
    }

    private ChatMessageResponse tratarCrise(String sessionId, String respostaTexto, String contato) {
        List<Psicologo> psicologosDePlantao = plantaoService.buscarPsicologosDePlantaoHoje();
        if (!psicologosDePlantao.isEmpty()) {
            psicologosDePlantao.forEach(psicologo -> emailService.enviarAlertaPlantao(
                    psicologo.getUsuario().getEmail(), psicologo.getUsuario().getNome(), contato));
            return new ChatMessageResponse(sessionId, respostaTexto, true, true, List.of(), false);
        }

        Optional<Psicologo> proximaDisponibilidade = agendaService.buscarPsicologoComProximaDisponibilidade();
        if (proximaDisponibilidade.isPresent()) {
            Psicologo psicologo = proximaDisponibilidade.get();
            emailService.enviarAlertaProximaDisponibilidade(
                    psicologo.getUsuario().getEmail(), psicologo.getUsuario().getNome(), contato);
            return new ChatMessageResponse(sessionId, respostaTexto, true, true, List.of(), false);
        }

        return new ChatMessageResponse(sessionId, respostaTexto, true, false, CONTATOS_EMERGENCIA, false);
    }
}
