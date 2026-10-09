package com.empresa.erp.modules.vendas.application.mapper;

import com.empresa.erp.modules.estoque.api.ItemMovimento;
import com.empresa.erp.modules.vendas.application.dto.VendaDto;
import com.empresa.erp.modules.vendas.domain.entity.ItemVenda;
import com.empresa.erp.modules.vendas.domain.entity.Venda;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VendaMapper {

    public VendaDto.Response toResponse(Venda v) {
        return new VendaDto.Response(
                v.getId(),
                v.getClienteId(),
                v.getNomeCliente(),
                v.getStatus().name(),
                v.getItens().stream().map(this::toItemResponse).toList(),
                v.getValorTotal().valor(),
                v.getDataVenda(),
                v.getDataConfirmacao());
    }

    /** Domínio de Vendas → comando da api do Estoque (tradução na aplicação, não no domínio). */
    public List<ItemMovimento> toItensMovimento(Venda v) {
        return v.getItens().stream()
                .map(i -> ItemMovimento.semCusto(i.getProdutoId(), i.getQuantidade()))
                .toList();
    }

    private VendaDto.ItemResponse toItemResponse(ItemVenda i) {
        return new VendaDto.ItemResponse(i.getProdutoId(), i.getSku(), i.getNomeProduto(), i.getQuantidade(),
                i.getPrecoUnitario().valor(), i.subtotal().valor());
    }
}
