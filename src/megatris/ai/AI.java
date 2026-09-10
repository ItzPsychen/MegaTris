package megatris.ai;

import megatris.game.GameRules;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Computer-player logic for MegaTris/Ultimate Tic-Tac-Toe.
 *
 * <p>The representation intentionally matches {@code MegaTris}: the first two
 * coordinates select a small board and the last two select a cell in it.
 * This class does not retain or modify the arrays supplied by the caller.</p>
 */
public final class AI {
    public enum Difficulty {
        BEGINNER, MEDIUM, DIFFICULT, IMPOSSIBLE
    }

    /** A move is addressed as big-board row/column followed by cell row/column. */
    public static final class Move {
        public final int bigRow;
        public final int bigCol;
        public final int row;
        public final int col;

        public Move(int bigRow, int bigCol, int row, int col) {
            this.bigRow = bigRow;
            this.bigCol = bigCol;
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return "(" + bigRow + "," + bigCol + "," + row + "," + col + ")";
        }
    }

    private static final char EMPTY = '\0';
    private static final int[][] LINES = {
            {0, 0, 0, 1, 0, 2}, {1, 0, 1, 1, 1, 2}, {2, 0, 2, 1, 2, 2},
            {0, 0, 1, 0, 2, 0}, {0, 1, 1, 1, 2, 1}, {0, 2, 1, 2, 2, 2},
            {0, 0, 1, 1, 2, 2}, {0, 2, 1, 1, 2, 0}
    };

    private final Random random;

    public AI() {
        this(new Random());
    }

    /** A seeded constructor is useful for reproducible tests and replays. */
    public AI(long seed) {
        this(new Random(seed));
    }

    private AI(Random random) {
        this.random = random;
    }

    /**
     * Selects a legal move. {@code activeBigRow/activeBigCol} may both be -1
     * for a free choice. If the selected board is finished, free choice is used.
     *
     * @return a legal move, or {@code null} when no move remains
     */
    public Move chooseMove(char[][][][] board, char[][] bigBoard,
                           int activeBigRow, int activeBigCol,
                           char player, Difficulty difficulty) {
        Difficulty level = difficulty == null ? Difficulty.MEDIUM : difficulty;
        return chooseMove(board, bigBoard, activeBigRow, activeBigCol, player,
                level, defaultThinkTime(level));
    }

    /**
     * Selects a move using iterative deepening for the requested time.
     * The returned move is always from the last fully completed search.
     * @param thinkTimeMillis maximum search time; values below one millisecond
     *                        still receive a legal, tactical fallback
     */
    public Move chooseMove(char[][][][] board, char[][] bigBoard,
                           int activeBigRow, int activeBigCol,
                           char player, Difficulty difficulty,
                           long thinkTimeMillis) {
        validate(board, bigBoard, player);
        List<Move> legal = legalMoves(board, bigBoard, activeBigRow, activeBigCol);
        if (legal.isEmpty()) return null;

        Difficulty level = difficulty == null ? Difficulty.MEDIUM : difficulty;
        if (level == Difficulty.BEGINNER) {
            for (Move move : legal) {
                if (winsAfter(board, bigBoard, move, player)) return move;
            }
            return legal.get(random.nextInt(legal.size()));
        }

        char opponent = player == 'X' ? 'O' : 'X';
        Move fallback = tacticalMove(board, bigBoard, legal, activeBigRow, activeBigCol, player, opponent);
        Move best = fallback;
        long budget = Math.max(1L, thinkTimeMillis);
        long deadline = System.nanoTime() + budget * 1000000L;
        Collections.shuffle(legal, random);
        int depth = 1;
        while (System.nanoTime() < deadline) {
            try {
                SearchResult result = searchRoot(board, bigBoard, activeBigRow, activeBigCol,
                        player, opponent, legal, depth, deadline);
                best = result.move;
                depth++;
            } catch (SearchTimeout timeout) {
                break;
            }
        }
        return best;
    }

    private long defaultThinkTime(Difficulty difficulty) {
        if (difficulty == Difficulty.MEDIUM) return 1000L;
        if (difficulty == Difficulty.DIFFICULT) return 2000L;
        if (difficulty == Difficulty.IMPOSSIBLE) return 4000L;
        return 1L;
    }

    private Move tacticalMove(char[][][][] board, char[][] bigBoard, List<Move> legal,
                              int activeRow, int activeCol, char player, char opponent) {
        List<Move> safe = new ArrayList<Move>();
        int before = 0;
        for (Move threat : legalMoves(board, bigBoard, activeRow, activeCol)) {
            if (winsAfter(board, bigBoard, threat, opponent)) before++;
        }
        for (Move move : legal) {
            if (winsAfter(board, bigBoard, move, player)) return move;
            if (before > 0 && blocks(board, bigBoard, move, player, opponent, before)) return move;
            safe.add(move);
        }
        return safe.get(random.nextInt(safe.size()));
    }

    private boolean winsAfter(char[][][][] board, char[][] bigBoard, Move move, char player) {
        return GameRules.checkWin(play(board, bigBoard, move, player).bigBoard) == player;
    }

    private boolean blocks(char[][][][] board, char[][] bigBoard, Move move,
                           char player, char opponent, int before) {
        SearchPosition after = play(board, bigBoard, move, player);
        List<Move> opponentThreats = legalMoves(after.board, after.bigBoard,
                after.activeRow, after.activeCol);
        int remaining = 0;
        for (Move threat : opponentThreats)
            if (winsAfter(after.board, after.bigBoard, threat, opponent)) remaining++;
        return before > remaining;
    }

    private SearchResult searchRoot(char[][][][] board, char[][] bigBoard,
                                    int activeRow, int activeCol, char player,
                                    char opponent, List<Move> moves, int depth,
                                    long deadline) {
        Move best = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE + 1;
        for (Move move : ordered(moves, board, bigBoard, player)) {
            checkTime(deadline);
            SearchPosition next = play(board, bigBoard, move, player);
            int score = minimax(next, next.activeRow, next.activeCol, opponent,
                    player, depth - 1, alpha, Integer.MAX_VALUE - 1, deadline);
            if (best == null || score > bestScore) {
                best = move;
                bestScore = score;
            }
            alpha = Math.max(alpha, bestScore);
        }
        return new SearchResult(best, bestScore);
    }

    private int minimax(SearchPosition position, int activeRow, int activeCol, char turn,
                        char maximizer, int depth, int alpha, int beta,
                        long deadline) {
        checkTime(deadline);
        char winner = GameRules.checkWin(position.bigBoard);
        char opponent = maximizer == 'X' ? 'O' : 'X';
        if (winner == maximizer) return 100000 + depth;
        if (winner == opponent) return -100000 - depth;
        if (depth == 0) return evaluate(position, maximizer);

        List<Move> moves = legalMoves(position.board, position.bigBoard, activeRow, activeCol);
        if (moves.isEmpty()) return evaluate(position, maximizer);
        boolean maximizing = turn == maximizer;
        int value = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move move : ordered(moves, position.board, position.bigBoard, turn)) {
            SearchPosition next = play(position.board, position.bigBoard, move, turn);
            int score = minimax(next, next.activeRow, next.activeCol,
                    turn == 'X' ? 'O' : 'X', maximizer, depth - 1,
                    alpha, beta, deadline);
            if (maximizing) {
                value = Math.max(value, score);
                alpha = Math.max(alpha, value);
            } else {
                value = Math.min(value, score);
                beta = Math.min(beta, value);
            }
            if (beta <= alpha) break;
        }
        return value;
    }

    private static void checkTime(long deadline) {
        if (System.nanoTime() >= deadline) throw new SearchTimeout();
    }

    private int evaluate(SearchPosition p, char player) {
        char opponent = player == 'X' ? 'O' : 'X';
        int score = 0;
        for (int br = 0; br < 3; br++) for (int bc = 0; bc < 3; bc++) {
            if (p.bigBoard[br][bc] == player) score += 120;
            else if (p.bigBoard[br][bc] == opponent) score -= 120;
            else score += linePotential(p.bigBoard, br, bc, player, opponent) * 12;
        }
        for (int br = 0; br < 3; br++) for (int bc = 0; bc < 3; bc++)
            score += linePotential(p.board[br][bc], player, opponent);
        return score;
    }

    private int linePotential(char[][] grid, int br, int bc, char player, char opponent) {
        return linePotential(grid, player, opponent) + ((br == 1 && bc == 1) ? 2 : 0);
    }

    private int linePotential(char[][] grid, char player, char opponent) {
        int score = 0;
        for (int[] line : LINES) {
            int mine = 0, theirs = 0;
            for (int i = 0; i < 3; i++) {
                char value = grid[line[i * 2]][line[i * 2 + 1]];
                if (value == player) mine++;
                else if (value == opponent) theirs++;
            }
            if (theirs == 0) score += mine * mine;
            if (mine == 0) score -= theirs * theirs;
        }
        return score;
    }

    private List<Move> ordered(List<Move> moves, final char[][][][] board,
                               final char[][] bigBoard, final char player) {
        Collections.sort(moves, new Comparator<Move>() {
            public int compare(Move a, Move b) {
                return Integer.compare(moveValue(b, board, bigBoard, player),
                        moveValue(a, board, bigBoard, player));
            }
        });
        return moves;
    }

    private int moveValue(Move m, char[][][][] board, char[][] bigBoard, char player) {
        int value = (m.row == 1 && m.col == 1 ? 3 : 0);
        if (m.row == 1 || m.col == 1) value++;
        if (bigBoard[m.row][m.col] == EMPTY) value += 2;
        if (winsAfter(board, bigBoard, m, player)) value += 1000;
        return value;
    }

    private static List<Move> legalMoves(char[][][][] board, char[][] bigBoard, int ar, int ac) {
        List<Move> result = new ArrayList<Move>();
        boolean constrained = ar >= 0 && ac >= 0 && ar < 3 && ac < 3
                && bigBoard[ar][ac] == EMPTY && !GameRules.isFull(board[ar][ac]);
        for (int br = 0; br < 3; br++) for (int bc = 0; bc < 3; bc++) {
            if (constrained && (br != ar || bc != ac)) continue;
            if (bigBoard[br][bc] != EMPTY || GameRules.isFull(board[br][bc])) continue;
            for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++)
                if (board[br][bc][r][c] == EMPTY) result.add(new Move(br, bc, r, c));
        }
        return result;
    }

    private static SearchPosition play(char[][][][] source, char[][] sourceBig, Move move, char player) {
        char[][][][] board = copy(source);
        char[][] big = copy(sourceBig);
        board[move.bigRow][move.bigCol][move.row][move.col] = player;
        if (big[move.bigRow][move.bigCol] == EMPTY) {
            char result = GameRules.checkWin(board[move.bigRow][move.bigCol]);
            if (result != EMPTY) big[move.bigRow][move.bigCol] = result;
        }
        int nextRow = move.row, nextCol = move.col;
        if (big[nextRow][nextCol] != EMPTY || GameRules.isFull(board[nextRow][nextCol])) {
            nextRow = move.bigRow; nextCol = move.bigCol;
            if (big[nextRow][nextCol] != EMPTY || GameRules.isFull(board[nextRow][nextCol])) {
                nextRow = -1; nextCol = -1;
            }
        }
        return new SearchPosition(board, big, nextRow, nextCol);
    }

    private static char[][][][] copy(char[][][][] source) {
        char[][][][] result = new char[3][3][3][3];
        for (int a = 0; a < 3; a++) for (int b = 0; b < 3; b++)
            for (int c = 0; c < 3; c++) System.arraycopy(source[a][b][c], 0, result[a][b][c], 0, 3);
        return result;
    }

    private static char[][] copy(char[][] source) {
        char[][] result = new char[3][3];
        for (int r = 0; r < 3; r++) System.arraycopy(source[r], 0, result[r], 0, 3);
        return result;
    }

    private static void validate(char[][][][] board, char[][] bigBoard, char player) {
        if (board == null || bigBoard == null || board.length != 3 || bigBoard.length != 3
                || (player != 'X' && player != 'O')) throw new IllegalArgumentException("Invalid game state");
    }

}
