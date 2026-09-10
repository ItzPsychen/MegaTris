package megatris.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.*;

/** Shared styling and sizing for the compact MegaTris controls. */
final class UiStyler {
    static final int CONTROL_HEIGHT = 30;
    static final int MENU_BUTTON_WIDTH = 280;
    static final int MENU_BUTTON_HEIGHT = 48;
    static final Font CONTROL_FONT = new Font("Arial", Font.BOLD, 14);

    private UiStyler() {}

    static void titleButton(JButton button) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        button.setFont(CONTROL_FONT);
    }

    static void minimalButton(AbstractButton button) {
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        
        if (button instanceof JButton || button instanceof JToggleButton) {
            button.setUI(new BasicButtonUI() {
                @Override
                public void paint(Graphics graphics, JComponent component) {
                    AbstractButton currentButton = (AbstractButton) component;
                    Graphics2D copy = (Graphics2D) graphics.create();
                    copy.setColor(currentButton.getBackground());
                    copy.fillRect(0, 0, component.getWidth(), component.getHeight());
                    super.paint(copy, component);
                    copy.dispose();
                }
            });
        }
        button.setMargin(new Insets(4, 10, 4, 10));
        button.setFont(CONTROL_FONT);
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, CONTROL_HEIGHT));
        button.setMinimumSize(new Dimension(0, CONTROL_HEIGHT));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, CONTROL_HEIGHT));
        
        // --- LA VERA SOLUZIONE AL BUG ---
        // Sostituiamo la logica difettosa di Java con un controllo manuale inattaccabile
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.putClientProperty("isHovered", true);
                updateButtonState(button);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.putClientProperty("isHovered", false);
                updateButtonState(button);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.putClientProperty("isPressed", true);
                updateButtonState(button);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.putClientProperty("isPressed", false);
                updateButtonState(button);
            }
        });
        
        // Il HierarchyListener rileva quando cambi pagina (il CardLayout nasconde il pannello)
        button.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (!button.isShowing()) {
                    button.putClientProperty("isHovered", false);
                    button.putClientProperty("isPressed", false);
                    updateButtonState(button);
                }
            }
        });
    }

    // ABBIAMO AGGIUNTO IL QUARTO PARAMETRO: java.awt.Color borderC
    public static void buttonColors(javax.swing.AbstractButton btn, java.awt.Color fg, java.awt.Color bg, java.awt.Color borderC) {
        if (!(btn.getUI() instanceof javax.swing.plaf.basic.BasicButtonUI)) {
            btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        }
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.putClientProperty("baseBackground", bg);
        
        // ORA USA IL NUOVO COLORE DEDICATO PER I BORDI!
        btn.putClientProperty("themeBorder", borderC);

        if (btn.getClientProperty("antiStuckAdded") == null) {
            btn.setRolloverEnabled(false);
            
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    btn.putClientProperty("isHovered", true);
                    updateButtonState(btn);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.putClientProperty("isHovered", false);
                    btn.putClientProperty("isPressed", false);
                    updateButtonState(btn);
                }
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    btn.putClientProperty("isPressed", true);
                    updateButtonState(btn);
                }
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    btn.putClientProperty("isPressed", false);
                    updateButtonState(btn);
                }
            });
            
            btn.addHierarchyListener(e -> {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (!btn.isShowing()) {
                        btn.putClientProperty("isHovered", false);
                        btn.putClientProperty("isPressed", false);
                        updateButtonState(btn);
                    }
                }
            });
            
            btn.putClientProperty("antiStuckAdded", true);
        }
        
        updateButtonState(btn);
    }

    private static void updateButtonState(AbstractButton button) {
        Color base = (Color) button.getClientProperty("baseBackground");
        if (base == null) base = button.getBackground();
        
        Color borderC = (Color) button.getClientProperty("themeBorder");
        if (borderC == null) borderC = new Color(98, 114, 164); 

        // Usa solo le NOSTRE variabili, ignorando i finti stati bloccati di Swing
        boolean isPressed = Boolean.TRUE.equals(button.getClientProperty("isPressed"));
        boolean isHovered = Boolean.TRUE.equals(button.getClientProperty("isHovered"));
        
        if (isPressed) {
            button.setBackground(darken(base, 0.58f, 235));
            button.setBorder(new LineBorder(borderC, 3));
        } else if (isHovered) {
            // Illumina leggermente il bottone al passaggio del mouse
            button.setBackground(lighten(base, 1.25f, 215));
            button.setBorder(new LineBorder(borderC, 2));
        } else {
            button.setBackground(base);
            button.setBorder(new LineBorder(borderC, 1));
        }
    }

    private static Color darken(Color color, float amount, int alpha) {
        return new Color(Math.round(color.getRed() * amount),
                Math.round(color.getGreen() * amount),
                Math.round(color.getBlue() * amount), alpha);
    }
    
    private static Color lighten(Color color, float amount, int alpha) {
        return new Color(
            Math.min(255, Math.round(color.getRed() * amount)),
            Math.min(255, Math.round(color.getGreen() * amount)),
            Math.min(255, Math.round(color.getBlue() * amount)), 
            alpha
        );
    }

    static void boardButton(JButton button) {
        button.setRolloverEnabled(true);
        button.addChangeListener(e -> {
            if (!button.getText().isEmpty()) return;
            ButtonModel model = button.getModel();
            if (model.isRollover() || model.isPressed()) {
                button.setBorderPainted(true);
                button.setBorder(new LineBorder(new Color(139, 233, 253),
                        model.isPressed() ? 3 : 2));
            } else {
                button.setBorderPainted(false);
            }
        });
    }

    static void selectionBox(JComboBox<String> box) {
        box.setOpaque(true);
        box.setFocusable(false);
        box.setBorder(new LineBorder(new Color(98, 114, 164), 1));
        box.setFont(CONTROL_FONT);
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, CONTROL_HEIGHT));
        box.setMinimumSize(new Dimension(0, CONTROL_HEIGHT));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, CONTROL_HEIGHT));
    }

    static void width(JComponent component, int width) {
        Dimension size = new Dimension(width, CONTROL_HEIGHT);
        component.setPreferredSize(size);
        component.setMinimumSize(size);
        component.setMaximumSize(size);
    }

    static void buttonSize(AbstractButton button, int width, int height) {
        Dimension size = new Dimension(width, height);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
    }

    static void selectionTheme(JComboBox<String> box, Color foreground, Color background) {
        box.setForeground(foreground);
        box.setBackground(background);
        box.setBorder(new LineBorder(new Color(98, 114, 164), 1));
        box.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(background);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(foreground);
                        int w = 10;
                        int h = 5;
                        int x = (getWidth() - w) / 2;
                        int y = (getHeight() - h) / 2;
                        g2.fillPolygon(new int[]{x, x + w, x + w / 2}, new int[]{y, y, y + h}, 3);
                    }
                };
                arrow.setBorderPainted(false);
                arrow.setFocusPainted(false);
                arrow.setContentAreaFilled(false);
                return arrow;
            }
        });
    }
}