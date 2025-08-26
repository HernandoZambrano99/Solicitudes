package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.SolicitudResponseDto;
import co.com.bancolombia.model.estados.Estados;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface SolicitudMapper {

    @Mappings({
            @Mapping(target = "idSolicitud", source = "solicitud.idSolicitud"),
            @Mapping(target = "documentoIdentidad", source = "solicitud.documentoIdentidad"),
            @Mapping(target = "estado", source = "estado.nombre"),
            @Mapping(target = "tipoPrestamo", source = "tipo.nombre"),
            @Mapping(target = "monto", source = "solicitud.monto"),
            @Mapping(target = "plazo", source = "solicitud.plazo"),
            @Mapping(target = "email", source = "solicitud.email")
    })
    SolicitudResponseDto toDto(Solicitud solicitud, Estados estado, TipoPrestamo tipo);
}