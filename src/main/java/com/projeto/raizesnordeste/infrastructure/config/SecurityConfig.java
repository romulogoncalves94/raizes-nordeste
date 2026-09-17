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
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Usuários
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios", "/api/usuarios/**").hasAnyRole("GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("GERENTE")
                        // Unidades
                        .requestMatchers(HttpMethod.POST, "/api/unidades").hasRole("GERENTE")
                        .requestMatchers(HttpMethod.GET, "/api/unidades", "/api/unidades/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/unidades/**").hasRole("GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/unidades/**").hasRole("GERENTE")
                        // Produtos
                        .requestMatchers(HttpMethod.POST, "/api/produtos").hasAnyRole("GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.GET, "/api/produtos", "/api/produtos/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/produtos/**").hasAnyRole("GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/produtos/**").hasAnyRole("GERENTE", "ATENDENTE")
                        // Estoques
                        .requestMatchers(HttpMethod.POST, "/api/estoques/movimentar").hasAnyRole("GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.POST, "/api/estoques").hasRole("GERENTE")
                        .requestMatchers(HttpMethod.GET, "/api/estoques", "/api/estoques/**").hasAnyRole("GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/estoques/**").hasRole("GERENTE")
                        // Pedidos
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pedidos", "/api/pedidos/**").hasAnyRole("GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/status").hasAnyRole("GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/*/cancelar").hasAnyRole("GERENTE", "ATENDENTE")
                        // Pagamentos
                        .requestMatchers(HttpMethod.POST, "/api/pagamentos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pagamentos/**").hasAnyRole("GERENTE", "ATENDENTE")
                        // Fallback: qualquer outro endpoint exige apenas autenticação
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
