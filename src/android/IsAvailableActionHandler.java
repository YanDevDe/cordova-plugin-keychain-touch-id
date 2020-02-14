package com.cordova.plugin.android.biometricauth;

import android.annotation.TargetApi;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

@TargetApi(29)
public class IsAvailableActionHandler extends BiometricActionHandler {

    @Override
    protected void onBiometricsAvailable(JSONArray args, CallbackContext callbackContext, CordovaInterface cordova, BiometricContext biometricContext) {
        callbackContext.success();
    }

}
