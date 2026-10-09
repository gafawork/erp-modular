package com.empresa.erp.modules.compras.infrastructure.web;

import com.empresa.erp.modules.compras.application.dto.FornecedorDto;
import com.empresa.erp.modules.compras.application.service.FornecedorApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fornecedores")
@PreAuthorize("hasAnyRole('ADMIN','COMPRAS')")
public class FornecedorController {

    private final FornecedorApplicationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FornecedorDto.Response criar(@Valid @RequestBody FornecedorDto.Request request) {
        return service.criar(request);
    }

    @GetMapping("/{id}")
    public FornecedorDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public Page<FornecedorDto.Response> listar(@PageableDefault(size = 20, sort = "razaoSocial") Pageable pageable) {
        return service.listar(pageable);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long id) {
        service.inativar(id);
    }
}
