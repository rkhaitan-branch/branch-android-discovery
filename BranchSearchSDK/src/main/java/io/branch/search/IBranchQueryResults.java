package io.branch.search;

/**
 * Interface for calling back Branch query related results.
 */
public interface IBranchQueryResults {
    /**
     * Called when there is a successful Branch result available.
     * @param result {@link BranchQueryResult} object containing the result
     */
    void onQueryResult(final BranchQueryResult result);

    /**
     * Called when there is an error that occurred.
     * @param error {@link BranchSearchError} object with error code and detailed message
     */
    void onError(final BranchSearchError error);
}
