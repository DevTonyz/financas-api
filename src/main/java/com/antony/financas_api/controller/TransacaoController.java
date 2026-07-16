package com.antony.financas_api.controller;

import com.antony.financas_api.dto.TransacaoRequestDTO;
import com.antony.financas_api.dto.TransacaoResponseDTO;
import com.antony.financas_api.repository.TransacaoRepository;
import com.antony.financas_api.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    @Autowired
    private TransacaoService service;

    @Autowired
    private TransacaoRepository repository;

    @PostMapping
    public List<TransacaoResponseDTO> criarTransacao(@Valid @RequestBody TransacaoRequestDTO dto) {
        return service.salvarTransacao(dto);
    }

    @GetMapping
    public Page<TransacaoResponseDTO> listarTransacoes(@PageableDefault(size = 10, sort = {"data"}) Pageable paginacao) {
        // O método .map() funciona perfeitamente em Páginas também!
        return repository.findAll(paginacao).map(TransacaoResponseDTO::new);
    }

    @GetMapping("/categoria/{id}")
    public Page<TransacaoResponseDTO> listarPorCategoria(@PathVariable Long id, @PageableDefault(size = 10) Pageable paginacao) {
        return repository.findByCategoriaId(id, paginacao).map(TransacaoResponseDTO::new);
    }

    @GetMapping("/periodo")
    public Page<TransacaoResponseDTO> listarPorPeriodo(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim,
            @PageableDefault(size = 10, sort = {"data"}) Pageable paginacao) {

        return repository.findByDataBetween(inicio, fim, paginacao).map(TransacaoResponseDTO::new);
    }

    @PutMapping("/{id}")
    public TransacaoResponseDTO atualizarTransacao(@PathVariable Long id, @Valid @RequestBody TransacaoRequestDTO dto) {
        return service.atualizarTransacao(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTransacao(@PathVariable Long id) {
        service.deletarTransacao(id);
        // Retorna o status 204 (Sucesso, mas sem conteúdo na tela, já que foi apagado)
        return ResponseEntity.noContent().build();
    }
}