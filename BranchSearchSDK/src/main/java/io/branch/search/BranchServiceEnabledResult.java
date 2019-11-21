package io.branch.search;

import android.support.annotation.NonNull;

import org.json.JSONObject;

/**
 * Class for representing a 'service enabled' query result.
 */
public class BranchServiceEnabledResult {
    private static final String DISABLED_KEY = "disabled";

    private boolean isEnabled;

    private BranchServiceEnabledResult() {
    }

    /**
     * Whether the service is enabled or not.
     * @return true if enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    static BranchServiceEnabledResult createFromJson(@NonNull JSONObject jsonObject) {
        BranchServiceEnabledResult result = new BranchServiceEnabledResult();
        boolean isDisabled = jsonObject.has(DISABLED_KEY)
                && jsonObject.optBoolean(DISABLED_KEY, false);
        result.isEnabled = !isDisabled;
        return result;
    }

    static BranchServiceEnabledResult createFromError(@NonNull BranchSearchError error) {
        // The IBranchServiceEnabledEvents interface has no error callback on purpose, because
        // we don't want it to be misused (misinterpreted as a 'disabled' response).
        // So we treat all errors as a 'enabled' response.
        BranchServiceEnabledResult result = new BranchServiceEnabledResult();
        result.isEnabled = true;
        return result;
    }
}
