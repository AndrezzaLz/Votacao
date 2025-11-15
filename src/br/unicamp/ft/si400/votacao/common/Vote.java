package br.unicamp.ft.si400.votacao.common;

/**
 * Primitiva de rede (Record) que encapsula o voto do usuário.
 * Enviado do Cliente para o Servidor.
 */
public record Vote(
    String cpf, 
    String selectedOption
) implements NetworkMessage {
}