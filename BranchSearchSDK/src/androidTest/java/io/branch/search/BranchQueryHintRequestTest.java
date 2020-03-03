package io.branch.search;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * QueryHint Request Tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchQueryHintRequestTest {
    @Test
    public void testRequestCreation() throws Throwable {
        BranchQueryHintRequest requestIn = BranchQueryHintRequest.Create();
        requestIn.setLatitude(10);
        requestIn.setLongitude(20);

        BranchConfiguration config = new BranchConfiguration();
        config.setBranchKey("key_live_123"); // need a "valid" key
        config.setCountryCode("ZZ");
        config.setGoogleAdID("XYZ");

        BranchDeviceInfo info = new BranchDeviceInfo();
        JSONObject jsonIn = BranchSearchInterface.createPayload(requestIn, config, info);

        Log.d("Branch", "QueryHint::testRequestCreation(): " + jsonIn.toString());

        Assert.assertEquals(jsonIn.getInt(BranchDiscoveryRequest.JSONKey.Latitude.toString()), 10);
        Assert.assertEquals(jsonIn.getInt(BranchDiscoveryRequest.JSONKey.Longitude.toString()), 20);

        Assert.assertEquals(jsonIn.getString(BranchConfiguration.JSONKey.BranchKey.toString()), "key_live_123");
        Assert.assertEquals(jsonIn.getString(BranchConfiguration.JSONKey.Country.toString()), "ZZ");
        Assert.assertEquals(jsonIn.getString(BranchConfiguration.JSONKey.GAID.toString()), "XYZ");

        Assert.assertEquals(jsonIn.getString(BranchDeviceInfo.JSONKey.OS.toString()), "ANDROID");
    }

    @Test
    public void testHasDeviceInfo() throws Throwable {
        BranchQueryHintRequest request = BranchQueryHintRequest.Create();

        JSONObject jsonOut = BranchSearchInterface.createPayload(request,
                new BranchConfiguration(), new BranchDeviceInfo());

        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Brand.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Model.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.OSVersion.toString()));
        Assert.assertNotNull(jsonOut.getString(BranchDeviceInfo.JSONKey.Carrier.toString()));
    }

    @Test
    public void testQueryHint() {

    }
}
