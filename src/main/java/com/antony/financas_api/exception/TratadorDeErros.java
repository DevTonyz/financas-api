package com.antony.financas_api.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice // Diz ao Spring: "Se der erro em qualquer Controller, chame esta classe"
public class TratadorDeErros {

    // Intercepta especificamente os erros dos nossos "Guardas" (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosErroValidacao>> tratarErro400(MethodArgumentNotValidException ex) {

        // Pega todos os campos que falharam na validação
        List<FieldError> erros = ex.getFieldErrors();

        // Transforma a lista complexa do Spring na nossa versão enxuta e legível
        List<DadosErroValidacao> errosFormatados = erros.stream()
                .map(DadosErroValidacao::new)
                .toList();

        return ResponseEntity.badRequest().body(errosFormatados);
    }

    // Um 'record' é uma forma moderna e curta do Java para criar classes de transporte de dados
    private record DadosErroValidacao(String campo, String mensagem) {
        public DadosErroValidacao(FieldError erro) {
            // Pega apenas o nome da variável e a mensagem padrão de erro
            this(erro.getField(), erro.getDefaultMessage());
        }
    }
}