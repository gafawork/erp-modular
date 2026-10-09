package com.empresa.erp.modules.vendas.infrastructure.web;

import com.empresa.erp.modules.vendas.application.dto.VendaDto;
import com.empresa.erp.modules.vendas.application.service.VendaApplicationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vendas")
@PreAuthorize("hasAnyRole('ADMIN','VENDAS')")
public class VendaController {

    private final VendaApplicationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendaDto.Response criar(@Valid @RequestBody VendaDto.Request request) {
        return service.criar(request);
    }

    @GetMapping("/{id}")
    public VendaDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public Page<VendaDto.Response> listar(
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(size = 20, sort = "dataVenda", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(clienteId, pageable);
    }

    /** Confirma e baixa o estoque; publica VendaConfirmadaEvent (outbox → JMS). */
    @PostMapping("/{id}/confirmacao")
    public VendaDto.Response confirmar(@PathVariable Long id) {
        return service.confirmar(id);
    }

    /** Cancela; se já confirmada, estorna o estoque. */
    @PostMapping("/{id}/cancelamento")
    public VendaDto.Response cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
