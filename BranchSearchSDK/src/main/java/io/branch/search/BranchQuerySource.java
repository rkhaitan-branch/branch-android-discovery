package io.branch.search;

import android.support.annotation.NonNull;

/**
 * Indicates the source of a certain query that is being passed to Branch search.
 * Can be applied to the search request by using
 * {@link BranchSearchRequest#setQuerySource(BranchQuerySource)}.
 */
public enum BranchQuerySource {

    /**
     * Indicates that this query originated from the results
     * of a query hint call.
     */
    QUERY_HINT_RESULTS("query_hint"),

    /**
     * Indicates that this query originated from the results
     * of an autosuggest call.
     */
    AUTOSUGGEST_RESULTS("autosuggest"),

    /**
     * Indicates that the source of this query is unknown or
     * simply unspecified.
     */
    UNSPECIFIED("unspecified");

    private final String mName;

    BranchQuerySource(@NonNull String name) {
        mName = name;
    }

    @NonNull
    @Override
    public String toString() {
        return mName;
    }
}
