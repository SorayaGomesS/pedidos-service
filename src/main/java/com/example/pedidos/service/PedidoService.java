package com.example.pedidos.service;

import com.example.pedidos.dto.PedidoDTO.*;
import com.example.pedidos.events.PedidoEventPublisher;
import com.example.pedidos.exception.PedidoNotFoundException;
import com.example.pedidos.exception.PedidoStatusInvalidoException;
import com.example.pedidos.model.*;
import com.example.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final EstoqueClient estoqueClient;
    private final PedidoEventPublisher eventPublisher;

    @Transactional
    public PedidoResponse criarPedido(CriarPedidoRequest request) {
        log.info("Criando pedido para clienteId={}", request.getClienteId());

        if (request.getClienteId() == null || request.getClienteId().isBlank()) {
            throw new IllegalArgumentException("clienteId é obrigatório");
        }
        if (request.getItens() == null || request.getItens().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter ao menos um item");
        }

        List<ItemPedido> itens = request.getItens().stream().map(itemReq -> {
            if (itemReq.getQuantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade deve ser maior que zero");
            }
            // Consulta estoque e preço
            var produtoInfo = estoqueClient.consultarProduto(itemReq.getProdutoId());
            BigDecimal subtotal = produtoInfo.getPreco().multiply(BigDecimal.valueOf(itemReq.getQuantidade()));

            return ItemPedido.builder()
                    .produtoId(itemReq.getProdutoId())
                    .nomeProduto(produtoInfo.getNome())
                    .quantidade(itemReq.getQuantidade())
                    .precoUnitario(produtoInfo.getPreco())
                    .subtotal(subtotal)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal valorTotal = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Pedido pedido = Pedido.builder()
                .clienteId(request.getClienteId())
                .itens(itens)
                .valorTotal(valorTotal)
                .status(StatusPedido.PENDENTE)
                .build();

        pedido = pedidoRepository.save(pedido);
        eventPublisher.publicarPedidoCriado(pedido);

        log.info("Pedido criado com id={}", pedido.getId());
        return toResponse(pedido);
    }

    public PedidoResponse buscarPorId(String id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException("Pedido não encontrado: " + id));
        return toResponse(pedido);
    }

    public List<PedidoResponse> listarPorCliente(String clienteId) {
        return pedidoRepository.findByClienteId(clienteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PedidoResponse atualizarStatus(String id, AtualizarStatusRequest request) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException("Pedido não encontrado: " + id));

        validarTransicaoStatus(pedido.getStatus(), request.getStatus());
        pedido.setStatus(request.getStatus());
        pedido = pedidoRepository.save(pedido);

        if (request.getStatus() == StatusPedido.CANCELADO) {
            eventPublisher.publicarPedidoCancelado(pedido);
        } else if (request.getStatus() == StatusPedido.CONFIRMADO) {
            eventPublisher.publicarPedidoConfirmado(pedido);
        }

        return toResponse(pedido);
    }

    private void validarTransicaoStatus(StatusPedido atual, StatusPedido novo) {
        if (atual == StatusPedido.CANCELADO || atual == StatusPedido.ENTREGUE) {
            throw new PedidoStatusInvalidoException(
                    "Não é possível alterar um pedido com status: " + atual);
        }
        if (novo == StatusPedido.PENDENTE) {
            throw new PedidoStatusInvalidoException("Não é possível voltar ao status PENDENTE");
        }
    }

    private PedidoResponse toResponse(Pedido pedido) {
        List<ItemResponse> itensResponse = pedido.getItens().stream()
                .map(item -> ItemResponse.builder()
                        .produtoId(item.getProdutoId())
                        .nomeProduto(item.getNomeProduto())
                        .quantidade(item.getQuantidade())
                        .precoUnitario(item.getPrecoUnitario())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return PedidoResponse.builder()
                .id(pedido.getId())
                .clienteId(pedido.getClienteId())
                .status(pedido.getStatus())
                .itens(itensResponse)
                .valorTotal(pedido.getValorTotal())
                .criadoEm(pedido.getCriadoEm())
                .atualizadoEm(pedido.getAtualizadoEm())
                .build();
    }
}
