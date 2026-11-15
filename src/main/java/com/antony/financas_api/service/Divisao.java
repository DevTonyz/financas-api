package com.antony.financas_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

// Divide um valor em n partes com centavos exatos: 100 em 3 = 33,34 + 33,33 + 33,33.
// Os centavos que sobram vão para as primeiras partes, assim a soma das partes é sempre o total
final class Divisao {

    private Divisao() {
    }

    static List<BigDecimal> emPartes(BigDecimal total, int n) {
        long totalCentavos = total.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        long base = totalCentavos / n;
        long sobra = totalCentavos % n;
        List<BigDecimal> partes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            partes.add(BigDecimal.valueOf(base + (i < sobra ? 1 : 0), 2));
        }
        return partes;
    }
}
