package br.com.unipsi.prontuario.service;

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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;
    private final AnotacaoRepository anotacaoRepository;
    private final PsicologoRepository psicologoRepository;
    private final CriptografiaService criptografiaService;
    private final AuditoriaProntuarioService auditoriaProntuarioService;

    @Transactional
    public ProntuarioResponse criarProntuario(UUID psicologoId, CriarProntuarioRequest pedido) {
        garantirCodinomeDisponivel(psicologoId, pedido.codinome());

        Psicologo psicologo = psicologoRepository.findById(psicologoId)
                .orElseThrow(() -> new IllegalArgumentException("Psicólogo não encontrado"));

        Prontuario prontuario = prontuarioRepository.save(
                Prontuario.builder().psicologo(psicologo).codinome(pedido.codinome()).build());

        return ProntuarioResponse.from(prontuario, 0);
    }

    @Transactional(readOnly = true)
    public List<ProntuarioResponse> listar(UUID psicologoId) {
        return prontuarioRepository.findByPsicologoIdOrderByCriadoEmDesc(psicologoId).stream()
                .map(prontuario ->
                        ProntuarioResponse.from(prontuario, anotacaoRepository.countByProntuarioId(prontuario.getId())))
                .toList();
    }

    @Transactional
    public ProntuarioResponse atualizarCodinome(UUID psicologoId, String codinome, String novoCodinome) {
        Prontuario prontuario = buscarProntuarioDoPsicologo(psicologoId, codinome);
        if (!novoCodinome.equals(codinome)) {
            garantirCodinomeDisponivel(psicologoId, novoCodinome);
        }
        prontuario.setCodinome(novoCodinome);
        return ProntuarioResponse.from(
                prontuarioRepository.save(prontuario), anotacaoRepository.countByProntuarioId(prontuario.getId()));
    }

    @Transactional
    public AnotacaoResponse criarAnotacao(UUID psicologoId, String codinome, CriarAnotacaoRequest pedido) {
        Prontuario prontuario = buscarProntuarioDoPsicologo(psicologoId, codinome);

        CriptografiaService.ConteudoCifrado cifrado = criptografiaService.encrypt(pedido.conteudo());
        Anotacao anotacao = anotacaoRepository.save(Anotacao.builder()
                .prontuario(prontuario)
                .conteudoEnc(cifrado.conteudoEnc())
                .iv(cifrado.iv())
                .build());

        auditoriaProntuarioService.registrar(prontuario, AcaoAuditoria.ESCRITA);

        return new AnotacaoResponse(anotacao.getId(), pedido.conteudo(), anotacao.getCriadaEm());
    }

    @Transactional
    public List<AnotacaoResponse> listarAnotacoes(UUID psicologoId, String codinome, String busca) {
        Prontuario prontuario = buscarProntuarioDoPsicologo(psicologoId, codinome);

        auditoriaProntuarioService.registrar(prontuario, AcaoAuditoria.LEITURA);

        String buscaNormalizada = (busca == null) ? null : busca.toLowerCase();
        return anotacaoRepository.findByProntuarioIdOrderByCriadaEmDesc(prontuario.getId()).stream()
                .map(anotacao -> new AnotacaoResponse(
                        anotacao.getId(),
                        criptografiaService.decrypt(anotacao.getConteudoEnc(), anotacao.getIv()),
                        anotacao.getCriadaEm()))
                .filter(anotacao -> buscaNormalizada == null || buscaNormalizada.isBlank()
                        || anotacao.conteudo().toLowerCase().contains(buscaNormalizada))
                .toList();
    }

    /**
     * Escopado por psicólogo por construção: um codinome só existe dentro do prontuário do
     * psicólogo que o criou. Se não encontrado — seja porque não existe, seja porque pertence a
     * outro psicólogo — o resultado é sempre 403, sem distinguir os dois casos (evita enumeração).
     */
    private Prontuario buscarProntuarioDoPsicologo(UUID psicologoId, String codinome) {
        return prontuarioRepository
                .findByPsicologoIdAndCodinome(psicologoId, codinome)
                .orElseThrow(() -> new AcessoProntuarioNegadoException(
                        "Prontuário não encontrado ou você não tem acesso a ele"));
    }

    private void garantirCodinomeDisponivel(UUID psicologoId, String codinome) {
        if (prontuarioRepository.existsByPsicologoIdAndCodinome(psicologoId, codinome)) {
            throw new CodinomeJaCadastradoException("Já existe um paciente com esse codinome no seu prontuário");
        }
    }
}
