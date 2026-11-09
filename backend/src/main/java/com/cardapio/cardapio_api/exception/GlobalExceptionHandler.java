package com.cardapio.cardapio_api.exception;

import com.cardapio.cardapio_api.dto.ErroResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Transforma os erros da API em respostas JSON no mesmo formato (ErroResponse),
 * em vez de páginas de erro ou mensagens soltas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Erros que o próprio código lança de propósito (ex.: 404 item não encontrado). */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> tratarStatus(ResponseStatusException ex) {
        int status = ex.getStatusCode().value();
        String mensagem = ex.getReason() != null ? ex.getReason() : "Erro ao processar a requisição";
        return ResponseEntity.status(status).body(new ErroResponse(status, mensagem, List.of()));
    }

    /** Dados enviados que não passaram nas validações (campo vazio, quantidade 0...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ErroResponse(400, "Dados inválidos", detalhes));
    }

    /** JSON malformado ou com tipo errado (ex.: texto onde devia ser número). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
                new ErroResponse(400, "JSON inválido ou campo com tipo incorreto", List.of()));
    }
}
