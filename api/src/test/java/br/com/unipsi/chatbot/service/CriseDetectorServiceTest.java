package br.com.unipsi.chatbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.unipsi.chatbot.domain.StatusTriagem;
import org.junit.jupiter.api.Test;

class CriseDetectorServiceTest {

    private final CriseDetectorService criseDetectorService = new CriseDetectorService();

    @Test
    void classificar_respostaComIndicativoDeAutolesao_deveRetornarCRISE() {
        StatusTriagem status = criseDetectorService.classificar("Tenho pensado em me cortar quando fico assim.");

        assertThat(status).isEqualTo(StatusTriagem.CRISE);
    }

    @Test
    void classificar_respostaComPanicoSevero_deveRetornarCRISE() {
        StatusTriagem status = criseDetectorService.classificar("Estou tendo uma crise de pânico agora, o coração dispara.");

        assertThat(status).isEqualTo(StatusTriagem.CRISE);
    }

    @Test
    void classificar_respostaComAnsiedadeAlta_deveRetornarCRISE() {
        StatusTriagem status = criseDetectorService.classificar("Sinto uma ansiedade severa desde ontem.");

        assertThat(status).isEqualTo(StatusTriagem.CRISE);
    }

    @Test
    void classificar_respostaComPalavraAcentuada_deveNormalizarEDetectarCrise() {
        StatusTriagem status = criseDetectorService.classificar("Penso em suicídio quase todos os dias.");

        assertThat(status).isEqualTo(StatusTriagem.CRISE);
    }

    @Test
    void classificar_respostaComDesabafoComum_deveRetornarNORMAL() {
        StatusTriagem status = criseDetectorService.classificar("Tive uma semana puxada no trabalho e estou cansado.");

        assertThat(status).isEqualTo(StatusTriagem.NORMAL);
    }

    @Test
    void classificar_respostaVazia_deveRetornarNORMAL() {
        StatusTriagem status = criseDetectorService.classificar("   ");

        assertThat(status).isEqualTo(StatusTriagem.NORMAL);
    }

    @Test
    void classificar_respostaNula_deveLancarException() {
        assertThatThrownBy(() -> criseDetectorService.classificar(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
