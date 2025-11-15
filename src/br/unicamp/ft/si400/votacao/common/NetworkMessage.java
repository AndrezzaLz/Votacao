package br.unicamp.ft.si400.votacao.common;

import java.io.Serializable;
/**
 * Classe abstrata base para todas as "primitivas de rede".
 * Implementa Serializable para permitir o envio via Object Streams.
 * Usar uma classe base comum é uma boa prática de POO.
 */
public interface NetworkMessage extends Serializable {
    // A interface Serializable não possui métodos, mas é necessária
    // para que os objetos possam ser escritos em um ObjectOutputStream.
}