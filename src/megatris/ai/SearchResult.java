package megatris.ai;

/** Result of one fully completed root search iteration. */
final class SearchResult {
    final AI.Move move;
    final int score;

    SearchResult(AI.Move move, int score) {
        this.move = move;
        this.score = score;
    }
}
