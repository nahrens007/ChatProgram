
package server;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class Server
{
	
	private final static int SOCKET = 3440;
	ArrayList<Client> clients;
	
	/**
	 * This inner class handles each individual client that connects using a new
	 * thread for each client.
	 * 
	 * @param clientSocket
	 */
	public class ClientHandler implements Runnable
	{
		
		BufferedReader reader;
		
		/**
		 * This constructor handles the connection of the client using the
		 * client's socket.
		 * 
		 * @param clientSocket
		 */
		public ClientHandler(Client client)
		{
			
			try
			{
				// Get the client's input stream so that we can read messages
				// from the client.
				reader = new BufferedReader(
						new InputStreamReader( client.getSocket().getInputStream() ) );
			} catch ( IOException e )
			{
				System.out.println( "ClientHandler() exception: " + e.getMessage() );
			}
		}
		
		/**
		 * This implemented method continuously waits for messages from clients,
		 * and when received, broadcasts it to everyone.
		 */
		public void run()
		{
			
			String message;
			try
			{
				// Wait for a message to be sent from the client and handle it
				while ( (message = reader.readLine()) != null )
				{
					System.out.println( "read " + message );
					broadcast( message );
				}
			} catch ( IOException e )
			{
				System.out.println( "ClientHandler.run() exception: " + e.getMessage() );
			}
		}
	}
	
	/**
	 * This method listens for new clients connecting and handles their
	 * connection.
	 */
	public void runServer()
	{
		
		clients = new ArrayList<Client>();
		try
		{
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket( SOCKET );
			
			while ( true )
			{
				// Accept any new client and adds it to the clients array list
				Socket clientSocket = serverSocket.accept();
				Client client = new Client( clientSocket );
				clients.add( client );
				
				// Create a new thread to listen to that client
				Thread t = new Thread( new ClientHandler( client ) );
				t.start();
				broadcast( "[SERVER]User connected." );
				
			}
		} catch ( IOException e )
		{
			System.out.println( "go() exception: " + e.getMessage() );
		}
	}
	
	/**
	 * This method broadcasts the message to everyone on the server.
	 * 
	 * @param message
	 *            The String message to be sent to all connected clients.
	 */
	public void broadcast( String message )
	{
		
		Iterator<Client> clientIt = clients.iterator();
		while ( clientIt.hasNext() )
		{
			try
			{
				// Get the clients PrintWriter stream so that we can send
				// messages to it.
				PrintWriter writer = clientIt.next().getPrintWriter();
				// Send the message to the client
				writer.println( message );
				writer.flush();
			} catch ( Exception e )
			{
				System.out.println( "broadcast() exception: " + e.getMessage() );
			}
		}
		
		// Log the message.
		log( message );
		
	}
	
	/**
	 * This method logs the message in a text file (appends to it). The file is
	 * named in the format of "[DATE]_log.txt"
	 * 
	 * @param logMsg
	 */
	public void log( String logMsg )
	{
		
		String date = String.format( "%tY%<tB%<td", new Date() );
		String filePath = date + "_log.txt";
		// writes everything to a log file
		try
		{
			FileWriter fileWriter = new FileWriter( filePath, true );
			fileWriter.write( logMsg + "\n" );
			fileWriter.close();
		} catch ( IOException e )
		{
			System.out.println( "Couldn't write to log file" );
			System.out.println( "log() exception: " + e.getMessage() );
		}
	}
	
	public static void main( String[] args )
	{
		
		System.out.println( "Starting the server..." );
		new Server().runServer();
	}
}
