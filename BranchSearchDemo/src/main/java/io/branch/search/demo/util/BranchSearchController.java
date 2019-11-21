package io.branch.search.demo.util;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import io.branch.search.BranchSearchResult;
import io.branch.search.demo.R;


/**
 * Created by sojanpr on 1/26/17.
 * <p/>
 * Controller class for Branch Local search Data.Queries and update the data for recommendations and search
 * <p>
 * see {@link ContentView} To see the view associated with this controller
 * </p>
 */
public class BranchSearchController extends RelativeLayout {
    ContentView contentView;

    public BranchSearchController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BranchSearchController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void expand() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        if (wm != null) {
            wm.getDefaultDisplay().getSize(size);
        }
        expand(getResources().getInteger(R.integer.scroll_anim_delay), size.y);
    }

    private void init(Context context) {
        contentView = new ContentView(context);
        this.addView(contentView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void expand(int duration, int targetHeight) {
        int prevHeight = 0;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getLayoutParams().height = (int) animation.getAnimatedValue();
                requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();

        View contentRoot = this.getChildAt(0);
        if (contentRoot != null) {
            Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
            contentRoot.startAnimation(slideDown);

        }
    }

    public void onBranchSearchResult(final BranchSearchResult branchSearchResult) {
        contentView.updateSearchResults(branchSearchResult);
    }

    public void setEmptyView (View emptyView) {
        contentView.setEmptyView(emptyView);
    }
}

