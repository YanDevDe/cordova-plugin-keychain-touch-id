package com.cordova.plugin.android.biometricauth;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class HasActionHandler implements ActionHandler {

    @Override
    public void handle(JSONArray args, CallbackContext callbackContext, CordovaInterface cordova, BiometricContext biometricContext) throws JSONException {
        String key = args.getString(0);
        migrateExistingPreferences(cordova, key);
        boolean hasEncodedPassword = hasEncodedPassword(cordova, key);
        PluginResult.Status status = hasEncodedPassword ? PluginResult.Status.OK : PluginResult.Status.ERROR;
        callbackContext.sendPluginResult(new PluginResult(status));
    }

    private void migrateExistingPreferences(CordovaInterface cordova, String key) {
        SharedPreferences oldSharedPreferences = cordova.getActivity().getApplicationContext().getSharedPreferences(Preferences.OLD_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String encodedPasswordKey = Preferences.getEncodedPasswordKey(key);
        String encodedPassword = oldSharedPreferences.getString(encodedPasswordKey, "");

        if (isNullOrEmpty(encodedPassword)) {
            return;
        }

        SharedPreferences newSharedPreferences = cordova.getActivity().getApplicationContext().getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String initializationVectorKey = Preferences.getInitializationVectorKey(key);
        newSharedPreferences.edit()
                .putString(encodedPasswordKey, oldSharedPreferences.getString(encodedPasswordKey, ""))
                .putString(initializationVectorKey, oldSharedPreferences.getString(initializationVectorKey, ""))
                .commit();
        oldSharedPreferences.edit()
                .remove(encodedPasswordKey)
                .remove(initializationVectorKey)
                .commit();
    }

    private boolean hasEncodedPassword(CordovaInterface cordova, String key) {
        SharedPreferences sharedPref = cordova.getActivity().getApplicationContext().getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String encodedPassword = sharedPref.getString(Preferences.getEncodedPasswordKey(key), "");
        return !isNullOrEmpty(encodedPassword);
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

}
