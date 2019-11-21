package io.branch.search;

/**
 * Interface for calling back Branch search related events.
 */
public interface IBranchSearchEvents {
    /**
     * Called when there is a Branch search result available
     * @param result {@link BranchSearchResult} object containing the search result
     */
    void onBranchSearchResult(BranchSearchResult result);

    /**
     * Called when there is an error occurred while searching operation with Branch search SDK
     * @param error {@link BranchSearchError} object with error code and detailed message
     */
    void onBranchSearchError(BranchSearchError error);
}
