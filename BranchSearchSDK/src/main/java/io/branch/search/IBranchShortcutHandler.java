package io.branch.search;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
        @Override
        public boolean validateShortcut(@NonNull Context context,
                                        @NonNull String id,
                                        @NonNull String packageName) {
            if (Build.VERSION.SDK_INT < 25) return false;
            LauncherApps launcherApps = context.getSystemService(LauncherApps.class);
            try {
                LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
                query.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                        | LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                        | LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED);
                query.setPackage(packageName);
                List<ShortcutInfo> shortcuts = launcherApps.getShortcuts(query, Process.myUserHandle());
                if (shortcuts != null) {
                    for (ShortcutInfo shortcut : shortcuts) {
                        if (shortcut.isEnabled() && shortcut.getId().equals(id)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                // Not a launcher, not installed, invalid, ....
            }
            return false;
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
            } catch (Exception e) {
                return false;
            }
        }
    };
}
