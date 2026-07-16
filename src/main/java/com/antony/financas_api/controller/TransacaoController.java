package com.antony.financas_api.controller;

import com.antony.financas_api.model.Transacao;
import com.antony.financas_api.repository.TransacaoRepository;
import com.antony.financas_api.service.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public List<Transacao> criarTransacao(@RequestBody Transacao transacao) {
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
}