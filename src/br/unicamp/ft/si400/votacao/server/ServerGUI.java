package br.unicamp.ft.si400.votacao.server;

import java.awt.*;
import java.io.File;
import java.util.Map;
import javax.swing.*;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import br.unicamp.ft.si400.votacao.utils.Info;
import br.unicamp.ft.si400.votacao.utils.MsgScreen;

/**
 * Janela principal (JFrame) da interface gráfica do servidor. [cite: 36]
 * Baseada na estrutura de 'MainWindow.java'.
 * * Responsável por:
 * - Iniciar/Parar o servidor de rede.
 * - Carregar os dados da eleição. [cite: 19]
 * - Exibir logs e resultados gráficos. [cite: 22]
 * - Salvar o relatório final. [cite: 23]
 */
class ServerGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private ServerNetwork serverNetwork; // O objeto que gerencia a rede

    // --- Componentes da GUI ---
    private JMenuBar menuBar;
    private JMenu menuServidor;
    private JMenu menuAjuda;
    private JMenuItem menuItemCarregar;
    private JMenuItem menuItemIniciar;
    private JMenuItem menuItemPararSalvar;
    private JMenuItem menuItemSair;
    private JMenuItem menuItemAjuda;
    private JMenuItem menuItemSobre;
    
    private JTextArea logArea;
    private ResultsPanel resultsPanel; // Painel para resultados gráficos
    private JLabel statusLabel;

    /**
     * Construtor da GUI do Servidor.
     */
    ServerGUI(String title) throws HeadlessException {
        super(title);
        setupWindow();
        setupMenus();
        bindMenus();
        setupPanels();
        this.setStatus("Servidor parado. Carregue uma eleição para começar.");
    }

    /**
     * Configura a janela principal.
     */
    private void setupWindow() {    
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout(5, 5));
        
        try {
            this.setIconImage(Info.getLogoImage());
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone: " + e.getMessage());
        }
    }

    /**
     * Configura os painéis centrais (Log e Gráfico).
     */
    private void setupPanels() {
        // Painel de Log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log de Atividade"));

        // Painel de Resultados Gráficos [cite: 22]
        resultsPanel = new ResultsPanel();
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Resultados Parciais"));
        resultsPanel.setPreferredSize(new Dimension(0, 200)); // Altura fixa

        // Split Pane para dividir Log e Gráfico
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logScrollPane, resultsPanel);
        splitPane.setResizeWeight(0.7); // 70% para o log
        this.add(splitPane, BorderLayout.CENTER);

        // Painel de Status (reutilizado de MainWindow)
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Status:");
        statusPanel.add(statusLabel);
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        this.add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * Configura a barra de menus.
     */
    private void setupMenus() {
        menuBar = new JMenuBar();

        // Menu "Servidor"
        menuServidor = new JMenu("Servidor");
        menuServidor.setMnemonic('S');
        
        menuItemCarregar = new JMenuItem("Carregar Eleição...", 'C');
        menuItemIniciar = new JMenuItem("Iniciar Servidor", 'I');
        menuItemIniciar.setEnabled(false); // Desabilitado até carregar eleição
        menuItemPararSalvar = new JMenuItem("Parar e Salvar Relatório", 'P');
        menuItemPararSalvar.setEnabled(false); // Desabilitado até iniciar
        menuItemSair = new JMenuItem("Sair", 'R');

        menuServidor.add(menuItemCarregar);
        menuServidor.add(menuItemIniciar);
        menuServidor.add(menuItemPararSalvar);
        menuServidor.addSeparator();
        menuServidor.add(menuItemSair);

        // Menu "Ajuda" (Reutilizado) 
        menuAjuda = new JMenu("Ajuda");
        menuAjuda.setMnemonic('A');
        menuItemAjuda = new JMenuItem("Tópicos de Ajuda", 'T');
        menuItemSobre = new JMenuItem("Sobre...", 'S');
        
        menuAjuda.add(menuItemAjuda);
        menuAjuda.add(menuItemSobre);

        menuBar.add(menuServidor);
        menuBar.add(menuAjuda);
        this.setJMenuBar(menuBar);
    }

    /**
     * Vincula os ActionListeners aos menus.
     */
    private void bindMenus() {
        // Vincula todos os JMenuItems ao 'this' (ActionListener)
        for (Component menu : menuBar.getComponents()) {
            if (menu instanceof JMenu jMenu) {
                for (Component item : jMenu.getMenuComponents()) {
                    if (item instanceof JMenuItem jMenuItem) {
                        jMenuItem.addActionListener(this);
                    }
                }
            }
        }
    }

    /**
     * Manipulador central de ações dos menus.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == menuItemSair) {
            exitSystem();
        } else if (source == menuItemCarregar) {
            actionLoadElection();
        } else if (source == menuItemIniciar) {
            actionStartServer();
        } else if (source == menuItemPararSalvar) {
            actionStopServerAndSave();
        } else if (source == menuItemAjuda) {
            // Reutiliza MsgScreen 
            new MsgScreen(this, "Ajuda - " + Info.getShortVersion(), Info.getHelpText());
        } else if (source == menuItemSobre) {
            // Reutiliza MsgScreen 
            new MsgScreen(this, "Sobre - " + Info.getShortVersion(), Info.getAboutText());
        }
    }

    /**
     * Ação: Carrega os dados da eleição [cite: 19]
     */
    private void actionLoadElection() {
        // (Em um projeto real, isso poderia vir de um arquivo)
        // Por simplicidade, usamos JOptionPane
        String pergunta = JOptionPane.showInputDialog(this, "Digite a pergunta da eleição:", "Carregar Eleição", JOptionPane.PLAIN_MESSAGE);
        if (pergunta == null || pergunta.trim().isEmpty()) return;

        String opcoesStr = JOptionPane.showInputDialog(this, "Digite as opções, separadas por vírgula (,)", "Carregar Eleição", JOptionPane.PLAIN_MESSAGE);
        if (opcoesStr == null || opcoesStr.trim().isEmpty()) return;

        List<String> opcoes = List.of(opcoesStr.split(","));
        opcoes = opcoes.stream().map(String::trim).toList();

        // Cria a instância do servidor de rede (mas ainda não inicia)
        this.serverNetwork = new ServerNetwork(this, pergunta, opcoes);
        
        log("Eleição carregada: " + pergunta);
        for (String op : opcoes) log("- " + op);
        
        resultsPanel.setOptions(opcoes); // Configura o painel gráfico
        
        menuItemCarregar.setEnabled(false);
        menuItemIniciar.setEnabled(true);
        setStatus("Eleição carregada. Pronto para iniciar o servidor.");
    }

    /**
     * Ação: Inicia o servidor de rede.
     */
    private void actionStartServer() {
        if (serverNetwork == null) {
            JOptionPane.showMessageDialog(this, "Carregue uma eleição primeiro!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int porta = Integer.parseInt(JOptionPane.showInputDialog(this, "Digite a porta do servidor:", "Iniciar Servidor", JOptionPane.PLAIN_MESSAGE, null, null, "9876").toString());
            
            serverNetwork.startServer(porta); // Inicia a thread do servidor
            
            menuItemIniciar.setEnabled(false);
            menuItemPararSalvar.setEnabled(true);
            setStatus("Servidor rodando na porta " + porta + ". Aguardando clientes...");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Porta inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (HeadlessException | IOException ex) {
            log("ERRO AO INICIAR SERVIDOR: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Erro ao iniciar servidor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Ação: Para o servidor e salva o relatório final. [cite: 23]
     */
    private void actionStopServerAndSave() {
        if (serverNetwork == null) return;

        serverNetwork.stopServer();
        log("Servidor parado. Aceitação de votos encerrada.");
        
        menuItemIniciar.setEnabled(true); // Pode reiniciar se quiser
        menuItemPararSalvar.setEnabled(false);
        menuItemCarregar.setEnabled(true); // Pode carregar nova eleição
        setStatus("Servidor parado. Salvando relatório...");

        // Salvar Relatório [cite: 23]
        saveReport(serverNetwork.getResults(), serverNetwork.getVoterList());
        
        this.serverNetwork = null; // Limpa para próxima eleição
        resultsPanel.clear();
    }

    /**
     * Salva o relatório final em um arquivo de texto.
     */
    private void saveReport(Map<String, Integer> results, Map<String, String> voterList) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório Final");
        fileChooser.setSelectedFile(new File("Relatorio_Votacao.txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                
                writer.println("--- RELATÓRIO FINAL DA VOTAÇÃO ---");
                writer.println("Sistema: " + Info.getLongVersion());
                writer.println("Pergunta: " + serverNetwork.getElectionData().question());
                writer.println("------------------------------------");
                writer.println("\n--- RESULTADO DA APURAÇÃO ---");
                
                results.forEach((option, count) -> {
                    writer.println(String.format("%s: %d voto(s)", option, count));
                });
                
                writer.println("\n--- LISTA DE VOTANTES ---");
                writer.println("(CPF | Voto Registrado)");
                
                voterList.forEach((cpf, vote) -> {
                     writer.println(String.format("%s | %s", cpf, vote));
                });
                
                log("Relatório salvo com sucesso em: " + file.getAbsolutePath());
                setStatus("Relatório salvo. Servidor ocioso.");
                
            } catch (IOException e) {
                log("ERRO AO SALVAR RELATÓRIO: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Erro ao salvar relatório: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Encerra o sistema.
     */
    private void exitSystem() {
        if (serverNetwork != null && serverNetwork.isRunning()) {
            int res = JOptionPane.showConfirmDialog(this, 
                "O servidor está rodando. Deseja pará-lo e salvar o relatório antes de sair?", 
                "Sair", JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (res == JOptionPane.YES_OPTION) {
                actionStopServerAndSave();
            } else if (res == JOptionPane.CANCEL_OPTION) {
                return; // Cancela a saída
            }
        }
        System.exit(ServerMain.EXIT_SUCCESS); // (Usando a constante do projeto original)
    }

    /**
     * Torna a janela visível.
     */
    void go() {
        this.setVisible(true);
    }

    /**
     * Define a mensagem na barra de status (thread-safe).
     */
    final void setStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }
    
    /**
     * Adiciona uma mensagem ao log (thread-safe).
     */
    public final void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll
        });
    }
    
    /**
     * Atualiza o painel gráfico de resultados (thread-safe). [cite: 22]
     */
    public final void updateResults(Map<String, Integer> results) {
        SwingUtilities.invokeLater(() -> resultsPanel.updateResults(results));
    }
}