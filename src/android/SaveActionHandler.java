package com.cordova.plugin.android.biometricauth;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricPrompt;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
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
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static com.cordova.plugin.android.biometricauth.Preferences.getEncodedPasswordKey;
import static com.cordova.plugin.android.biometricauth.Preferences.getInitializationVectorKey;

@TargetApi(29)
public class SaveActionHandler extends BiometricActionHandler {

    @RequiresPermission(Manifest.permission.USE_BIOMETRIC)
    @Override
    protected void onBiometricsAvailable(JSONArray args, CallbackContext callbackContext, CordovaInterface cordova, BiometricContext biometricContext) throws JSONException {
        String preferencesKey = args.getString(0);
        String password = args.getString(1);
        boolean userAuthenticationRequired = args.getBoolean(2);
        SecretKey secretKey;
        try {
            biometricContext.getKeyStore().load(null);
            secretKey = (SecretKey) biometricContext.getKeyStore().getKey(BiometricAuthentication.CLIENT_ID, null);
            if (secretKey == null) {
                secretKey = generateSecretKey(userAuthenticationRequired, biometricContext);
            }
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | InvalidAlgorithmParameterException e) {
            Log.e(BiometricAuthentication.TAG, "Exception while saving", e);
            callbackContext.sendPluginResult(Error.NO_SECRET_KEY.toPluginResult(e.getMessage()));
            return;
        }
        SharedPreferences sharedPreferences;
        Cipher cipher;
        try {
            sharedPreferences = cordova.getActivity().getApplicationContext().getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
            cipher = getCipher(secretKey);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e(BiometricAuthentication.TAG, "Exception while saving", e);
            callbackContext.sendPluginResult(Error.NO_CIPHER.toPluginResult(e.getMessage()));
            return;
        }
        if (userAuthenticationRequired) {
            showBiometricPrompt(callbackContext, cordova, preferencesKey, password, sharedPreferences, cipher);
        } else {
            encryptPassword(cipher, sharedPreferences, preferencesKey, password, callbackContext);
        }
    }

    private SecretKey generateSecretKey(boolean userAuthenticationRequired, BiometricContext biometricContext) throws CertificateException, NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException {
        biometricContext.getKeyStore().load(null);
        KeyGenParameterSpec spec = new KeyGenParameterSpec
                .Builder(BiometricAuthentication.CLIENT_ID, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(userAuthenticationRequired)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build();
        biometricContext.getKeyGenerator().init(spec);
        return biometricContext.getKeyGenerator().generateKey();
    }

    @RequiresPermission(Manifest.permission.USE_BIOMETRIC)
    private void showBiometricPrompt(CallbackContext callbackContext, CordovaInterface cordova, String preferencesKey, String password, SharedPreferences sharedPreferences, Cipher cipher) {
        BiometricPrompt biometricPrompt = buildBiometricPrompt(callbackContext, cordova);
        biometricPrompt.authenticate(
                new BiometricPrompt.CryptoObject(cipher),
                new CancellationSignal(),
                getExecutor(cordova),
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        encryptPassword(result.getCryptoObject().getCipher(), sharedPreferences, preferencesKey, password, callbackContext);
                    }
                }
        );
    }

    private BiometricPrompt buildBiometricPrompt(CallbackContext callbackContext, CordovaInterface cordova) {
        DialogInterface.OnClickListener onClickCancel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callbackContext.sendPluginResult(Error.CANCELLED.toPluginResult());
            }
        };
        return new BiometricPrompt.Builder(cordova.getContext())
                .setNegativeButton(BiometricResources.getCancelText(cordova), getExecutor(cordova), onClickCancel)
                .setTitle(BiometricResources.getTitleText(cordova))
                .build();
    }

    private Executor getExecutor(CordovaInterface cordova) {
        return cordova.getActivity().getMainExecutor();
    }

    private void encryptPassword(Cipher cipher, SharedPreferences sharedPreferences, String preferencesKey, String password, CallbackContext callbackContext) {
        byte[] encryptedPassword;
        try {
            encryptedPassword = cipher.doFinal(password.getBytes());
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            Log.e(BiometricAuthentication.TAG, "Exception while saving", e);
            callbackContext.sendPluginResult(Error.OTHER.toPluginResult(e.getMessage()));
            return;
        }
        sharedPreferences.edit()
                .putString(Preferences.getEncodedPasswordKey(preferencesKey), Base64.encodeToString(encryptedPassword, Base64.DEFAULT))
                .putString(Preferences.getInitializationVectorKey(preferencesKey), Base64.encodeToString(cipher.getIV(), Base64.DEFAULT))
                .commit();
        callbackContext.success();
    }

    private Cipher getCipher(SecretKey secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        SecureRandom random = new SecureRandom();
        byte[] ivBytes = new byte[16];
        random.nextBytes(ivBytes);
        Cipher cipher = Cipher.getInstance(String.format("%s/%s/%s", KeyProperties.KEY_ALGORITHM_AES, KeyProperties.BLOCK_MODE_CBC, KeyProperties.ENCRYPTION_PADDING_PKCS7));
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher;
    }
}
