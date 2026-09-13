package megatris.ui;

import javax.swing.*;
import java.awt.*;

public class RulesDialog extends JDialog {
    public RulesDialog(MegaTris parent) {
        // Applica il titolo tradotto
        super(parent, "IT".equals(parent.currentLang()) ? "REGOLE" : ("DE".equals(parent.currentLang()) ? "REGELN" : "RULES"), true);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(parent.bgMain());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Testo dinamico in 3 lingue
        String regoleTxt;
        if ("IT".equals(parent.currentLang())) {
            regoleTxt = "<html><b>1.</b> Posiziona tre simboli per vincere un mini-tavolo.<br><b>2.</b> La mossa dell'avversario determina dove giocherai dopo.</html>";
        } else if ("DE".equals(parent.currentLang())) {
            regoleTxt = "<html><b>1.</b> Platziere drei Symbole, um ein kleines Feld zu gewinnen.<br><b>2.</b> Der Zug deines Gegners bestimmt, wo du als nächstes spielen musst.</html>";
        } else {
            regoleTxt = "<html><b>1.</b> Place three symbols to win a small board.<br><b>2.</b> Your opponent's move determines where you play next.</html>";
        }

        JLabel rulesText = new JLabel(regoleTxt);
        rulesText.setForeground(parent.fgDefault());
        rulesText.setFont(new Font("Arial", Font.PLAIN, 16));
        rulesText.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel rulesImage = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/megatris/images/rules_img.png"));
            rulesImage.setIcon(icon);
        } catch (Exception e) {}
        rulesImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(rulesText);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(rulesImage);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setBackground(parent.bgMain());
        add(scrollPane);
    }
}