package com.empresa.erp.modules.vendas.domain.repository;

import com.empresa.erp.modules.vendas.domain.entity.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface VendaRepository {

    Venda salvar(Venda venda);

    Optional<Venda> buscarPorId(Long id);

    Page<Venda> listar(Pageable pageable);

    Page<Venda> listarPorCliente(Long clienteId, Pageable pageable);
}
