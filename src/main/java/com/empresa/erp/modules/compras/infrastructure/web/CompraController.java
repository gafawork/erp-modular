package com.empresa.erp.modules.compras.infrastructure.web;

import com.empresa.erp.modules.compras.application.dto.CompraDto;
import com.empresa.erp.modules.compras.application.service.CompraApplicationService;
import com.empresa.erp.modules.compras.application.service.SugestaoReposicaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/compras")
@PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
public class CompraController {

    private final CompraApplicationService service;
    private final SugestaoReposicaoService sugestaoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompraDto.Response criar(@Valid @RequestBody CompraDto.Request request) {
        return service.criar(request);
    }

    @GetMapping("/{id}")
    public CompraDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public Page<CompraDto.Response> listar(
            @PageableDefault(size = 20, sort = "dataEmissao", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(pageable);
    }

    /** Recebimento da mercadoria: dá entrada no estoque (módulo Estoque) na mesma transação. */
    @PostMapping("/{id}/recebimento")
    @PreAuthorize("hasAnyRole('ADMIN','COMPRAS','ESTOQUE')")
    public CompraDto.Response receber(@PathVariable Long id) {
        return service.receber(id);
    }

    @PostMapping("/{id}/cancelamento")
    public CompraDto.Response cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    /** Sugestões geradas a partir dos alertas de estoque baixo (evento do módulo Estoque). */
    @GetMapping("/sugestoes-reposicao")
    public List<CompraDto.SugestaoResponse> sugestoesPendentes() {
        return sugestaoService.listarPendentes();
    }
}
