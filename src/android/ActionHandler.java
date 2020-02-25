package com.cordova.plugin.android.biometricauth;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;

public interface ActionHandler {

    void handle(JSONArray args, CallbackContext callbackContext, CordovaInterface cordova, BiometricContext biometricContext) throws JSONException;

}
