package br.com.unipsi.auth.service;

import br.com.unipsi.auth.domain.CadastroNaoAprovadoException;
import br.com.unipsi.auth.domain.CredenciaisInvalidasException;
import br.com.unipsi.auth.domain.RefreshTokenInvalidoException;
import br.com.unipsi.auth.dto.AuthResponse;
import br.com.unipsi.auth.dto.LoginRequest;
import br.com.unipsi.auth.dto.RegisterPacienteRequest;
import br.com.unipsi.auth.dto.RegisterPsicologoRequest;
import br.com.unipsi.notificacao.service.EmailService;
import br.com.unipsi.usuario.domain.EmailJaCadastradoException;
import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.PacienteNaoElegivelException;
import br.com.unipsi.usuario.domain.Psicologo;
import br.com.unipsi.usuario.domain.Role;
import br.com.unipsi.usuario.domain.StatusAprovacao;
import br.com.unipsi.usuario.domain.Usuario;
import br.com.unipsi.usuario.repository.PacienteRepository;
import br.com.unipsi.usuario.repository.PsicologoRepository;
import br.com.unipsi.usuario.repository.UsuarioRepository;
import br.com.unipsi.usuario.service.MinioService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PsicologoRepository psicologoRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final MinioService minioService;
    private final EmailService emailService;

    @Transactional
    public void registrarPsicologo(RegisterPsicologoRequest dados, MultipartFile curriculo) {
        garantirEmailDisponivel(dados.email());

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome(dados.nome())
                .email(dados.email())
                .senhaHash(passwordEncoder.encode(dados.senha()))
                .role(Role.PSICOLOGO)
                .build());

        String curriculoUrl = minioService.enviarCurriculo(usuario.getId(), curriculo);

        psicologoRepository.save(Psicologo.builder()
                .usuario(usuario)
                .crp(dados.crp())
                .especializacao(dados.especializacao())
                .politicaCancelamento(dados.politicaCancelamento())
                .curriculoUrl(curriculoUrl)
                .statusAprovacao(StatusAprovacao.PENDENTE_APROVACAO)
                .build());

        emailService.enviarConfirmacaoCadastroPsicologo(usuario.getEmail(), usuario.getNome());
    }

    @Transactional
    public void registrarPaciente(RegisterPacienteRequest dados) {
        if (dados.faixaRenda() == FaixaRenda.FORA_DO_ESCOPO) {
            throw new PacienteNaoElegivelException(
                    "A plataforma atende exclusivamente pacientes de baixa renda (até Classe D). "
                            + "Procure atendimento particular.");
        }
        garantirEmailDisponivel(dados.email());

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome(dados.nome())
                .email(dados.email())
                .senhaHash(passwordEncoder.encode(dados.senha()))
                .role(Role.PACIENTE)
                .build());

        pacienteRepository.save(Paciente.builder()
                .usuario(usuario)
                .faixaRenda(dados.faixaRenda())
                .build());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest dados) {
        Usuario usuario = usuarioRepository.findByEmail(dados.email())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(dados.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        if (usuario.getRole() == Role.PSICOLOGO) {
            Psicologo psicologo = psicologoRepository.findById(usuario.getId()).orElseThrow();
            if (psicologo.getStatusAprovacao() != StatusAprovacao.APROVADO) {
                throw new CadastroNaoAprovadoException(psicologo.getStatusAprovacao());
            }
        }

        return gerarTokens(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        UUID usuarioId = refreshTokenService.validar(refreshToken)
                .orElseThrow(RefreshTokenInvalidoException::new);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(RefreshTokenInvalidoException::new);

        refreshTokenService.revogar(refreshToken);
        return gerarTokens(usuario);
    }

    private AuthResponse gerarTokens(Usuario usuario) {
        String accessToken = jwtService.gerarAccessToken(usuario);
        String refreshToken = refreshTokenService.gerar(usuario.getId());
        return new AuthResponse(accessToken, refreshToken, usuario.getRole().name(), usuario.getNome());
    }

    private void garantirEmailDisponivel(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }
    }
}
