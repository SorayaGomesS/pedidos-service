package com.example.pedidos.service;

import lombok.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Cliente HTTP para comunicação com o microsserviço de Estoque.
 * Em produção, utilizar Feign Client ou WebClient.
 */
@Component
public class EstoqueClient {

    /**
     * Consulta informações de um produto no serviço de Estoque.
     * @param produtoId ID do produto
     * @return Dados do produto (nome e preço)
     */
    public ProdutoInfo consultarProduto(String produtoId) {
        // Stub - em produção, faz chamada HTTP para o serviço de Estoque
        // Exemplo: GET http://estoque-service/api/v1/produtos/{produtoId}
        return new ProdutoInfo("Produto Exemplo", new BigDecimal("99.90"));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProdutoInfo {
        private String nome;
        private BigDecimal preco;
    }
}
