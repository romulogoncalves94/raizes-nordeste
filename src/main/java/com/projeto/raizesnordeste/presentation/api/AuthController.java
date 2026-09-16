package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.application.ports.IUsuarioPort;
import com.projeto.raizesnordeste.domain.model.Usuario;
import com.projeto.raizesnordeste.infrastructure.security.JwtService;
import com.projeto.raizesnordeste.presentation.records.LoginRequest;
import com.projeto.raizesnordeste.presentation.records.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements IAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final IUsuarioPort useCase;

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        Usuario usuario = useCase.findByEmail(request.email());
        String token = jwtService.generateToken(usuario.getId(), usuario.getEmail(), usuario.getPerfil());

        return ResponseEntity.ok(new LoginResponse(token, "Bearer", usuario.getPerfil()));
    }
}
