package com.projeto.raizesnordeste.infrastructure.config;

import com.projeto.raizesnordeste.infrastructure.security.JwtAuthenticationFilter;
import com.projeto.raizesnordeste.infrastructure.security.RestAccessDeniedHandler;
import com.projeto.raizesnordeste.infrastructure.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(this::configurarTratamentoExcecoes)
                .authorizeHttpRequests(this::configurarAutorizacoes)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void configurarTratamentoExcecoes(ExceptionHandlingConfigurer<HttpSecurity> exception) {
        exception
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler);
    }

    private void configurarAutorizacoes(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        autorizarRotasPublicas(auth);
        autorizarUsuarios(auth);
        autorizarUnidades(auth);
        autorizarProdutos(auth);
        autorizarEstoques(auth);
        autorizarPedidos(auth);
        autorizarPagamentos(auth);
        autorizarFidelidade(auth);
        autorizarCampanhas(auth);

        auth.anyRequest().authenticated();
    }

    private void autorizarRotasPublicas(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers("/api/auth/**").permitAll();
        auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
    }

    private void autorizarUsuarios(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll();
        auth.requestMatchers(HttpMethod.GET, "/api/usuarios", "/api/usuarios/**").hasAnyRole("GERENTE", "ATENDENTE");
        auth.requestMatchers(HttpMethod.PUT, "/api/usuarios/**").authenticated();
        auth.requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("GERENTE");
    }

    private void autorizarUnidades(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, "/api/unidades").hasRole("GERENTE");
        auth.requestMatchers(HttpMethod.GET, "/api/unidades", "/api/unidades/**").authenticated();
        auth.requestMatchers(HttpMethod.PUT, "/api/unidades/**").hasRole("GERENTE");
        auth.requestMatchers(HttpMethod.DELETE, "/api/unidades/**").hasRole("GERENTE");
    }

    private void autorizarProdutos(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, "/api/produtos").hasAnyRole("GERENTE", "ATENDENTE");
        auth.requestMatchers(HttpMethod.GET, "/api/produtos", "/api/produtos/**").authenticated();
        auth.requestMatchers(HttpMethod.PUT, "/api/produtos/**").hasAnyRole("GERENTE", "ATENDENTE");
        auth.requestMatchers(HttpMethod.DELETE, "/api/produtos/**").hasAnyRole("GERENTE", "ATENDENTE");
    }

    private void autorizarEstoques(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, "/api/estoques/movimentar").hasAnyRole("GERENTE", "ATENDENTE");
        auth.requestMatchers(HttpMethod.POST, "/api/estoques").hasRole("GERENTE");
        auth.requestMatchers(HttpMethod.GET, "/api/estoques", "/api/estoques/**").hasAnyRole("GERENTE", "ATENDENTE");
        auth.requestMatchers(HttpMethod.DELETE, "/api/estoques/**").hasRole("GERENTE");
    }

    private void autorizarPedidos(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, "/api/pedidos").authenticated();
        auth.requestMatchers(HttpMethod.GET, "/api/pedidos", "/api/pedidos/**").hasAnyRole("GERENTE", "ATENDENTE");
        auth.requestMatchers(HttpMethod.PUT, "/api/pedidos/*/status").hasAnyRole("GERENTE", "ATENDENTE");
        auth.requestMatchers(HttpMethod.POST, "/api/pedidos/*/cancelar").hasAnyRole("GERENTE", "ATENDENTE");
    }

    private void autorizarPagamentos(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, "/api/pagamentos").authenticated();
        auth.requestMatchers(HttpMethod.GET, "/api/pagamentos/**").hasAnyRole("GERENTE", "ATENDENTE");
    }

    private void autorizarFidelidade(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers("/api/fidelidade/**").authenticated();
    }

    private void autorizarCampanhas(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, "/api/campanhas").hasRole("GERENTE");
        auth.requestMatchers(HttpMethod.GET, "/api/campanhas", "/api/campanhas/**").authenticated();
        auth.requestMatchers(HttpMethod.PUT, "/api/campanhas/**").hasRole("GERENTE");
        auth.requestMatchers(HttpMethod.DELETE, "/api/campanhas/**").hasRole("GERENTE");
    }
}
