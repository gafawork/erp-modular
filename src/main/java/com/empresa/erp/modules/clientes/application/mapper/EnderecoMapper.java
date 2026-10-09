package com.empresa.erp.modules.clientes.application.mapper;

import com.empresa.erp.modules.clientes.application.dto.EnderecoDto;
import com.empresa.erp.modules.clientes.domain.vo.Endereco;
import org.springframework.stereotype.Component;

@Component
public class EnderecoMapper {

    public Endereco toDomain(EnderecoDto dto) {
        return new Endereco(dto.logradouro(), dto.numero(), dto.complemento(), dto.bairro(),
                dto.cidade(), dto.uf(), dto.cep());
    }

    public EnderecoDto toDto(Endereco e) {
        if (e == null) {
            return null;
        }
        return new EnderecoDto(e.logradouro(), e.numero(), e.complemento(), e.bairro(),
                e.cidade(), e.uf(), e.cep());
    }
}
