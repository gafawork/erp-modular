package com.empresa.erp.modules.clientes.infrastructure.web;

import com.empresa.erp.modules.clientes.application.dto.ClienteDto;
import com.empresa.erp.modules.clientes.application.service.ClienteApplicationService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clientes")
@PreAuthorize("hasAnyRole('ADMIN','VENDAS')")
public class ClienteController {

    private final ClienteApplicationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDto.Response criar(@Valid @RequestBody ClienteDto.Request request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public ClienteDto.Response atualizar(@PathVariable Long id,
                                         @Valid @RequestBody ClienteDto.AtualizacaoRequest request) {
        return service.atualizar(id, request);
    }

    @GetMapping("/{id}")
    public ClienteDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public Page<ClienteDto.Response> listar(@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.listar(pageable);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long id) {
        service.inativar(id);
    }
}
