package com.empresa.erp.modules.compras.application.service;

import com.empresa.erp.modules.compras.application.dto.FornecedorDto;
import com.empresa.erp.modules.compras.application.mapper.FornecedorMapper;
import com.empresa.erp.modules.compras.domain.entity.Fornecedor;
import com.empresa.erp.modules.compras.domain.repository.FornecedorRepository;
import com.empresa.erp.shared.domain.exception.DomainException;
import com.empresa.erp.shared.domain.exception.EntidadeNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FornecedorApplicationService {

    private final FornecedorRepository fornecedorRepository;
    private final FornecedorMapper fornecedorMapper;

    @Transactional
    public FornecedorDto.Response criar(FornecedorDto.Request request) {
        Fornecedor fornecedor = fornecedorMapper.toDomain(request);
        if (fornecedorRepository.existePorCnpj(fornecedor.getCnpj().numero())) {
            throw new DomainException("Já existe fornecedor com o CNPJ " + fornecedor.getCnpj().numero());
        }
        return fornecedorMapper.toResponse(fornecedorRepository.salvar(fornecedor));
    }

    @Transactional
    public void inativar(Long id) {
        obter(id).inativar();
    }

    public FornecedorDto.Response buscar(Long id) {
        return fornecedorMapper.toResponse(obter(id));
    }

    public Page<FornecedorDto.Response> listar(Pageable pageable) {
        return fornecedorRepository.listar(pageable).map(fornecedorMapper::toResponse);
    }

    private Fornecedor obter(Long id) {
        return fornecedorRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Fornecedor", id));
    }
}
