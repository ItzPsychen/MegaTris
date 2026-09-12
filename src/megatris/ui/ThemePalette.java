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
                new Color(240, 240, 240), // main:       Sfondo principale
                new Color(200, 200, 200), // highlight:  Hover dei bottoni e sfondo cronologia
                new Color(180, 255, 180), // wonX:       Sfondo del quadrante quando vince X
                new Color(255, 180, 180), // wonO:       Sfondo del quadrante quando vince O
                new Color(0, 70, 200),   // x:          Colore del simbolo X e scritte X
                new Color(240, 15, 60),   // o:          Colore del simbolo O e scritte O
                Color.BLACK,              // foreground: Colore del testo generale
                new Color(90, 90, 90)  // grid:       Linee della griglia e bordi base
            );
        } else if ("Retro".equals(name)) {
            return new ThemePalette(
                new Color(40, 20, 10),    // main:       Sfondo principale
                new Color(80, 40, 20),    // highlight:  Hover dei bottoni e sfondo cronologia
                new Color(60, 80, 20),    // wonX:       Sfondo del quadrante quando vince X
                new Color(80, 20, 20),    // wonO:       Sfondo del quadrante quando vince O
                new Color(255, 150, 0),   // x:          Colore del simbolo X e scritte X
                new Color(255, 255, 0),   // o:          Colore del simbolo O e scritte O
                new Color(255, 230, 200), // foreground: Colore del testo generale
                new Color(120, 60, 30)    // grid:       Linee della griglia e bordi base
            );
        } else if ("Neon".equals(name)) {
            return new ThemePalette(
                new Color(15, 10, 30),    // main:       Sfondo principale
                new Color(40, 20, 60),    // highlight:  Hover dei bottoni e sfondo cronologia
                new Color(20, 80, 40),    // wonX:       Sfondo del quadrante quando vince X
                new Color(80, 20, 80),    // wonO:       Sfondo del quadrante quando vince O
                new Color(255, 0, 255),   // x:          Colore del simbolo X e scritte X
                new Color(0, 255, 255),   // o:          Colore del simbolo O e scritte O
                Color.WHITE,              // foreground: Colore del testo generale
                new Color(80, 40, 120)    // grid:       Linee della griglia, bordi base (e bordi bottoni!)
            );
        }
        
        // DEFAULT: Dark Mode
        return new ThemePalette(
            new Color(40, 42, 54),    // main:       Sfondo principale
            new Color(68, 71, 90),    // highlight:  Hover dei bottoni e sfondo cronologia
            new Color(20, 60, 40),    // wonX:       Sfondo del quadrante quando vince X
            new Color(70, 20, 40),    // wonO:       Sfondo del quadrante quando vince O
            new Color(139, 233, 253), // x:          Colore del simbolo X e scritte X
            new Color(255, 121, 198), // o:          Colore del simbolo O e scritte O
            Color.WHITE,              // foreground: Colore del testo generale
            new Color(98, 114, 164)   // grid:       Linee della griglia e bordi base
        );
    }
}