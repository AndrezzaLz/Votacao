package br.unicamp.ft.si400.votacao.utils;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Insets;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.LineBorder;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;


public final class MsgScreen extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private final JButton     closeButton;
    private final JPanel      panelButton;
    private final JPanel      panelLogo;
    private final JTextArea   textArea;
    private final JScrollPane scrollPane;

    public MsgScreen(JFrame owner, String title, String message) throws HeadlessException {
        super(owner, title);
        setSize(800, 320);
        setResizable(false);
        setLocationRelativeTo(owner.getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        textArea = new JTextArea();
        textArea.setText(message);
        formatTextArea();

        scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(textArea);
        scrollPane.setBackground(Color.white);
        scrollPane.setBorder(new TitledBorder(new LineBorder(Color.gray), Info.author));

        add(scrollPane, BorderLayout.CENTER);

        panelLogo = new LogoPanel();
        panelLogo.setPreferredSize(new Dimension(200, 220));
        panelLogo.setBorder(new TitledBorder(new LineBorder(Color.gray), Info.sysName));
        panelLogo.setBackground(Color.white);
        add(panelLogo, BorderLayout.WEST);

        panelButton = new JPanel();
        closeButton = new JButton("Fechar"); 
        closeButton.addActionListener(this);
        panelButton.add(closeButton);
        add(panelButton, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        this.setVisible(false);
    }

    private void formatTextArea() {
        textArea.setForeground(Color.black);
        textArea.setBackground(Color.white);
        textArea.setEditable(false);
        textArea.setFocusable(true);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 20, 15, 20));
        textArea.setFont(new Font("Arial", Font.BOLD, 12));
        textArea.setCaretPosition(0);
    }
}
