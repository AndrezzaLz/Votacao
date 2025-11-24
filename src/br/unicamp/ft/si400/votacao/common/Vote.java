package br.unicamp.ft.si400.votacao.common;

//encapsula o voto do usuario
public record Vote(
    String cpf, 
    String selectedOption
) implements NetworkMessage {
}
