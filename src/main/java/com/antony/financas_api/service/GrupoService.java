package com.antony.financas_api.service;

import com.antony.financas_api.dto.GrupoDetalheDTO;
import com.antony.financas_api.dto.GrupoRequestDTO;
import com.antony.financas_api.dto.GrupoResponseDTO;
import com.antony.financas_api.dto.PagamentoDTOs.PagamentoRequest;
import com.antony.financas_api.dto.PagamentoDTOs.PagamentoResponse;
import com.antony.financas_api.dto.PagamentoDTOs.SaldoDoMembro;
import com.antony.financas_api.dto.PagamentoDTOs.SaldosResponse;
import com.antony.financas_api.dto.PagamentoDTOs.Sugestao;
import com.antony.financas_api.model.EntradaNoGrupo;
import com.antony.financas_api.model.Pagamento;
import com.antony.financas_api.model.Transacao;
import com.antony.financas_api.repository.EntradaNoGrupoRepository;
import com.antony.financas_api.repository.PagamentoRepository;
import com.antony.financas_api.exception.AcessoNegadoException;
import com.antony.financas_api.exception.ConflitoException;
import com.antony.financas_api.model.Grupo;
import com.antony.financas_api.model.Usuario;
import com.antony.financas_api.repository.GrupoRepository;
import com.antony.financas_api.repository.TransacaoRepository;
import com.antony.financas_api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Regras de grupo num lugar só. A principal: só quem é membro vê ou mexe no grupo
@Service
public class GrupoService {

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private EntradaNoGrupoRepository entradaRepository;

    // 404 se o grupo não existe, 403 se existe mas o usuário não é membro
    public Grupo buscarDoMembro(Long idGrupo, Long idUsuario) {
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado"));
        boolean fazParte = grupo.getUsuarios().stream().anyMatch(u -> u.getId().equals(idUsuario));
        if (!fazParte) {
            throw new AcessoNegadoException("Acesso negado: você não faz parte deste grupo.");
        }
        return grupo;
    }

    public GrupoResponseDTO criar(GrupoRequestDTO dto, Long idUsuario) {
        Usuario criador = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        Grupo grupo = new Grupo();
        grupo.setNome(dto.nome().trim());
        grupo.setDescricao(dto.descricao() == null || dto.descricao().isBlank() ? null : dto.descricao().trim());
        // Quem cria já entra como membro (o JPA preenche a tabela grupo_usuarios)
        grupo.getUsuarios().add(criador);
        // O criador não ganha data de entrada: conta como "desde sempre" e divide até gastos lançados
        // com data anterior à criação do grupo (comum para registrar despesas que já aconteceram)
        return new GrupoResponseDTO(grupoRepository.save(grupo));
    }

    public List<GrupoResponseDTO> listarDoUsuario(Long idUsuario) {
        return grupoRepository.findByUsuariosIdOrderByNomeAsc(idUsuario).stream()
                .map(GrupoResponseDTO::new).toList();
    }

    public GrupoDetalheDTO detalhar(Long idGrupo, Long idUsuario) {
        return new GrupoDetalheDTO(buscarDoMembro(idGrupo, idUsuario), idUsuario, entradas(idGrupo));
    }

    @Transactional
    // dataEntrada é opcional (padrão: hoje). Serve para quem entrou uns dias antes de ser adicionado no app
    public GrupoDetalheDTO adicionarMembroPorLogin(Long idGrupo, String login, LocalDate dataEntrada, Long idUsuario) {
        Grupo grupo = buscarDoMembro(idGrupo, idUsuario);
        Usuario novoMembro = usuarioRepository.findFirstByLoginIgnoreCase(login.trim())
                .orElseThrow(() -> new EntityNotFoundException("Nenhuma conta com o e-mail " + login.trim()));
        if (grupo.getUsuarios().contains(novoMembro)) {
            throw new ConflitoException(novoMembro.getLogin() + " já faz parte do grupo");
        }
        LocalDate entrada = dataEntrada != null ? dataEntrada : LocalDate.now();
        if (entrada.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de entrada não pode ser no futuro.");
        }
        grupo.getUsuarios().add(novoMembro);
        Grupo salvo = grupoRepository.save(grupo);
        entradaRepository.save(new EntradaNoGrupo(idGrupo, novoMembro.getId(), entrada));
        return new GrupoDetalheDTO(salvo, idUsuario, entradas(idGrupo));
    }

    // Mantido para quem já usava a rota antiga, que recebe o id do usuário
    @Transactional
    public GrupoResponseDTO adicionarMembroPorId(Long idGrupo, Long idNovoMembro, Long idUsuario) {
        Grupo grupo = buscarDoMembro(idGrupo, idUsuario);
        Usuario novoMembro = usuarioRepository.findById(idNovoMembro)
                .orElseThrow(() -> new EntityNotFoundException("Usuário a ser adicionado não encontrado"));
        if (grupo.getUsuarios().add(novoMembro)) {
            entradaRepository.save(new EntradaNoGrupo(idGrupo, novoMembro.getId(), LocalDate.now()));
        }
        return new GrupoResponseDTO(grupoRepository.save(grupo));
    }

    // @Transactional: desligar os gastos, sair e (se for o caso) apagar o grupo acontecem juntos ou nada acontece
    @Transactional
    public void sair(Long idGrupo, Long idUsuario) {
        Grupo grupo = buscarDoMembro(idGrupo, idUsuario);
        // Não dá para sair devendo (ou com algo a receber): primeiro acerta, depois sai.
        // Com um membro só, o saldo é sempre 0
        BigDecimal meuSaldo = calcularSaldos(grupo, idUsuario).membros().stream()
                .filter(SaldoDoMembro::voce).findFirst().map(SaldoDoMembro::saldo).orElse(BigDecimal.ZERO);
        if (meuSaldo.signum() != 0) {
            throw new ConflitoException(meuSaldo.signum() < 0
                    ? "Você ainda deve " + reais(meuSaldo.abs()) + " no grupo. Registre o pagamento antes de sair."
                    : "Você ainda tem " + reais(meuSaldo) + " a receber no grupo. Acerte as contas antes de sair.");
        }
        // Os acertos em que a pessoa aparece deixam de valer junto com a saída
        pagamentoRepository.apagarDoMembro(idGrupo, idUsuario);
        // Se voltar um dia, entra com a data nova
        entradaRepository.apagarDoMembro(idGrupo, idUsuario);
        // Os gastos de quem sai deixam de ser do grupo (continuam na conta da pessoa).
        // Assim o acerto de contas só envolve quem ainda é membro e os saldos continuam somando 0
        transacaoRepository.desligarDoGrupo(idGrupo, idUsuario);
        grupo.getUsuarios().removeIf(u -> u.getId().equals(idUsuario));
        if (grupo.getUsuarios().isEmpty()) {
            // Último a sair: o grupo é apagado. Antes, solta qualquer gasto que ainda aponte para ele
            // (senão a chave estrangeira impede apagar)
            transacaoRepository.desligarTodasDoGrupo(idGrupo);
            pagamentoRepository.apagarDoGrupo(idGrupo);
            entradaRepository.apagarDoGrupo(idGrupo);
            grupoRepository.delete(grupo);
        } else {
            grupoRepository.save(grupo);
        }
    }

    // --- Saldo acumulado (todos os meses) e pagamentos entre membros ---

    public SaldosResponse saldos(Long idGrupo, Long idUsuario) {
        return calcularSaldos(buscarDoMembro(idGrupo, idUsuario), idUsuario);
    }

    // id do usuário → data em que entrou. Quem não aparece já era membro antes desta regra e divide tudo
    public Map<Long, LocalDate> entradas(Long idGrupo) {
        Map<Long, LocalDate> mapa = new HashMap<>();
        entradaRepository.findAllByGrupoId(idGrupo).forEach(e -> mapa.put(e.getUsuarioId(), e.getDataEntrada()));
        return mapa;
    }

    // Parte justa de cada membro num conjunto de gastos, respeitando a data de entrada (ver DivisaoPorEntrada)
    public Map<Long, BigDecimal> partesJustas(Grupo grupo, List<Usuario> membrosEmOrdem, List<Transacao> gastos) {
        return DivisaoPorEntrada.partes(membrosEmOrdem, entradas(grupo.getId()), gastos);
    }

    /**
     * Saldo de cada membro (gastos com data até hoje) = o que pagou − a parte dele nos gastos que divide
     *                       + pagamentos que fez − pagamentos que recebeu.
     * Cada gasto só é dividido entre quem já estava no grupo na data dele: quem entra não herda dívida antiga.
     * Ex.: o total foi 140 (70 para cada); A pagou 100 → +30, B pagou 40 → −30.
     * Se B registra que pagou 30 para A, os dois ficam em 0.
     * A soma dos saldos é sempre 0.
     */
    private SaldosResponse calcularSaldos(Grupo grupo, Long idUsuario) {
        List<Usuario> membros = grupo.getUsuarios().stream()
                .sorted(Comparator.comparing(Usuario::getLogin)).toList();

        // Só gastos até hoje: parcelas futuras do crédito entram no saldo quando chega o mês delas
        List<Transacao> gastos = transacaoRepository.findAllByGrupoIdAndDataLessThanEqual(grupo.getId(), LocalDate.now());
        Map<Long, BigDecimal> pago = new HashMap<>();
        for (Transacao t : gastos) {
            pago.merge(t.getUsuario().getId(), t.getValor(), BigDecimal::add);
        }

        // Começa com "pagou − parte justa", com a divisão em centavos exatos
        Map<Long, BigDecimal> partes = partesJustas(grupo, membros, gastos);
        Map<Long, BigDecimal> saldo = new HashMap<>();
        for (Usuario m : membros) {
            saldo.put(m.getId(), pago.getOrDefault(m.getId(), BigDecimal.ZERO).subtract(partes.get(m.getId())));
        }

        // Pagamentos: quem pagou diminui a dívida (saldo sobe); quem recebeu diminui o crédito (saldo desce)
        for (Pagamento p : pagamentoRepository.findAllByGrupoId(grupo.getId())) {
            saldo.computeIfPresent(p.getPagador().getId(), (k, v) -> v.add(p.getValor()));
            saldo.computeIfPresent(p.getRecebedor().getId(), (k, v) -> v.subtract(p.getValor()));
        }

        List<SaldoDoMembro> linhas = membros.stream()
                .map(m -> new SaldoDoMembro(m.getId(), m.getLogin(), m.getId().equals(idUsuario),
                        saldo.get(m.getId()).setScale(2, java.math.RoundingMode.HALF_UP)))
                .toList();
        return new SaldosResponse(linhas, sugerirPagamentos(linhas));
    }

    // Junta quem deve mais com quem tem mais a receber, até zerar. Dá poucos pagamentos e é fácil de entender
    private List<Sugestao> sugerirPagamentos(List<SaldoDoMembro> saldos) {
        List<SaldoDoMembro> devedores = new ArrayList<>(saldos.stream().filter(s -> s.saldo().signum() < 0)
                .sorted(Comparator.comparing(SaldoDoMembro::saldo)).toList());
        List<SaldoDoMembro> credores = new ArrayList<>(saldos.stream().filter(s -> s.saldo().signum() > 0)
                .sorted(Comparator.comparing(SaldoDoMembro::saldo).reversed()).toList());
        Map<Long, BigDecimal> resta = new HashMap<>();
        saldos.forEach(s -> resta.put(s.usuarioId(), s.saldo().abs()));

        List<Sugestao> sugestoes = new ArrayList<>();
        int d = 0, c = 0;
        while (d < devedores.size() && c < credores.size()) {
            SaldoDoMembro dev = devedores.get(d);
            SaldoDoMembro cred = credores.get(c);
            BigDecimal valor = resta.get(dev.usuarioId()).min(resta.get(cred.usuarioId()));
            sugestoes.add(new Sugestao(dev.usuarioId(), dev.login(), cred.usuarioId(), cred.login(), valor));
            resta.merge(dev.usuarioId(), valor.negate(), BigDecimal::add);
            resta.merge(cred.usuarioId(), valor.negate(), BigDecimal::add);
            if (resta.get(dev.usuarioId()).signum() == 0) d++;
            if (resta.get(cred.usuarioId()).signum() == 0) c++;
        }
        return sugestoes;
    }

    public List<PagamentoResponse> listarPagamentos(Long idGrupo, LocalDate inicio, LocalDate fim, Long idUsuario) {
        buscarDoMembro(idGrupo, idUsuario);
        return pagamentoRepository.findAllByGrupoIdAndDataBetweenOrderByDataDescIdDesc(idGrupo, inicio, fim)
                .stream().map(PagamentoResponse::new).toList();
    }

    // Quem registra precisa ser uma das duas pontas (quem pagou ou quem recebeu), e as duas precisam ser membros
    public PagamentoResponse registrarPagamento(Long idGrupo, PagamentoRequest dto, Long idUsuario) {
        Grupo grupo = buscarDoMembro(idGrupo, idUsuario);
        if (dto.deUsuarioId().equals(dto.paraUsuarioId())) {
            throw new IllegalArgumentException("Quem paga e quem recebe precisam ser pessoas diferentes.");
        }
        if (!idUsuario.equals(dto.deUsuarioId()) && !idUsuario.equals(dto.paraUsuarioId())) {
            throw new AcessoNegadoException("Só quem pagou ou quem recebeu pode registrar o pagamento.");
        }
        Usuario pagador = membro(grupo, dto.deUsuarioId());
        Usuario recebedor = membro(grupo, dto.paraUsuarioId());

        Pagamento pagamento = new Pagamento();
        pagamento.setGrupo(grupo);
        pagamento.setPagador(pagador);
        pagamento.setRecebedor(recebedor);
        pagamento.setValor(dto.valor().setScale(2, java.math.RoundingMode.HALF_UP));
        pagamento.setData(dto.data() != null ? dto.data() : LocalDate.now());
        return new PagamentoResponse(pagamentoRepository.save(pagamento));
    }

    // Para desfazer um registro errado. Também só quem pagou ou quem recebeu
    public void excluirPagamento(Long idGrupo, Long idPagamento, Long idUsuario) {
        buscarDoMembro(idGrupo, idUsuario);
        Pagamento pagamento = pagamentoRepository.findById(idPagamento)
                .filter(p -> p.getGrupo().getId().equals(idGrupo))
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
        if (!idUsuario.equals(pagamento.getPagador().getId()) && !idUsuario.equals(pagamento.getRecebedor().getId())) {
            throw new AcessoNegadoException("Só quem pagou ou quem recebeu pode apagar o pagamento.");
        }
        pagamentoRepository.delete(pagamento);
    }

    private Usuario membro(Grupo grupo, Long idUsuario) {
        return grupo.getUsuarios().stream().filter(u -> u.getId().equals(idUsuario)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Essa pessoa não faz parte do grupo"));
    }

    private static String reais(BigDecimal valor) {
        return java.text.NumberFormat.getCurrencyInstance(java.util.Locale.of("pt", "BR")).format(valor);
    }
}
