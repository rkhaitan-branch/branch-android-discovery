package io.branch.search.demo.util;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import io.branch.search.demo.R;


/**
 * Created by sojanpr on 3/18/18.
 * <p>
 * Class for managing permission and permission requests
 * </p>
 */

public class PermissionManager {
    
    public static void requestPermissions(final Activity activity, final String permission, final int reqCode) {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        permission);
        
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            showSnackbar(activity, R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{permission},
                                    reqCode);
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    reqCode);
        }
    }
    
    
    private static void showSnackbar(Activity activity, final int mainTextStringId, final int actionStringId,
                                     View.OnClickListener listener) {
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                activity.getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(activity.getString(actionStringId), listener).show();
    }
    
}
