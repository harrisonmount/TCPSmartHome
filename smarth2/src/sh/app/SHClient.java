/**
 * 
 */
package sh.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import sh.app.message.MCmds;
import sh.app.message.Message;
import sh.app.protocol.SHProtocol;
import sh.app.protocol.SHProtocolException;

/**
 * @author nigel
 *
 */
public class SHClient {
	
	private SHProtocol proto;

	/**
	 * 
	 */
	public SHClient(SHProtocol p) {
		// TODO Auto-generated constructor stub
		
		proto = p;
	}
	
	public void test() {
		// for user input
		BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
		
		
		try {
			// send START
			Message ms = new Message();
		
			proto.putMessage(ms);
		
			// get username
			Message mr = proto.getMessage();
		
			System.out.println(mr.getBody());
			System.out.print("-->");
		
			String in = bin.readLine();
			
			ms.clear();
			ms.setCmd(MCmds.USER);
			ms.addParam("user",in);
			proto.putMessage(ms);
			
			// get password
			mr = proto.getMessage();
		
			System.out.println(mr.getBody());
			System.out.print("-->");
		
			in = bin.readLine();
			
			ms.clear();
			ms.setCmd(MCmds.PASS);
			ms.addParam("pass",in);
			proto.putMessage(ms);
			
			proto.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SHProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SHException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		// for user input
		BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			// send START
			Message ms = new Message();
			proto.putMessage(ms);
			Message mr;
			
			while(true) {
				mr = proto.getMessage();
				System.out.println(mr.getBody());
				System.out.print(": ");
				String in = bin.readLine();
				
				ms.clear();
				ms.setCmd(MCmds.CHOICE);
				ms.addParam(mr.getParam("1"),in);
				proto.putMessage(ms);
				
				mr.clear();
			}
		} catch (SHException e) {
			//e.printStackTrace();
			shutdown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			shutdown();
		}
	}
	
	public void shutdown() {
		try {
			proto.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

