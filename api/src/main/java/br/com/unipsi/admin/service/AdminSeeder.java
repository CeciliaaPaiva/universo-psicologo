package br.com.unipsi.admin.service;

import br.com.unipsi.usuario.domain.Role;
import br.com.unipsi.usuario.domain.Usuario;
import br.com.unipsi.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Garante a existência de um usuário ADMIN em ambientes onde ADMIN_EMAIL/ADMIN_PASSWORD
 * estão configurados. Não há cadastro de admin pela API — só há esse bootstrap.
 */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${unipsi.admin.email:}")
    private String adminEmail;

    @Value("${unipsi.admin.password:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }
        if (usuarioRepository.existsByEmail(adminEmail)) {
            return;
        }

        usuarioRepository.save(Usuario.builder()
                .nome("Administrador")
                .email(adminEmail)
                .senhaHash(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build());
        log.info("Usuário ADMIN criado para {}", adminEmail);
    }
}
