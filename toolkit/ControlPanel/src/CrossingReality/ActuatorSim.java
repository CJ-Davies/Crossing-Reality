package CrossingReality;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Simple test utility to simulate an actuator command being sent from an object in a virtual world to an actuator
 * using the VW-to-Actuator interface and HTTP POST.
 * 
 * @author 060005151
 * @version 15/04/2011
 */
public class ActuatorSim {

	/**
	 * Arguments are as follows;
	 * 
	 * args[0] = address of interface
	 * args[1] = actuator id
	 * args[2] = action
	 * 
	 * @param args
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void main (String [] args) throws MalformedURLException, IOException {

		if (args.length != 3) {
			System.out.println("ActuatorSim usage:\n<address of interface> <actuator id> <action>");
			System.exit(0);
		}
		
		/*
		 * HTTP POST
		 * Concatenating the body to the URL would make this a GET, but using an OutputStreamWriter makes it a POST. Clever little trick!
		 */

		//construct the body of the POST as name/value pairs
		String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(args[1], "UTF-8") + "&" + 
					  URLEncoder.encode("action", "UTF-8") + "=" + URLEncoder.encode(args[2], "UTF-8");

		System.out.println(data);
		
		//send the HTTP POST request with the body created above
		URLConnection conn = (new URL(args[0])).openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();

		//read the response to the request, print it to the terminal
		BufferedReader buf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = buf.readLine()) != null) {
			System.out.println(line);
		}
		wr.close();
		buf.close();
		
	}
}
