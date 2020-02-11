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

    private String url;
    private String key;

    private String googleAdID;
    private boolean is_lat;
    private Locale locale;      ///< Override BranchDeviceInfo
    private String countryCode;
    private int intentFlags = Intent.FLAG_ACTIVITY_NEW_TASK;
    private final Map<String, Object> requestExtra = new HashMap<>();
    private IBranchShortcutHandler shortcutHandler;

    // JSONKeys associated with a Configuration
    enum JSONKey {
        BranchKey("branch_key"),
        Country("country"),
        GAID("gaid"),
        LAT("is_lat"),
        /** matches {@link BranchDiscoveryRequest.JSONKey#Extra} */
        RequestExtra("extra_data"),
        Locale(BranchDeviceInfo.JSONKey.Locale.toString()); ///< Configuration overrides DeviceInfo

        JSONKey(String key) {
            _key = key;
        }

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
    BranchConfiguration setDefaults(Context context) {

        // Check to see if the configuration already has a URL endpoint.  Default if not.
        if (TextUtils.isEmpty(url)) {
            this.url = BranchSearchInterface.BRANCH_SEARCH_URL;
        }

        // Check to see if the configuration already has a valid branch key.  Default if not.
        if (!hasValidKey()) {
            setBranchKey(context);
        }

        // Check to see if the configuration already has a valid country code.  Default if not.
        if (TextUtils.isEmpty(countryCode)) {
            this.countryCode = Util.getCountryCode(context);
        }

        // Check to see if the configuration already has a valid shortcut handler.  Default if not.
        if (shortcutHandler == null) {
            this.shortcutHandler = IBranchShortcutHandler.DEFAULT;
        }

        return this;
    }

    /**
     * Override the Search URL.
     * @param url URL to use, or null to use the default
     * @return this BranchConfiguration
     */
    public BranchConfiguration setUrl(String url) {
        this.url = url;
        return this;
    }

    // Package Private
    String getUrl() {
        return this.url;
    }

    /**
     * Override the default Branch Key.
     * @param key Key to use, or null to use the default
     * @return this BranchConfiguration
     */
    public BranchConfiguration setBranchKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * Override the default Intent Launch Flags.
     * See {@link android.content.Intent#addFlags(int)} for available flags.
     * @param flags The flags to set
     * @return this BranchConfiguration
     */
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
    String getBranchKey() {
        return this.key;
    }

    /**
     * Set the Configuration Google Ad ID
     * @param id Google Ad ID
     * @return this BranchConfiguration
     */
    BranchConfiguration setGoogleAdID(String id) {
        this.googleAdID = id;
        return this;
    }

    /**
     * @return the Google Ad Id.
     */
    String getGoogleAdID() {
        return this.googleAdID;
    }

    /**
     * Set the Configuration Locale.
     * @param locale Locale.
     * @return this BranchConfiguration
     */
    BranchConfiguration setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * @return the Locale.
     * Note that for a locale, this looks like "en-US"
     */
    Locale getLocale() {
        return this.locale;
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
    private void setBranchKey(Context context) {
        String branch_key = null;
        try {
            final ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData != null) {
                branch_key = ai.metaData.getString(MANIFEST_KEY);
            }
        } catch (final Exception ignore) {
        }

        this.key = branch_key;
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
     * @param shortcutHandler handler to use, or null to use the default
     * @return this BranchConfiguration
     */
    @NonNull
    public BranchConfiguration setShortcutHandler(@Nullable IBranchShortcutHandler shortcutHandler) {
        this.shortcutHandler = shortcutHandler;
        return this;
    }

    /**
     * Returns the shortcut handler.
     * @see #setShortcutHandler(IBranchShortcutHandler)
     * @return the shortcut handler
     */
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