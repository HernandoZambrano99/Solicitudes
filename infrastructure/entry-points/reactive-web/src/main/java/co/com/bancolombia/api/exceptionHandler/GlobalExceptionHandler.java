package co.com.bancolombia.api.exceptionHandler;

import co.com.bancolombia.api.constants.ErrorCode;
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
            return buildResponse(HttpStatus.BAD_REQUEST, ErrorConstants.VALIDATION_FAILED, request.path(), vex.getDetails());
        }

        if (error instanceof IllegalArgumentException iae) {
            return buildResponse(HttpStatus.CONFLICT, iae.getMessage(), request.path());
        }

        if (error instanceof org.springframework.web.server.ServerWebInputException sie) {
            return buildResponse(HttpStatus.BAD_REQUEST, sie.getReason(), request.path());
        }

        if (error instanceof SolicitudNotFoundException snf) {
            return buildResponse(HttpStatus.NOT_FOUND, snf.getMessage(), request.path());
        }

        if (error instanceof TipoPrestamoNotFoundException tpnf) {
            return buildResponse(HttpStatus.NOT_FOUND, tpnf.getMessage(), request.path());
        }

        if (error instanceof MontoFueraDeRangoException mfe) {
            return buildResponse(HttpStatus.BAD_REQUEST, mfe.getMessage(), request.path());
        }

        if (error instanceof UsuarioNotFoundException unfe) {
            return buildResponse(HttpStatus.NOT_FOUND, unfe.getMessage(), request.path());
        }

        if (error instanceof UsuarioNoCoincideException unce) {
            return buildResponse(HttpStatus.BAD_REQUEST, unce.getMessage(), request.path());
        }

        if (error instanceof SolicitudSoloClienteException ssce) {
            return buildResponse(HttpStatus.FORBIDDEN, ssce.getMessage(), request.path());
        }

        var errorProps = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        log.error("Error no controlado: {}", error.getMessage(), error);
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorProps);
    }

    private Mono<ServerResponse> buildResponse(HttpStatus status, String message, String path) {
        var body = new LinkedHashMap<String, Object>();
        body.put(ErrorConstants.ERROR, mapToErrorCode(status).getCode());
        body.put(ErrorConstants.MESSAGE, message);
        body.put(ErrorConstants.PATH, path);
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    private Mono<ServerResponse> buildResponse(HttpStatus status, String errorCode, String path, Object details) {
        var body = new LinkedHashMap<String, Object>();
        body.put(ErrorConstants.ERROR, errorCode);
        body.put(ErrorConstants.PATH, path);
        body.put(ErrorConstants.DETAILS, details);
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    private ErrorCode mapToErrorCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ErrorCode.BAD_REQUEST;
            case CONFLICT -> ErrorCode.CONFLICT;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case FORBIDDEN -> ErrorCode.FORBIDDEN;
            case INTERNAL_SERVER_ERROR -> ErrorCode.INTERNAL_ERROR;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}