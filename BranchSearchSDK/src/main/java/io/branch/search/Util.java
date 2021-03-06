package io.branch.search;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by sojanpr on 3/17/18.
 * <p>
 * Holder for general utilities
 * </p>
 */
class Util {
    private static String isoCountryCode;

    /**
     * Return the ISO2 Country Code.
     * @param context Context
     * @return the ISO2 Country Code.  Note that this will return something that looks like "US" for the United States.
     */
    static String getCountryCode(Context context) {
        if (TextUtils.isEmpty(isoCountryCode)) {
            try {
                final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    isoCountryCode = tm.getSimCountryIso();
                    if (TextUtils.isEmpty(isoCountryCode)) {
                        if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
                            isoCountryCode = tm.getNetworkCountryIso();
                        }
                    }
                }
            } catch (Exception ignore) {
            }

            // If Country code is not available form Telephony check with locale
            if (context != null && TextUtils.isEmpty(isoCountryCode)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    isoCountryCode = context.getResources().getConfiguration().getLocales().get(0).getCountry();
                } else {
                    isoCountryCode = context.getResources().getConfiguration().locale.getCountry();
                }
            }
        }
        if (TextUtils.isEmpty(isoCountryCode)) {
            isoCountryCode = "us"; // Default to US if not able to find country
        }
        isoCountryCode = isoCountryCode.toUpperCase();
        return isoCountryCode;
    }

    /**
     * Reference: https://developer.android.com/reference/java/util/Locale.html#toLanguageTag()
     * @return the Default Locale, as a String.
     */
    static String getLocaleString(Locale locale) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            String country = locale.getCountry();
            String language = locale.getLanguage();

            return (language + "-" + country);
        } else {
            return Locale.getDefault().toLanguageTag();
        }
    }

    /**
     * Parse a Locale String.
     * The built-in parser only exists for API 21+.
     * Found this neat solution from https://stackoverflow.com/users/846977/andy
     * @param locale Locale String
     * @return a new Locale, or null if it was unable to parse.
     */
    static Locale parseLocaleString(String locale) {
        Locale tmpLocale;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            String[] parts = locale.split("_", -1);
            if (parts.length == 1) {
                tmpLocale = new Locale(parts[0]);
            } else if (parts.length == 2 || (parts.length == 3 && parts[2].startsWith("#"))) {
                tmpLocale = new Locale(parts[0], parts[1]);
            } else {
                tmpLocale = new Locale(parts[0], parts[1], parts[2]);
            }
        } else {
            tmpLocale = Locale.forLanguageTag(locale);
        }

        return tmpLocale;
    }

    static boolean openApp(Context context, boolean fallbackToPlayStore, String destinationStoreID) {
        if (!TextUtils.isEmpty(destinationStoreID)) {
            try {
                Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(destinationStoreID);
                context.startActivity(LaunchIntent);
            } catch (Exception ignore1) {
                if (fallbackToPlayStore) {
                    openAppInPlayStore(context, destinationStoreID);
                }
            }
            return true;
        }
        return false;
    }

    static boolean openAppInPlayStore(Context context, String destinationStoreID) {
        boolean tryOpenApp = false;
        if (!TextUtils.isEmpty(destinationStoreID)) {
            try {
                context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + destinationStoreID)));
                tryOpenApp = true;
            } catch (ActivityNotFoundException ignore) {
            }

            if (!tryOpenApp) {
                try {
                    context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + destinationStoreID)));
                    tryOpenApp = true;
                } catch (ActivityNotFoundException ignore) {
                }
            }
        }
        return tryOpenApp;
    }

    /**
     * Handle null values in JSON.
     * There is a bug in Android where null is converted to the String "null".   We can either fix
     * the behavior by returning null correctly, or choosing to return the empty string instead.
     * It is semantically incorrect to return the empty string, but for this case it is much much
     * safer for the downstream consumers.
     * @param json JSONObject
     * @param key Key
     * @return the value for the given key, or the Empty String if not found.
     */
    static String optString(JSONObject json, String key) {
        // http://code.google.com/p/android/issues/detail?id=13830
        if (json.isNull(key))
            return ""; // << Not null
        else
            return json.optString(key, "");
    }



}

