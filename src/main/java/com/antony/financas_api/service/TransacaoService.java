package com.antony.financas_api.service;

import com.antony.financas_api.dto.TransacaoRequestDTO;
import com.antony.financas_api.dto.TransacaoResponseDTO;
import com.antony.financas_api.model.Categoria;
import com.antony.financas_api.model.FormaPagamento;
import com.antony.financas_api.model.Transacao;
import com.antony.financas_api.repository.CategoriaRepository;
import com.antony.financas_api.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository repository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Repare que agora a saída é TransacaoResponseDTO
    public List<TransacaoResponseDTO> salvarTransacao(TransacaoRequestDTO dto) {

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada!"));

        Transacao transacaoOriginal = new Transacao();
        transacaoOriginal.setDescricao(dto.descricao());
        transacaoOriginal.setValor(dto.valor());
        transacaoOriginal.setData(dto.data());
        transacaoOriginal.setCategoria(categoria);
        transacaoOriginal.setFormaPagamento(dto.formaPagamento());
        transacaoOriginal.setParcelas(dto.parcelas());

        List<Transacao> transacoesSalvas = new ArrayList<>();

        if (transacaoOriginal.getFormaPagamento() == FormaPagamento.CREDITO && transacaoOriginal.getParcelas() > 1) {
            BigDecimal valorParcela = transacaoOriginal.getValor().divide(
                    new BigDecimal(transacaoOriginal.getParcelas()), 2, RoundingMode.HALF_UP);

            for (int i = 0; i < transacaoOriginal.getParcelas(); i++) {
                Transacao novaTransacao = new Transacao();
                novaTransacao.setDescricao(transacaoOriginal.getDescricao() + " " + (i + 1) + "/" + transacaoOriginal.getParcelas());
                novaTransacao.setValor(valorParcela);
                novaTransacao.setData(transacaoOriginal.getData().plusMonths(i));
                novaTransacao.setCategoria(transacaoOriginal.getCategoria());
                novaTransacao.setFormaPagamento(transacaoOriginal.getFormaPagamento());
                novaTransacao.setParcelas(transacaoOriginal.getParcelas());

                transacoesSalvas.add(repository.save(novaTransacao));
            }
        } else {
            transacoesSalvas.add(repository.save(transacaoOriginal));
        }

        // A MÁGICA FINAL: Transforma a lista de Transacoes do banco na lista de ResponseDTOs
        return transacoesSalvas.stream().map(TransacaoResponseDTO::new).toList();
    }
}