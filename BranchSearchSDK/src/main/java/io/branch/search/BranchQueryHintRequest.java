package io.branch.search;

import org.json.JSONObject;

/**
 * Request model for Branch Search.
 */
class BranchQueryHintRequest extends BranchDiscoveryRequest<BranchQueryHintRequest> {

    /**
     * Factory Method to create a new BranchQueryHintRequest.
     * @return a new BranchQueryHintRequest.
     */
    static BranchQueryHintRequest Create() {
        return new  BranchQueryHintRequest();
    }

    JSONObject convertToJson() {
        JSONObject object = new JSONObject();
        super.convertToJson(object);

        return object;
    }
}
