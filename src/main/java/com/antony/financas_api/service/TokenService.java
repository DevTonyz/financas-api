package com.antony.financas_api.service;

import com.antony.financas_api.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    // Puxa aquela senha secreta que acabamos de colocar no application.properties
    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario) {
        try {
            // Define o algoritmo de criptografia usando a nossa senha secreta
            Algorithm algoritmo = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("API Financas") // Quem está emitindo o token
                    .withSubject(usuario.getUsername()) // Quem é o dono do token (seu e-mail)
                    .withExpiresAt(dataExpiracao()) // Quando o token vence
                    .sign(algoritmo); // Assina e finaliza

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String getSubject(String tokenJWT) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer("API Financas")
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!");
        }
    }

    // Regra de negócio: o crachá expira em 2 horas.
    // Depois disso, o aplicativo do celular terá que pedir outro.
    private Instant dataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}