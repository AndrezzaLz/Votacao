package br.unicamp.ft.si400.votacao.client;

import javax.swing.SwingUtilities;
import br.unicamp.ft.si400.votacao.utils.Info;

/**
 * Ponto de entrada (main) para a aplicação do Cliente.
 * Inicia a GUI na Event Dispatch Thread (EDT).
 */
public class ClientMain implements Runnable {

    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_FAILURE = 1;

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(new ClientMain());
        } catch (Exception exceptionValue) {
            System.err.println(Info.getShortVersion() + " (Cliente) encontrou um erro: " + exceptionValue.getMessage());
            exceptionValue.printStackTrace();
            System.exit(EXIT_FAILURE);
        }
    }

    @Override
    public void run() {
        // Cria e exibe a janela principal do cliente
        ClientGUI program = new ClientGUI(Info.sysName + " - Cliente");
        program.go();
    }
}