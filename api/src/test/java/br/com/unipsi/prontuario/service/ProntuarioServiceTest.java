package br.com.unipsi.prontuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.unipsi.prontuario.domain.AcaoAuditoria;
import br.com.unipsi.prontuario.domain.AcessoProntuarioNegadoException;
import br.com.unipsi.prontuario.domain.Anotacao;
import br.com.unipsi.prontuario.domain.CodinomeJaCadastradoException;
import br.com.unipsi.prontuario.domain.Prontuario;
import br.com.unipsi.prontuario.dto.AnotacaoResponse;
import br.com.unipsi.prontuario.dto.CriarAnotacaoRequest;
import br.com.unipsi.prontuario.dto.CriarProntuarioRequest;
import br.com.unipsi.prontuario.dto.ProntuarioResponse;
import br.com.unipsi.prontuario.repository.AnotacaoRepository;
import br.com.unipsi.prontuario.repository.ProntuarioRepository;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProntuarioServiceTest {

    @Mock
    private ProntuarioRepository prontuarioRepository;

    @Mock
    private AnotacaoRepository anotacaoRepository;

    @Mock
    private PsicologoRepository psicologoRepository;

    @Mock
    private CriptografiaService criptografiaService;

    @Mock
    private AuditoriaProntuarioService auditoriaProntuarioService;

    @InjectMocks
    private ProntuarioService prontuarioService;

    private UUID psicologoId;
    private Psicologo psicologo;

    @BeforeEach
    void setUp() {
        psicologoId = UUID.randomUUID();
        psicologo = Psicologo.builder().id(psicologoId).build();
    }

    @Test
    void criarProntuario_codinomeUnicoPorPsicologo_deveSalvar() {
        when(prontuarioRepository.existsByPsicologoIdAndCodinome(psicologoId, "Sol")).thenReturn(false);
        when(psicologoRepository.findById(psicologoId)).thenReturn(Optional.of(psicologo));
        when(prontuarioRepository.save(any(Prontuario.class))).thenAnswer(invocation -> {
            Prontuario prontuario = invocation.getArgument(0);
            prontuario.setId(UUID.randomUUID());
            prontuario.setCriadoEm(Instant.now());
            return prontuario;
        });

        ProntuarioResponse resposta = prontuarioService.criarProntuario(psicologoId, new CriarProntuarioRequest("Sol"));

        assertThat(resposta.codinome()).isEqualTo("Sol");
        assertThat(resposta.totalAnotacoes()).isZero();
    }

    @Test
    void criarProntuario_codinomeDuplicadoPorMesmoPsicologo_deveLancarException() {
        when(prontuarioRepository.existsByPsicologoIdAndCodinome(psicologoId, "Sol")).thenReturn(true);

        assertThatThrownBy(() -> prontuarioService.criarProntuario(psicologoId, new CriarProntuarioRequest("Sol")))
                .isInstanceOf(CodinomeJaCadastradoException.class);
    }

    @Test
    void criarProntuario_codinomeDuplicadoPorPsicologoDiferente_devePermitir() {
        // existsByPsicologoIdAndCodinome já é escopado pelo psicologoId do chamador — outro
        // psicólogo usando o mesmo codinome nunca colide nessa consulta.
        when(prontuarioRepository.existsByPsicologoIdAndCodinome(psicologoId, "Sol")).thenReturn(false);
        when(psicologoRepository.findById(psicologoId)).thenReturn(Optional.of(psicologo));
        when(prontuarioRepository.save(any(Prontuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProntuarioResponse resposta = prontuarioService.criarProntuario(psicologoId, new CriarProntuarioRequest("Sol"));

        assertThat(resposta.codinome()).isEqualTo("Sol");
    }

    @Test
    void criarAnotacao_deveArmazenarConteudoCifrado() {
        Prontuario prontuario = Prontuario.builder().id(UUID.randomUUID()).psicologo(psicologo).codinome("Sol").build();
        when(prontuarioRepository.findByPsicologoIdAndCodinome(psicologoId, "Sol")).thenReturn(Optional.of(prontuario));
        when(criptografiaService.encrypt("relato da sessão"))
                .thenReturn(new CriptografiaService.ConteudoCifrado("cifrado==", "iv=="));
        when(anotacaoRepository.save(any(Anotacao.class))).thenAnswer(invocation -> {
            Anotacao anotacao = invocation.getArgument(0);
            anotacao.setId(UUID.randomUUID());
            anotacao.setCriadaEm(Instant.now());
            return anotacao;
        });

        AnotacaoResponse resposta =
                prontuarioService.criarAnotacao(psicologoId, "Sol", new CriarAnotacaoRequest("relato da sessão"));

        assertThat(resposta.conteudo()).isEqualTo("relato da sessão");
        verify(anotacaoRepository).save(argThatContentIs("cifrado==", "iv=="));
        verify(auditoriaProntuarioService).registrar(prontuario, AcaoAuditoria.ESCRITA);
    }

    @Test
    void buscarAnotacoes_deveDecifrarConteudoAntesDeRetornar() {
        Prontuario prontuario = Prontuario.builder().id(UUID.randomUUID()).psicologo(psicologo).codinome("Sol").build();
        Anotacao anotacao = Anotacao.builder()
                .id(UUID.randomUUID())
                .prontuario(prontuario)
                .conteudoEnc("cifrado==")
                .iv("iv==")
                .criadaEm(Instant.now())
                .build();
        when(prontuarioRepository.findByPsicologoIdAndCodinome(psicologoId, "Sol")).thenReturn(Optional.of(prontuario));
        when(anotacaoRepository.findByProntuarioIdOrderByCriadaEmDesc(prontuario.getId()))
                .thenReturn(List.of(anotacao));
        when(criptografiaService.decrypt("cifrado==", "iv==")).thenReturn("texto decifrado");

        List<AnotacaoResponse> respostas = prontuarioService.listarAnotacoes(psicologoId, "Sol", null);

        assertThat(respostas).hasSize(1);
        assertThat(respostas.get(0).conteudo()).isEqualTo("texto decifrado");
        verify(auditoriaProntuarioService).registrar(prontuario, AcaoAuditoria.LEITURA);
    }

    @Test
    void buscarAnotacoes_filtraPorPalavraChave() {
        Prontuario prontuario = Prontuario.builder().id(UUID.randomUUID()).psicologo(psicologo).codinome("Sol").build();
        Anotacao anotacaoRelevante = Anotacao.builder()
                .id(UUID.randomUUID())
                .prontuario(prontuario)
                .conteudoEnc("enc1")
                .iv("iv1")
                .criadaEm(Instant.now())
                .build();
        Anotacao anotacaoIrrelevante = Anotacao.builder()
                .id(UUID.randomUUID())
                .prontuario(prontuario)
                .conteudoEnc("enc2")
                .iv("iv2")
                .criadaEm(Instant.now())
                .build();
        when(prontuarioRepository.findByPsicologoIdAndCodinome(psicologoId, "Sol")).thenReturn(Optional.of(prontuario));
        when(anotacaoRepository.findByProntuarioIdOrderByCriadaEmDesc(prontuario.getId()))
                .thenReturn(List.of(anotacaoRelevante, anotacaoIrrelevante));
        when(criptografiaService.decrypt("enc1", "iv1")).thenReturn("crise de ansiedade relatada");
        when(criptografiaService.decrypt("enc2", "iv2")).thenReturn("sessão tranquila, sem intercorrências");

        List<AnotacaoResponse> respostas = prontuarioService.listarAnotacoes(psicologoId, "Sol", "ansiedade");

        assertThat(respostas).hasSize(1);
        assertThat(respostas.get(0).conteudo()).contains("ansiedade");
    }

    @Test
    void buscarAnotacoes_psicologoNaoAutor_deveLancarAcessoProntuarioNegadoException() {
        when(prontuarioRepository.findByPsicologoIdAndCodinome(psicologoId, "Sol")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prontuarioService.listarAnotacoes(psicologoId, "Sol", null))
                .isInstanceOf(AcessoProntuarioNegadoException.class);
        verify(auditoriaProntuarioService, never()).registrar(any(), any());
    }

    @Test
    void criarAnotacao_codinomeNaoPertenceAoPsicologo_deveLancarAcessoProntuarioNegadoException() {
        when(prontuarioRepository.findByPsicologoIdAndCodinome(psicologoId, "Sol")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                        prontuarioService.criarAnotacao(psicologoId, "Sol", new CriarAnotacaoRequest("texto")))
                .isInstanceOf(AcessoProntuarioNegadoException.class);
    }

    @Test
    void atualizarCodinome_novoCodinomeDisponivel_deveAtualizar() {
        Prontuario prontuario = Prontuario.builder().id(UUID.randomUUID()).psicologo(psicologo).codinome("Sol").build();
        when(prontuarioRepository.findByPsicologoIdAndCodinome(psicologoId, "Sol")).thenReturn(Optional.of(prontuario));
        when(prontuarioRepository.existsByPsicologoIdAndCodinome(psicologoId, "Lua")).thenReturn(false);
        when(prontuarioRepository.save(any(Prontuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProntuarioResponse resposta = prontuarioService.atualizarCodinome(psicologoId, "Sol", "Lua");

        assertThat(resposta.codinome()).isEqualTo("Lua");
    }

    @Test
    void atualizarCodinome_novoCodinomeJaExistente_deveLancarException() {
        Prontuario prontuario = Prontuario.builder().id(UUID.randomUUID()).psicologo(psicologo).codinome("Sol").build();
        when(prontuarioRepository.findByPsicologoIdAndCodinome(psicologoId, "Sol")).thenReturn(Optional.of(prontuario));
        when(prontuarioRepository.existsByPsicologoIdAndCodinome(psicologoId, "Lua")).thenReturn(true);

        assertThatThrownBy(() -> prontuarioService.atualizarCodinome(psicologoId, "Sol", "Lua"))
                .isInstanceOf(CodinomeJaCadastradoException.class);
    }

    private static Anotacao argThatContentIs(String conteudoEnc, String iv) {
        return org.mockito.ArgumentMatchers.argThat(
                anotacao -> anotacao.getConteudoEnc().equals(conteudoEnc) && anotacao.getIv().equals(iv));
    }
}
