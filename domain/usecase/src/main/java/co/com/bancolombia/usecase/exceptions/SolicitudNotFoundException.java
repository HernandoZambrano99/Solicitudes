package co.com.bancolombia.usecase.exceptions;

public class SolicitudNotFoundException extends RuntimeException {
    public SolicitudNotFoundException(Integer id) {
        super("Solicitud con id " + id + " no encontrada");
    }
}