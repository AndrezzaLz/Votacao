package br.unicamp.ft.si400.votacao.common;

import java.util.List;
//encapsula os dados da eleicao
public record ElectionData(
    String question, 
    List<String> options
) implements NetworkMessage {
}
