package com.antony.financas_api.config;

import com.antony.financas_api.repository.UsuarioRepository;
import com.antony.financas_api.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Passo 1: Extrai o token do cabeçalho
        var tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            // Passo 2: Descriptografa e pega o e-mail
            var login = tokenService.getSubject(tokenJWT);

            // Passo 3: Busca o usuário (se não achar, daria erro, mas assumimos que o token é válido)
            var usuario = repository.findByLogin(login);

            // Passo 4: Avisa ao Spring Security que o usuário está autenticado
            var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continua o fluxo normal da requisição (se não autenticou acima, o Spring bloqueia logo depois daqui)
        filterChain.doFilter(request, response);
    }

    // Método auxiliar para limpar a palavra "Bearer " que o protocolo HTTP coloca na frente do Token
    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }
}