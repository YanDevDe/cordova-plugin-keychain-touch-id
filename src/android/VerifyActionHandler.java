package com.cordova.plugin.android.biometricauth;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricPrompt;
import android.os.CancellationSignal;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresPermission;
import android.util.Base64;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import ch.viac.vorsorge3a.dev.R;

import static com.cordova.plugin.android.biometricauth.Preferences.getEncodedPasswordKey;
import static com.cordova.plugin.android.biometricauth.Preferences.getInitializationVectorKey;

@TargetApi(29)
public class VerifyActionHandler extends BiometricActionHandler {

    @RequiresPermission(Manifest.permission.USE_BIOMETRIC)
    @Override
    protected void onBiometricsAvailable(JSONArray args, CallbackContext callbackContext, CordovaInterface cordova, BiometricContext biometricContext) throws JSONException {
        String preferencesKey = args.getString(0);
        String titleMessage = args.getString(1);
        SecretKey secretKey;
        try {
            biometricContext.getKeyStore().load(null);
            secretKey = (SecretKey) biometricContext.getKeyStore().getKey(BiometricAuthentication.CLIENT_ID, null);
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
            Log.e(BiometricAuthentication.TAG, "Exception while verifying", e);
            callbackContext.sendPluginResult(Error.NO_SECRET_KEY.toPluginResult());
            return;
        }
        SharedPreferences sharedPreferences;
        Cipher cipher;
        try {
            sharedPreferences = cordova.getActivity().getApplicationContext().getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
            cipher = getCipher(preferencesKey, secretKey, sharedPreferences);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            Log.e(BiometricAuthentication.TAG, "Exception while verifying", e);
            callbackContext.error(e.getMessage());
            return;
        }
        showBiometricPrompt(callbackContext, cordova, preferencesKey, titleMessage, sharedPreferences, cipher);
    }

    @RequiresPermission(Manifest.permission.USE_BIOMETRIC)
    private void showBiometricPrompt(CallbackContext callbackContext, CordovaInterface cordova, String preferencesKey, String titleMessage, SharedPreferences sharedPreferences, Cipher cipher) {
        BiometricPrompt biometricPrompt = buildBiometricPrompt(callbackContext, cordova, titleMessage);
        biometricPrompt.authenticate(
                new BiometricPrompt.CryptoObject(cipher),
                new CancellationSignal(),
                getExecutor(cordova),
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        decryptPassword(result, sharedPreferences, preferencesKey, callbackContext);
                    }
                }
        );
    }

    private void decryptPassword(BiometricPrompt.AuthenticationResult result, SharedPreferences sharedPreferences, String preferencesKey, CallbackContext callbackContext) {
        byte[] encryptedPassword = Base64.decode(sharedPreferences.getString(getEncodedPasswordKey(preferencesKey), ""), Base64.DEFAULT);
        byte[] decryptedPassword;
        try {
            decryptedPassword = result.getCryptoObject().getCipher().doFinal(encryptedPassword);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            Log.e(BiometricAuthentication.TAG, "Exception while verifying", e);
            callbackContext.error(e.getMessage());
            return;
        }
        callbackContext.success(new String(decryptedPassword));
    }

    private BiometricPrompt buildBiometricPrompt(CallbackContext callbackContext, CordovaInterface cordova, String message) {
        DialogInterface.OnClickListener onClickCancel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callbackContext.error("Cancelled");
            }
        };
        return new BiometricPrompt.Builder(cordova.getContext())
                .setNegativeButton(cordova.getActivity().getResources().getString(R.string.cancel), getExecutor(cordova), onClickCancel)
                .setTitle(cordova.getActivity().getResources().getString(R.string.fingerprint_auth_dialog_title))
                .setSubtitle(message)
                .build();
    }

    private Cipher getCipher(String preferencesKey, SecretKey secretKey, SharedPreferences sharedPreferences) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] bytes = Base64.decode(sharedPreferences.getString(getInitializationVectorKey(preferencesKey), ""), Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance(String.format("%s/%s/%s", KeyProperties.KEY_ALGORITHM_AES, KeyProperties.BLOCK_MODE_CBC, KeyProperties.ENCRYPTION_PADDING_PKCS7));
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(bytes));
        return cipher;
    }

    private Executor getExecutor(CordovaInterface cordova) {
        return cordova.getActivity().getMainExecutor();
    }

}
