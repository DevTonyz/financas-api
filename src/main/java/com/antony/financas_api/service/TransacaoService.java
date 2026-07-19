package com.antony.financas_api.service;

import com.antony.financas_api.dto.TransacaoRequestDTO;
import com.antony.financas_api.dto.TransacaoResponseDTO;
import com.antony.financas_api.model.Categoria;
import com.antony.financas_api.model.FormaPagamento;
import com.antony.financas_api.model.Transacao;
import com.antony.financas_api.model.Usuario;
import com.antony.financas_api.repository.CategoriaRepository;
import com.antony.financas_api.repository.TransacaoRepository;
import com.antony.financas_api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.antony.financas_api.dto.ResumoResponseDTO;
import java.time.LocalDate;
import java.time.YearMonth;

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

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<TransacaoResponseDTO> salvarTransacao(TransacaoRequestDTO dto, Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada!"));

        Transacao transacaoOriginal = new Transacao();
        transacaoOriginal.setDescricao(dto.descricao());
        transacaoOriginal.setValor(dto.valor());
        transacaoOriginal.setData(dto.data());
        transacaoOriginal.setCategoria(categoria);
        transacaoOriginal.setFormaPagamento(dto.formaPagamento());
        transacaoOriginal.setParcelas(dto.parcelas());
        transacaoOriginal.setUsuario(usuario);

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
                novaTransacao.setUsuario(usuario);

                transacoesSalvas.add(repository.save(novaTransacao));
            }
        } else {
            transacoesSalvas.add(repository.save(transacaoOriginal));
        }

        return transacoesSalvas.stream().map(TransacaoResponseDTO::new).toList();
    }

    public TransacaoResponseDTO atualizarTransacao(Long id, TransacaoRequestDTO dto, Long idUsuario) {
        Transacao transacaoExistente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada!"));

        if (!transacaoExistente.getUsuario().getId().equals(idUsuario)) {
            throw new RuntimeException("Acesso negado.");
        }

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada!"));

        transacaoExistente.setDescricao(dto.descricao());
        transacaoExistente.setValor(dto.valor());
        transacaoExistente.setData(dto.data());
        transacaoExistente.setCategoria(categoria);
        transacaoExistente.setFormaPagamento(dto.formaPagamento());
        transacaoExistente.setParcelas(dto.parcelas());

        return new TransacaoResponseDTO(repository.save(transacaoExistente));
    }

    public void deletarTransacao(Long id, Long idUsuario) {
        Transacao transacaoExistente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada!"));

        if (!transacaoExistente.getUsuario().getId().equals(idUsuario)) {
            throw new RuntimeException("Acesso negado.");
        }

        repository.delete(transacaoExistente);
    }

    public ResumoResponseDTO obterResumoMensal(Integer mes, Integer ano, Long idUsuario) {

        // A mágica do Java para calcular datas automaticamente
        YearMonth anoMes = YearMonth.of(ano, mes);
        LocalDate dataInicio = anoMes.atDay(1);
        LocalDate dataFim = anoMes.atEndOfMonth();

        // Chama a sua Query customizada no banco, passando o filtro do usuário
        BigDecimal totalGasto = repository.somarGastosPorPeriodo(dataInicio, dataFim, idUsuario);

        // Se você não gastou nada no mês, o banco devolve 'null'.
        // Tratamos isso para devolver 0 e não quebrar o aplicativo.
        if (totalGasto == null) {
            totalGasto = BigDecimal.ZERO;
        }

        return new ResumoResponseDTO(mes, ano, totalGasto);
    }
}