package io.branch.search.demo.util;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import io.branch.search.BranchLinkResult;
import io.branch.search.demo.R;

/**
 * Created by sojanpr on 3/17/18.
 * <p>
 * Extended content result to handle Vulcan
 * </p>
 */

public class ContentDisplayHelper {

    private static final String SEARCH_NAME = "Search";
    private static final int TYPE_IMAGE_PADDING = 60;


    public static boolean isMoreSearchInSpecificAppItem(BranchLinkResult contentResult) {
        return contentResult.getType() != null && SEARCH_NAME.equals(contentResult.getType());
    }


    public static CharSequence getFormattedSearchTitle(Context context, String query, BranchLinkResult contentResult) {
        String searchTitle = "\t\tContinue search on " + contentResult.getName().replace(SEARCH_NAME, "") + " for ";
        query = "\"" + query + "\"";
        String displayText = searchTitle + "  " + query;
        Drawable searchImg = context.getResources().getDrawable(R.drawable.ic_search);
        searchImg.setBounds(0, 0, 40, 40);
        SpannableStringBuilder searchSpannableBuilder = new SpannableStringBuilder(displayText);

        searchSpannableBuilder.setSpan(new StyleSpan(Typeface.ITALIC), searchTitle.length(), displayText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        searchSpannableBuilder.setSpan(new RelativeSizeSpan(1.0f), 0, displayText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        searchSpannableBuilder.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.bf_text_bg_white)), 0, displayText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        searchSpannableBuilder.setSpan(new ImageSpan(searchImg), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return searchSpannableBuilder;
    }


    public static void loadContentImage(final Context context, BranchLinkResult contentResult, final ImageView imageView, final int size) {
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(TYPE_IMAGE_PADDING, TYPE_IMAGE_PADDING, TYPE_IMAGE_PADDING, TYPE_IMAGE_PADDING);
        if (!TextUtils.isEmpty(contentResult.getImageUrl()) || !TextUtils.isEmpty(contentResult.getAppIconUrl())) {
            String icon_url = contentResult.getImageUrl();
            if (icon_url == null || TextUtils.isEmpty(icon_url.trim())) {
                icon_url = contentResult.getAppIconUrl().trim();
            }
            Glide.with(context)
                    .load(icon_url)
                    .apply(new RequestOptions().override(size, size)
                            .centerCrop()
                            .dontAnimate()
                            .dontTransform())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            imageView.setPadding(0, 0, 0, 0);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            return false;
                        }
                    })
                    .into(imageView);
        }
    }
}
