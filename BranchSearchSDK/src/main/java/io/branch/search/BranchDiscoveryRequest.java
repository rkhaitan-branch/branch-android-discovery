package io.branch.search;

import android.location.Location;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Request model for Branch Discovery.
 */
@SuppressWarnings("unchecked")
public class BranchDiscoveryRequest<T extends BranchDiscoveryRequest> {

    enum JSONKey {
        Latitude("user_latitude"),
        Longitude("user_longitude"),
        Timestamp("utc_timestamp"),
        Extra("extra_data");

        JSONKey(String key) {
            _key = key;
        }

        @NonNull
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

    private Map<String, Object> extra_data = new HashMap<>();

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
        return (T) this;
    }

    /**
     * Set the current location - latitude.
     * @param latitude latitude
     * @return this BranchDiscoveryRequest
     */
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public T setLatitude(double latitude) {
        this.user_latitude = latitude;
        return (T) this;
    }

    /**
     * Set the current location - longitude.
     * @param longitude latitude
     * @return this BranchDiscoveryRequest
     */
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public T setLongitude(double longitude) {
        this.user_longitude = longitude;
        return (T) this;
    }

    /**
     * Adds extra data to be passed to server in forms
     * of a key-value pair.
     * @param key a key
     * @param data value
     * @return this BranchDiscoveryRequest
     */
    public T addExtra(@NonNull String key, @NonNull Object data) {
        this.extra_data.put(key, data);
        return (T) this;
    }

    JSONObject convertToJson(JSONObject jsonObject) {
        try {
            jsonObject.putOpt(JSONKey.Latitude.toString(), user_latitude);
            jsonObject.putOpt(JSONKey.Longitude.toString(), user_longitude);

            // Add the current timestamp.
            Long tsLong = System.currentTimeMillis();
            jsonObject.putOpt(JSONKey.Timestamp.toString(), tsLong);

            // Add extra data.
            if (!extra_data.keySet().isEmpty()) {
                JSONObject extraData = new JSONObject();
                for (String key : extra_data.keySet()) {
                    Object value = extra_data.get(key);
                    extraData.putOpt(key, value);
                }
                jsonObject.putOpt(JSONKey.Extra.toString(), extraData);
            }


        } catch (JSONException ignore) {
        }
        return jsonObject;
    }
}
