package com.antony.financas_api.controller;

import com.antony.financas_api.dto.GrupoDetalheDTO;
import com.antony.financas_api.dto.GrupoRequestDTO;
import com.antony.financas_api.dto.GrupoResponseDTO;
import com.antony.financas_api.dto.PagamentoDTOs.PagamentoRequest;
import com.antony.financas_api.dto.PagamentoDTOs.PagamentoResponse;
import com.antony.financas_api.dto.PagamentoDTOs.SaldosResponse;
import com.antony.financas_api.service.GrupoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

// As regras (só membro mexe no grupo, 404/403/409) ficam no GrupoService
@RestController
@RequestMapping("/grupos")
@SecurityRequirement(name = "bearer-key")
public class GrupoController {

    @Autowired
    private GrupoService service;

    @PostMapping
    public ResponseEntity<GrupoResponseDTO> criarGrupo(@RequestBody @Valid GrupoRequestDTO dto, HttpServletRequest request) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        GrupoResponseDTO grupo = service.criar(dto, idUsuario);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(grupo.id()).toUri();
        return ResponseEntity.created(uri).body(grupo);
    }

    // Só os grupos de que o usuário logado faz parte
    @GetMapping
    public ResponseEntity<List<GrupoResponseDTO>> listarMeusGrupos(HttpServletRequest request) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        return ResponseEntity.ok(service.listarDoUsuario(idUsuario));
    }

    @GetMapping("/{idGrupo}")
    public ResponseEntity<GrupoDetalheDTO> detalharGrupo(@PathVariable Long idGrupo, HttpServletRequest request) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        return ResponseEntity.ok(service.detalhar(idGrupo, idUsuario));
    }

    // Adiciona pelo e-mail: 404 se não existe conta, 409 se já é membro, 403 se quem pede não é membro
    @PostMapping("/{idGrupo}/membros")
    public ResponseEntity<GrupoDetalheDTO> adicionarMembroPorEmail(
            @PathVariable Long idGrupo,
            @RequestBody @Valid NovoMembroDTO dto,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        return ResponseEntity.ok(service.adicionarMembroPorLogin(idGrupo, dto.login(), idUsuario));
    }

    // Rota antiga, pelo id do usuário. O app usa a de cima, por e-mail
    @PutMapping("/{idGrupo}/membros/{idNovoMembro}")
    public ResponseEntity<GrupoResponseDTO> adicionarMembro(
            @PathVariable Long idGrupo,
            @PathVariable Long idNovoMembro,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        return ResponseEntity.ok(service.adicionarMembroPorId(idGrupo, idNovoMembro, idUsuario));
    }

    // Sair do grupo. Se ninguém sobrar, o grupo é apagado
    @DeleteMapping("/{idGrupo}/membros/me")
    public ResponseEntity<Void> sairDoGrupo(@PathVariable Long idGrupo, HttpServletRequest request) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        service.sair(idGrupo, idUsuario);
        return ResponseEntity.noContent().build();
    }

    // Saldo acumulado de cada membro (todos os meses, já descontando os pagamentos) e sugestões de acerto
    @GetMapping("/{idGrupo}/saldos")
    public ResponseEntity<SaldosResponse> saldos(@PathVariable Long idGrupo, HttpServletRequest request) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        return ResponseEntity.ok(service.saldos(idGrupo, idUsuario));
    }

    // Pagamentos entre membros num período (o app usa o mês). A data entra em "yyyy-MM-dd"
    @GetMapping("/{idGrupo}/pagamentos")
    public ResponseEntity<List<PagamentoResponse>> listarPagamentos(
            @PathVariable Long idGrupo,
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        return ResponseEntity.ok(service.listarPagamentos(idGrupo, inicio, fim, idUsuario));
    }

    // "Fulano pagou R$ X para ciclano". Não é gasto: só mexe nos saldos
    @PostMapping("/{idGrupo}/pagamentos")
    public ResponseEntity<PagamentoResponse> registrarPagamento(
            @PathVariable Long idGrupo,
            @RequestBody @Valid PagamentoRequest dto,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        return ResponseEntity.status(201).body(service.registrarPagamento(idGrupo, dto, idUsuario));
    }

    @DeleteMapping("/{idGrupo}/pagamentos/{idPagamento}")
    public ResponseEntity<Void> excluirPagamento(
            @PathVariable Long idGrupo,
            @PathVariable Long idPagamento,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        service.excluirPagamento(idGrupo, idPagamento, idUsuario);
        return ResponseEntity.noContent().build();
    }

    public record NovoMembroDTO(@NotBlank @Email String login) {
    }
}
