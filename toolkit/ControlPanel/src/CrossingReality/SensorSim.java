package CrossingReality;

import java.io.IOException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

/**
 * Simple test utility to simulate a sensor reading being sent from a wireless sensor network node to a virtual world
 * using the WSN-to-VW interface and XML-RPC.
 * 
 * @author 060005151
 * @version 15/04/2011
 */
public class SensorSim {

	/**
	 * Arguments are as follows;
	 * 
	 * args[0] = address of interface
	 * args[1] = node id
	 * args[2] = sensor type
	 * args[3] = value 
	 * 
	 * @param args
	 * @throws IOException
	 * @throws XmlRpcException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, XmlRpcException, InterruptedException {

		if (args.length != 4) {
			System.out.println("SensorSim usage:\n<address of interface> <node id> <sensor type> <value>");
			System.exit(0);
		}
		
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(args[0]));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);

		Boolean result = (Boolean)client.execute("ControlPanel.sensed", new Object[]{new String(args[1]), new String(args[2]), new Integer(Integer.parseInt(args[3]))});
		System.out.println(result);
	}

}
