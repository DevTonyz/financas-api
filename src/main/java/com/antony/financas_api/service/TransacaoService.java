package com.antony.financas_api.service;

import com.antony.financas_api.model.FormaPagamento;
import com.antony.financas_api.model.Transacao;
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

    public List<Transacao> salvarTransacao(Transacao transacaoOriginal) {

        List<Transacao> transacoesSalvas = new ArrayList<>();

        if (transacaoOriginal.getFormaPagamento() == FormaPagamento.CREDITO && transacaoOriginal.getParcelas() > 1) {

            // Divide o valor total pelo número de parcelas, arredondando os centavos corretamente
            BigDecimal valorParcela = transacaoOriginal.getValor().divide(
                    new BigDecimal(transacaoOriginal.getParcelas()), 2, RoundingMode.HALF_UP);

            // Cria um registro no banco para cada parcela
            for (int i = 0; i < transacaoOriginal.getParcelas(); i++) {
                Transacao novaTransacao = new Transacao();

                // Formata a descrição para mostrar a parcela atual (ex: "Pneu Twister 1/3")
                novaTransacao.setDescricao(transacaoOriginal.getDescricao() + " " + (i + 1) + "/" + transacaoOriginal.getParcelas());
                novaTransacao.setValor(valorParcela);
                // Joga a data de cobrança para o mês seguinte a cada iteração do laço
                novaTransacao.setData(transacaoOriginal.getData().plusMonths(i));
                novaTransacao.setCategoria(transacaoOriginal.getCategoria());
                novaTransacao.setFormaPagamento(transacaoOriginal.getFormaPagamento());
                novaTransacao.setParcelas(transacaoOriginal.getParcelas());

                // Salva no banco e adiciona na lista de retorno
                transacoesSalvas.add(repository.save(novaTransacao));
            }
        } else {
            // Se for Pix, Débito, ou Crédito em 1x, salva a transação original diretamente
            transacoesSalvas.add(repository.save(transacaoOriginal));
        }

        return transacoesSalvas;
    }
}