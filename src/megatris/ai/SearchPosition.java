package megatris.ai;

/** Immutable node state used while searching candidate moves. */
final class SearchPosition {
    final char[][][][] board;
    final char[][] bigBoard;
    final int activeRow;
    final int activeCol;

    SearchPosition(char[][][][] board, char[][] bigBoard, int activeRow, int activeCol) {
        this.board = board;
        this.bigBoard = bigBoard;
        this.activeRow = activeRow;
        this.activeCol = activeCol;
    }
}
