package com.cordova.plugin.android.fingerprintauth;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import com.cordova.plugin.android.biometricauth.BiometricAuthentication;
import com.cordova.plugin.android.biometricauth.Error;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@TargetApi(23) public class FingerprintAuth extends CordovaPlugin {


  public static final String TAG = "FingerprintAuth";

  /**
   * Alias for our key in the Android Key Store
   */
  public static String packageName;
  public static CallbackContext mCallbackContext;

  public FingerprintAuthAux mFingerprintAuthAux;
  /**
   * Constructor.
   */
  public FingerprintAuth() {
  }

  private BiometricAuthentication biometricAuthentication;

  public static void onCancelled() {
    mCallbackContext.sendPluginResult(Error.CANCELLED.toPluginResult());
  }


  /**
   * Sets the context of the Command. This can then be used to do things like
   * get file paths associated with the Activity.
   *
   * @param cordova The context of the main Activity.
   * @param webView The CordovaWebView Cordova is running in.
   */

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    if (Build.VERSION.SDK_INT >= 29) {
      biometricAuthentication = new BiometricAuthentication();
      biometricAuthentication.initialize(cordova);
      return;
    }
    super.initialize(cordova, webView);

    packageName = cordova.getActivity().getApplicationContext().getPackageName();


    if (android.os.Build.VERSION.SDK_INT < 23) {
      return;
    }
    if (mFingerprintAuthAux == null) {
      mFingerprintAuthAux = new FingerprintAuthAux(this);
    }

    mFingerprintAuthAux.initialize(cordova, webView);

  }

  /**
   * Executes the request and returns PluginResult.
   *
   * @param action The action to execute.
   * @param args JSONArry of arguments for the plugin.
   * @param callbackContext The callback id used when calling back into JavaScript.
   * @return A PluginResult object with a status and message.
   */
  public boolean execute(final String action, JSONArray args, CallbackContext callbackContext)
      throws JSONException {
    if (biometricAuthentication != null) {
      return biometricAuthentication.execute(cordova, action, args, callbackContext);
    }

    mCallbackContext = callbackContext;
          
    if (android.os.Build.VERSION.SDK_INT < 23) {
      callbackContext.sendPluginResult(Error.NO_HARDWARE.toPluginResult());
      return true;
    }

    if (mFingerprintAuthAux == null) {
      mFingerprintAuthAux = new FingerprintAuthAux(this);
    }


    return mFingerprintAuthAux.execute(action, args, callbackContext, cordova);

  }

}
