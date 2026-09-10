package megatris.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class ThemeSelector extends JPanel {
    private String[] themes;
    private Color[] outerColors;
    private Color[] innerColors;
    private int selectedIndex;
    private Color fgDefault = Color.WHITE;
    private String prefix = "Theme";
    private String[] displayThemes;

    public ThemeSelector(String[] themes, Color[] outers, Color[] inners, int startIdx, Consumer<String> onSelect) {
        this.themes = themes;
        this.displayThemes = themes.clone();
        this.outerColors = outers;
        this.innerColors = inners;
        this.selectedIndex = startIdx;
        
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        Dimension size = new Dimension(280, 80);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(new Dimension(400, 80));
        
        addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                int w = getWidth(), num = themes.length;
                int padding = 34; // Margine laterale
                int spacing = num > 1 ? (w - 2 * padding) / (num - 1) : 0;
                int best = 0, minD = Integer.MAX_VALUE;
                
                for (int i = 0; i < num; i++) {
                    int cx = padding + i * spacing;
                    int d = Math.abs(e.getX() - cx);
                    if (d < minD) { minD = d; best = i; }
                }
                
                if (best != selectedIndex) {
                    selectedIndex = best;
                    SoundManager.playSound("src/megatris/sound/click.wav");
                    onSelect.accept(themes[selectedIndex]); // Attiva il cambio tema
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        if (w == 0) return;
        
        // Disegna il titolo del tema selezionato
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        String title = prefix + ": " + displayThemes[selectedIndex];
        g2.setColor(fgDefault);
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, fm.getAscent() + 5);
        
        int cy = 52; 
        int num = themes.length;
        int padding = 34;
        int spacing = num > 1 ? (w - 2 * padding) / (num - 1) : 0;
        int outerRadius = 14;
        int innerRadius = 7;
        
        for (int i = 0; i < num; i++) {
            int cx = padding + i * spacing;
            
            // Cerchio Esterno (Sfondo del tema)
            g2.setColor(outerColors[i]);
            g2.fillOval(cx - outerRadius, cy - outerRadius, outerRadius * 2, outerRadius * 2);
            
            // Cerchio Interno (Accento del tema)
            g2.setColor(innerColors[i]);
            g2.fillOval(cx - innerRadius, cy - innerRadius, innerRadius * 2, innerRadius * 2);
            
            // Anello di selezione
            if (i == selectedIndex) {
                g2.setColor(fgDefault);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(cx - outerRadius - 5, cy - outerRadius - 5, outerRadius * 2 + 10, outerRadius * 2 + 10);
            } else {
                g2.setColor(new Color(150, 150, 150, 80)); // Bordo sottile per i non selezionati
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(cx - outerRadius, cy - outerRadius, outerRadius * 2, outerRadius * 2);
            }
        }
    }
    
    public void updateTheme(Color fgDefault) {
        this.fgDefault = fgDefault;
        repaint();
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        repaint();
    }

    public void setTranslatedThemes(String[] translated) {
        this.displayThemes = translated;
        repaint();
    }
}