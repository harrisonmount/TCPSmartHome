package sh.app.message;

import java.util.HashMap;
import java.util.Vector;

import sh.app.SHException;

public class Message {
	
	static String CRLF = "\r\n";
	static String SPACE = " ";
	static String PJOIN = "&"; // join 
	static String VJOIN = "="; // join param_name to param_value

	private String cmd;
	private HashMap<String, String> params;
	private Vector<String> body;
	private int bodyLines;
	
	/**
	 * 
	 */
	public Message() {
		// TODO Auto-generated constructor stub
		clear();
	}
	
	public Message(String s) throws SHException {
		unmarshal(s);
	}
	
	public void clear() {
		cmd = MCmds.START.toString();
		params = new HashMap<String,String>();
		addParam("lines","0");
		body = null;
		bodyLines = 0;
	}
	
	public void setCmd(MCmds c) {
		cmd = c.toString();
	}
	
	public MCmds getCmd() {
		return MCmds.valueOf(cmd);
	}
	
	public void addParam(String pname, String pvalue) {
		if (params==null) params = new HashMap<String, String>();
		params.put(pname, pvalue);
	}
	
	public String getParam(String pname) {
		return params.get(pname);
	}
	
	public void addLineToBody(String line) {
		if (body==null) body = new Vector<String>();
		body.add(line);
		bodyLines++;
	}
	
	public void addLinesToBody(String lines) {
		String line[] = lines.split(CRLF);
		for(String l : line) {
			addLineToBody(l);
		}
	}
	
	public String getBody() {
		StringBuilder b = new StringBuilder();
		if (body!=null) {
			for(int i=0;i<body.size();i++) {
				b.append(body.get(i));
				b.append(CRLF);
			}
		}
		return b.toString();
	}
	
	public String marshal() {
		addParam("lines", Integer.toString(bodyLines));
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(cmd);
		sb.append(CRLF);
		
		if (params!=null) {
			StringBuilder p = new StringBuilder();
			for (String key : params.keySet()) {
				p.append(key);
				p.append(VJOIN);
				p.append(params.get(key));
				p.append(PJOIN);
			}
			p.deleteCharAt(p.length()-1);
			sb.append(p.toString());
			sb.append(CRLF);
		}
		
		if (body!=null) {
			StringBuilder b = new StringBuilder();
			for(int i=0;i<body.size();i++) {
				b.append(body.get(i));
				b.append(CRLF);
			}
			sb.append(b.toString());
		}
		
		return sb.toString();
	}
	
	public void unmarshal(String s) throws SHException {
		String cmds[] = s.split(CRLF);
		if (cmds.length<2) throw new SHException("unmarshal: not enough lines");
		cmd = cmds[0];
		if (cmds.length>1) {
			String params[] = cmds[1].split(PJOIN);
			if (params.length<1) throw new SHException("unmarshal: not enough params");
			for (String param : params) {
				String kv[] = param.split(VJOIN);
				if (kv.length<2) throw new SHException("unmarshal: bad param");
				addParam(kv[0],kv[1]);
			}
		}
		if (cmds.length>2) {
			for(int i=2;i<cmds.length;i++) {
				addLineToBody(cmds[i]);
			}
		}
	}
	
	public String toString() {
		return marshal();
	}

}
