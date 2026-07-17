package com.antony.financas_api.controller;

import com.antony.financas_api.model.Usuario;
import com.antony.financas_api.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    // Injetamos aquele codificador de senhas que criamos no SecurityConfig
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity cadastrar(@RequestBody @Valid DadosCadastroUsuario dados, UriComponentsBuilder uriBuilder) {
        // 1. Pegamos a senha limpa e transformamos no hash seguro
        var senhaCriptografada = passwordEncoder.encode(dados.senha());

        // 2. Montamos o objeto usuário
        var usuario = new Usuario();
        usuario.setLogin(dados.login());
        usuario.setSenha(senhaCriptografada);

        // 3. Salvamos no banco
        repository.save(usuario);

        // 4. Devolvemos o status 201 (Created) e mostramos os dados salvos (ocultando a senha, claro)
        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoUsuario(usuario));
    }
}

// DTOs auxiliares
record DadosCadastroUsuario(
        @NotBlank @Email String login,
        @NotBlank String senha
) {}

record DadosDetalhamentoUsuario(Long id, String login) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getLogin());
    }
}