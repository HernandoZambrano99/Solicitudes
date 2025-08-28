package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.r2dbc.entity.TipoPrestamoEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoPrestamoRepositoryAdapterTest {

    @InjectMocks
    private TipoPrestamoRepositoryAdapter repositoryAdapter;

    @Mock
    private TipoPrestamoDataRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Test
    void mustSaveTipoPrestamo() {

        TipoPrestamoEntity entity = TipoPrestamoEntity.builder()
                .idTipoPrestamo(1)
                .nombre("Consumo")
                .montoMinimo(1000.0)
                .montoMaximo(10000.0)
                .build();

        TipoPrestamo model = TipoPrestamo.builder()
                .idTipoPrestamo(1)
                .nombre("Consumo")
                .montoMinimo(BigDecimal.valueOf(500000))
                .montoMaximo(BigDecimal.valueOf(100000000))
                .build();

        when(repository.save(any())).thenReturn(Mono.just(entity));
        when(mapper.map(entity, TipoPrestamo.class)).thenReturn(model);

        Mono<TipoPrestamo> result = repositoryAdapter.save(model);

        StepVerifier.create(result)
                .expectNextMatches(tp -> tp.getNombre().equals("Consumo"))
                .verifyComplete();
    }

    @Test
    void mustFindById() {

        TipoPrestamoEntity entity = TipoPrestamoEntity.builder()
                .idTipoPrestamo(1)
                .nombre("Hipotecario")
                .montoMinimo(5000.0)
                .montoMaximo(20000.0)
                .build();

        TipoPrestamo model = TipoPrestamo.builder()
                .idTipoPrestamo(1)
                .nombre("Hipotecario")
                .montoMinimo(BigDecimal.valueOf(500000))
                .montoMaximo(BigDecimal.valueOf(100000000))
                .build();

        when(repository.findById(1)).thenReturn(Mono.just(entity));
        when(mapper.map(entity, TipoPrestamo.class)).thenReturn(model);

        Mono<TipoPrestamo> result = repositoryAdapter.findById(1);

        StepVerifier.create(result)
                .expectNextMatches(tp -> tp.getIdTipoPrestamo() == 1 && tp.getNombre().equals("Hipotecario"))
                .verifyComplete();
    }
}
