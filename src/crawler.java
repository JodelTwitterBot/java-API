import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;



public class crawler {
	
	public static void main(String[] args) throws Exception {

		try {
			URL url = new URL("https://api.go-tellm.com/api/v2/posts/");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Authorization", "Bearer " + "507ecd8a-79fa-425a-9249-777e88c740a6");
			urlConnection.setRequestMethod("GET");    
		        
		    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    urlConnection.getInputStream()));
		    String inputLine;
		  
		    StringBuilder sb = new StringBuilder();

		    String line = null;
		    while ((line = reader.readLine()) != null)
		    {
		        sb.append(line + "\n");
		    }
		    String result = null;
		    result = sb.toString();
		    
		    reader.close();
		    
		    System.out.println(result);
		    JSONObject jObject = new JSONObject(result);
		    
		    //experimentCount = jObject.getInt("tot");
		    JSONArray jArray = jObject.getJSONArray("posts");
		    
		    
		    
		    for (int i=0; i < jArray.length(); i++)
		    {  
		        JSONObject oneObject = jArray.getJSONObject(i);
		        // Pulling items from the array
		        String tempExpString = oneObject.getString("message");  
		        System.out.println(tempExpString);
		    }

		} catch (Exception e) {
}
	}

	
}
