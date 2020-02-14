package com.cordova.plugin.android.biometricauth;

import android.annotation.TargetApi;
import android.security.keystore.KeyProperties;

import android.hardware.biometrics.BiometricManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.KeyGenerator;

@TargetApi(29)
public class BiometricAuthentication {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    public static final String CLIENT_ID = "CordovaTouchPlugin";
    public static final String TAG = BiometricAuthentication.class.getSimpleName();

    private BiometricContext biometricContext;

    public void initialize(CordovaInterface cordova) {
        BiometricManager biometricManager = cordova.getContext().getSystemService(BiometricManager.class);
        KeyGenerator keyGenerator;
        KeyStore keyStore;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }
        biometricContext = new BiometricContext(biometricManager, keyStore, keyGenerator);
    }

    public boolean execute(CordovaInterface cordova, String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Action pluginAction = Action.getOrNull(action);
        if (pluginAction == null) {
            return false;
        }
        pluginAction.getHandler().handle(args, callbackContext, cordova, biometricContext);
        return true;
    }

}
