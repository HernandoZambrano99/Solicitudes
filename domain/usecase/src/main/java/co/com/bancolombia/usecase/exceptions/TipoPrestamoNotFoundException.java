package co.com.bancolombia.usecase.exceptions;

public class TipoPrestamoNotFoundException extends RuntimeException {
    public TipoPrestamoNotFoundException(Integer id) {
        super("Tipo de préstamo con id " + id + " no existe");
    }
}