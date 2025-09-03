package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.SolicitudResponseDto;
import co.com.bancolombia.model.estados.Estados;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.model.usuario.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudMapper {

    @Mapping(target = "idSolicitud", source = "solicitud.idSolicitud")
    @Mapping(target = "documentoIdentidad", source = "solicitud.documentoIdentidad")
    @Mapping(target = "estado", source = "estado.nombre")
    @Mapping(target = "tipoPrestamo", source = "tipo.nombre")
    @Mapping(target = "monto", source = "solicitud.monto")
    @Mapping(target = "plazo", source = "solicitud.plazo")
    @Mapping(target = "email", source = "solicitud.email")
    @Mapping(target = "tasaInteres", source = "tipo.tasaInteres")
    @Mapping(target = "usuario", expression = "java(concatNombre(user))")
    @Mapping(target = "deudaTotalMensual", ignore = true)
    SolicitudResponseDto toDto(Solicitud solicitud, Estados estado, TipoPrestamo tipo, User user);

    default String concatNombre(User user) {
        if (user == null) return null;
        String n = user.getName() == null ? "" : user.getName().trim();
        String ln = user.getLastName() == null ? "" : user.getLastName().trim();
        String full = (n + " " + ln).trim();
        return full.isEmpty() ? null : full;
    }
}
