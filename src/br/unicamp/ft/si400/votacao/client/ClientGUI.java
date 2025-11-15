package br.unicamp.ft.si400.votacao.client;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import br.unicamp.ft.si400.votacao.utils.Info;
import br.unicamp.ft.si400.votacao.utils.MsgScreen;
import br.unicamp.ft.si400.votacao.utils.CPFValidator;
import br.unicamp.ft.si400.votacao.common.ElectionData;
import br.unicamp.ft.si400.votacao.common.StatusUpdate;

/**
 * Janela principal (JFrame) da interface gráfica do Cliente.
 * CORRIGIDO: Nomes de variáveis de componentes Swing para inglês.
 */
class ClientGUI extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;

    private ClientNetwork clientNetwork; // Objeto que gerencia a rede

    // --- Componentes da GUI (Identificadores em Inglês) ---
    private JMenuBar menuBar;
    // CORRIGIDO: menuAjuda -> menuHelp
    private JMenu menuHelp;
    // CORRIGIDO: menuItemAjuda -> menuItemHelpTopics
    private JMenuItem menuItemHelpTopics;
    // CORRIGIDO: menuItemSobre -> menuItemAbout
    private JMenuItem menuItemAbout;
    
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JTextField cpfField;
    private JPanel optionsPanel; // Painel para os RadioButtons
    private JButton voteButton;
    private JLabel statusLabel;
    
    private ButtonGroup optionsGroup; // Agrupa os RadioButtons

    /**
     * Construtor da GUI do Cliente.
     */
    ClientGUI(String title) throws HeadlessException {
        super(title);
        setupWindow();
        setupMenus();
        bindMenus();
        setupMainPanel();
        setupStatusPanel();
        
        // Estado inicial
        setConnectionState(false);
        this.setStatus("Desconectado. Insira o IP/Porta e conecte-se.");
    }

    /**
     * Configura a janela principal.
     */
    private void setupWindow() {
        this.setSize(500, 400);
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
     * Configura o painel principal com os campos de entrada e votação.
     * Textos da GUI em português, identificadores em inglês.
     */
    private void setupMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Painel de Conexão
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Conexão"));
        connectionPanel.add(new JLabel("IP:"));
        ipField = new JTextField("127.0.0.1", 15);
        connectionPanel.add(ipField);
        connectionPanel.add(new JLabel("Porta:"));
        portField = new JTextField("9876", 5);
        connectionPanel.add(portField);
        connectButton = new JButton("Conectar"); // Texto da GUI em PT
        connectButton.addActionListener(this);
        connectionPanel.add(connectButton);
        
        mainPanel.add(connectionPanel, BorderLayout.NORTH);

        // 2. Painel de Votação (Centro)
        JPanel votingPanel = new JPanel(new BorderLayout(5, 5));
        votingPanel.setBorder(BorderFactory.createTitledBorder("Votação"));
        
        // 2a. CPF
        JPanel cpfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cpfPanel.add(new JLabel("CPF:"));
        cpfField = new JTextField(15);
        cpfPanel.add(cpfField);
        votingPanel.add(cpfPanel, BorderLayout.NORTH);

        // 2b. Opções (será preenchido após conexão)
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.add(new JLabel("Conecte-se ao servidor para ver as opções."));
        votingPanel.add(new JScrollPane(optionsPanel), BorderLayout.CENTER);

        // 2c. Botão de Votar
        JPanel voteButtonPanel = new JPanel();
        voteButton = new JButton("VOTAR"); // Texto da GUI em PT
        voteButton.setFont(new Font("Arial", Font.BOLD, 16));
        voteButton.addActionListener(this);
        voteButtonPanel.add(voteButton);
        votingPanel.add(voteButtonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(votingPanel, BorderLayout.CENTER);
        this.add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Configura a barra de menus (apenas Ajuda).
     * Textos da GUI em português, identificadores em inglês.
     */
    private void setupMenus() {
        menuBar = new JMenuBar();
        // CORRIGIDO: menuAjuda -> menuHelp
        menuHelp = new JMenu("Ajuda");
        menuHelp.setMnemonic('A');
        // CORRIGIDO: menuItemAjuda -> menuItemHelpTopics
        menuItemHelpTopics = new JMenuItem("Tópicos de Ajuda", 'T');
        // CORRIGIDO: menuItemSobre -> menuItemAbout
        menuItemAbout = new JMenuItem("Sobre...", 'S');
        menuHelp.add(menuItemHelpTopics);
        menuHelp.add(menuItemAbout);
        menuBar.add(menuHelp);
        this.setJMenuBar(menuBar);
    }

    /**
     * Vincula os ActionListeners aos menus.
     */
    private void bindMenus() {
        // CORRIGIDO: Nomes das variáveis
        menuItemHelpTopics.addActionListener(this);
        menuItemAbout.addActionListener(this);
    }

    /**
     * Configura o painel de status (reutilizado).
     */
    private void setupStatusPanel() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Status:");
        statusPanel.add(statusLabel);
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        this.add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * Manipulador central de ações.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == connectButton) {
            actionConnect();
        } else if (source == voteButton) {
            actionVote();
        // CORRIGIDO: Nomes das variáveis
        } else if (source == menuItemHelpTopics) {
            new MsgScreen(this, "Ajuda - " + Info.getShortVersion(), Info.getHelpText());
        } else if (source == menuItemAbout) {
            new MsgScreen(this, "Sobre - " + Info.getShortVersion(), Info.getAboutText());
        }
    }

    /**
     * Ação: Tentar conectar ao servidor.
     */
    private void actionConnect() {
        String ip = ipField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            setStatus("Porta inválida.");
            return;
        }

        setStatus("Conectando a " + ip + ":" + port + "...");
        setConnectionState(true); // Desabilita botões durante a tentativa
        
        this.clientNetwork = new ClientNetwork();
        
        // Tenta conectar e carregar dados (em uma thread separada para não travar a GUI)
        new Thread(() -> {
            try {
                ElectionData data = clientNetwork.connect(ip, port); //
                
                // Sucesso! Atualiza a GUI (na thread da GUI)
                SwingUtilities.invokeLater(() -> {
                    loadElectionOptions(data);
                    setStatus("Conectado! Por favor, identifique-se e vote.");
                });
                
            } catch (Exception e) {
                // Falha! Atualiza a GUI (na thread da GUI)
                SwingUtilities.invokeLater(() -> {
                    setStatus("Erro ao conectar: " + e.getMessage());
                    setConnectionState(false); // Reabilita
                });
            }
        }).start();
    }

    /**
     * Ação: Tentar enviar o voto.
     */
    private void actionVote() {
        // 1. Validar CPF
        String cpf = cpfField.getText().trim();
        if (!CPFValidator.validate(cpf)) {
            setStatus("CPF inválido! Verifique os dígitos.");
            JOptionPane.showMessageDialog(this, "O CPF digitado é inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Obter a opção selecionada
        String selectedOption = getSelectedOption();
        if (selectedOption == null) {
            setStatus("Selecione uma opção para votar.");
            JOptionPane.showMessageDialog(this, "Você deve selecionar uma opção.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        setStatus("Enviando voto...");
        
        // Envia o voto (em uma thread separada para não travar a GUI)
        new Thread(() -> {
            try {
                StatusUpdate response = clientNetwork.sendVote(cpf, selectedOption);
                
                // Sucesso! Processa a resposta do servidor
                SwingUtilities.invokeLater(() -> processVoteResponse(response));
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("Voto não registrado. Tente novamente: " + e.getMessage());
                    voteButton.setEnabled(true); // Permite tentar de novo
                });
            }
        }).start();
    }
    
    /**
     * Processa a resposta do servidor após o voto.
     */
    private void processVoteResponse(StatusUpdate response) {
        String message;
        int messageType;
        
        switch (response.status()) {
            case VOTE_ACCEPTED:
                message = "Voto computado com sucesso! Obrigado por participar.";
                messageType = JOptionPane.INFORMATION_MESSAGE;
                setVotingFinishedState(); // Desabilita tudo
                break;
            case ALREADY_VOTED:
                message = "Este CPF já votou nesta eleição. Voto não computado.";
                messageType = JOptionPane.ERROR_MESSAGE;
                setVotingFinishedState(); // Desabilita
                break;
            case INVALID_CPF:
                message = "O servidor rejeitou seu CPF como inválido.";
                messageType = JOptionPane.ERROR_MESSAGE;
                voteButton.setEnabled(true); // Permite corrigir
                break;
            default:
                message = "Resposta inesperada do servidor: " + response.status();
                messageType = JOptionPane.WARNING_MESSAGE;
                voteButton.setEnabled(true); // Permite tentar de novo
                break;
        }
        
        setStatus(message);
        JOptionPane.showMessageDialog(this, message, "Resultado da Votação", messageType);
    }

    /**
     * Preenche o painel de opções com os dados da eleição.
     */
    private void loadElectionOptions(ElectionData data) {
        optionsPanel.removeAll();
        optionsGroup = new ButtonGroup();
        
        optionsPanel.add(new JLabel("Pergunta: " + data.question()));
        
        for (String option : data.options()) {
            JRadioButton radioButton = new JRadioButton(option);
            radioButton.setActionCommand(option); // O comando é a própria opção
            optionsGroup.add(radioButton);
            optionsPanel.add(radioButton);
        }
        
        optionsPanel.revalidate();
        optionsPanel.repaint();
    }
    
    /**
     * Retorna o texto da opção selecionada no ButtonGroup.
     */
    private String getSelectedOption() {
        if (optionsGroup.getSelection() == null) {
            return null;
        }
        return optionsGroup.getSelection().getActionCommand();
    }

    /**
     * Habilita/desabilita campos durante a conexão.
     */
    private void setConnectionState(boolean isConnected) {
        ipField.setEnabled(!isConnected);
        portField.setEnabled(!isConnected);
        connectButton.setEnabled(!isConnected);
        
        cpfField.setEnabled(isConnected);
        optionsPanel.setEnabled(isConnected);
        voteButton.setEnabled(isConnected);
    }
    
    /**
     * Desabilita a GUI após um voto ser finalizado (aceito ou duplicado).
     */
    private void setVotingFinishedState() {
        cpfField.setEnabled(false);
        voteButton.setEnabled(false);
        for(AbstractButton button : java.util.Collections.list(optionsGroup.getElements())) {
            button.setEnabled(false);
        }
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
    void setStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }
}