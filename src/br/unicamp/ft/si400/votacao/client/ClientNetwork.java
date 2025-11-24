package br.unicamp.ft.si400.votacao.client;

import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import br.unicamp.ft.si400.votacao.common.Vote;
import br.unicamp.ft.si400.votacao.common.ElectionData;
import br.unicamp.ft.si400.votacao.common.StatusUpdate;
import br.unicamp.ft.si400.votacao.common.NetworkMessage;

//comunicacao de rede para o client
class ClientNetwork {

    private Socket socket;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;

    public ElectionData connect(String ip, int port) throws Exception {
        socket = new Socket(ip, port);
        
        outStream = new ObjectOutputStream(socket.getOutputStream());
        inStream = new ObjectInputStream(socket.getInputStream());

        NetworkMessage msg = (NetworkMessage) inStream.readObject();
        if (msg instanceof ElectionData data) {
            return data;
        } else {
            throw new IOException("Resposta inesperada do servidor. Esperando ElectionData.");
        }
    }

    public StatusUpdate sendVote(String cpf, String selectedOption) throws Exception {
        if (socket == null || !socket.isConnected()) {
            throw new IOException("Não conectado ao servidor.");
        }

        Vote vote = new Vote(cpf, selectedOption);
        outStream.writeObject(vote);
        outStream.flush();

        NetworkMessage msg = (NetworkMessage) inStream.readObject();
        
        disconnect();

        if (msg instanceof StatusUpdate status) {
            return status;
        } else {
            throw new IOException("Resposta inesperada do servidor. Esperando StatusUpdate.");
        }
    }


    public void disconnect() {
        try {
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Erro ao desconectar: " + e.getMessage());
        }
    }
}
