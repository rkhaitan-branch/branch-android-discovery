package io.branch.search.demo.util;

import io.branch.search.BranchLinkResult;

// Normalize Review Data
public class ContentDisplayReviewHelper {
    // If the review range is missing from the data, the default is 5
    private static final int RATING_SCALE = 5;

    private boolean ratingAvailable;
    private double rating;

    private boolean reviewAvailable;
    private int reviewCount;

    public ContentDisplayReviewHelper(BranchLinkResult data) {
        int ratingScale = 0;

        // First check to see if there is a rating available at all
        try {
            rating = data.getMetadata().getDouble("rating");
            ratingAvailable = true;
        } catch (Exception e) {
            ratingAvailable = false;
            // Log.d("BRANCH", "No rating available");
        }

        if (ratingAvailable) {
            try {
                ratingScale = data.getMetadata().getInt("rating_scale");
                if (ratingScale > 0 && ratingScale < RATING_SCALE) {
                    // Never scale up.
                    ratingScale = 0;
                    ratingAvailable = false;
                }
            } catch (Exception e) {
                ratingScale = RATING_SCALE;
            }
        }

        // Scale if needed
        if (ratingAvailable) {
            if (ratingScale > RATING_SCALE) {
                // Scale Down
                rating = ((rating * RATING_SCALE) / ratingScale);    // ratingScale should never be zero here.
            }

            // Log.d("BRANCH", "RATING: " + (float)rating + " OUT OF " + RATING_SCALE);
        }

        // If there are ratings available, try to get the review count.
        try {
            reviewCount = data.getMetadata().getInt("review_count");
            reviewAvailable = (reviewCount > 0);
        } catch (Exception e) {
            // Any exceptions here makes rating count not available.
            // Log.d("BRANCH", "NO REVIEW COUNT");
        }
    }

    public boolean isRatingAvailable() {
        return ratingAvailable;
    }

    public boolean isReviewAvailable() {
        return reviewAvailable;
    }

    public double getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public int getRatingScale() {
        return RATING_SCALE;
    }
}
