package com.cardapio.cardapio_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Liga o agendador de tarefas do Spring. Ele é usado pelo "batimento" do
 * tempo real (PedidoStreamService), que a cada 25 segundos mantém as conexões vivas.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
