package io.branch.search;

import android.content.Context;
import android.os.Build;
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
 */
public class BranchDeviceInfo {
    private static final String UNKNOWN_CARRIER = "bnc_no_value";

    private static String _carrierName = UNKNOWN_CARRIER;
    private static DisplayMetrics _displayMetrics;
    private static Locale _locale;

    enum JSONKey {
        Brand("brand"),
        Model("model"),
        OS("os"),
        OSVersion("os_version"),
        Carrier("carrier"),
        Locale("locale"),
        SDK("sdk"),
        SDKVersion("sdk_version"),

        ScreenDpi("screen_dpi"),
        ScreenWidth("screen_width"),
        ScreenHeight("screen_height");

        JSONKey(String key) {
            _key = key;
        }

        @Override
        public String toString() {
            return _key;
        }

        private String _key;
    }

    static void init(Context context) {
        initCarrierCheck(context);
        initDisplayMetrics(context);
        initLocale(context);
    }

    /**
     * Returns the hardware manufacturer of the current device, as defined by the manufacturer.
     *
     * @return A value containing the hardware manufacturer of the current device.
     * @see <a href="http://developer.android.com/reference/android/os/Build.html#MANUFACTURER">Build.MANUFACTURER</a>
     */
    private static String getBrand() {
        return android.os.Build.MANUFACTURER;
    }

    /**
     * Returns the hardware model of the current device, as defined by the manufacturer.
     *
     * @return A value containing the hardware model of the current device.
     * @see <a href="http://developer.android.com/reference/android/os/Build.html#MODEL">Build.MODEL</a>
     */
    private static String getModel() {
        return android.os.Build.MODEL;
    }

    private static String getLocale() {
        if (_locale != null) {
            return Util.getLocaleString(_locale);
        }
        return "en-US";
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
    private static int getOSVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * Returns the name of current registered operator.
     *
     * @return A value containing the name of current registered operator.
     * @see <a href="https://developer.android.com/reference/android/telephony/TelephonyManager.html#getNetworkOperatorName()">Carrier</a>
     */
    private static String getCarrier() {
        return _carrierName;
    }

    private static void initCarrierCheck(Context context) {

        try {
            if (context != null) {
                TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                _carrierName = manager.getNetworkOperatorName();
            }
        } catch (Exception e) {
        }

        if (TextUtils.isEmpty(_carrierName)) {
            _carrierName = UNKNOWN_CARRIER;
        }
    }

    private static void initDisplayMetrics(Context context) {
        _displayMetrics = new DisplayMetrics();
        if (context != null) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                Display display = windowManager.getDefaultDisplay();
                display.getMetrics(_displayMetrics);
            }
        }
    }

    private static void initLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            _locale = context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            _locale = context.getResources().getConfiguration().locale;
        }
    }

    /**
     * Add Device Information to a JSON object.
     */
    public static void addDeviceInfo(JSONObject jsonObject) {
        addDeviceInfo(jsonObject, JSONKey.Brand.toString(), getBrand());
        addDeviceInfo(jsonObject, JSONKey.Carrier.toString(), getCarrier());
        addDeviceInfo(jsonObject, JSONKey.Locale.toString(), getLocale());
        addDeviceInfo(jsonObject, JSONKey.Model.toString(), getModel());
        addDeviceInfo(jsonObject, JSONKey.OSVersion.toString(), getOSVersion());
        addDeviceInfo(jsonObject, JSONKey.OS.toString(), "ANDROID");
        addDeviceInfo(jsonObject, JSONKey.SDK.toString(), "discovery_android");
        addDeviceInfo(jsonObject, JSONKey.SDKVersion.toString(), BranchSearch.getVersion());

        if (_displayMetrics != null) {
            addDeviceInfo(jsonObject, JSONKey.ScreenDpi.toString(), _displayMetrics.densityDpi);
            addDeviceInfo(jsonObject, JSONKey.ScreenWidth.toString(), _displayMetrics.widthPixels);
            addDeviceInfo(jsonObject, JSONKey.ScreenHeight.toString(), _displayMetrics.heightPixels);
        }
    }

    private static <T> void addDeviceInfo(JSONObject jsonObject, String key, T value) {
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
        }
    }
}