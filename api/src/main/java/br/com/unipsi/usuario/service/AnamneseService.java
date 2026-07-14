package br.com.unipsi.usuario.service;

import br.com.unipsi.prontuario.service.CriptografiaService;
import br.com.unipsi.usuario.domain.Anamnese;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.dto.AnamneseRequest;
import br.com.unipsi.usuario.dto.AnamneseResponse;
import br.com.unipsi.usuario.repository.AnamneseRepository;
import br.com.unipsi.usuario.repository.PacienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A anamnese é sempre do paciente — nunca pública, nunca permanente para o psicólogo (ver
 * atas/2026-07-07-alinhamento-sprint-4.md). Esta classe só cobre o preenchimento pelo paciente
 * (US-028); o acesso temporário do psicólogo (US-030) depende do módulo financeiro e entra na
 * Sprint 5.5. Conteúdo é serializado em JSON e cifrado como um único blob, no mesmo padrão de
 * {@code CriptografiaService} usado no prontuário.
 */
@Service
@RequiredArgsConstructor
public class AnamneseService {

    private final AnamneseRepository anamneseRepository;
    private final PacienteRepository pacienteRepository;
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
