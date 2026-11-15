package com.antony.financas_api.service;

import com.antony.financas_api.model.Transacao;
import com.antony.financas_api.model.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Teste sem Spring nem banco: só a conta da divisão com data de entrada
class DivisaoPorEntradaTest {

    private final Usuario a = usuario(1L, "a@x.test");
    private final Usuario b = usuario(2L, "b@x.test");
    private final Usuario c = usuario(3L, "c@x.test");

    @Test
    void quemEntraDepoisNaoDivideGastoAntigo() {
        // A e B desde sempre; C entra em 10/11
        Map<Long, LocalDate> entradas = Map.of(3L, LocalDate.of(2026, 11, 10));
        List<Transacao> gastos = List.of(
                gasto(a, "100.00", LocalDate.of(2026, 11, 5)),   // só A e B: 50 + 50
                gasto(b, "90.00", LocalDate.of(2026, 11, 12)));  // os três: 30 + 30 + 30

        Map<Long, BigDecimal> partes = DivisaoPorEntrada.partes(List.of(a, b, c), entradas, gastos);

        assertEquals(new BigDecimal("80.00"), partes.get(1L));
        assertEquals(new BigDecimal("80.00"), partes.get(2L));
        assertEquals(new BigDecimal("30.00"), partes.get(3L));
    }

    @Test
    void gastoNoDiaDaEntradaJaEntraNaDivisao() {
        Map<Long, LocalDate> entradas = Map.of(3L, LocalDate.of(2026, 11, 10));
        List<Transacao> gastos = List.of(gasto(a, "100.00", LocalDate.of(2026, 11, 10)));

        Map<Long, BigDecimal> partes = DivisaoPorEntrada.partes(List.of(a, b, c), entradas, gastos);

        // 100 em 3: os centavos vão para os primeiros e a soma fecha em 100
        assertEquals(new BigDecimal("33.34"), partes.get(1L));
        assertEquals(new BigDecimal("33.33"), partes.get(2L));
        assertEquals(new BigDecimal("33.33"), partes.get(3L));
    }

    @Test
    void quemLancaGastoAntesDaPropriaEntradaTambemDivide() {
        Map<Long, LocalDate> entradas = Map.of(3L, LocalDate.of(2026, 11, 10));
        List<Transacao> gastos = List.of(gasto(c, "60.00", LocalDate.of(2026, 11, 1)));

        Map<Long, BigDecimal> partes = DivisaoPorEntrada.partes(List.of(a, b, c), entradas, gastos);

        assertEquals(new BigDecimal("20.00"), partes.get(1L));
        assertEquals(new BigDecimal("20.00"), partes.get(2L));
        assertEquals(new BigDecimal("20.00"), partes.get(3L));
    }

    private static Usuario usuario(Long id, String login) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setLogin(login);
        return u;
    }

    private static Transacao gasto(Usuario quem, String valor, LocalDate data) {
        Transacao t = new Transacao();
        t.setUsuario(quem);
        t.setValor(new BigDecimal(valor));
        t.setData(data);
        return t;
    }
}
