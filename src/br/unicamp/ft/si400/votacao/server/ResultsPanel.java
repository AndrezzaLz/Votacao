package br.unicamp.ft.si400.votacao.server;

import java.awt.Font;
import java.util.Map;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;

//exibir resultado em grafico de barrar
class ResultsPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Map<String, Integer> results;

    public ResultsPanel() {
        this.results = new LinkedHashMap<>();
        setBackground(Color.WHITE);
    }

    public void setOptions(java.util.List<String> options) {
        results.clear();
        for (String option : options) {
            results.put(option, 0);
        }
        repaint();
    }


    public void updateResults(Map<String, Integer> newResults) {
        newResults.forEach((option, count) -> {
            if (results.containsKey(option)) {
                results.put(option, count);
            }
        });
        repaint();
    }
    

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
        

        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            int votes = entry.getValue();
            int barHeight = (int) (((double) votes / maxVotes) * maxHeight);
            
            g2d.setColor(Color.MAGENTA);
            g2d.fillRect(x, yAxis - barHeight, barWidth, barHeight);
            

            g2d.setColor(Color.BLACK);
            g2d.drawString(entry.getKey(), x, yAxis + 15);
            
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString(String.valueOf(votes), x + barWidth / 2 - 5, yAxis - barHeight - 5);
            
            x += (barWidth + spacing);
        }
        
        g2d.setColor(Color.BLACK);
        g2d.drawLine(30, yAxis, 30, 40);
        g2d.drawLine(30, yAxis, getWidth() - 30, yAxis);
    }
}
