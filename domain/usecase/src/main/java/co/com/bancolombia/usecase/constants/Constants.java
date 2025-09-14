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
    public static final String APROBAR_RECHAZAR_ESTADO_ACTUALIZADO =
            "Actualizando estado de solicitud %d a %s";
    public static final String APROBAR_RECHAZAR_ERROR =
            "Error en aprobarORechazar para solicitud %d: %s";
    public static final String SOLICITUD_ACTUALIZADA_EXITO =
            "Solicitud %d actualizada con éxito a estado %s";
    public static final String SOLICITUD_ACTUALIZADA_ERROR =
            "Error actualizando solicitud %d: %s";
    public static final String VALIDAR_CAPACIDAD_INICIO =
            "Entrando a validarYEnviarCapacidadEndeudamiento con id %d";
    public static final String VALIDAR_CAPACIDAD_TIPO =
            "Tipo préstamo %s validacionAutomatica=%s";
    public static final String VALIDAR_CAPACIDAD_LLAMANDO =
            "Llamando enviarCapacidadEndeudamiento...";
    public static final String VALIDAR_CAPACIDAD_ENVIADA =
            "Capacidad de endeudamiento enviada para solicitud %d";
    public static final String VALIDAR_CAPACIDAD_ERROR =
            "Error enviando capacidad de endeudamiento: %s";
}
