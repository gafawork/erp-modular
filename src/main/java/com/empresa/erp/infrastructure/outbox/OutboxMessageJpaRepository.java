package com.empresa.erp.infrastructure.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessageJpaRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findByEnviadoEmIsNullOrderByIdAsc(Pageable pageable);

    long countByEnviadoEmIsNotNull();

    long countByEnviadoEmIsNull();
}
