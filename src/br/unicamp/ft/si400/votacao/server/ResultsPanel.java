package br.unicamp.ft.si400.votacao.server;

import java.awt.Font;
import java.util.Map;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;

/**
 * Painel customizado para exibir os resultados da votação
 * em forma de gráfico de barras.
 */
class ResultsPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Map<String, Integer> results;

    public ResultsPanel() {
        this.results = new LinkedHashMap<>(); // Mantém a ordem de inserção
        setBackground(Color.WHITE);
    }

    /**
     * Define as opções iniciais (com 0 votos).
     */
    public void setOptions(java.util.List<String> options) {
        results.clear();
        for (String option : options) {
            results.put(option, 0);
        }
        repaint();
    }

    /**
     * Atualiza o mapa de resultados e redesenha o painel.
     */
    public void updateResults(Map<String, Integer> newResults) {
        // Atualiza mantendo a ordem
        newResults.forEach((option, count) -> {
            if (results.containsKey(option)) {
                results.put(option, count);
            }
        });
        repaint();
    }
    
    /**
     * Limpa os resultados (para nova eleição).
     */
    public void clear() {
        results.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (results.isEmpty()) {
            g.drawString("Aguardando carregamento da eleição...", 20, 30);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        
        int barWidth = 60;
        int spacing = 40;
        int x = 50;
        int yAxis = getHeight() - 40;
        int maxVotes = Math.max(1, results.values().stream().mapToInt(Integer::intValue).max().orElse(1));
        int maxHeight = getHeight() - 80;

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Desenha as barras
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            int votes = entry.getValue();
            int barHeight = (int) (((double) votes / maxVotes) * maxHeight);
            
            g2d.setColor(Color.BLUE);
            g2d.fillRect(x, yAxis - barHeight, barWidth, barHeight);
            
            // Desenha o rótulo da opção
            g2d.setColor(Color.BLACK);
            g2d.drawString(entry.getKey(), x, yAxis + 15);
            
            // Desenha a contagem de votos
            g2d.setColor(Color.RED);
            g2d.drawString(String.valueOf(votes), x + barWidth / 2 - 5, yAxis - barHeight - 5);
            
            x += (barWidth + spacing);
        }
        
        // Desenha eixo Y
        g2d.setColor(Color.BLACK);
        g2d.drawLine(30, yAxis, 30, 40);
        g2d.drawLine(30, yAxis, getWidth() - 30, yAxis);
    }
}