package br.unicamp.ft.si400.votacao.client;

import javax.swing.SwingUtilities;
import br.unicamp.ft.si400.votacao.utils.Info;

// roda esse pra iniciar
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
        ClientGUI program = new ClientGUI(Info.sysName + " - Cliente");
        program.go();
    }
}
