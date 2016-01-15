import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;



public class crawler {
	
	public static void main (String[] args) throws Exception {
		while (true) {
			jodelFetcher();
			Thread.sleep(20000);
		}
	}
	
	public static void jodelFetcher() throws Exception {

		try {
			URL url = new URL("https://api.go-tellm.com/api/v2/posts/");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Authorization", "Bearer " + "UUID");
			urlConnection.setRequestMethod("GET");    
		        
		    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    urlConnection.getInputStream()));
		  
		    StringBuilder sb = new StringBuilder();

		    String line = null;
		    while ((line = reader.readLine()) != null)
		    {
		        sb.append(line + "\n");
		    }
		    String result = null;
		    result = sb.toString();
		    
		    reader.close();
		    
		    //System.out.println(result);
		    JSONObject jObject = new JSONObject(result);
		    
		    JSONArray jArray = jObject.getJSONArray("posts");
		    
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        	Date now = new Date();
        	
		    for (int i=0; i < jArray.length(); i++)
		    {  
		        JSONObject oneObject = jArray.getJSONObject(i);
		        // Pulling items from the array
		        String message = oneObject.getString("message");
		        
		        if (message.length() < 140 && message.contains("photo") == false && message.contains("image") == false) {
		        	Date d = sdf.parse(oneObject.getString("created_at"));
		        	if (now.getTime() - d.getTime() < 60 * 1000) {
		        		System.out.println(message);
		        		System.out.println(d);
		        		twitter(message); 
		        	}
		        }
		    }
		    
		    //System.exit(-1);
		    
		   

		} catch (Exception e) {
}
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
                // this will throw IllegalStateException if access token is already available
                RequestToken requestToken = twitter.getOAuthRequestToken();
                System.out.println("Got request token.");
                System.out.println("Request token: " + requestToken.getToken());
                System.out.println("Request token secret: " + requestToken.getTokenSecret());
                AccessToken accessToken = null;

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                while (null == accessToken) {
                    //System.out.println("Open the following URL and grant access to your account:");
                    //System.out.println(requestToken.getAuthorizationURL());
                    //System.out.print("Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
                    String pin = br.readLine();
                    try {
                        if (pin.length() > 0) {
                            accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                        } else {
                            accessToken = twitter.getOAuthAccessToken(requestToken);
                        }
                    } catch (TwitterException te) {
                        if (401 == te.getStatusCode()) {
                            System.out.println("Unable to get the access token.");
                        } else {
                            te.printStackTrace();
                        }
                    }
                }
                System.out.println("Got access token.");
                System.out.println("Access token: " + accessToken.getToken());
                System.out.println("Access token secret: " + accessToken.getTokenSecret());
            } catch (IllegalStateException ie) {
                // access token is already available, or consumer key/secret is not set.
                if (!twitter.getAuthorization().isEnabled()) {
                    System.out.println("OAuth consumer key/secret is not set.");
                    //System.exit(-1);
                }
            }
            Status status = twitter.updateStatus(message);
            System.out.println("Successfully updated the status to [" + status.getText() + "].");
            //System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get timeline: " + te.getMessage());
            //System.exit(-1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Failed to read the system input.");
            //System.exit(-1);
        }
    }


	
}
