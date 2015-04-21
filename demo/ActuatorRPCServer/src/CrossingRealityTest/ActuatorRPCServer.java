package CrossingRealityTest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import com.phidgets.*;

import org.apache.xmlrpc.XmlRpcException;

/**
 * Example implementation of an XML-RPC server to receive commands from the Control Panel as XML-RPC requests, then to act upon
 * these by controlling Phidget relays.
 * @author 060005151
 * @version 15/04/2011
 */
public class ActuatorRPCServer extends MethodServer {

	private static ActuatorRPCServer ARPCS;
	private static InterfaceKitPhidget kit;
	
	/**
	 * Arguments to main are
	 * [0] - Port on which to run the XML-RPC server.
	 * 
	 * @param args
	 */
	public static void main (String [] args) {
		ARPCS = new ActuatorRPCServer();
		
		/*
		 * Set up the XML-RPC server.
		 */
		ARPCS.setupXMLRPCServer(Integer.parseInt(args[0]));
		
		/*
		 * Set up the connection to the Phidget kit.
		 */
		ARPCS.setupPhidgetConnection();
	}

	/**
	 * Runs an XML-RPC server on the given port, adds the methods that are wanted to be made
	 * available for invocation remotely by RPC to the PropertyHandlerMapping.
	 * @param port
	 * @throws XmlRpcException
	 * @throws IOException
	 */
	private void setupXMLRPCServer(int port) {
		try {
			remoteAddHandler("ActuatorRPCServer", ActuatorRPCServer.class);
			start(port);
		} catch (XmlRpcException e) {
			System.out.println("Problem starting XML-RPC server, please turn-it-off-&-on-again.");
			return;
		} catch (IOException e) {
			System.out.println("Problem starting XML-RPC server, please turn-it-off-&-on-again.");
			return;
		}

		try {
			InetAddress address = InetAddress.getLocalHost();	
			System.out.println("XML-RPC server running on " + address + ":" + String.valueOf(port));
		} catch (UnknownHostException e) {
			System.out.println("Could not determine the IP address of the local machine. XML-RPC server may be unreachable.");
		}
	}
	
	/**
	 * Attempts to connect to a connected Phidget kit.
	 */
	private void setupPhidgetConnection() {
		try {
			kit = new InterfaceKitPhidget();
			kit.openAny();
		} catch (PhidgetException e) {
			System.out.println("Could not connect to Phidget kit.");
			return;
		}
	}
	
	/**
	 * Switches relays.
	 * @param id
	 * @param action
	 * @return
	 */
	public boolean control(String id, String action) {
		
		System.out.println("id: " + id);
		System.out.println("action: " + action);
		
		/*
		 * false == on
		 * true == off
		 * No, really!
		 */
		
		/*
		 * id == 1 == blue == output 7
		 * id == 2 == red == output 6
		 */
		
		/*
		 * If id is for the blue light & the action is 'on' then switch on the blue light.
		 * No effect if it is already on.
		 */
		if (Integer.parseInt(id) == 1 && action.equalsIgnoreCase("on")) {
			try {
				ARPCS.kit.setOutputState(7, false);
			} catch (PhidgetException e) {
				System.out.println("Problem switching blue light on.");
				return false;
			}
			System.out.println("Blue light switched on (or already on).");
			return true;
		}
		
		/*
		 * If id is for the blue light & the action is 'off' then switch off the blue light.
		 * No effect if it is already off.
		 */
		else if (Integer.parseInt(id) == 1 && action.equalsIgnoreCase("off")) {
			try {
				ARPCS.kit.setOutputState(7, true);
			} catch (PhidgetException e) {
				System.out.println("Problem switching blue light off.");
				return false;
			}
			System.out.println("Blue light switched off (or already off).");
			return true;
		}
		
		/*
		 * If id is for the red light & the action is 'on' then switch on the red light.
		 * No effect if it is already on.
		 */
		else if (Integer.parseInt(id) == 2 && action.equalsIgnoreCase("on")) {
			try {
				ARPCS.kit.setOutputState(6, false);
			} catch (PhidgetException e) {
				System.out.println("Problem switching red light on.");
				return false;
			}
			System.out.println("Red light switched on (or already on).");
			return true;
		}
		
		/*
		 * If id is for the red light & the action is 'off' then switch off the red light.
		 * No effect if it is already off.
		 */
		else if (Integer.parseInt(id) == 2 && action.equalsIgnoreCase("off")) {
			try {
				ARPCS.kit.setOutputState(6, true);
			} catch (PhidgetException e) {
				System.out.println("Problem switching red light off.");
				return false;
			}
			System.out.println("Red light switched off (or alread off).");
			return true;
		}
		
		return false;
	}

}
