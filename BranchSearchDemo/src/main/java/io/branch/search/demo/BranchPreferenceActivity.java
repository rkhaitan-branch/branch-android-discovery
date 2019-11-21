package io.branch.search.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.text.TextUtils;

import io.branch.search.BranchConfiguration;
import io.branch.search.BranchSearch;

public class BranchPreferenceActivity extends PreferenceActivity {
    private static final String PREF_BRANCH_INIT = "branch_init";
    private static final String PREF_BRANCH_KEY = "branch_key";
    private static final String PREF_BRANCH_URL = "branch_url";
    private static final String PREF_BRANCH_INTENT_FLAGS = "branch_intent_flags";
    private static final String PREF_USE_MOCK_LOCATION = "branch_use_mock_location";
    private static final String PREF_MOCK_LOCATION = "branch_mock_location";
    private static final String PREF_MOCK_LOCATION_LIST = "branch_mock_location_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        private boolean isResuming = false;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_preferences);

            // Initialize the preference that acts to reinitialize the SDK.
            Preference button = findPreference(PREF_BRANCH_INIT);
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Attempt to Reinitialize Branch using values from these preferences
                    if (reInitializeBranch(getActivity())) {
                        getActivity().finish();
                    }
                    return true;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            isResuming = true;
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
                Preference preference = getPreferenceScreen().getPreference(i);
                if (preference instanceof PreferenceGroup) {
                    PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                    for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                        Preference singlePref = preferenceGroup.getPreference(j);
                        updatePreference(singlePref, singlePref.getKey());
                    }
                } else {
                    updatePreference(preference, preference.getKey());
                }
            }

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            isResuming = false;
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePreference(findPreference(key), key);
        }

        // Update the preference summary based on the value of the key
        private void updatePreference(Preference preference, String key) {
            if (preference == null) return;
            if (key.equals(PREF_BRANCH_INIT)) return;

            SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;

                // Update the preference edit value here
                String value = listPreference.getValue();
                if (!isResuming) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    if (TextUtils.isEmpty(value)) {
                        editor.remove(PREF_MOCK_LOCATION);
                    } else {
                        editor.putString(PREF_MOCK_LOCATION, value);
                    }
                    editor.apply();
                    onSharedPreferenceChanged(sharedPrefs, PREF_MOCK_LOCATION);
                }
                listPreference.setSummary(listPreference.getEntry());
            } else if (preference instanceof EditTextPreference) {
                String test = sharedPrefs.getString(key, "").trim();
                if (TextUtils.isEmpty(test)) {
                    // Remove the empty preference.
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.remove(key);
                    editor.apply();
                }
                preference.setSummary(TextUtils.isEmpty(test) ? getResources().getString(R.string.pref_default) : test);
            }

            // These key changes will have side effects of enabling and disabling other preferences.
            switch(key) {
                case PREF_USE_MOCK_LOCATION:
                case PREF_MOCK_LOCATION_LIST:
                    enableCustomLocationPreferences();
                    break;
            }

        }

        // Enable/Disable the Mock Location Edit Preference when the List is set to "Custom"
        private void enableCustomLocationPreferences() {
            SwitchPreference switchPreference = (SwitchPreference)findPreference(PREF_USE_MOCK_LOCATION);
            boolean isChecked = switchPreference.isChecked();

            ListPreference listPreference = (ListPreference)findPreference(PREF_MOCK_LOCATION_LIST);
            String value = listPreference.getValue();
            listPreference.setEnabled(isChecked);

            EditTextPreference editPreference = (EditTextPreference)findPreference(PREF_MOCK_LOCATION);
            boolean enableTest = (isChecked && TextUtils.isEmpty(value));
            editPreference.setEnabled(enableTest);
        }
    }

    private static boolean reInitializeBranch(Context context) {
        Context appContext = context.getApplicationContext();
        BranchConfiguration config = new BranchConfiguration();
        config.setUrl(getBranchUrlPreference(appContext));
        config.setBranchKey(getBranchKeyPreference(appContext));
        config.setLaunchIntentFlags(getBranchIntentFlags(appContext));

        BranchSearch searchSDK = BranchSearch.init(appContext, config);
        return (searchSDK != null);
    }

    public static String getBranchKeyPreference(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(PREF_BRANCH_KEY, "");
    }

    public static String getBranchUrlPreference(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(PREF_BRANCH_URL, "");
    }

    static int getBranchIntentFlags(Context context) {
        int defaultFlags = new BranchConfiguration().getLaunchIntentFlags();

        // Converting back and forth between integers and Strings is not optimal, but for Demo Preferences acceptable.
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String pref = sharedPrefs.getString(PREF_BRANCH_INTENT_FLAGS, "").trim();

        if (TextUtils.isEmpty(pref)) {
            pref = Integer.toString(defaultFlags);
        }

        int flags = 0;
        try {
            flags = Integer.parseInt(pref);
        } catch (NumberFormatException e) {
        }

        return flags;
    }

    public static boolean useMockLocation(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(PREF_USE_MOCK_LOCATION, false);
    }

    public static Location getMockLocation(Context context) {
        Location location = new Location("dummyprovider");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (useMockLocation(context)) {
            String pref = sharedPrefs.getString(PREF_MOCK_LOCATION, "");
            if (pref.length() > 0) {
                try {
                    String[] latlong = pref.split(",");
                    location.setLatitude(Double.parseDouble(latlong[0].trim()));
                    location.setLongitude(Double.parseDouble(latlong[1].trim()));

                } catch (Exception e) {
                    location.reset();
                }
            }
        }
        return location;
    }
}