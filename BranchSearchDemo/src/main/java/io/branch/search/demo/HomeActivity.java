package io.branch.search.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

import io.branch.search.BranchQueryResult;
import io.branch.search.BranchSearch;
import io.branch.search.BranchSearchError;
import io.branch.search.BranchSearchRequest;
import io.branch.search.BranchSearchResult;
import io.branch.search.IBranchQueryResults;
import io.branch.search.IBranchSearchEvents;
import io.branch.search.demo.util.BFSearchBox;
import io.branch.search.demo.util.BranchLocationFinder;
import io.branch.search.demo.util.BranchSearchController;
import io.branch.search.demo.util.PermissionManager;


public class HomeActivity extends AppCompatActivity implements BFSearchBox.IKeywordChangeListener {
    private static final String TAG = "BranchSearchDemo";

    private BFSearchBox bfSearchBox;
    private BranchSearchController branchSearchController;
    private InputMethodManager imm;
    private List<String> queryHints;
    private static final int LOCATION_PERMISSION_REQ_CODE = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bfSearchBox = findViewById(R.id.search_txt);
        bfSearchBox.setKeywordChangeListener(this);
        branchSearchController = findViewById(R.id.recommendation_layout);
        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // Initialize the Branch Search SDK
        BranchSearch searchSDK = BranchSearch.init(getApplicationContext());
        if (searchSDK == null) {
            Toast.makeText(this, R.string.sdk_not_initialized, Toast.LENGTH_LONG).show();
            finish();
        }

        // Initialize the Location Utility.  Applications would probably do this differently based on their needs.
        BranchLocationFinder.initialize(this, new BranchLocationFinder.ILocationFinderEvents() {
            @Override
            public void onRequestLocationPermission() {
                // Location permission is optional but provides better search results if available. Please ask user for location permission from user.
                // Please Notify the BranchLocationFinder with `BranchSearch.onLocationPermissionGranted()` when location permission granted by user
                // Ignore if don't want to provide location permissions
                PermissionManager.requestPermissions(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_REQ_CODE);
            }
        });

        displaySDKVersion();
        branchSearchController.setEmptyView(findViewById(android.R.id.empty));

        fetchQueryHints();
    }

    @Override
    protected void onResume() {
        super.onResume();
        branchSearchController.expand();
        String lastSearchKeyword = bfSearchBox.getText().toString().trim();
        bfSearchBox.setText(lastSearchKeyword);

        updateQueryHint();
    }

    @Override
    public void onKeywordChanged(String keyword) {
        if (!TextUtils.isEmpty(keyword)) {
            // Obtain the last known location before searching.  Use mocks if available.
            Location location = BranchLocationFinder.getLastKnownLocation();
            if (BranchPreferenceActivity.useMockLocation(this)) {
                location = BranchPreferenceActivity.getMockLocation(this);
                Log.d(TAG, "Using Mock Location: " + location.toString());
            }

            // Create a Branch Search Request for the keyword
            BranchSearchRequest request = BranchSearchRequest.Create(keyword)
                    .setLocation(location);

            // Search for the keyword with the Branch Search SDK
            BranchSearch.getInstance().query(request, new IBranchSearchEvents() {
                @Override
                public void onBranchSearchResult(BranchSearchResult result) {
                    // Update UI with search results. BranchSearchResult contains the result of any search.
                    branchSearchController.onBranchSearchResult(result);
                }

                @Override
                public void onBranchSearchError(BranchSearchError error) {
                    if (error.getErrorCode() == BranchSearchError.ERR_CODE.REQUEST_CANCELED) {
                        Log.d(TAG, "Branch Search request was canceled.");
                    } else {
                        // Handle any errors here
                        Log.d(TAG, "Error for Branch Search. " +
                                error.getErrorCode() + " " + error.getErrorMsg());
                    }
                }
            });

            // Get Autosuggestions (Log them only)
            BranchSearch.getInstance().autoSuggest(request, new IBranchQueryResults() {
                @Override
                public void onQueryResult(final BranchQueryResult result) {
                    Log.d("Branch", "onAutoSuggest: " + result.getQueryResults().toString());
                }

                @Override
                public void onError(final BranchSearchError error) {
                    if (error.getErrorCode() == BranchSearchError.ERR_CODE.REQUEST_CANCELED) {
                        Log.d(TAG, "Branch AutoSuggest request was canceled.");
                    } else {
                        // Handle any errors here
                        Log.d(TAG, "Error for Branch AutoSuggest. " +
                                error.getErrorCode() + " " + error.getErrorMsg());
                    }
                }
            });
        }
    }

    @Override
    public void onSearchBoxClosed() {
    }

    private void hideKeyboard() {
        imm.hideSoftInputFromWindow(bfSearchBox.getWindowToken(), 0);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof BFSearchBox) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    if (view.hasFocus()) {
                        hideKeyboard();
                    }
                    view.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                BranchLocationFinder.getInstance().onLocationPermissionGranted(HomeActivity.this);
            }
        }
    }

    private void displaySDKVersion() {
        TextView about = findViewById(R.id.version_name_txt);
        String versionName = "SDK: v" + BranchSearch.getVersion();
        about.setText(versionName);
        Log.d(TAG, "Branch SDK Version: " + versionName);

        about.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(HomeActivity.this, BranchPreferenceActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

    private void fetchQueryHints() {
        BranchSearch.getInstance().queryHint(new IBranchQueryResults() {

            @Override
            public void onQueryResult(final BranchQueryResult result) {
                Log.d("Branch", "QueryHint results: " + result.getQueryResults().toString());
                queryHints = result.getQueryResults();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateQueryHint();
                    }
                });
            }

            @Override
            public void onError(final BranchSearchError error) {
                if (error.getErrorCode() == BranchSearchError.ERR_CODE.REQUEST_CANCELED) {
                    Log.d(TAG, "Branch QueryHint request was canceled.");
                } else {
                    // Handle any errors here
                    Log.d(TAG, "Error for Branch QueryHint. " +
                            error.getErrorCode() + " " + error.getErrorMsg());
                }
            }
        });

    }

    private synchronized void updateQueryHint() {
        if (queryHints != null && queryHints.size() > 0) {
            int id = new Random().nextInt(queryHints.size());
            bfSearchBox.setHint(queryHints.get(id));
        }
    }

}
