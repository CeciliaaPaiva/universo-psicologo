package br.com.unipsi.usuario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "psicologo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Psicologo {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @Column(nullable = false, length = 20)
    private String crp;

    @Column(length = 255)
    private String especializacao;

    @Column(name = "curriculo_url", length = 500)
    private String curriculoUrl;

    @Column(name = "politica_cancelamento", columnDefinition = "TEXT")
    private String politicaCancelamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_aprovacao", nullable = false, length = 30)
    private StatusAprovacao statusAprovacao;

    @Column(name = "motivo_reprovacao", columnDefinition = "TEXT")
    private String motivoReprovacao;
}
