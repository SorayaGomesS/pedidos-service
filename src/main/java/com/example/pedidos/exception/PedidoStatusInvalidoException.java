package com.example.pedidos.exception;

public class PedidoStatusInvalidoException extends RuntimeException {
    public PedidoStatusInvalidoException(String message) {
        super(message);
    }
}
