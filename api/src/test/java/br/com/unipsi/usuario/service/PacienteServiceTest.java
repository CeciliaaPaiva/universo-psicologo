package br.com.unipsi.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;
import br.com.unipsi.usuario.domain.PacienteNaoElegivelException;
import br.com.unipsi.usuario.domain.Usuario;
import br.com.unipsi.usuario.dto.AtualizarPerfilPacienteRequest;
import br.com.unipsi.usuario.dto.PerfilPacienteResponse;
import br.com.unipsi.usuario.repository.PacienteRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private UUID pacienteId;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        pacienteId = UUID.randomUUID();
        Usuario usuario = Usuario.builder().nome("Paciente Teste").email("paciente@teste.com").build();
        paciente = Paciente.builder().id(pacienteId).usuario(usuario).faixaRenda(FaixaRenda.FAIXA_1).build();
    }

    @Test
    void buscarPerfil_deveRetornarDadosDoPaciente() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        PerfilPacienteResponse resposta = pacienteService.buscarPerfil(pacienteId);

        assertThat(resposta.faixaRenda()).isEqualTo(FaixaRenda.FAIXA_1);
    }

    @Test
    void atualizarPerfil_novaFaixaValida_deveAtualizarERetornar() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerfilPacienteResponse resposta =
                pacienteService.atualizarPerfil(pacienteId, new AtualizarPerfilPacienteRequest(FaixaRenda.FAIXA_3));

        assertThat(resposta.faixaRenda()).isEqualTo(FaixaRenda.FAIXA_3);
    }

    @Test
    void atualizarPerfil_faixaForaDoEscopo_deveLancarPacienteNaoElegivelException() {
        assertThatThrownBy(() -> pacienteService.atualizarPerfil(
                        pacienteId, new AtualizarPerfilPacienteRequest(FaixaRenda.FORA_DO_ESCOPO)))
                .isInstanceOf(PacienteNaoElegivelException.class);
    }
}
