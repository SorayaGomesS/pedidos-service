package com.example.pedidos.controller;

import com.example.pedidos.dto.PedidoDTO.*;
import com.example.pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Cria um novo pedido")
    public ResponseEntity<PedidoResponse> criarPedido(@RequestBody CriarPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.criarPedido(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca pedido por ID")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista pedidos de um cliente")
    public ResponseEntity<List<PedidoResponse>> listarPorCliente(@RequestParam String clienteId) {
        return ResponseEntity.ok(pedidoService.listarPorCliente(clienteId));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza o status de um pedido")
    public ResponseEntity<PedidoResponse> atualizarStatus(
            @PathVariable String id,
            @RequestBody AtualizarStatusRequest request) {
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, request));
    }
}
