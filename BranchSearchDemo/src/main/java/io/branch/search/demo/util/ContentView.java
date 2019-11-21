package io.branch.search.demo.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.lang.reflect.Field;

import io.branch.search.BranchSearchResult;

/**
 * Created by sojanpr on 2/23/17.
 * <p>
 * View for showing content recommendation and app recommendations and search results
 * </p>
 */
@SuppressWarnings("JavaReflectionMemberAccess")
public class ContentView extends ListView {
    ContentAdapter contentAdapter;
    final int DIVIDER_HEIGHT = 0;

    public ContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ContentView(Context context) {
        super(context);
        init(context);
    }

    public ContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.setDividerHeight(DIVIDER_HEIGHT);
        this.setVerticalScrollBarEnabled(false);
        this.setFocusableInTouchMode(true);
        //noinspection deprecation
        this.setCacheColorHint(getResources().getColor(android.R.color.transparent));
        this.setDrawingCacheEnabled(false);
        this.setBackground(null);
        this.setScrollingCacheEnabled(false);


        contentAdapter = new ContentAdapter(context);
        setAdapter(contentAdapter);

        LinearLayout.LayoutParams contentTitleLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentTitleLP.gravity = Gravity.BOTTOM;
    }

    public void updateSearchResults(BranchSearchResult branchSearchResult) {
        contentAdapter.updateUIWithContentSearchResult(branchSearchResult);
    }


}
