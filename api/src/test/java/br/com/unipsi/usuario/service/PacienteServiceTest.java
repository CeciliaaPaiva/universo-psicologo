package br.com.unipsi.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.unipsi.usuario.domain.FaixaRenda;
import br.com.unipsi.usuario.domain.Paciente;
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
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private MinioService minioService;

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
    void atualizarPerfil_deveAtualizarNomeEIdadeSemAlterarFaixaDeRenda() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerfilPacienteResponse resposta = pacienteService.atualizarPerfil(
                pacienteId, new AtualizarPerfilPacienteRequest("Novo Nome", 30), null);

        assertThat(resposta.nome()).isEqualTo("Novo Nome");
        assertThat(resposta.idade()).isEqualTo(30);
        assertThat(resposta.faixaRenda()).isEqualTo(FaixaRenda.FAIXA_1);
        assertThat(resposta.menorDeIdade()).isFalse();
        verifyNoInteractions(minioService);
    }

    @Test
    void atualizarPerfil_comIdadeMenorDeIdade_deveMarcarMenorDeIdade() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerfilPacienteResponse resposta = pacienteService.atualizarPerfil(
                pacienteId, new AtualizarPerfilPacienteRequest("Paciente Jovem", 16), null);

        assertThat(resposta.menorDeIdade()).isTrue();
    }

    @Test
    void atualizarPerfil_comFoto_deveEnviarParaMinioEGravarUrl() {
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var foto = new MockMultipartFile("foto", "foto.png", "image/png", new byte[] {1, 2, 3});
        when(minioService.enviarFoto(pacienteId, foto)).thenReturn("fotos/" + pacienteId + "/foto.png");

        PerfilPacienteResponse resposta =
                pacienteService.atualizarPerfil(pacienteId, new AtualizarPerfilPacienteRequest("Paciente Teste", 30), foto);

        assertThat(resposta.fotoUrl()).isEqualTo("fotos/" + pacienteId + "/foto.png");
    }
}
