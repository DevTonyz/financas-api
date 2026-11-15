package com.antony.financas_api.service;

import com.antony.financas_api.dto.AcertoDeContasDTO;
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
    private GrupoService grupoService;

    public List<TransacaoResponseDTO> salvarTransacao(TransacaoRequestDTO dto, Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(dto.categoriaId(), idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Grupo grupo = null;
        if (dto.grupoId() != null) {
            grupo = grupoService.buscarDoMembro(dto.grupoId(), idUsuario);
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
            grupo = grupoService.buscarDoMembro(dto.grupoId(), idUsuario);
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

    // Sem datas: todas as transações do grupo. Com inicio e fim: só as do período (o app usa o mês)
    public Page<TransacaoResponseDTO> listarPorGrupo(Long idGrupo, LocalDate inicio, LocalDate fim, Long idUsuario, Pageable paginacao) {
        grupoService.buscarDoMembro(idGrupo, idUsuario);
        Page<Transacao> pagina = (inicio != null && fim != null)
                ? repository.findByGrupoIdAndDataBetween(idGrupo, inicio, fim, paginacao)
                : repository.findByGrupoId(idGrupo, paginacao);
        return pagina.map(TransacaoResponseDTO::new);
    }

    public ResumoResponseDTO obterResumoMensalGrupo(Long idGrupo, Integer mes, Integer ano, Long idUsuario) {
        grupoService.buscarDoMembro(idGrupo, idUsuario);
        YearMonth anoMes = YearMonth.of(ano, mes);
        BigDecimal totalGasto = repository.somarGastosPorGrupoEPeriodo(idGrupo, anoMes.atDay(1), anoMes.atEndOfMonth());
        // Sem gastos o banco devolve null; mandamos zero para não dar erro na tela
        return new ResumoResponseDTO(mes, ano, totalGasto != null ? totalGasto : BigDecimal.ZERO);
    }

    // Quanto cada membro pagou no mês e o saldo dele em relação à parte justa (total ÷ membros).
    // Saldo positivo = tem a receber; negativo = deve. A soma dos saldos é sempre 0
    public AcertoDeContasDTO obterAcertoDeContas(Long idGrupo, Integer mes, Integer ano, Long idUsuario) {
        Grupo grupo = grupoService.buscarDoMembro(idGrupo, idUsuario);
        YearMonth anoMes = YearMonth.of(ano, mes);
        List<Transacao> transacoes = repository.findAllByGrupoIdAndDataBetween(idGrupo, anoMes.atDay(1), anoMes.atEndOfMonth());

        // Começa todo membro com 0, para quem não pagou nada também aparecer
        List<Usuario> membros = grupo.getUsuarios().stream()
                .sorted(java.util.Comparator.comparing(Usuario::getLogin)).toList();
        java.util.Map<Long, BigDecimal> pagoPorMembro = new java.util.LinkedHashMap<>();
        membros.forEach(m -> pagoPorMembro.put(m.getId(), BigDecimal.ZERO));
        for (Transacao t : transacoes) {
            // merge soma o valor ao que o membro já tinha
            pagoPorMembro.merge(t.getUsuario().getId(), t.getValor(), BigDecimal::add);
        }

        BigDecimal total = transacoes.stream().map(Transacao::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        int n = membros.size();
        BigDecimal parteJusta = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);

        // Divisão em centavos: 100 entre 3 = 33,34 + 33,33 + 33,33. Os centavos que sobram vão para
        // os primeiros da lista, assim as partes somam exatamente o total e os saldos somam 0
        long totalCentavos = total.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        long baseCentavos = totalCentavos / n;
        long sobra = totalCentavos % n;

        List<MembroResumoDTO> linhas = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Usuario membro = membros.get(i);
            BigDecimal parte = BigDecimal.valueOf(baseCentavos + (i < sobra ? 1 : 0), 2);
            BigDecimal pago = pagoPorMembro.get(membro.getId()).setScale(2, RoundingMode.HALF_UP);
            linhas.add(new MembroResumoDTO(membro.getId(), membro.getLogin(),
                    membro.getId().equals(idUsuario), pago, pago.subtract(parte)));
        }
        return new AcertoDeContasDTO(mes, ano, total.setScale(2, RoundingMode.HALF_UP), parteJusta, linhas);
    }
}
