package io.branch.search;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * SearchRequest Tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchHammerTest extends BranchTest {
    private static final String TAG = "Branch::HammerTest";

    private static BranchSearchRequest createTestRequest(String query) {
        BranchSearchRequest request = BranchSearchRequest.Create(query);

        request.setLatitude(19.042813);
        request.setLongitude(72.840779);
        request.setMaxAppResults(100);
        request.setMaxContentPerAppResults(200);
        request.disableQueryModification();

        return request;
    }

    private static BranchConfiguration createTestConfiguration() {
        BranchConfiguration config = new BranchConfiguration();

        config.setBranchKey("key_live_jgKhogSvbj1sZ7PJiQ47dfkozAmfeA4H");
        config.setCountryCode("IN");

        return config;
    }

    class DoQuery extends Thread implements IBranchSearchEvents, IBranchQueryResults {

        final CountDownLatch mLatch;
        BranchSearchRequest mRequest;
        long mStart;
        long mEnd;
        final int mId;
        boolean mSuccess;

        DoQuery(int id, String query, CountDownLatch latch) {
            setName("HammerThread #" + id);
            mId = id;
            mLatch = latch;
            mRequest = createTestRequest(query);
        }

        @Override
        public void run() {
            Log.d(TAG, "Query#:" + mId + " STARTED.");
            mStart = System.currentTimeMillis();
//            BranchSearch.getInstance().query(mRequest, this);
            BranchSearch.getInstance().queryHint( this);
//            BranchSearch.getInstance().autoSuggest( mRequest, this);
        }

        boolean didSucceed() {
            return mSuccess;
        }

        long elapsed() {
            return (mEnd - mStart);
        }

        @Override
        public void onBranchSearchResult(BranchSearchResult result) {
            mEnd = System.currentTimeMillis();
            mSuccess = true;
            Log.d(TAG, "Query#:" + mId + " ENDED. Success:" + mSuccess + " Left:" + mLatch.getCount());
            mLatch.countDown();
        }

        @Override
        public void onBranchSearchError(BranchSearchError error) {
            mEnd = System.currentTimeMillis();
            Log.d(TAG, "Query#:" + mId + " ENDED. Success:" + mSuccess + " Left:" + mLatch.getCount());
            mLatch.countDown();
        }

        @Override
        public void onQueryResult(BranchQueryResult result) {
            mEnd = System.currentTimeMillis();
            mSuccess = true;
            Log.d(TAG, "Query#:" + mId + " ENDED. Success:" + mSuccess + " Left:" + mLatch.getCount());
            mLatch.countDown();
        }

        @Override
        public void onError(BranchSearchError error) {
            mEnd = System.currentTimeMillis();
            Log.d(TAG, "Query#:" + mId + " ENDED. Success:" + mSuccess + " Left:" + mLatch.getCount());
            mLatch.countDown();
        }
    }

    // Do all 3 queries at once
    class DoQuery3 extends DoQuery {
        DoQuery3(int id, String query, CountDownLatch latch) {
            super(id, query, latch);
        }

        @Override
        public void run() {
            Log.d(TAG, "Query#:" + mId + " STARTED.");
            mStart = System.currentTimeMillis();
            BranchSearch.getInstance().query(mRequest, this);
            BranchSearch.getInstance().queryHint( this);
            BranchSearch.getInstance().autoSuggest( mRequest, this);
        }
    }

    @Test
    @UiThread // remove non-ui thread optimizations we have
    public void testConcurrentQueries() throws Throwable {
        final int MAX = 50;
        initializeAndWarmUp();

        CountDownLatch queryLatch = new CountDownLatch(MAX);
        List<DoQuery> queryList = new ArrayList<>();

        for (int i = 0; i < MAX; i++) {
            DoQuery q = new DoQuery(i, "pizza", queryLatch);
            queryList.add(q);
            q.start();
        }

        queryLatch.await(MAX * 2, TimeUnit.SECONDS);
        logStats(queryList);
    }

    // This tries to emulate what a keyboard would do - many requests coming from the same
    // thread, with a delay between each.
    @Test
    @UiThread // remove non-ui thread optimizations we have
    public void testSerialQueries() throws Throwable {
        final int MAX = 50;
        final long KEYBOARD_DELAY = 200L;
        initializeAndWarmUp();

        // Execute MAX serial queries on a background thread.
        final CountDownLatch threadLatch = new CountDownLatch(1);
        final CountDownLatch queryLatch = new CountDownLatch(MAX);
        final List<DoQuery> queryList = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < MAX; i++) {
                    DoQuery q = new DoQuery(i, "pizza", queryLatch);
                    queryList.add(q);
                    q.run(); // Use run instead of start
                    try {
                        Thread.sleep(KEYBOARD_DELAY);
                    } catch (InterruptedException ignore) {}
                }
                try {
                    queryLatch.await();
                } catch (InterruptedException ignore) {}
                threadLatch.countDown();
            }
        }).start();
        threadLatch.await(MAX * 2, TimeUnit.SECONDS);
        logStats(queryList);
    }

    @Test
    @UiThread // remove non-ui thread optimizations we have
    public void testThreeEndpoints() throws Throwable {
        final int MAX = 50;
        initializeAndWarmUp();

        CountDownLatch queryLatch = new CountDownLatch(MAX * 3);
        List<DoQuery> queryList = new ArrayList<>();

        for (int i = 0; i < MAX; i++) {
            DoQuery q = new DoQuery3(i, "pizza", queryLatch);
            queryList.add(q);
            q.start();
        }

        queryLatch.await(MAX * 2, TimeUnit.SECONDS);
        logStats(queryList); // stats do not log totally right for DoQuery3
    }

    private void initializeAndWarmUp() throws Throwable {
        initBranch(createTestConfiguration());
        Thread.sleep(10);

        // Warm-up the threads
        // NOTE: If each requests cancel the previous, doing this is not guaranteed
        // to wake up all of the executor threads.
        final int THREADS = 6;
        CountDownLatch warmUpLatch = new CountDownLatch(THREADS);
        for (int i = 0; i < THREADS; i++) {
            DoQuery q = new DoQuery(-1, "pizza", warmUpLatch);
            q.start();
        }
        if (!warmUpLatch.await(10, TimeUnit.SECONDS)) {
            throw new RuntimeException("Warmup timeout.");
        }
    }

    private void logStats(@NonNull List<DoQuery> queryList) throws Throwable {
        Log.w(TAG, "Completed " + queryList.size() + " queries.");
        int success = 0;
        int failures = 0;
        int timeouts = 0;
        long successLatency = 0;
        long failureLatency = 0;
        for (int i = 0; i < queryList.size(); i++) {
            DoQuery q = queryList.get(i);
            if (q.didSucceed()) {
                success++;
                successLatency += q.elapsed();
            } else if (q.elapsed() > 0) {
                failures++;
                failureLatency += q.elapsed();
            } else {
                timeouts++;
            }
            String elapsed = (q.elapsed() > 0 ? "" + q.elapsed() : "TIMEOUT");
            Log.d(TAG, "Query#: " + i + "\tSuccess: " + q.didSucceed() + "\tElapsed: " + elapsed);
        }
        Thread.sleep(10);

        successLatency = success > 0 ? (long) ((double) successLatency / success) : 0;
        failureLatency = failures > 0 ? (long) ((double) failureLatency / failures) : 0;
        Log.w(TAG, "[final stats] total:" + queryList.size() +
                " success:" + success +
                " failures:" + failures +
                " timeouts:" + timeouts);
        Log.w(TAG, "[final stats] average success latency:" + successLatency);
        Log.w(TAG, "[final stats] average failure latency:" + failureLatency);
    }
}
