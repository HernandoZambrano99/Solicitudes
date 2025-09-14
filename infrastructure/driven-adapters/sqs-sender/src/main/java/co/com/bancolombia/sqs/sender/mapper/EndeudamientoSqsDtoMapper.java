package co.com.bancolombia.sqs.sender.mapper;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.sqs.sender.dto.EndeudamientoSqsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EndeudamientoSqsDtoMapper {

    @Mapping(target = "solicitudId", source = "solicitud.idSolicitud")
    @Mapping(target = "usuarioId", source = "user.id")
    @Mapping(target = "nombreUsuario", expression = "java(detalle.getUser().getName() + \" \" + detalle.getUser().getLastName())")
    @Mapping(target = "emailUsuario", source = "user.email")
    @Mapping(target = "ingresosTotales", source = "user.salary")
    @Mapping(target = "documentoIdentidad", source = "user.identityDocument")
    @Mapping(target = "montoNuevoPrestamo", source = "solicitud.monto")
    @Mapping(target = "tasaInteresAnualNuevo", source = "tipoPrestamo.tasaInteres")
    @Mapping(target = "plazoMesesNuevo", source = "solicitud.plazo")
    @Mapping(target = "idTipoPrestamo", source = "tipoPrestamo.idTipoPrestamo")
    @Mapping(target = "nombreTipoPrestamo", source = "tipoPrestamo.nombre")
    @Mapping(target = "validacionAutomatica", source = "tipoPrestamo.validacionAutomatica")
    EndeudamientoSqsDto toDto(SolicitudDetalle detalle);
}