package io.branch.search;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Branch Configuration.  Use the Branch Configuration to override default Search options.
 * <br><br>
 * Note that any empty or null options will continue to use the defaults.
 */
public class BranchConfiguration {
    final static String MANIFEST_KEY = "io.branch.sdk.BranchKey";

    private String url = BranchSearchInterface.BRANCH_SEARCH_URL;
    private String key;

    private String googleAdID;
    private boolean is_lat;
    private Locale locale;      ///< Override BranchDeviceInfo
    private String countryCode;
    private int intentFlags = Intent.FLAG_ACTIVITY_NEW_TASK;
    private final Map<String, Object> requestExtra = new HashMap<>();
    private IBranchShortcutHandler shortcutHandler = IBranchShortcutHandler.DEFAULT;

    // JSONKeys associated with a Configuration
    enum JSONKey {
        BranchKey("branch_key"),
        Country("country"),
        GAID("gaid"),
        LAT("is_lat"),
        /** matches {@link BranchDiscoveryRequest.JSONKey#Extra} */
        RequestExtra(BranchDiscoveryRequest.JSONKey.Extra.toString()),
        /** overrides {@link BranchDeviceInfo.JSONKey#Locale} */
        Locale(BranchDeviceInfo.JSONKey.Locale.toString());

        JSONKey(@NonNull String key) {
            _key = key;
        }

        @NonNull
        @Override
        public String toString() {
            return _key;
        }

        private String _key;
    }


    public BranchConfiguration() {
    }

    /**
     * Update this configuration to defaults.
     * @param context Context
     */
    boolean ensureValid(@NonNull Context context) {
        // Check to see if the configuration already has a valid branch key.  Default if not.
        if (!hasValidKey()) {
            fetchBranchKey(context);
        }

        // Check to see if the configuration already has a valid country code.  Default if not.
        if (TextUtils.isEmpty(countryCode)) {
            this.countryCode = Util.getCountryCode(context);
        }

        // If we still don't have a valid key, signal to the caller.
        return hasValidKey();
    }

    /**
     * Override the Search URL.
     * @param url URL to use, or null to use the default
     * @return this BranchConfiguration
     */
    @NonNull
    public BranchConfiguration setUrl(@Nullable String url) {
        this.url = url != null ? url : BranchSearchInterface.BRANCH_SEARCH_URL;
        return this;
    }

    // Package Private
    @NonNull
    String getUrl() {
        return this.url;
    }

    /**
     * Override the default Branch Key.
     * @param key Key to use, or null to use the default
     * @return this BranchConfiguration
     */
    @SuppressWarnings("UnusedReturnValue")
    @NonNull
    public BranchConfiguration setBranchKey(@Nullable String key) {
        this.key = key;
        return this;
    }

    /**
     * Override the default Intent Launch Flags.
     * See {@link android.content.Intent#addFlags(int)} for available flags.
     * @param flags The flags to set
     * @return this BranchConfiguration
     */
    @SuppressWarnings("UnusedReturnValue")
    @NonNull
    public BranchConfiguration setLaunchIntentFlags(int flags) {
        this.intentFlags = flags;
        return this;
    }

    // Undocumented
    public int getLaunchIntentFlags() {
        return intentFlags;
    }

    /**
     * Enables or disables ad tracking limiting.
     * @param limit true to limit
     */
    void limitAdTracking(boolean limit) {
        is_lat = limit;
    }

    /**
     * Whether ad tracking is limited or not.
     * @return true if limited
     */
    @SuppressWarnings("WeakerAccess")
    boolean isAdTrackingLimited() {
        return this.is_lat;
    }

    /**
     * @return the Key.
     */
    @Nullable
    String getBranchKey() {
        return this.key;
    }

    /**
     * Set the Configuration Google Ad ID
     * @param id Google Ad ID
     * @return this BranchConfiguration
     */
    @SuppressWarnings("UnusedReturnValue")
    @NonNull
    BranchConfiguration setGoogleAdID(@Nullable String id) {
        this.googleAdID = id;
        return this;
    }

    /**
     * @return the Google Ad Id.
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    String getGoogleAdID() {
        return this.googleAdID;
    }

    /**
     * Set the Configuration Locale.
     * @param locale Locale.
     * @return this BranchConfiguration
     */
    @SuppressWarnings("UnusedReturnValue")
    @NonNull
    BranchConfiguration setLocale(@Nullable Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Set the Country Code.  eg. "US"
     * @param cc Country Code
     * @return this BranchConfiguration
     */
    BranchConfiguration setCountryCode(String cc) {
        this.countryCode = cc;
        return this;
    }

    String getCountryCode() {
        return this.countryCode;
    }

    /**
     * @return true if the Branch Key is valid.
     */
    boolean hasValidKey() {
        return (this.key != null && this.key.startsWith("key_live"));
    }

    /**
     * Set the Branch Key from the Package Manager Metadata.
     * @param context Context
     */
    private void fetchBranchKey(@NonNull Context context) {
        String key = null;
        try {
            final ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData != null) {
                key = ai.metaData.getString(MANIFEST_KEY);
            }
        } catch (final Exception ignore) {
        }
        setBranchKey(key);
    }

    /**
     * Adds extra data that will be attached to server requests in form
     * of a key-value pair. If request specific values are passed to
     * {@link BranchDiscoveryRequest#setExtra(String, Object)}, those values
     * will override the ones specified here with the same key.
     * @param key a key
     * @param data value
     */
    @SuppressWarnings("WeakerAccess")
    public void addRequestExtra(@NonNull String key, @NonNull Object data) {
        requestExtra.put(key, data);
    }

    /**
     * Override the default shortcut handler to validate and launch Shortcut results.
     * @param shortcutHandler handler to use
     * @return this BranchConfiguration
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    @NonNull
    public BranchConfiguration setShortcutHandler(@NonNull IBranchShortcutHandler shortcutHandler) {
        this.shortcutHandler = shortcutHandler;
        return this;
    }

    /**
     * Returns the shortcut handler.
     * @see #setShortcutHandler(IBranchShortcutHandler)
     * @return the shortcut handler
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public IBranchShortcutHandler getShortcutHandler() {
        return shortcutHandler;
    }

    /**
     * Add Configuration Information to a JSON object.
     */
    JSONObject addConfigurationInfo(JSONObject jsonObject) {
        try {
            jsonObject.putOpt(JSONKey.BranchKey.toString(), getBranchKey());
            jsonObject.putOpt(JSONKey.Country.toString(), countryCode);

            if (locale != null) {
                jsonObject.putOpt(JSONKey.Locale.toString(), locale.getDisplayName());
            }

            // Pass the GAID, but also pass the LAT flag.
            jsonObject.putOpt(JSONKey.GAID.toString(), getGoogleAdID());
            jsonObject.putOpt(JSONKey.LAT.toString(), (isAdTrackingLimited() ? 1 : 0));

            // Add extra request data.
            // The JSONObject for this key might already exist because the key is shared
            // between this class and BranchDiscoveryRequest.
            if (!requestExtra.keySet().isEmpty()) {
                JSONObject extraData = jsonObject.optJSONObject(JSONKey.RequestExtra.toString());
                if (extraData == null) extraData = new JSONObject();

                for (String key : requestExtra.keySet()) {
                    Object value = requestExtra.get(key);
                    if (!extraData.has(key)) {
                        extraData.putOpt(key, value);
                    }
                }
                jsonObject.putOpt(JSONKey.RequestExtra.toString(), extraData);
            }

        } catch (JSONException ignore) {
        }
        return jsonObject;
    }


}