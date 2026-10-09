package com.empresa.erp.modules.compras.infrastructure.persistence.jpa;

import com.empresa.erp.modules.compras.domain.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraJpaRepository extends JpaRepository<Compra, Long> {
}
