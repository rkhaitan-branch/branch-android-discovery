package io.branch.search;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

/**
 * SearchRequest Tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchSearchRequestTest {

    @Test
    public void testRequestCreation() throws Throwable {
        BranchSearchRequest requestIn = BranchSearchRequest.Create("餐厅");

        requestIn.setLatitude(10);
        requestIn.setLongitude(20);
        requestIn.setMaxAppResults(100);
        requestIn.setMaxContentPerAppResults(200);
        requestIn.disableQueryModification();
        requestIn.setQuerySource(BranchQuerySource.QUERY_HINT_RESULTS);

        BranchConfiguration config = new BranchConfiguration();

        config.setBranchKey("123");
        config.setCountryCode("ZZ");
        config.setGoogleAdID("XYZ");

        JSONObject jsonIn = BranchSearchInterface.createPayload(requestIn, config);
        Log.d("Branch", "SearchRequest::testRequestCreation(): " + jsonIn.toString());

        Assert.assertEquals(100,
                jsonIn.getInt(BranchSearchRequest.JSONKey.LimitAppResults.toString()));
        Assert.assertEquals(200,
                jsonIn.getInt(BranchSearchRequest.JSONKey.LimitLinkResults.toString()));
        Assert.assertTrue(jsonIn.getBoolean(BranchSearchRequest.JSONKey.DoNotModify.toString()));
        Assert.assertEquals(BranchQuerySource.QUERY_HINT_RESULTS.toString(),
                jsonIn.getString(BranchSearchRequest.JSONKey.QuerySource.toString()));

        Assert.assertEquals(10, jsonIn.getInt(BranchDiscoveryRequest.JSONKey.Latitude.toString()));
        Assert.assertEquals(20, jsonIn.getInt(BranchDiscoveryRequest.JSONKey.Longitude.toString()));

        Assert.assertEquals("123", jsonIn.getString(BranchConfiguration.JSONKey.BranchKey.toString()));
        Assert.assertEquals("ZZ", jsonIn.getString(BranchConfiguration.JSONKey.Country.toString()));
        Assert.assertEquals("XYZ", jsonIn.getString(BranchConfiguration.JSONKey.GAID.toString()));
        Assert.assertFalse(TextUtils.isEmpty(jsonIn.getString(BranchConfiguration.JSONKey.Locale.toString())));

        Assert.assertEquals("ANDROID", jsonIn.getString(BranchDeviceInfo.JSONKey.OS.toString()));
    }

    @Test
    public void testHasDeviceInfo() throws Throwable {
        BranchSearchRequest request = BranchSearchRequest.Create("MOD Pizza");
        JSONObject jsonOut = BranchSearchInterface.createPayload(request, new BranchConfiguration());

        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Brand.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Model.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.OSVersion.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Carrier.toString()));
    }

    @Test
    public void testHasQueryModificationFlag() throws Throwable {
        final String MODIFY_KEY = "do_not_modify";

        BranchSearchRequest request = BranchSearchRequest.Create("MOD Pizza");
        JSONObject jsonOut = BranchSearchInterface.createPayload(request, new BranchConfiguration());

        // Per Spec:  The field should not be sent if false.
        try {
            // In fact, we fully expect this to throw as the key should not exist.
            boolean flag = jsonOut.getBoolean(MODIFY_KEY);
            Assert.assertFalse(flag);
            Assert.fail();
        } catch(JSONException e) {
            Assert.assertFalse(jsonOut.optBoolean(MODIFY_KEY));
        }

        // "Disable" query modifications.  The key should now exist.
        request.disableQueryModification();
        jsonOut = BranchSearchInterface.createPayload(request, new BranchConfiguration());
        Assert.assertTrue(jsonOut.getBoolean(MODIFY_KEY));
    }

    @Test
    public void testOverrideLocale() {
        String testLocale = "xx_YY";

        BranchSearchRequest request = BranchSearchRequest.Create("MOD Pizza");
        BranchConfiguration config = new BranchConfiguration();

        JSONObject jsonObject = BranchSearchInterface.createPayload(request, config);

        String localeString = jsonObject.optString(BranchDeviceInfo.JSONKey.Locale.toString());
        Assert.assertFalse(TextUtils.isEmpty(localeString));
        Assert.assertNotSame(testLocale, localeString);

        config.setLocale(new Locale("xx_YY"));

        jsonObject = BranchSearchInterface.createPayload(request, config);
        Assert.assertEquals(testLocale.toLowerCase(), jsonObject.optString(BranchDeviceInfo.JSONKey.Locale.toString().toLowerCase()));
    }
}
