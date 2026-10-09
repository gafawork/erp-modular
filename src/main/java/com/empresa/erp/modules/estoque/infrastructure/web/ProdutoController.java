package com.empresa.erp.modules.estoque.infrastructure.web;

import com.empresa.erp.modules.estoque.application.dto.EstoqueDto;
import com.empresa.erp.modules.estoque.application.dto.ProdutoDto;
import com.empresa.erp.modules.estoque.application.service.ProdutoApplicationService;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/produtos")
public class ProdutoController {

    private final ProdutoApplicationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','ESTOQUE')")
    public ProdutoDto.Response criar(@Valid @RequestBody ProdutoDto.Request request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ESTOQUE')")
    public ProdutoDto.Response atualizar(@PathVariable Long id,
                                         @Valid @RequestBody ProdutoDto.AtualizacaoRequest request) {
        return service.atualizar(id, request);
    }

    /** Consulta liberada a qualquer usuário autenticado (vendas e compras precisam do catálogo). */
    @GetMapping("/{id}")
    public ProdutoDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping
    public Page<ProdutoDto.Response> listar(@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/abaixo-do-minimo")
    @PreAuthorize("hasAnyRole('ADMIN','ESTOQUE','COMPRAS')")
    public List<ProdutoDto.Response> abaixoDoMinimo() {
        return service.listarAbaixoDoMinimo();
    }

    @PostMapping("/{id}/ajustes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','ESTOQUE')")
    public EstoqueDto.MovimentacaoResponse ajustar(@PathVariable Long id,
                                                   @Valid @RequestBody EstoqueDto.AjusteRequest request) {
        return service.ajustarEstoque(id, request);
    }

    @GetMapping("/{id}/movimentacoes")
    @PreAuthorize("hasAnyRole('ADMIN','ESTOQUE')")
    public Page<EstoqueDto.MovimentacaoResponse> movimentacoes(@PathVariable Long id,
                                                               @PageableDefault(size = 50) Pageable pageable) {
        return service.listarMovimentacoes(id, pageable);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long id) {
        service.inativar(id);
    }
}
