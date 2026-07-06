package br.com.unipsi.plantao.repository;

import br.com.unipsi.plantao.domain.DisponibilidadePlantao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisponibilidadePlantaoRepository extends JpaRepository<DisponibilidadePlantao, UUID> {

    List<DisponibilidadePlantao> findByPsicologoIdOrderByCriadoEmDesc(UUID psicologoId);
}
