package com.antony.financas_api.service;

import com.antony.financas_api.dto.MembroResumoDTO;
import com.antony.financas_api.dto.ResumoResponseDTO;
import com.antony.financas_api.dto.TransacaoRequestDTO;
import com.antony.financas_api.dto.TransacaoResponseDTO;
import com.antony.financas_api.model.Categoria;
import com.antony.financas_api.model.FormaPagamento;
import com.antony.financas_api.model.Grupo;
import com.antony.financas_api.model.Transacao;
import com.antony.financas_api.model.Usuario;
import com.antony.financas_api.repository.CategoriaRepository;
import com.antony.financas_api.repository.GrupoRepository;
import com.antony.financas_api.repository.TransacaoRepository;
import com.antony.financas_api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
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

    @Autowired
    private GrupoRepository grupoRepository;

    public List<TransacaoResponseDTO> salvarTransacao(TransacaoRequestDTO dto, Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(dto.categoriaId(), idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Grupo grupo = null;
        if (dto.grupoId() != null) {
            grupo = grupoRepository.findById(dto.grupoId())
                    .orElseThrow(() -> new RuntimeException("Grupo não encontrado!"));

            boolean fazParte = grupo.getUsuarios().stream()
                    .anyMatch(u -> u.getId().equals(idUsuario));
            if (!fazParte) {
                throw new RuntimeException("Acesso negado: Você não faz parte deste grupo.");
            }
        }

        Transacao transacaoOriginal = new Transacao();
        transacaoOriginal.setDescricao(dto.descricao());
        transacaoOriginal.setValor(dto.valor());
        transacaoOriginal.setData(dto.data());
        transacaoOriginal.setCategoria(categoria);
        transacaoOriginal.setFormaPagamento(dto.formaPagamento());
        transacaoOriginal.setParcelas(dto.parcelas());
        transacaoOriginal.setUsuario(usuario);
        transacaoOriginal.setGrupo(grupo);

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
                novaTransacao.setGrupo(grupo);

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

        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(dto.categoriaId(), idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Grupo grupo = null;
        if (dto.grupoId() != null) {
            grupo = grupoRepository.findById(dto.grupoId())
                    .orElseThrow(() -> new RuntimeException("Grupo não encontrado!"));

            boolean fazParte = grupo.getUsuarios().stream()
                    .anyMatch(u -> u.getId().equals(idUsuario));
            if (!fazParte) {
                throw new RuntimeException("Acesso negado: Você não faz parte deste grupo.");
            }
        }

        transacaoExistente.setDescricao(dto.descricao());
        transacaoExistente.setValor(dto.valor());
        transacaoExistente.setData(dto.data());
        transacaoExistente.setCategoria(categoria);
        transacaoExistente.setFormaPagamento(dto.formaPagamento());
        transacaoExistente.setParcelas(dto.parcelas());
        transacaoExistente.setGrupo(grupo);

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
        YearMonth anoMes = YearMonth.of(ano, mes);
        LocalDate dataInicio = anoMes.atDay(1);
        LocalDate dataFim = anoMes.atEndOfMonth();

        BigDecimal totalGasto = repository.somarGastosPorPeriodo(dataInicio, dataFim, idUsuario);

        if (totalGasto == null) {
            totalGasto = BigDecimal.ZERO;
        }

        return new ResumoResponseDTO(mes, ano, totalGasto);
    }

    public Page<TransacaoResponseDTO> listarPorGrupo(Long idGrupo, Long idUsuario, Pageable paginacao) {
        // 1. Busca o grupo
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado!"));

        // 2. Trava de segurança: você realmente faz parte dessa casa?
        boolean fazParte = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(idUsuario));
        if (!fazParte) {
            throw new RuntimeException("Acesso negado: Você não pode ver as contas deste grupo.");
        }

        // 3. Busca os dados no banco e converte para o DTO limpo
        return repository.findByGrupoId(idGrupo, paginacao).map(TransacaoResponseDTO::new);
    }

    public ResumoResponseDTO obterResumoMensalGrupo(Long idGrupo, Integer mes, Integer ano, Long idUsuario) {
        // 1. Busca e valida o grupo
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado!"));

        // 2. Trava de Segurança
        boolean fazParte = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(idUsuario));
        if (!fazParte) {
            throw new RuntimeException("Acesso negado: Você não pode ver o resumo deste grupo.");
        }

        // 3. Monta o calendário do mês
        YearMonth anoMes = YearMonth.of(ano, mes);
        LocalDate dataInicio = anoMes.atDay(1);
        LocalDate dataFim = anoMes.atEndOfMonth();

        // 4. Manda o banco somar tudo
        BigDecimal totalGasto = repository.somarGastosPorGrupoEPeriodo(idGrupo, dataInicio, dataFim);

        // 5. Se não houver gastos, retorna zero para não dar erro na tela
        if (totalGasto == null) {
            totalGasto = BigDecimal.ZERO;
        }

        return new ResumoResponseDTO(mes, ano, totalGasto);
    }

    public java.util.List<MembroResumoDTO> obterAcertoDeContas(Long idGrupo, Integer mes, Integer ano, Long idUsuario) {
        // 1. Busca e valida o grupo (mesma regra de segurança)
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado!"));

        boolean fazParte = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(idUsuario));
        if (!fazParte) {
            throw new RuntimeException("Acesso negado: Você não pode ver o acerto deste grupo.");
        }

        // 2. Monta o calendário
        java.time.YearMonth anoMes = java.time.YearMonth.of(ano, mes);
        java.time.LocalDate dataInicio = anoMes.atDay(1);
        java.time.LocalDate dataFim = anoMes.atEndOfMonth();

        // 3. Busca todas as transações do mês
        java.util.List<Transacao> transacoes = repository.findAllByGrupoIdAndDataBetween(idGrupo, dataInicio, dataFim);

        // 4. Agrupa por usuário e soma o valor de cada um (Inteligência do Java Streams)
        java.util.Map<Usuario, BigDecimal> gastosPorUsuario = transacoes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Transacao::getUsuario,
                        java.util.stream.Collectors.mapping(
                                Transacao::getValor,
                                java.util.stream.Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        // 5. Converte o resultado para a nossa lista de DTOs limpa
        return gastosPorUsuario.entrySet().stream()
                .map(entry -> new MembroResumoDTO(
                        entry.getKey().getId(),
                        entry.getKey().getLogin(),
                        entry.getValue()))
                .toList();
    }
}