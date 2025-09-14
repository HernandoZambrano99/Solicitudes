package co.com.bancolombia.usecase.enums;

public enum EstadoSolicitudEnum {

    APROBADO(2),
    RECHAZADO(3),
    REVISION_MANUAL(4),
    PENDIENTE_REVISION(1);


    private final int idEstado;

    EstadoSolicitudEnum(int idEstado) {
        this.idEstado = idEstado;
    }

    public int getIdEstado() {
        return idEstado;
    }

    /**
     * Convierte un string a EstadoSolicitud, ignorando mayúsculas/minúsculas.
     */
    public static EstadoSolicitudEnum fromString(String estado) {
        for (EstadoSolicitudEnum e : values()) {
            if (e.name().equalsIgnoreCase(estado)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado inválido: " + estado);
    }
}