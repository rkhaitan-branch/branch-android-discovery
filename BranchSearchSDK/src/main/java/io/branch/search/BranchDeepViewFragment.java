package io.branch.search;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
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
        String imageUrl = !TextUtils.isEmpty(result.getImageUrl().trim())
                ? result.getImageUrl().trim()
                : result.getAppIconUrl().trim();
        Uri destination = Uri.parse("https://littlewhip.app.link")
                .buildUpon()
                .appendPath("deepview")
                .appendQueryParameter("og_title", result.getName())
                .appendQueryParameter("og_description", result.getDescription())
                .appendQueryParameter("og_image_url", imageUrl)
                .appendQueryParameter("cta_url", ctaUrl)
                .appendQueryParameter("app_name", result.getAppName())
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // We want to have all the default attributes based on the current runtime Context,
        // but override a few ones that are defined in the R.style.BranchDeepViewFragment.
        // This is how the Dialog class finds a good theme:
        Context context = getContext();
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.dialogTheme, value, true);
        context = new ContextThemeWrapper(context, value.resourceId);
        // Now wrap it with our style:
        context = new ContextThemeWrapper(context, R.style.BranchDeepViewFragment);
        // Finally create the dialog:
        return new Dialog(context, 0);
    }

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
