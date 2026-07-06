package br.com.unipsi.agenda.service;

import br.com.unipsi.auth.service.JwtService;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private static final String FINALIDADE_STATE = "google_calendar_oauth";

    private final GoogleCalendarService googleCalendarService;
    private final PsicologoRepository psicologoRepository;
    private final JwtService jwtService;

    public String gerarUrlAutorizacao(UUID psicologoId) {
        String state = jwtService.gerarTokenEstado(psicologoId, FINALIDADE_STATE);
        return googleCalendarService.gerarUrlAutorizacao(state);
    }

    @Transactional
    public void tratarCallback(String code, String state) {
        UUID psicologoId = jwtService.validarTokenEstado(state, FINALIDADE_STATE);
        Psicologo psicologo = psicologoRepository.findById(psicologoId)
                .orElseThrow(() -> new IllegalArgumentException("Psicólogo não encontrado"));

        String refreshToken = googleCalendarService.trocarCodigoPorRefreshToken(code);
        if (refreshToken != null) {
            psicologo.setGoogleRefreshToken(refreshToken);
            psicologoRepository.save(psicologo);
        }
    }
}
