package com.cordova.plugin.android.biometricauth;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;

@TargetApi(29)
public class IsAvailableActionHandler extends BiometricActionHandler {

    private static final String FACE_RECOGNITION_RESPONSE = "face";
    private static final String FINGERPRINT_RESPONSE = "touch";

    @Override
    protected void onBiometricsAvailable(JSONArray args, CallbackContext callbackContext, CordovaInterface cordova, BiometricContext biometricContext) {
        if (hasFeature(cordova, PackageManager.FEATURE_FACE)) {
            callbackContext.success(FACE_RECOGNITION_RESPONSE);
        } else if (hasFeature(cordova, PackageManager.FEATURE_FINGERPRINT)) {
            callbackContext.success(FINGERPRINT_RESPONSE);
        } else {
            callbackContext.success();
        }
    }

    private boolean hasFeature(CordovaInterface cordova, String feature) {
        return cordova.getContext().getPackageManager().hasSystemFeature(feature);
    }

}
