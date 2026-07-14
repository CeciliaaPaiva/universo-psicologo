package br.com.unipsi;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiApplication {

	public static void main(String[] args) {
		// Plataforma atende exclusivamente o Brasil; LocalDateTime.now() é usado sem fuso
		// explícito em toda a aplicação (agenda, lembretes, plantão), então fixamos o horário
		// padrão da JVM em Brasília para bater com o horário de parede que o frontend envia.
		TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
		SpringApplication.run(ApiApplication.class, args);
	}

}
