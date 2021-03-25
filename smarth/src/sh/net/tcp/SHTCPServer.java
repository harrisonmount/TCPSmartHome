/**
 * 
 */
//package net.tcp;
package sh.net.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import sh.app.SHServer;
import sh.app.protocol.SHProtocol;

/**
 * 
 */
public class SHTCPServer {

	/**
	 * 
	 */
	public SHTCPServer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int port = 50010;
		int backlog = 5;
		
		try {
			// Create a ServerSocket and bind to port
			ServerSocket ss = new ServerSocket(port, backlog);
			
			while(true) { // run forever
				System.out.println("Listening on "+port+" ...");
				// wait for a connection
				Socket comm = ss.accept();
				
				// Run the protocol
				SHProtocol proto = new SHProtocol(comm);
				SHServer server = new SHServer(proto);
				server.run();
				
				
				// Close the communication socket
				comm.close();
			}
			
			//ss.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
