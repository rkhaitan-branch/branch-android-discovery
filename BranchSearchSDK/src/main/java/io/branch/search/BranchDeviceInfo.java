package io.branch.search;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Branch DeviceInfo.
 *
 * Just like {@link BranchConfiguration}, this class must be synced before using through
 * {@link #sync(Context)}. The class will also sync automatically anytime it's used in
 * {@link #addDeviceInfo(JSONObject)}, with a 1-hour interval between checks.
 * This ensures that information here is always up to date.
 */
class BranchDeviceInfo {
    private static final String UNKNOWN_CARRIER = "bnc_no_value";
    private static final String DEFAULT_LOCALE = "en-US";
    private static final long SYNC_TIME_MILLIS = 1000 * 60 * 60; // 1 hour

    private String carrierName = UNKNOWN_CARRIER;
    private DisplayMetrics displayMetrics = null;
    private String locale = DEFAULT_LOCALE;
    private String appPackage = null;
    private String appVersion = null;

    private long lastSyncTimeMillis = 0L;
    private final Object syncLock = new Object();

    enum JSONKey {
        Brand("brand"),
        Model("model"),
        OS("os"),
        OSVersion("os_version"),
        Carrier("carrier"),
        Locale("locale"),
        SDK("sdk"),
        SDKVersion("sdk_version"),

        AppPackage("app_package"),
        AppVersion("app_version"),

        ScreenDpi("screen_dpi"),
        ScreenWidth("screen_width"),
        ScreenHeight("screen_height");

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

    BranchDeviceInfo() {
    }

    /**
     * Ensure that this object is in a valid state and updated.
     * Some values might be identical but some others might change.
     * @param context a context
     */
    void sync(@NonNull Context context) {
        // Check for carrier name.
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            carrierName = manager.getNetworkOperatorName();
        } catch (Exception ignore) { }
        if (TextUtils.isEmpty(carrierName)) {
            carrierName = UNKNOWN_CARRIER;
        }

        // Check for display metrics.
        // Apparently the display can be null in some cases.
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display;
        if (windowManager != null && (display = windowManager.getDefaultDisplay()) != null) {
            displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
        } else {
            displayMetrics = null;
        }

        // Check for locale.
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        if (locale != null) {
            this.locale = Util.getLocaleString(locale);
        } else {
            this.locale = DEFAULT_LOCALE;
        }

        // Check for app version and package.
        appPackage = context.getPackageName();
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(appPackage, 0);
            appVersion = info.versionName;
        } catch (PackageManager.NameNotFoundException ignore) {
            // Can't happen.
        }

        lastSyncTimeMillis = System.currentTimeMillis();
    }

    /**
     * Returns the hardware manufacturer of the current device, as defined by the manufacturer.
     *
     * @return A value containing the hardware manufacturer of the current device.
     * @see <a href="http://developer.android.com/reference/android/os/Build.html#MANUFACTURER">Build.MANUFACTURER</a>
     */
    @NonNull
    private String getBrand() {
        return android.os.Build.MANUFACTURER;
    }

    /**
     * Returns the hardware model of the current device, as defined by the manufacturer.
     *
     * @return A value containing the hardware model of the current device.
     * @see <a href="http://developer.android.com/reference/android/os/Build.html#MODEL">Build.MODEL</a>
     */
    @NonNull
    private String getModel() {
        return android.os.Build.MODEL;
    }

    @NonNull
    private String getLocale() {
        return locale;
    }

    @Nullable
    private String getAppPackage() {
        return appPackage;
    }

    @Nullable
    private String getAppVersion() {
        return appVersion;
    }

    /**
     * Returns the Android API version of the current device as an Integer.
     * Common values:
     * <ul>
     * <li>22 - Android 5.1, Lollipop MR1</li>
     * <li>21 - Android 5.0, Lollipop</li>
     * <li>19 - Android 4.4, Kitkat</li>
     * <li>18 - Android 4.3, Jellybean</li>
     * <li>15 - Android 4.0.4, Ice Cream Sandwich MR1</li>
     * <li>13 - Android 3.2, Honeycomb MR2</li>
     * <li>10 - Android 2.3.4, Gingerbread MR1</li>
     * </ul>
     *
     * @return An Integer value representing the SDK/Platform Version of the OS of the
     * current device.
     * @see <a href="http://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels">Android Developers - API Level and Platform Version</a>
     */
    private int getOSVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * Returns the name of current registered operator.
     *
     * @return A value containing the name of current registered operator.
     * @see <a href="https://developer.android.com/reference/android/telephony/TelephonyManager.html#getNetworkOperatorName()">Carrier</a>
     */
    @NonNull
    private String getCarrier() {
        return carrierName;
    }

    /**
     * Add Device Information to a JSON object.
     */
    void addDeviceInfo(@NonNull JSONObject jsonObject) {
        // Anytime we're being used, see if we should re-sync.
        // Synchronize just in case someone fires requests from different threads at once.
        synchronized (syncLock) {
            long now = System.currentTimeMillis();
            if (now > lastSyncTimeMillis + SYNC_TIME_MILLIS) {
                BranchSearch search = BranchSearch.getInstance();
                if (search != null) {
                    sync(search.getApplicationContext());
                } else {
                    // Object being used but BranchSearch not initialized.
                    // This can happen in tests. Ignore.
                }
            }
        }

        // Write.
        addDeviceInfo(jsonObject, JSONKey.Brand.toString(), getBrand());
        addDeviceInfo(jsonObject, JSONKey.Carrier.toString(), getCarrier());
        addDeviceInfo(jsonObject, JSONKey.Locale.toString(), getLocale());
        addDeviceInfo(jsonObject, JSONKey.Model.toString(), getModel());
        addDeviceInfo(jsonObject, JSONKey.OSVersion.toString(), getOSVersion());
        addDeviceInfo(jsonObject, JSONKey.OS.toString(), "ANDROID");
        addDeviceInfo(jsonObject, JSONKey.SDK.toString(), "discovery_android");
        addDeviceInfo(jsonObject, JSONKey.SDKVersion.toString(), BranchSearch.getVersion());
        if (displayMetrics != null) {
            addDeviceInfo(jsonObject, JSONKey.ScreenDpi.toString(), displayMetrics.densityDpi);
            addDeviceInfo(jsonObject, JSONKey.ScreenWidth.toString(), displayMetrics.widthPixels);
            addDeviceInfo(jsonObject, JSONKey.ScreenHeight.toString(), displayMetrics.heightPixels);
        }
        String appPackage = getAppPackage();
        String appVersion = getAppVersion();
        if (appPackage != null) {
            addDeviceInfo(jsonObject, JSONKey.AppPackage.toString(), appPackage);
        }
        if (appVersion != null) {
            addDeviceInfo(jsonObject, JSONKey.AppVersion.toString(), appVersion);
        }
    }

    private static <T> void addDeviceInfo(@NonNull JSONObject jsonObject,
                                          @NonNull String key,
                                          @Nullable T value) {
        try {
            jsonObject.put(key, value);
        } catch (JSONException ignore) {
        }
    }
}