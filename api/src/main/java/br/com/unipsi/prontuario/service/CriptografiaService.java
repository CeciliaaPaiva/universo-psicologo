package br.com.unipsi.prontuario.service;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cifra/decifra anotações de prontuário com AES-256-GCM. A chave vem de variável de ambiente
 * (CRIPTOGRAFIA_CHAVE) — nunca é persistida no banco. Cada chamada de encrypt gera um IV
 * aleatório novo, então o mesmo texto produz cifrados diferentes a cada vez.
 */
@Service
public class CriptografiaService {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int TAMANHO_IV_BYTES = 12;
    private static final int TAMANHO_TAG_BITS = 128;

    private final SecretKey chave;

    public CriptografiaService(@Value("${unipsi.criptografia.chave}") String chaveBase64) {
        this.chave = new SecretKeySpec(Base64.getDecoder().decode(chaveBase64), "AES");
    }

    public ConteudoCifrado encrypt(String textoPlano) {
        if (textoPlano == null || textoPlano.isBlank()) {
            throw new IllegalArgumentException("Conteúdo da anotação não pode ser vazio");
        }
        try {
            byte[] iv = new byte[TAMANHO_IV_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

            return new ConteudoCifrado(
                    Base64.getEncoder().encodeToString(cifrado), Base64.getEncoder().encodeToString(iv));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao cifrar anotação", e);
        }
    }

    public String decrypt(String conteudoEncBase64, String ivBase64) {
        try {
            byte[] iv = Base64.getDecoder().decode(ivBase64);
            byte[] conteudoCifrado = Base64.getDecoder().decode(conteudoEncBase64);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG_BITS, iv));
            byte[] textoPlano = cipher.doFinal(conteudoCifrado);

            return new String(textoPlano, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Falha ao decifrar anotação", e);
        }
    }

    public record ConteudoCifrado(String conteudoEnc, String iv) {
    }
}
