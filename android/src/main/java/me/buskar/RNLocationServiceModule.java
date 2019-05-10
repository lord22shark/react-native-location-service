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
	private final int REQUEST_PERMISSION_LOCATION = 1;

	/**
	 *
	 */
	private Boolean permissionsGranted = null;

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

			Log.v(TAG, "[JAVA] OnServiceConnected");

		}

		// THIS IS NOT CALLING!!!!!!!!!!!!!!!!
		@Override
		public void onServiceDisconnected (ComponentName name) {

			isBound = false;

			RNLocationServiceModule.this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onBindService", false);

			Log.v(TAG, "[JAVA] onServiceDisconnected");

		}

	};

	/**
	 *
	 */
	public RNLocationServiceModule (ReactApplicationContext reactContext) {

		super(reactContext);
	
		this.reactContext = reactContext;

		this.permissionsGranted = (ActivityCompat.checkSelfPermission(this.reactContext.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this.getReactApplicationContext().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);

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

	@ReactMethod
	public void isPermited (Callback onSuccess) {

		onSuccess.invoke(this.permissionsGranted);

	}

	@ReactMethod
	public void requestPermission () {

		ActivityCompat.requestPermissions(this.getCurrentActivity(), this.permissions, REQUEST_PERMISSION_LOCATION);

	}

	/**
	 *
	 */
	@ReactMethod
	public void startListener (int minTime, float minDistance, String url, String privateKey, String identifier, Callback callback) {

		try {

			if (this.permissionsGranted == true) {

				if ((minTime <= 0) || (minDistance <= 0.0) || (url == null)) {

					callback.invoke(null, "minTime or minDistance cannot be less or equals 0 and url must not be null");

				} else if (this.listener == null) {

					this.listener = new RNLocationServiceListener(this.context, this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class), minTime, minDistance, url, privateKey, identifier);

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

				Log.v(TAG, "[JAVA] isRunning - TRUE");

				return true;

			}

		}

		//return false;
		Log.v(TAG, "[JAVA] isRunning - FALSE");

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
	public void startService (int minTime, float minDistance, String url, String privateKey, String identifier, Callback callback) {

		Log.v(TAG, "[JAVA] ReactMethod s1 Starting...");

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

			Log.v(TAG, "ANTES " + this.preferences.getAll().toString());

			this.context.bindService(this.serviceIntent, connection, Context.BIND_AUTO_CREATE);

			//context.startService(this.serviceIntent);

			callback.invoke("[JAVA] Started", null);

		} catch (Exception e) {

			Log.v(TAG, "[JAVA] s1 Error", e);

			callback.invoke(null, e.toString());

		}

	}

	@ReactMethod
	public void stopService (Callback callback) {

		Log.v(TAG, "[JAVA] ReactMethod s2 Stopping...");

		try {

			//Intent intent = new Intent(this.context, RNLocationService.class);

			/*if (!isBind) {

				return;

			}*/


			//

			//this.isBind = false;

			this.context.unbindService(this.connection);

			this.context.stopService(this.serviceIntent);

			callback.invoke("[JAVA] Stopped", null);

		} catch (Exception e) {

			Log.v(TAG, "[JAVA] s2 Error", e);

			callback.invoke(null, e.toString());

		}

	}


	/**
	 *
	 */
	@ReactMethod
	public void isServiceRunning (Callback callback) {

		callback.invoke(this.isServiceRunning());

	}

}