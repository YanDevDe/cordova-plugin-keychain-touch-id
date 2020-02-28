package com.cordova.plugin.android.biometricauth;

import android.content.res.Resources;

import org.apache.cordova.CordovaInterface;

public final class BiometricResources {

    private BiometricResources() {
    }

    public static String getCancelText(CordovaInterface cordova) {
        return BiometricResources.getText("cancel", cordova);
    }

    public static String getTitleText(CordovaInterface cordova) {
        return BiometricResources.getText("fingerprint_auth_dialog_title", cordova);
    }

    private static String getText(String key, CordovaInterface cordova) {
        Resources resources = cordova.getActivity().getResources();
        return resources.getString(resources.getIdentifier(key, "string", BiometricResources.getPackageName(cordova)));
    }

    private static String getPackageName(CordovaInterface cordova) {
        return cordova.getActivity().getApplicationContext().getPackageName();
    }

}
