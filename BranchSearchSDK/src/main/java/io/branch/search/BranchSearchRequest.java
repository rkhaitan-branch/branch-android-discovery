package io.branch.search;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request model for Branch Search.
 */
public class BranchSearchRequest extends BranchDiscoveryRequest<BranchSearchRequest> {
    // Default result limits
    private static final int MAX_APP_RESULT = 5;
    private static final int MAX_CONTENT_PER_APP_RESULT = 5;

    // User query string
    private final String user_query;

    // Flag to send to the server to indicate the query is exactly what they want.
    private boolean doNotModifyQuery;

    // Source of the query
    private BranchQuerySource querySource = BranchQuerySource.UNSPECIFIED;

    // Result limit params
    private int maxAppResults;

    private int maxContentPerAppResults;

    enum JSONKey {
        LimitAppResults("limit_app_results"),
        LimitLinkResults("limit_link_results"),
        UserQuery("user_query"),
        DoNotModify("do_not_modify"),
        QuerySource("query_source");

        JSONKey(String key) {
            _key = key;
        }

        @Override
        public String toString() {
            return _key;
        }

        private String _key;
    }

    /**
     * Factory Method to create a new BranchSearchRequest.
     * @param query Query String to use
     * @return a new BranchSearchRequest.
     */
    public static BranchSearchRequest Create(String query) {
        return new BranchSearchRequest(query);
    }

    private BranchSearchRequest(String query) {
        super();

        this.user_query = query;
        maxAppResults = MAX_APP_RESULT;
        maxContentPerAppResults = MAX_CONTENT_PER_APP_RESULT;
    }

    /**
     * Disable Query Modifications.
     * This is a flag to suggest that Branch should not internally modify a query when logic dictates
     * the user intended a query other than what they actually typed. For example, because of a typo.
     * @return this BranchSearchRequest
     */
    public BranchSearchRequest disableQueryModification() {
        this.doNotModifyQuery = true;
        return this;
    }

    public BranchSearchRequest setMaxContentPerAppResults(int maxContentPerAppResults) {
        this.maxContentPerAppResults = maxContentPerAppResults;
        return this;
    }

    public BranchSearchRequest setMaxAppResults(int maxAppResults) {
        this.maxAppResults = maxAppResults;
        return this;
    }

    /**
     * Should be called to notify that the query in this request originated from the
     * results of another Branch SDK call.
     * @param querySource the source
     * @return this for chaining
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public BranchSearchRequest setQuerySource(@NonNull BranchQuerySource querySource) {
        this.querySource = querySource;
        return this;
    }

    JSONObject convertToJson() {
        JSONObject object = new JSONObject();
        super.convertToJson(object);

        try {
            object.putOpt(JSONKey.LimitAppResults.toString(), maxAppResults);
            object.putOpt(JSONKey.LimitLinkResults.toString(), maxContentPerAppResults);
            object.putOpt(JSONKey.UserQuery.toString(), user_query);

            if (doNotModifyQuery) {
                object.putOpt(JSONKey.DoNotModify.toString(), true);
            }

            object.putOpt(JSONKey.QuerySource.toString(), querySource);
        } catch (JSONException ignore) {
        }
        return object;
    }

    public String getQuery() {
        return user_query;
    }
}
