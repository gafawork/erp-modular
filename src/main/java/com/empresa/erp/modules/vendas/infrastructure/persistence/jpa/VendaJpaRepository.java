package com.empresa.erp.modules.vendas.infrastructure.persistence.jpa;

import com.empresa.erp.modules.vendas.domain.entity.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendaJpaRepository extends JpaRepository<Venda, Long> {

    Page<Venda> findByClienteId(Long clienteId, Pageable pageable);
}
