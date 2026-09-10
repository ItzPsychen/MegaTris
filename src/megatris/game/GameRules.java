package megatris.game;

/** Shared rules for a 3x3 board and the corresponding MegaTris boards. */
public final class GameRules {
    private static final char EMPTY = '\0';
    private static final char DRAW = 'D';

    private GameRules() {
    }

    public static char checkWin(char[][] grid) {
        for (int i = 0; i < 3; i++) {
            if (grid[i][0] != EMPTY && grid[i][0] != DRAW
                    && grid[i][0] == grid[i][1] && grid[i][1] == grid[i][2]) return grid[i][0];
            if (grid[0][i] != EMPTY && grid[0][i] != DRAW
                    && grid[0][i] == grid[1][i] && grid[1][i] == grid[2][i]) return grid[0][i];
        }
        if (grid[0][0] != EMPTY && grid[0][0] != DRAW
                && grid[0][0] == grid[1][1] && grid[1][1] == grid[2][2]) return grid[0][0];
        if (grid[0][2] != EMPTY && grid[0][2] != DRAW
                && grid[0][2] == grid[1][1] && grid[1][1] == grid[2][0]) return grid[0][2];
        return isFull(grid) ? DRAW : EMPTY;
    }

    /** Returns line kind (row, column, diagonal) and index for a winning player. */
    public static int[] winningLine(char[][] grid, char player) {
        for (int i = 0; i < 3; i++) {
            if (grid[i][0] == player && grid[i][1] == player && grid[i][2] == player) {
                return new int[]{0, i};
            }
            if (grid[0][i] == player && grid[1][i] == player && grid[2][i] == player) {
                return new int[]{1, i};
            }
        }
        if (grid[0][0] == player && grid[1][1] == player && grid[2][2] == player) {
            return new int[]{2, 0};
        }
        if (grid[0][2] == player && grid[1][1] == player && grid[2][0] == player) {
            return new int[]{3, 0};
        }
        return null;
    }

    public static boolean isFull(char[][] grid) {
        for (char[] row : grid) {
            for (char cell : row) {
                if (cell == EMPTY) return false;
            }
        }
        return true;
    }
}
