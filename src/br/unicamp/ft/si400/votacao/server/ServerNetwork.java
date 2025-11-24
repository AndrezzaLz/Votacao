package br.unicamp.ft.si400.votacao.server;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import br.unicamp.ft.si400.votacao.common.ElectionData;

class ServerNetwork extends Thread {

    private final ServerGUI gui;
    private final ElectionData electionData;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    
    private ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();

    private final Set<String> votedCPFs = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Integer> voteCount = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, String> voterList = Collections.synchronizedMap(new HashMap<>()); 


    public ServerNetwork(ServerGUI gui, String question, List<String> options) {
        this.gui = gui;
        this.electionData = new ElectionData(question, options);
        for (String option : options) {
            voteCount.put(option, 0);
        }
    }


    public void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        isRunning = true;
        this.start();
    }


    @Override
    public void run() {
        gui.log("Servidor iniciado. Aguardando conexões na porta " + serverSocket.getLocalPort() + "...");
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                gui.log("Cliente conectado: " + clientSocket.getInetAddress());
                
                ClientHandler handler = new ClientHandler(clientSocket, this);
                threadPool.submit(handler);
                
            } catch (SocketException e) {
                if (isRunning) {
                    gui.log("ERRO DE SOCKET: " + e.getMessage());
                } else {
                    gui.log("Servidor foi parado.");
                }
            } catch (IOException e) {
                if(isRunning) {
                    gui.log("ERRO DE I/O: " + e.getMessage());
                }
            }
        }
    }


    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            gui.log("Erro ao fechar o socket do servidor: " + e.getMessage());
        }
        
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
        gui.log("Pool de threads de clientes desligado.");
    }


    public boolean registerVote(String cpf, String option) {

        if (votedCPFs.contains(cpf)) {
            return false;
        }
        

        if (!voteCount.containsKey(option)) {
            gui.log("Voto inválido (opção desconhecida): " + option);
            throw new IllegalArgumentException("Opção de voto inválida: " + option);
        }


        votedCPFs.add(cpf);
        voterList.put(cpf, option);
        voteCount.compute(option, (key, count) -> (count == null ? 1 : count + 1));
        
        gui.log("Voto registrado: CPF " + cpf.substring(0, 3) + ".xxx.xxx-" + cpf.substring(9) + " | Opção: " + option);
        
        gui.updateResults(getResults());
        
        return true;
    }


    public boolean isRunning() {
        return isRunning;
    }
    
    public ElectionData getElectionData() {
        return electionData;
    }

    public ServerGUI getGui() {
        return gui;
    }


    public Map<String, Integer> getResults() {
        return new HashMap<>(voteCount);
    }
    

    public Map<String, String> getVoterList() {
        return new HashMap<>(voterList);
    }
}
