package com.empresa.erp.modules.compras.application.listener;

import com.empresa.erp.modules.compras.application.service.SugestaoReposicaoService;
import com.empresa.erp.modules.estoque.events.EstoqueBaixoEvent;
import com.empresa.erp.shared.event.ListenerDeModulo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Reage a um evento do módulo Estoque. Roda após o commit de quem publicou (ex.: a venda),
 * de forma assíncrona e em transação própria: uma falha aqui não desfaz a venda.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class EstoqueBaixoListener {

    private final SugestaoReposicaoService sugestaoService;

    @ListenerDeModulo
    public void on(EstoqueBaixoEvent evento) {
        log.info("Estoque baixo para {} ({} <= {}): registrando sugestão de reposição",
                evento.sku(), evento.quantidadeAtual(), evento.estoqueMinimo());
        sugestaoService.registrar(evento.produtoId(), evento.sku(), evento.quantidadeAtual(), evento.estoqueMinimo());
    }
}
