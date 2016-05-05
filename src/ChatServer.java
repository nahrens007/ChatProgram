import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

	private final static int SOCKET = 3440;
	ArrayList clientOutputStreams;

	/**
	 * This inner class handles each individual client that connects using a new
	 * thread for each client.
	 * 
	 * @param clientSocket
	 */
	public class ClientHandler implements Runnable {

		BufferedReader reader;
		Socket socket;

		/**
		 * This constructor handles the connection of the client using the
		 * client's socket.
		 * 
		 * @param clientSocket
		 */
		public ClientHandler(Socket clientSocket) {

			try {
				// Get the client's input stream so that we can read messages
				// from the client.
				socket = clientSocket;
				InputStreamReader isReader = new InputStreamReader(
								socket.getInputStream());
				reader = new BufferedReader(isReader);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}

		/**
		 * This implemented method continuously reads for messages from clients,
		 * and when received, broadcasts it to everyone.
		 */
		public void run() {

			String message;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("read " + message);
					tellEveryone(message);
				}
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	/**
	 * This method listens for new clients connecting and handles their
	 * connection.
	 */
	public void go() {

		clientOutputStreams = new ArrayList();
		try {
			ServerSocket serverSock = new ServerSocket(SOCKET);

			while (true) {
				// Accept any new client
				Socket clientSocket = serverSock.accept();

				// Create the client's writer so the server can read it.
				PrintWriter writer = new PrintWriter(
								clientSocket.getOutputStream());

				// Add the new client's writer to the list of clients so that
				// the server can send messages to them.
				clientOutputStreams.add(writer);

				// Create a new thread to listen to that client
				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();
				tellEveryone("[SERVER]User connected.");
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	/**
	 * This method broadcasts the message to everyone on the server.
	 * 
	 * @param message
	 */
	public void tellEveryone(String message) {

		Iterator it = clientOutputStreams.iterator();
		while (it.hasNext()) {
			try {
				PrintWriter writer = (PrintWriter) it.next();
				writer.println(message);
				writer.flush();
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}

		log(message);

	}

	/**
	 * This method logs the message in a text file (appends to it). The file is
	 * named in the format of "[DATE]_log.txt"
	 * 
	 * @param logMsg
	 */
	public void log(String logMsg) {

		String date = String.format("%tY%<tB%<td", new Date());
		String filePath = date + "_log.txt";
		// writes everything to a log file
		try {
			FileWriter fileWriter = new FileWriter(filePath, true);
			fileWriter.write(logMsg + "\n");
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("Couldn't write to log file");
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) {

		new ChatServer().go();
	}
}
