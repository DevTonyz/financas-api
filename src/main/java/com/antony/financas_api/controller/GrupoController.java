package com.antony.financas_api.controller;

import com.antony.financas_api.dto.GrupoRequestDTO;
import com.antony.financas_api.dto.GrupoResponseDTO;
import com.antony.financas_api.model.Grupo;
import com.antony.financas_api.model.Usuario;
import com.antony.financas_api.repository.GrupoRepository;
import com.antony.financas_api.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.antony.financas_api.exception.AcessoNegadoException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/grupos")
@SecurityRequirement(name = "bearer-key")
public class GrupoController {

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<GrupoResponseDTO> criarGrupo(@RequestBody @Valid GrupoRequestDTO dto, HttpServletRequest request) {
        // 1. Descobre quem é o usuário logado
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        Usuario usuarioLogado = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        // 2. Monta o novo Grupo
        Grupo novoGrupo = new Grupo();
        novoGrupo.setNome(dto.nome());
        novoGrupo.setDescricao(dto.descricao());

        // 3. Adiciona o usuário logado na lista de participantes deste grupo
        novoGrupo.getUsuarios().add(usuarioLogado);

        // 4. Salva no banco (O JPA vai cuidar de preencher a tabela intermediária 'grupo_usuarios' sozinho!)
        Grupo grupoSalvo = grupoRepository.save(novoGrupo);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(grupoSalvo.getId()).toUri();

        return ResponseEntity.created(uri).body(new GrupoResponseDTO(grupoSalvo));
    }

    @PutMapping("/{idGrupo}/membros/{idNovoMembro}")
    public ResponseEntity<GrupoResponseDTO> adicionarMembro(
            @PathVariable Long idGrupo,
            @PathVariable Long idNovoMembro,
            HttpServletRequest request
    ) {
        // 1. Pega o ID de quem está fazendo a requisição (você)
        Long idUsuarioLogado = (Long) request.getAttribute("idUsuario");

        // 2. Busca o grupo no banco de dados
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado"));

        // 3. Regra de Segurança: Verifica se você realmente faz parte desse grupo antes de adicionar alguém
        boolean fazParte = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(idUsuarioLogado));

        if (!fazParte) {
            throw new AcessoNegadoException("Acesso negado: Você não faz parte deste grupo.");
        }

        // 4. Busca a conta da pessoa que será adicionada
        Usuario novoMembro = usuarioRepository.findById(idNovoMembro)
                .orElseThrow(() -> new EntityNotFoundException("Usuário a ser adicionado não encontrado"));

        // 5. Adiciona o novo membro e salva (o JPA atualiza a tabela intermediária automaticamente)
        grupo.getUsuarios().add(novoMembro);
        grupoRepository.save(grupo);

        return ResponseEntity.ok(new GrupoResponseDTO(grupo));
    }
}