package br.com.unipsi.chatbot.service;

import br.com.unipsi.chatbot.domain.StatusTriagem;
import java.text.Normalizer;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Classifica um texto (resposta do chatbot ou mensagem do usuário) como NORMAL ou CRISE por
 * palavras-chave, normalizando acentos para tornar o casamento mais robusto. Cobertura de
 * branches é prioritária aqui — cada ramo é exercitado em {@code CriseDetectorServiceTest}.
 *
 * <p>A lista inclui tanto sintomas ditos pelo usuário (para classificar mensagens cruas) quanto
 * as frases que o {@code system_prompt.txt} obriga o modelo a incluir em qualquer resposta de
 * crise ("técnica de suporte imediato", "buscando um psicólogo disponível agora") — sem isso,
 * uma resposta de crise que só acolhe e orienta (sem repetir os sintomas do usuário) não seria
 * classificada como CRISE, quebrando o acionamento do plantão.
 */
@Service
public class CriseDetectorService {

    private static final List<String> PALAVRAS_CRISE = List.of(
            "suicid",
            "me matar",
            "tirar minha vida",
            "tirar a propria vida",
            "acabar com tudo",
            "nao quero mais viver",
            "autolesao",
            "me cortar",
            "me machucar",
            "crise de panico",
            "ataque de panico",
            "nao consigo respirar",
            "ansiedade severa",
            "em desespero",
            "nao aguento mais",
            "psicologo disponivel agora",
            "respiracao 4-7-8",
            "ancoragem 5-4-3-2-1");

    public StatusTriagem classificar(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("Texto para classificação não pode ser nulo");
        }
        if (texto.isBlank()) {
            return StatusTriagem.NORMAL;
        }

        String normalizado = normalizar(texto);
        boolean contemCrise = PALAVRAS_CRISE.stream().anyMatch(normalizado::contains);
        return contemCrise ? StatusTriagem.CRISE : StatusTriagem.NORMAL;
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }
}
