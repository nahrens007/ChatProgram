
package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Client
{
	
	private Socket socket;
	private int uuid;
	private String username;
	
	public Client(Socket socket)
	{
		this.socket = socket;
	}
	
	public Client(Socket socket, int uuid, String username)
	{
		this.socket = socket;
		this.uuid = uuid;
		this.username = username;
	}
	
	public void setSocket( Socket socket )
	{
		
		this.socket = socket;
	}
	
	public void setUUID( int uuid )
	{
		
		this.uuid = uuid;
	}
	
	public void setUsername( String username )
	{
		
		this.username = username;
	}
	
	public Socket getSocket()
	{
		
		return this.socket;
	}
	
	public int getUUID()
	{
		
		return this.uuid;
	}
	
	public String getUsername()
	{
		
		return this.username;
	}
	
	public PrintWriter getPrintWriter() throws IOException
	{
		
		return new PrintWriter( socket.getOutputStream() );
	}
	
}
