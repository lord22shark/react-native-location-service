package me.buskar;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

		HttpURLConnection urlConnection = null;

		try {

			byte[] body = params[0].getBytes(StandardCharsets.UTF_8);

			URL url = new URL(this.url);

			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setDoOutput(true);
			urlConnection.setInstanceFollowRedirects(false);
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
			urlConnection.setRequestProperty("Content-Length", Integer.toString(body.length));
			urlConnection.setUseCaches(false);
			urlConnection.getOutputStream().write(body);
			urlConnection.setReadTimeout(15000);
			urlConnection.setConnectTimeout(15000);

			urlConnection.connect();

			Reader inputReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append("[RNLS RESPONSE] ");

			for (int c; (c = inputReader.read()) >= 0;) {

				stringBuilder.append((char) c);

			}

			Log.v(TAG, stringBuilder.toString());

			urlConnection.disconnect();

		} catch (Exception exception) {

			Log.e(this.TAG, "Could not send post data to " + this.url, exception);

		} finally {

			urlConnection.disconnect();

		}

		return null;

	}

}
