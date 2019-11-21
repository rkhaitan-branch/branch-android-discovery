package io.branch.search;

import android.os.Parcel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing a a query result
 */
public class BranchQueryResult {
    private static final String RESULTS_KEY = "results";

    private List<String> queryResults;

    private BranchQueryResult() {
    }

    public List<String> getQueryResults() {
        return queryResults;
    }

    static BranchQueryResult createFromJson(JSONObject jsonObject) {
        BranchQueryResult result = new BranchQueryResult();
        result.queryResults = new ArrayList<>();

        try {
            JSONArray jsonArray = jsonObject.optJSONArray(RESULTS_KEY);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    result.queryResults.add(jsonArray.getString(i));
                }
            }
        } catch (JSONException e) {
        }

        return result;
    }

    private BranchQueryResult(Parcel in) {
        queryResults = new ArrayList<>();

        int length = in.readInt();
        for (int i = 0; i < length; i++) {
            queryResults.add(in.readString());
        }
    }
}
