package br.unicamp.ft.si400.votacao.client;

import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import br.unicamp.ft.si400.votacao.common.Vote;
import br.unicamp.ft.si400.votacao.common.ElectionData;
import br.unicamp.ft.si400.votacao.common.StatusUpdate;
import br.unicamp.ft.si400.votacao.common.NetworkMessage;

/**
 * Classe que gerencia a comunicação de rede para o Cliente.
 */
class ClientNetwork {

    private Socket socket;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;

    /**
     * Conecta ao servidor e recebe os dados da eleição.
     * @param ip IP do servidor
     * @param port Porta do servidor
     * @return Os dados da eleição
     * @throws Exception Se a conexão ou leitura falhar
     */
    public ElectionData connect(String ip, int port) throws Exception {
        // 1. Conectar ao servidor
        socket = new Socket(ip, port);
        
        // 2. Configurar streams (ordem inversa do servidor)
        outStream = new ObjectOutputStream(socket.getOutputStream());
        inStream = new ObjectInputStream(socket.getInputStream());

        // 3. Receber os dados da eleição
        NetworkMessage msg = (NetworkMessage) inStream.readObject();
        if (msg instanceof ElectionData data) {
            return data;
        } else {
            throw new IOException("Resposta inesperada do servidor. Esperando ElectionData.");
        }
    }

    /**
     * Envia o voto para o servidor e aguarda a resposta.
     * @param cpf CPF do votante
     * @param selectedOption Opção escolhida
     * @return O status da atualização (resposta do servidor)
     * @throws Exception Se o envio ou recebimento falhar
     */
    public StatusUpdate sendVote(String cpf, String selectedOption) throws Exception {
        if (socket == null || !socket.isConnected()) {
            throw new IOException("Não conectado ao servidor.");
        }

        // 1. Criar e enviar a primitiva de voto
        Vote vote = new Vote(cpf, selectedOption);
        outStream.writeObject(vote);
        outStream.flush();

        // 2. Aguardar a resposta (StatusUpdate)
        NetworkMessage msg = (NetworkMessage) inStream.readObject();
        
        // 3. Desconectar após o voto
        disconnect();

        if (msg instanceof StatusUpdate status) {
            return status;
        } else {
            throw new IOException("Resposta inesperada do servidor. Esperando StatusUpdate.");
        }
    }

    /**
     * Fecha os streams e o socket.
     */
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