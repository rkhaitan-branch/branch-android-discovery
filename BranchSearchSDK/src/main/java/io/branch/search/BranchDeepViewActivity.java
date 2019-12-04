package io.branch.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

/**
 * Hosts a {@link BranchDeepViewFragment}, providing a fragment manager for it.
 */
public class BranchDeepViewActivity extends FragmentActivity {

    private final static String EXTRA_RESULT = "result";

    @NonNull
    public static Intent getIntent(@NonNull Context context, @NonNull BranchLinkResult result) {
        Intent intent = new Intent(context, BranchDeepViewActivity.class);
        intent.putExtra(EXTRA_RESULT, result);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            BranchLinkResult result = getIntent().getParcelableExtra(EXTRA_RESULT);
            BranchDeepViewFragment fragment = BranchDeepViewFragment.getInstance(result);
            fragment.show(getSupportFragmentManager(),
                    BranchDeepViewFragment.class.getSimpleName());
        }
    }
}
