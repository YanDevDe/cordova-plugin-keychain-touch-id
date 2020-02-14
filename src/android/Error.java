package com.cordova.plugin.android.biometricauth;

import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

public enum Error {
    NO_SECRET_KEY(-5, "Secret Key not set."),
    NO_HARDWARE(-6, "Biometry is not available on this device."),
    NO_FINGERPRINT_ENROLLED(-7, "No fingers are enrolled with Touch ID.");

    private static final String OS_KEY = "OS";
    private static final String OS_VALUE = "Android";
    private static final String ERROR_CODE_KEY = "ErrorCode";
    private static final String ERROR_MESSAGE_KEY = "ErrorMessage";

    private final int code;
    private final String message;

    Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public PluginResult toPluginResult() {
        String message;
        try {
            message = toJSONObject().toString();
        } catch (JSONException e) {
            message = e.getMessage();
        }
        return new PluginResult(PluginResult.Status.ERROR, message);
    }

    private JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(OS_KEY, OS_VALUE);
        json.put(ERROR_CODE_KEY, String.valueOf(code));
        json.put(ERROR_MESSAGE_KEY, message);
        return json;
    }
}
