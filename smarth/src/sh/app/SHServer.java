/**
 * 
 */
package sh.app;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import sh.app.Home;
import sh.app.Light;
import sh.app.Room;

import sh.app.message.MCmds;
import sh.app.message.Message;
import sh.app.protocol.SHProtocol;
import sh.app.protocol.SHProtocolException;

public class SHServer {
	
	private boolean debug;
	private Home myHouse;
	private boolean login;
	private SHProtocol proto;
	private String mLevel;
	
	private Room currentroom; 
	private Light currentlight;
	private Security currentdevice;
	
	private String lighteditchoice;
	private String securityeditchoice;
	
	private String lightname;
	private boolean lightstate;
	private int lightlevel;
	private String lightcolor;
	
	private void _init() {
		// create a dummy Home
		myHouse = new Home("username", "pass");
		myHouse.addRoom("Living Room");
		myHouse.addRoom("Kitchen");
		myHouse.addRoom("Dining Room");
		myHouse.addRoom("Bedroom");
		myHouse.addDevice("Lock 1");
		myHouse.addDevice("Lock 2");
		myHouse.addDevice("Lock 3");
		myHouse.addDevice("Lock 4");
		myHouse.addDevice("Alarm");
		
		Collection<Room> rooms = myHouse.getRooms();
		HashMap<String, Room> mEntries = new HashMap<String, Room>();
		//Initializes one "Standard Light" in each room
		for(Room room : rooms) {
			room.addLight("Standard Light");;
		};
		login = false;
		proto = null;
		debug = true;
		mLevel = "main";
	}
	
	private void _debug(String s, Message m) {
		if (debug) {
			System.out.println(s);
			System.out.print(m);
		}
	}

	/**
	 * 
	 */
	public SHServer(SHProtocol p) {
		// TODO Auto-generated constructor stub
		_init();
		proto = p;
	}
	
	public void test() {
		try {
			// get START
			Message mr = proto.getMessage();
			System.out.println("Recv'd:");
			System.out.println(mr);
			
			// send USER
			Message ms = new Message();
			ms.setCmd(MCmds.USER);
			ms.addParam("user","none");
			ms.addLineToBody("Enter your username:");
			proto.putMessage(ms);
			
			// get USER
			mr.clear();
			mr = proto.getMessage();
			System.out.println("Recv'd:");
			System.out.println(mr);
			
			// send PASS
			ms.clear();
			ms.setCmd(MCmds.PASS);
			ms.addParam("pass","none");
			ms.addLineToBody("Enter your password:");
			proto.putMessage(ms);
			
			// get PASS
			mr.clear();
			mr = proto.getMessage();
			System.out.println("Recv'd:");
			System.out.println(mr);
			
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
		try {
			// do START
			Message mr = proto.getMessage();
			if (mr.getCmd()!=MCmds.START) {
				throw new SHException("server: bad start message");
			}
			_debug("Recv:", mr);
			
			// do Login
			_doLogin();
			
			// menu loop
			while (login) {
				switch (mLevel) {
					case "main":
						_doNewMainMenu();
						break;
					/*case "display":
						_doDisplayMenu();
						break;*/
					case "displayRooms":
						_doDisplayRooms();
						break;
					case "displaySingleRoom":
						_doDisplaySingleRoom(currentroom);
						break;
					case "displaySingleLight":
						_doDisplaySingleLight(currentlight);
						break;
					case "editSingleLight":
						_doEditSingleLight(currentlight, lighteditchoice);
						break;
					case "displaySecurity":
						_doDisplaySecurity();
						break;
					case "displaySingleSecurity":
						_doDisplaySingleSecurity(currentdevice);
						break;
						
					case "editSecurity":
						_doEditSecurity(currentdevice, securityeditchoice);
						break;
						
					case "displayAllLights":
						_doDisplayAllLights();
						break;
					case "editAllLights":
						_doEditAllLights(lighteditchoice);
						break;
					default:
						login = false;
				}
			}
			shutdown();
			
		} catch (SHException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Shutdown...");
			shutdown();
		}
		
	}
	
	private boolean _doLogin() throws SHException {
		boolean ret = true;
		
		// login:
		// 1. send 'enter username'
		// 2. get username
		// 3. send 'enter password'
		// 4. get password
		// 5. check login
		// 6. if success, return true
		//    else go to 1
		
		try {
			int tries = 0;
			while(!login) {
				// ask for username
				Message ms = new Message();
				
				ms.setCmd(MCmds.USER);
				ms.addParam("pnum","1");
				ms.addParam("1","user");
				ms.addLineToBody("Enter your username:");
				proto.putMessage(ms);
				_debug("Send:", ms);
				
				Message mr = proto.getMessage();
				String uname = mr.getParam("user");
				_debug("Recv:", mr);

				
				// ask for password
				ms.clear();
				ms.setCmd(MCmds.PASS);
				ms.addParam("pnum","1");
				ms.addParam("1","pass");
				ms.addLineToBody("Enter your password:");
				proto.putMessage(ms);
				_debug("Send:", ms);
				
				mr = proto.getMessage();
				String pass = mr.getParam("pass");
				_debug("Recv:", mr);
				
				login = myHouse.checkLogin(uname,pass);
				
				tries++;
				if (tries>3) {
					throw new SHException("doLogin: to many failed attempts");
				}
			}
			
			
		} catch (SHProtocolException e) {
			throw new SHException("doLogin: login error");
		}
		
		
		return ret;
	}
	
	private void _doError(String mess) throws SHException {
		// send mess to client with 'Hit ENTER to continue'
		// empty choice in return
		Message ms = new Message();
		ms.setCmd(MCmds.ERROR);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		ms.addLineToBody(mess);
		ms.addLineToBody("Hit 'Enter' to return:");
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		// do not care what the user typed
		_debug("Recv:", mr);
	}
	
	private void _doMainMenu() throws SHException {
		// send main menu
		// get user choice
		mLevel = "main";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		ms.addLineToBody("Main Menu");
		ms.addLineToBody("1. Display Status");
		ms.addLineToBody("2. Change Status");
		ms.addLineToBody("99. Logout");
		ms.addLineToBody("Enter your choice:");
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		String choice = mr.getParam("choice");
		_debug("Recv:", mr);
		
		// example of a hard coded menu choice
		if (choice.compareTo("1")==0) mLevel = "display";
		else if (choice.compareTo("2")==0) mLevel = "change";
		else if (choice.compareTo("99")==0) mLevel = "logout";
		else _doError("ERROR: Not a valid choice");
		
	}
	
	private void _doNewMainMenu() throws SHException {
		// send main menu
		// get user choice
		mLevel = "main";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		ms.addLineToBody("Main Menu");
		ms.addLineToBody("1. Display Rooms");
		ms.addLineToBody("2. Display Security Devices");
		ms.addLineToBody("3. Display All Lights");

		ms.addLineToBody("99. Logout");
		ms.addLineToBody("Enter your choice:");
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		String choice = mr.getParam("choice");
		_debug("Recv:", mr);
		
		// example of a hard coded menu choice
		if (choice.compareTo("1")==0) mLevel = "displayRooms";
		else if (choice.compareTo("2")==0) mLevel = "displaySecurity";
		else if (choice.compareTo("3")==0) mLevel = "displayAllLights";
		
		else if (choice.compareTo("99")==0) mLevel = "logout";
		else _doError("ERROR: Not a valid choice");
		
	}
	
	
	
	private void _doDisplayMenu() throws SHException {
		mLevel = "display";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		ms.addLineToBody("Display Menu");
		/*Collection<Light> lights = myHouse.getLights();
		lights.forEach(light  -> {
			ms.addLineToBody(light.getName()+" is "+light.getState());
		});*/
		ms.addLineToBody("99. Return to Main Menu");
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		_debug("Recv:", mr);
		
		mLevel = "main";
	}

	
	private void _doDisplayRooms() throws SHException {
		mLevel = "displayRooms";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		ms.addLineToBody("Display Menu");
		Collection<Room> rooms = myHouse.getRooms();
		HashMap<String, Room> mEntries = new HashMap<String, Room>();
		int i = 1;
		for(Room room : rooms) {
			ms.addLineToBody(Integer.toString(i)+". "+room.getName());
			mEntries.put(Integer.toString(i),room);
			i++;
		};
		ms.addLineToBody("99. Return to Main Menu");
		ms.addLineToBody("or Enter a room number to view lights");
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		String choice = mr.getParam("choice");
		if (mEntries.containsKey(choice)) {
			currentroom = (Room)mEntries.get(choice);
			mLevel = "displaySingleRoom";
			//((Room)mEntries.get(choice)).toggleState();
		} else if (choice.compareTo("99")==0) {
			mLevel = "main";
		} else {
			mLevel = "displayRooms";
		}
		_debug("Recv:", mr);
		
	}
	
	private void _doDisplaySingleRoom(Room r) throws SHException {
		mLevel = "displaySingleRoom";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		ms.addLineToBody("Current Lights in "+r.getName());
		Collection<Light> lights = r.getLights();
		HashMap<String, Light> mEntries = new HashMap<String, Light>();
		int i = 1;
		for(Light light : lights) {
			ms.addLineToBody(Integer.toString(i)+". "+light.toString());
			mEntries.put(Integer.toString(i),light);
			i++;
		};
		
		ms.addLineToBody("99. To go back");
		ms.addLineToBody("ADD lightname to add a light");
		ms.addLineToBody("or Enter a light number to edit a light");
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		String choice = mr.getParam("choice");
		String[] arrOfStr = choice.split(" ",2);
		String add = arrOfStr[0];
		if (mEntries.containsKey(choice)) {
			currentlight = (Light)mEntries.get(choice);
			mLevel = "displaySingleLight";
		} else if (choice.compareTo("99")==0) {
			mLevel = "displayRooms";
		} else if (add.compareTo("ADD")==0) {
			r.addLight(arrOfStr[1]);
			mLevel = "displaySingleRoom";
		}
		else {
			mLevel = "displaySingleRoom";
		}
		_debug("Recv:", mr);
	}
	
	private void _doDisplaySingleLight(Light l) throws SHException {
		mLevel = "displaySingleLight";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		ms.addLineToBody("Current Light");
		ms.addLineToBody("1. Name = "+l.getName());
		ms.addLineToBody("2. State = "+l.getState());
		ms.addLineToBody("3. Level = "+l.getLevel());
		ms.addLineToBody("4. Color = "+l.getColor());
		
		ms.addLineToBody("99. To go back");
		ms.addLineToBody("or Enter a attribute number to edit a light");
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		String choice = mr.getParam("choice");
		if (choice.compareTo("1")==0||choice.compareTo("3")==0||choice.compareTo("4")==0) {
			lighteditchoice = choice;
			mLevel = "editSingleLight";
		} else if (choice.compareTo("2")==0) {
			l.toggleState();
			mLevel = "displaySingleLight";
		}else if (choice.compareTo("99")==0) {
			mLevel = "displaySingleRoom";
		} else {
			mLevel = "displaySingleLight";
		}
		
		_debug("Recv:", mr);
	}
	
	private void _doEditSingleLight(Light l, String c) throws SHException{
		mLevel = "editSingleLight";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		ms.addLineToBody("Editing " + l.getName());
		if (c.equals("1"))
		{
			ms.addLineToBody("Enter new Name");
		} else if (c.equals("2"))
		{
			ms.addLineToBody("Enter new State");
		} else if (c.equals("3"))
		{
			ms.addLineToBody("Enter new Level");
		} else if (c.equals("4"))
		{
			ms.addLineToBody("Enter new Color");
		}
		
		
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		String choice = mr.getParam("choice");
		
		if (choice.compareTo("BACK")==0) {
			mLevel = "displaySingleLight";
		}
		
		if (c.equals("1")) {
			l.setName(choice);
			mLevel = "displaySingleLight";
		} else if (c.equals("2")) {
			l.toggleState();
			mLevel = "displaySingleLight";
		} else if (c.equals("3")) {
			l.setLevel(Integer.parseInt(choice));
			mLevel = "displaySingleLight";
		} else if (c.equals("4")) {
			l.setColor(choice);
			mLevel = "displaySingleLight";
		}
		
		
		mLevel = "displaySingleLight";
		
		
		_debug("Recv:", mr);
	}
	
	private void _doDisplaySecurity() throws SHException {
		mLevel = "displaySecurity";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		ms.addLineToBody("Display Menu");
		Collection<Security> devices = myHouse.getDevices();
		HashMap<String, Security> mEntries = new HashMap<String, Security>();
		int i = 1;
		for(Security device : devices) {
			ms.addLineToBody(Integer.toString(i)+". "+device.getName());
			mEntries.put(Integer.toString(i),device);
			i++;
		};
		ms.addLineToBody("99. Return to Main Menu");
		ms.addLineToBody("or Enter a device number and passcode to enter");
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		String choice = mr.getParam("choice");
		String[] arrOfStr = choice.split(" ",2);
		String option = arrOfStr[0];
		
		if (mEntries.containsKey(option)) {
			currentdevice = (Security)mEntries.get(option);
			mLevel = "displaySingleSecurity";
			/*if(arrOfStr[0].compareTo(currentdevice.getPin())==0) {
				mLevel = "displaySingleSecurity";
			}*/
		} else if (option.compareTo("99")==0) {
			mLevel = "main";
		} else if (option.compareTo("ADD")==0) {
			myHouse.addDevice(arrOfStr[1]);
			mLevel = "displaySecurity";
		} else {
			mLevel = "displaySecurity";
		}
		_debug("Recv:", mr);
		
	}
	
	private void _doDisplaySingleSecurity(Security s) throws SHException {
		mLevel = "displaySingleSecurity";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		ms.addLineToBody("Current Device");
		ms.addLineToBody("1. Name = "+s.getName());
		ms.addLineToBody("2. State = "+s.getState());
		ms.addLineToBody("3. Edit Pin");
		
		ms.addLineToBody("99. To go back");
		ms.addLineToBody("or Enter a attribute number and PIN to edit attribute");
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		String choice = mr.getParam("choice");
		String[] arrOfStr = choice.split(" ",2);
		String option = arrOfStr[0];
		
		if ((option.compareTo("1")==0||option.compareTo("3")==0)&&arrOfStr[1].compareTo(s.getPin())==0) {
			securityeditchoice = option;
			mLevel = "editSecurity";
		} else if (option.compareTo("2")==0 && arrOfStr[1].compareTo(s.getPin())==0) {
			s.toggleState();;
			mLevel = "displaySingleSecurity";
		} 
		else if (option.compareTo("99")==0) {
			mLevel = "displaySecurity";
		} else {
			mLevel = "displaySingleSecurity";
		}
		
		_debug("Recv:", mr);
	}
	
	private void _doEditSecurity(Security s, String c) throws SHException{
		mLevel = "editSecurity";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		
		ms.addLineToBody("Editing " + s.getName());
		if (c.equals("1"))
		{
			ms.addLineToBody("Enter new Name");
		} else if (c.equals("3"))
		{
			ms.addLineToBody("Enter new Pin");
		}
		
		
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		String choice = mr.getParam("choice");
		
		if (c.equals("1")) {
			s.setName(choice);
		} else if (c.equals("3")) {
			s.setPin(choice);
		} 
	
		mLevel = "displaySingleSecurity";
		
		_debug("Recv:", mr);
	}
	
	private void _doDisplayAllLights() throws SHException {
		mLevel = "displayAllLights";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		ms.addLineToBody("Display Menu");
		Collection<Room> rooms = myHouse.getRooms();
		HashMap<String, Room> mEntries = new HashMap<String, Room>();
		int i = 1;
		for(Room room : rooms) {
			ms.addLineToBody(room.getName());
			Collection<Light> lights = room.getLights();
			HashMap<String, Light> xEntries = new HashMap<String, Light>();
			for(Light light : lights) {
				ms.addLineToBody(Integer.toString(i)+". "+light.toString());
				xEntries.put(Integer.toString(i),light);
				i++;
			};
		};
		ms.addLineToBody("99. Return to Main Menu");
		ms.addLineToBody("Type ALL (state, level, color) to edit all lights");
		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		String choice = mr.getParam("choice");
		String[] arrOfStr = choice.split(" ",2);
		String option = arrOfStr[0];
		
		if (option.compareTo("ALL")==0&&(arrOfStr[1].compareTo("state")==0||arrOfStr[1].compareTo("level")==0||arrOfStr[1].compareTo("color")==0)) {
			lighteditchoice = arrOfStr[1];
			mLevel = "editAllLights";
			//((Room)mEntries.get(choice)).toggleState();
		} else if (option.compareTo("99")==0) {
			mLevel = "main";
		} else {
			mLevel = "displayAllLights";
		}
		_debug("Recv:", mr);
		
	}
	
	private void _doEditAllLights(String c) throws SHException{
		mLevel = "editAllLights";
		Message ms = new Message();
		ms.setCmd(MCmds.MENU);
		ms.addParam("menu",mLevel);
		ms.addParam("pnum","1");
		ms.addParam("1","choice");
		if (c.equals("state"))
		{
			ms.addLineToBody("Enter new State (on/off)");
		} 
		else if (c.equals("level"))
		{
			ms.addLineToBody("Enter new level");
		} 
		else if (c.equals("color"))
		{
			ms.addLineToBody("Enter new color");
		}

		proto.putMessage(ms);
		_debug("Send:", ms);
		
		Message mr = proto.getMessage();
		String choice = mr.getParam("choice");
		
		
		
		Collection<Room> rooms = myHouse.getRooms();
		for(Room room : rooms) {
			Collection<Light> lights = room.getLights();
			for(Light light : lights) {
				light.toggleState();
				if (c.equals("state"))
				{
					if (choice.equals("on")) {
						light.setState(true);
					}
					else if (choice.equals("off")) {
						light.setState(false);
					}
				} else if (c.equals("level")) {
					light.setLevel(Integer.parseInt(choice));
				} else if (c.equals("color")) {
					light.setColor(choice);
				}
			};
		};
		
		mLevel = "displayAllLights";
		
		
		_debug("Recv:", mr);
	}
	
	// to be called if any error occurs
	public void shutdown() {
		login = false;
		try {
			proto.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
