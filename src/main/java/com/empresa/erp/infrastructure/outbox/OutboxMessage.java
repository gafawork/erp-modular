package com.empresa.erp.infrastructure.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Mensagem pendente de envio para um sistema externo (padrão Transactional Outbox). */
@Getter
@Entity
@Table(name = "outbox_message", schema = "plataforma",
        indexes = @Index(name = "ix_outbox_pendentes", columnList = "enviado_em, id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String destino;

    @Column(nullable = false, length = 300)
    private String tipo;

    @Column(nullable = false, length = 10000)
    private String payload;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "enviado_em")
    private Instant enviadoEm;

    @Column(nullable = false)
    private int tentativas;

    @Column(name = "ultimo_erro", length = 1000)
    private String ultimoErro;

    public static OutboxMessage nova(String destino, String tipo, String payload) {
        OutboxMessage m = new OutboxMessage();
        m.destino = destino;
        m.tipo = tipo;
        m.payload = payload;
        m.criadoEm = Instant.now();
        return m;
    }

    public void marcarEnviada() {
        this.tentativas++;
        this.enviadoEm = Instant.now();
        this.ultimoErro = null;
    }

    public void registrarFalha(String erro) {
        this.tentativas++;
        this.ultimoErro = erro == null ? null : erro.substring(0, Math.min(erro.length(), 1000));
    }
}
