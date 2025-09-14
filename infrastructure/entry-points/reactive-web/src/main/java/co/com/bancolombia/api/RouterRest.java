package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.SolicitudRequestDto;
import co.com.bancolombia.api.dto.SolicitudResponseDto;
import co.com.bancolombia.model.paginacion.PagedResponse;
import co.com.bancolombia.model.solicitud.Solicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitud/{id}",
                    produces = {"application/json"},
                    method = {org.springframework.web.bind.annotation.RequestMethod.GET},
                    beanClass = Handler.class,
                    beanMethod = "getSolicitudById",
                    operation = @Operation(
                            operationId = "getSolicitudById",
                            summary = "Obtener solicitud por ID",
                            description = "Devuelve una solicitud existente a partir de su identificador.",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "ID de la solicitud")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Solicitud encontrada",
                                            content = @Content(schema = @Schema(implementation = Solicitud.class))),
                                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = {"application/json"},
                    method = {org.springframework.web.bind.annotation.RequestMethod.POST},
                    beanClass = Handler.class,
                    beanMethod = "crearSolicitud",
                    operation = @Operation(
                            operationId = "crearSolicitud",
                            summary = "Crear nueva solicitud",
                            description = "Crea una nueva solicitud con los datos proporcionados.",
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos de la solicitud",
                                    content = @Content(schema = @Schema(implementation = SolicitudRequestDto.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Solicitud creada exitosamente",
                                            content = @Content(schema = @Schema(implementation = SolicitudResponseDto.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Error de validación en los datos"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = {"application/json"},
                    method = {org.springframework.web.bind.annotation.RequestMethod.GET},
                    beanClass = Handler.class,
                    beanMethod = "getAllSolicitudesWithFilters",
                    operation = @Operation(
                            operationId = "getAllSolicitudesWithFilters",
                            summary = "Listar solicitudes con filtros y paginación",
                            description = "Devuelve un listado paginado de solicitudes filtrado por estados. "
                                    + "Si no se especifica el parámetro de estados, se devuelven por defecto los estados 1, 3 y 4.",
                            parameters = {
                                    @Parameter(in = ParameterIn.QUERY, name = "status", required = false,
                                            description = "Lista de estados para filtrar, separados por coma (ej: 1,2,3)"),
                                    @Parameter(in = ParameterIn.QUERY, name = "pageSize", required = false,
                                            description = "Cantidad de registros por página (por defecto 10)"),
                                    @Parameter(in = ParameterIn.QUERY, name = "pageNumber", required = false,
                                            description = "Número de página (por defecto 1)"),
                                    @Parameter(in = ParameterIn.HEADER, name = "Authorization", required = true,
                                            description = "Token Bearer JWT para autenticación")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200",
                                            description = "Listado de solicitudes filtradas y paginadas",
                                            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Error en los parámetros de entrada"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(GET("/api/v1/solicitud/{id}"), handler::getSolicitudById)
                .andRoute(POST("/api/v1/solicitud"), handler::crearSolicitud)
                .andRoute(GET("/api/v1/solicitud"), handler::getAllSolicitudesWithFilters)
                .andRoute(PUT("/api/v1/solicitud/{id}"), handler::aprobarORechazarSolicitud)
                .andRoute(POST("/api/v1/calcular-capacidad"), handler::calcularCapacidadEndeudamiento);
    }
}
