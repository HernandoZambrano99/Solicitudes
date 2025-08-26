package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.SolicitudRequestDto;
import co.com.bancolombia.model.solicitud.Solicitud;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudRequestMapper {

    @Mapping(target = "idSolicitud", ignore = true)
    @Mapping(target = "idEstado", constant = "1")
    @Mapping(target = "idTipoPrestamo", source = "tipoPrestamoId")
    Solicitud toModel(SolicitudRequestDto dto);
}