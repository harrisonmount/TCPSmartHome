 /*
 * 
 */
package sh.net.tcp;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import sh.app.SHClient;
import sh.app.protocol.SHProtocol;

public class SHTCPClient {

	public SHTCPClient() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// server name and port
		String hostname = "localhost";
		int port = 50010;
				
		try {
			// Create a Socket and connect to server
			Socket comm = new Socket(hostname,port);
					
			// Run the protocol
			SHProtocol proto = new SHProtocol(comm);
			SHClient client = new SHClient(proto);
			client.run();
			
			
			// close the Socket
			comm.close();
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
