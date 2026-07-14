package br.com.unipsi.usuario.service;

import br.com.unipsi.agenda.domain.StatusSessao;
import br.com.unipsi.agenda.repository.SessaoRepository;
import br.com.unipsi.prontuario.service.CriptografiaService;
import br.com.unipsi.usuario.domain.AcessoAnamneseNegadoException;
import br.com.unipsi.usuario.domain.Anamnese;
import br.com.unipsi.usuario.domain.AuditoriaAnamnese;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.dto.AnamnesePsicologoResponse;
import br.com.unipsi.usuario.dto.AnamneseRequest;
import br.com.unipsi.usuario.dto.AnamneseResponse;
import br.com.unipsi.usuario.repository.AnamneseRepository;
import br.com.unipsi.usuario.repository.AuditoriaAnamneseRepository;
import br.com.unipsi.usuario.repository.PacienteRepository;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A anamnese é sempre do paciente — nunca pública, nunca permanente para o psicólogo (ver
 * atas/2026-07-07-alinhamento-sprint-4.md). O preenchimento é feito pelo paciente (US-028); o
 * acesso do psicólogo (US-030) é temporário, por janela de tempo — liberado enquanto existir uma
 * {@code Sessao} {@code AGENDADA} entre os dois, revogado assim que ela vira {@code REALIZADA}.
 * (Adaptação de 14/07/2026: a US-030 original condicionava a liberação a uma {@code Cobranca}
 * paga associada à sessão ainda AGENDADA — mas no modelo financeiro implementado na Sprint 5 a
 * cobrança só é gerada no momento em que a sessão vira REALIZADA, então essa combinação nunca
 * ocorreria. A liberação por sessão AGENDADA, sem exigir cobrança paga, é a adaptação prática
 * decidida com o stakeholder para manter a mesma janela temporal pretendida.)
 */
@Service
@RequiredArgsConstructor
public class AnamneseService {

    private final AnamneseRepository anamneseRepository;
    private final PacienteRepository pacienteRepository;
    private final PsicologoRepository psicologoRepository;
    private final SessaoRepository sessaoRepository;
    private final AuditoriaAnamneseRepository auditoriaAnamneseRepository;
    private final CriptografiaService criptografiaService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public AnamneseResponse buscar(UUID pacienteId) {
        Paciente paciente = buscarPaciente(pacienteId);
        return anamneseRepository.findByPacienteId(pacienteId)
                .map(anamnese -> paraResposta(anamnese, paciente))
                .orElseGet(() -> new AnamneseResponse(false, null, null, null, null, paciente.isMenorDeIdade()));
    }

    @Transactional
    public AnamneseResponse salvar(UUID pacienteId, AnamneseRequest dados) {
        Paciente paciente = buscarPaciente(pacienteId);

        if (paciente.isMenorDeIdade() && (dados.contatoResponsavel() == null || dados.contatoResponsavel().isBlank())) {
            throw new IllegalArgumentException(
                    "Contato do responsável é obrigatório: pacientes menores de idade precisam da presença/consentimento "
                            + "do responsável, especialmente na primeira sessão.");
        }

        Conteudo conteudo = new Conteudo(
                dados.jaFezTerapia(), dados.motivoBusca(), dados.medicacaoControlada(), dados.contatoResponsavel());
        CriptografiaService.ConteudoCifrado cifrado = criptografiaService.encrypt(serializar(conteudo));

        Anamnese anamnese = anamneseRepository.findByPacienteId(pacienteId).orElseGet(() -> Anamnese.builder()
                .paciente(paciente)
                .build());
        anamnese.setConteudoEnc(cifrado.conteudoEnc());
        anamnese.setIv(cifrado.iv());

        Anamnese salva = anamneseRepository.save(anamnese);
        return paraResposta(salva, paciente);
    }

    @Transactional
    public AnamnesePsicologoResponse buscarParaPsicologo(UUID psicologoId, UUID pacienteId) {
        boolean acessoLiberado = sessaoRepository.existsByPacienteIdAndPsicologoIdAndStatus(
                pacienteId, psicologoId, StatusSessao.AGENDADA);
        if (!acessoLiberado) {
            throw new AcessoAnamneseNegadoException(
                    "Anamnese só pode ser acessada enquanto houver uma sessão agendada com este paciente");
        }

        Paciente paciente = buscarPaciente(pacienteId);
        Anamnese anamnese = anamneseRepository.findByPacienteId(pacienteId).orElse(null);
        if (anamnese == null) {
            return new AnamnesePsicologoResponse(false, null, null, null, null, paciente.isMenorDeIdade());
        }

        registrarAuditoria(anamnese, psicologoId);

        Conteudo conteudo = desserializar(criptografiaService.decrypt(anamnese.getConteudoEnc(), anamnese.getIv()));
        return new AnamnesePsicologoResponse(
                true,
                conteudo.jaFezTerapia(),
                conteudo.motivoBusca(),
                conteudo.medicacaoControlada(),
                conteudo.contatoResponsavel(),
                paciente.isMenorDeIdade());
    }

    private void registrarAuditoria(Anamnese anamnese, UUID psicologoId) {
        Psicologo psicologo = psicologoRepository.findById(psicologoId)
                .orElseThrow(() -> new IllegalArgumentException("Psicólogo não encontrado"));
        auditoriaAnamneseRepository.save(
                AuditoriaAnamnese.builder().anamnese(anamnese).psicologo(psicologo).build());
    }

    private AnamneseResponse paraResposta(Anamnese anamnese, Paciente paciente) {
        Conteudo conteudo = desserializar(criptografiaService.decrypt(anamnese.getConteudoEnc(), anamnese.getIv()));
        return new AnamneseResponse(
                true,
                conteudo.jaFezTerapia(),
                conteudo.motivoBusca(),
                conteudo.medicacaoControlada(),
                conteudo.contatoResponsavel(),
                paciente.isMenorDeIdade());
    }

    private String serializar(Conteudo conteudo) {
        try {
            return objectMapper.writeValueAsString(conteudo);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao serializar anamnese", e);
        }
    }

    private Conteudo desserializar(String json) {
        try {
            return objectMapper.readValue(json, Conteudo.class);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao desserializar anamnese", e);
        }
    }

    private Paciente buscarPaciente(UUID pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));
    }

    private record Conteudo(Boolean jaFezTerapia, String motivoBusca, String medicacaoControlada, String contatoResponsavel) {
    }
}
