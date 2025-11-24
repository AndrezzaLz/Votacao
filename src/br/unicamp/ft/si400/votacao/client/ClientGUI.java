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

// janela principal da interface do client
class ClientGUI extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;

    private ClientNetwork clientNetwork;

    private JMenuBar menuBar;
    private JMenu menuHelp;
    private JMenuItem menuItemHelpTopics;
    private JMenuItem menuItemAbout;
    
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JTextField cpfField;
    private JPanel optionsPanel;
    private JButton voteButton;
    private JLabel statusLabel;
    
    private ButtonGroup optionsGroup;


    ClientGUI(String title) throws HeadlessException {
        super(title);
        setupWindow();
        setupMenus();
        bindMenus();
        setupMainPanel();
        setupStatusPanel();
        
        setConnectionState(false);
        this.setStatus("Desconectado. Insira o IP/Porta e conecte-se.");
    }


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


    private void setupMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Conexão"));
        connectionPanel.add(new JLabel("IP:"));
        ipField = new JTextField("127.0.0.1", 15);
        connectionPanel.add(ipField);
        connectionPanel.add(new JLabel("Porta:"));
        portField = new JTextField("9876", 5);
        connectionPanel.add(portField);
        connectButton = new JButton("Conectar"); 
        connectButton.addActionListener(this);
        connectionPanel.add(connectButton);
        
        mainPanel.add(connectionPanel, BorderLayout.NORTH);

        JPanel votingPanel = new JPanel(new BorderLayout(5, 5));
        votingPanel.setBorder(BorderFactory.createTitledBorder("Votação"));
        
        JPanel cpfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cpfPanel.add(new JLabel("CPF:"));
        cpfField = new JTextField(15);
        cpfPanel.add(cpfField);
        votingPanel.add(cpfPanel, BorderLayout.NORTH);

        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.add(new JLabel("Conecte-se ao servidor para ver as opções."));
        votingPanel.add(new JScrollPane(optionsPanel), BorderLayout.CENTER);

        JPanel voteButtonPanel = new JPanel();
        voteButton = new JButton("VOTAR");
        voteButton.setFont(new Font("Arial", Font.BOLD, 16));
        voteButton.addActionListener(this);
        voteButtonPanel.add(voteButton);
        votingPanel.add(voteButtonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(votingPanel, BorderLayout.CENTER);
        this.add(mainPanel, BorderLayout.CENTER);
    }
    

    private void setupMenus() {
        menuBar = new JMenuBar();
        menuHelp = new JMenu("Ajuda");
        menuHelp.setMnemonic('A');
        menuItemHelpTopics = new JMenuItem("Tópicos de Ajuda", 'T');
        menuItemAbout = new JMenuItem("Sobre...", 'S');
        menuHelp.add(menuItemHelpTopics);
        menuHelp.add(menuItemAbout);
        menuBar.add(menuHelp);
        this.setJMenuBar(menuBar);
    }


    private void bindMenus() {
        menuItemHelpTopics.addActionListener(this);
        menuItemAbout.addActionListener(this);
    }


    private void setupStatusPanel() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Status:");
        statusPanel.add(statusLabel);
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        this.add(statusPanel, BorderLayout.SOUTH);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == connectButton) {
            actionConnect();
        } else if (source == voteButton) {
            actionVote();
        } else if (source == menuItemHelpTopics) {
            new MsgScreen(this, "Ajuda - " + Info.getShortVersion(), Info.getHelpText());
        } else if (source == menuItemAbout) {
            new MsgScreen(this, "Sobre - " + Info.getShortVersion(), Info.getAboutText());
        }
    }


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
        setConnectionState(true);
        
        this.clientNetwork = new ClientNetwork();
        
        new Thread(() -> {
            try {
                ElectionData data = clientNetwork.connect(ip, port); //
                
                SwingUtilities.invokeLater(() -> {
                    loadElectionOptions(data);
                    setStatus("Conectado! Por favor, identifique-se e vote.");
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("Erro ao conectar: " + e.getMessage());
                    setConnectionState(false);
                });
            }
        }).start();
    }


    private void actionVote() {
        String cpf = cpfField.getText().trim();
        if (!CPFValidator.validate(cpf)) {
            setStatus("CPF inválido! Verifique os dígitos.");
            JOptionPane.showMessageDialog(this, "O CPF digitado é inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedOption = getSelectedOption();
        if (selectedOption == null) {
            setStatus("Selecione uma opção para votar.");
            JOptionPane.showMessageDialog(this, "Você deve selecionar uma opção.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        setStatus("Enviando voto...");
        
        new Thread(() -> {
            try {
                StatusUpdate response = clientNetwork.sendVote(cpf, selectedOption);
                
                SwingUtilities.invokeLater(() -> processVoteResponse(response));
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setStatus("Voto não registrado. Tente novamente: " + e.getMessage());
                    voteButton.setEnabled(true);
                });
            }
        }).start();
    }
    

    private void processVoteResponse(StatusUpdate response) {
        String message;
        int messageType;
        
        switch (response.status()) {
            case VOTE_ACCEPTED:
                message = "Voto computado com sucesso! Obrigado por participar.";
                messageType = JOptionPane.INFORMATION_MESSAGE;
                setVotingFinishedState();
                break;
            case ALREADY_VOTED:
                message = "Este CPF já votou nesta eleição. Voto não computado.";
                messageType = JOptionPane.ERROR_MESSAGE;
                setVotingFinishedState();
                break;
            case INVALID_CPF:
                message = "O servidor rejeitou seu CPF como inválido.";
                messageType = JOptionPane.ERROR_MESSAGE;
                voteButton.setEnabled(true);
                break;
            default:
                message = "Resposta inesperada do servidor: " + response.status();
                messageType = JOptionPane.WARNING_MESSAGE;
                voteButton.setEnabled(true);
                break;
        }
        
        setStatus(message);
        JOptionPane.showMessageDialog(this, message, "Resultado da votação", messageType);
    }


    private void loadElectionOptions(ElectionData data) {
        optionsPanel.removeAll();
        optionsGroup = new ButtonGroup();
        
        optionsPanel.add(new JLabel("Pergunta: " + data.question()));
        
        for (String option : data.options()) {
            JRadioButton radioButton = new JRadioButton(option);
            radioButton.setActionCommand(option);
            optionsGroup.add(radioButton);
            optionsPanel.add(radioButton);
        }
        
        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private String getSelectedOption() {
        if (optionsGroup.getSelection() == null) {
            return null;
        }
        return optionsGroup.getSelection().getActionCommand();
    }


    private void setConnectionState(boolean isConnected) {
        ipField.setEnabled(!isConnected);
        portField.setEnabled(!isConnected);
        connectButton.setEnabled(!isConnected);
        
        cpfField.setEnabled(isConnected);
        optionsPanel.setEnabled(isConnected);
        voteButton.setEnabled(isConnected);
    }
    

    private void setVotingFinishedState() {
        cpfField.setEnabled(false);
        voteButton.setEnabled(false);
        for(AbstractButton button : java.util.Collections.list(optionsGroup.getElements())) {
            button.setEnabled(false);
        }
    }

    void go() {
        this.setVisible(true);
    }


    void setStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }
}
