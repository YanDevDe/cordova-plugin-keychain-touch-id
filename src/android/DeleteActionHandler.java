package com.cordova.plugin.android.biometricauth;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class DeleteActionHandler implements ActionHandler {

    @Override
    public void handle(JSONArray args, CallbackContext callbackContext, CordovaInterface cordova, BiometricContext biometricContext) throws JSONException {
        String key = args.getString(0);
        boolean removed = removeSharedPreferences(cordova, key);
        PluginResult pluginResult = removed ? new PluginResult(PluginResult.Status.OK) : Error.EDITING_FAILED.toPluginResult();
        callbackContext.sendPluginResult(pluginResult);
    }

    private boolean removeSharedPreferences(CordovaInterface cordova, String key) {
        SharedPreferences sharedPreferences = cordova.getActivity().getApplicationContext().getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.edit()
                .remove(Preferences.getEncodedPasswordKey(key))
                .remove(Preferences.getInitializationVectorKey(key))
                .commit();
    }

}
