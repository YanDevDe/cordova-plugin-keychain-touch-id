package com.cordova.plugin.android.biometricauth;

import android.hardware.biometrics.BiometricManager;

import java.security.KeyStore;

import javax.crypto.KeyGenerator;

public class BiometricContext {

    private final BiometricManager biometricManager;
    private final KeyStore keyStore;
    private final KeyGenerator keyGenerator;

    BiometricContext(BiometricManager biometricManager, KeyStore keyStore, KeyGenerator keyGenerator) {
        this.biometricManager = biometricManager;
        this.keyStore = keyStore;
        this.keyGenerator = keyGenerator;
    }

    public BiometricManager getBiometricManager() {
        return biometricManager;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }
}
