package io.branch.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Branch Search results.
 */
public class BranchSearchResult {
    private final BranchSearchRequest query;
    private String corrected_query;

    final List<BranchAppResult> results;

    BranchSearchResult(BranchSearchRequest query, String corrected_query) {
        this.query = query;
        this.corrected_query = corrected_query;
        this.results = new ArrayList<>();
    }

    /**
     * @return the original Branch search query.
     */
    public BranchSearchRequest getBranchSearchRequest() {
        return query;
    }

    /**
     * @return the corrected Branch search query.
     */
    public String getCorrectedQuery() { return corrected_query; }

    /**
     * @return a list of {@link BranchAppResult}.
     */
    public List<BranchAppResult> getResults() {
        return this.results;
    }
}