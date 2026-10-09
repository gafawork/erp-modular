package com.empresa.erp.modules.clientes.events;

import java.time.Instant;

public record ClienteInativadoEvent(Long clienteId, Instant ocorridoEm) {
}
