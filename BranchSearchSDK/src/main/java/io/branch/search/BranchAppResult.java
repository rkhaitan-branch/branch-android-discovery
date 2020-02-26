package io.branch.search;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Application Result.
 * Contains enough information to open the app or fall back to the play store.
 */
public class BranchAppResult implements Parcelable {
    private final String app_store_id;
    private final String app_name;
    private final String app_icon_url;
    private final BranchLinkResult app_search_deep_link;
    private String ranking_hint;
    private final float score;
    private final List<BranchLinkResult> deep_links;

    BranchAppResult(String appStoreID, String appName, String appIconUrl,
                    BranchLinkResult searchDeepLink,
                    @NonNull String rankingHint,
                    float score,
                    ArrayList<BranchLinkResult> deep_links) {
        this.app_store_id = appStoreID;
        this.app_name = appName;
        this.app_icon_url = appIconUrl;
        this.app_search_deep_link = searchDeepLink;
        this.ranking_hint = rankingHint;
        this.deep_links = deep_links;
        this.score = score;
    }

    /**
     * @return the App Name.
     */
    public String getAppName() {
        return this.app_name;
    }

    /**
     * @return the Package Name.
     */
    public String getPackageName() {
        return this.app_store_id;
    }

    /**
     * @return the App Icon Url.
     */
    public String getAppIconUrl() {
        return this.app_icon_url;
    }

    /**
     * @return the Ranking Hint.
     */
    @NonNull
    public String getRankingHint() {
        return this.ranking_hint;
    }

    /**
     * Returns true if this link represents an ad.
     * @return true if ad, false otherwise
     */
    @SuppressWarnings("unused")
    public boolean isAd() {
        return ranking_hint.toLowerCase().startsWith("featured");
    }

    /**
     * @return the App Score.
     */
    public float getScore() {
        return this.score;
    }

    /**
     * @return a list of Deep Links.
     */
    public List<BranchLinkResult> getDeepLinks() { return this.deep_links; }

    //---- Parcelable implementation -------//
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.app_store_id);
        dest.writeString(this.app_name);
        dest.writeString(this.app_icon_url);
        dest.writeValue(this.app_search_deep_link);
        dest.writeString(this.ranking_hint);
        dest.writeFloat(this.score);
        dest.writeTypedList(this.deep_links);
    }

    private BranchAppResult(Parcel in) {
        this.app_store_id = in.readString();
        this.app_name = in.readString();
        this.app_icon_url = in.readString();
        this.app_search_deep_link = (BranchLinkResult) in.readValue(BranchLinkResult.class.getClassLoader());
        this.ranking_hint = in.readString();
        this.score = in.readFloat();
        this.deep_links = in.createTypedArrayList(BranchLinkResult.CREATOR);
    }

    public static final Creator<BranchAppResult> CREATOR = new Creator<BranchAppResult>() {
        @Override
        public BranchAppResult createFromParcel(Parcel source) {
            return new BranchAppResult(source);
        }

        @Override
        public BranchAppResult[] newArray(int size) {
            return new BranchAppResult[size];
        }
    };

    /***
     * Opens the app to the home screen if installed or fallback to play store if opted.
     * @param context Application context
     * @param fallbackToPlayStore If set, opens the app in play store if not installed
     * @return BranchSearchError Return  {@link BranchSearchError} in case of an error else null
     */
    public BranchSearchError openApp(Context context, boolean fallbackToPlayStore) {
        return Util.openApp(context,fallbackToPlayStore, app_store_id)? null : new BranchSearchError(BranchSearchError.ERR_CODE.ROUTING_ERR_UNABLE_TO_OPEN_APP);
    }

    /**
     * Triggers the deep link that will open the app or website and search for the query that the user entered.
     * 1. Try to open the app with deep linking, if app is installed on the device
     * 2. Opens the browser with the web fallback link
     * 3. Opens the play store if none of the above succeeded and fallbackToPlayStore is set to true
     *
     * @param context             Application context
     * @param fallbackToPlayStore If set to {@code true} fallbacks to the Google play if the app is not installed and there is no valid web url.
     * @return BranchSearchError {@link BranchSearchError} object to pass any error with complete action. Null if succeeded.
     */
    public BranchSearchError openSearchDeepLink(Context context, boolean fallbackToPlayStore) {
        if (app_search_deep_link != null) {
            return app_search_deep_link.openContent(context, fallbackToPlayStore);
        }
        return openApp(context, fallbackToPlayStore);
    }

    /**
     * This method will inform whether the search deep link is available, or if the app will just
     * open to the home page when "openSearchDeepLink" is called.
     *
     * @return boolean  True if the search deep link is available. False if not.
     */
    public boolean isSearchDeepLinkAvailable() {
        return app_search_deep_link != null;
    }
}

