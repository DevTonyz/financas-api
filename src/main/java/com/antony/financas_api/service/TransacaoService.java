package com.antony.financas_api.service;

import com.antony.financas_api.dto.MembroResumoDTO;
import com.antony.financas_api.dto.ResumoResponseDTO;
import com.antony.financas_api.dto.TransacaoRequestDTO;
import com.antony.financas_api.dto.TransacaoResponseDTO;
import com.antony.financas_api.model.Categoria;
import com.antony.financas_api.model.EscopoParcelas;
import com.antony.financas_api.model.FormaPagamento;
import com.antony.financas_api.model.Grupo;
import com.antony.financas_api.model.Transacao;
import com.antony.financas_api.model.Usuario;
import com.antony.financas_api.repository.CategoriaRepository;
import com.antony.financas_api.repository.GrupoRepository;
import com.antony.financas_api.repository.TransacaoRepository;
import com.antony.financas_api.repository.UsuarioRepository;
import com.antony.financas_api.exception.AcessoNegadoException;
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
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(dto.categoriaId(), idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Grupo grupo = null;
        if (dto.grupoId() != null) {
            grupo = grupoRepository.findById(dto.grupoId())
                    .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado"));

            boolean fazParte = grupo.getUsuarios().stream()
                    .anyMatch(u -> u.getId().equals(idUsuario));
            if (!fazParte) {
                throw new AcessoNegadoException("Acesso negado: Você não faz parte deste grupo.");
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
            int totalParcelas = transacaoOriginal.getParcelas();
            BigDecimal valorParcela = transacaoOriginal.getValor().divide(
                    new BigDecimal(totalParcelas), 2, RoundingMode.HALF_UP);
            // A última parcela leva a sobra do arredondamento, para a soma bater com o total
            // (ex.: 100 em 3x = 33,33 + 33,33 + 33,34, e não 99,99)
            BigDecimal valorUltima = transacaoOriginal.getValor()
                    .subtract(valorParcela.multiply(new BigDecimal(totalParcelas - 1)));
            // Mesmo id em todas as parcelas: é o que permite editar "esta e as próximas" depois
            String compraId = java.util.UUID.randomUUID().toString();

            for (int i = 0; i < totalParcelas; i++) {
                Transacao novaTransacao = new Transacao();
                novaTransacao.setDescricao(transacaoOriginal.getDescricao() + " " + (i + 1) + "/" + totalParcelas);
                novaTransacao.setValor(i == totalParcelas - 1 ? valorUltima : valorParcela);
                novaTransacao.setCompraId(compraId);
                novaTransacao.setNumeroParcela(i + 1);
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

    // Busca a transação garantindo que é do usuário: 404 se não existe, 403 se é de outra pessoa
    private Transacao buscarDoUsuario(Long id, Long idUsuario) {
        Transacao transacao = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transação não encontrada"));
        if (!transacao.getUsuario().getId().equals(idUsuario)) {
            throw new AcessoNegadoException("Acesso negado.");
        }
        return transacao;
    }

    public TransacaoResponseDTO buscarTransacao(Long id, Long idUsuario) {
        // GET de transação alheia responde 404 (não revela que o id existe)
        Transacao transacao = repository.findById(id)
                .filter(t -> t.getUsuario().getId().equals(idUsuario))
                .orElseThrow(() -> new EntityNotFoundException("Transação não encontrada"));
        return new TransacaoResponseDTO(transacao);
    }

    public TransacaoResponseDTO atualizarTransacao(Long id, TransacaoRequestDTO dto, EscopoParcelas escopo, Long idUsuario) {
        Transacao transacaoExistente = buscarDoUsuario(id, idUsuario);

        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(dto.categoriaId(), idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Grupo grupo = null;
        if (dto.grupoId() != null) {
            grupo = grupoRepository.findById(dto.grupoId())
                    .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado"));

            boolean fazParte = grupo.getUsuarios().stream()
                    .anyMatch(u -> u.getId().equals(idUsuario));
            if (!fazParte) {
                throw new AcessoNegadoException("Acesso negado: Você não faz parte deste grupo.");
            }
        }

        // Gasto comum (ou parcela antiga, sem compraId): edita só ele, como antes
        if (transacaoExistente.getCompraId() == null) {
            transacaoExistente.setDescricao(dto.descricao());
            transacaoExistente.setValor(dto.valor());
            transacaoExistente.setData(dto.data());
            transacaoExistente.setCategoria(categoria);
            transacaoExistente.setFormaPagamento(dto.formaPagamento());
            transacaoExistente.setParcelas(dto.parcelas());
            transacaoExistente.setGrupo(grupo);
            return new TransacaoResponseDTO(repository.save(transacaoExistente));
        }

        if (escopo == EscopoParcelas.TODAS) {
            throw new IllegalArgumentException("Na edição, use ESTA ou PROXIMAS.");
        }

        // Parcela de uma compra: continua no crédito e com o mesmo número de parcelas
        // (o que vier no corpo para esses dois campos é ignorado). O " 2/3" é refeito aqui.
        List<Transacao> alvo = escopo == EscopoParcelas.PROXIMAS
                ? repository.findByCompraIdAndNumeroParcelaGreaterThanEqualOrderByNumeroParcela(
                        transacaoExistente.getCompraId(), transacaoExistente.getNumeroParcela())
                : List.of(transacaoExistente);

        String descricaoBase = TransacaoResponseDTO.semSufixoDeParcela(dto.descricao().trim());
        for (Transacao parcela : alvo) {
            // A data escolhida vale para esta parcela; as seguintes ficam um mês depois da anterior
            int mesesDepois = parcela.getNumeroParcela() - transacaoExistente.getNumeroParcela();
            parcela.setDescricao(descricaoBase + " " + parcela.getNumeroParcela() + "/" + parcela.getParcelas());
            parcela.setValor(dto.valor());
            parcela.setData(dto.data().plusMonths(mesesDepois));
            parcela.setCategoria(categoria);
            parcela.setGrupo(grupo);
        }
        repository.saveAll(alvo);

        return new TransacaoResponseDTO(transacaoExistente);
    }

    // Devolve quantas transações foram apagadas
    public int deletarTransacao(Long id, EscopoParcelas escopo, Long idUsuario) {
        Transacao transacaoExistente = buscarDoUsuario(id, idUsuario);

        List<Transacao> alvo;
        if (transacaoExistente.getCompraId() == null || escopo == EscopoParcelas.ESTA) {
            alvo = List.of(transacaoExistente);
        } else {
            // TODAS começa da parcela 1; PROXIMAS, da parcela escolhida
            int aPartirDe = escopo == EscopoParcelas.TODAS ? 1 : transacaoExistente.getNumeroParcela();
            alvo = repository.findByCompraIdAndNumeroParcelaGreaterThanEqualOrderByNumeroParcela(
                    transacaoExistente.getCompraId(), aPartirDe);
        }
        repository.deleteAll(alvo);
        return alvo.size();
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
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado"));

        // 2. Trava de segurança: você realmente faz parte dessa casa?
        boolean fazParte = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(idUsuario));
        if (!fazParte) {
            throw new AcessoNegadoException("Acesso negado: Você não pode ver as contas deste grupo.");
        }

        // 3. Busca os dados no banco e converte para o DTO limpo
        return repository.findByGrupoId(idGrupo, paginacao).map(TransacaoResponseDTO::new);
    }

    public ResumoResponseDTO obterResumoMensalGrupo(Long idGrupo, Integer mes, Integer ano, Long idUsuario) {
        // 1. Busca e valida o grupo
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado"));

        // 2. Trava de Segurança
        boolean fazParte = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(idUsuario));
        if (!fazParte) {
            throw new AcessoNegadoException("Acesso negado: Você não pode ver o resumo deste grupo.");
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
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado"));

        boolean fazParte = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(idUsuario));
        if (!fazParte) {
            throw new AcessoNegadoException("Acesso negado: Você não pode ver o acerto deste grupo.");
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