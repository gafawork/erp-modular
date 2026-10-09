package com.empresa.erp.modules.compras.domain.repository;

import com.empresa.erp.modules.compras.domain.entity.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FornecedorRepository {

    Fornecedor salvar(Fornecedor fornecedor);

    Optional<Fornecedor> buscarPorId(Long id);

    boolean existePorCnpj(String cnpj);

    Page<Fornecedor> listar(Pageable pageable);
}
