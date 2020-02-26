package com.cordova.plugin.android.biometricauth;

import static java.lang.String.format;

import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

public enum Error {
    CANCELLED(-2, "Cancelled"),
    EDITING_FAILED(-3, "Could not edit shared preferences"),
    NO_CIPHER(-4, "No cipher available"),
    NO_SECRET_KEY(-5, "Secret Key not set."),
    NO_HARDWARE(-6, "Biometry is not available on this device."),
    NO_FINGERPRINT_ENROLLED(-7, "No fingers are enrolled with Touch ID."),
    OTHER(-9, "Unknown error");

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
        try {
            return new PluginResult(PluginResult.Status.ERROR, toJSONObject(message));
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
        }
    }

    public PluginResult toPluginResult(String customErrorMessage) {
        try {
            String errorMessage = format("%s: %s", message, customErrorMessage);
            return new PluginResult(PluginResult.Status.ERROR, toJSONObject(errorMessage));
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
        }
    }

    private JSONObject toJSONObject(String errorMessage) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(OS_KEY, OS_VALUE);
        json.put(ERROR_CODE_KEY, String.valueOf(code));
        json.put(ERROR_MESSAGE_KEY, errorMessage);
        return json;
    }
}
