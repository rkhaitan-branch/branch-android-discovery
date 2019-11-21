package io.branch.search;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request model for Branch Discovery.
 */
public class BranchDiscoveryRequest<T extends BranchDiscoveryRequest> {

    enum JSONKey {
        Latitude("user_latitude"),
        Longitude("user_longitude"),
        Timestamp("utc_timestamp");

        JSONKey(String key) {
            _key = key;
        }

        @Override
        public String toString() {
            return _key;
        }

        private String _key;
    }

    // Latitude of the user
    private double user_latitude;

    // Longitude for the user
    private double user_longitude;

    /**
     * Private Constructor.
     */
    BranchDiscoveryRequest() {
    }

    /**
     * Set the current location.
     * Branch Search needs location permission for better search experience.  Call this method when location updates happen.
     * @param location Location
     * @return this BranchDiscoveryRequest
     */
    public T setLocation(Location location) {
        if (location != null) {
            setLatitude(location.getLatitude());
            setLongitude(location.getLongitude());
        }
        return (T)this;
    }

    /**
     * Set the current location - latitude.
     * @param latitude latitude
     * @return this BranchDiscoveryRequest
     */
    public T setLatitude(double latitude) {
        this.user_latitude = latitude;
        return (T)this;
    }

    /**
     * Set the current location - longitude.
     * @param longitude latitude
     * @return this BranchDiscoveryRequest
     */
    public T setLongitude(double longitude) {
        this.user_longitude = longitude;
        return (T)this;
    }

    JSONObject convertToJson(JSONObject jsonObject) {
        try {
            jsonObject.putOpt(JSONKey.Latitude.toString(), user_latitude);
            jsonObject.putOpt(JSONKey.Longitude.toString(), user_longitude);

            // Add the current timestamp.
            Long tsLong = System.currentTimeMillis();
            jsonObject.putOpt(JSONKey.Timestamp.toString(), tsLong);


        } catch (JSONException ignore) {
        }
        return jsonObject;
    }
}
