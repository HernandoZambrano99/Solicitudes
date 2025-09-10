package co.com.bancolombia.api.constants;

public final class ErrorConstants {
    private ErrorConstants() {}

    public static final String STATUS = "status";
    public static final String ERROR = "error";
    public static final String MESSAGE = "message";
    public static final String PATH = "path";
    public static final String DETAILS = "details";

    public static final String VALIDATION_FAILED = "Validation Failed";
    public static final String CONFLICT = "Conflict";
    public static final String BAD_REQUEST = "Bad Request";
    public static final String NOT_FOUND = "Not Found";

    public static final String INVALID_TOKEN = "Formato de token inválido";
    public static final String EXPIRED_JWT = "Token JWT inválido o expirado";
    public static final String AUTHORIZATION_NOT_FOUND = "El header Authorization es obligatorio";
    public static final String ID_IS_MANDATORY = "El idSolicitud es obligatorio";
    public static final String NO_NUMERIC_ID = "El idSolicitud debe ser numérico";
    public static final String BODY_IS_MANDATORY = "El cuerpo de la solicitud es obligatorio";
}
