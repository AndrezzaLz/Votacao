package br.unicamp.ft.si400.votacao.common;

import java.util.List;
/**
 * Primitiva de rede (Record) que encapsula os dados da eleição.
 * Enviada do Servidor para o Cliente.
 * * Usamos um 'record' (Java 16+) para imutabilidade e concisão.
 */
public record ElectionData(
    String question, 
    List<String> options
) implements NetworkMessage {
    // 'record' implementa automaticamente Serializable se seus campos forem
}