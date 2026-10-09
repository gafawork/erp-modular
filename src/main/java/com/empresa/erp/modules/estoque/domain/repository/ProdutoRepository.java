package com.empresa.erp.modules.estoque.domain.repository;

import com.empresa.erp.modules.estoque.domain.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository {

    Produto salvar(Produto produto);

    Optional<Produto> buscarPorId(Long id);

    boolean existePorSku(String sku);

    Page<Produto> listar(Pageable pageable);

    List<Produto> listarAbaixoDoMinimo();
}
