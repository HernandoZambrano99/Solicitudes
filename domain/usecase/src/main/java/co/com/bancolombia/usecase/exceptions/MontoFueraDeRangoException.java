package co.com.bancolombia.usecase.exceptions;

public class MontoFueraDeRangoException extends RuntimeException {
    public MontoFueraDeRangoException(Double monto, Double min, Double max) {
        super("Monto " + monto + " fuera del rango permitido [" + min + " - " + max + "]");
    }
}