package me.buskar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

public class RNLocationService extends Service {

	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	class RNServiceBinder extends Binder {

		/**
		 * // Return this instance of RNLocationService so clients can call public methods
		 * @return
		 */
		RNLocationService getService() {

			return RNLocationService.this;

		}

	}

	/**
	 * Fixed ID for the 'foreground' notification
	 */
	public static final int NOTIFICATION_ID = -30495834;

	/**
	 * Default title of the background notification
	 */
	private static final String NOTIFICATION_TITLE = "Buskar está em execução...";

	/**
	 * Default text of the background notification
	 */
	private static final String NOTIFICATION_TEXT = "Atualizando sua localização!";

	/**
	 * Default icon of the background notification
	 */
	private static final String NOTIFICATION_ICON = "icon";

	/**
	 * Binder given to clients
	 */
	private final IBinder binder = new RNServiceBinder();

	/**
	 *
	 */
	private PowerManager.WakeLock wakeLock;

	/**
	 *
	 */
	private RNLocationServiceListener listener;

	/**
	 *
	 */
	private String TAG = "ReactNativeJS";

	/**
	 *
	 */
	private SharedPreferences preferences;

	/**
	 *
	 */
	public RNLocationService () {

	}

	/**
	 *
	 * @param intent
	 * @return
	 */
	@Override
	public IBinder onBind (Intent intent) {

		Log.v(TAG, "[RNLS] onBind()");

		return binder;

	}

	/**
	 * Put the service in a foreground state to prevent app from being killed
	 * by the OS.
	 */
	@Override
	public void onCreate ()  {

		super.onCreate();

		keepAwake();

	}

	/**
	 *
	 * @param intent
	 * @param flags
	 * @param startId
	 * @return
	 */
	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {

		/*if (this.preferences == null) {

			this.preferences = this.getSharedPreferences(RNLocationServiceModule.SHARED_PREFERENCES, Context.MODE_PRIVATE);

		}

		if (this.listener != null) {

			this.preferences.edit().putString("keep", "true");

			if (!this.listener.isRunning()) {

				this.listener.start();

			}

		} else {

			try {

				this.listener = new RNLocationServiceListener(this.getApplicationContext(), null, 2121, 1, "http://", "q1w2e3r4t5y6u7i8", "1");

				this.listener.start();

			} catch (Exception exception) {

				Log.e(TAG, "Exception on startCommand instantiating Listener");

				this.onDestroy();

				return START_NOT_STICKY;

			}

		}*/

		/*this.thread = new Thread(new Runnable() {

			private int a = 0;

			@Override
			public void run() {

				while (a < 10000) {

					try {

						Log.v(TAG, String.valueOf(a));

						Thread.sleep(1000);

						a += 1;

					} catch (Exception e) {
						Log.e(TAG, "---", e);
					}

				}

			}

		});*/

		//this.thread.start();

		Log.v(TAG, "[RNLS] onStartCommand()");

		return START_STICKY;

	}

	@Override
	public void onDestroy () {

		/*try {

			Log.e(TAG, this.toString());

			Log.e(TAG, "onDestroy");

			Log.e(TAG, "onDestroy ->" + this.keep);

			if (this.keep) {

				Log.v(TAG, "começa novamente");

				this.startService(new Intent(this, RNLocationService.class));

			} else {

				super.onDestroy();

				Log.e(TAG, "onDestroy - super");

			}

		} catch (Exception exception) {

			Log.e(TAG, "Exception on onDestroy destroying service", exception);

		}*/

		//this.thread.currentThread().interrupt();

		//this.thread = null;

		sleepWell();

		super.onDestroy();

		Log.v(TAG, "[RNLS] onDestroy()");

	}

	// ----

	/**
	 * Put the service in a foreground state to prevent app from being killed
	 * by the OS.
	 */
	@SuppressLint("WakelockTimeout")
	private void keepAwake () {

		try {

			this.preferences = this.getApplicationContext().getSharedPreferences(RNLocationServiceModule.SHARED_PREFERENCES, Context.MODE_PRIVATE);

			//JSONObject settings = BackgroundMode.getSettings();

			//boolean isSilent    = settings.optBoolean("silent", false);

			//if (!isSilent) {
			startForeground(NOTIFICATION_ID, makeNotification(false));
			//}

			PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);

			this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "rnlocation:wakelock");

			this.wakeLock.acquire();

			// ---

			//this.listener = new RNLocationServiceListener(this.getApplicationContext(), null, 2000, 10, "http://localhost/", "q1w2e3r4t5y6u7i8", "33");
			//this.listener = new RNLocationServiceListener(this.getApplicationContext(), null, this.bundle.getInt("minTime"), this.bundle.getInt("minDistance"), this.bundle.getString("url"), this.bundle.getString("privateKey"), this.bundle.getString("identifier"));

			int minTime = this.preferences.getInt("minTime", 9999);
			long minDistance = this.preferences.getInt("minDistance", 100);
			String url = this.preferences.getString("url", "http://localhost/echo");
			String privateKey = this.preferences.getString("privateKey", "q0w9e8r7t6y5u4i3");
			String identifier = this.preferences.getString("identifier", "-1");
			String user = this.preferences.getString("user", "-1");

			this.listener = new RNLocationServiceListener(this.getApplicationContext(), null, minTime, minDistance, url, privateKey, identifier, user);

			this.listener.start();

		} catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {

			Log.e(TAG, "[RNLS] keepAwake()", e);

		}

		Log.v(TAG, "[RNLS] keepAwake()");

	}

	/**
	 * Stop background mode.
	 */
	private void sleepWell() {

		if (this.listener.isRunning()) {

			this.listener.stop();

			this.listener = null;

		}

		this.stopForeground(true);

		this.getNotificationManager().cancel(NOTIFICATION_ID);

		if (this.wakeLock != null) {

			this.wakeLock.release();

			this.wakeLock = null;

		}

	}

	/**
	 * Create a notification as the visible part to be able to put the service
	 * in a foreground state by using the default settings.
	 */
	private Notification makeNotification (Boolean resume)  {

		// use channelid for Oreo and higher

		String CHANNEL_ID = "react-native-location-service-id";

		if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

			// The user-visible name of the channel.
			CharSequence name = "react-native-location-service";

			// The user-visible description of the channel.
			String description = "react-native-location-service notification";

			int importance = NotificationManager.IMPORTANCE_HIGH;

			NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);

			getNotificationManager().createNotificationChannel(notificationChannel);

		}

		/*String title = NOTIFICATION_TITLE;
		String text     = settings.optString("text", NOTIFICATION_TEXT);
		boolean bigText = settings.optBoolean("bigText", false);*/

		Context context = this.getApplicationContext();

		String packageName = context.getPackageName();

		Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);

		Notification.Builder notification = new Notification.Builder(context).setContentTitle(NOTIFICATION_TITLE).setContentText(NOTIFICATION_TEXT).setOngoing(true).setSmallIcon(getIconResId());

		if (Build.VERSION.SDK_INT >= 26){

			notification.setChannelId(CHANNEL_ID);

		}

		/*if (settings.optBoolean("hidden", true)) {
			notification.setPriority(Notification.PRIORITY_MIN);
		}*/

		/*if (bigText || text.contains("\n")) {
			notification.setStyle(
					new Notification.BigTextStyle().bigText(text));
		}*/

		setColor(notification);

		//if (intent != null && settings.optBoolean("resume")) {
		if (intent != null && resume == true) {

			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

			PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			notification.setContentIntent(contentIntent);

		}

		return notification.build();

	}

	/**
	 * Update the notification.
	 *
	 */
	protected void updateNotification (Boolean isSilent) {

		//boolean isSilent = settings.optBoolean("silent", false);

		if (isSilent) {

			stopForeground(true);

			return;

		}

		Notification notification = makeNotification(false);

		getNotificationManager().notify(NOTIFICATION_ID, notification);

	}

	/**
	 * Retrieves the resource ID of the app icon.
	 *
	 */
	private int getIconResId () {

		int resId = getIconResId(NOTIFICATION_ICON, "mipmap");

		if (resId == 0) {

			resId = getIconResId(NOTIFICATION_ICON, "drawable");

		}

		return resId;
	}

	/**
	 * Retrieve resource id of the specified icon.
	 *
	 * @param icon The name of the icon.
	 * @param type The resource type where to look for.
	 *
	 * @return The resource id or 0 if not found.
	 */
	private int getIconResId (String icon, String type) {

		Resources res  = getResources();

		String packageName = getPackageName();

		int resId = res.getIdentifier(icon, type, packageName);

		if (resId == 0) {

			resId = res.getIdentifier("icon", type, packageName);

		}

		return resId;
	}

	/**
	 * Set notification color if its supported by the SDK.
	 *
	 * @param notification A Notification.Builder instance
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void setColor (Notification.Builder notification) {

		String hex = "D90000"; //settings.optString("color", null);

		if (Build.VERSION.SDK_INT < 21 || hex == null) {

			return;

		}

		try {

			int aRGB = Integer.parseInt(hex, 16) + 0xFF000000;

			notification.setColor(aRGB);

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	/**
	 * Returns the shared notification service manager.
	 */
	private NotificationManager getNotificationManager()  {

		return (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

	}

}
