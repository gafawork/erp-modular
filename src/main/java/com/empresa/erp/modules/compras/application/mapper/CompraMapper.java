package com.empresa.erp.modules.compras.application.mapper;

import com.empresa.erp.modules.compras.application.dto.CompraDto;
import com.empresa.erp.modules.compras.domain.entity.Compra;
import com.empresa.erp.modules.compras.domain.entity.ItemCompra;
import com.empresa.erp.modules.compras.domain.entity.SugestaoReposicao;
import com.empresa.erp.modules.estoque.api.ItemMovimento;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompraMapper {

    public CompraDto.Response toResponse(Compra c) {
        return new CompraDto.Response(
                c.getId(),
                c.getFornecedor().getId(),
                c.getFornecedor().getRazaoSocial(),
                c.getStatus().name(),
                c.getItens().stream().map(this::toItemResponse).toList(),
                c.getValorTotal().valor(),
                c.getDataEmissao(),
                c.getDataRecebimento());
    }

    public CompraDto.SugestaoResponse toResponse(SugestaoReposicao s) {
        return new CompraDto.SugestaoResponse(s.getId(), s.getProdutoId(), s.getSku(), s.getQuantidadeAtual(),
                s.getEstoqueMinimo(), s.quantidadeSugerida(), s.getStatus().name(), s.getDataUltimoAlerta());
    }

    /** Domínio de Compras → comando da api do Estoque (a tradução fica na aplicação, não no domínio). */
    public List<ItemMovimento> toItensMovimento(Compra c) {
        return c.getItens().stream()
                .map(i -> ItemMovimento.comCusto(i.getProdutoId(), i.getQuantidade(), i.getCustoUnitario().valor()))
                .toList();
    }

    private CompraDto.ItemResponse toItemResponse(ItemCompra i) {
        return new CompraDto.ItemResponse(i.getProdutoId(), i.getSku(), i.getNomeProduto(), i.getQuantidade(),
                i.getCustoUnitario().valor(), i.subtotal().valor());
    }
}
