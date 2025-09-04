package co.com.bancolombia.usecase.constants;

public final class Constants {
    private Constants() {}

    public static final String REQUEST_SAVED_SUCCESS =
            "Solicitud guardada exitosamente con ID: ";
    public static final String AMOUNT_OUT_RANGE =
            "Monto fuera de rango: ";
    public static final String GET_SOLICITUDES_BY_ESTADO_INIT =
            "Buscando solicitudes por estado(s): %s | page: %d, size: %d";
    public static final String GET_SOLICITUDES_BY_ESTADO_EMPTY =
            "No se encontraron solicitudes para los estados: %s";
}
