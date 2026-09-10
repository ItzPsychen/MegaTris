package megatris.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameBackground extends JPanel implements ActionListener {

    // Parametri personalizzabili
    private final int NUM_SYMBOLS = 60; // Quante X e O vuoi a schermo
    private final Color COLOR_BG = new Color(37, 38, 54); // Il grigio/blu scuro della tua foto
    private final Color COLOR_X = new Color(90, 179, 180); // Azzurro/Teal
    private final Color COLOR_O = new Color(185, 83, 142); // Fucsia/Magenta

    private List<Symbol> symbols;
    private Random random;
    private Timer timer;
    private boolean positionsInitialized;
    private int positionsWidth;
    private int positionsHeight;

    public GameBackground() {
        this.setBackground(COLOR_BG);
        this.random = new Random();
        this.symbols = new ArrayList<>();
        this.positionsInitialized = false;
        this.positionsWidth = 0;
        this.positionsHeight = 0;

        // Inizializza i simboli
        for (int i = 0; i < NUM_SYMBOLS; i++) {
            symbols.add(new Symbol(800, 600, random)); // Partiamo con una risoluzione base 800x600
        }

        // Il timer fa da "Game Loop" aggiornando lo schermo a circa 60 FPS (16ms)
        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Attiva l'antialiasing per rendere i bordi lisci e non seghettati
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Disegna ogni simbolo
        for (Symbol s : symbols) {
            drawSymbol(g2d, s, 0.12f, s.thickness + 7f);
            drawSymbol(g2d, s, 0.62f, s.thickness);
        }
    }

    private void drawSymbol(Graphics2D g2d, Symbol symbol, float opacity, float strokeWidth) {
        Composite oldComposite = g2d.getComposite();
        var oldTransform = g2d.getTransform();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g2d.translate(symbol.x, symbol.y);
        g2d.rotate(symbol.angle);
        g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(symbol.isX ? COLOR_X : COLOR_O);
        if (symbol.isX) {
            g2d.draw(new Line2D.Double(-symbol.size / 2, -symbol.size / 2,
                    symbol.size / 2, symbol.size / 2));
            g2d.draw(new Line2D.Double(-symbol.size / 2, symbol.size / 2,
                    symbol.size / 2, -symbol.size / 2));
        } else {
            g2d.draw(new Ellipse2D.Double(-symbol.size / 2, -symbol.size / 2,
                    symbol.size, symbol.size));
        }
        g2d.setTransform(oldTransform);
        g2d.setComposite(oldComposite);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Aggiorna la logica ad ogni frame
        int width = getWidth() > 0 ? getWidth() : 800;
        int height = getHeight() > 0 ? getHeight() : 600;

        if ((!positionsInitialized || width != positionsWidth || height != positionsHeight)
                && getWidth() > 0 && getHeight() > 0) {
            for (int i = 0; i < symbols.size(); i++) {
                Symbol s = symbols.get(i);
                s.x = (i + random.nextDouble()) * width / symbols.size();
                s.y = random.nextInt(height);
            }
            positionsInitialized = true;
            positionsWidth = width;
            positionsHeight = height;
        }

        for (Symbol s : symbols) {
            s.update(height);

            // Se il simbolo esce dallo schermo in basso, lo riportiamo in alto
            if (s.y - s.size > height) {
                s.resetPosition(width, random);
            }
        }
        // Richiede di ridisegnare il pannello
        repaint();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    public static void main(String[] args) {
        MegaTris.main(args);
    }

    // Classe interna che rappresenta una singola X o O
    private class Symbol {
        double x, y;
        double speedY;
        double angle;
        double rotationSpeed;
        double size;
        float thickness;
        boolean isX;

        public Symbol(int screenWidth, int screenHeight, Random rand) {
            // Posiziona i simboli sparsi per lo schermo all'avvio
            this.x = rand.nextInt(screenWidth);
            this.y = rand.nextInt(screenHeight);
            initRandoms(rand);
        }

        public void resetPosition(int screenWidth, Random rand) {
            // Quando respawna, parte da sopra lo schermo
            this.x = rand.nextInt(screenWidth);
            this.y = -size - rand.nextInt(100); 
            initRandoms(rand);
        }

        private void initRandoms(Random rand) {
            this.isX = rand.nextBoolean();
            // Dimensione variabile
            this.size = 20 + rand.nextInt(40);
            // Spessore proporzionale alla dimensione
            this.thickness = (float) (this.size / 6.0);
            // Velocità di caduta costante, ma diversa per ogni simbolo
            this.speedY = 1.0 + rand.nextDouble() * 2.5;
            // Rotazione iniziale a caso, velocità di rotazione costante per questo simbolo
            this.angle = rand.nextDouble() * Math.PI * 2;
            this.rotationSpeed = (rand.nextDouble() - 0.5) * 0.05; // Gira in senso orario o antiorario
        }

        public void update(int screenHeight) {
            this.y += speedY;
            this.angle += rotationSpeed;
        }
    }

}