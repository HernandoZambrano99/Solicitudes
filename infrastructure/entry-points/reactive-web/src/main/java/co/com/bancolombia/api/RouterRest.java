package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.SolicitudRequestDto;
import co.com.bancolombia.api.dto.SolicitudResponseDto;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
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
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(GET("/api/v1/solicitud/{id}"), handler::getSolicitudById)
                .andRoute(POST("/api/v1/solicitud"), handler::crearSolicitud);
    }
}
