package me.buskar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ~lordshark on 02/05/2019.
 */

public class RNLocationServiceListener implements LocationListener {

	/**
	 *
	 */
	private String TAG = "ReactNativeJS";

	/**
	 *
	 */
	private LocationManager locationManager = null;

	/**
	 *
	 */
	private Location location = null;

	/**
	 * minimum time interval between location updates, in milliseconds
	 */
	private int minTime = 3000;

	/**
	 * minimum distance between location updates, in meters
	 */
	private float minDistance = 5f;

	/**
	 *
	 */
	private String url = null;

	/**
	 *
	 */
	private RNLocationServiceAPI api = null;

	/**
	 *
	 */
	private Boolean useEvents = true;

	/**
	 *
	 */
	private Key privateKey = null;

	/**
	 *
	 */
	private String identifier = null;

	/**
	 *
	 */
	private Cipher cipher;

	/**
	 *
	 */
	private DeviceEventManagerModule.RCTDeviceEventEmitter emitter;

	/**
	 *
	 * @param applicationContext
	 * @param emitter
	 * @param minTime
	 * @param minDistance
	 * @param url
	 * @param privateKey
	 * @param identifier
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 */
	public RNLocationServiceListener(Context applicationContext, DeviceEventManagerModule.RCTDeviceEventEmitter emitter, int minTime, float minDistance, String url, String privateKey, String identifier) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {

		this.locationManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);

		this.url = url;

		this.minDistance = minDistance;

		this.minTime = minTime;

		//this.api = new RNLocationServiceAPI(this.url);

		this.privateKey = new SecretKeySpec(privateKey.getBytes(), "AES");

		this.cipher = Cipher.getInstance("AES");

		this.cipher.init(Cipher.ENCRYPT_MODE, this.privateKey);

		this.identifier = identifier;

		if (emitter != null) {

			this.emitter = emitter;

		}

	}

	/**
	 *
	 */
	@Override
	public void onLocationChanged (Location location) {

		try {

			Log.v(TAG, this.toPLAIN(location));

			//data = this.toCRYPT(data.getBytes());

			//Log.v(TAG, data);

			//data = this.toZip(data);

			//Log.v(TAG, data);

			String data = this.toBase64(this.toZip(this.toCRYPT(this.toPLAIN(location).getBytes())));

			data += "&identifier=" + this.identifier;

			Log.v(TAG, data);

			//this.api.doInBackground(data);
			new RNLocationServiceAPI(this.url).execute(data);

			if (emitter != null) {

				this.emitter.emit("onLocationChanged", this.toJSON(location));

			}

		} catch (Exception exception) {

			Log.e(this.TAG, "onLocationChanged did not work properly", exception);

			if (emitter != null) {

				this.emitter.emit("onLocationChanged", null);

			}

		}

	}

	/**
	 *
	 */
	@Override
	public void onStatusChanged (String provider, int status, Bundle extras) {

		Log.d(this.TAG, provider);

	}

	/**
	 *
	 */
	@Override
	public void onProviderEnabled (String provider) {

		Log.d(this.TAG, provider);

	}

	/**
	 *
	 */
	@Override
	public void onProviderDisabled (String provider) {

		Log.d(this.TAG, provider);

	}

	/**
	 *
	 */
	@SuppressLint("MissingPermission")
	public void start () {

		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);

		this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);

		this.location = this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		if (this.location == null) {

			this.location = this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		}

		if (this.location != null) {

			this.onLocationChanged(location);

		}

	}

	/**
	 *
	 */
	public void stop () {

		if (this.locationManager != null) {

			this.locationManager.removeUpdates(this);

			this.locationManager = null;

		}

	}

	/**
	 *
	 * @param location
	 * @return
	 */
	private String toJSON (Location location) {

		return "{\"latitude\": \"" + location.getLatitude() + "\", \"longitude\": \"" + location.getLongitude() + "\", \"bearing\": \"" + location.getBearing() + "\", \"altitude\": \"" + location.getAltitude() + "\", \"accuracy\": \"" + location.getAccuracy() + "\", \"speed\": \"" + location.getSpeed() + "\", \"tsp\": \"" + location.getTime() + "\"}";

	}

	/**
	 *
	 * @param location
	 * @return
	 */
	private String toPLAIN (Location location) {

		return location.getLatitude() + ";" + location.getLongitude() + ";" + location.getBearing() + ";" + location.getAltitude() + ";" + location.getAccuracy() + ";" + location.getSpeed() + ";" + location.getTime() + ";";

	}

	/**
	 *
	 * @param data
	 * @return
	 */
	private String toCRYPT (byte[] data) {

		try {

			return new String(this.cipher.doFinal(data));

		} catch (BadPaddingException badPaddingException) {

			Log.e(this.TAG, "Cloud not encrypt location data (bad)", badPaddingException);

		} catch (IllegalBlockSizeException illegalBlockSizeException) {

			Log.e(this.TAG, "Cloud not encrypt location data (illegal)", illegalBlockSizeException);

		}

		return "";

	}

	/**
	 *
	 */
	private String toZip (String data) {

		try {

			ByteArrayOutputStream baStream = new ByteArrayOutputStream(data.length());

			GZIPOutputStream gzStream = new GZIPOutputStream(baStream);

			gzStream.write(data.getBytes());

			gzStream.close();

			byte[] compressed = baStream.toByteArray();

			baStream.close();

			return new String(compressed, "UTF-8");

		} catch (IOException exception) {

			Log.e(this.TAG, "Cloud not compress location data (io)", exception);

			return "";

		}

	}

	/**
	 *
	 * @param data
	 * @return
	 */
	private String toBase64 (String data) {

		try {

			return "data=" + Base64.encodeToString(data.getBytes("UTF-8"), Base64.DEFAULT);

		} catch (Exception exception) {

			Log.e(this.TAG, "Cloud not encode data (64)", exception);

			return "";

		}

	}

	/**
	 *
	 * @return
	 */
	public Boolean isRunning () {

		return this.locationManager != null;

	}

}
