/**
 * 
 */
package sh.app.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import sh.app.SHException;
import sh.app.message.Message;

/**
 * @author nigel
 *
 */
public class SHProtocol {
	
	private Socket sock;
	private BufferedReader bin;
	private PrintWriter bout;
	
	private void _setup(Socket s) throws IOException {
		sock = s;
		bin = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		bout = new PrintWriter(sock.getOutputStream());
	}

	/**
	 * 
	 */
	public SHProtocol() {
		// TODO Auto-generated constructor stub
		sock = null;
		bin = null;
		bout = null;
	}
	
	public SHProtocol(Socket s) {
		try {
			_setup(s);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void close() throws IOException {
		bin.close();
		bout.close();
		sock.close();
	}
	
	public boolean putMessage(Message m) throws SHProtocolException {
		boolean ret = true;
		
		String mess = m.marshal();
		if (bout!=null) {
			bout.print(mess);
			bout.flush();
		} else {
			throw new SHProtocolException("putMessage: write error");
		}
		
		return ret;
	}
	
	public Message getMessage() throws SHProtocolException, SHException {
		Message ret = null;
		if (bin!=null) {
			try {
				String l1 = bin.readLine();
				String l2 = bin.readLine();
				ret = new Message(l1+"\r\n"+l2);
				int lines = Integer.parseInt(ret.getParam("lines"));
				for(int i = 0;i<lines;i++) {
					String line = bin.readLine();
					ret.addLineToBody(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new SHProtocolException("getMessage: read error");
			} catch (NumberFormatException e) {
				e.printStackTrace();
				throw new SHProtocolException("getMessage: unable to convert lines");
			}
		}
		
		return ret;
	}

}
