package io.branch.search;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sojanpr on 3/16/18.
 * <p>
 * Class for parsing a Branch search response
 * </p>
 */

class BranchResponseParser {

    private static final String REQUEST_ID_KEY = "request_id";
    private static final String RESULTS_KEY = "results";
    private static final String SUCCESS_KEY = "success";
    private static final String CORRECTED_QUERY_KEY = "search_query_string";

    private static final String APP_NAME_KEY = "app_name";
    private static final String APP_STORE_ID_KEY = "app_store_id";
    private static final String APP_ICON_URL_KEY = "app_icon_url";
    private static final String APP_SEARCH_DEEP_LINK_KEY = "app_search_deep_link";
    private static final String APP_SCORE_KEY = "score";
    private static final String APP_DEEP_LINKS_KEY = "deep_links";
    private static final String RANKING_HINT_KEY = "ranking_hint";


    static BranchSearchResult parse(BranchSearchRequest query, JSONObject object) {
        String corrected_query = null;
        if (object.optString(CORRECTED_QUERY_KEY, null) != null) {
            corrected_query = object.optString(CORRECTED_QUERY_KEY);
        }

        BranchSearchResult branchSearchResult = new BranchSearchResult(query, corrected_query);
        // Return if request is not succeeded
        if (object.optBoolean(SUCCESS_KEY)) {
            // Parse results
            parseResultArray(object.optString(REQUEST_ID_KEY), object.optJSONArray(RESULTS_KEY), branchSearchResult);
        }
        return branchSearchResult;
    }

    private static void parseResultArray(String requestID, JSONArray resultsArray, BranchSearchResult branchSearchResult) {
        if (resultsArray != null) {
            for (int i = 0; i < resultsArray.length(); i++) {

                JSONObject resultObj = resultsArray.optJSONObject(i);
                if (resultObj != null) {
                    String name = Util.optString(resultObj, APP_NAME_KEY);
                    String store_id = Util.optString(resultObj, APP_STORE_ID_KEY);
                    String icon_url = Util.optString(resultObj, APP_ICON_URL_KEY);
                    JSONObject app_search_link = resultObj.optJSONObject(APP_SEARCH_DEEP_LINK_KEY);
                    float score = (float)resultObj.optDouble(APP_SCORE_KEY, 0.0);
                    String rankingHint = Util.optString(resultObj, RANKING_HINT_KEY);

                    JSONArray rawDeepLinks = resultObj.optJSONArray(APP_DEEP_LINKS_KEY);
                    ArrayList<BranchLinkResult> deepLinks = new ArrayList<>();
                    if (rawDeepLinks != null) {

                        for (int j = 0; j < rawDeepLinks.length(); j++) {
                            BranchLinkResult link = BranchLinkResult.createFromJson(
                                    rawDeepLinks.optJSONObject(j), name, store_id, icon_url);
                            deepLinks.add(link);
                        }
                    }

                    BranchLinkResult link = null;
                    if (app_search_link != null) {
                        link = BranchLinkResult.createFromJson(app_search_link, name, store_id,
                                icon_url);
                    }

                    BranchAppResult appResult = new BranchAppResult(store_id, name, icon_url, link,
                            rankingHint, score, deepLinks);
                    branchSearchResult.results.add(appResult);
                }
            }
        }
    }
}
