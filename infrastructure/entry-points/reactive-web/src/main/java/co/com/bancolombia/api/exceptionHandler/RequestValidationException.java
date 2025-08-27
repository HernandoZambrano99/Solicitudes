package co.com.bancolombia.api.exceptionHandler;

import java.util.List;

public class RequestValidationException extends RuntimeException {
    private final List<FieldErrorDetail> details;

    public RequestValidationException(List<FieldErrorDetail> details) {
        super("Error de validación en la solicitud");
        this.details = details;
    }

    public List<FieldErrorDetail> getDetails() {
        return details;
    }

    public record FieldErrorDetail(String field, String message) {}
}