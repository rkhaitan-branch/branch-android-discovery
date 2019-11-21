package io.branch.search.demo.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DimensionMatrix {
    public static int APP_SMALL_ICON_SIZE;
    public static int SEARCH_ICON_SMALL_SIZE;
    public static int HEADER_VIEW_TOP_MARGIN;
    public static int EXPANDED_APP_VIEW_BOTTOM_PADDING;

    public static int APPS_VIEW_SIDE_PADDING;
    public static int APPS_VIEW_TO_PADDING;

    public static int ACTION_VIEW_SIDE_PADDING;
    public static int ACTION_VIEW_TOP_PADDING;

    public static int APPS_TITLE_BOTTOM_MARGIN = 30;


    private static DisplayMetrics displayMetrics;

    public static int convertDpToPixel(Context context, float dpVal) {
        if (displayMetrics == null) {
            displayMetrics = context.getResources().getDisplayMetrics();
        }
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, displayMetrics);
    }

    public static void init(Context context) {
        displayMetrics = context.getResources().getDisplayMetrics();
        APP_SMALL_ICON_SIZE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, displayMetrics);
        SEARCH_ICON_SMALL_SIZE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displayMetrics);
        HEADER_VIEW_TOP_MARGIN = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, displayMetrics);
        EXPANDED_APP_VIEW_BOTTOM_PADDING = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, displayMetrics);

        APPS_VIEW_SIDE_PADDING = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, displayMetrics);
        APPS_VIEW_TO_PADDING = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, displayMetrics);

        ACTION_VIEW_SIDE_PADDING = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, displayMetrics);
        ACTION_VIEW_TOP_PADDING = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, displayMetrics);

        APPS_TITLE_BOTTOM_MARGIN = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, displayMetrics);
    }


}
