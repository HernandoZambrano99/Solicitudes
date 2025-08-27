package co.com.bancolombia.usecase.exceptions;

import java.text.DecimalFormat;

public class MontoFueraDeRangoException extends RuntimeException {

    private static final DecimalFormat formatter = new DecimalFormat("#,###.##");

    public MontoFueraDeRangoException(Double monto, Double minimo, Double maximo) {
        super("Monto " + formatter.format(monto)
                + " fuera del rango permitido [" + formatter.format(minimo)
                + " - " + formatter.format(maximo) + "]");
    }
}