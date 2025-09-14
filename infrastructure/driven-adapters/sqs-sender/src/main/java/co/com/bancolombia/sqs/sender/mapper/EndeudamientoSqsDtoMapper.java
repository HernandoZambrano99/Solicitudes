package co.com.bancolombia.sqs.sender.mapper;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.tipoprestamo.PrestamoConTipo;
import co.com.bancolombia.sqs.sender.dto.EndeudamientoSqsDto;
import co.com.bancolombia.sqs.sender.dto.PrestamoActivoDto;
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
    @Mapping(target = "prestamosActivos", source = "prestamosAprobados")
    EndeudamientoSqsDto toDto(SolicitudDetalle detalle);

    // Este método auxiliar convierte cada Solicitud en un PrestamoActivoDto
    default PrestamoActivoDto solicitudToPrestamoActivoDto(PrestamoConTipo prestamoConTipo) {
        if (prestamoConTipo == null) return null;

        // aquí decides qué campos de Solicitud van en PrestamoActivoDto
        PrestamoActivoDto dto = new PrestamoActivoDto();
        dto.setMonto(prestamoConTipo.getSolicitud().getMonto());
        // si no tienes tasaInteres en Solicitud, debes sacarla de otro lado
        dto.setTasaInteresAnual(prestamoConTipo.getTipoPrestamo().getTasaInteres());
        dto.setPlazoMeses(prestamoConTipo.getSolicitud().getPlazo());
        // si tienes meses restantes, también aquí
        dto.setMesesRestantes(null);
        // si tienes estado, por ejemplo idEstado → nombre estado:
        dto.setEstado("Aprobado");
        return dto;
    }
}