package io.branch.search;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Branch Search Error definitions.
 */
public class BranchSearchError extends JSONObject {

    private final ERR_CODE errorCode;
    private final String errorMsg;

    /**
     * Constructor.
     * @param error_code Error code
     */
    BranchSearchError(ERR_CODE error_code) {
        this.errorCode = error_code;
        this.errorMsg = getErrorMessage();
    }

    /**
     * Constructor.
     * @param jsonObject Error data
     */
    BranchSearchError(JSONObject jsonObject) throws JSONException {
        super(jsonObject.toString());

        this.errorMsg = optString("message");
        this.errorCode = ERR_CODE.convert(optInt("code"));
    }

    /**
     * Error Code definitions.
     */
    public enum ERR_CODE {
        /** Unknown Error. */
        UNKNOWN_ERR,

        /** Bad format of request. */
        BAD_REQUEST_ERR,

        /** The Branch key is unauthorized. */
        UNAUTHORIZED_ERR,

        /** No regional support. */
        NOT_SUPPORTED_ERROR,

        /** Error processing request since app doesn't have internet permission. */
        NO_INTERNET_PERMISSION_ERR,

        /** Request failed due to poor connectivity. */
        BRANCH_NO_CONNECTIVITY_ERR,

        /** Internal Branch server error. */
        INTERNAL_SERVER_ERR,

        /** Request to Branch Services timed out. */
        REQUEST_TIMED_OUT_ERR,

        /** Request was canceled due to new requests being scheduled. */
        REQUEST_CANCELED,

        /** Service is disabled, see
         * {@link BranchSearch#isServiceEnabled(Context, IBranchServiceEnabledEvents)}. */
        SERVICE_DISABLED_ERR,

        // == App routing related =============================================

        /** Unable to open the destination app. */
        ROUTING_ERR_UNABLE_TO_OPEN_APP,

        /** Unable to open the web url associated with the app. */
        ROUTING_ERR_UNABLE_TO_OPEN_WEB_URL,

        /** Unable to open the Android shortcut associated with the link. */
        ROUTING_ERR_UNABLE_TO_OPEN_ANDROID_SHORTCUT,

        /** Unable to open the Google Play Store for the app. */
        ROUTING_ERR_UNABLE_TO_OPEN_PS,

        /** An unknown error happened. Unable to open the app. */
        ROUTING_ERR_UNABLE_TO_COMPLETE_ACTION;

        static ERR_CODE convert(int error_code) {
            if (error_code == 400) {
                return ERR_CODE.BAD_REQUEST_ERR;
            } else if (error_code == 401) {
                return ERR_CODE.UNAUTHORIZED_ERR;
            } else if (error_code == 404) {
                return ERR_CODE.NOT_SUPPORTED_ERROR;
            }

            return ERR_CODE.UNKNOWN_ERR;
        }
    }

    private String getErrorMessage() {
        String errMsg = "An unknown error occurred.";
        if (errorCode == ERR_CODE.BRANCH_NO_CONNECTIVITY_ERR) {
            errMsg = "Poor network connectivity. Please try again later. Please make sure app has internet access permission";
        } else if (errorCode == ERR_CODE.NO_INTERNET_PERMISSION_ERR) {
            errMsg = "Please add 'android.permission.INTERNET' in your applications manifest file.";
        } else if (errorCode == ERR_CODE.INTERNAL_SERVER_ERR) {
            errMsg = "Unable to process your request now. An internal error happened. Please try later.";
        } else if (errorCode == ERR_CODE.REQUEST_TIMED_OUT_ERR) {
            errMsg = "Request to Branch server timed out. Please check your connection or try again later.";
        } else if (errorCode == ERR_CODE.REQUEST_CANCELED) {
            errMsg = "Request was canceled due to new requests being scheduled before it could be completed.";
        } else if (errorCode == ERR_CODE.SERVICE_DISABLED_ERR) {
            errMsg = "The search service is disabled.";
        } else if (errorCode == ERR_CODE.ROUTING_ERR_UNABLE_TO_OPEN_APP) {
            errMsg = "Unable to open the destination application or its fallback url.";
        } else if (errorCode == ERR_CODE.ROUTING_ERR_UNABLE_TO_OPEN_WEB_URL) {
            errMsg = "Unable to open the web url associated with the app.";
        } else if (errorCode == ERR_CODE.ROUTING_ERR_UNABLE_TO_OPEN_PS) {
            errMsg = "Unable to open the Google Play Store for the app.";
        } else if (errorCode == ERR_CODE.ROUTING_ERR_UNABLE_TO_COMPLETE_ACTION) {
            errMsg = "An unknown error happened. Unable to open the app.";
        }
        return errMsg;
    }

    /**
     * Get the non-localized error message.
     * @return the non-localized error message.
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * @return the error code
     */
    public ERR_CODE getErrorCode() {
        return errorCode;
    }
}
