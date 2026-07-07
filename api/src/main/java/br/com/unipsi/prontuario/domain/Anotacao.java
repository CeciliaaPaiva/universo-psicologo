package br.com.unipsi.prontuario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code conteudoEnc} e {@code iv} nunca trafegam em texto claro fora do CriptografiaService —
 * ambos são base64 do resultado do AES-256-GCM.
 */
@Entity
@Table(name = "anotacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prontuario_id", nullable = false)
    private Prontuario prontuario;

    @Column(name = "conteudo_enc", nullable = false, columnDefinition = "TEXT")
    private String conteudoEnc;

    @Column(nullable = false, length = 64)
    private String iv;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;

    @PrePersist
    void aoCriar() {
        if (criadaEm == null) {
            criadaEm = Instant.now();
        }
    }
}
