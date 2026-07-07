package br.com.unipsi.prontuario.domain;

import br.com.unipsi.usuario.domain.Psicologo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "auditoria_prontuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaProntuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prontuario_id", nullable = false)
    private Prontuario prontuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psicologo_id", nullable = false)
    private Psicologo psicologo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcaoAuditoria acao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }
}
