
package plain.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Client
{
	
	private Socket socket;

	
	public Client(Socket socket)
	{
		this.socket = socket;
	}

	public void setSocket( Socket socket )
	{
		
		this.socket = socket;
	}
	
	public Socket getSocket()
	{
		
		return this.socket;
	}

	public PrintWriter getPrintWriter() throws IOException
	{
		
		return new PrintWriter( socket.getOutputStream() );
	}
	
}
