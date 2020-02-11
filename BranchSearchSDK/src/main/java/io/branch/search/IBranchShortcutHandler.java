package io.branch.search;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.os.Build;
import android.os.Process;
import android.support.annotation.NonNull;

/**
 * Handles Android's shortcut validation and launch.
 */
public interface IBranchShortcutHandler {

    /**
     * Launches the given Android shortcut.
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
