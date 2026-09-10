package megatris.game;

public final class GameState implements java.io.Serializable {
    public final char[][][][] board;
    public final char[][] bigBoard;
    public final int activeBigRow, activeBigCol;
    public final boolean isXTurn, gameOver;
    public final int[] megaWinningLine;
    public final char megaWinner;
    public final int lastBigRow, lastBigCol, lastRow, lastCol;
    public final char lastMovePlayer;

    public GameState(char[][][][] b, char[][] bb, int ar, int ac, boolean turn, boolean over,
                         int[] winningLine, char winner,
                         int lastBigRow, int lastBigCol, int lastRow, int lastCol,
                         char lastMovePlayer) {
            this.board = new char[3][3][3][3];
            this.bigBoard = new char[3][3];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    this.bigBoard[i][j] = bb[i][j];
                    for (int k = 0; k < 3; k++) {
                        System.arraycopy(b[i][j][k], 0, this.board[i][j][k], 0, 3);
                    }
                }
            }
            this.activeBigRow = ar;
            this.activeBigCol = ac;
            this.isXTurn = turn;
            this.gameOver = over;
            this.megaWinningLine = winningLine == null ? null : winningLine.clone();
            this.megaWinner = winner;
            this.lastBigRow = lastBigRow;
            this.lastBigCol = lastBigCol;
            this.lastRow = lastRow;
            this.lastCol = lastCol;
            this.lastMovePlayer = lastMovePlayer;
        }
}