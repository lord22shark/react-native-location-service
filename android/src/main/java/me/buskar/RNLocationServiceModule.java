package me.buskar;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 *
 */
public class RNLocationServiceModule extends ReactContextBaseJavaModule {

	/**
	 *
	 */
	private final ReactApplicationContext reactContext;

	/**
	 *
	 */
	private String TAG = "ReactNativeJS";

	/**
	 *
	 */
	private RNLocationServiceListener listener;

	/**
	 *
	 */
	private String[] permissions = {
		Manifest.permission.ACCESS_FINE_LOCATION,
		Manifest.permission.ACCESS_COARSE_LOCATION
	};

	/**
	 *
	 */
	private final int REQUEST_PERMISSION_LOCATION = 22;

	/**
	 *
	 */
	public static final String SHARED_PREFERENCES = "me.buskar.RNLocationServiceModule";

	/**
	 *
	 */
	private Context context;

	/**
	 *
	 */
	private Intent serviceIntent;

	/**
	 *
	 */
	private SharedPreferences preferences;

	/**
	 *
	 */
	RNLocationService rnLocationService;

	/**
	 *
	 */
	Boolean isBound = false;


	/**
	 * Used to (un)bind the service to with the activity
	 */
	private final ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected (ComponentName name, IBinder service) {

			RNLocationService.RNServiceBinder binder = (RNLocationService.RNServiceBinder) service;

			rnLocationService = binder.getService();

			isBound = true;

			RNLocationServiceModule.this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onBindService", true);

			Log.v(TAG, "[RNLS] onServiceConnected()");

		}

		// THIS IS NOT CALLING!!!!!!!!!!!!!!!!
		@Override
		public void onServiceDisconnected (ComponentName name) {

			isBound = false;

			RNLocationServiceModule.this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onBindService", false);

			Log.v(TAG, "[RNLS] onServiceDisconnected()");

		}

	};

	/**
	 *
	 */
	public RNLocationServiceModule (ReactApplicationContext reactContext) {

		super(reactContext);
	
		this.reactContext = reactContext;

		this.context = this.reactContext.getApplicationContext();

		this.preferences = this.context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);

	}

	/**
	 *
	 */
	@Override
	public String getName () {

		return "RNLocationService";
	
	}

	/**
	 *
	 * @return
	 */
	private Boolean isPermited () {

		Context context;

		try {

			context = this.getCurrentActivity().getApplicationContext();

		} catch (Exception exception) {

			Log.e(TAG, "getCurrentActivity().getApplicationContext() Fail", exception);

			try {

				context = this.context;

			} catch (Exception exception2) {

				Log.e(TAG, "this.context Fail", exception);

				return false;

			}

		}

		Boolean finePermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

		Boolean coarsePermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

		Log.i(TAG, "Fine: " + finePermission.toString());
		Log.i(TAG, "Coarse: " + coarsePermission.toString());

		return (finePermission && coarsePermission);

	}

	@ReactMethod
	public void isPermited (Callback onSuccess) {

		onSuccess.invoke(this.isPermited());

	}

	@ReactMethod
	public void requestPermission () {

		Log.i(TAG, "Requested");

		ActivityCompat.requestPermissions(this.getCurrentActivity(), this.permissions, REQUEST_PERMISSION_LOCATION);

	}

	/**
	 *
	 */
	@ReactMethod
	public void startListener (int minTime, float minDistance, String url, String privateKey, String identifier, String user, Callback callback) {

		if (callback == null) {

			Log.e(TAG, "[RNLS] RNLocationServiceModule.startListener - callback MUST NOT be null");

			return;

		}

		try {

			if (this.isPermited()) {

				if ((minTime <= 0) || (minDistance <= 0.0) || (url == null)) {

					callback.invoke(null, "minTime or minDistance cannot be less or equals 0 and url must not be null");

				} else if (this.listener == null) {

					this.listener = new RNLocationServiceListener(this.context, this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class), minTime, minDistance, url, privateKey, identifier, user);

					this.listener.start();

					callback.invoke(true, null);

				} else {

					callback.invoke(null,"Service Listener is already started!");

				}

			} else {

				callback.invoke(null, "Permissions of FINE and COARSE location are not GRANTED! Request!");

			}

		} catch (Exception exception) {

			Log.v(this.TAG, "Internal Error when starting listener RNLocationServiceListener", exception);

			callback.invoke(null, exception.getMessage());

		}

	}

	/**
	 *
	 */
	@ReactMethod
	public void stopListener (Callback callback) {

		if (callback == null) {

			Log.e(TAG, "[RNLS] RNLocationServiceModule.stopListener - callback MUST NOT be null");

			return;

		}

		try {

			if (this.listener != null) {

				if (this.listener.isRunning()) {

					this.listener.stop();

					this.listener = null;

					callback.invoke(true, null);

				} else {

					callback.invoke(null, "Cannot stop listener that is not running");

				}

			} else {

				callback.invoke(null, "Cannot stop listener that is null");

			}

		} catch (Exception exception) {

			Log.v(this.TAG, "Internal Error when stopping listener RNLocationServiceListener", exception);

			callback.invoke(null, exception.getMessage());

		}

	}

	/**
	 *
	 * @param key
	 * @param value
	 * @param callback
	 */
	@ReactMethod
	public void addPreference (String key, String value, Callback callback) {

		try {

			SharedPreferences.Editor editor = this.preferences.edit();

			editor.putString(key, value);

			editor.apply();

			callback.invoke(this.preferences.getAll().toString(), null);

		} catch (Exception exception) {

			callback.invoke(null, exception.getMessage());

		}

	}

	/**
	 *
	 * @param callback
	 */
	@ReactMethod
	public void getPreference (Callback callback) {

		try {

			callback.invoke(this.preferences.getAll().toString(), null);

		} catch (Exception exception) {

			callback.invoke(null, exception.getMessage());

		}

	}

	/**
	 *
	 * @param callback
	 */
	@ReactMethod
	public void removePreference (String key, Callback callback) {

		try {

			SharedPreferences.Editor editor = this.preferences.edit();

			editor.remove(key);

			editor.apply();

			callback.invoke(this.preferences.getAll().toString(), null);

		} catch (Exception exception) {

			callback.invoke(null, exception.getMessage());

		}

	}

	/**
	 *
	 * @return
	 */
	//@Deprecated
	private boolean isServiceRunning () {

		ActivityManager manager = (ActivityManager) this.getCurrentActivity().getSystemService(Context.ACTIVITY_SERVICE);

		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

			if (RNLocationService.class.getName().equals(service.service.getClassName())) {

				return true;

			}

		}

		return false;

	}

	/**
	 *
	 * @param minTime
	 * @param minDistance
	 * @param url
	 * @param privateKey
	 * @param identifier
	 * @param callback
	 */
	@ReactMethod
	public void startService (int minTime, float minDistance, String url, String privateKey, String identifier, String user, Callback callback) {

		if (callback == null) {

			Log.e(TAG, "[RNLS] RNLocationServiceModule.startService - callback MUST NOT be null");

			return;

		}

		if (this.isPermited()) {

			try {

				/*if (this.isDisabled || this.isBind) {

					return;

				}*/

				this.serviceIntent = new Intent(this.context, RNLocationService.class);

				/*this.serviceIntent.putExtra("minTime", minTime);
				this.serviceIntent.putExtra("minDistance", minDistance);
				this.serviceIntent.putExtra("privateKey", privateKey);
				this.serviceIntent.putExtra("identifier", identifier);
				this.serviceIntent.putExtra("url", url);*/

				SharedPreferences.Editor editor = this.preferences.edit();

				editor.clear();
				editor.apply();
				editor.putInt("minTime", minTime);
				editor.apply();
				editor.putInt("minDistance", (int) minDistance);
				editor.apply();
				editor.putString("privateKey", privateKey);
				editor.apply();
				editor.putString("identifier", identifier);
				editor.apply();
				editor.putString("url", url);
				editor.apply();
				editor.putString("user", user);
				editor.apply();

				this.context.bindService(this.serviceIntent, connection, Context.BIND_AUTO_CREATE);

				//context.startService(this.serviceIntent);

				callback.invoke(true, null);

			} catch (Exception exception) {

				Log.e(TAG, "[RNLS] startService()", exception);

				callback.invoke(null, exception.toString());

			}

		} else {

			callback.invoke(null, "Permissions of FINE and COARSE location are not GRANTED! Request!");

		}

	}

	@ReactMethod
	public void stopService (Callback callback) {

		if (callback == null) {

			Log.e(TAG, "[RNLS] RNLocationServiceModule.stopService - callback MUST NOT be null");

			return;

		}

		try {

			//Intent intent = new Intent(this.context, RNLocationService.class);

			/*if (!isBind) {

				return;

			}*/


			//

			//this.isBind = false;

			this.context.unbindService(this.connection);

			this.context.stopService(this.serviceIntent);

			callback.invoke(true, null);

		} catch (Exception exception) {

			Log.e(TAG, "[RNLS] stopService()", exception);

			callback.invoke(null, exception.toString());

		}

	}

	/**
	 *
	 */
	@ReactMethod
	public void isServiceRunning (Callback callback) {

		callback.invoke(this.isServiceRunning());

	}

	@ReactMethod
	public void isListenerRunning (Callback callback) {

		if (this.listener != null) {

			callback.invoke(this.listener.isRunning());

		} else {

			callback.invoke(false);

		}

	}

}
