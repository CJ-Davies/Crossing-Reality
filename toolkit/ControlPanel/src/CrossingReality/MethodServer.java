package CrossingReality;

import java.io.IOException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

/**
 * An abstract XML-RPC server, designed to be extended by a class with methods it wishes
 * to make available via XML-RPC.
 * @author 060005151
 * @version 26/03/2010 modified 15/04/2011
 */
public abstract class MethodServer {

	private int port;		//the port that this MethodServer binds to
	private PropertyHandlerMapping phm = new PropertyHandlerMapping();		//mapping of methods offered via XML-RPC by this MethodServer

	/**
	 * Starts the server on a port defined by the invoking call.
	 * @throws IOException
	 * @throws XmlRpcException 
	 */
	public void start(int givenPort) throws IOException, XmlRpcException {
		port = givenPort;
		WebServer webServer = new WebServer(port);
		XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
		xmlRpcServer.setHandlerMapping(phm);

		XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
		serverConfig.setEnabledForExtensions(true);
		serverConfig.setContentLengthOptional(false);

		webServer.start();
		System.out.println("MethodServer running on " + webServer.getPort() + "...");
	}

	/**
	 * Returns a String that represents a nicely formatted representation of methods that have
	 * been registered with this instance of the server, intended to be printed to the console
	 * for human reading.
	 * @return
	 * @throws XmlRpcException
	 */
	public String printAvailableMethods() throws XmlRpcException {
		String result = new String("\nAvailable methods on " + port + "\n");
		String[] objects = phm.getListMethods();
		if (objects.length == 0) {
			result = result + "> No methods available!\n";
		}
		else {
			for (int i = 0; i < objects.length; i++) {
				result = result + "> " + objects[i] + "\n";
			}
		}
		return result;
	}

	/**
	 * Allows a MethodServer to add a method to its PropertyHandlerMapping for offering via XML-RPC.
	 * @param pKey
	 * @param pClass
	 * @throws XmlRpcException
	 */
	@SuppressWarnings("rawtypes")
	public void remoteAddHandler(String pKey, Class pClass) throws XmlRpcException {
		phm.addHandler(pKey, pClass);
		System.out.println("Added " + pKey + " to the mapping at " + pClass.getCanonicalName()
				+ ".");
	}

}