package com.projeto.raizesnordeste.presentation.exceptions;

import com.projeto.raizesnordeste.infrastructure.logging.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PAYMENT_REQUIRED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> handleDataIntegrityViolationException(final DataIntegrityViolationException ex, final HttpServletRequest request) {
        String requestId = requestId();
        log.warn("Violação de integridade de dados em {} {} [requestId={}]: {}",
                request.getMethod(), request.getRequestURI(), requestId, ex.getMessage());

        return ResponseEntity.status(CONFLICT).body(
                StandardError.builder()
                        .requestId(requestId)
                        .timestamp(now())
                        .status(CONFLICT.value())
                        .error(CONFLICT.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<StandardError> handleUsuarioJaExisteException(final BusinessRuleException ex, final HttpServletRequest request) {
        String requestId = requestId();
        log.warn("Regra de negócio violada em {} {} [requestId={}]: {}",
                request.getMethod(), request.getRequestURI(), requestId, ex.getMessage());

        return ResponseEntity.status(CONFLICT).body(StandardError.builder()
                .requestId(requestId)
                .timestamp(now())
                .status(CONFLICT.value())
                .error(CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<StandardError> handleUsuarioNotFoundException(final ResourceNotFoundException ex, final HttpServletRequest request) {
        String requestId = requestId();
        log.info("Recurso não encontrado em {} {} [requestId={}]: {}",
                request.getMethod(), request.getRequestURI(), requestId, ex.getMessage());

        return ResponseEntity.status(NOT_FOUND).body(StandardError.builder()
                .requestId(requestId)
                .timestamp(now())
                .status(NOT_FOUND.value())
                .error(NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<StandardError> handleBadCredentialsException(final BadCredentialsException ex, final HttpServletRequest request) {
        String requestId = requestId();
        log.warn("Tentativa de autenticação inválida em {} {} [requestId={}]", request.getMethod(), request.getRequestURI(), requestId);

        return ResponseEntity.status(UNAUTHORIZED).body(StandardError.builder()
                .requestId(requestId)
                .timestamp(now())
                .status(UNAUTHORIZED.value())
                .error(UNAUTHORIZED.getReasonPhrase())
                .message("Email ou senha inválidos")
                .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(PaymentRequiredException.class)
    ResponseEntity<StandardError> handlePaymentRequiredException(final PaymentRequiredException ex, final HttpServletRequest request) {
        String requestId = requestId();
        log.warn("Pagamento recusado em {} {} [requestId={}]: {}",
                request.getMethod(), request.getRequestURI(), requestId, ex.getMessage());

        return ResponseEntity.status(PAYMENT_REQUIRED).body(StandardError.builder()
                .requestId(requestId)
                .timestamp(now())
                .status(PAYMENT_REQUIRED.value())
                .error(PAYMENT_REQUIRED.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ValidationException> handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex, final HttpServletRequest request) {
        String requestId = requestId();

        var error = ValidationException.builder()
                .requestId(requestId)
                .timestamp(now())
                .status(BAD_REQUEST.value())
                .error("Validation Exception")
                .message("Exception in validation attributes")
                .path(request.getRequestURI())
                .build();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            error.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.info("Erro de validação em {} {} [requestId={}]: {}",
                request.getMethod(), request.getRequestURI(), requestId, error.getDetails());

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Reaproveita o traceId gerado pelo {@link TraceIdFilter} como requestId da resposta de erro,
     * de forma que o valor exposto ao cliente seja o mesmo usado para localizar os logs da requisição.
     * Faz fallback para um UUID novo caso o filtro não tenha sido executado (ex.: testes unitários).
     */
    private String requestId() {
        String traceId = ThreadContext.get(TraceIdFilter.TRACE_ID_MDC_KEY);
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }

}
