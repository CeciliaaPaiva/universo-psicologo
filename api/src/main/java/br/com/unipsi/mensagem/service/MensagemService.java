package br.com.unipsi.mensagem.service;

import br.com.unipsi.financeiro.domain.StatusCobranca;
import br.com.unipsi.financeiro.repository.CobrancaRepository;
import br.com.unipsi.mensagem.domain.CanalMensagemFechadoException;
import br.com.unipsi.mensagem.domain.Mensagem;
import br.com.unipsi.mensagem.dto.ContatoMensagemResponse;
import br.com.unipsi.mensagem.dto.MensagemResponse;
import br.com.unipsi.mensagem.repository.MensagemRepository;
import br.com.unipsi.notificacao.service.NotificacaoService;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.repository.PacienteRepository;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import br.com.unipsi.usuario.repository.UsuarioRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Chat interno entre paciente e psicólogo (US-029). Liberado apenas se existir uma
 * {@code Cobranca} com status {@code PAGO} entre os dois — verificado a cada envio/listagem, não
 * apenas na primeira mensagem, para que o canal feche automaticamente se essa cobrança for
 * cancelada depois (não deveria acontecer na prática, já que cobrança paga não é cancelável, mas
 * mantém a checagem consistente com a regra de negócio "libera depois do pagamento").
 */
@Service
@RequiredArgsConstructor
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final CobrancaRepository cobrancaRepository;
    private final PacienteRepository pacienteRepository;
    private final PsicologoRepository psicologoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    @Transactional(readOnly = true)
    public List<ContatoMensagemResponse> listarContatosDoPaciente(UUID pacienteId) {
        return cobrancaRepository.buscarPsicologosComCobrancaPaga(pacienteId, StatusCobranca.PAGO).stream()
                .map(psicologoId -> paraContato(psicologoId, pacienteId, psicologoId, pacienteId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContatoMensagemResponse> listarContatosDoPsicologo(UUID psicologoId) {
        return cobrancaRepository.buscarPacientesComCobrancaPaga(psicologoId, StatusCobranca.PAGO).stream()
                .map(pacienteId -> paraContato(pacienteId, pacienteId, psicologoId, psicologoId))
                .toList();
    }

    @Transactional
    public List<MensagemResponse> listarConversa(UUID usuarioId, boolean chamadorEhPsicologo, UUID outroId) {
        UUID pacienteId = chamadorEhPsicologo ? outroId : usuarioId;
        UUID psicologoId = chamadorEhPsicologo ? usuarioId : outroId;
        validarCanalAberto(pacienteId, psicologoId);

        List<Mensagem> naoLidas = mensagemRepository.findByPacienteIdAndPsicologoIdAndLidaFalseAndRemetenteIdNot(
                pacienteId, psicologoId, usuarioId);
        naoLidas.forEach(m -> m.setLida(true));

        return mensagemRepository.findByPacienteIdAndPsicologoIdOrderByCriadaEmAsc(pacienteId, psicologoId).stream()
                .map(MensagemResponse::from)
                .toList();
    }

    @Transactional
    public MensagemResponse enviar(UUID usuarioId, boolean chamadorEhPsicologo, UUID outroId, String conteudo) {
        UUID pacienteId = chamadorEhPsicologo ? outroId : usuarioId;
        UUID psicologoId = chamadorEhPsicologo ? usuarioId : outroId;
        validarCanalAberto(pacienteId, psicologoId);

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));
        Psicologo psicologo = psicologoRepository.findById(psicologoId)
                .orElseThrow(() -> new IllegalArgumentException("Psicólogo não encontrado"));

        Mensagem mensagem = mensagemRepository.save(Mensagem.builder()
                .paciente(paciente)
                .psicologo(psicologo)
                .remetenteId(usuarioId)
                .conteudo(conteudo)
                .lida(false)
                .build());

        UUID destinatarioId = chamadorEhPsicologo ? pacienteId : psicologoId;
        String nomeRemetente = chamadorEhPsicologo
                ? psicologo.getUsuario().getNome()
                : paciente.getUsuario().getNome();
        notificacaoService.criar(destinatarioId, "Nova mensagem de %s".formatted(nomeRemetente));

        return MensagemResponse.from(mensagem);
    }

    private void validarCanalAberto(UUID pacienteId, UUID psicologoId) {
        boolean liberado = cobrancaRepository.existsPagaEntrePacienteEPsicologo(
                pacienteId, psicologoId, StatusCobranca.PAGO);
        if (!liberado) {
            throw new CanalMensagemFechadoException(
                    "O chat só é liberado depois que uma sessão entre vocês for agendada e paga");
        }
    }

    private ContatoMensagemResponse paraContato(UUID outroUsuarioId, UUID pacienteId, UUID psicologoId, UUID chamadorId) {
        String nome = usuarioRepository.findById(outroUsuarioId)
                .map(u -> u.getNome())
                .orElse("Usuário");
        long naoLidas = mensagemRepository.countByPacienteIdAndPsicologoIdAndLidaFalseAndRemetenteIdNot(
                pacienteId, psicologoId, chamadorId);
        return new ContatoMensagemResponse(outroUsuarioId, nome, naoLidas);
    }
}
