package megatris.ui;

import java.awt.*;
import javax.swing.*;
import megatris.game.GameRules;

interface BoardViewState {
    char[][][][] board();
    char[][] bigBoard();
    boolean gameOver();
    int activeBigRow();
    int activeBigCol();
    boolean isXTurn();
    float selectionPhase();
    float[][] boardAnimation();
    Color bgMain();
    Color bgHighlight();
    Color bgWonX();
    Color bgWonO();
    Color fgX();
    Color fgO();
    Color gridColor();
    int[] megaWinningLine();
    float megaWinAnimation();
    char megaWinner();
    JPanel[][] bigPanels();
    boolean isBoardFull(int row, int col);
}

final class SubBoardPanel extends JPanel {
    private final BoardViewState state;
    private final int br;
    private final int bc;

    SubBoardPanel(BoardViewState state, int br, int bc) {
        super(new GridLayout(3, 3, 0, 0));
        this.state = state;
        this.br = br;
        this.bc = bc;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        char boardState = state.bigBoard()[br][bc];
        boolean full = state.isBoardFull(br, bc);
        boolean active = (!state.gameOver() && boardState == '\0' && !full
                && (state.activeBigRow() == -1
                || (state.activeBigRow() == br && state.activeBigCol() == bc)));

        if (boardState == 'X') g2.setColor(state.bgWonX());
        else if (boardState == 'O') g2.setColor(state.bgWonO());
        else if (full) g2.setColor(new Color(80, 80, 80));
        else if (active) g2.setColor(darken(state.bgHighlight(), 0.72f));
        else g2.setColor(darken(state.bgMain(), 0.72f));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(state.gridColor());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        int w = getWidth(), h = getHeight();
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(w / 3, 0, w / 3, h);
        g2.drawLine(2 * w / 3, 0, 2 * w / 3, h);
        g2.drawLine(0, h / 3, w, h / 3);
        g2.drawLine(0, 2 * h / 3, w, 2 * h / 3);

        if (boardState == 'X' || boardState == 'O') {
            g2.setStroke(new BasicStroke(15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    0.4f * state.boardAnimation()[br][bc]));
            int pad = 25;
            if (boardState == 'X') {
                g2.setColor(state.fgX());
                g2.drawLine(pad, pad, getWidth() - pad, getHeight() - pad);
                g2.drawLine(getWidth() - pad, pad, pad, getHeight() - pad);
            } else {
                g2.setColor(state.fgO());
                g2.drawOval(pad, pad, getWidth() - 2 * pad, getHeight() - 2 * pad);
            }
        }

        if (active) {
            g2.setColor(state.isXTurn() ? state.fgX() : state.fgO());
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{8f, 6f}, state.selectionPhase()));
            g2.drawRect(2, 2, getWidth() - 5, getHeight() - 5);
        }
    }

    private Color darken(Color color, float amount) {
        return new Color(Math.round(color.getRed() * amount),
                Math.round(color.getGreen() * amount),
                Math.round(color.getBlue() * amount));
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (state.bigBoard()[br][bc] != 'D') return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.GRAY);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                0.9f * state.boardAnimation()[br][bc]));
        g2.setStroke(new BasicStroke(15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int pad = 25;
        g2.drawLine(pad, getHeight() / 2, getWidth() - pad, getHeight() / 2);
        int[] line = GameRules.winningLine(state.board()[br][bc], 'X');
        if (line == null) line = GameRules.winningLine(state.board()[br][bc], 'O');
        if (line != null) drawLocalWinningLine(g2, line, state.boardAnimation()[br][bc]);
        g2.dispose();
    }

    private void drawLocalWinningLine(Graphics2D g2, int[] line, float progress) {
        float[][] points = {
                {getWidth() / 6f, getHeight() / 2f},
                {getWidth() / 2f, getHeight() / 2f},
                {5f * getWidth() / 6f, getHeight() / 2f}
        };
        if (line[0] == 1) {
            points = new float[][]{{getWidth() / 2f, getHeight() / 6f},
                    {getWidth() / 2f, getHeight() / 2f},
                    {getWidth() / 2f, 5f * getHeight() / 6f}};
        } else if (line[0] == 2) {
            points = new float[][]{{getWidth() / 6f, getHeight() / 6f},
                    {getWidth() / 2f, getHeight() / 2f},
                    {5f * getWidth() / 6f, 5f * getHeight() / 6f}};
        } else if (line[0] == 3) {
            points = new float[][]{{5f * getWidth() / 6f, getHeight() / 6f},
                    {getWidth() / 2f, getHeight() / 2f},
                    {getWidth() / 6f, 5f * getHeight() / 6f}};
        } else {
            float y = (line[1] + 0.5f) * getHeight() / 3f;
            points[0][1] = y;
            points[1][1] = y;
            points[2][1] = y;
        }
        float endX = points[0][0] + (points[2][0] - points[0][0]) * progress;
        float endY = points[0][1] + (points[2][1] - points[0][1]) * progress;
        g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(Math.round(points[0][0]), Math.round(points[0][1]),
                Math.round(endX), Math.round(endY));
    }
}

final class MainBoardPanel extends JPanel {
    private final BoardViewState state;

    MainBoardPanel(BoardViewState state, LayoutManager layout) {
        super(layout);
        this.state = state;
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        int[] line = state.megaWinningLine();
        if (line == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        Point start;
        Point end;
        if (line[0] == 0) {
            start = panelCenter(line[1], 0);
            end = panelCenter(line[1], 2);
        } else if (line[0] == 1) {
            start = panelCenter(0, line[1]);
            end = panelCenter(2, line[1]);
        } else if (line[0] == 2) {
            start = panelCenter(0, 0);
            end = panelCenter(2, 2);
        } else {
            start = panelCenter(0, 2);
            end = panelCenter(2, 0);
        }
        float progress = 1f - (float) Math.pow(1f - state.megaWinAnimation(), 3);
        int x = start.x + Math.round((end.x - start.x) * progress);
        int y = start.y + Math.round((end.y - start.y) * progress);
        g2.setColor(state.megaWinner() == 'O' ? state.fgO() : state.fgX());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
        g2.setStroke(new BasicStroke(28f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(start.x, start.y, x, y);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
        g2.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(start.x, start.y, x, y);
        g2.dispose();
    }

    private Point panelCenter(int row, int col) {
        JPanel panel = state.bigPanels()[row][col];
        return new Point(panel.getX() + panel.getWidth() / 2,
                panel.getY() + panel.getHeight() / 2);
    }
}
