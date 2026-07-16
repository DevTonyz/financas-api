package com.antony.financas_api.controller;

import com.antony.financas_api.model.Transacao;
import com.antony.financas_api.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transacoes") // O endereço agora é localhost:8080/transacoes
public class TransacaoController {

    @Autowired
    private TransacaoRepository repository;

    @PostMapping
    public Transacao criarTransacao(@RequestBody Transacao transacao) {
        return repository.save(transacao);
    }

    @GetMapping
    public List<Transacao> listarTransacoes() {
        return repository.findAll();
    }
}