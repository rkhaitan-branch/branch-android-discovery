package io.branch.search.demo.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by sojanpr on 9/20/17.
 * <p>
 * Class for finding the last known location. Handles permission related to location
 * </p>
 */

public class BranchLocationFinder {
    private static BranchLocationFinder instance;
    private static Location lastKnownLocation = null;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            lastKnownLocation = locationResult.getLastLocation();
        }
    };

    private BranchLocationFinder() {

    }

    public static BranchLocationFinder initialize(Activity activity, ILocationFinderEvents callback) {
        if (instance == null) {
            instance = new BranchLocationFinder();
        }
        if (!isPermissionAvailable(activity)) {
            if (callback != null) {
                callback.onRequestLocationPermission();
            }
        } else {
            instance.requestLocationUpdates(activity);
        }
        return instance;
    }

    public static BranchLocationFinder getInstance() {
        return instance;
    }

    private void requestLocationUpdates(final Activity activity) {
        LocationServices.getSettingsClient(activity).checkLocationSettings(
                new LocationSettingsRequest.Builder().addLocationRequest(
                        new LocationRequest().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)).build())
                .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        subscribeLocationChange(activity);
                    }
                });
    }

    public void onLocationPermissionGranted(Activity activity) {
        requestLocationUpdates(activity);
    }

    public static Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    private static boolean isPermissionAvailable(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void dispose() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    private void subscribeLocationChange(Activity activity) {
        dispose();
        final LocationRequest locationRequest = new LocationRequest().setInterval(1000 * 60 * 2).setMaxWaitTime(2000);
        if (isPermissionAvailable(activity)) {
            try {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, activity.getMainLooper());
            } catch (SecurityException ignore) {
            }
        }
    }

    public interface ILocationFinderEvents {
        void onRequestLocationPermission();
    }

}
