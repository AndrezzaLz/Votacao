package br.unicamp.ft.si400.votacao.server;

import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import br.unicamp.ft.si400.votacao.common.Vote;
import br.unicamp.ft.si400.votacao.utils.CPFValidator;
import br.unicamp.ft.si400.votacao.common.ElectionData;
import br.unicamp.ft.si400.votacao.common.StatusUpdate;
import br.unicamp.ft.si400.votacao.common.NetworkMessage;

/**
 * Lida com a comunicação de um único cliente.
 * Roda em sua própria thread (virtual).
 */
class ClientHandler implements Runnable {

    private final Socket socket;
    private final ServerNetwork server;
    private final ServerGUI gui;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;

    public ClientHandler(Socket socket, ServerNetwork server) {
        this.socket = socket;
        this.server = server;
        this.gui = server.getGui();
    }

    @Override
    public void run() {
        try {
            // Configura os Object Streams
            outStream = new ObjectOutputStream(socket.getOutputStream());
            inStream = new ObjectInputStream(socket.getInputStream());

            // 1. Envia os dados da eleição para o cliente
            ElectionData data = server.getElectionData();
            sendMessage(data);

            // 2. Aguarda o voto do cliente
            NetworkMessage msg = (NetworkMessage) inStream.readObject();

            if (msg instanceof Vote vote) {
                // 3. Recebeu um voto, agora valida
                StatusUpdate.Status status;
                
                // 3.1. Valida CPF
                if (!CPFValidator.validate(vote.cpf())) {
                    status = StatusUpdate.Status.INVALID_CPF;
                    gui.log("Cliente " + socket.getInetAddress() + " enviou CPF inválido.");
                
                // 3.2. Valida duplicidade
                } else if (!server.registerVote(vote.cpf(), vote.selectedOption())) {
                    status = StatusUpdate.Status.ALREADY_VOTED;
                    gui.log("Cliente " + socket.getInetAddress() + " (CPF: " + vote.cpf() + ") tentou votar novamente.");
                
                // 3.3. Voto aceito
                } else {
                    status = StatusUpdate.Status.VOTE_ACCEPTED;
                }

                // 4. Envia a resposta (StatusUpdate) para o cliente
                sendMessage(new StatusUpdate(status, "Obrigado por votar."));
            }

        } catch (IOException  | ClassNotFoundException e) {
            gui.log("Cliente " + socket.getInetAddress() + " desconectou: " + e.getMessage());
        } finally {
            // 5. Encerra a conexão
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                gui.log("Erro ao fechar socket do cliente: " + e.getMessage());
            }
        }
    }

    /**
     * Envia um objeto serializável (primitiva) para o cliente.
     */
    private void sendMessage(NetworkMessage msg) {
        try {
            outStream.writeObject(msg);
            outStream.flush();
        } catch (IOException e) {
            gui.log("Erro ao enviar mensagem para " + socket.getInetAddress() + ": " + e.getMessage());
        }
    }
}