/**
 * Created on January 10th, 2014
 * @author Nathan
 */

package client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class Client
{
	
	// These fields are used as a prefix for all messages sent.
	/**
	 * This field signals the closing of a connection so the server can remove
	 * the client from its list of receiving participants.
	 */
	private static final String CLOSE = "CLS";
	/**
	 * This field signals a normal message to be broadcasted to all clients.
	 */
	private static final String MESSAGE = "MSG";
	/**
	 * This field signals setting the username of the client.
	 */
	private static final String SET_USERNAME = "SUN";
	/**
	 * This field signals that the client is requesting his UUID.
	 */
	private static final String GET_UUID = "GID";
	
	// Instantiates required objects
	private JTextArea incoming;
	private JTextField outgoing;
	private JTextField ipFeild;
	private JTextField idFeild;
	private BufferedReader reader;
	private PrintWriter writer;
	private Socket sock;
	private String IP;
	private String username;
	private String settingPath;
	private FileWriter fileWriter;
	
	/**
	 * The method go() is the only method called by main(). It starts the
	 * application up.
	 */
	public void runClient()
	{
		
		setupWindow();
		
		// Loads the settings for the application.
		loadSettings( new File( "settings.txt" ) );
	}
	
	/**
	 * This method sets up the window.
	 */
	private void setupWindow()
	{
		
		// sets the JPanel up for the GUI
		JPanel mainPanel = new JPanel();
		
		// Prepares JTextArea for incoming messages
		incoming = new JTextArea( 25, 30 );
		incoming.setLineWrap( true );
		incoming.setWrapStyleWord( true );
		incoming.setEditable( false );
		
		// places the incoming message text area in a scroll pane and formats
		// the scroll pane
		JScrollPane qScroller = new JScrollPane( incoming );
		qScroller.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		qScroller.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		
		// sets the JTextFields up for outgoing messages, the IP, and the user
		// name
		outgoing = new JTextField( 22 );
		outgoing.addActionListener( new SendButtonListener() );
		outgoing.setToolTipText( "Enter message to send" );
		
		idFeild = new JTextField( 28 );
		idFeild.setText( "Username" );
		idFeild.setToolTipText( "Enter user ID" );
		
		ipFeild = new JTextField( 20 );
		ipFeild.setText( "Enter IP" );
		ipFeild.setToolTipText( "Enter IP to connect to" );
		
		// sets the buttons up for sending messages and connecting to a server
		// and adds listeners to them
		JButton sendButton = new JButton( "Send" );
		sendButton.addActionListener( new SendButtonListener() );
		
		JButton connectButton = new JButton( "Connect" );
		connectButton.addActionListener( new ConnectButtonListener() );
		
		JButton disconnectButton = new JButton( "Disconnect" );
		disconnectButton.addActionListener( new DisconnectButtonListener() );
		
		// adds content to the GUI panel
		mainPanel.add( qScroller );
		mainPanel.add( outgoing );
		mainPanel.add( sendButton );
		mainPanel.add( idFeild );
		mainPanel.add( ipFeild );
		mainPanel.add( connectButton );
		mainPanel.add( disconnectButton );
		
		// sets the JFrame up and adds the panel to it
		JFrame frame = new JFrame( "Chat Client 1.0 BETA" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.getContentPane().add( BorderLayout.CENTER, mainPanel );
		frame.setSize( 380, 600 );
		frame.setVisible( true );
	}
	
	/**
	 * This method attempts to connect to the server at the specified IP
	 * address.
	 * 
	 * @throws ConnectException
	 *             It will be thrown if the connection to the server is
	 *             unsuccessful.
	 */
	public void setUpNetworking() throws ConnectException
	{
		
		try
		{
			sock = new Socket( IP, 34400 );
			InputStreamReader streamReader = new InputStreamReader( sock.getInputStream() );
			reader = new BufferedReader( streamReader );
			
			// Set up the writer and send the server the client's username
			writer = new PrintWriter( sock.getOutputStream() );
			writer.println( Client.SET_USERNAME + ":" + username );
			writer.flush();
			
			incoming.append( "Connected. \n" );
		} catch ( ConnectException e )
		{
			throw new ConnectException();
		} catch ( UnknownHostException e )
		{
			incoming.append( "Unknown host." );
		} catch ( IOException e )
		{
			System.out.println( "IOException in setUpNetworking()" );
		} catch ( Exception e )
		{
			e.getMessage();
			System.out.println( "setUpNetwork() exception: " + e.getClass() );
		}
	}
	
	/**
	 * This method closes the writer and reader stream.
	 */
	private void disconnect()
	{
		
		if ( writer != null )
		{
			writer.println( Client.CLOSE ); // Send the "CLOSE" signal to
											// the server
			writer.close();
		}
		if ( reader != null )
			try
			{
				reader.close();
			} catch ( IOException e )
			{
				System.out.println( e.getMessage() );
				System.out.println( "disconnect() reader exception: " + e.getClass() );
			}
		if ( sock != null )
			try
			{
				
				sock.close();
			} catch ( IOException e )
			{
				System.out.println( e.getMessage() );
				System.out.println( "disconnect() socket exception: " + e.getClass() );
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
	public class IncomingReader implements Runnable
	{
		
		public void run()
		{
			
			String message;
			try
			{
				while ( (message = reader.readLine()) != null )
				{
					incoming.append( message + "\n" );
					incoming.setCaretPosition( incoming.getDocument().getLength() );// scrolls
																					// to
																					// bottom
				}
			} catch ( SocketException e )
			{
				incoming.append( "Disconnected from server\n" );
				incoming.setCaretPosition( incoming.getDocument().getLength() );
			} catch ( NullPointerException e )
			{
				// Do nothing when the pointer is null;
				System.out.println( "Tried to read something from a non-existent server." );
			} catch ( IOException e )
			{
			
			} catch ( Exception e )
			{
				System.out.println( "IncomingReader exception: " + e.getClass() );
			}
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
	private class SendButtonListener implements ActionListener
	{
		
		public void actionPerformed( ActionEvent ev )
		{
			
			try
			{
				writer.println( Client.MESSAGE + ":" + outgoing.getText() );
				writer.flush();
			} catch ( NullPointerException e )
			{
				System.out.println( "Tried to send something to a non-existent server." );
			} catch ( Exception ex )
			{
				System.out.println( "SendButtonListener exception: " + ex.getClass() );
			}
			outgoing.setText( "" );
			outgoing.requestFocus();
		}
	}
	
	/**
	 * When the connect button is pressed, this listener attempts to connect to
	 * a server using the IP address in the IP text field.
	 * 
	 * @author Nathan
	 * 		
	 */
	public class ConnectButtonListener implements ActionListener
	{
		
		public void actionPerformed( ActionEvent event )
		{
			
			// Make sure that you disconnect from the current connection before
			// connecting to another one.
			disconnect();
			
			username = idFeild.getText();
			IP = ipFeild.getText();
			
			// Try to set up the networking. If unsuccessful, then do not start
			// the reader thread.
			try
			{
				setUpNetworking();
				Thread readerThread = new Thread( new IncomingReader() );
				readerThread.start();
			} catch ( ConnectException e )
			{
				incoming.append( "Connection failed. \n" );
			}
			
			// saves IP and user name to a file
			settingPath = "settings.txt";
			try
			{
				fileWriter = new FileWriter( settingPath );
				fileWriter.write( IP + "/" + username );
				fileWriter.write( "\n" );
				fileWriter.close();
			} catch ( IOException e )
			{
				e.printStackTrace();
				System.out.println( "ConnectButtonListener exception:" + e.getClass() );
			}
			
			outgoing.requestFocus();
			
		}
	}
	
	/**
	 * This listener responds to the disconnect button being pressed.
	 * 
	 * @author Nathan
	 * 		
	 */
	private class DisconnectButtonListener implements ActionListener
	{
		
		@Override
		public void actionPerformed( ActionEvent e )
		{
			
			disconnect();
		}
		
	}
	
	/**
	 * This method loads the settings file passed in to it and attempts to load
	 * the last used IP and ID.
	 * 
	 * @param file
	 */
	public void loadSettings( File file )
	{
		
		try
		{
			BufferedReader reader = new BufferedReader( new FileReader( file ) );
			String line = null;
			while ( (line = reader.readLine()) != null )
			{
				String[] result = line.split( "/" );
				// System.out.println(result[0]);
				ipFeild.setText( result[0] );
				// System.out.println(result[1]);
				idFeild.setText( result[1] );
			}
			reader.close();
		} catch ( FileNotFoundException e )
		{
			incoming.append( "Couldn't load settings file. Please enter IP and user name.\n" );
		} catch ( IOException e )
		{
			System.out.println( "loadSettings() exception:" + e.getClass() );
		}
	}
	
	public static void main( String[] args )
	{
		
		new Client().runClient();
	}
	
}
