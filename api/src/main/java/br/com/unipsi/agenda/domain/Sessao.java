package br.com.unipsi.agenda.domain;

import br.com.unipsi.usuario.domain.Paciente;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sessao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private Slot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psicologo_id", nullable = false)
    private Psicologo psicologo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidade modalidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_atendimento", nullable = false, length = 20)
    private TipoAtendimento tipoAtendimento;

    @Column(name = "valor_sessao", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSessao;

    @Column(name = "taxa_plataforma", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaPlataforma;

    @Column(name = "valor_liquido", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorLiquido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSessao status;

    @Column(name = "cancelado_em")
    private Instant canceladoEm;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;

    @PrePersist
    void aoCriar() {
        if (criadaEm == null) {
            criadaEm = Instant.now();
        }
    }
}
