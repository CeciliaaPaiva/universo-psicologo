package br.com.unipsi.usuario.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MinioService {

    private static final List<String> TIPOS_PERMITIDOS = List.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    private static final long TAMANHO_MAXIMO_BYTES = 5L * 1024 * 1024;

    private static final List<String> TIPOS_IMAGEM_PERMITIDOS = List.of("image/jpeg", "image/png", "image/webp");
    private static final long TAMANHO_MAXIMO_FOTO_BYTES = 2L * 1024 * 1024;

    private final MinioClient minioClient;

    @Value("${unipsi.minio.bucket}")
    private String bucket;

    public String enviarCurriculo(UUID psicologoId, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Currículo é obrigatório");
        }
        if (!TIPOS_PERMITIDOS.contains(arquivo.getContentType())) {
            throw new IllegalArgumentException("Currículo deve ser PDF ou DOCX");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("Currículo deve ter no máximo 5 MB");
        }

        String chaveObjeto = "curriculos/%s/%s".formatted(psicologoId, arquivo.getOriginalFilename());
        try (var inputStream = arquivo.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(chaveObjeto)
                    .stream(inputStream, arquivo.getSize(), -1)
                    .contentType(arquivo.getContentType())
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao enviar currículo", e);
        }
        return chaveObjeto;
    }

    public String enviarFoto(UUID donoId, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Foto é obrigatória");
        }
        if (!TIPOS_IMAGEM_PERMITIDOS.contains(arquivo.getContentType())) {
            throw new IllegalArgumentException("Foto deve ser JPEG, PNG ou WEBP");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_FOTO_BYTES) {
            throw new IllegalArgumentException("Foto deve ter no máximo 2 MB");
        }

        String chaveObjeto = "fotos/%s/%s".formatted(donoId, arquivo.getOriginalFilename());
        try (var inputStream = arquivo.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(chaveObjeto)
                    .stream(inputStream, arquivo.getSize(), -1)
                    .contentType(arquivo.getContentType())
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao enviar foto", e);
        }
        return chaveObjeto;
    }
}
