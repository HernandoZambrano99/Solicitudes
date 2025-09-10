package co.com.bancolombia.api.exceptionHandler;

public class InvalidParameterException extends RuntimeException {
    public InvalidParameterException(String message) {
        super(message);
    }
}
