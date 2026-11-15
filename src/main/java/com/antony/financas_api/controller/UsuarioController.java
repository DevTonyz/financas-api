package com.antony.financas_api.controller;

import com.antony.financas_api.model.Categoria;
import com.antony.financas_api.model.Usuario;
import com.antony.financas_api.repository.CategoriaRepository;
import com.antony.financas_api.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Todo usuário novo já começa com estas categorias
    private static final List<String> CATEGORIAS_PADRAO = List.of(
            "Alimentação", "Transporte", "Moradia", "Lazer", "Saúde", "Educação", "Outros");

    // @Transactional: se falhar ao criar as categorias, o usuário também não fica salvo pela metade
    @Transactional
    @PostMapping
    public ResponseEntity<?> cadastrar(
            @RequestBody @Valid DadosCadastroUsuario dados,
            UriComponentsBuilder uriBuilder
    ) {
        // E-mail já cadastrado: 409 (conflito), para o app mostrar uma mensagem clara
        if (repository.existsByLogin(dados.login())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado");
        }

        var senhaCriptografada = passwordEncoder.encode(dados.senha());

        var usuario = new Usuario();
        usuario.setLogin(dados.login());
        usuario.setSenha(senhaCriptografada);

        repository.save(usuario);

        var categorias = CATEGORIAS_PADRAO.stream().map(nome -> {
            var categoria = new Categoria();
            categoria.setNome(nome);
            categoria.setUsuario(usuario);
            return categoria;
        }).toList();
        categoriaRepository.saveAll(categorias);

        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoUsuario(usuario));
    }
}

record DadosCadastroUsuario(
        @NotBlank @Email String login,
        @NotBlank @Size(min = 6, message = "deve ter pelo menos 6 caracteres") String senha
) {}

record DadosDetalhamentoUsuario(Long id, String login) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getLogin());
    }
}