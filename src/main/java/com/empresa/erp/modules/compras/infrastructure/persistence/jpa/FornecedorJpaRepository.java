package com.empresa.erp.modules.compras.infrastructure.persistence.jpa;

import com.empresa.erp.modules.compras.domain.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FornecedorJpaRepository extends JpaRepository<Fornecedor, Long> {

    @Query("select count(f) > 0 from Fornecedor f where f.cnpj.numero = :cnpj")
    boolean existsByCnpj(@Param("cnpj") String cnpj);
}
