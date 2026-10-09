package com.empresa.erp.modules.estoque.infrastructure.persistence.jpa;

import com.empresa.erp.modules.estoque.domain.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProdutoJpaRepository extends JpaRepository<Produto, Long> {

    @Query("select count(p) > 0 from Produto p where p.sku.valor = :sku")
    boolean existsBySku(@Param("sku") String sku);

    @Query("""
            select p from Produto p
            where p.ativo = true and p.quantidadeEmEstoque <= p.estoqueMinimo
            order by p.quantidadeEmEstoque asc
            """)
    List<Produto> findAbaixoDoMinimo();
}
