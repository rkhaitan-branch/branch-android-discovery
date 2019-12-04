package io.branch.search;

import android.content.Intent;
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
import android.webkit.WebViewClient;

/**
 * A dialog that hosts a WebView where we can show deepviews.
 */
public class BranchDeepViewFragment extends DialogFragment {

    public static final String TAG = BranchDeepViewFragment.class.getSimpleName();

    private static final String KEY_DESTINATION = "destination";

    // CTA keys are required as a temporary workaround for overriding the CTA button behavior
    // in our template, so that it directs to the correct url.
    private static final String KEY_CTA_URL = "cta_url";
    private static final String KEY_CTA_REPLACEMENT = "cta_replacement";

    @NonNull
    public static BranchDeepViewFragment getInstance(@NonNull BranchLinkResult result) {
        String cta = "https://play.google.com/store/apps/details?id="
                + result.getDestinationPackageName();
        Uri destination = Uri.parse("https://nma.app.link")
                .buildUpon()
                .appendPath("disco")
                .appendQueryParameter("$og_title", result.getName())
                .appendQueryParameter("$og_description", result.getDescription())
                .appendQueryParameter("$og_image_url", result.getImageUrl())
                .build();
        return getInstance(destination,
                Uri.parse("https://branch.io/"),
                Uri.parse(cta));
    }

    @NonNull
    private static BranchDeepViewFragment getInstance(@NonNull Uri destination,
                                                      @Nullable Uri ctaUrl,
                                                      @Nullable Uri ctaReplacement) {
        BranchDeepViewFragment fragment = new BranchDeepViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_DESTINATION, destination);
        args.putParcelable(KEY_CTA_URL, ctaUrl);
        args.putParcelable(KEY_CTA_REPLACEMENT, ctaReplacement);
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
            final Uri ctaUrl = args.getParcelable(KEY_CTA_URL);
            final Uri ctaReplacement = args.getParcelable(KEY_CTA_REPLACEMENT);
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Uri uri = Uri.parse(url);
                    if (uri.equals(ctaUrl)) {
                        uri = ctaReplacement;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
            });
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
