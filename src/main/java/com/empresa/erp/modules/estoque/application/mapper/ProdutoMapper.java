package com.empresa.erp.modules.estoque.application.mapper;

import com.empresa.erp.modules.estoque.api.ProdutoResumo;
import com.empresa.erp.modules.estoque.application.dto.EstoqueDto;
import com.empresa.erp.modules.estoque.application.dto.ProdutoDto;
import com.empresa.erp.modules.estoque.domain.entity.MovimentacaoEstoque;
import com.empresa.erp.modules.estoque.domain.entity.Produto;
import com.empresa.erp.modules.estoque.domain.vo.Sku;
import com.empresa.erp.shared.domain.vo.Dinheiro;
import org.springframework.stereotype.Component;

@Component
public class ProdutoMapper {

    public Produto toDomain(ProdutoDto.Request req) {
        return Produto.novo(new Sku(req.sku()), req.nome(), req.descricao(),
                Dinheiro.of(req.precoCusto()), Dinheiro.of(req.precoVenda()), req.estoqueMinimo());
    }

    public ProdutoDto.Response toResponse(Produto p) {
        return new ProdutoDto.Response(
                p.getId(),
                p.getSku().valor(),
                p.getNome(),
                p.getDescricao(),
                p.getPrecoCusto().valor(),
                p.getPrecoVenda().valor(),
                p.getQuantidadeEmEstoque(),
                p.getEstoqueMinimo(),
                p.abaixoDoMinimo(),
                p.isAtivo());
    }

    public ProdutoResumo toResumo(Produto p) {
        return new ProdutoResumo(p.getId(), p.getSku().valor(), p.getNome(),
                p.getPrecoVenda().valor(), p.getQuantidadeEmEstoque(), p.isAtivo());
    }

    public EstoqueDto.MovimentacaoResponse toResponse(MovimentacaoEstoque m) {
        return new EstoqueDto.MovimentacaoResponse(
                m.getId(),
                m.getProduto().getId(),
                m.getProduto().getSku().valor(),
                m.getTipo().name(),
                m.getQuantidade(),
                m.getSaldoApos(),
                m.getMotivo(),
                m.getDocumentoReferencia(),
                m.getDataHora(),
                m.getCriadoPor());
    }
}
