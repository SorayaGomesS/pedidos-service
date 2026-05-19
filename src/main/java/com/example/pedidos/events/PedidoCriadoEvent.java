package com.example.pedidos.events;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoCriadoEvent {
    private String pedidoId;
    private String clienteId;
    private BigDecimal valorTotal;
}
