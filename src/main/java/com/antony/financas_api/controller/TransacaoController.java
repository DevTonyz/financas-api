package com.antony.financas_api.controller;

import com.antony.financas_api.dto.AcertoDeContasDTO;
import com.antony.financas_api.dto.TransacaoRequestDTO;
import com.antony.financas_api.dto.TransacaoResponseDTO;
import com.antony.financas_api.repository.TransacaoRepository;
import com.antony.financas_api.service.TransacaoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import com.antony.financas_api.dto.ResumoResponseDTO;
import com.antony.financas_api.model.EscopoParcelas;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transacoes")
@SecurityRequirement(name = "bearer-key")
public class TransacaoController {

    @Autowired
    private TransacaoService service;

    @Autowired
    private TransacaoRepository repository;

    @PostMapping
    public ResponseEntity<List<TransacaoResponseDTO>> criarTransacao(
            @Valid @RequestBody TransacaoRequestDTO dto,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        List<TransacaoResponseDTO> transacoes = service.salvarTransacao(dto, idUsuario);
        return ResponseEntity.status(201).body(transacoes);
    }

    @GetMapping
    public ResponseEntity<Page<TransacaoResponseDTO>> listarTransacoes(
            @ParameterObject @PageableDefault(size = 10, sort = {"data"}) Pageable paginacao,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        Page<TransacaoResponseDTO> page = repository.findAllByUsuarioId(idUsuario, paginacao).map(TransacaoResponseDTO::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<Page<TransacaoResponseDTO>> listarPorCategoria(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 10) Pageable paginacao,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        Page<TransacaoResponseDTO> page = repository.findByCategoriaIdAndUsuarioId(id, idUsuario, paginacao).map(TransacaoResponseDTO::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/periodo")
    public ResponseEntity<Page<TransacaoResponseDTO>> listarPorPeriodo(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fim,
            @ParameterObject @PageableDefault(size = 10, sort = {"data"}) Pageable paginacao,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        Page<TransacaoResponseDTO> page = repository.findByDataBetweenAndUsuarioId(inicio, fim, idUsuario, paginacao).map(TransacaoResponseDTO::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoResponseDTO> obterResumo(
            @RequestParam Integer mes,
            @RequestParam Integer ano,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        ResumoResponseDTO resumo = service.obterResumoMensal(mes, ano, idUsuario);
        return ResponseEntity.ok(resumo);
    }

    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<Page<TransacaoResponseDTO>> listarPorGrupo(
            @PathVariable Long idGrupo,
            // Opcionais: com os dois, lista só o período (ex.: o mês na tela do grupo)
            @RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim,
            @ParameterObject @PageableDefault(size = 10, sort = {"data"}) Pageable paginacao,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        Page<TransacaoResponseDTO> page = service.listarPorGrupo(idGrupo, inicio, fim, idUsuario, paginacao);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/grupo/{idGrupo}/acerto")
    public ResponseEntity<AcertoDeContasDTO> obterAcertoDeContas(
            @PathVariable Long idGrupo,
            @RequestParam Integer mes,
            @RequestParam Integer ano,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        AcertoDeContasDTO acerto = service.obterAcertoDeContas(idGrupo, mes, ano, idUsuario);
        return ResponseEntity.ok(acerto);
    }

    @GetMapping("/grupo/{idGrupo}/resumo")
    public ResponseEntity<ResumoResponseDTO> obterResumoDoGrupo(
            @PathVariable Long idGrupo,
            @RequestParam Integer mes,
            @RequestParam Integer ano,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        ResumoResponseDTO resumo = service.obterResumoMensalGrupo(idGrupo, mes, ano, idUsuario);
        return ResponseEntity.ok(resumo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> buscarTransacao(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        return ResponseEntity.ok(service.buscarTransacao(id, idUsuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> atualizarTransacao(
            @PathVariable Long id,
            @Valid @RequestBody TransacaoRequestDTO dto,
            // Só importa em parcelas de uma compra: ESTA (padrão) ou PROXIMAS
            @RequestParam(defaultValue = "ESTA") EscopoParcelas escopo,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        TransacaoResponseDTO transacaoAtualizada = service.atualizarTransacao(id, dto, escopo, idUsuario);
        return ResponseEntity.ok(transacaoAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTransacao(
            @PathVariable Long id,
            // Só importa em parcelas de uma compra: ESTA (padrão), PROXIMAS ou TODAS
            @RequestParam(defaultValue = "ESTA") EscopoParcelas escopo,
            HttpServletRequest request
    ) {
        Long idUsuario = (Long) request.getAttribute("idUsuario");
        service.deletarTransacao(id, escopo, idUsuario);
        return ResponseEntity.noContent().build();
    }
}