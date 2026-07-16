package com.antony.financas_api.controller;

import com.antony.financas_api.dto.TransacaoRequestDTO;
import com.antony.financas_api.dto.TransacaoResponseDTO;
import com.antony.financas_api.repository.TransacaoRepository;
import com.antony.financas_api.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public List<TransacaoResponseDTO> listarTransacoes() {
        // Busca do banco e transforma no DTO antes de devolver para o aplicativo
        return repository.findAll().stream().map(TransacaoResponseDTO::new).toList();
    }

    @GetMapping("/categoria/{id}")
    public List<TransacaoResponseDTO> listarPorCategoria(@PathVariable Long id) {
        return repository.findByCategoriaId(id).stream().map(TransacaoResponseDTO::new).toList();
    }

    @GetMapping("/periodo")
    public List<TransacaoResponseDTO> listarPorPeriodo(@RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
        return repository.findByDataBetween(inicio, fim).stream().map(TransacaoResponseDTO::new).toList();
    }

    @GetMapping("/total")
    public BigDecimal totalPorPeriodo(@RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
        return repository.somarGastosPorPeriodo(inicio, fim);
    }
}