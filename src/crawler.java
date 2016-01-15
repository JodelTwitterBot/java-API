import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class crawler {

	public static void main(String[] args) throws Exception {
		while (true) {
			jodelFetcher();
			Thread.sleep(20000);
		}
	}

	public static void jodelFetcher() throws Exception {

		try {
			URL url = new URL("https://api.go-tellm.com/api/v2/posts/");
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setRequestProperty("Authorization", "Bearer "
					+ "UUID");
			urlConnection.setRequestMethod("GET");

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));

			StringBuilder sb = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			String result = null;
			result = sb.toString();

			reader.close();

			// System.out.println(result);
			JSONObject jObject = new JSONObject(result);

			JSONArray jArray = jObject.getJSONArray("posts");

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

			Date now = new Date();

			System.out.println(jArray);

			for (int i = 0; i < jArray.length(); i++) {
				JSONObject oneObject = jArray.getJSONObject(i);
				// Pulling items from the array
				String message = oneObject.getString("message");
				// System.out.println(i);
				if (oneObject.has("thumbnail_url")) {
					System.out.println("bild!");
					Date d = sdf.parse(oneObject.getString("created_at"));
					if (now.getTime() - d.getTime() < 30 * 1000) {
						String imageURL = oneObject.getString("thumbnail_url");
						imageURL = "http:" + imageURL;
						System.out.println(imageURL);

						URL Imageurl = new URL(imageURL);
						InputStream in = new BufferedInputStream(
								Imageurl.openStream());
						OutputStream out = new BufferedOutputStream(
								new FileOutputStream("./tmp.jpeg"));

						for (int i1; (i1 = in.read()) != -1;) {
							out.write(i1);
						}
						in.close();
						out.close();

						int maxLength = (message.length() < 139) ? message
								.length() : 139;
						message = message.substring(0, maxLength);

						picture(message, "./tmp.jpeg");
					}
				}

				else if (message.length() < 140) {
					Date d = sdf.parse(oneObject.getString("created_at"));
					if (now.getTime() - d.getTime() < 5 * 60 * 1000) {
						System.out.println(message);
						System.out.println(d);
						twitter(message);
					}
				}

			}
			// System.exit(-1);

		} catch (Exception e) {
		}
	}

	public static void picture(String message, String pfad)
			throws TwitterException {

		// System.out.println("Upload started");

		ConfigurationBuilder twitterConfigBuilder = new ConfigurationBuilder();
		twitterConfigBuilder.setDebugEnabled(true);
		/*
		 * NOPE :P
		 */

		Twitter twitter = new TwitterFactory(twitterConfigBuilder.build())
				.getInstance();

		File file = new File(pfad);

		System.out.println(message);
		StatusUpdate status = new StatusUpdate(message);
		status.setMedia(file); // set the image to be uploaded here.
		twitter.updateStatus(status);
	}

	public static void twitter(String message) {

		try {

			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
					/*
					 * NOPE :P
					 */
			TwitterFactory tf = new TwitterFactory(cb.build());
			Twitter twitter = tf.getInstance();
			try {
				// get request token.
				// this will throw IllegalStateException if access token is
				// already available
				RequestToken requestToken = twitter.getOAuthRequestToken();
				System.out.println("Got request token.");
				System.out.println("Request token: " + requestToken.getToken());
				System.out.println("Request token secret: "
						+ requestToken.getTokenSecret());
				AccessToken accessToken = null;

				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				while (null == accessToken) {
					String pin = br.readLine();
					try {
						if (pin.length() > 0) {
							accessToken = twitter.getOAuthAccessToken(
									requestToken, pin);
						} else {
							accessToken = twitter
									.getOAuthAccessToken(requestToken);
						}
					} catch (TwitterException te) {
						if (401 == te.getStatusCode()) {
							System.out
									.println("Unable to get the access token.");
						} else {
							te.printStackTrace();
						}
					}
				}
				System.out.println("Got access token.");
				System.out.println("Access token: " + accessToken.getToken());
				System.out.println("Access token secret: "
						+ accessToken.getTokenSecret());
			} catch (IllegalStateException ie) {
				// access token is already available, or consumer key/secret is
				// not set.
				if (!twitter.getAuthorization().isEnabled()) {
					System.out.println("OAuth consumer key/secret is not set.");
					System.exit(-1);
				}
			}
			Status status = twitter.updateStatus(message);
			System.out.println("Successfully updated the status to ["
					+ status.getText() + "].");
			// System.exit(0);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to get timeline: " + te.getMessage());
			// System.exit(-1);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("Failed to read the system input.");
			// System.exit(-1);
		}
	}
}
