package co.com.bancolombia.api.config;

import co.com.bancolombia.api.constants.AppConstants;
import co.com.bancolombia.api.constants.ErrorConstants;
import co.com.bancolombia.api.exceptionHandler.InvalidJwtTokenException;
import co.com.bancolombia.api.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SolicitudesSecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/v3/api-docs/**").permitAll()
                        .pathMatchers("/swagger-ui.html").permitAll()
                        .pathMatchers("/webjars/swagger-ui/**").permitAll()
                        .pathMatchers("/swagger-ui/**").permitAll()

                        .pathMatchers(HttpMethod.GET, "/api/v1/solicitud").hasAuthority("ROLE_ASESOR")
                        .pathMatchers(HttpMethod.GET, "/api/v1/solicitud/{id}").hasAuthority("ROLE_ASESOR")
                        .pathMatchers(HttpMethod.POST, "/api/v1/solicitud").hasAuthority("ROLE_CLIENT")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/solicitud/{id}").hasAuthority("ROLE_ASESOR")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private AuthenticationWebFilter jwtAuthenticationFilter() {
        ReactiveAuthenticationManager manager = authentication -> Mono.just(authentication);

        AuthenticationWebFilter filter = new AuthenticationWebFilter(manager);
        filter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.anyExchange());

        filter.setServerAuthenticationConverter(exchange -> {
            List<String> authHeaders = exchange.getRequest().getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);
            if (authHeaders.isEmpty()) return Mono.empty();

            String authHeader = authHeaders.get(0);
            if (!authHeader.startsWith(AppConstants.BEARER)) {
                return Mono.error(new InvalidJwtTokenException(ErrorConstants.INVALID_TOKEN));
            }

            String token = authHeader.substring(7);
            try {
                Jws<Claims> claimsJws = jwtUtil.validateToken(token);
                Claims claims = claimsJws.getBody();
                String email = claims.getSubject();
                String role = claims.get("role", String.class);

                List<SimpleGrantedAuthority> authorities = Arrays.stream(role.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                return Mono.just(new UsernamePasswordAuthenticationToken(email, token, authorities));
            } catch (Exception e) {
                return Mono.error(new InvalidJwtTokenException(ErrorConstants.EXPIRED_JWT));
            }
        });

        return filter;
    }
}
