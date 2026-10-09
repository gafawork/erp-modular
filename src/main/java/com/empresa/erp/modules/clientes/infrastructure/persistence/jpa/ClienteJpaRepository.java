package com.empresa.erp.modules.clientes.infrastructure.persistence.jpa;

import com.empresa.erp.modules.clientes.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteJpaRepository extends JpaRepository<Cliente, Long> {

    @Query("select count(c) > 0 from Cliente c where c.cpf.numero = :cpf")
    boolean existsByCpf(@Param("cpf") String cpf);
}
