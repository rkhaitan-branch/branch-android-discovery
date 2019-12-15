package io.branch.search;

import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * BranchConfiguration class tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchConfigurationTest extends BranchTest {

    @Test
    public void testDefaultInit() {
        BranchConfiguration config = new BranchConfiguration();

        // Before Initialization, everything should be null
        Assert.assertNull(config.getBranchKey());
        Assert.assertNull(config.getCountryCode());
        Assert.assertNull(config.getUrl());

        config.setDefaults(getTestContext());

        // After Initialization, everything should be set
        Assert.assertNotNull(config.getBranchKey());
        Assert.assertNotNull(config.getCountryCode());
        Assert.assertNotNull(config.getUrl());
    }

    @Test
    public void testCountryCode() {
        String expected = "EN-AU";
        BranchConfiguration config = new BranchConfiguration();
        config.setCountryCode(expected);
        Assert.assertEquals(expected, config.getCountryCode());
    }

    @Test
    public void testConfigurationFlags() {
        BranchConfiguration config = new BranchConfiguration();
        Assert.assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, config.getLaunchIntentFlags());

        config.setLaunchIntentFlags(0);
        Assert.assertEquals(0, config.getLaunchIntentFlags());

        int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP;
        config.setLaunchIntentFlags(flags);
        Assert.assertEquals(flags, config.getLaunchIntentFlags());
    }

    @Test
    public void testRequestExtras() {
        BranchConfiguration config = new BranchConfiguration();
        config.addRequestExtra("foo", "bar");
        JSONObject object = new JSONObject();
        config.addConfigurationInfo(object);
        JSONObject extra = object.optJSONObject(BranchConfiguration.JSONKey.RequestExtra.toString());
        Assert.assertNotNull(extra);
        String fooValue = extra.optString("foo");
        Assert.assertEquals("bar", fooValue);
    }
}
