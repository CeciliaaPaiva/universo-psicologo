package br.com.unipsi.usuario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Anamnese básica do paciente (US-028). {@code conteudoEnc}/{@code iv} seguem o mesmo padrão de
 * cifra do prontuário (AES-256-GCM via CriptografiaService) — nunca em texto claro no banco.
 * Sempre pertence ao paciente; acesso temporário do psicólogo (US-030) é trabalho futuro, ligado
 * ao módulo financeiro (Sprint 5.5).
 */
@Entity
@Table(name = "anamnese")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anamnese {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    @Column(name = "conteudo_enc", nullable = false, columnDefinition = "TEXT")
    private String conteudoEnc;

    @Column(nullable = false, length = 64)
    private String iv;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private Instant atualizadaEm;

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        criadaEm = agora;
        atualizadaEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadaEm = Instant.now();
    }
}
