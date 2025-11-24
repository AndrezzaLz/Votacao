package br.unicamp.ft.si400.votacao.common;

//encapsula a resposta do servidor apos uma tentativa de voto

public record StatusUpdate(
    Status status,
    String message
) implements NetworkMessage {
        

    public enum Status {
        VOTE_ACCEPTED,
        INVALID_CPF,
        ALREADY_VOTED,
        INVALID_OPTION,
        ELECTION_CLOSED
    }
}
