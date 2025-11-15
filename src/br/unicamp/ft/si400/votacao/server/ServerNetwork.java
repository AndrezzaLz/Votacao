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

/**
 * Classe principal de rede do servidor.
 * CORRIGIDO: Nomes de coleções internas para inglês.
 */
class ServerNetwork extends Thread {

    private final ServerGUI gui; // Referência à GUI para logs e atualizações
    private final ElectionData electionData; // Dados da eleição
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    
    // Java 21: Usando Virtual Threads para escalabilidade massiva!
    private ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();

    // Estruturas de dados para gerenciar a votação (thread-safe)
    // CORRIGIDO: cpfsVotados -> votedCPFs
    private final Set<String> votedCPFs = Collections.synchronizedSet(new HashSet<>());
    // CORRIGIDO: contagemVotos -> voteCount
    private final Map<String, Integer> voteCount = Collections.synchronizedMap(new HashMap<>());
    // CORRIGIDO: listaVotantes -> voterList
    private final Map<String, String> voterList = Collections.synchronizedMap(new HashMap<>()); // CPF -> Voto

    /**
     * Construtor.
     */
    public ServerNetwork(ServerGUI gui, String question, List<String> options) {
        this.gui = gui;
        this.electionData = new ElectionData(question, options);
        // Inicializa a contagem de votos com zero para todas as opções
        for (String option : options) {
            voteCount.put(option, 0);
        }
    }

    /**
     * Inicia o servidor em uma porta específica.
     */
    public void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        isRunning = true;
        this.start(); // Inicia a thread (chama o método run())
    }

    /**
     * O loop principal da thread do servidor.
     * Aceita conexões e as despacha.
     */
    @Override
    public void run() {
        gui.log("Servidor iniciado. Aguardando conexões na porta " + serverSocket.getLocalPort() + "...");
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept(); // Bloqueia até um cliente conectar
                gui.log("Cliente conectado: " + clientSocket.getInetAddress());
                
                // Cria um handler para o cliente e o submete ao pool de threads virtuais
                ClientHandler handler = new ClientHandler(clientSocket, this);
                threadPool.submit(handler); //
                
            } catch (SocketException e) {
                if (isRunning) {
                    gui.log("ERRO DE SOCKET: " + e.getMessage());
                } else {
                    gui.log("Servidor foi parado."); // Exceção esperada ao fechar o socket
                }
            } catch (IOException e) {
                if(isRunning) {
                    gui.log("ERRO DE I/O: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Para o servidor e libera os recursos.
     */
    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            gui.log("Erro ao fechar o socket do servidor: " + e.getMessage());
        }
        
        // Desliga o pool de threads
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

    // --- Métodos de Negócio (Thread-Safe) ---

    /**
     * Tenta registrar um voto.
     * @param cpf CPF do votante
     * @param option Opção escolhida
     * @return true se o voto foi registrado, false se já votou
     */
    public boolean registerVote(String cpf, String option) {
        // Prevenção de duplicidade
        // CORRIGIDO: cpfsVotados -> votedCPFs
        if (votedCPFs.contains(cpf)) {
            return false; // Já votou
        }
        
        // Validação de opção (segurança extra)
        // CORRIGIDO: contagemVotos -> voteCount
        if (!voteCount.containsKey(option)) {
            gui.log("Voto inválido (opção desconhecida): " + option);
            throw new IllegalArgumentException("Opção de voto inválida: " + option);
        }

        // Adiciona o voto
        votedCPFs.add(cpf);
        voterList.put(cpf, option); // Salva o voto do CPF
        voteCount.compute(option, (key, count) -> (count == null ? 1 : count + 1));
        
        gui.log("Voto registrado: CPF " + cpf.substring(0, 3) + ".xxx.xxx-" + cpf.substring(9) + " | Opção: " + option);
        
        // Atualiza a GUI com os resultados parciais
        gui.updateResults(getResults());
        
        return true;
    }

    // --- Getters ---

    public boolean isRunning() {
        return isRunning;
    }
    
    public ElectionData getElectionData() {
        return electionData;
    }

    public ServerGUI getGui() {
        return gui;
    }

    /**
     * Retorna uma cópia defensiva dos resultados.
     */
    public Map<String, Integer> getResults() {
        // CORRIGIDO: contagemVotos -> voteCount
        return new HashMap<>(voteCount);
    }
    
    /**
     * Retorna uma cópia defensiva da lista de votantes.
     */
    public Map<String, String> getVoterList() {
        // CORRIGIDO: listaVotantes -> voterList
        return new HashMap<>(voterList);
    }
}