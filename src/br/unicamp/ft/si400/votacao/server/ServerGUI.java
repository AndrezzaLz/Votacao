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

//janela principal da interface do server
class ServerGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private ServerNetwork serverNetwork;

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
    private ResultsPanel resultsPanel;
    private JLabel statusLabel;


    ServerGUI(String title) throws HeadlessException {
        super(title);
        setupWindow();
        setupMenus();
        bindMenus();
        setupPanels();
        this.setStatus("Servidor parado. Carregue uma eleição para começar.");
    }


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


    private void setupPanels() {
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log de atividade"));

        resultsPanel = new ResultsPanel();
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Resultados parciais"));
        resultsPanel.setPreferredSize(new Dimension(0, 200));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logScrollPane, resultsPanel);
        splitPane.setResizeWeight(0.7);
        this.add(splitPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Status:");
        statusPanel.add(statusLabel);
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        this.add(statusPanel, BorderLayout.SOUTH);
    }


    private void setupMenus() {
        menuBar = new JMenuBar();

        menuServidor = new JMenu("Servidor");
        menuServidor.setMnemonic('S');
        
        menuItemCarregar = new JMenuItem("Carregar eleição...", 'C');
        menuItemIniciar = new JMenuItem("Iniciar servidor", 'I');
        menuItemIniciar.setEnabled(false);
        menuItemPararSalvar = new JMenuItem("Parar e salvar relatório", 'P');
        menuItemPararSalvar.setEnabled(false);
        menuItemSair = new JMenuItem("Sair", 'R');

        menuServidor.add(menuItemCarregar);
        menuServidor.add(menuItemIniciar);
        menuServidor.add(menuItemPararSalvar);
        menuServidor.addSeparator();
        menuServidor.add(menuItemSair);

        menuAjuda = new JMenu("Ajuda");
        menuAjuda.setMnemonic('A');
        menuItemAjuda = new JMenuItem("Tópicos de ajuda", 'T');
        menuItemSobre = new JMenuItem("Sobre...", 'S');
        
        menuAjuda.add(menuItemAjuda);
        menuAjuda.add(menuItemSobre);

        menuBar.add(menuServidor);
        menuBar.add(menuAjuda);
        this.setJMenuBar(menuBar);
    }


    private void bindMenus() {
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
            new MsgScreen(this, "Ajuda - " + Info.getShortVersion(), Info.getHelpText());
        } else if (source == menuItemSobre) {
            new MsgScreen(this, "Sobre - " + Info.getShortVersion(), Info.getAboutText());
        }
    }


    private void actionLoadElection() {

        String pergunta = JOptionPane.showInputDialog(this, "Digite a pergunta da eleição:", "Carregar eleição", JOptionPane.PLAIN_MESSAGE);
        if (pergunta == null || pergunta.trim().isEmpty()) return;

        String opcoesStr = JOptionPane.showInputDialog(this, "Digite as opções, separadas por vírgula (,)", "Carregar eleição", JOptionPane.PLAIN_MESSAGE);
        if (opcoesStr == null || opcoesStr.trim().isEmpty()) return;

        List<String> opcoes = List.of(opcoesStr.split(","));
        opcoes = opcoes.stream().map(String::trim).toList();

        this.serverNetwork = new ServerNetwork(this, pergunta, opcoes);
        
        log("Eleição carregada: " + pergunta);
        for (String op : opcoes) log("- " + op);
        
        resultsPanel.setOptions(opcoes); 
        
        menuItemCarregar.setEnabled(false);
        menuItemIniciar.setEnabled(true);
        setStatus("Eleição carregada. Pronto para iniciar o servidor.");
    }


    private void actionStartServer() {
        if (serverNetwork == null) {
            JOptionPane.showMessageDialog(this, "Carregue uma eleição primeiro!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int porta = Integer.parseInt(JOptionPane.showInputDialog(this, "Digite a porta do servidor:", "Iniciar Servidor", JOptionPane.PLAIN_MESSAGE, null, null, "9876").toString());
            
            serverNetwork.startServer(porta);
            
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


    private void actionStopServerAndSave() {
        if (serverNetwork == null) return;

        serverNetwork.stopServer();
        log("Servidor parado. Aceitação de votos encerrada.");
        
        menuItemIniciar.setEnabled(true);
        menuItemPararSalvar.setEnabled(false);
        menuItemCarregar.setEnabled(true);
        setStatus("Servidor parado. Salvando relatório...");

        saveReport(serverNetwork.getResults(), serverNetwork.getVoterList());
        
        this.serverNetwork = null;
        resultsPanel.clear();
    }


    private void saveReport(Map<String, Integer> results, Map<String, String> voterList) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar relatório final");
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

    private void exitSystem() {
        if (serverNetwork != null && serverNetwork.isRunning()) {
            int res = JOptionPane.showConfirmDialog(this, 
                "O servidor está rodando. Deseja pará-lo e salvar o relatório antes de sair?", 
                "Sair", JOptionPane.YES_NO_CANCEL_OPTION);
            
            if (res == JOptionPane.YES_OPTION) {
                actionStopServerAndSave();
            } else if (res == JOptionPane.CANCEL_OPTION) {
                return; 
            }
        }
        System.exit(ServerMain.EXIT_SUCCESS); 
    }


    void go() {
        this.setVisible(true);
    }


    final void setStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }
    

    public final void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    

    public final void updateResults(Map<String, Integer> results) {
        SwingUtilities.invokeLater(() -> resultsPanel.updateResults(results));
    }
}
