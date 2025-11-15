package br.unicamp.ft.si400.votacao.common;

/**
 * Primitiva de rede (Record) que encapsula a resposta do servidor
 * após uma tentativa de voto.
 */
public record StatusUpdate(
    Status status,
    String message
) implements NetworkMessage {
        
    /**
     * Enum para representar os possíveis status da validação do voto.
     */
    public enum Status {
        VOTE_ACCEPTED,    // Voto aceito com sucesso
        INVALID_CPF,      // CPF não passou na validação
        ALREADY_VOTED,    // CPF já votou nesta eleição
        INVALID_OPTION,   // A opção de voto não existe
        ELECTION_CLOSED   // A eleição não está mais aceitando votos
    }
}