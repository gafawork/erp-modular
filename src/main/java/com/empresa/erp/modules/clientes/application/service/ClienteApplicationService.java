package com.empresa.erp.modules.clientes.application.service;

import com.empresa.erp.modules.clientes.application.dto.ClienteDto;
import com.empresa.erp.modules.clientes.application.mapper.ClienteMapper;
import com.empresa.erp.modules.clientes.application.mapper.EnderecoMapper;
import com.empresa.erp.modules.clientes.domain.entity.Cliente;
import com.empresa.erp.modules.clientes.domain.repository.ClienteRepository;
import com.empresa.erp.modules.clientes.events.ClienteInativadoEvent;
import com.empresa.erp.shared.domain.exception.DomainException;
import com.empresa.erp.shared.domain.exception.EntidadeNaoEncontradaException;
import com.empresa.erp.shared.domain.vo.Email;
import com.empresa.erp.shared.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteApplicationService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final EnderecoMapper enderecoMapper;
    private final DomainEventPublisher eventos;

    @Transactional
    public ClienteDto.Response criar(ClienteDto.Request request) {
        Cliente cliente = clienteMapper.toDomain(request);
        if (clienteRepository.existePorCpf(cliente.getCpf().numero())) {
            throw new DomainException("Já existe cliente cadastrado com o CPF " + cliente.getCpf().formatado());
        }
        return clienteMapper.toResponse(clienteRepository.salvar(cliente));
    }

    @Transactional
    public ClienteDto.Response atualizar(Long id, ClienteDto.AtualizacaoRequest request) {
        Cliente cliente = obter(id);
        cliente.atualizar(request.nome(), new Email(request.email()), enderecoMapper.toDomain(request.endereco()));
        return clienteMapper.toResponse(cliente);
    }

    @Transactional
    public void inativar(Long id) {
        obter(id).inativar();
        eventos.publicar(new ClienteInativadoEvent(id, Instant.now()));
    }

    public ClienteDto.Response buscar(Long id) {
        return clienteMapper.toResponse(obter(id));
    }

    public Page<ClienteDto.Response> listar(Pageable pageable) {
        return clienteRepository.listar(pageable).map(clienteMapper::toResponse);
    }

    private Cliente obter(Long id) {
        return clienteRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente", id));
    }
}
