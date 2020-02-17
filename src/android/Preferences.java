package com.cordova.plugin.android.biometricauth;

public final class Preferences {

    private Preferences() {
    }

    public static final String OLD_SHARED_PREFERENCE_NAME = "MainActivity";

    public static final String SHARED_PREFERENCE_NAME = "FingerSPref";

    public static String getEncodedPasswordKey(String key) {
        return "fing" + key;
    }

    public static String getInitializationVectorKey(String key) {
        return "fing_iv" + key;
    }

}
