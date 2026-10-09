package com.empresa.erp.modules.compras.application.mapper;

import com.empresa.erp.modules.compras.application.dto.FornecedorDto;
import com.empresa.erp.modules.compras.domain.entity.Fornecedor;
import com.empresa.erp.modules.compras.domain.vo.Cnpj;
import com.empresa.erp.shared.domain.vo.Email;
import org.springframework.stereotype.Component;

@Component
public class FornecedorMapper {

    public Fornecedor toDomain(FornecedorDto.Request req) {
        return Fornecedor.novo(req.razaoSocial(), new Cnpj(req.cnpj()), new Email(req.email()));
    }

    public FornecedorDto.Response toResponse(Fornecedor f) {
        return new FornecedorDto.Response(f.getId(), f.getRazaoSocial(), f.getCnpj().numero(),
                f.getEmail().valor(), f.isAtivo());
    }
}
