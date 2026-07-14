package br.com.unipsi.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.unipsi.agenda.domain.StatusSessao;
import br.com.unipsi.agenda.repository.SessaoRepository;
import br.com.unipsi.usuario.domain.AcessoAnamneseNegadoException;
import br.com.unipsi.usuario.domain.Anamnese;
import br.com.unipsi.usuario.domain.AuditoriaAnamnese;
import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.domain.Usuario;
import br.com.unipsi.usuario.dto.AnamnesePsicologoResponse;
import br.com.unipsi.usuario.dto.AnamneseRequest;
import br.com.unipsi.usuario.dto.AnamneseResponse;
import br.com.unipsi.usuario.repository.AnamneseRepository;
import br.com.unipsi.usuario.repository.AuditoriaAnamneseRepository;
import br.com.unipsi.usuario.repository.PacienteRepository;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock
    private PsicologoRepository psicologoRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private AuditoriaAnamneseRepository auditoriaAnamneseRepository;

    private AnamneseService anamneseService;

    private UUID pacienteId;
    private UUID psicologoId;
    private Paciente pacienteAdulto;
    private Paciente pacienteMenor;
    private Psicologo psicologo;

    @BeforeEach
    void setUp() {
        anamneseService = new AnamneseService(
                anamneseRepository,
                pacienteRepository,
                psicologoRepository,
                sessaoRepository,
                auditoriaAnamneseRepository,
                new br.com.unipsi.prontuario.service.CriptografiaService(CHAVE_BASE64),
                new ObjectMapper());

        pacienteId = UUID.randomUUID();
        psicologoId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().nome("Paciente Teste").email("paciente@teste.com").build();
        pacienteAdulto = Paciente.builder().id(pacienteId).usuario(usuario).faixaRenda(FaixaRenda.FAIXA_1).idade(30).build();
        pacienteMenor = Paciente.builder().id(pacienteId).usuario(usuario).faixaRenda(FaixaRenda.FAIXA_1).idade(16).build();
        Usuario usuarioPsicologo = Usuario.builder().nome("Psi Teste").email("psi@teste.com").build();
        psicologo = Psicologo.builder().id(psicologoId).usuario(usuarioPsicologo).build();
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

    @Test
    void buscarParaPsicologo_semSessaoAgendadaEntreOsDois_deveLancarAcessoNegado() {
        when(sessaoRepository.existsByPacienteIdAndPsicologoIdAndStatus(pacienteId, psicologoId, StatusSessao.AGENDADA))
                .thenReturn(false);

        assertThatThrownBy(() -> anamneseService.buscarParaPsicologo(psicologoId, pacienteId))
                .isInstanceOf(AcessoAnamneseNegadoException.class);
    }

    @Test
    void buscarParaPsicologo_comSessaoAgendada_deveDescriptografarERegistrarAuditoria() {
        when(sessaoRepository.existsByPacienteIdAndPsicologoIdAndStatus(pacienteId, psicologoId, StatusSessao.AGENDADA))
                .thenReturn(true);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(pacienteAdulto));
        when(psicologoRepository.findById(psicologoId)).thenReturn(Optional.of(psicologo));

        var cripto = new br.com.unipsi.prontuario.service.CriptografiaService(CHAVE_BASE64);
        var cifrado = cripto.encrypt("{\"jaFezTerapia\":true,\"motivoBusca\":\"Ansiedade\","
                + "\"medicacaoControlada\":null,\"contatoResponsavel\":null}");
        Anamnese anamnese = Anamnese.builder()
                .id(UUID.randomUUID())
                .paciente(pacienteAdulto)
                .conteudoEnc(cifrado.conteudoEnc())
                .iv(cifrado.iv())
                .build();
        when(anamneseRepository.findByPacienteId(pacienteId)).thenReturn(Optional.of(anamnese));
        when(auditoriaAnamneseRepository.save(any(AuditoriaAnamnese.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnamnesePsicologoResponse resposta = anamneseService.buscarParaPsicologo(psicologoId, pacienteId);

        assertThat(resposta.preenchida()).isTrue();
        assertThat(resposta.motivoBusca()).isEqualTo("Ansiedade");
        org.mockito.Mockito.verify(auditoriaAnamneseRepository).save(any(AuditoriaAnamnese.class));
    }

    @Test
    void buscarParaPsicologo_comSessaoAgendadaMasSemAnamnesePreenchida_deveRetornarNaoPreenchidaSemAuditar() {
        when(sessaoRepository.existsByPacienteIdAndPsicologoIdAndStatus(pacienteId, psicologoId, StatusSessao.AGENDADA))
                .thenReturn(true);
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(pacienteAdulto));
        when(anamneseRepository.findByPacienteId(pacienteId)).thenReturn(Optional.empty());

        AnamnesePsicologoResponse resposta = anamneseService.buscarParaPsicologo(psicologoId, pacienteId);

        assertThat(resposta.preenchida()).isFalse();
        org.mockito.Mockito.verify(auditoriaAnamneseRepository, org.mockito.Mockito.never()).save(any());
    }
}
