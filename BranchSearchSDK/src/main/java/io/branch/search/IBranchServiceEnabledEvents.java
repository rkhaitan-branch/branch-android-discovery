package io.branch.search;

import android.support.annotation.NonNull;

/**
 * Interface for calling back Branch 'service enabled' events.
 */
public interface IBranchServiceEnabledEvents {

    /**
     * Called when the service enabled requests ends.
     * Users can inspect the {@link BranchServiceEnabledResult} object for results.
     *
     * @param result {@link BranchServiceEnabledResult} object containing the search result
     */
    void onBranchServiceEnabledResult(@NonNull BranchServiceEnabledResult result);
}
