package com.antony.financas_api.controller;

import com.antony.financas_api.model.Transacao;
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

    // Injetamos o Service para cuidar da lógica complexa de parcelamento (POST)
    @Autowired
    private TransacaoService service;

    // Injetamos o Repository de volta apenas para fazer buscas simples no banco (GET)
    @Autowired
    private TransacaoRepository repository;

    @PostMapping
    public List<Transacao> criarTransacao(@Valid @RequestBody Transacao transacao) {
        // O garçom entrega o pedido para a cozinha (Service)
        return service.salvarTransacao(transacao);
    }

    @GetMapping
    public List<Transacao> listarTransacoes() {
        // Busca direto no banco e devolve a lista
        return repository.findAll();
    }

    // O @PathVariable pega o número que você digitar na URL e joga na variável 'id'
    @GetMapping("/categoria/{id}")
    public List<Transacao> listarPorCategoria(@PathVariable Long id) {
        return repository.findByCategoriaId(id);
    }

    // Exemplo de URL: localhost:8080/transacoes/periodo?inicio=2026-07-01&fim=2026-07-31
    @GetMapping("/periodo")
    public List<Transacao> listarPorPeriodo(@RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
        return repository.findByDataBetween(inicio, fim);
    }

    // Exemplo de URL: localhost:8080/transacoes/total?inicio=2026-07-01&fim=2026-07-31
    @GetMapping("/total")
    public BigDecimal totalPorPeriodo(@RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
        return repository.somarGastosPorPeriodo(inicio, fim);
    }
}