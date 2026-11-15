package com.antony.financas_api.controller;

import com.antony.financas_api.dto.CategoriaRequestDTO;
import com.antony.financas_api.dto.CategoriaResponseDTO;
import com.antony.financas_api.model.Categoria;
import com.antony.financas_api.repository.CategoriaRepository;
import com.antony.financas_api.repository.TransacaoRepository;
import com.antony.financas_api.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Cada usuário só enxerga e altera as próprias categorias
@RestController
@RequestMapping("/categorias")
@SecurityRequirement(name = "bearer-key")
public class CategoriaController {

    @Autowired
    private CategoriaRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> criarCategoria(
            @RequestBody @Valid CategoriaRequestDTO dto,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");

        Categoria categoria = new Categoria();
        categoria.setNome(dto.nome().trim());
        // getReferenceById não vai ao banco: só monta a ligação com o usuário pelo id
        categoria.setUsuario(usuarioRepository.getReferenceById(idUsuario));
        Categoria novaCategoria = repository.save(categoria);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(novaCategoria.getId()).toUri();

        return ResponseEntity.created(uri).body(new CategoriaResponseDTO(novaCategoria));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias(HttpServletRequest request) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        List<CategoriaResponseDTO> categorias = repository.findAllByUsuarioIdOrderByNomeAsc(idUsuario)
                .stream().map(CategoriaResponseDTO::new).toList();
        return ResponseEntity.ok(categorias);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> atualizarCategoria(
            @PathVariable Long id,
            @RequestBody @Valid CategoriaRequestDTO dto,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        return repository.findByIdAndUsuarioId(id, idUsuario)
                .map(categoriaExistente -> {
                    categoriaExistente.setNome(dto.nome().trim());
                    return ResponseEntity.ok(new CategoriaResponseDTO(repository.save(categoriaExistente)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarCategoria(@PathVariable Long id, HttpServletRequest request) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        var categoria = repository.findByIdAndUsuarioId(id, idUsuario);
        if (categoria.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Com transações ligadas, apagar quebraria a chave estrangeira (500). Avisamos com 409.
        if (transacaoRepository.existsByCategoriaId(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Categoria em uso");
        }
        repository.delete(categoria.get());
        return ResponseEntity.noContent().build();
    }
}
