package io.branch.search;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * BranchSearch class tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchSearchTest extends BranchTest {

    @Test
    public void testAppVersion() {
        Assert.assertNotNull(BranchSearch.getVersion());
    }

    @Test
    public void testInit_manifestKey() throws Throwable {
        initBranch();
    }

    @Test
    public void testInit_twice() throws Throwable {
        initBranch();
        BranchSearch instance1 = BranchSearch.getInstance();
        Assert.assertNotNull(instance1);

        initBranch();
        BranchSearch instance2 = BranchSearch.getInstance();
        Assert.assertNotNull(instance2);

        // Init twice, two different instances
        Assert.assertNotSame(instance1, instance2);
    }

    @Test
    public void testInit_customConfig_1() throws Throwable {
        // Test that an empty configuration will still initialize correctly.
        BranchConfiguration config = new BranchConfiguration();
        initBranch(config);
        Assert.assertNotNull(BranchSearch.getInstance());
    }

    @Test
    public void testInit_customConfig_2() throws Throwable {
        // Test that an obviously bad key will default to a good one.
        String invalidKey = "ABC.DEF";

        BranchConfiguration config = new BranchConfiguration();
        config.setBranchKey(invalidKey);

        initBranch(config);
        Assert.assertNotNull(BranchSearch.getInstance());

        Assert.assertNotSame(invalidKey, config.getBranchKey());
        Assert.assertTrue(config.getBranchKey().startsWith("key_live"));
    }

    @Test
    public void testInit_customConfig_3() throws Throwable {
        // Test that a key that isn't the one in the manifest is retained after initialization.
        String alternateKey = "key_live_ABC.DEF";

        BranchConfiguration config = new BranchConfiguration();
        config.setBranchKey(alternateKey);

        initBranch(config);
        Assert.assertNotNull(BranchSearch.getInstance());

        Assert.assertSame(alternateKey, config.getBranchKey());
    }

    @Test
    public void testServiceEnabled_withoutInitialization() throws Throwable {
        // This API should work without initialization
        final CountDownLatch latch = new CountDownLatch(1);
        BranchSearch.isServiceEnabled("someKey", new IBranchServiceEnabledEvents() {
            @Override
            public void onBranchServiceEnabledResult(@NonNull BranchServiceEnabledResult result) {
                latch.countDown();
            }
        });
        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testServiceEnabled_witInitialization() throws Throwable {
        // This API should also work with initialization
        initBranch();
        final CountDownLatch latch = new CountDownLatch(1);
        BranchSearch.isServiceEnabled("someKey", new IBranchServiceEnabledEvents() {
            @Override
            public void onBranchServiceEnabledResult(@NonNull BranchServiceEnabledResult result) {
                latch.countDown();
            }
        });
        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testServiceEnabled_fromManifest() throws Throwable {
        // The androidTest manifest defines a key so we should be able to use this API.
        final CountDownLatch latch = new CountDownLatch(1);
        BranchSearch.isServiceEnabled(getTestContext(), new IBranchServiceEnabledEvents() {
            @Override
            public void onBranchServiceEnabledResult(@NonNull BranchServiceEnabledResult result) {
                latch.countDown();
            }
        });
        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}
