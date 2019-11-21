package io.branch.search.demo;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import io.branch.search.BranchSearch;
import io.branch.search.BranchSearchError;
import io.branch.search.BranchServiceEnabledResult;
import io.branch.search.IBranchServiceEnabledEvents;
import io.branch.search.demo.util.DimensionMatrix;


/**
 * Created by sojanpr on 9/30/16.
 * <p>
 * Application class for Branch Search Demo
 * </p>
 */
public class BranchSearchDemoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DimensionMatrix.init(getApplicationContext());
        BranchSearch.isServiceEnabled(this, new IBranchServiceEnabledEvents() {
            @Override
            public void onBranchServiceEnabledResult(@NonNull BranchServiceEnabledResult result) {
                Log.d("BranchSearchDemoApp", "Got service enabled result: " + result.isEnabled());
            }
        });
    }
}
