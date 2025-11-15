package br.unicamp.ft.si400.votacao.server;

import javax.swing.SwingUtilities;
import br.unicamp.ft.si400.votacao.utils.Info;

/**
 * Ponto de entrada (main) para a aplicação do Servidor.
 * Utiliza a lógica do SimpleGUI_Start para iniciar a GUI
 * na Event Dispatch Thread (EDT) de forma segura.
 */
public class ServerMain implements Runnable {
    
    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_FAILURE = 1;

    public static void main(String[] args) {
        try {
            // Inicia a GUI do servidor na thread correta
            SwingUtilities.invokeLater(new ServerMain());
        } catch (Exception exceptionValue) {
            System.err.println(Info.getShortVersion() + " encontrou um erro e não pode continuar.\nO erro foi: " + exceptionValue.getMessage());
            exceptionValue.printStackTrace();
            System.exit(EXIT_FAILURE);
        }
    }

    @Override
    public void run() {
        // Cria e exibe a janela principal do servidor
        ServerGUI program = new ServerGUI(Info.getLongVersion());
        program.go();
    }
}