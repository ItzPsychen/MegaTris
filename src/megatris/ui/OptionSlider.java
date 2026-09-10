package megatris.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class OptionSlider extends JPanel {
    private String[] options;
    private int selectedIndex = 0;
    private String prefix;
    private List<ActionListener> listeners = new ArrayList<>();
    private String[] displayOptions;

    private Color fgDefault = Color.WHITE;
    private Color bgHighlight = new Color(68, 71, 90);
    private Color bgMain = new Color(40, 42, 54);
    private Color fgX = new Color(139, 233, 253);

    private float animatedIndex = 0f;
    private Timer animTimer;

    public OptionSlider(String[] options, String prefix) {
        this.options = options;
        this.displayOptions = options.clone();
        this.prefix = prefix;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        animTimer = new Timer(16, e -> {
            float diff = selectedIndex - animatedIndex;
            if (Math.abs(diff) < 0.01f) {
                animatedIndex = selectedIndex; 
                animTimer.stop();
            } else {
                animatedIndex += diff * 0.25f; 
            }
            repaint();
        });
        
        MouseAdapter ma = new MouseAdapter() {
            private void update(MouseEvent e) {
                int w = getWidth(), num = options.length;
                int padding = 28, spacing = num > 1 ? (w - 2 * padding) / (num - 1) : 0;
                int best = 0, minD = Integer.MAX_VALUE;
                for (int i = 0; i < num; i++) {
                    int cx = padding + i * spacing;
                    int d = Math.abs(e.getX() - cx);
                    if (d < minD) { minD = d; best = i; }
                }
                if (best != selectedIndex) setSelectedIndex(best);
            }
            @Override public void mousePressed(MouseEvent e) { update(e); }
            @Override public void mouseDragged(MouseEvent e) { update(e); }
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

        // Disegno del testo
        String labelText = prefix.isEmpty() ? displayOptions[selectedIndex] : prefix + ": " + displayOptions[selectedIndex];
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        int textX = (w - fm.stringWidth(labelText)) / 2;
        int textY = fm.getAscent() + 5; 
        
        g2.setColor(fgDefault);
        g2.drawString(labelText, textX, textY);

        // CORREZIONI GRAFICHE APPLICATE QUI
        int trackH = 34; // Aumentato a 34 per contenere i 2px extra del bordo
        int radius = 16;
        int padding = 28; // Distanza dai lati
        int trackY = textY + 15; 
        
        // La barra ora parte dal bordo sinistro del primo pallino (padding - radius) 
        // e copre l'intera larghezza fino al bordo destro dell'ultimo pallino (+ 2 * radius)
        g2.setColor(bgHighlight);
        g2.fillRoundRect(padding - radius, trackY, w - 2 * padding + 2 * radius, trackH, trackH, trackH);
        
        int num = options.length;
        int spacing = num > 1 ? (w - 2 * padding) / (num - 1) : 0;
        int cy = trackY + trackH / 2;
        
        // Disegno dei pallini
        for (int i = 0; i < num; i++) {
            int cx = padding + i * spacing;
            g2.setColor(bgMain); 
            g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
            g2.setColor(fgX);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
        }

        // Pallino pieno animato
        int animCx = padding + Math.round(animatedIndex * spacing);
        g2.setColor(fgX);
        g2.fillOval(animCx - radius, cy - radius, radius * 2, radius * 2);
    }

    public int getSelectedIndex() { return selectedIndex; }

    public void setSelectedIndex(int i) { 
        selectedIndex = i; 
        animTimer.start();
        SoundManager.playSound("src/megatris/sound/click.wav");
        for(ActionListener l : listeners) {
            l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "changed"));
        }
    }

    public Object getSelectedItem() { return options[selectedIndex]; }
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

    public void setTranslatedOptions(String[] translated) {
        this.displayOptions = translated;
        repaint();
    }
}