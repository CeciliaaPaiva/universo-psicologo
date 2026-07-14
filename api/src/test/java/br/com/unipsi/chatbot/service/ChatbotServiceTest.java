package br.com.unipsi.chatbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.unipsi.agenda.service.AgendaService;
import br.com.unipsi.chatbot.domain.ChatMessage;
import br.com.unipsi.chatbot.domain.RateLimitExcedidoException;
import br.com.unipsi.chatbot.domain.StatusTriagem;
import br.com.unipsi.chatbot.dto.ChatMessageRequest;
import br.com.unipsi.chatbot.dto.ChatMessageResponse;
import br.com.unipsi.notificacao.service.EmailService;
import br.com.unipsi.plantao.service.PlantaoService;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private ChatRateLimitService rateLimitService;

    @Mock
    private ConversacaoStateService conversacaoStateService;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private CriseDetectorService criseDetectorService;

    @Mock
    private PlantaoService plantaoService;

    @Mock
    private AgendaService agendaService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ChatbotService chatbotService;

    @Test
    void processarMensagem_statusNormal_deveRetornarRespostaSemAcionarPlantao() {
        when(rateLimitService.excedeuLimite("1.2.3.4")).thenReturn(false);
        when(conversacaoStateService.obterHistorico(anyString())).thenReturn(List.of());
        when(geminiClient.gerarResposta(anyList())).thenReturn("Como você está se sentindo hoje?");
        when(criseDetectorService.classificar("Como você está se sentindo hoje?")).thenReturn(StatusTriagem.NORMAL);

        ChatMessageResponse resposta =
                chatbotService.processarMensagem("1.2.3.4", new ChatMessageRequest(null, "Oi", null));

        assertThat(resposta.crise()).isFalse();
        assertThat(resposta.profissionalAcionado()).isFalse();
        assertThat(resposta.contatosEmergencia()).isEmpty();
        assertThat(resposta.sugerirMarketplace()).isTrue();
        verifyNoInteractions(plantaoService, agendaService, emailService);
    }

    @Test
    void processarMensagem_statusCrise_comPlantaoAtivo_deveNotificarTodosOsPsicologosDePlantao() {
        Psicologo psicologo1 = psicologoComEmail("psi1@teste.com", "Psi Um");
        Psicologo psicologo2 = psicologoComEmail("psi2@teste.com", "Psi Dois");

        when(rateLimitService.excedeuLimite(anyString())).thenReturn(false);
        when(conversacaoStateService.obterHistorico(anyString())).thenReturn(List.of());
        when(geminiClient.gerarResposta(anyList())).thenReturn("resposta de crise");
        when(criseDetectorService.classificar("resposta de crise")).thenReturn(StatusTriagem.CRISE);
        when(plantaoService.buscarPsicologosDePlantaoHoje()).thenReturn(List.of(psicologo1, psicologo2));

        ChatMessageResponse resposta = chatbotService.processarMensagem(
                "1.2.3.4", new ChatMessageRequest(null, "não aguento mais", "11999990000"));

        assertThat(resposta.crise()).isTrue();
        assertThat(resposta.profissionalAcionado()).isTrue();
        assertThat(resposta.contatosEmergencia()).isEmpty();
        verify(emailService).enviarAlertaPlantao("psi1@teste.com", "Psi Um", "11999990000");
        verify(emailService).enviarAlertaPlantao("psi2@teste.com", "Psi Dois", "11999990000");
        verify(emailService, times(2)).enviarAlertaPlantao(anyString(), anyString(), any());
        verifyNoInteractions(agendaService);
    }

    @Test
    void processarMensagem_criseSemPlantaoMasComProximaDisponibilidade_deveNotificarEsseProfissional() {
        Psicologo proximo = psicologoComEmail("psi3@teste.com", "Psi Três");

        when(rateLimitService.excedeuLimite(anyString())).thenReturn(false);
        when(conversacaoStateService.obterHistorico(anyString())).thenReturn(List.of());
        when(geminiClient.gerarResposta(anyList())).thenReturn("resposta de crise");
        when(criseDetectorService.classificar("resposta de crise")).thenReturn(StatusTriagem.CRISE);
        when(plantaoService.buscarPsicologosDePlantaoHoje()).thenReturn(List.of());
        when(agendaService.buscarPsicologoComProximaDisponibilidade()).thenReturn(Optional.of(proximo));

        ChatMessageResponse resposta = chatbotService.processarMensagem(
                "1.2.3.4", new ChatMessageRequest(null, "não aguento mais", "11999990000"));

        assertThat(resposta.crise()).isTrue();
        assertThat(resposta.profissionalAcionado()).isTrue();
        assertThat(resposta.contatosEmergencia()).isEmpty();
        verify(emailService).enviarAlertaProximaDisponibilidade("psi3@teste.com", "Psi Três", "11999990000");
        verify(emailService, never()).enviarAlertaPlantao(anyString(), anyString(), any());
    }

    @Test
    void processarMensagem_criseSemPlantaoENemDisponibilidade_deveRetornarContatosEmergencia() {
        when(rateLimitService.excedeuLimite(anyString())).thenReturn(false);
        when(conversacaoStateService.obterHistorico(anyString())).thenReturn(List.of());
        when(geminiClient.gerarResposta(anyList())).thenReturn("resposta de crise");
        when(criseDetectorService.classificar("resposta de crise")).thenReturn(StatusTriagem.CRISE);
        when(plantaoService.buscarPsicologosDePlantaoHoje()).thenReturn(List.of());
        when(agendaService.buscarPsicologoComProximaDisponibilidade()).thenReturn(Optional.empty());

        ChatMessageResponse resposta =
                chatbotService.processarMensagem("1.2.3.4", new ChatMessageRequest(null, "socorro", null));

        assertThat(resposta.crise()).isTrue();
        assertThat(resposta.profissionalAcionado()).isFalse();
        assertThat(resposta.contatosEmergencia()).extracting("label")
                .containsExactly("CVV — conversar no chat", "CVV — ligar (188)", "SAMU (192)");
        assertThat(resposta.contatosEmergencia()).extracting("url")
                .containsExactly("https://cvv.org.br/chat/", "tel:188", "tel:192");
        verifyNoInteractions(emailService);
    }

    @Test
    void processarMensagem_deveSalvarHistoricoComMensagemDoUsuarioEDaResposta() {
        when(rateLimitService.excedeuLimite(anyString())).thenReturn(false);
        when(conversacaoStateService.obterHistorico(anyString())).thenReturn(List.of(new ChatMessage("user", "oi")));
        when(geminiClient.gerarResposta(anyList())).thenReturn("tudo bem?");
        when(criseDetectorService.classificar("tudo bem?")).thenReturn(StatusTriagem.NORMAL);

        chatbotService.processarMensagem("1.2.3.4", new ChatMessageRequest("sessao-1", "como vai?", null));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChatMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(conversacaoStateService).salvarHistorico(eq("sessao-1"), captor.capture());
        assertThat(captor.getValue()).containsExactly(
                new ChatMessage("user", "oi"), new ChatMessage("user", "como vai?"), new ChatMessage("model", "tudo bem?"));
    }

    @Test
    void processarMensagem_semSessionIdInformado_deveGerarNovoSessionId() {
        when(rateLimitService.excedeuLimite(anyString())).thenReturn(false);
        when(conversacaoStateService.obterHistorico(anyString())).thenReturn(List.of());
        when(geminiClient.gerarResposta(anyList())).thenReturn("olá!");
        when(criseDetectorService.classificar("olá!")).thenReturn(StatusTriagem.NORMAL);

        ChatMessageResponse resposta =
                chatbotService.processarMensagem("1.2.3.4", new ChatMessageRequest(null, "oi", null));

        assertThat(resposta.sessionId()).isNotBlank();
    }

    @Test
    void processarMensagem_rateLimitExcedido_deveLancarExceptionSemProcessarMensagem() {
        when(rateLimitService.excedeuLimite("1.2.3.4")).thenReturn(true);

        assertThatThrownBy(() ->
                        chatbotService.processarMensagem("1.2.3.4", new ChatMessageRequest(null, "oi", null)))
                .isInstanceOf(RateLimitExcedidoException.class);
        verifyNoInteractions(
                conversacaoStateService, geminiClient, criseDetectorService, plantaoService, agendaService, emailService);
    }

    private Psicologo psicologoComEmail(String email, String nome) {
        Usuario usuario = Usuario.builder().nome(nome).email(email).build();
        return Psicologo.builder().usuario(usuario).build();
    }
}
