package com.cordova.plugin.android.biometricauth;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;

import android.hardware.biometrics.BiometricManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

@TargetApi(29)
public abstract class BiometricActionHandler implements ActionHandler {

    @Override
    public final void handle(JSONArray args, CallbackContext callbackContext, CordovaInterface cordova, BiometricContext biometricContext) throws JSONException {
        if (!canUseBiometrics(cordova)) {
            callbackContext.sendPluginResult(Error.NO_HARDWARE.toPluginResult());
            return;
        }

        switch (biometricContext.getBiometricManager().canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                onBiometricsAvailable(args, callbackContext, cordova, biometricContext);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                callbackContext.sendPluginResult(Error.NO_FINGERPRINT_ENROLLED.toPluginResult());
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            default:
                callbackContext.sendPluginResult(Error.NO_HARDWARE.toPluginResult());
                break;
        }
    }

    protected abstract void onBiometricsAvailable(JSONArray args, CallbackContext callbackContext, CordovaInterface cordova, BiometricContext biometricContext) throws JSONException;

    private boolean canUseBiometrics(CordovaInterface cordova) {
        return isFingerprintPermissionGranted(cordova) || isBiometricPermissionGranted(cordova);
    }

    private boolean isFingerprintPermissionGranted(CordovaInterface cordova) {
        return isPermissionGranted(cordova, Manifest.permission.USE_FINGERPRINT);
    }

    private boolean isBiometricPermissionGranted(CordovaInterface cordova) {
        return Build.VERSION.SDK_INT >= 28 && isPermissionGranted(cordova, Manifest.permission.USE_BIOMETRIC);
    }

    private boolean isPermissionGranted(CordovaInterface cordova, String permission) {
        return cordova.getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

}
