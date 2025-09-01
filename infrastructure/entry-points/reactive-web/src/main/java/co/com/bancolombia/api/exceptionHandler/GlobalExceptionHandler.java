package co.com.bancolombia.api.exceptionHandler;

import co.com.bancolombia.api.constants.ErrorConstants;
import co.com.bancolombia.consumer.exception.SolicitudSoloClienteException;
import co.com.bancolombia.usecase.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  org.springframework.boot.autoconfigure.web.WebProperties.Resources resources,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(configurer.getWriters());
        this.setMessageReaders(configurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);

        if (error instanceof RequestValidationException vex) {
            var body = new LinkedHashMap<String, Object>();
            body.put(ErrorConstants.ERROR, ErrorConstants.VALIDATION_FAILED);
            body.put(ErrorConstants.PATH, request.path());
            body.put(ErrorConstants.DETAILS, vex.getDetails());
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        }

        if (error instanceof IllegalArgumentException iae) {
            var body = Map.of(
                    ErrorConstants.ERROR, ErrorConstants.CONFLICT,
                    ErrorConstants.MESSAGE, iae.getMessage(),
                    ErrorConstants.PATH, request.path()
            );
            return ServerResponse.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        }

        if (error instanceof org.springframework.web.server.ServerWebInputException sie) {
            var body = Map.of(
                    ErrorConstants.ERROR, ErrorConstants.BAD_REQUEST,
                    ErrorConstants.MESSAGE, sie.getReason(),
                    ErrorConstants.PATH, request.path()
            );
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        }

        if (error instanceof SolicitudNotFoundException snf) {
            var body = Map.of(
                    ErrorConstants.ERROR, ErrorConstants.NOT_FOUND,
                    ErrorConstants.MESSAGE, snf.getMessage(),
                    ErrorConstants.PATH, request.path()
            );
            return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        }

        if (error instanceof TipoPrestamoNotFoundException tpnf) {
            var body = Map.of(
                    ErrorConstants.ERROR, ErrorConstants.NOT_FOUND,
                    ErrorConstants.MESSAGE, tpnf.getMessage(),
                    ErrorConstants.PATH, request.path()
            );
            return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        }

        if (error instanceof MontoFueraDeRangoException mfe) {
            var body = Map.of(
                    ErrorConstants.ERROR, ErrorConstants.VALIDATION_FAILED,
                    ErrorConstants.MESSAGE, mfe.getMessage(),
                    ErrorConstants.PATH, request.path()
            );
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        }

        if (error instanceof UsuarioNotFoundException unfe) {
            var body = Map.of(
                    ErrorConstants.ERROR, ErrorConstants.NOT_FOUND,
                    ErrorConstants.MESSAGE, unfe.getMessage(),
                    ErrorConstants.PATH, request.path()
            );
            return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        }

        if (error instanceof UsuarioNoCoincideException unce) {
            var body = Map.of(
                    ErrorConstants.ERROR, ErrorConstants.NOT_FOUND,
                    ErrorConstants.MESSAGE, unce.getMessage(),
                    ErrorConstants.PATH, request.path()
            );
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        }

        if (error instanceof SolicitudSoloClienteException ssce) {
            var body = Map.of(
                    ErrorConstants.ERROR, ErrorConstants.NOT_FOUND,
                    ErrorConstants.MESSAGE, ssce.getMessage(),
                    ErrorConstants.PATH, request.path()
            );
            return ServerResponse.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        }

        var errorProps = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        log.error("Error no controlado: {}", error.getMessage(), error);
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorProps);
    }
}