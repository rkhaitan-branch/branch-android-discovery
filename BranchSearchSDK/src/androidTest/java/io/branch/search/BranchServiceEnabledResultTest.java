package io.branch.search;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.branch.search.util.AssetUtils;

/**
 * {@link BranchServiceEnabledResult} tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchServiceEnabledResultTest extends BranchTest {

    @Test
    public void testFromError() {
        // An error means 'true' response.
        BranchSearchError error = new BranchSearchError(BranchSearchError.ERR_CODE.BAD_REQUEST_ERR);
        BranchServiceEnabledResult result = BranchServiceEnabledResult.createFromError(error);
        Assert.assertTrue(result.isEnabled());
    }

    @Test
    public void testFromJson_noKey() throws Throwable {
        String response = AssetUtils.readJsonFile(getTestContext(), "serviceenabled_nokey.json");
        Assert.assertTrue(response.length() > 0);

        JSONObject jsonResponse = new JSONObject(response);

        // The absence of the disabled key should mean 'true' response.
        BranchServiceEnabledResult result = BranchServiceEnabledResult.createFromJson(jsonResponse);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEnabled());
    }

    @Test
    public void testFromJson_enabled() throws Throwable {
        String response = AssetUtils.readJsonFile(getTestContext(), "serviceenabled_key_enabled.json");
        Assert.assertTrue(response.length() > 0);

        JSONObject jsonResponse = new JSONObject(response);

        // Should return true.
        BranchServiceEnabledResult result = BranchServiceEnabledResult.createFromJson(jsonResponse);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEnabled());
    }

    @Test
    public void testFromJson_disabled() throws Throwable {
        String response = AssetUtils.readJsonFile(getTestContext(), "serviceenabled_key_disabled.json");
        Assert.assertTrue(response.length() > 0);

        JSONObject jsonResponse = new JSONObject(response);

        // Only when explicitly disabled, we return false.
        BranchServiceEnabledResult result = BranchServiceEnabledResult.createFromJson(jsonResponse);
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEnabled());
    }
}
