package com.projeto.raizesnordeste.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.raizesnordeste.application.ports.IUsuarioPort;
import com.projeto.raizesnordeste.application.ports.IUsuarioRepositoryPort;
import com.projeto.raizesnordeste.application.services.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeansConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public IUsuarioPort usuarioServicePort(IUsuarioRepositoryPort usuarioRepositoryPort, PasswordEncoder passwordEncoder) {
        return new UsuarioService(usuarioRepositoryPort, passwordEncoder);
    }

}
