package io.branch.search;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.branch.search.util.AssetUtils;

/**
 * QueryHint Result Test.
 */
@RunWith(AndroidJUnit4.class)
public class BranchQueryHintResultTest extends BranchTest {
    @Test
    public void testResultSuccess() throws Throwable {
        String response = AssetUtils.readJsonFile(getTestContext(), "success_queryhint.json");
        Assert.assertTrue(response.length() > 0);

        JSONObject jsonResponse = new JSONObject(response);

        BranchQueryResult result = BranchQueryResult.createFromJson(jsonResponse);
        Assert.assertNotNull(result);

        List<String> hints = result.getQueryResults();
        Assert.assertEquals(3, hints.size());
    }

    @Test
    public void testResultSuccess_empty1() throws Throwable {
        String response = AssetUtils.readJsonFile(getTestContext(), "success_queryhint_empty1.json");
        Assert.assertTrue(response.length() > 0);

        JSONObject jsonResponse = new JSONObject(response);

        BranchQueryResult result = BranchQueryResult.createFromJson(jsonResponse);
        Assert.assertNotNull(result);

        List<String> hints = result.getQueryResults();
        Assert.assertEquals(0, hints.size());
    }

    @Test
    public void testResultSuccess_empty2() throws Throwable {
        String response = AssetUtils.readJsonFile(getTestContext(), "success_queryhint_empty2.json");
        Assert.assertTrue(response.length() > 0);

        JSONObject jsonResponse = new JSONObject(response);

        BranchQueryResult result = BranchQueryResult.createFromJson(jsonResponse);
        Assert.assertNotNull(result);

        List<String> hints = result.getQueryResults();
        Assert.assertEquals(0, hints.size());
    }

    @Test
    public void testResultError() throws Throwable {
        String response = AssetUtils.readJsonFile(getTestContext(), "err_queryhint.json");
        Assert.assertTrue(response.length() > 0);

        JSONObject jsonObject = new JSONObject(response);

        BranchSearchError error = new BranchSearchError(jsonObject);
        Assert.assertEquals(error.getErrorCode(), BranchSearchError.ERR_CODE.BAD_REQUEST_ERR);
    }
}
