package co.com.bancolombia.sqs.sender.mapper;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.usuario.User;
import co.com.bancolombia.sqs.sender.dto.SolicitudesSqsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudesSqsDtoMapper {

    @Mapping(target = "solicitudId", source = "solicitud.idSolicitud")
    @Mapping(target = "estado", source = "estado.nombre")
    @Mapping(target = "tipoPrestamo", source = "tipoPrestamo.nombre")
    @Mapping(target = "monto", source = "solicitud.monto")
    @Mapping(target = "plazo", source = "solicitud.plazo")
    @Mapping(target = "correoCliente", source = "user.email")
    @Mapping(target = "nombreCliente", expression = "java(concatNombre(solicitudDetalle.getUser()))")
    SolicitudesSqsDto toDto(SolicitudDetalle solicitudDetalle);

    default String concatNombre(User user) {
        if (user == null) return null;
        String n = user.getName() == null ? "" : user.getName().trim();
        String ln = user.getLastName() == null ? "" : user.getLastName().trim();
        String full = (n + " " + ln).trim();
        return full.isEmpty() ? null : full;
    }
}
