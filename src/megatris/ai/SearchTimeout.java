package megatris.ai;

/** Internal non-error signal used to stop an expired iterative search. */
final class SearchTimeout extends RuntimeException {
    private static final long serialVersionUID = 1L;
}
