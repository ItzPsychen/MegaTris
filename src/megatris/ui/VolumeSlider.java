package megatris.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class VolumeSlider extends JPanel {
    private int value; // Da 0 a 100
    private String prefix;
    private List<ActionListener> listeners = new ArrayList<>();

    private Color fgDefault = Color.WHITE;
    private Color bgHighlight = new Color(68, 71, 90);
    private Color bgMain = new Color(40, 42, 54);
    private Color fgX = new Color(139, 233, 253); // Colore riempimento

    public VolumeSlider(String prefix, int initialValue) {
        this.prefix = prefix;
        this.value = initialValue;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        MouseAdapter ma = new MouseAdapter() {
            private void update(MouseEvent e) {
                int w = getWidth();
                int padding = 28;
                int trackW = w - 2 * padding;
                if (trackW <= 0) return;
                
                // Calcola la percentuale in base a dove si trova il mouse
                int mouseX = e.getX() - padding;
                float pct = (float) mouseX / trackW;
                
                // Blocca il valore tra 0 e 1 (0% e 100%)
                pct = Math.max(0f, Math.min(1f, pct));
                int newVal = Math.round(pct * 100);
                
                if (newVal != value) {
                    setValue(newVal);
                }
            }
            
            @Override public void mousePressed(MouseEvent e) { update(e); }
            @Override public void mouseDragged(MouseEvent e) { update(e); }
            @Override public void mouseReleased(MouseEvent e) {
                // Suona il click solo quando rilasci il cursore, per non fare "mitraglietta" mentre trascini
                SoundManager.playSound("src/megatris/sound/click.wav");
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    @Override public Dimension getPreferredSize() { return new Dimension(280, 85); }
    @Override public Dimension getMinimumSize() { return new Dimension(280, 85); }
    @Override public Dimension getMaximumSize() { return new Dimension(400, 85); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int w = getWidth();
        if (w == 0) return;

        // 1. DISEGNO DEL TESTO (es: "Music Volume: 100%")
        String labelText = prefix + ": " + value + "%";
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        int textX = (w - fm.stringWidth(labelText)) / 2;
        int textY = fm.getAscent() + 5; 
        
        g2.setColor(fgDefault);
        g2.drawString(labelText, textX, textY);

        // 2. DISEGNO DELLA BARRA CONTINUA
        int trackH = 14; // Barra un po' più sottile ed elegante
        int radius = 12; // Cursore rotondo
        int padding = 28; 
        int trackY = textY + 22; 
        int trackW = w - 2 * padding;
        
        // Sfondo grigio (la parte "vuota" a destra)
        g2.setColor(bgHighlight);
        g2.fillRoundRect(padding, trackY, trackW, trackH, trackH, trackH);
        
        // Colore azzurro (la parte "piena" a sinistra)
        int fillW = (int) ((value / 100f) * trackW);
        if (fillW > 0) {
            g2.setColor(fgX);
            // Riempie il rettangolo partendo da sinistra fino al cursore
            g2.fillRoundRect(padding, trackY, fillW, trackH, trackH, trackH);
        }
        
        // 3. DISEGNO DEL PALLINO (Thumb)
        int cx = padding + fillW;
        int cy = trackY + trackH / 2;
        
        // Sfondo grigio come i pallini vuoti delle difficoltà
        g2.setColor(bgMain); 
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        // Bordo azzurro spesso 2px (come gli altri pallini)
        g2.setColor(fgX);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
    }

    public int getValue() { return value; }

    public void setValue(int v) { 
        this.value = v; 
        repaint();
        for(ActionListener l : listeners) {
            l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "changed"));
        }
    }

    public void addActionListener(ActionListener l) { listeners.add(l); }
    
    public void updateTheme(Color bgMain, Color bgHighlight, Color fgX, Color fgDefault) { 
        this.bgMain = bgMain;
        this.bgHighlight = bgHighlight;
        this.fgX = fgX;
        this.fgDefault = fgDefault;
        repaint(); 
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        repaint();
    }
}