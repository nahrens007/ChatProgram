package client;

/**
 * Created on January 10th, 2014
 * Author: Nathan
 */

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class Client {

	// Instantiates required objects

	private JTextArea incoming;
	private JTextField outgoing;
	private JTextField ipFeild;
	private JTextField idFeild;
	private BufferedReader reader;
	private PrintWriter writer;
	private Socket sock;
	private String IP;
	private String userID;
	private String settingPath;
	private FileWriter fileWriter;

	/**
	 * The method go() is the only method called by main(). It starts the app
	 * up.
	 */
	public void go() {

		// sets the JPanel up for the GUI
		JPanel mainPanel = new JPanel();

		// Prepares JTextArea for incoming messages
		incoming = new JTextArea(25, 30);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);

		// places the incoming message text area in a scroll pane and formats
		// the scroll pane
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(
						ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// sets the JTextFields up for outgoing messages, the IP, and the user
		// name
		outgoing = new JTextField(22);
		outgoing.addActionListener(new SendButtonListener());
		outgoing.setToolTipText("Enter message to send");

		idFeild = new JTextField(28);
		idFeild.setText("Username");
		idFeild.setToolTipText("Enter user ID");

		ipFeild = new JTextField(20);
		ipFeild.setText("Enter IP");
		ipFeild.setToolTipText("Enter IP to connect to");

		// sets the buttons up for sending messages and connecting to a server
		// and adds listeners to them
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendButtonListener());

		JButton connectButton = new JButton("Connect");
		connectButton.addActionListener(new ConnectButtonListener());

		JButton disconnectButton = new JButton("Disconnect");
		disconnectButton.addActionListener(new DisconnectButtonListener());

		// adds content to the GUI panel
		mainPanel.add(qScroller);
		mainPanel.add(outgoing);
		mainPanel.add(sendButton);
		mainPanel.add(idFeild);
		mainPanel.add(ipFeild);
		mainPanel.add(connectButton);
		mainPanel.add(disconnectButton);

		// sets the JFrame up and adds the panel to it
		JFrame frame = new JFrame("Chat Client 1.0 BETA");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		frame.setSize(380, 600);
		frame.setVisible(true);
		loadSettings(new File("settings.txt"));
	}

	/**
	 * This method attempts to connect to the server at the specified IP
	 * address.
	 */
	public void setUpNetworking() {

		try {
			sock = new Socket(IP, 3440);
			InputStreamReader streamReader = new InputStreamReader(
							sock.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(sock.getOutputStream());
			incoming.append("Connected. \n");
		} catch (ConnectException e) {
			incoming.append("Connection failed. \n");
		} catch (IOException ex) {
			ex.getMessage();
			System.out.println("setUpNetwork() exception: " + ex.getClass());
		}
	}

	/**
	 * This method closes the writer and reader stream.
	 */
	private void disconnect() {

		if (writer != null)
			writer.close();
		if (reader != null)
			try {
				reader.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
				System.out.println("disconnect() reader exception: "
								+ e.getClass());
			}
		if (sock != null)
			try {

				sock.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
				System.out.println("disconnect() socket exception: "
								+ e.getClass());
			}
	}

	/**
	 * When the send button is pressed (or enter is pressed while the input text
	 * field has focus), the text in the input field attempts to be sent to the
	 * server.
	 * 
	 * @author Nathan
	 *
	 */
	private class SendButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent ev) {

			try {
				writer.println(userID + ": " + outgoing.getText());
				writer.flush();
			} catch (NullPointerException e) {
				System.out.println(
								"Tried to send something to a non-existent server.");
			} catch (Exception ex) {
				System.out.println("SendButtonListener exception: "
								+ ex.getClass());
			}
			outgoing.setText("");
			outgoing.requestFocus();
		}
	}

	/**
	 * This method starts another thread in order to continually checks the
	 * reader stream in order to check if there is a new message from the
	 * server. If there is, it is appended to the incoming text area.
	 * 
	 * @author Nathan
	 *
	 */
	public class IncomingReader implements Runnable {

		public void run() {

			String message;
			try {
				while ((message = reader.readLine()) != null) {
					incoming.append(message + "\n");
					incoming.setCaretPosition(
									incoming.getDocument().getLength());// scrolls
																		// to
																		// bottom
				}
			} catch (SocketException e) {
				incoming.append("Disconnected from server\n");
			} catch (NullPointerException e) {
				// Do nothing when the pointer is null;
				System.out.println(
								"Tried to read something from a non-existent server.");
			} catch (Exception ex) {
				System.out.println(
								"IncomingReader exception: " + ex.getClass());
			}
		}
	}

	/**
	 * When the connect button is pressed, this listener attempts to connect to
	 * a server using the IP address in the IP text field.
	 * 
	 * @author Nathan
	 *
	 */
	public class ConnectButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {

			// Make sure that you disconnect from the current connection before
			// connecting to another one.
			disconnect();

			userID = idFeild.getText();
			IP = ipFeild.getText();
			setUpNetworking();
			Thread readerThread = new Thread(new IncomingReader());
			readerThread.start();

			// saves IP and user name to a file
			settingPath = "settings.txt";
			try {
				fileWriter = new FileWriter(settingPath);
				fileWriter.write(IP + "/" + userID);
				fileWriter.write("\n");
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ConnectButtonListener exception:"
								+ e.getClass());
			}

		}
	}

	/**
	 * This listener responds to the disconnect button being pressed.
	 * 
	 * @author Nathan
	 *
	 */
	private class DisconnectButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			disconnect();
		}

	}

	/**
	 * This method loads the settings file passed in to it and attempts to load
	 * the last used IP and ID.
	 * 
	 * @param file
	 */
	public void loadSettings(File file) {

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] result = line.split("/");
				// System.out.println(result[0]);
				ipFeild.setText(result[0]);
				// System.out.println(result[1]);
				idFeild.setText(result[1]);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			incoming.append("Couldn't load settings file. Please enter IP and user name.\n");
		} catch (IOException e) {
			System.out.println("loadSettings() exception:" + e.getClass());
		}
	}

	public static void main(String[] args) {

		new Client().go();
	}

}
