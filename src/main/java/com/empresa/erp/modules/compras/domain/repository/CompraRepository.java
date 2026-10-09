package com.empresa.erp.modules.compras.domain.repository;

import com.empresa.erp.modules.compras.domain.entity.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CompraRepository {

    Compra salvar(Compra compra);

    Optional<Compra> buscarPorId(Long id);

    Page<Compra> listar(Pageable pageable);
}
