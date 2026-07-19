package com.antony.financas_api.controller;

import com.antony.financas_api.dto.TransacaoRequestDTO;
import com.antony.financas_api.dto.TransacaoResponseDTO;
import com.antony.financas_api.repository.TransacaoRepository;
import com.antony.financas_api.service.TransacaoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import com.antony.financas_api.dto.ResumoResponseDTO;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transacoes")
@SecurityRequirement(name = "bearer-key")
public class TransacaoController {

    @Autowired
    private TransacaoService service;

    @Autowired
    private TransacaoRepository repository;

    @PostMapping
    public ResponseEntity<List<TransacaoResponseDTO>> criarTransacao(
            @Valid @RequestBody TransacaoRequestDTO dto,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        List<TransacaoResponseDTO> transacoes = service.salvarTransacao(dto, idUsuario);
        return ResponseEntity.status(201).body(transacoes);
    }

    @GetMapping
    public ResponseEntity<Page<TransacaoResponseDTO>> listarTransacoes(
            @ParameterObject @PageableDefault(size = 10, sort = {"data"}) Pageable paginacao,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        Page<TransacaoResponseDTO> page = repository.findAllByUsuarioId(idUsuario, paginacao).map(TransacaoResponseDTO::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<Page<TransacaoResponseDTO>> listarPorCategoria(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 10) Pageable paginacao,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        Page<TransacaoResponseDTO> page = repository.findByCategoriaIdAndUsuarioId(id, idUsuario, paginacao).map(TransacaoResponseDTO::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/periodo")
    public ResponseEntity<Page<TransacaoResponseDTO>> listarPorPeriodo(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim,
            @ParameterObject @PageableDefault(size = 10, sort = {"data"}) Pageable paginacao,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        Page<TransacaoResponseDTO> page = repository.findByDataBetweenAndUsuarioId(inicio, fim, idUsuario, paginacao).map(TransacaoResponseDTO::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoResponseDTO> obterResumo(
            @RequestParam Integer mes,
            @RequestParam Integer ano,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        ResumoResponseDTO resumo = service.obterResumoMensal(mes, ano, idUsuario);
        return ResponseEntity.ok(resumo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> atualizarTransacao(
            @PathVariable Long id,
            @Valid @RequestBody TransacaoRequestDTO dto,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        TransacaoResponseDTO transacaoAtualizada = service.atualizarTransacao(id, dto, idUsuario);
        return ResponseEntity.ok(transacaoAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTransacao(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        service.deletarTransacao(id, idUsuario);
        return ResponseEntity.noContent().build();
    }
}