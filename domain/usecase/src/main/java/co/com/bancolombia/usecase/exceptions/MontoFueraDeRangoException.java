package co.com.bancolombia.usecase.exceptions;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class MontoFueraDeRangoException extends RuntimeException {

    private static final DecimalFormat formatter = new DecimalFormat("#,###.##");

    public MontoFueraDeRangoException(BigDecimal monto, BigDecimal minimo, BigDecimal maximo) {
        super("Monto " + formatter.format(monto)
                + " fuera del rango permitido [" + formatter.format(minimo)
                + " - " + formatter.format(maximo) + "]");
    }
}