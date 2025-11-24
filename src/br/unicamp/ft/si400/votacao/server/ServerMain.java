package br.unicamp.ft.si400.votacao.server;

import javax.swing.SwingUtilities;
import br.unicamp.ft.si400.votacao.utils.Info;


public class ServerMain implements Runnable {
    
    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_FAILURE = 1;

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(new ServerMain());
        } catch (Exception exceptionValue) {
            System.err.println(Info.getShortVersion() + " encontrou um erro e não pode continuar.\nO erro foi: " + exceptionValue.getMessage());
            exceptionValue.printStackTrace();
            System.exit(EXIT_FAILURE);
        }
    }

    @Override
    public void run() {
        ServerGUI program = new ServerGUI(Info.getLongVersion());
        program.go();
    }
}
