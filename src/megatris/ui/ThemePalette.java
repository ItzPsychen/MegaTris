package megatris.ui;

import java.awt.Color;

public class ThemePalette {
    public Color main, highlight, wonX, wonO, x, o, foreground, grid;

    public ThemePalette(Color m, Color h, Color wx, Color wo, Color x, Color o, Color fg, Color g) {
        this.main = m; this.highlight = h; this.wonX = wx; this.wonO = wo;
        this.x = x; this.o = o; this.foreground = fg; this.grid = g;
    }

    public static ThemePalette forName(String name) {
        if ("Light".equals(name)) {
            return new ThemePalette(
                new Color(240, 240, 240), // Sfondo bianco/grigio chiaro
                new Color(200, 200, 200), // Highlight
                new Color(180, 255, 180), new Color(255, 180, 180),
                new Color(0, 120, 215),   // Azzurro scuro
                new Color(220, 20, 60),   // Rosso
                Color.BLACK,              // TESTO NERO PER ESSERE LEGGIBILE!
                new Color(150, 150, 150)
            );
        } else if ("Retro".equals(name)) {
            return new ThemePalette(
                new Color(40, 20, 10),    // Sfondo arancio scuro/marrone (uguale al pallino)
                new Color(80, 40, 20),    // Highlight
                new Color(60, 80, 20), new Color(80, 20, 20),
                new Color(255, 150, 0),   // Arancione brillante
                new Color(255, 255, 0),   // Giallo
                new Color(255, 230, 200), // Testo beige chiaro
                new Color(120, 60, 30)
            );
        } else if ("Neon".equals(name)) {
            return new ThemePalette(
                new Color(15, 10, 30), new Color(40, 20, 60),
                new Color(20, 80, 40), new Color(80, 20, 80),
                new Color(255, 0, 255), new Color(0, 255, 255),
                Color.WHITE, new Color(80, 40, 120)
            );
        }
        // DEFAULT: Dark Mode
        return new ThemePalette(
            new Color(40, 42, 54), new Color(68, 71, 90),
            new Color(20, 60, 40), new Color(70, 20, 40),
            new Color(139, 233, 253), new Color(255, 121, 198),
            Color.WHITE, new Color(98, 114, 164)
        );
    }
}