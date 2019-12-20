package io.branch.search;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CircularProgressDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A dialog that can render deepviews.
 * Deepviews are renderer natively without using WebViews.
 */
public class BranchDeepViewFragment extends DialogFragment {

    public static final String TAG = "BranchDeepViewFragment";

    private static final String KEY_LINK = "link";
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder().build();

    private static final String PLAY_STORE_APP_URL_PREFIX
            = "https://play.google.com/store/apps/details?id=";
    private static final String APP_ICON_URL_SMALL_SUFFIX = "=s90";

    @NonNull
    static BranchDeepViewFragment getInstance(@NonNull BranchLinkResult link) {
        BranchDeepViewFragment fragment = new BranchDeepViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_LINK, link);
        fragment.setArguments(args);
        fragment.setCancelable(true);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // We want to have all the default attributes based on the current runtime Context
        // (light vs. dark, rounded corners, window background, ...) but override a few ones
        // that are defined in the R.style.BranchDeepViewFragment.
        // First, find a good base context: (this is how the Dialog class does it)
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.branch_deepview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final BranchLinkResult link = getArguments().getParcelable(KEY_LINK);
        if (link == null) return; // can't happen

        // App name
        TextView appName = view.findViewById(R.id.branch_deepview_app_name);
        if (appName != null) loadText(appName, link.getAppName());

        // App logo
        ImageView appIcon = view.findViewById(R.id.branch_deepview_app_icon);
        if (appIcon != null) loadImage(appIcon, link.getAppIconUrl());

        // Title
        TextView title = view.findViewById(R.id.branch_deepview_title);
        if (title != null) loadText(title, link.getName());

        // Description
        TextView description = view.findViewById(R.id.branch_deepview_description);
        if (description != null) loadText(description, link.getDescription());

        // Image
        ImageView image = view.findViewById(R.id.branch_deepview_image);
        if (image != null) {
            String url = link.getImageUrl();
            if (url != null
                    && url.equals(link.getAppIconUrl())
                    && url.endsWith(APP_ICON_URL_SMALL_SUFFIX)) {
                // Remove the =s90 at the end of our app icon urls. This makes them 90x90
                // which is not suitable for fullscreen images.
                url = url.substring(0, url.length() - APP_ICON_URL_SMALL_SUFFIX.length());
            }
            loadImage(image, url);
        }

        // Button
        Button button = view.findViewById(R.id.branch_deepview_button);
        if (button != null) {
            int background = ContextCompat.getColor(getContext(),
                    R.color.branch_deepview_button_background);
            int text = ContextCompat.getColor(getContext(),
                    R.color.branch_deepview_button_text);
            button.getBackground().setColorFilter(background,
                    PorterDuff.Mode.SRC_IN);
            button.setTextColor(text);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = PLAY_STORE_APP_URL_PREFIX + link.getDestinationPackageName();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    dismiss();
                }
            });
        } else {
            throw new IllegalStateException("Call to action button is missing!");
        }

        // Close button
        View close = view.findViewById(R.id.branch_deepview_close);
        if (close != null) {
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    private void loadText(TextView textView, @Nullable String text) {
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }
    }

    private void loadImage(@NonNull final ImageView imageView, @Nullable String url) {
        CircularProgressDrawable progress = new CircularProgressDrawable(getContext());
        progress.setArrowEnabled(false);
        progress.setCenterRadius(getResources()
                .getDimension(R.dimen.branch_deepview_loading_radius));
        progress.setStrokeWidth(getResources()
                .getDimension(R.dimen.branch_deepview_loading_stroke));
        progress.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.branch_deepview_loading));
        progress.start();
        imageView.setImageDrawable(progress);
        HttpUrl httpUrl = url == null ? null : HttpUrl.parse(url);
        if (httpUrl == null) {
            imageView.setVisibility(View.GONE);
        } else {
            Request request = new Request.Builder().url(httpUrl).build();
            CLIENT.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    InputStream stream = null;
                    try {
                        // Body is not null as per docs
                        //noinspection ConstantConditions
                        stream = response.body().byteStream();
                        final Bitmap bitmap = BitmapFactory.decodeStream(stream);
                        imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    } catch (Exception e) {
                        imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setVisibility(View.GONE);
                            }
                        });
                    } finally {
                        if (stream != null) {
                            stream.close();
                        }
                        response.close();
                    }
                }
            });
        }
    }

    public static class PercentImageView extends ImageView {
        public PercentImageView(Context context) {
            super(context);
        }

        public PercentImageView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public PercentImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                    (int) (0.4F * getResources().getDisplayMetrics().heightPixels),
                    MeasureSpec.EXACTLY
            ));
        }
    }
}
