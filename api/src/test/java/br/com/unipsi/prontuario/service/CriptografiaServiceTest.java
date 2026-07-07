package br.com.unipsi.prontuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CriptografiaServiceTest {

    // Chave AES-256 fixa e válida, apenas para teste — não é usada em nenhum ambiente real.
    private static final String CHAVE_TESTE = "L1bzEUTzGrpzHGueIAA679M43sDed+mTVqL39zGGNBY=";

    private final CriptografiaService criptografiaService = new CriptografiaService(CHAVE_TESTE);

    @Test
    void encrypt_textoSimples_deveRetornarConteudoCifradoEIv() {
        CriptografiaService.ConteudoCifrado cifrado = criptografiaService.encrypt("paciente relatou melhora");

        assertThat(cifrado.conteudoEnc()).isNotBlank();
        assertThat(cifrado.iv()).isNotBlank();
        assertThat(cifrado.conteudoEnc()).doesNotContain("paciente relatou melhora");
    }

    @Test
    void decrypt_dadosCifradosValidos_deveRetornarTextoOriginal() {
        String textoOriginal = "sessão de hoje: trabalhamos técnicas de respiração";
        CriptografiaService.ConteudoCifrado cifrado = criptografiaService.encrypt(textoOriginal);

        String textoDecifrado = criptografiaService.decrypt(cifrado.conteudoEnc(), cifrado.iv());

        assertThat(textoDecifrado).isEqualTo(textoOriginal);
    }

    @Test
    void encrypt_textosDiferentes_devemGerarIvsDiferentes() {
        CriptografiaService.ConteudoCifrado cifrado1 = criptografiaService.encrypt("texto A");
        CriptografiaService.ConteudoCifrado cifrado2 = criptografiaService.encrypt("texto B");

        assertThat(cifrado1.iv()).isNotEqualTo(cifrado2.iv());
    }

    @Test
    void encrypt_mesmoTexto_duasVezes_devemGerarCifradosDiferentes() {
        String texto = "mesma anotação";
        CriptografiaService.ConteudoCifrado cifrado1 = criptografiaService.encrypt(texto);
        CriptografiaService.ConteudoCifrado cifrado2 = criptografiaService.encrypt(texto);

        assertThat(cifrado1.conteudoEnc()).isNotEqualTo(cifrado2.conteudoEnc());
        assertThat(cifrado1.iv()).isNotEqualTo(cifrado2.iv());
    }

    @Test
    void decrypt_ivInvalido_deveLancarException() {
        CriptografiaService.ConteudoCifrado cifrado = criptografiaService.encrypt("texto qualquer");

        assertThatThrownBy(() -> criptografiaService.decrypt(cifrado.conteudoEnc(), "iv-invalido-nao-base64!!"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decrypt_conteudoCorrompido_deveLancarException() {
        CriptografiaService.ConteudoCifrado cifrado = criptografiaService.encrypt("texto qualquer");
        String conteudoCorrompido = cifrado.conteudoEnc().substring(0, cifrado.conteudoEnc().length() - 4) + "AAAA";

        assertThatThrownBy(() -> criptografiaService.decrypt(conteudoCorrompido, cifrado.iv()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void encrypt_textoVazio_deveLancarException() {
        assertThatThrownBy(() -> criptografiaService.encrypt("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void encrypt_textoNulo_deveLancarException() {
        assertThatThrownBy(() -> criptografiaService.encrypt(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
