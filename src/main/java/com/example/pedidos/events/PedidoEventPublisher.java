package com.example.pedidos.events;

import com.example.pedidos.model.Pedido;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Responsável por publicar eventos de domínio do serviço de Pedidos.
 * Em produção, utilizar RabbitMQ ou Kafka para publicação dos eventos.
 */
@Slf4j
@Component
public class PedidoEventPublisher {

    private static final String EXCHANGE = "pedidos.exchange";

    /**
     * Publica evento pedido.criado quando um novo pedido é registrado.
     */
    public void publicarPedidoCriado(Pedido pedido) {
        PedidoCriadoEvent event = new PedidoCriadoEvent(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getValorTotal()
        );
        log.info("Publicando evento pedido.criado: {}", event);
        // rabbitTemplate.convertAndSend(EXCHANGE, "pedido.criado", event);
    }

    /**
     * Publica evento pedido.confirmado quando o pagamento é aprovado.
     */
    public void publicarPedidoConfirmado(Pedido pedido) {
        log.info("Publicando evento pedido.confirmado para pedidoId={}", pedido.getId());
        // rabbitTemplate.convertAndSend(EXCHANGE, "pedido.confirmado", pedido.getId());
    }

    /**
     * Publica evento pedido.cancelado para reverter reserva de estoque.
     */
    public void publicarPedidoCancelado(Pedido pedido) {
        log.info("Publicando evento pedido.cancelado para pedidoId={}", pedido.getId());
        // rabbitTemplate.convertAndSend(EXCHANGE, "pedido.cancelado", pedido.getId());
    }
}
