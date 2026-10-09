package com.empresa.erp.modules.clientes.application.mapper;

import com.empresa.erp.modules.clientes.api.ClienteResumo;
import com.empresa.erp.modules.clientes.application.dto.ClienteDto;
import com.empresa.erp.modules.clientes.domain.entity.Cliente;
import com.empresa.erp.modules.clientes.domain.vo.Cpf;
import com.empresa.erp.shared.domain.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClienteMapper {

    private final EnderecoMapper enderecoMapper;

    public Cliente toDomain(ClienteDto.Request req) {
        return Cliente.novo(req.nome(), new Cpf(req.cpf()), new Email(req.email()),
                enderecoMapper.toDomain(req.endereco()));
    }

    public ClienteDto.Response toResponse(Cliente c) {
        return new ClienteDto.Response(
                c.getId(),
                c.getNome(),
                c.getCpf().formatado(),
                c.getEmail().valor(),
                enderecoMapper.toDto(c.getEndereco()),
                c.isAtivo(),
                c.getCriadoEm());
    }

    public ClienteResumo toResumo(Cliente c) {
        return new ClienteResumo(c.getId(), c.getNome(), c.getCpf().numero(), c.isAtivo());
    }
}
