package br.unicamp.ft.si400.votacao.utils;

import java.awt.Image;
import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.Graphics2D;


public final class LogoPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        final Graphics2D auxGraphics = (Graphics2D) g;
        final Image auxImage = Info.getLogoImage(); 
        
        if (auxImage == null) {
            auxGraphics.drawString("Logo não encontrado", 10, 20);
            return;
        }

        double scale = .7;
        double space = ((1.0 - scale) / 2);
        double aspectRatio = ((double) auxImage.getWidth(null) / auxImage.getHeight(null));
        int targetWidth = (int) ((this.getHeight() * scale) * aspectRatio);
        int horPosition = (this.getWidth() - targetWidth) / 2;

        auxGraphics.drawImage(auxImage, horPosition, (int) (this.getHeight() * space), 
                              horPosition + targetWidth, (int) (this.getHeight() * (1.0 - space)), 
                              0, 0, auxImage.getWidth(null), auxImage.getHeight(null), null);
    }
}
