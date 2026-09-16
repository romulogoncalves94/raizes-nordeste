package com.projeto.raizesnordeste.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.raizesnordeste.application.ports.IEstoquePort;
import com.projeto.raizesnordeste.application.ports.IEstoqueRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IPedidoPort;
import com.projeto.raizesnordeste.application.ports.IPedidoRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IProdutoPort;
import com.projeto.raizesnordeste.application.ports.IProdutoRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IUnidadePort;
import com.projeto.raizesnordeste.application.ports.IUnidadeRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IUsuarioPort;
import com.projeto.raizesnordeste.application.ports.IUsuarioRepositoryPort;
import com.projeto.raizesnordeste.application.services.EstoqueService;
import com.projeto.raizesnordeste.application.services.PedidoService;
import com.projeto.raizesnordeste.application.services.ProdutoService;
import com.projeto.raizesnordeste.application.services.UnidadeService;
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

    @Bean
    public IUnidadePort unidadeServicePort(IUnidadeRepositoryPort unidadeRepositoryPort) {
        return new UnidadeService(unidadeRepositoryPort);
    }

    @Bean
    public IProdutoPort produtoServicePort(IProdutoRepositoryPort produtoRepositoryPort) {
        return new ProdutoService(produtoRepositoryPort);
    }

    @Bean
    public IEstoquePort estoqueServicePort(IEstoqueRepositoryPort estoqueRepositoryPort, IUnidadePort unidadePort, IProdutoPort produtoPort) {
        return new EstoqueService(estoqueRepositoryPort, unidadePort, produtoPort);
    }

    @Bean
    public IPedidoPort pedidoServicePort(IPedidoRepositoryPort pedidoRepositoryPort, IUsuarioPort usuarioPort,
                                          IUnidadePort unidadePort, IProdutoPort produtoPort, IEstoquePort estoquePort) {
        return new PedidoService(pedidoRepositoryPort, usuarioPort, unidadePort, produtoPort, estoquePort);
    }

}
