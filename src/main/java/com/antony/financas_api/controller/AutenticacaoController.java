package com.antony.financas_api.controller;

import com.antony.financas_api.model.Usuario;
import com.antony.financas_api.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
        // Empacota o e-mail e senha em um formato que o Spring Security entende
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());

        // Dispara a validação (vai lá no banco checar se a senha bate)
        var authentication = manager.authenticate(authenticationToken);

        // Se a senha estiver correta, gera o nosso crachá JWT
        var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        // Devolve o token na tela do Swagger (ou para o aplicativo do celular)
        return ResponseEntity.ok(new DadosTokenJWT(tokenJWT));
    }
}

// DTOs auxiliares para receber e devolver os dados limpos
record DadosAutenticacao(String login, String senha) {}
record DadosTokenJWT(String token) {}