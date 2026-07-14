package br.com.unipsi.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.unipsi.usuario.domain.Anamnese;
import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Usuario;
import br.com.unipsi.usuario.dto.AnamneseRequest;
import br.com.unipsi.usuario.dto.AnamneseResponse;
import br.com.unipsi.usuario.repository.AnamneseRepository;
import br.com.unipsi.usuario.repository.PacienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnamneseServiceTest {

    // Chave AES-256 fixa e válida, apenas para teste — não é usada em nenhum ambiente real.
    private static final String CHAVE_BASE64 = "L1bzEUTzGrpzHGueIAA679M43sDed+mTVqL39zGGNBY=";

    @Mock
    private AnamneseRepository anamneseRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    private AnamneseService anamneseService;

    private UUID pacienteId;
    private Paciente pacienteAdulto;
    private Paciente pacienteMenor;

    @BeforeEach
    void setUp() {
        anamneseService = new AnamneseService(
                anamneseRepository,
                pacienteRepository,
                new br.com.unipsi.prontuario.service.CriptografiaService(CHAVE_BASE64),
                new ObjectMapper());

        pacienteId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().nome("Paciente Teste").email("paciente@teste.com").build();
        pacienteAdulto = Paciente.builder().id(pacienteId).usuario(usuario).faixaRenda(FaixaRenda.FAIXA_1).idade(30).build();
        pacienteMenor = Paciente.builder().id(pacienteId).usuario(usuario).faixaRenda(FaixaRenda.FAIXA_1).idade(16).build();
    }

    @Test
    void buscar_semAnamnesePreenchida_deveRetornarNaoPreenchida() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(pacienteAdulto));
        when(anamneseRepository.findByPacienteId(pacienteId)).thenReturn(Optional.empty());

        AnamneseResponse resposta = anamneseService.buscar(pacienteId);

        assertThat(resposta.preenchida()).isFalse();
        assertThat(resposta.exigeContatoResponsavel()).isFalse();
    }

    @Test
    void salvar_pacienteAdulto_deveCifrarEPersistir() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(pacienteAdulto));
        when(anamneseRepository.findByPacienteId(pacienteId)).thenReturn(Optional.empty());
        when(anamneseRepository.save(any(Anamnese.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnamneseResponse resposta = anamneseService.salvar(
                pacienteId, new AnamneseRequest(true, "Ansiedade no trabalho", "Não", null));

        assertThat(resposta.preenchida()).isTrue();
        assertThat(resposta.jaFezTerapia()).isTrue();
        assertThat(resposta.motivoBusca()).isEqualTo("Ansiedade no trabalho");
    }

    @Test
    void salvar_pacienteMenorDeIdadeSemContatoResponsavel_deveLancarExcecao() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(pacienteMenor));

        assertThatThrownBy(() -> anamneseService.salvar(
                        pacienteId, new AnamneseRequest(false, "Ansiedade escolar", "Não", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void salvar_pacienteMenorDeIdadeComContatoResponsavel_devePersistir() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(pacienteMenor));
        when(anamneseRepository.findByPacienteId(pacienteId)).thenReturn(Optional.empty());
        when(anamneseRepository.save(any(Anamnese.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnamneseResponse resposta = anamneseService.salvar(
                pacienteId,
                new AnamneseRequest(false, "Ansiedade escolar", "Não", "responsavel@teste.com"));

        assertThat(resposta.contatoResponsavel()).isEqualTo("responsavel@teste.com");
        assertThat(resposta.exigeContatoResponsavel()).isTrue();
    }

    @Test
    void salvar_anamneseJaExistente_deveAtualizarConteudo() {
        Anamnese existente = Anamnese.builder()
                .id(UUID.randomUUID())
                .paciente(pacienteAdulto)
                .conteudoEnc("antigo")
                .iv("antigo")
                .build();
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(pacienteAdulto));
        when(anamneseRepository.findByPacienteId(pacienteId)).thenReturn(Optional.of(existente));
        when(anamneseRepository.save(any(Anamnese.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnamneseResponse resposta = anamneseService.salvar(
                pacienteId, new AnamneseRequest(true, "Motivo atualizado", null, null));

        assertThat(resposta.motivoBusca()).isEqualTo("Motivo atualizado");
    }
}
