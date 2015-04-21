package CrossingReality;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * This is the central application of the 'Crossing Reality' toolkit for creating cross-reality environments leveraging wireless sensor networks,
 * actuators and virtual worlds. This application combines the GUI, database connectivity and the 2 interfaces, WSN-to-VW and VW-to-Actuators that
 * make up the toolkit.
 * 
 * This class extends the MethodServer class, which implements much of the common functionality and required mappings of an XML-RPC server.
 * 
 * Arguments to the main method are;
 * [0] - the port that the XML-RPC server runs on (this forms part of the WSN-to-VW interface)
 * [1] - the port that the HTTP server runs on (this forms part of the VW-t0-ACtuator interface)
 * 
 * For further information about the design, implementation and usage of this application, please see the accompanying report, in particular
 * Appendix C (User Manual). 
 *
 * @author 060005151
 * @version 15/04/2011
 */
@SuppressWarnings("restriction")
public class ControlPanel extends MethodServer implements ItemListener, ActionListener {

	static ControlPanel cp;
	static String mysqlAddress, mysqlUser, mysqlPass;

	String dateFormat = "HH:mm:ss";

	/*
	 * Global variables for GUI components, so that the values they contain are accessible from anywhere within the class.
	 */
	JTextArea wsnGeneralPanelOutput, wsnSensorsPanelOutput, actuatorPanelOutput;
	JButton wsnGeneralUpdateMysqlAddressButton, wsnGeneralUpdateNodeAddressButton, wsnGeneralSaveMysqlDetailsButton,
	wsnGeneralManualNodeAddButton, wsnGeneralRemoveNodeButton, wsnSensorsManualAddSensorButton, wsnSensorsRemoveSensorButton,
	wsnSensorsCalibrateResetButton, wsnGeneralRefreshNodeAddressButton, actuatorRefreshActuatorAddressButton,
	actuatorUpdateActuatorAddressButton, actuatorManualActuatorAddButton, actuatorRemoveActuatorButton;
	JTextField wsnGeneralMysqlAddressField, wsnGeneralMysqlUserField, wsnGeneralNodeAddressField, wsnGeneralWsnInterfaceAddressField,
	wsnGeneralManualNodeAddId, wsnGeneralManualNodeAddAddress, wsnSensorsNodeAddressField, wsnSensorsManualSensorAddType,
	wsnSensorsCalibrationMinField, wsnSensorsCalibrationMaxField, actuatorInterfaceAddressField, actuatorAddressField, actuatorManualActuatorAddId,
	actuatorManualActuatorAddAddress;
	JPasswordField wsnGeneralMysqlPassField;
	JCheckBox wsnGeneralAutoAddCheck, wsnGeneralRemoveNodeIgnoreCheck, wsnGeneralPauseWsnToVwCheck, wsnSensorsAutoAddCheck,
	wsnSensorsRemoveSensorIgnoreCheck, wsnSensorsCalibrateCheck, actuatorAutoAddCheck, actuatorPauseVwToActuatorCheck;
	JComboBox wsnGeneralNodeComboBox, wsnGeneralRemoveNodeComboBox, wsnSensorsNodeComboBox, wsnSensorsSensorsComboBox, actuatorActuatorComboBox,
	actuatorRemoveActuatorComboBox;
	ArrayList<String> ignoreNodeList;
	ArrayList<SensorIgnoreIdTypeObject> ignoreSensorList;

	/**
	 * Arguments to the main method are;
	 * [0] - the port that the XML-RPC server runs on (this forms part of the WSN-to-VW interface)
	 * [1] - the port that the HTTP server runs on (this forms part of the VW-t0-ACtuator interface)
	 * 
	 * For further information about the design, implementation and usage of this application, please see the accompanying report, in particular
	 * Appendix C (User Manual). 
	 * 
	 * @param args
	 * @throws XmlRpcException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws XmlRpcException, IOException, InterruptedException {
		cp = new ControlPanel();
		cp.setupGUI();
		cp.setupXMLRPCServer(Integer.parseInt(args[0]));
		cp.setupHTTPServer(Integer.parseInt(args[1]));
		cp.loadMysqlDetails();
		cp.ignoreNodeList = new ArrayList<String>();
		cp.ignoreSensorList = new ArrayList<SensorIgnoreIdTypeObject>();
	}

	/**
	 * Sets up the GUI.
	 */
	private void setupGUI() {

		/*
		 * Entire frame, undivided.
		 */
		JFrame frame = new JFrame("Crossing Reality Control Panel");
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(50, 50);

		/*
		 * Use tabs to logically divide up the GUI into its constituent sections.
		 */
		JTabbedPane tabbedPane = new JTabbedPane();
		frame.add(tabbedPane);

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* ******* ******* Nodes/General Tab ******* ******* ******* ******* *****
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 */
		JPanel wsnGeneralPanel = new JPanel(new GridLayout(3, 1));
		tabbedPane.addTab("WSN General/Node Management", wsnGeneralPanel);

		/*
		 * Entire panel is divided into 3 rows.
		 */
		JPanel wsnGeneralPanelTopPanel = new JPanel(new GridLayout(1, 3));
		JPanel wsnGeneralPanelMiddlePanel = new JPanel(new GridLayout(1, 3));
		JScrollPane wsnGeneralPanelBottomPanel = new JScrollPane();
		wsnGeneralPanel.add(wsnGeneralPanelTopPanel);
		wsnGeneralPanel.add(wsnGeneralPanelMiddlePanel);
		wsnGeneralPanel.add(wsnGeneralPanelBottomPanel);

		/*
		 * Top row is divided into 3 columns.
		 */
		JPanel wsnGeneralPanelTopPanelLeftPanel = new JPanel(new GridLayout(2, 1));
		JPanel wsnGeneralPanelTopPanelMiddlePanel = new JPanel(new GridLayout(8, 1));
		JPanel wsnGeneralPanelTopPanelRightPanel = new JPanel(new GridLayout(4, 1));
		wsnGeneralPanelTopPanel.add(wsnGeneralPanelTopPanelLeftPanel);
		wsnGeneralPanelTopPanel.add(wsnGeneralPanelTopPanelMiddlePanel);
		wsnGeneralPanelTopPanel.add(wsnGeneralPanelTopPanelRightPanel);

		/*
		 * Top row, left column is divided into 2 rows.
		 */
		JPanel wsnGeneralPanelTopPanelLeftPanelTopPanel = new JPanel();
		JPanel wsnGeneralPanelTopPanelLeftPanelBottomPanel = new JPanel();
		wsnGeneralPanelTopPanelLeftPanel.add(wsnGeneralPanelTopPanelLeftPanelTopPanel);
		wsnGeneralPanelTopPanelLeftPanel.add(wsnGeneralPanelTopPanelLeftPanelBottomPanel);

		/*
		 * Top row, left column, top row.
		 */
		TitledBorder wsnGeneralPanelTopPanelLeftPanelTopPanelTitle = BorderFactory.createTitledBorder("Address of WSN interface");
		wsnGeneralPanelTopPanelLeftPanelTopPanel.setBorder(wsnGeneralPanelTopPanelLeftPanelTopPanelTitle);
		wsnGeneralWsnInterfaceAddressField = new JTextField("");
		wsnGeneralWsnInterfaceAddressField.setEditable(false);
		wsnGeneralPanelTopPanelLeftPanelTopPanel.add(wsnGeneralWsnInterfaceAddressField);

		/*
		 * Top row, left column, bottom row.
		 */
		TitledBorder wsnGeneralPanelTopPanelLeftPanelBottomPanelTitle = BorderFactory.createTitledBorder("Auto add new nodes");
		wsnGeneralPanelTopPanelLeftPanelBottomPanel.setBorder(wsnGeneralPanelTopPanelLeftPanelBottomPanelTitle);
		wsnGeneralAutoAddCheck = new JCheckBox("Automatically add new nodes?", false);
		wsnGeneralPanelTopPanelLeftPanelBottomPanel.add(wsnGeneralAutoAddCheck);

		/*
		 * Top row, middle column.
		 */
		TitledBorder wsnGeneralPanelTopPanelMiddlePanelTitle = BorderFactory.createTitledBorder("MySQL database details");
		wsnGeneralPanelTopPanelMiddlePanel.setBorder(wsnGeneralPanelTopPanelMiddlePanelTitle);
		JLabel mysqlAddressFieldLabel = new JLabel("Address (domain:port/database)");
		JLabel mysqlUserFieldLabel = new JLabel("Username");
		JLabel mysqlPassFieldLabel = new JLabel("Password");
		wsnGeneralMysqlAddressField = new JTextField("");
		wsnGeneralMysqlUserField = new JTextField("");
		wsnGeneralMysqlPassField = new JPasswordField("");
		wsnGeneralUpdateMysqlAddressButton = new JButton("Update details & (re)connect.");
		wsnGeneralUpdateMysqlAddressButton.addActionListener(this);
		wsnGeneralSaveMysqlDetailsButton = new JButton("Save details.");
		wsnGeneralSaveMysqlDetailsButton.addActionListener(this);
		wsnGeneralPanelTopPanelMiddlePanel.add(mysqlAddressFieldLabel);
		wsnGeneralPanelTopPanelMiddlePanel.add(wsnGeneralMysqlAddressField);
		wsnGeneralPanelTopPanelMiddlePanel.add(mysqlUserFieldLabel);
		wsnGeneralPanelTopPanelMiddlePanel.add(wsnGeneralMysqlUserField);
		wsnGeneralPanelTopPanelMiddlePanel.add(mysqlPassFieldLabel);
		wsnGeneralPanelTopPanelMiddlePanel.add(wsnGeneralMysqlPassField);
		wsnGeneralPanelTopPanelMiddlePanel.add(wsnGeneralUpdateMysqlAddressButton);
		wsnGeneralPanelTopPanelMiddlePanel.add(wsnGeneralSaveMysqlDetailsButton);

		/*
		 * Top row, right column.
		 */
		TitledBorder wsnGeneralPanelTopPanelRightPanelTitle = BorderFactory.createTitledBorder("Node addresses");
		wsnGeneralPanelTopPanelRightPanel.setBorder(wsnGeneralPanelTopPanelRightPanelTitle);
		wsnGeneralNodeComboBox = new JComboBox();
		wsnGeneralNodeComboBox.addItemListener(this);
		wsnGeneralPanelTopPanelRightPanel.add(wsnGeneralNodeComboBox);
		wsnGeneralNodeAddressField = new JTextField();
		wsnGeneralPanelTopPanelRightPanel.add(wsnGeneralNodeAddressField);
		wsnGeneralRefreshNodeAddressButton = new JButton("Refresh node address.");
		wsnGeneralPanelTopPanelRightPanel.add(wsnGeneralRefreshNodeAddressButton);
		wsnGeneralRefreshNodeAddressButton.addActionListener(this);
		wsnGeneralUpdateNodeAddressButton = new JButton("Save node address.");
		wsnGeneralUpdateNodeAddressButton.addActionListener(this);
		wsnGeneralPanelTopPanelRightPanel.add(wsnGeneralUpdateNodeAddressButton);

		/*
		 * Middle row is divided into 3 columns.
		 */
		JPanel wsnGeneralPanelMiddlePanelLeftPanel = new JPanel(new GridLayout(5, 1));
		JPanel wsnGeneralPanelMiddlePanelMiddlePanel = new JPanel(new GridLayout(3, 1));
		JPanel wsnGeneralPanelMiddlePanelRightPanel = new JPanel(new GridLayout(2, 1));
		wsnGeneralPanelMiddlePanel.add(wsnGeneralPanelMiddlePanelLeftPanel);
		wsnGeneralPanelMiddlePanel.add(wsnGeneralPanelMiddlePanelMiddlePanel);
		wsnGeneralPanelMiddlePanel.add(wsnGeneralPanelMiddlePanelRightPanel);

		/*
		 * Middle row, left column.
		 */
		TitledBorder wsnGeneralPanelMiddlePanelLeftPanelTitle = BorderFactory.createTitledBorder("Manually add node");
		wsnGeneralPanelMiddlePanelLeftPanel.setBorder(wsnGeneralPanelMiddlePanelLeftPanelTitle);
		JLabel manualNodeAddIdLabel = new JLabel("Id");
		wsnGeneralManualNodeAddId = new JTextField("");
		JLabel manualNodeAddAddressLabel = new JLabel("Address");
		wsnGeneralManualNodeAddAddress = new JTextField("");
		wsnGeneralManualNodeAddButton = new JButton("Add node");
		wsnGeneralManualNodeAddButton.addActionListener(this);
		wsnGeneralPanelMiddlePanelLeftPanel.add(manualNodeAddIdLabel);
		wsnGeneralPanelMiddlePanelLeftPanel.add(wsnGeneralManualNodeAddId);
		wsnGeneralPanelMiddlePanelLeftPanel.add(manualNodeAddAddressLabel);
		wsnGeneralPanelMiddlePanelLeftPanel.add(wsnGeneralManualNodeAddAddress);
		wsnGeneralPanelMiddlePanelLeftPanel.add(wsnGeneralManualNodeAddButton);

		/*
		 * Middle row, middle column.
		 */
		TitledBorder wsnGeneralPanelMiddlePanelMiddlePanelTitle = BorderFactory.createTitledBorder("Remove node");
		wsnGeneralPanelMiddlePanelMiddlePanel.setBorder(wsnGeneralPanelMiddlePanelMiddlePanelTitle);
		wsnGeneralRemoveNodeComboBox = new JComboBox();
		wsnGeneralRemoveNodeComboBox.addItemListener(this);
		wsnGeneralPanelMiddlePanelMiddlePanel.add(wsnGeneralRemoveNodeComboBox);
		wsnGeneralRemoveNodeIgnoreCheck = new JCheckBox("Prevent auto add (this session)?");
		wsnGeneralPanelMiddlePanelMiddlePanel.add(wsnGeneralRemoveNodeIgnoreCheck);
		wsnGeneralRemoveNodeButton = new JButton("Remove");
		wsnGeneralRemoveNodeButton.addActionListener(this);
		wsnGeneralPanelMiddlePanelMiddlePanel.add(wsnGeneralRemoveNodeButton);

		/*
		 * Middle row, right column is divided into 2 rows.
		 */
		JPanel wsnGeneralPanelMiddlePanelRightPanelTopPanel = new JPanel();
		JPanel wsnGeneralPanelMiddlePanelRightPanelBottomPanel = new JPanel();
		wsnGeneralPanelMiddlePanelRightPanel.add(wsnGeneralPanelMiddlePanelRightPanelTopPanel);
		wsnGeneralPanelMiddlePanelRightPanel.add(wsnGeneralPanelMiddlePanelRightPanelBottomPanel);

		/*
		 * Middle row, right column.
		 */
		TitledBorder wsnGeneralPanelMiddlePanelRightPanelTopPanelTitle = BorderFactory.createTitledBorder("Pause WSN-to-VW forwarding");
		wsnGeneralPanelMiddlePanelRightPanelTopPanel.setBorder(wsnGeneralPanelMiddlePanelRightPanelTopPanelTitle);
		wsnGeneralPauseWsnToVwCheck = new JCheckBox("Pause?");
		wsnGeneralPanelMiddlePanelRightPanelTopPanel.add(wsnGeneralPauseWsnToVwCheck);

		/*
		 * Bottom row.
		 */
		TitledBorder wsnGeneralPanelBottomPanelWholePanelTitle = BorderFactory.createTitledBorder("Terminal");
		wsnGeneralPanelBottomPanel.setBorder(wsnGeneralPanelBottomPanelWholePanelTitle);
		wsnGeneralPanelOutput = new JTextArea();
		wsnGeneralPanelOutput.setEditable(false);	
		wsnGeneralPanelBottomPanel.getViewport().add(wsnGeneralPanelOutput);

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* ******* ******* Sensors Tab ******* ******* ******* ******* ******* ***
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 */
		JPanel wsnSensorsPanel = new JPanel(new GridLayout(3, 1));
		tabbedPane.addTab("WSN General", wsnSensorsPanel);
		tabbedPane.addTab("WSN Sensor Management", wsnSensorsPanel);

		/*
		 * Entire panel is divided into 3 rows.
		 */
		JPanel wsnSensorsPanelTopPanel = new JPanel(new GridLayout(1, 3));
		JPanel wsnSensorsPanelMiddlePanel = new JPanel(new GridLayout(1, 3));
		JScrollPane wsnSensorsPanelBottomPanel = new JScrollPane();
		wsnSensorsPanel.add(wsnSensorsPanelTopPanel);
		wsnSensorsPanel.add(wsnSensorsPanelMiddlePanel);
		wsnSensorsPanel.add(wsnSensorsPanelBottomPanel);

		/*
		 * Top row is divided into 3 columns.
		 */
		JPanel wsnSensorsPanelTopPanelLeftPanel = new JPanel(new GridLayout(2, 1));
		JPanel wsnSensorsPanelTopPanelMiddlePanel = new JPanel(new GridLayout(3, 1));
		JPanel wsnSensorsPanelTopPanelRightPanel = new JPanel(new GridLayout(3, 1));
		wsnSensorsPanelTopPanel.add(wsnSensorsPanelTopPanelLeftPanel);
		wsnSensorsPanelTopPanel.add(wsnSensorsPanelTopPanelMiddlePanel);
		wsnSensorsPanelTopPanel.add(wsnSensorsPanelTopPanelRightPanel);

		/*
		 * Top row, left column is divided into 2 rows.
		 */
		JPanel wsnSensorsPanelTopPanelLeftPanelTopPanel = new JPanel(new GridLayout(2, 1));
		JPanel wsnSensorsPanelTopPanelLeftPanelBottomPanel = new JPanel();
		wsnSensorsPanelTopPanelLeftPanel.add(wsnSensorsPanelTopPanelLeftPanelTopPanel);
		wsnSensorsPanelTopPanelLeftPanel.add(wsnSensorsPanelTopPanelLeftPanelBottomPanel);

		/*
		 * Top row, left column, top row.
		 */
		TitledBorder wsnSensorsPanelTopPanelLeftPanelTopPanelTitle = BorderFactory.createTitledBorder("Select node");
		wsnSensorsPanelTopPanelLeftPanelTopPanel.setBorder(wsnSensorsPanelTopPanelLeftPanelTopPanelTitle);
		wsnSensorsNodeComboBox = new JComboBox();
		wsnSensorsNodeComboBox.addItemListener(this);
		wsnSensorsPanelTopPanelLeftPanelTopPanel.add(wsnSensorsNodeComboBox);
		wsnSensorsNodeAddressField = new JTextField();
		wsnSensorsNodeAddressField.setEditable(false);
		wsnSensorsPanelTopPanelLeftPanelTopPanel.add(wsnSensorsNodeAddressField);

		/*
		 * Top row, left column, bottom row.
		 */
		TitledBorder wsnSensorsPanelTopPanelLeftPanelBottomPanelTitle = BorderFactory.createTitledBorder("Auto add new sensors");
		wsnSensorsPanelTopPanelLeftPanelBottomPanel.setBorder(wsnSensorsPanelTopPanelLeftPanelBottomPanelTitle);
		wsnSensorsAutoAddCheck = new JCheckBox("Automatically add new sensors?");
		wsnSensorsPanelTopPanelLeftPanelBottomPanel.add(wsnSensorsAutoAddCheck);

		/*
		 * Top row, middle column.
		 */
		TitledBorder wsnSensorsPanelTopPanelMiddlePanelTitle = BorderFactory.createTitledBorder("Manually add sensor for node");
		wsnSensorsPanelTopPanelMiddlePanel.setBorder(wsnSensorsPanelTopPanelMiddlePanelTitle);
		JLabel wsnSensorsManualSensorAddTypeLabel = new JLabel("Type");
		wsnSensorsPanelTopPanelMiddlePanel.add(wsnSensorsManualSensorAddTypeLabel);
		wsnSensorsManualSensorAddType = new JTextField();
		wsnSensorsPanelTopPanelMiddlePanel.add(wsnSensorsManualSensorAddType);
		wsnSensorsManualAddSensorButton = new JButton("Add");
		wsnSensorsManualAddSensorButton.addActionListener(this);
		wsnSensorsPanelTopPanelMiddlePanel.add(wsnSensorsManualAddSensorButton);

		/*
		 * Top row, right column.
		 */
		TitledBorder wsnSensorsPanelTopPanelRightPanelTitle = BorderFactory.createTitledBorder("Remove sensor from node");
		wsnSensorsPanelTopPanelRightPanel.setBorder(wsnSensorsPanelTopPanelRightPanelTitle );
		wsnSensorsSensorsComboBox = new JComboBox();
		wsnSensorsSensorsComboBox.addItemListener(this);
		wsnSensorsPanelTopPanelRightPanel.add(wsnSensorsSensorsComboBox);
		wsnSensorsRemoveSensorIgnoreCheck = new JCheckBox("Prevent auto add (this session)?");
		wsnSensorsPanelTopPanelRightPanel.add(wsnSensorsRemoveSensorIgnoreCheck);
		wsnSensorsRemoveSensorButton = new JButton("Remove");
		wsnSensorsRemoveSensorButton.addActionListener(this);
		wsnSensorsPanelTopPanelRightPanel.add(wsnSensorsRemoveSensorButton);

		/*
		 * Middle row is divided into 3 columns.
		 */
		JPanel wsnSensorsPanelMiddlePanelLeftPanel = new JPanel(new GridLayout(6, 1));
		JPanel wsnSensorsPanelMiddlePanelMiddlePanel = new JPanel(new GridLayout(3, 1));
		JPanel wsnSensorsPanelMiddlePanelRightPanel = new JPanel(new GridLayout(3, 1));
		wsnSensorsPanelMiddlePanel.add(wsnSensorsPanelMiddlePanelLeftPanel);
		wsnSensorsPanelMiddlePanel.add(wsnSensorsPanelMiddlePanelMiddlePanel);
		wsnSensorsPanelMiddlePanel.add(wsnSensorsPanelMiddlePanelRightPanel);

		/*
		 * Middle row, left column.
		 */
		TitledBorder wsnSensorsPanelMiddlePanelLeftPanelTitle = BorderFactory.createTitledBorder("Calibrate sensor");
		wsnSensorsPanelMiddlePanelLeftPanel.setBorder(wsnSensorsPanelMiddlePanelLeftPanelTitle);
		wsnSensorsCalibrateCheck = new JCheckBox("Calibrate?");
		wsnSensorsPanelMiddlePanelLeftPanel.add(wsnSensorsCalibrateCheck);
		JLabel wsnSensorsCalibrationMinLabel = new JLabel("Minimum reading");
		wsnSensorsPanelMiddlePanelLeftPanel.add(wsnSensorsCalibrationMinLabel);
		wsnSensorsCalibrationMinField = new JTextField();
		wsnSensorsCalibrationMinField.setEditable(false);
		wsnSensorsPanelMiddlePanelLeftPanel.add(wsnSensorsCalibrationMinField);
		wsnSensorsCalibrationMaxField = new JTextField();
		wsnSensorsCalibrationMaxField.setEditable(false);
		JLabel wsnSensorsCalibrationMaxLabel = new JLabel("Maximum reading");
		wsnSensorsPanelMiddlePanelLeftPanel.add(wsnSensorsCalibrationMaxLabel);
		wsnSensorsPanelMiddlePanelLeftPanel.add(wsnSensorsCalibrationMaxField);
		wsnSensorsCalibrateResetButton = new JButton("Reset");
		wsnSensorsCalibrateResetButton.addActionListener(this);
		wsnSensorsPanelMiddlePanelLeftPanel.add(wsnSensorsCalibrateResetButton);

		/*
		 * Bottom row.
		 */
		TitledBorder wsnSensorsPanelBottomPanelWholePanelTitle = BorderFactory.createTitledBorder("Terminal");
		wsnSensorsPanelBottomPanel.setBorder(wsnSensorsPanelBottomPanelWholePanelTitle);
		wsnSensorsPanelOutput = new JTextArea();
		wsnSensorsPanelOutput.setEditable(false);	
		wsnSensorsPanelBottomPanel.getViewport().add(wsnSensorsPanelOutput);

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* ******* **** Actuators Tab **** ******* ******* ******* ******* *******
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 */
		JPanel actuatorPanel = new JPanel(new GridLayout(3, 1));

		tabbedPane.addTab("Actuator Management", actuatorPanel);

		/*
		 * Entire panel is divided into 3 rows.
		 */
		JPanel actuatorPanelTopPanel = new JPanel(new GridLayout(1, 3));
		JPanel actuatorPanelMiddlePanel = new JPanel(new GridLayout(1, 3));
		JScrollPane actuatorPanelBottomPanel = new JScrollPane();
		actuatorPanel.add(actuatorPanelTopPanel);
		actuatorPanel.add(actuatorPanelMiddlePanel);
		actuatorPanel.add(actuatorPanelBottomPanel);

		/*
		 * Top row is divided into 3 columns.
		 */
		JPanel actuatorPanelTopPanelLeftPanel = new JPanel(new GridLayout(2, 1));
		JPanel actuatorPanelTopPanelMiddlePanel = new JPanel(new GridLayout(5, 1));
		JPanel actuatorPanelTopPanelRightPanel = new JPanel(new GridLayout(4, 1));
		actuatorPanelTopPanel.add(actuatorPanelTopPanelLeftPanel);
		actuatorPanelTopPanel.add(actuatorPanelTopPanelMiddlePanel);
		actuatorPanelTopPanel.add(actuatorPanelTopPanelRightPanel);

		/*
		 * Top row, left column is divided into 2 rows.
		 */
		JPanel actuatorPanelTopPanelLeftPanelTopPanel = new JPanel();
		JPanel actuatorPanelTopPanelLeftPanelBottomPanel = new JPanel();
		actuatorPanelTopPanelLeftPanel.add(actuatorPanelTopPanelLeftPanelTopPanel);
		actuatorPanelTopPanelLeftPanel.add(actuatorPanelTopPanelLeftPanelBottomPanel);

		/*
		 * Top row, left column, top row.
		 */
		TitledBorder actuatorPanelTopPanelLeftPanelTopPanelTitle = BorderFactory.createTitledBorder("Address of actuator interface");
		actuatorPanelTopPanelLeftPanelTopPanel.setBorder(actuatorPanelTopPanelLeftPanelTopPanelTitle);
		actuatorInterfaceAddressField = new JTextField("");
		actuatorInterfaceAddressField.setEditable(false);
		actuatorPanelTopPanelLeftPanelTopPanel.add(actuatorInterfaceAddressField);

		/*
		 * Top row, left column, bottom row.
		 */
		TitledBorder actuatorPanelTopPanelLeftPanelBottomPanelTitle = BorderFactory.createTitledBorder("Pause VW-to-Actuator forwarding");
		actuatorPanelTopPanelLeftPanelBottomPanel.setBorder(actuatorPanelTopPanelLeftPanelBottomPanelTitle);
		actuatorPauseVwToActuatorCheck = new JCheckBox("Pause?", false);
		actuatorPanelTopPanelLeftPanelBottomPanel.add(actuatorPauseVwToActuatorCheck);

		/*
		 * Top row, middle column.
		 */
		TitledBorder actuatorPanelTopPanelMiddlePanelTitle = BorderFactory.createTitledBorder("Manually add actuator");
		actuatorPanelTopPanelMiddlePanel.setBorder(actuatorPanelTopPanelMiddlePanelTitle);
		JLabel manualActuatorAddIdLabel = new JLabel("Id");
		actuatorManualActuatorAddId = new JTextField("");
		JLabel manualActuatorAddAddressLabel = new JLabel("Address");
		actuatorManualActuatorAddAddress = new JTextField("");
		actuatorManualActuatorAddButton = new JButton("Add actuator");
		actuatorManualActuatorAddButton.addActionListener(this);
		actuatorPanelTopPanelMiddlePanel.add(manualActuatorAddIdLabel);
		actuatorPanelTopPanelMiddlePanel.add(actuatorManualActuatorAddId);
		actuatorPanelTopPanelMiddlePanel.add(manualActuatorAddAddressLabel);
		actuatorPanelTopPanelMiddlePanel.add(actuatorManualActuatorAddAddress);
		actuatorPanelTopPanelMiddlePanel.add(actuatorManualActuatorAddButton);

		/*
		 * Top row, right column.
		 */
		TitledBorder actuatorPanelTopPanelRightPanelTitle = BorderFactory.createTitledBorder("Actuator addresses");
		actuatorPanelTopPanelRightPanel.setBorder(actuatorPanelTopPanelRightPanelTitle);
		actuatorActuatorComboBox = new JComboBox();
		actuatorActuatorComboBox.addItemListener(this);
		actuatorPanelTopPanelRightPanel.add(actuatorActuatorComboBox);
		actuatorAddressField = new JTextField();
		actuatorPanelTopPanelRightPanel.add(actuatorAddressField);
		actuatorRefreshActuatorAddressButton = new JButton("Refresh actuator address.");
		actuatorPanelTopPanelRightPanel.add(actuatorRefreshActuatorAddressButton);
		actuatorRefreshActuatorAddressButton.addActionListener(this);
		actuatorUpdateActuatorAddressButton = new JButton("Save actuator address.");
		actuatorUpdateActuatorAddressButton.addActionListener(this);
		actuatorPanelTopPanelRightPanel.add(actuatorUpdateActuatorAddressButton);

		/*
		 * Middle row is divided into 3 columns.
		 */
		JPanel actuatorPanelMiddlePanelLeftPanel = new JPanel(new GridLayout(2, 1));
		JPanel actuatorPanelMiddlePanelMiddlePanel = new JPanel(new GridLayout(1, 1));
		JPanel actuatorPanelMiddlePanelRightPanel = new JPanel(new GridLayout(1, 1));
		actuatorPanelMiddlePanel.add(actuatorPanelMiddlePanelLeftPanel);
		actuatorPanelMiddlePanel.add(actuatorPanelMiddlePanelMiddlePanel);
		actuatorPanelMiddlePanel.add(actuatorPanelMiddlePanelRightPanel);

		/*
		 * Middle row, left column.
		 */
		TitledBorder actuatorPanelMiddlePanelLeftPanelTitle = BorderFactory.createTitledBorder("Remove actuator");
		actuatorPanelMiddlePanelLeftPanel.setBorder(actuatorPanelMiddlePanelLeftPanelTitle);
		actuatorRemoveActuatorComboBox = new JComboBox();
		actuatorRemoveActuatorComboBox.addItemListener(this);
		actuatorPanelMiddlePanelLeftPanel.add(actuatorRemoveActuatorComboBox);
		actuatorRemoveActuatorButton = new JButton("Remove");
		actuatorRemoveActuatorButton.addActionListener(this);
		actuatorPanelMiddlePanelLeftPanel.add(actuatorRemoveActuatorButton);

		/*
		 * Bottom row.
		 */
		TitledBorder actuatorPanelBottomPanelWholePanelTitle = BorderFactory.createTitledBorder("Terminal");
		actuatorPanelBottomPanel.setBorder(actuatorPanelBottomPanelWholePanelTitle);
		actuatorPanelOutput = new JTextArea();
		actuatorPanelOutput.setEditable(false);	
		actuatorPanelBottomPanel.getViewport().add(actuatorPanelOutput);

		/*
		 * Make the GUI visible(!).
		 */
		frame.setVisible(true);
	}

	/**
	 * Used to add a line of output to the terminal(s) of the GUI, better than sending to System.out as this displays
	 * in the GUI. Timestamps are added to the beginning of each line, using the globally defined date format.
	 * @param toAppend
	 */
	private void appendToOutput(String toAppend) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		cp.wsnGeneralPanelOutput.append("\n[" + sdf.format(cal.getTime()) + "] " + toAppend);
		cp.wsnGeneralPanelOutput.setCaretPosition(wsnGeneralPanelOutput.getDocument().getLength());
		cp.wsnSensorsPanelOutput.append("\n[" + sdf.format(cal.getTime()) + "] " + toAppend);
		cp.wsnSensorsPanelOutput.setCaretPosition(wsnGeneralPanelOutput.getDocument().getLength());
		cp.actuatorPanelOutput.append("\n[" + sdf.format(cal.getTime()) + "] " + toAppend);
		cp.actuatorPanelOutput.setCaretPosition(wsnGeneralPanelOutput.getDocument().getLength());
	}

	/**
	 * Runs an XML-RPC server on the given port, adds the methods that are wanted to be made
	 * available for invocation remotely by RPC to the PropertyHandlerMapping.
	 * 
	 * This XML-RPC server is responsible for receiving reports of sensor readings from the WSN
	 * as part of the WSN-to-VW interface.
	 * @param port
	 * @throws XmlRpcException
	 * @throws IOException
	 */
	private void setupXMLRPCServer(int port) {
		try {
			remoteAddHandler("ControlPanel", ControlPanel.class);
			start(port);
		} catch (XmlRpcException e) {
			appendToOutput("Problem starting XML-RPC server, please close the Control Panel & re-open it.");
			return;
		} catch (IOException e) {
			appendToOutput("Problem starting XML-RPC server, please close the Control Panel & re-open it.");
			return;
		}

		/*
		 * Display the address & port that the XML-RPC server is running upon in the relevant section of the GUI.
		 */
		try {
			InetAddress address = InetAddress.getLocalHost();	
			wsnGeneralWsnInterfaceAddressField.setText(address + ":" + String.valueOf(port));
			appendToOutput("XML-RPC server running on " + address + ":" + String.valueOf(port));
		} catch (UnknownHostException e) {
			appendToOutput("Could not determine the IP address of the local machine. XML-RPC server may be unreachable.");
		}
	}

	/**
	 * Runs an HTTP server on the given port, registers a handler to handle requests coming in. Handling of requests is performed
	 * by the HTTPHandler class nested within this class.
	 * 
	 * This HTTP server is responsible for receiving actuator commands from the VW as part of the VW-to-Actuators interface.
	 * @param port
	 */
	private void setupHTTPServer(int port) {
		HttpServer srv;
		try {
			srv = HttpServer.create(new InetSocketAddress(port), 0);
			srv.createContext("/", new HTTPHandler());
			srv.start();
		} catch (IOException e) {
			appendToOutput("Problem starting HTTP server, VW-to-Actuator functionality will not work.");
		}

		/*
		 * Display the address & port that the HTTP server is running upon in the relevant section of the GUI.
		 */
		try {
			InetAddress address = InetAddress.getLocalHost();	
			actuatorInterfaceAddressField.setText(address + ":" + String.valueOf(port));
			appendToOutput("HTTP server running on " + address + ":" + String.valueOf(port));
		} catch (UnknownHostException e) {
			appendToOutput("Could not determine the IP address of the local machine. HTTP server may be unreachable.");
		}
	}

	/**
	 * Allows the wireless sensor network to report a sensor reading to the Control
	 * Panel, using the agreed upon protocol. This is the method that is remotely invoked
	 * by XMKL-RPC by the WSN when it wishes to report a reading to the virtual world.
	 * @return
	 */
	public Boolean sensed(String id, String type, int value) {

		/*
		 * Check that the node that sent this sensor reading is one that the Control Panel
		 * knows about & is monitoring, by querying the database for it.
		 */
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return false;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect;

		try {
			connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return false;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s;

		try {
			s = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem creating MySQL statement.");
			return false;
		}

		ResultSet rs = null;

		try {
			rs = s.executeQuery("SELECT * FROM nodes WHERE id = '" + id +"'");
		} catch (SQLException e) {
			cp.appendToOutput("Problem executing MySQL statement.");
			return false;
		}

		ArrayList<String> ids = new ArrayList<String>();
		ArrayList<URL> addresses = new ArrayList<URL>();
		try {
			while (rs.next()) {
				ids.add(rs.getString("id"));
				try {
					addresses.add(new URL(rs.getString("address")));
				} catch (MalformedURLException e) {
					cp.appendToOutput("Problem parsing URL of node, may be null.");
				}
			}
		}  catch (SQLException e) {
			cp.appendToOutput("Problem processing results from MySQL statement.");
			return false;
		}

		/*
		 * If the Control Panel doesn't know about this node...
		 */
		if (ids.size() == 0) {
			cp.appendToOutput("Received a sensor reading from a node that the Control Panel does not recognise (id = " + id + ").");

			/*
			 * ... then if auto adding of nodes is on, just ignore it and do nothing (but send useful output to the terminals)
			 */
			if (!cp.wsnGeneralAutoAddCheck.isSelected()) {
				cp.appendToOutput("Autoadd is off, not adding unrecognised node.");
				return false;
			}

			/*
			 * ... but if auto affing of nodes is on & this node isn't in the ignore list for auto adding, then add it to the database
			 */
			else {
				if (cp.ignoreNodeList.contains(id)) {
					cp.appendToOutput("Node id is in ignore list, not auto adding (manually add to override).");
					return false;
				}
				Statement insertS;
				cp.appendToOutput("Autoadding unrecognised node to the database...");
				try {
					insertS = connect.createStatement();
				} catch (SQLException e) {
					cp.appendToOutput("Problem adding node to the database.");
					return false;
				}
				try {
					insertS.executeUpdate("INSERT INTO nodes VALUES ('" + id +"', null)");
				} catch (SQLException e) {
					cp.appendToOutput("Problem adding node to the database.");
					return false;
				}

				try {
					connect.close();
				} catch (SQLException e1) {
					appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
				}

				cp.appendToOutput("Node added to the database.");
				cp.updateNodeSelect(cp.wsnGeneralNodeComboBox);
				cp.updateNodeSelect(cp.wsnGeneralRemoveNodeComboBox);
				cp.updateNodeSelect(cp.wsnSensorsNodeComboBox);

				/*
				 * Do not use the reading if this is the first time we've seen the node.
				 * Do not auto add the sensor type if this is the first time we've seen the node.
				 * So just return.
				 */
				return false;
			}
		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* NODE MUST BE RECOGNISED TO GET THIS FAR ******* ******* ******* *******
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * When execution reaches this far, the node must be recognised by the Control Panel from a previous manual or auto add.
		 */
		cp.appendToOutput("Received a sensor reading from a node that the Control Panel recognises.");

		/*
		 * Although the Control Panel recognises the node, check that it recognises the particular sensor too.
		 *	
		 * First get all of the sensors for this node from the database.
		 */
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return false;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect2;

		try {
			connect2 = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return false;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s2;

		try {
			s2 = connect2.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem creating MySQL statement.");
			return false;
		}

		ResultSet rs2 = null;

		try {
			rs2 = s2.executeQuery("SELECT * FROM sensors where id = '" + id +"'");
		} catch (SQLException e) {
			cp.appendToOutput("Problem executing MySQL statement.");
			return false;
		}

		ArrayList<String> types = new ArrayList<String>();

		try {
			while (rs2.next()) {
				types.add(rs2.getString("type"));
			}
		} catch (SQLException e) {
			cp.appendToOutput("Problem processing results from MySQL statement.");
			return false;
		}

		try {
			connect.close();
		} catch (SQLException e1) {
			cp.appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
		}

		/*
		 * If the sensor type is unrecognised & auto add of sensors is off, don't do anything with it.
		 */
		if (!types.contains(type) && !cp.wsnSensorsAutoAddCheck.isSelected()) {
			cp.appendToOutput("Recognised node has reported from an unrecognised sensor, sensor auto add is off, not adding.");
			return false;
		}

		/*
		 * If the sensor type is unrecognised & auto add of sensors is on, register the new sensor but don't forward the reading this first time.
		 */
		else if (!types.contains(type) && cp.wsnSensorsAutoAddCheck.isSelected()) {
			/*
			 * Check the ignore list first.
			 */
			SensorIgnoreIdTypeObject ignoreChecker = new SensorIgnoreIdTypeObject(id, type);
			if (cp.ignoreSensorList.contains(ignoreChecker)) {
				cp.appendToOutput("Recognised node has reported from an unrecognised sensor, auto add is on, but sensor is on the ignore list " +
				" so not adding (manually add sensor to override ignore).");
				return false;
			}

			cp.appendToOutput("Recognised node has reported from an unrecognised sensor, sensor auto add is on, adding sensor.");

			/*
			 * Add the sensor to the database for this node.
			 */
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				cp.appendToOutput("Problem loading MySQL database driver.");
				return false;
			}
			cp.appendToOutput("MySQL driver loaded...");

			Connection connect3;

			try {
				connect3 = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				cp.appendToOutput("Problem connecting to MySQL database.");
				return false;
			}
			cp.appendToOutput("Connected to MySQL database...");

			Statement s3;

			try {
				s3 = connect3.createStatement();
			} catch (SQLException e) {
				cp.appendToOutput("Problem connecting to the database.");
				return false;
			}
			try {
				s3.executeUpdate("INSERT INTO sensors VALUES ('" + id + "', '" + type + "', null, null)");
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				cp.appendToOutput("Problem adding sensor to the database.");
				return false;
			}
			cp.appendToOutput("Node added to the database.");

			try {
				connect3.close();
			} catch (SQLException e1) {
				cp.appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}

			/*
			 * Update the combo box of sensors on the Sensors tab in case the currently selected sensor in wsnSensorsNodeComboBox is
			 * the one which we have just added a sensor for. 
			 */
			cp.updateSensorsSelect(cp.wsnSensorsSensorsComboBox, (String)cp.wsnSensorsNodeComboBox.getSelectedItem());

			cp.appendToOutput("Sensor type = " + type + " added to the database for node id = " + id + ".");

			/*
			 * Do not forward the reading if this is the first time we've seen this particular sensor
			 */
			return false;

		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* ******* **** Sensor calibration ******* ******* ******* ******* ******* 
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If execution gets this far, it means that the Node is recognised & the Sensor is also recognised for that Node.
		 * Thus, we can forward it on to the Virtual World, but first we check to see whether we are calibrating the Sensor.
		 */

		/*
		 * If calibration is selected...
		 */
		if (cp.wsnSensorsCalibrateCheck.isSelected()) {

			/*
			 * Get the current values of max & min from the database for the node/sensor combination that has just reported.
			 */
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				cp.appendToOutput("Problem loading MySQL database driver.");
				return false;
			}
			cp.appendToOutput("MySQL driver loaded...");

			Connection connect4;

			try {
				connect4 = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				cp.appendToOutput("Problem connecting to MySQL database.");
				return false;
			}
			cp.appendToOutput("Connected to MySQL database...");

			Statement s4;

			try {
				s4 = connect4.createStatement();
			} catch (SQLException e) {
				cp.appendToOutput("Problem connecting to the database.");
				return false;
			}

			ResultSet rs4 = null;

			try {
				rs4 = s4.executeQuery("SELECT max, min FROM sensors WHERE id = '" + id + "' and type = '" + type + "'"); 
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				cp.appendToOutput("Problem getting sensor max & min from database.");
				return false;
			}

			ArrayList<Object> min = new ArrayList<Object>();
			ArrayList<Object> max = new ArrayList<Object>();

			try {
				while (rs4.next()) {					
					min.add(rs4.getObject("min"));
					max.add(rs4.getObject("max"));
				}
			} catch (SQLException e) {
				cp.appendToOutput("Problem processing results from MySQL statement.");
				return false;
			}

			/*
			 * Connect4 can be closed now, because the values we need (if any) are in the ArrayLists.
			 */
			try {
				connect4.close();
			} catch (SQLException e1) {
				cp.appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}

			/*
			 * If the values for BOTH min & max are NOT null.
			 */
			if (!(min.get(0) == null) && !(max.get(0) == null)) {

				/*
				 * If the newly received value is less than the current minimum, update the minimum value in the database.
				 */
				if (value < (Integer)min.get(0)) {
					try {
						Class.forName("com.mysql.jdbc.Driver");
					} catch (ClassNotFoundException e) {
						cp.appendToOutput("Problem loading MySQL database driver.");
						return false;
					}
					cp.appendToOutput("MySQL driver loaded...");

					Connection connect5;

					try {
						connect5 = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
					} catch (SQLException e) {
						cp.appendToOutput("Problem connecting to MySQL database.");
						return false;
					}
					cp.appendToOutput("Connected to MySQL database...");

					Statement s5;

					try {
						s5 = connect5.createStatement();
					} catch (SQLException e) {
						cp.appendToOutput("Problem connecting to the database.");
						return false;
					}

					try {
						s5.executeUpdate("UPDATE sensors SET min = " + value + " WHERE id = '" + id + "' AND type = '" + type + "'"); 
					} catch (SQLException e) {
						System.out.println(e.getMessage());
						cp.appendToOutput("Problem updating sensor min value in database.");
						return false;
					}

					try {
						connect5.close();
					} catch (SQLException e1) {
						cp.appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
					}

					cp.appendToOutput("Sensor min value updated in database.");

					/*
					 * Update the text field that displays this value in wsnSensors, but only if the sensor that has just had its min/max
					 * updated is actually the one selected, otherwise it looks like the new value for one sensor is being applied to whatever
					 * other sensor is currently selected!
					 */
					if ((id.equalsIgnoreCase((String)(cp.wsnSensorsNodeComboBox.getSelectedItem()))) && (type.equalsIgnoreCase((String)(cp.wsnSensorsSensorsComboBox.getSelectedItem())))) {
						cp.wsnSensorsCalibrationMinField.setText(Integer.toString(value));
					}

				}

				/*
				 * If the newly received value is greater than the current maximum, update the maximum value in the database.
				 */
				if (value > (Integer)max.get(0)) {
					try {
						Class.forName("com.mysql.jdbc.Driver");
					} catch (ClassNotFoundException e) {
						cp.appendToOutput("Problem loading MySQL database driver.");
						return false;
					}
					cp.appendToOutput("MySQL driver loaded...");

					Connection connect6;

					try {
						connect6 = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
					} catch (SQLException e) {
						cp.appendToOutput("Problem connecting to MySQL database.");
						return false;
					}
					cp.appendToOutput("Connected to MySQL database...");

					Statement s6;

					try {
						s6 = connect6.createStatement();
					} catch (SQLException e) {
						cp.appendToOutput("Problem connecting to the database.");
						return false;
					}

					try {
						s6.executeUpdate("UPDATE sensors SET max = " + value + " WHERE id = '" + id + "' AND type = '" + type + "'"); 
					} catch (SQLException e) {
						System.out.println(e.getMessage());
						cp.appendToOutput("Problem updating sensor max value in database.");
						return false;
					}

					try {
						connect6.close();
					} catch (SQLException e1) {
						cp.appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
					}

					cp.appendToOutput("Sensor max value updated in database.");

					/*
					 * Update the text field that displays this value in wsnSensors, but only if the sensor that has just had its min/max
					 * updated is actually the one selected, otherwise it looks like the new value for one sensor is being applied to whatever
					 * other sensor is currently selected!
					 */
					if ((id.equalsIgnoreCase((String)(cp.wsnSensorsNodeComboBox.getSelectedItem()))) && (type.equalsIgnoreCase((String)(cp.wsnSensorsSensorsComboBox.getSelectedItem())))) {
						cp.wsnSensorsCalibrationMaxField.setText(Integer.toString(value));
					}
				}
			}

			/*
			 * If BOTH the values for min & max are BOTH null.
			 */
			else if ((min.get(0) == null) && (max.get(0) == null)) {

				/*
				 * Insert the newly acquired value as both max & min into the database.
				 */
				try {
					Class.forName("com.mysql.jdbc.Driver");
				} catch (ClassNotFoundException e) {
					cp.appendToOutput("Problem loading MySQL database driver.");
					return false;
				}
				cp.appendToOutput("MySQL driver loaded...");

				Connection connect7;

				try {
					connect7 = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
				} catch (SQLException e) {
					cp.appendToOutput("Problem connecting to MySQL database.");
					return false;
				}
				cp.appendToOutput("Connected to MySQL database...");

				Statement s7;

				try {
					s7 = connect7.createStatement();
				} catch (SQLException e) {
					cp.appendToOutput("Problem connecting to the database.");
					return false;
				}

				try {
					s7.executeUpdate("UPDATE sensors SET min = " + value + ", max = " + value + " WHERE id = '" + id + "' AND type = '" + type + "'");
				} catch (SQLException e) {
					System.out.println(e.getMessage());
					cp.appendToOutput("Problem updating sensor min value in database.");
					return false;
				}

				try {
					connect7.close();
				} catch (SQLException e1) {
					cp.appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
				}

				cp.appendToOutput("Sensor min & max value updated in database.");


				/*
				 * Update the text fields that display these values in wsnSensors, but only if the sensor that has just had its min/max
				 * updated is actually the one selected, otherwise it looks like the new value for one sensor is being applied to whatever
				 * other sensor is currently selected!
				 */
				if ((id.equalsIgnoreCase((String)(cp.wsnSensorsNodeComboBox.getSelectedItem()))) && (type.equalsIgnoreCase((String)(cp.wsnSensorsSensorsComboBox.getSelectedItem())))) {
					cp.wsnSensorsCalibrationMinField.setText(Integer.toString(value));
					cp.wsnSensorsCalibrationMaxField.setText(Integer.toString(value));
				}
			}
		}

		/*
		 * As the sensor type is recognised, forward it on!
		 */
		cp.appendToOutput("id = " + id + ", type = " + type + ", value = " + value);

		/*
		 * But only if the forwarding pause isn't enabled.
		 */
		if (cp.wsnGeneralPauseWsnToVwCheck.isSelected()) {
			cp.appendToOutput("WSN-to-VW forwarding is paused, not forwarding.");
			return false;
		}

		/*
		 * One final check that there is actually a valid HTTP URI to forward to
		 */
		try {
			cp.appendToOutput("Forwarding reading to: " + addresses.get(0));
		} catch (IndexOutOfBoundsException e) {
			cp.appendToOutput("No address for node in database, not forwarding.");
			return false;
		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* ******* Forward to Virtual World ******* ******* ******* ******* ****** 
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * When execution gets this far we actually need to forward the sensor reading on to the virtual
		 * world as a HTTP POST.
		 */
		return cp.HTTPPOSTForward(id, type, value, addresses.get(0));
	}

	/**
	 * Forwards a sensor reading to a Virtual World via the defined HTTP POST standard/protocol.
	 * Returns true if the POST is delivered successfully (receives a 200 HTTP response code back), false otherwise (eg receives a 400 code back).
	 * @param id
	 * @param type
	 * @param value
	 * @param address
	 * @return
	 */
	public boolean HTTPPOSTForward(String id, String type, int value, URL address) {

		/*
		 * Scale the value to something between 0 & 1 if both max & min values have been calibrated for this sensor.
		 */
		Double scaledValue = new Double(value);

		/*
		 * Get the max & min values from the database.
		 */
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return false;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect;

		try {
			connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return false;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s;

		try {
			s = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to the database.");
			return false;
		}

		ResultSet rs = null;

		try {
			rs = s.executeQuery("SELECT max, min FROM sensors WHERE id = '" + id + "' and type = '" + type + "'"); 
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			cp.appendToOutput("Problem getting sensor max & min from database.");
			return false;
		}

		ArrayList<Object> min = new ArrayList<Object>();
		ArrayList<Object> max = new ArrayList<Object>();

		try {
			while (rs.next()) {					
				min.add(rs.getObject("min"));
				max.add(rs.getObject("max"));
			}
		} catch (SQLException e) {
			cp.appendToOutput("Problem processing results from MySQL statement.");
			return false;
		}

		try {
			connect.close();
		} catch (SQLException e1) {
			cp.appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
		}

		/*
		 * If the sensor has been calibrated (eg there are values present for max & min).
		 */
		if (!(min.get(0) == null) && !(max.get(0) == null)) {
			System.out.println("scaling");
			scaledValue = (scaledValue/((Integer)max.get(0)-(Integer)min.get(0)));

			/*
			 * If the scaling results in the scaled value being greater than 1 or smaller than 0, send 1 or 0 and alert the user
			 * that recalibration is required.
			 */
			if (scaledValue > 1) {
				scaledValue = 1D;
				cp.appendToOutput("Reading has exceeded maximum, recommend recalibration of sensor.");
			}
			if (scaledValue < 0) {
				scaledValue = 0D;
				cp.appendToOutput("Reading is less than minimum, recommend recalibration of sensor.");
			}

		}

		/*
		 * Construct the body of the HTTP POST
		 */
		String data;
		try {
			data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8") + "&"
			+ URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8") + "&"
			+ URLEncoder.encode("value", "UTF-8") + "=" + URLEncoder.encode(Double.toString(scaledValue), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			cp.appendToOutput("Problem creating HTTP POST, unable to forward reading to virtual world.");
			return false;
		}

		/*
		 * Send the POST request.
		 */
		URLConnection conn = null;

		try {
			conn = address.openConnection();
		} catch (IOException e) {
			cp.appendToOutput("Problem connecting to virtual world URI for this reading forwarding, unable to foward reading to virtual world");
			return false;
		}

		conn.setDoOutput(true);

		OutputStreamWriter wr;
		try {
			wr = new OutputStreamWriter(conn.getOutputStream());
		} catch (IOException e1) {
			cp.appendToOutput("Problem connecting to virtual world URI for this reading forwarding, unable to foward reading to virtual world");
			return false;
		}

		try {
			wr.write(data);
		} catch (IOException e) {
			cp.appendToOutput("Problem forwarding reading to virtual world.");
			return false;
		}

		try {
			wr.flush();
		} catch (IOException e) {
			cp.appendToOutput("Problem flushing connection to virtual world, reading may not have been forwarding.");
			return false;
		}

		/*
		 * Receive the response from the POST request. Return true if it indicates success, false otherwise.
		 */
		BufferedReader buf;

		try {
			buf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} catch (IOException e) {
			cp.appendToOutput("Problem receiving HTTP response from virtual world, reading may not have been forwarded.");
			return false;
		}

		String line;

		try {
			while ((line = buf.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			cp.appendToOutput("Problem processing HTTP response from virtual world, reading may not have been forwarded.");
			return false;
		}

		try {
			wr.close();
			buf.close();
		} catch (IOException e) {
			cp.appendToOutput("Problem closing HTTP response connection from virtual world, reading may not have been forwarded.");
			return false;
		}

		cp.appendToOutput("Reading successfully forward to virtual world, with body " + data);
		return true;
	}

	/**
	 * Replaces the contents of the given node JComboBox with all of the nodes that are currently registered with the Control panel &
	 * are thus in the database.
	 */
	private void updateNodeSelect(JComboBox list) {

		cp.appendToOutput("Updating the node list...");

		/*
		 * First empty the list.
		 */
		list.removeAllItems();

		/*
		 * Clear the node address field so an address doesn't remain from a deleted node.
		 */
		if (list.equals(wsnGeneralNodeComboBox)) {
			wsnGeneralNodeAddressField.setText("");
		}

		if (list.equals(wsnSensorsNodeComboBox)) {
			wsnSensorsNodeAddressField.setText("");
		}

		/*
		 * Get all of the nodes from the database.
		 */
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect;

		try {
			connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s;

		try {
			s = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem creating MySQL statement.");
			return;
		}

		ResultSet rs = null;

		try {
			rs = s.executeQuery("SELECT * FROM nodes");
		} catch (SQLException e) {
			cp.appendToOutput("Problem executing MySQL statement.");
			return;
		}

		ArrayList<String> ids = new ArrayList<String>();

		try {
			while (rs.next()) {
				ids.add(rs.getString("id"));
			}
		} catch (SQLException e) {
			cp.appendToOutput("Problem processing results from MySQL statement.");
			return;
		}

		try {
			connect.close();
		} catch (SQLException e1) {
			appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
		}

		Iterator<String> idsIter = ids.iterator();
		while (idsIter.hasNext()) {
			list.addItem(idsIter.next());
		}

		appendToOutput("Node list updated.");

	}

	/**
	 * Replaces the contents of the given sensors JComboBox with all of the sensors that are registered to the node with the given id & are
	 * thus in the database.
	 * @param list
	 */
	private void updateSensorsSelect(JComboBox list, String id) {
		cp.appendToOutput("Updating the sensors list...");

		/*
		 * First empty the list.
		 */
		list.removeAllItems();

		/*
		 * Get all of the sensors from the database.
		 */
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect;

		try {
			connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s;

		try {
			s = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem creating MySQL statement.");
			return;
		}

		ResultSet rs = null;

		try {
			rs = s.executeQuery("SELECT * FROM sensors where id = '" + id +"'");
		} catch (SQLException e) {
			cp.appendToOutput("Problem executing MySQL statement.");
			return;
		}

		ArrayList<String> types = new ArrayList<String>();

		try {
			while (rs.next()) {
				types.add(rs.getString("type"));
			}
		} catch (SQLException e) {
			cp.appendToOutput("Problem processing results from MySQL statement.");
			return;
		}

		try {
			connect.close();
		} catch (SQLException e1) {
			appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
		}

		Iterator<String> typesIter = types.iterator();
		while (typesIter.hasNext()) {
			list.addItem(typesIter.next());
		}

		appendToOutput("Types list updated.");

	}

	/**
	 * Replaces the contents of the given actuators JComboBox with all of the actuators that are registered with the Control Panel & thus
	 * in the database.
	 * @param list
	 */
	private void updateActuatorsSelect(JComboBox list) {
		cp.appendToOutput("Updating the actuators list...");

		/*
		 * First empty the list.
		 */
		list.removeAllItems();

		/*
		 * Clear the actuator address field so an address doesn't remain from a deleted actuator.
		 */
		if (list.equals(actuatorActuatorComboBox)) {
			actuatorAddressField.setText("");
		}

		/*
		 * Get all of the actuators from the database.
		 */
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect;

		try {
			connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s;

		try {
			s = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem creating MySQL statement.");
			return;
		}

		ResultSet rs = null;

		try {
			rs = s.executeQuery("SELECT * FROM actuators");
		} catch (SQLException e) {
			cp.appendToOutput("Problem executing MySQL statement.");
			return;
		}

		ArrayList<String> ids = new ArrayList<String>();

		try {
			while (rs.next()) {
				ids.add(rs.getString("id"));
			}
		} catch (SQLException e) {
			cp.appendToOutput("Problem processing results from MySQL statement.");
			return;
		}

		try {
			connect.close();
		} catch (SQLException e1) {
			appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
		}

		Iterator<String> idsIter = ids.iterator();
		while (idsIter.hasNext()) {
			list.addItem(idsIter.next());
		}

		appendToOutput("Actuator list updated.");
	}

	/**
	 * Manually add a node (id & address) to the database. Called from the action on a GUI button, uses values in
	 * globally accessible text fields as input.
	 */
	private void manualNodeAdd() {

		/*
		 * Simple check first of whether there is actually an id provided.
		 */
		if (wsnGeneralManualNodeAddId.getText().length() == 0) {
			appendToOutput("Failed to add node to the database, id string is null.");
			return;
		}

		/*
		 * Now check to see whether the id is already taken by a node in the database.
		 */
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect;

		try {
			connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s;

		try {
			s = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem creating MySQL statement.");
			return;
		}

		ResultSet rs = null;

		try {
			rs = s.executeQuery("SELECT * FROM nodes WHERE id = '" + wsnGeneralManualNodeAddId.getText() + "'");
		} catch (SQLException e) {
			cp.appendToOutput("Problem executing MySQL statement.");
			return;
		}

		ArrayList<String> ids = new ArrayList<String>();
		try {
			while (rs.next()) {
				ids.add(rs.getString("id"));
			}
		}  catch (SQLException e) {
			cp.appendToOutput("Problem processing results from MySQL statement.");
			return;
		}

		/*
		 * If there is already a node in the database with that id, don't write over it and return instead.
		 */
		if (ids.size() != 0) {
			appendToOutput("There is already a node with that id in the database, not adding it again.");
			return;
		}

		/*
		 * Check that what is in the address field is actually a valid address.
		 */
		try {
			@SuppressWarnings("unused")
			URL address = new URL(wsnGeneralManualNodeAddAddress.getText());
		} catch (MalformedURLException e2) {
			appendToOutput("Failed to add node to the database, address is not valid.");
			return;
		}

		/*
		 * If the id isn't already in the database and the address is valid, go about adding it.
		 */
		appendToOutput("Id is unique, adding it to the database.");

		if(ignoreNodeList.contains(wsnGeneralManualNodeAddId.getText())) {
			appendToOutput("Node being manually added has been removed from this session's ignore list");
			ignoreNodeList.remove(wsnGeneralManualNodeAddId.getText());
		}

		Statement insertS;

		try {
			insertS = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to the database.");
			return;
		}
		try {
			insertS.executeUpdate("INSERT INTO nodes VALUES ('" + wsnGeneralManualNodeAddId.getText() + "','" + wsnGeneralManualNodeAddAddress.getText() + "')");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			cp.appendToOutput("Problem adding node to the database.");
			return;
		}
		cp.appendToOutput("Node added to the database.");

		try {
			connect.close();
		} catch (SQLException e1) {
			appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
		}

		/*
		 * Update all of the GUI components that display the information that has been changed by this database update, so that
		 * they reflect these updates.
		 */
		updateNodeSelect(wsnGeneralNodeComboBox);
		updateNodeSelect(wsnGeneralRemoveNodeComboBox);
		updateNodeSelect(wsnSensorsNodeComboBox);
		wsnGeneralManualNodeAddId.setText("");
		wsnGeneralManualNodeAddAddress.setText("");

		return;
	}

	/**
	 * Manually add a sensor (id & type) to the database.
	 */
	private void manualSensorAdd() {

		/*
		 * Simple check first of whether a type has been provided.
		 */
		if (wsnSensorsManualSensorAddType.getText().length() == 0) {
			appendToOutput("Failed to add sensor to the database, type string is null.");
			return;
		}

		/*
		 * Now check to see whether the node in question already has a sensor registered of this type.
		 */
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect;

		try {
			connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s;

		try {
			s = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem creating MySQL statement.");
			return;
		}

		ResultSet rs = null;

		try {
			rs = s.executeQuery("SELECT * FROM sensors WHERE id = '" + wsnSensorsNodeComboBox.getSelectedItem() + "' AND type = '" +
					wsnSensorsManualSensorAddType.getText() +"'");
		} catch (SQLException e) {
			cp.appendToOutput("Problem executing MySQL statement.");
			return;
		}

		ArrayList<String> types = new ArrayList<String>();
		try {
			while (rs.next()) {
				types.add(rs.getString("id"));
			}
		}  catch (SQLException e) {
			cp.appendToOutput("Problem processing results from MySQL statement.");
			return;
		}

		if (types.size() != 0) {
			appendToOutput("There is already a sensor of that type registered for this node, not adding it again.");
			return;
		}

		/*
		 * If the sensor type isn't already in the database for this node, go about adding it.
		 */
		appendToOutput("Type is unique, adding it to the database.");

		if(ignoreSensorList.contains(wsnSensorsManualSensorAddType.getText())) {
			appendToOutput("Sensor being manually added has been removed from this session's ignore list");
			ignoreSensorList.remove(wsnSensorsManualSensorAddType.getText());
		}

		Statement insertS;

		try {
			insertS = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to the database.");
			return;
		}
		try {
			insertS.executeUpdate("INSERT INTO sensors VALUES ('" + wsnSensorsNodeComboBox.getSelectedItem() + "', '" + 
					wsnSensorsManualSensorAddType.getText() + "', null, null)");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			cp.appendToOutput("Problem adding sensor to the database.");
			return;
		}
		cp.appendToOutput("Sensor added to the database.");

		try {
			connect.close();
		} catch (SQLException e1) {
			appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
		}

		SensorIgnoreIdTypeObject removeChecker = new SensorIgnoreIdTypeObject((String)wsnSensorsNodeComboBox.getSelectedItem(), 
				(String)wsnSensorsManualSensorAddType.getText());
		if (ignoreSensorList.contains(removeChecker)) {
			ignoreSensorList.remove(removeChecker);
			appendToOutput("Sensor that has just been added has been removed from this session's ignore list.");
		}

		updateSensorsSelect(wsnSensorsSensorsComboBox, (String)wsnSensorsNodeComboBox.getSelectedItem());
		wsnSensorsManualSensorAddType.setText("");

		return;
	}

	/**
	 * Manually add an actuator (id, address & type) to the database.
	 */
	private void manualActuatorAdd() {
		/*
		 * Simple checks first, for whether an id and an address are provided.
		 */
		if (actuatorManualActuatorAddId.getText().length() == 0) {
			appendToOutput("Failed to add actuator to the database, id string is null.");
			return;
		}
		if (actuatorManualActuatorAddAddress.getText().length() == 0) {	
			appendToOutput("Failed to add actuator to the database, address string is null.");
			return;
		}

		/*
		 * Check that what is in the address field is actually a valid address.
		 */
		try {
			@SuppressWarnings("unused")
			URL address = new URL(actuatorManualActuatorAddAddress.getText());
		} catch (MalformedURLException e2) {
			appendToOutput("Failed to add actuator to the database, address is not valid.");
			return;
		}

		/*
		 * Now check to see whether this id already exists in the database;
		 */
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect;

		try {
			connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s;

		try {
			s = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem creating MySQL statement.");
			return;
		}

		ResultSet rs = null;

		try {
			rs = s.executeQuery("SELECT * FROM actuators WHERE id = '" + actuatorManualActuatorAddId.getText() + "'");
		} catch (SQLException e) {
			cp.appendToOutput("Problem executing MySQL statement.");
			return;
		}

		ArrayList<String> types = new ArrayList<String>();
		try {
			while (rs.next()) {
				types.add(rs.getString("id"));
			}
		}  catch (SQLException e) {
			cp.appendToOutput("Problem processing results from MySQL statement.");
			return;
		}

		if (types.size() != 0) {
			appendToOutput("There is already an actuator in the database with that id, not adding again.");
			return;
		}

		/*
		 * If the id/address combination isn't already in the database, go about adding it.
		 */
		appendToOutput("Actuator is unique, adding it to the database.");

		Statement insertS;

		try {
			insertS = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to the database.");
			return;
		}
		try {
			insertS.executeUpdate("INSERT INTO actuators VALUES ('" + actuatorManualActuatorAddId.getText() + "', '" + 
					actuatorManualActuatorAddAddress.getText() + "')");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			cp.appendToOutput("Problem adding actuator to the database.");
			return;
		}
		cp.appendToOutput("Actuator added to the database.");

		try {
			connect.close();
		} catch (SQLException e1) {
			appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
		}

		/*
		 * Update any of the GUI components that display the information that has just been updated in the database.
		 */
		actuatorManualActuatorAddAddress.setText("");
		actuatorManualActuatorAddId.setText("");
		updateActuatorsSelect(actuatorActuatorComboBox);
		updateActuatorsSelect(actuatorRemoveActuatorComboBox);

		return;
	}

	/**
	 * Removes a node from the database, so that the Control Panel won't 'recognise' it the next time it sends a message.
	 * Can also ignore the removed node & not auto add it the next time it sends a message.
	 * Checks for any dependent information that must also be removed to maintain database integrity.
	 * @param id
	 */
	private void removeNode(String id) {

		/*
		 * Check whether the node should also be ignored for future auto adding, if so add it to an ArrayList that keeps track
		 * of which nodes to ignore this session.
		 */
		if (wsnGeneralRemoveNodeIgnoreCheck.isSelected()) {
			ignoreNodeList.add(id);
			appendToOutput("Node with id = " + id + " will not be auto added again this  (manually add to override).");
		}

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect;

		try {
			connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s;

		try {
			s = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem creating MySQL statement.");
			return;
		}

		try {
			s.executeUpdate("DELETE FROM nodes WHERE id = '" + id + "'");

			/*
			 * Also delete any sensors associated with this node, otherwise they will come back to haunt when another
			 * node with the same name as the one we're deleting is created.
			 */
			s.executeUpdate("DELETE FROM sensors WHERE id = '" + id + "'");
		} catch (SQLException e) {
			appendToOutput("Problem executing MySQL statement.");
			return;
		}

		/*
		 * Update all GUI components that display any of the information that has just been removed.
		 */
		appendToOutput("Node removed from the database");
		wsnGeneralRemoveNodeIgnoreCheck.setSelected(false);
		updateNodeSelect(wsnGeneralNodeComboBox);
		updateNodeSelect(wsnGeneralRemoveNodeComboBox);
		updateNodeSelect(wsnSensorsNodeComboBox);
	}

	/**
	 * Removes the given sensor from the database for the given node, so that the Control Panel won't 'recognise' it the next time it
	 * is part of a sensed call.
	 * Can also ignore the removed sensor & not auto add it the next time it is part of a sensed call.
	 * @param id
	 * @param type
	 */
	private void removeSensor(String id, String type) {

		/*
		 * Check whether the sensor is to be ignored for the rest of the session, if so add it to this session's ignore list.
		 */
		if (wsnSensorsRemoveSensorIgnoreCheck.isSelected()) {
			ignoreSensorList.add(new SensorIgnoreIdTypeObject(id, type));
			appendToOutput("Sensor type = " + type + " on node with id = " + id + " will not be auto added again this session " + 
			"(manually add to override).");
		}

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect;

		try {
			connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s;

		try {
			s = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem creating MySQL statement.");
			return;
		}

		/*
		 * Remove the sensor from the database.
		 */
		try {
			s.executeUpdate("DELETE FROM sensors WHERE id = '" + id + "' AND type = '" + type + "'");
		} catch (SQLException e) {
			appendToOutput("Problem executing MySQL statement.");
			return;
		}

		/*
		 * Update all GUI components that display the information that has just been removed.
		 */
		appendToOutput("Sensor removed from the database");
		wsnSensorsRemoveSensorIgnoreCheck.setSelected(false);
		updateSensorsSelect(wsnSensorsSensorsComboBox, id);
	}

	/**
	 * Removes the given actuator from the database. There is no auto adding feature for actuators, so adding
	 * them to an ignore list isn't necessary.
	 * @param id
	 */
	private void removeActuator(String id) {

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			cp.appendToOutput("Problem loading MySQL database driver.");
			return;
		}
		cp.appendToOutput("MySQL driver loaded...");

		Connection connect;

		try {
			connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
		} catch (SQLException e) {
			cp.appendToOutput("Problem connecting to MySQL database.");
			return;
		}
		cp.appendToOutput("Connected to MySQL database...");

		Statement s;

		try {
			s = connect.createStatement();
		} catch (SQLException e) {
			cp.appendToOutput("Problem creating MySQL statement.");
			return;
		}

		try {
			s.executeUpdate("DELETE FROM actuators WHERE id = '" + id + "'");
		} catch (SQLException e) {
			appendToOutput("Problem executing MySQL statement.");
			return;
		}

		/*
		 * Update all of the GUI components that display the information that has just been changed.
		 */
		appendToOutput("Actuator removed from the database");
		updateActuatorsSelect(actuatorActuatorComboBox);
		updateActuatorsSelect(actuatorRemoveActuatorComboBox);
	}

	/**
	 * Saves the address, username & password of the MySQL server entered by the user for retrieval
	 * at a later execution of the program. Saves to a plaintext file, which is insecure, but is more
	 * a proof of concept than a final implementation which would store a hash.
	 * @param address
	 * @param user
	 * @param pass
	 */
	public void saveMysqlDetails() {
		FileWriter fout;
		try {
			fout = new FileWriter(new File("mysqldetails.txt"));
			fout.write(mysqlAddress + "," + mysqlUser + "," + mysqlPass);
			fout.close();
			appendToOutput("MySQL details saved to file.");
		} catch (IOException e) {
			appendToOutput("Problem writing MySQL details to file. Details not saved.");
		}
	}

	/**
	 * Loads the address, username & password of the MySQL server entered by the user previously &
	 * saved to file.
	 */
	public void loadMysqlDetails() {
		FileReader fin;
		try {
			fin = new FileReader("mysqldetails.txt");
			BufferedReader br = new BufferedReader(fin);
			String s = br.readLine();
			mysqlAddress = s.split(",")[0];
			wsnGeneralMysqlAddressField.setText(mysqlAddress);
			mysqlUser = s.split(",")[1];
			wsnGeneralMysqlUserField.setText(mysqlUser);
			mysqlPass = s.split(",")[2];
			wsnGeneralMysqlPassField.setText(mysqlPass);
		} catch (FileNotFoundException e) {
			appendToOutput("Problem reading MySQL details from file: file not found. Have you saved details before?.");
		} catch (IOException e) {
			appendToOutput("Problem reading MySQL details from file. Details not loaded.");
		}

		/*
		 * Update all of the GUI components that display information from the database, as a new database connection
		 * has just been given.
		 */
		updateNodeSelect(wsnGeneralNodeComboBox);
		updateNodeSelect(wsnGeneralRemoveNodeComboBox);
		updateNodeSelect(wsnSensorsNodeComboBox);
		updateActuatorsSelect(actuatorActuatorComboBox);
		updateActuatorsSelect(actuatorRemoveActuatorComboBox);
	}

	/**
	 * Catches the events that are thrown when buttons are pressed and other GUI components are interacted with.
	 */
	public void actionPerformed(ActionEvent ev) {


		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* ******* updateNodeAddressButton pressed ******* ******* ******* *******
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If the event comes from updateNodeAddressButton it means we are updating the address
		 * of a node in the database.
		 */
		if (ev.getSource().equals(wsnGeneralUpdateNodeAddressButton )) {

			String newAddress = wsnGeneralNodeAddressField.getText();
			String id = (String)wsnGeneralNodeComboBox.getSelectedItem();

			/*
			 * Check that what is in the address field is actually a valid address.
			 */
			try {
				@SuppressWarnings("unused")
				URL address = new URL(wsnGeneralNodeAddressField.getText());
			} catch (MalformedURLException e2) {
				appendToOutput("Failed to update node address in the database, address is not valid.");
				return;
			}

			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				appendToOutput("Problem loading MySQL database driver.");
				return;
			}
			appendToOutput("MySQL driver loaded...");

			Connection connect;

			try {
				connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				appendToOutput("Problem connecting to MySQL database.");
				return;
			}
			appendToOutput("Connected to MySQL database...");

			Statement s;

			try {
				s = connect.createStatement();
			} catch (SQLException e) {
				appendToOutput("Problem creating MySQL statement.");
				return;
			}

			try {
				s.executeUpdate("UPDATE nodes SET address = '" + newAddress + "' WHERE id = '" + id +"'");
			} catch (SQLException e) {
				appendToOutput("Problem executing MySQL statement.");
				return;
			}
			appendToOutput("Node address updated in the database");

			try {
				connect.close();
			} catch (SQLException e1) {
				appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}

			/*
			 * Don't forget to update the address at the other place it is displayed, on the wsnSensors tab.
			 */
			wsnSensorsNodeAddressField.setText(newAddress);
		}


		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* wsnGeneralUpdateMysqlAddressButton pressed ******* ******* ******* ****
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If the event comes from updateMysqlAddressButton then it means we are changing the
		 * address of the MySQL database.
		 */
		else if (ev.getSource().equals(wsnGeneralUpdateMysqlAddressButton)) {
			mysqlAddress = "jdbc:mysql://" + wsnGeneralMysqlAddressField.getText();
			mysqlUser = wsnGeneralMysqlUserField.getText();
			mysqlPass = new String(wsnGeneralMysqlPassField.getPassword());
			cp.updateNodeSelect(wsnGeneralNodeComboBox);
			cp.updateNodeSelect(wsnGeneralRemoveNodeComboBox);
			cp.updateNodeSelect(wsnSensorsNodeComboBox);
		}

		/*
		 * Saving new MySQL details.
		 */
		else if (ev.getSource().equals(wsnGeneralSaveMysqlDetailsButton)) {
			mysqlAddress = "jdbc:mysql://" + wsnGeneralMysqlAddressField.getText();
			mysqlUser = wsnGeneralMysqlUserField.getText();
			mysqlPass = new String(wsnGeneralMysqlPassField.getPassword());
			saveMysqlDetails();
		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* wsnGeneralManualNodeAddButton pressed ******* ******* ******* ******* *
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If the event comes from the manual node adding button, then we've got a new node to add to the database.
		 */
		else if (ev.getSource().equals(wsnGeneralManualNodeAddButton)) {
			manualNodeAdd();
		}

		
		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* wsnGeneralRemoveNodeButton pressed ******* ******* ******* ******* ****
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If the event comes from the manual node remove button, then we've got a node to remove from the database.
		 */
		else if (ev.getSource().equals(wsnGeneralRemoveNodeButton)) {
			removeNode((String)wsnGeneralRemoveNodeComboBox.getSelectedItem());
		}

		
		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* wsnSensorsManualAddSensorButton pressed ******* ******* ******* *******
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If the event comes from the add sensor button, then we should go about adding that sensor to the database.
		 */
		else if (ev.getSource().equals(wsnSensorsManualAddSensorButton)) {
			manualSensorAdd();
		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* wsnSensorsRemoveSensorButton pressed ******* ******* ******* ******* **
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If the event comes from the remove sensor button, then we should set about deleting that sensor from the database.
		 */
		else if(ev.getSource().equals(wsnSensorsRemoveSensorButton)) {
			removeSensor((String)wsnSensorsNodeComboBox.getSelectedItem(), (String)wsnSensorsSensorsComboBox.getSelectedItem());
		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* wsnSensorsCalibrateResetButton pressed ******* ******* ******* ********
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If the event comes from the calibrarion reset button, then delete the max & min for the relevant node/sensor combo from
		 * the database & update the text fields to read blank.
		 */
		else if (ev.getSource().equals(wsnSensorsCalibrateResetButton)) {

			String id = (String)wsnSensorsNodeComboBox.getSelectedItem();
			String type = (String)wsnSensorsSensorsComboBox.getSelectedItem();

			/*
			 * First remove the values from the database.
			 */
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				appendToOutput("Problem loading MySQL database driver.");
				return;
			}
			appendToOutput("MySQL driver loaded...");

			Connection connect;

			try {
				connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				appendToOutput("Problem connecting to MySQL database.");
				return;
			}
			appendToOutput("Connected to MySQL database...");

			Statement s;

			try {
				s = connect.createStatement();
			} catch (SQLException e) {
				appendToOutput("Problem creating MySQL statement.");
				return;
			}

			try {
				s.executeUpdate("UPDATE sensors SET max = null, min = null where id = '" + id + "' and type = '" + type +"'");
			} catch (SQLException e) {
				appendToOutput("Problem executing MySQL statement.");
				return;
			}
			appendToOutput("Min & max for this sensor have been reset in the database");

			try {
				connect.close();
			} catch (SQLException e1) {
				appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}

			/*
			 * Now set the text fields to be blank.
			 */
			wsnSensorsCalibrationMaxField.setText("");
			wsnSensorsCalibrationMinField.setText("");
		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* wsnGeneralRefreshNodeAddressButton pressed ******* ******* ******* ****
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If the source of the event is the node address refresh button, then get the latest node address from the database
		 * & put it in the text fields in both wsnGeneral & wsnSensors.
		 */
		else if (ev.getSource().equals(wsnGeneralRefreshNodeAddressButton)) {
			String id = (String)wsnGeneralNodeComboBox.getSelectedItem();

			/*
			 * Get the latest address for this node from the database.
			 */
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				appendToOutput("Problem loading MySQL database driver.");
				return;
			}
			appendToOutput("MySQL driver loaded...");

			Connection connect;

			try {
				connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				appendToOutput("Problem connecting to MySQL database.");
				return;
			}
			appendToOutput("Connected to MySQL database...");

			Statement s;

			try {
				s = connect.createStatement();
			} catch (SQLException e) {
				appendToOutput("Problem creating MySQL statement.");
				return;
			}

			ResultSet rs = null;

			try {
				rs = s.executeQuery("SELECT * FROM nodes where id = '" + id + "'");
			} catch (SQLException e) {
				cp.appendToOutput("Problem executing MySQL statement.");
				return;
			}

			try {
				rs.next();
				wsnGeneralNodeAddressField.setText(rs.getString("address"));
				wsnSensorsNodeAddressField.setText(rs.getString("address"));
			}
			catch (SQLException e) {
				cp.appendToOutput("Problem processing results from MySQL statement.");
				return;
			}

			try {
				connect.close();
			} catch (SQLException e1) {
				appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}
		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* actuatorManualActuatorAddButton pressed ******* ******* ******* *******
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * Adding an actuator to the database.
		 */
		else if (ev.getSource().equals(actuatorManualActuatorAddButton)) {
			manualActuatorAdd();
		}

		/*
		 * Actuator address update button.
		 */
		else if (ev.getSource().equals(actuatorUpdateActuatorAddressButton)) {
			String newAddress = actuatorAddressField.getText();
			String id = (String)actuatorActuatorComboBox.getSelectedItem();

			/*
			 * Check that it's a valid url
			 */
			try {
				@SuppressWarnings("unused")
				URL address = new URL(actuatorAddressField.getText());
			} catch (MalformedURLException e2) {
				appendToOutput("Failed to add actuator to the database, address is not valid.");
				return;
			}

			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				appendToOutput("Problem loading MySQL database driver.");
				return;
			}
			appendToOutput("MySQL driver loaded...");

			Connection connect;

			try {
				connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				appendToOutput("Problem connecting to MySQL database.");
				return;
			}
			appendToOutput("Connected to MySQL database...");

			Statement s;

			try {
				s = connect.createStatement();
			} catch (SQLException e) {
				appendToOutput("Problem creating MySQL statement.");
				return;
			}

			try {
				s.executeUpdate("UPDATE actuators SET address = '" + newAddress + "' WHERE id = '" + id +"'");
			} catch (SQLException e) {
				appendToOutput("Problem executing MySQL statement.");
				return;
			}
			appendToOutput("Actuator address updated in the database");

			try {
				connect.close();
			} catch (SQLException e1) {
				appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}

			/*
			 * Don't forget to update the address at the other place it is displayed, on the wsnSensors tab.
			 */
			actuatorAddressField.setText(newAddress);
		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* actuatorRefreshActuatorAddressButton pressed ******* ******* ******* **
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * Refreshing the actuator address field.
		 */
		else if (ev.getSource().equals(actuatorRefreshActuatorAddressButton)) {
			String id = (String)actuatorActuatorComboBox.getSelectedItem();

			/*
			 * Get the latest address for this actuator from the database.
			 */
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				appendToOutput("Problem loading MySQL database driver.");
				return;
			}
			appendToOutput("MySQL driver loaded...");

			Connection connect;

			try {
				connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				appendToOutput("Problem connecting to MySQL database.");
				return;
			}
			appendToOutput("Connected to MySQL database...");

			Statement s;

			try {
				s = connect.createStatement();
			} catch (SQLException e) {
				appendToOutput("Problem creating MySQL statement.");
				return;
			}

			ResultSet rs = null;

			try {
				rs = s.executeQuery("SELECT * FROM actuators where id = '" + id + "'");
			} catch (SQLException e) {
				cp.appendToOutput("Problem executing MySQL statement.");
				return;
			}

			try {
				rs.next();
				actuatorAddressField.setText(rs.getString("address"));
			}
			catch (SQLException e) {
				cp.appendToOutput("Problem processing results from MySQL statement.");
				return;
			}

			try {
				connect.close();
			} catch (SQLException e1) {
				appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}
		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* actuatorRemoveActuatorButton pressed ******* ******* ******* ******* **
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * Removing an actuator from the database
		 */
		else if (ev.getSource().equals(actuatorRemoveActuatorButton)) {
			removeActuator((String)actuatorRemoveActuatorComboBox.getSelectedItem());
		}

	}

	/**
	 * Handles what happens when selected items in combo boxes of the GUI are changed.
	 */
	public void itemStateChanged(ItemEvent ev) {

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* ******* wsnGeneralNodeComboBox changed ******* ******* ******* ********
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If the event comes from wsnGeneralNodeComboBox it means that the selected node in the General tab
		 * list has been changed so the address in the text area beneath should be updated to reflect this change.
		 */
		if (ev.getSource().equals(wsnGeneralNodeComboBox)) {
			String id = (String)wsnGeneralNodeComboBox.getSelectedItem();

			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				appendToOutput("Problem loading MySQL database driver.");
				return;
			}
			appendToOutput("MySQL driver loaded...");

			Connection connect;

			try {
				connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				appendToOutput("Problem connecting to MySQL database.");
				return;
			}
			appendToOutput("Connected to MySQL database...");

			Statement s;

			try {
				s = connect.createStatement();
			} catch (SQLException e) {
				appendToOutput("Problem creating MySQL statement.");
				return;
			}

			ResultSet rs = null;

			try {
				rs = s.executeQuery("SELECT * FROM nodes where id = '" + id + "'");
			} catch (SQLException e) {
				cp.appendToOutput("Problem executing MySQL statement.");
				return;
			}

			try {
				rs.next();
				wsnGeneralNodeAddressField.setText(rs.getString("address"));
			}
			catch (SQLException e) {
				cp.appendToOutput("Problem processing results from MySQL statement.");
				return;
			}

			try {
				connect.close();
			} catch (SQLException e1) {
				appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}
		}
		
		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* ******* wsnSensorsNodeComboBox changed ******* ******* ******* ********
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If the event comes from wsnSensorsNodeComboBox it means that the selected node in the Sensors tab
		 * list has been changed so the address in the text area beneath should be updated to reflect this change.
		 * Also, the entries in wsnSensorsSensorsComboBox should be updated to show the sensors that are registered
		 * to this newly selected node in the database.
		 * Also, the maximum & minimum values should be updated in their text fields.
		 */
		else if (ev.getSource().equals(wsnSensorsNodeComboBox)) {
			String id = (String)wsnSensorsNodeComboBox.getSelectedItem();

			/*
			 * First update the text area beneath wsnSensorsNodeComboBox.
			 */
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				appendToOutput("Problem loading MySQL database driver.");
				return;
			}
			appendToOutput("MySQL driver loaded...");

			Connection connect;

			try {
				connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				appendToOutput("Problem connecting to MySQL database.");
				return;
			}
			appendToOutput("Connected to MySQL database...");

			Statement s;

			try {
				s = connect.createStatement();
			} catch (SQLException e) {
				appendToOutput("Problem creating MySQL statement.");
				return;
			}

			ResultSet rs = null;

			try {
				rs = s.executeQuery("SELECT * FROM nodes where id = '" + id + "'");
			} catch (SQLException e) {
				cp.appendToOutput("FOOProblem executing MySQL statement.");
				return;
			}

			try {
				rs.next();
				wsnSensorsNodeAddressField.setText(rs.getString("address"));
			}
			catch (SQLException e) {
				cp.appendToOutput("Problem processing results from MySQL statement.");
				return;
			}

			try {
				connect.close();
			} catch (SQLException e1) {
				appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}

			/*
			 * Now update wsnSensorsSensorsComboBox.
			 */
			updateSensorsSelect(wsnSensorsSensorsComboBox, id);

			/*
			 * Now update the maximum & minimum values.
			 */
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				appendToOutput("Problem loading MySQL database driver.");
				return;
			}
			appendToOutput("MySQL driver loaded...");

			Connection connect2;

			try {
				connect2 = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				appendToOutput("Problem connecting to MySQL database.");
				return;
			}
			appendToOutput("Connected to MySQL database...");

			Statement s2;

			try {
				s2 = connect2.createStatement();
			} catch (SQLException e) {
				appendToOutput("Problem creating MySQL statement.");
				return;
			}

			ResultSet rs2 = null;

			try {
				rs2 = s2.executeQuery("SELECT max, min FROM sensors where id = '" + id + "' and type = '" +
						(String)wsnSensorsSensorsComboBox.getSelectedItem() + "'");
			} catch (SQLException e) {
				cp.appendToOutput("Problem executing MySQL statement.");
				return;
			}

			try {
				rs2.next();
				wsnSensorsCalibrationMaxField.setText(rs2.getString("max"));
				wsnSensorsCalibrationMinField.setText(rs2.getString("min"));
			}
			catch (SQLException e) {
				cp.appendToOutput("Problem processing results from MySQL statement (max & min are probably null).");
				wsnSensorsCalibrationMaxField.setText("");
				wsnSensorsCalibrationMinField.setText("");
				return;
			}

			try {
				connect.close();
			} catch (SQLException e1) {
				appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}
		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* ******* wsnSensorsSensorsComboBox changed ******* ******* ******* *****
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * If the source of the event is the sensors combo box on the sensors pane, then we need to update the max & min values on display.
		 */
		else if (ev.getSource().equals(wsnSensorsSensorsComboBox)) {

			String id = (String)wsnSensorsNodeComboBox.getSelectedItem();

			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				appendToOutput("Problem loading MySQL database driver.");
				return;
			}
			appendToOutput("MySQL driver loaded...");

			Connection connect;

			try {
				connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				appendToOutput("Problem connecting to MySQL database.");
				return;
			}
			appendToOutput("Connected to MySQL database...");

			Statement s;

			try {
				s = connect.createStatement();
			} catch (SQLException e) {
				appendToOutput("Problem creating MySQL statement.");
				return;
			}

			ResultSet rs = null;

			try {
				rs = s.executeQuery("SELECT max, min FROM sensors where id = '" + id + "' and type = '" +
						(String)wsnSensorsSensorsComboBox.getSelectedItem() + "'");
			} catch (SQLException e) {
				cp.appendToOutput("Problem executing MySQL statement.");
				return;
			}

			try {
				rs.next();
				wsnSensorsCalibrationMaxField.setText(rs.getString("max"));
				wsnSensorsCalibrationMinField.setText(rs.getString("min"));
			}
			catch (SQLException e) {
				cp.appendToOutput("Problem processing results from MySQL statement (max & min are probably null).");
				wsnSensorsCalibrationMaxField.setText("");
				wsnSensorsCalibrationMinField.setText("");
				return;
			}

			try {
				connect.close();
			} catch (SQLException e1) {
				appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}
		}

		/*
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * ******* ******* ******* ******* actuatorActuatorComboBox changed ******* ******* ******* ******
		 * ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* ******* *******
		 * 
		 * Update actuator address field to reflect new selection in actuator combo box.
		 */
		else if (ev.getSource().equals(actuatorActuatorComboBox)) {
			String id = (String)actuatorActuatorComboBox.getSelectedItem();

			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				appendToOutput("Problem loading MySQL database driver.");
				return;
			}
			appendToOutput("MySQL driver loaded...");

			Connection connect;

			try {
				connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
			} catch (SQLException e) {
				appendToOutput("Problem connecting to MySQL database.");
				return;
			}
			appendToOutput("Connected to MySQL database...");

			Statement s;

			try {
				s = connect.createStatement();
			} catch (SQLException e) {
				appendToOutput("Problem creating MySQL statement.");
				return;
			}

			ResultSet rs = null;

			try {
				rs = s.executeQuery("SELECT * FROM actuators where id = '" + id + "'");
			} catch (SQLException e) {
				cp.appendToOutput("Problem executing MySQL statement.");
				return;
			}

			try {
				rs.next();
				actuatorAddressField.setText(rs.getString("address"));	
			}
			catch (SQLException e) {
				cp.appendToOutput("Problem processing results from MySQL statement (address may be null).");
				return;
			}

			try {
				connect.close();
			} catch (SQLException e1) {
				appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
			}
		}

	}

	/**
	 * Makes an XML-RPC request to the given address, with the given values. Used to forward an actuator command that came into the Control
	 * Panel via HTTP POST from the virtual world, to the appropriate actuator via XML-RPC. Returns true if the XML-RPC request is successful,
	 * false otherwise
	 * @return boolean
	 */
	private boolean SendXMLRPCRequest(String address, String id, String action) {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

		try {
			config.setServerURL(new URL(address));
		} catch (MalformedURLException e) {
			cp.appendToOutput("Problem sending XML-RPC command to actuator, address is not valid.");
			return false;
		}

		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);

		System.out.println("address: " + address);
		System.out.println("id: " + id);
		System.out.println("action: " + action);

		Boolean result;
		try {
			result = (Boolean)client.execute("ActuatorRPCServer.control", new Object[]{new String(id), new String(action)});
		} catch (XmlRpcException e) {
			cp.appendToOutput("Problem executing actuator command via XML-RPC.");
			return false;
		}

		return result;
	}

	/*
	 * ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= =======
	 * ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= ======= =======
	 */
	
	/**
	 * An implementation of com.sun.net.HttpHandler for handling actuator commands sent from the virtual world to the control panel
	 * using HTTP POST.
	 * @author 060005151
	 * @version 15/04/2011
	 */
	class HTTPHandler implements HttpHandler {

		/**
		 * This method is called whenever the virtual world sends an HTTP request to the Control Panel, eg. to relay an actuator
		 * command on to the relevant actuator controller.
		 * 
		 * Return HTTP 400 response codes if the request is not successful, 200 response codes if it is.
		 * 
		 * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
		 */
		public void handle(HttpExchange exchange) throws IOException {

			/*
			 * This should be POST, we are not interested if we get a GET (no pun intended).
			 */
			String requestMethod = exchange.getRequestMethod();

			if (requestMethod.equalsIgnoreCase("POST")) {

				/*
				 * Extract the body of the POST request
				 */
				byte[] bodyTemp = new byte[32];
				exchange.getRequestBody().read(bodyTemp);
				String body = new String(bodyTemp);

				/*
				 * body is (hopefully) in the form 
				 * id=1&action=on
				 */

				/*
				 * pairs contains
				 * [id=1, action=on]
				 */
				String[] pairs = body.split("&");

				/*
				 * id = 1
				 */
				String id = pairs[0].split("=")[1];

				/*
				 * action = on
				 * trim to remove the trailing end-of-line character(?) which confuses XML-RPC when I create a new String (presumably with its own
				 * end-of-line character, e.g. two in a row & appears as 0x0 to XML-RPC, which is the unicode null character).
				 */
				String action = pairs[1].split("=")[1].trim();

				/*
				 * First check that the id is known in the database.
				 */
				try {
					Class.forName("com.mysql.jdbc.Driver");
				} catch (ClassNotFoundException e) {
					appendToOutput("Problem loading MySQL database driver.");

					Headers responseHeaders = exchange.getResponseHeaders();
					responseHeaders.set("Content-Type", "text/plain");
					exchange.sendResponseHeaders(400, 0);

					OutputStream responseBody = exchange.getResponseBody();
					Headers requestHeaders = exchange.getRequestHeaders();
					Set<String> keySet = requestHeaders.keySet();
					Iterator<String> iter = keySet.iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						List values = requestHeaders.get(key);
						String s2 = key + " = " + values.toString() + "\n";
						responseBody.write(s2.getBytes());
					}
					responseBody.close();

					return;
				}
				appendToOutput("MySQL driver loaded...");

				Connection connect;

				try {
					connect = DriverManager.getConnection(mysqlAddress, mysqlUser, mysqlPass);
				} catch (SQLException e) {
					appendToOutput("Problem connecting to MySQL database.");

					Headers responseHeaders = exchange.getResponseHeaders();
					responseHeaders.set("Content-Type", "text/plain");
					exchange.sendResponseHeaders(400, 0);

					OutputStream responseBody = exchange.getResponseBody();
					Headers requestHeaders = exchange.getRequestHeaders();
					Set<String> keySet = requestHeaders.keySet();
					Iterator<String> iter = keySet.iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						List values = requestHeaders.get(key);
						String s2 = key + " = " + values.toString() + "\n";
						responseBody.write(s2.getBytes());
					}
					responseBody.close();

					return;
				}
				appendToOutput("Connected to MySQL database...");

				Statement s;

				try {
					s = connect.createStatement();
				} catch (SQLException e) {
					appendToOutput("Problem creating MySQL statement.");

					Headers responseHeaders = exchange.getResponseHeaders();
					responseHeaders.set("Content-Type", "text/plain");
					exchange.sendResponseHeaders(400, 0);

					OutputStream responseBody = exchange.getResponseBody();
					Headers requestHeaders = exchange.getRequestHeaders();
					Set<String> keySet = requestHeaders.keySet();
					Iterator<String> iter = keySet.iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						List values = requestHeaders.get(key);
						String s2 = key + " = " + values.toString() + "\n";
						responseBody.write(s2.getBytes());
					}
					responseBody.close();

					return;
				}

				ResultSet rs = null;

				try {
					rs = s.executeQuery("SELECT * FROM actuators where id = '" + id + "'");
				} catch (SQLException e) {
					cp.appendToOutput("Problem executing MySQL statement.");

					Headers responseHeaders = exchange.getResponseHeaders();
					responseHeaders.set("Content-Type", "text/plain");
					exchange.sendResponseHeaders(400, 0);

					OutputStream responseBody = exchange.getResponseBody();
					Headers requestHeaders = exchange.getRequestHeaders();
					Set<String> keySet = requestHeaders.keySet();
					Iterator<String> iter = keySet.iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						List values = requestHeaders.get(key);
						String s2 = key + " = " + values.toString() + "\n";
						responseBody.write(s2.getBytes());
					}
					responseBody.close();

					return;
				}

				String dbId, dbAddress;

				try {
					rs.next();
					dbId = rs.getString("id");
					dbAddress = rs.getString("address");
				}
				catch (SQLException e) {
					cp.appendToOutput("Problem processing results from MySQL statement, actuator probably not recognised by database.");

					Headers responseHeaders = exchange.getResponseHeaders();
					responseHeaders.set("Content-Type", "text/plain");
					exchange.sendResponseHeaders(400, 0);

					OutputStream responseBody = exchange.getResponseBody();
					Headers requestHeaders = exchange.getRequestHeaders();
					Set<String> keySet = requestHeaders.keySet();
					Iterator<String> iter = keySet.iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						List values = requestHeaders.get(key);
						String s2 = key + " = " + values.toString() + "\n";
						responseBody.write(s2.getBytes());
					}
					responseBody.close();

					return;
				}

				try {
					connect.close();
				} catch (SQLException e1) {
					appendToOutput("Problem explicitly closing the database connection, probably already automatically closed.");
				}

				/*
				 * If execution gets this far then the command must be for an actuator which the Control Panel knows about by id &
				 * has an address for. See about sending it on.
				 */
				cp.appendToOutput("Receieved an actuator command from the virtual world for a recognised actuator.");

				/*
				 * Check whether forwarding is paused though!
				 */
				if (cp.actuatorPauseVwToActuatorCheck.isSelected()) {
					cp.appendToOutput("VW to actuator pause is enabled, not forwarding.");
					Headers responseHeaders = exchange.getResponseHeaders();
					responseHeaders.set("Content-Type", "text/plain");
					exchange.sendResponseHeaders(400, 0);

					OutputStream responseBody = exchange.getResponseBody();
					Headers requestHeaders = exchange.getRequestHeaders();
					Set<String> keySet = requestHeaders.keySet();
					Iterator<String> iter = keySet.iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						List values = requestHeaders.get(key);
						String s2 = key + " = " + values.toString() + "\n";
						responseBody.write(s2.getBytes());
					}
					responseBody.close();

					return;
				}

				cp.appendToOutput("Forwarding actuator command.");

				/*
				 * Make the XML-RPC request, depending upon the result (true/false) send back a 200 or 400 HTTP response.
				 */

				if (cp.SendXMLRPCRequest(dbAddress, id, action)) {

					Headers responseHeaders = exchange.getResponseHeaders();
					responseHeaders.set("Content-Type", "text/plain");
					exchange.sendResponseHeaders(200, 0);

					OutputStream responseBody = exchange.getResponseBody();
					Headers requestHeaders = exchange.getRequestHeaders();
					Set<String> keySet = requestHeaders.keySet();
					Iterator<String> iter = keySet.iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						List values = requestHeaders.get(key);
						String s2 = key + " = " + values.toString() + "\n";
						responseBody.write(s2.getBytes());
					}
					responseBody.close();

					cp.appendToOutput("XML-RPC request successfully completed.");

					return;
				}

				cp.appendToOutput("Problem executing XML-RPC request.");

				Headers responseHeaders = exchange.getResponseHeaders();
				responseHeaders.set("Content-Type", "text/plain");
				exchange.sendResponseHeaders(400, 0);

				OutputStream responseBody = exchange.getResponseBody();
				Headers requestHeaders = exchange.getRequestHeaders();
				Set<String> keySet = requestHeaders.keySet();
				Iterator<String> iter = keySet.iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					List values = requestHeaders.get(key);
					String s2 = key + " = " + values.toString() + "\n";
					responseBody.write(s2.getBytes());
				}
				responseBody.close();

				return;
			}

		}
	}

}