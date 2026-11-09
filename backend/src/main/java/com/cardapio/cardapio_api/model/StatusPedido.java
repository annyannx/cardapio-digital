package com.cardapio.cardapio_api.model;

/**
 * Status possíveis de um pedido (RF008).
 * No banco, o nome é guardado como texto (ex.: "EM_PREPARO").
 */
public enum StatusPedido {
    RECEBIDO,
    EM_PREPARO,
    PRONTO,
    ENTREGUE
}
