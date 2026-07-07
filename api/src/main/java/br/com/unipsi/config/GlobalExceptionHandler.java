package br.com.unipsi.config;

import br.com.unipsi.agenda.domain.SlotIndisponivelException;
import br.com.unipsi.auth.domain.CadastroNaoAprovadoException;
import br.com.unipsi.auth.domain.CredenciaisInvalidasException;
import br.com.unipsi.auth.domain.RefreshTokenInvalidoException;
import br.com.unipsi.chatbot.domain.RateLimitExcedidoException;
import br.com.unipsi.prontuario.domain.AcessoProntuarioNegadoException;
import br.com.unipsi.prontuario.domain.CodinomeJaCadastradoException;
import br.com.unipsi.usuario.domain.EmailJaCadastradoException;
import br.com.unipsi.usuario.domain.PacienteNaoElegivelException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<Map<String, String>> handle(EmailJaCadastradoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(PacienteNaoElegivelException.class)
    public ResponseEntity<Map<String, String>> handle(PacienteNaoElegivelException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Map<String, String>> handle(CredenciaisInvalidasException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(CadastroNaoAprovadoException.class)
    public ResponseEntity<Map<String, String>> handle(CadastroNaoAprovadoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(RefreshTokenInvalidoException.class)
    public ResponseEntity<Map<String, String>> handle(RefreshTokenInvalidoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(SlotIndisponivelException.class)
    public ResponseEntity<Map<String, String>> handle(SlotIndisponivelException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(CodinomeJaCadastradoException.class)
    public ResponseEntity<Map<String, String>> handle(CodinomeJaCadastradoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(AcessoProntuarioNegadoException.class)
    public ResponseEntity<Map<String, String>> handle(AcessoProntuarioNegadoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(RateLimitExcedidoException.class)
    public ResponseEntity<Map<String, String>> handle(RateLimitExcedidoException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handle(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handle(MethodArgumentNotValidException e) {
        Map<String, String> erros = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, fe -> fe.getDefaultMessage(), (a, b) -> a));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
    }
}
