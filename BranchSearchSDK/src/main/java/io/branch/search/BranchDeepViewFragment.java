package io.branch.search;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
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
        final BranchLinkResult link;
        Bundle args = getArguments();
        if (args == null || (link = args.getParcelable(KEY_LINK)) == null) {
            throw new IllegalStateException("No link!");
        }

        // App name
        TextView appName = view.findViewById(R.id.branch_deepview_app_name);
        if (appName != null) {
            loadText(appName, link.getAppName());
        }

        // App logo
        ImageView appIcon = view.findViewById(R.id.branch_deepview_app_icon);
        if (appIcon != null) {
            loadImage(appIcon, link.getAppIconUrl());
        }

        // Title
        TextView title = view.findViewById(R.id.branch_deepview_title);
        if (title != null) {
            loadText(title, link.getName());
        }

        // Description
        TextView description = view.findViewById(R.id.branch_deepview_description);
        if (description != null) {
            loadText(description, link.getDescription());
        }

        // Image
        ImageView image = view.findViewById(R.id.branch_deepview_image);
        if (image != null) {
            String url = link.getImageUrl();
            if (url != null
                    && url.equals(link.getAppIconUrl())
                    && url.endsWith("=s90")) {
                url = url.substring(0, url.length() - "=s90".length());
            }
            loadImage(image, url);
        }

        // Button
        Button button = view.findViewById(R.id.branch_deepview_button);
        if (button != null) {
            int color = ContextCompat.getColor(getContext(),
                    R.color.branch_deepview_button_background);
            float luminance = (float) ((0.2126 * Color.red(color))
                    + (0.7152 * Color.green(color))
                    + (0.0722 * Color.blue(color))) / 255;
            button.getBackground().setColorFilter(color,
                    PorterDuff.Mode.SRC_IN);
            button.setTextColor(luminance > 0.5F ? Color.BLACK : Color.WHITE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "https://play.google.com/store/apps/details?id="
                            + link.getDestinationPackageName();
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
        Context context = imageView.getContext();
        CircularProgressDrawable progress = new CircularProgressDrawable(context);
        float density = context.getResources().getDisplayMetrics().density;
        progress.setCenterRadius(16 * density);
        progress.setStrokeWidth(4 * density);
        progress.setArrowEnabled(false);
        int color = ContextCompat.getColor(getActivity(), R.color.branch_deepview_loading);
        progress.setColorSchemeColors(color);
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
            this(context, null);
        }

        public PercentImageView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
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
