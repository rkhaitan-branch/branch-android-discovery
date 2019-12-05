package io.branch.search;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * A dialog that hosts a WebView where we can show deepviews.
 */
public class BranchDeepViewFragment extends DialogFragment {

    private static final String KEY_DESTINATION = "destination";

    @NonNull
    public static BranchDeepViewFragment getInstance(@NonNull BranchLinkResult result) {
        String ctaUrl = "https://play.google.com/store/apps/details?id="
                + result.getDestinationPackageName();
        Uri destination = Uri.parse("https://littlewhip.app.link")
                .buildUpon()
                .appendPath("deepview")
                .appendQueryParameter("og_title", result.getName())
                .appendQueryParameter("og_description", result.getDescription())
                .appendQueryParameter("og_image_url", result.getImageUrl())
                .appendQueryParameter("cta_url", ctaUrl)
                // .appendQueryParameter("app_name", result.ge)
                .appendQueryParameter("app_image_url", result.getAppIconUrl())
                .build();
        return getInstance(destination);
    }

    @NonNull
    private static BranchDeepViewFragment getInstance(@NonNull Uri destination) {
        BranchDeepViewFragment fragment = new BranchDeepViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_DESTINATION, destination);
        fragment.setArguments(args);
        return fragment;
    }

    private WebView mWebView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = new WebView(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWebView = (WebView) view;
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(false);

        Bundle args = getArguments();
        Uri uri;
        if (args != null && (uri = args.getParcelable(KEY_DESTINATION)) != null) {
            mWebView.loadUrl(uri.toString());
        } else {
            throw new IllegalStateException("No destination!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWebView = null;
    }

}
