import java.util.*;
import java.sql.*;
import java.io.*;
import java.net.*;

import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

/**
 * Uses mig (Message Interface Generator) to parse packets received over
 * mote-to-pc serial connection from a Basestation mote, which in turn
 * received these packets from PhotoToRadio (a) mote(s) via mote-to-mote
 * radio. Mig automatically parses each of the fields (namely nodeid &
 * photoreading), providing standard accessors & mutators for them.
 * 
 * Forwards the reading on to the Control Panel by XML-RPC.
 * 
 * @author 060005151
 * @version 15/04/2011
 */
 public class PhotoMsgReaderToXMLRPC implements net.tinyos.message.MessageListener {

	/**
	 * "MoteIF provides an application-level Java interface for
	 * receiving messages from, and sending messages to, a mote through
	 * a serial port, TCP connection, or some other means of
	 * connectivity."
	 */
	private MoteIF moteIF;

	/**
	 * Make MsgReader instances.
	 */
	public PhotoMsgReaderToXMLRPC(String source) throws Exception {
		try {
			if (source != null) {
				moteIF = new MoteIF(BuildSource.makePhoenix(source, PrintStreamMessenger.err));
			}
			else {
				moteIF = new MoteIF(BuildSource.makePhoenix(PrintStreamMessenger.err));
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Print for incorrect arguments.
	 */
	private static void usage() {
		System.err.println(
		"usage: PhotoMsgReaderToXMLRPC [-comm <source>]");
	}

	/**
	 * What to do when a message is received (required by the interface)
	 */ 
	public void messageReceived(int to, Message message) {
		try {
			int value = 0;
			int id = 0;
			if (message instanceof PhotoToRadioMsg) {
				value = ((PhotoToRadioMsg)message).get_photoreading();
				id = ((PhotoToRadioMsg)message).get_nodeid();
			}

			/*
			 * Send the reading to the Control Panel via XML-RPC, according to the interface's standard/protocol.
			 */
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL("http://cjd44shproj.cs.st-andrews.ac.uk:8080/"));
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);
	    
			System.out.println("Sending XML-RPC request: ControlPanel.sensed(" + id +", light, " + value +")");

			System.out.println(client.execute("ControlPanel.sensed", new Object[]{new String(Integer.toString(id)), new String("light"), new Integer(value)}));	
		}
		catch (Exception e) {
			System.out.println(e);
		}	
	}

	/**
	 * Used just before call to start();
	 */
	private void addMsgType(Message msg) {
		moteIF.registerListener(msg, this);
	}

	/**
	 * Main.
	 */
	public static void main(String[] args) {
		try {
			String source = null;
			Vector v = new Vector();
			if (args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					if (args[i].equals("-comm")) {
						source = args[++i];
					}
					else {
						String className = args[i];
						try {
							Class c = Class.forName(className);
							Object packet = c.newInstance();
							Message msg = (Message)packet;
							v.addElement(msg);
						}
						catch (Exception e) {
							System.err.println(e);
						}
					}
				}
			}
			else if (args.length != 0) {
				usage();
				System.exit(1);
			}
 
			PhotoMsgReaderToXMLRPC mr = new 
									PhotoMsgReaderToXMLRPC(source);
			Enumeration msgs = v.elements();
			while (msgs.hasMoreElements()) {
				Message m = (Message)msgs.nextElement();
				mr.addMsgType(m);
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
	/**
	 * EOMain.
	 */
	 
}
