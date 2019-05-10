package me.buskar;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ~lordshark on 02/05/2019.
 */
public class RNLocationServiceAPI extends AsyncTask<String, Integer, Void> {

	/**
	 *
	 */
	private String TAG = "ReactNativeJS";

	/**
	 *
	 */
	private String url;

	/**
	 *
	 */
	public RNLocationServiceAPI (String url) {

		this.url = url;

	}

	/**
	 *
	 */
	@Override
	protected void onPreExecute () {

		super.onPreExecute();

	}

	/**
	 *
	 * @param params
	 */
	@Override
	protected Void doInBackground(String... params) {

		OutputStream out;

		HttpURLConnection urlConnection = null;

		try {

			URL url = new URL(this.url);

			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setDoOutput(true);
			urlConnection.setInstanceFollowRedirects(false);
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			urlConnection.setRequestProperty("charset", "utf-8");
			urlConnection.setRequestProperty("Content-Length", Integer.toString(params[0].length()));
			urlConnection.setUseCaches(false);
			urlConnection.setReadTimeout(15000);
			urlConnection.setConnectTimeout(15000);

			out = new BufferedOutputStream(urlConnection.getOutputStream());

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

			writer.write(params[0]);

			writer.flush();

			writer.close();

			out.close();

			urlConnection.connect();

		} catch (Exception exception) {

			Log.e(this.TAG, "Could not send post data to " + this.url, exception);

		} finally {

			urlConnection.disconnect();

		}

		return null;

	}

}
