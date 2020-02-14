package com.cordova.plugin.android.biometricauth;

import android.content.res.Configuration;
import android.content.res.Resources;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Locale;

public class SetLocaleActionHandler implements ActionHandler {

    @Override
    public void handle(JSONArray args, CallbackContext callbackContext, CordovaInterface cordova, BiometricContext biometricContext) throws JSONException {
        String language = args.getString(0);
        Resources resources = cordova.getActivity().getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(language.toLowerCase()));
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        callbackContext.success();
    }

}
