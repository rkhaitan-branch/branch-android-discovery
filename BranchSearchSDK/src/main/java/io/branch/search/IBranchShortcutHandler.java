package io.branch.search;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Process;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles Android's shortcut validation and launch.
 */
public interface IBranchShortcutHandler {

    /**
     * Validates the given Android shortcut to understand if it's valid or not.
     * @param context context
     * @param id shortcut id
     * @param packageName package name
     * @return true if this shortcut should be shown
     */
    boolean validateShortcut(@NonNull Context context, @NonNull String id, @NonNull String packageName);

    /**
     * Launches the given Android shortcut, assuming it was previously validated.
     * @param context context
     * @param id shortcut id
     * @param packageName package name
     * @return true if launched correctly
     */
    boolean launchShortcut(@NonNull Context context, @NonNull String id, @NonNull String packageName);

    /**
     * The default shortcut handler.
     */
    IBranchShortcutHandler DEFAULT = new IBranchShortcutHandler() {
        // Caching values between results of the same query and across queries.
        // This is important because querying is not so fast.
        // https://developer.android.com/reference/android/content/pm/LauncherApps.ShortcutQuery
        private final Map<String, Set<String>> mCache = new HashMap<>();

        @Override
        public boolean validateShortcut(@NonNull Context context,
                                        @NonNull String id,
                                        @NonNull String packageName) {
            if (Build.VERSION.SDK_INT < 25) return false;
            if (!mCache.containsKey(id)) {
                Context appContext = BranchSearch.getInstance().getApplicationContext();
                LauncherApps launcherApps = appContext.getSystemService(LauncherApps.class);
                Set<String> ids = new HashSet<>();
                try {
                    LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
                    query.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                            | LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                            | LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED);
                    query.setPackage(packageName);
                    List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle());
                    if (shortcuts != null) {
                        for (ShortcutInfo shortcut : shortcuts) {
                            if (shortcut.isEnabled()) ids.add(shortcut.getId());
                        }
                    }
                } catch (SecurityException | IllegalStateException e) {
                    // Not a launcher
                }
                mCache.put(packageName, ids);
            }
            //noinspection ConstantConditions
            return mCache.get(packageName).contains(id);
        }

        @Override
        public boolean launchShortcut(@NonNull Context context,
                                   @NonNull String id,
                                   @NonNull String packageName) {
            if (Build.VERSION.SDK_INT < 25) return false;
            try {
                LauncherApps apps = context.getSystemService(LauncherApps.class);
                apps.startShortcut(packageName, id, null, null,
                        Process.myUserHandle());
                return true;
            } catch (SecurityException e) {
                return false;
            }
        }
    };
}
