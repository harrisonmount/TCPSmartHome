package sh.app;

import java.util.Collection;
import java.util.HashMap;

public class Home {

	private String username;
	private String password;
	
	private HashMap<String,Security> devices;
	private HashMap<String,Room> rooms;
	//private HashMap<String,Light> lights;
	
	//constructor
	public Home(String u, String p) {
		username = u;
		password = p;
		devices = new HashMap<String, Security>();
		rooms = new HashMap<String, Room>();
		//lights = new HashMap<String, Light>();
		
	}
	
	public boolean checkLogin(String u, String p) {
		boolean success = false;
		if(username.compareTo(u)==0 && password.compareTo(p)==0) success=true;
		return success;
	}
	

	public void addRoom(String name) {
		Room r = new Room(name);
		rooms.put(name, r);
	}
	
	/*public void deleteRoom(String name) {
		rooms.remove(name);
	}*/
	
	public void addDevice(String name) {
		Security d = new Security(name);
		devices.put(name, d);
	}
	
	/*public void deleteDevice(String name) {
		devices.remove(name);
	}*/
	
	public void setLightState(String name, boolean s) {
		Security d = new Security(name);
		if(d!=null) {
			d.setState(s);
		}
	}
	
	
	public Collection<Security>getDevices(){
		return devices.values();
	}
	public Collection<Room> getRooms(){
		return rooms.values();
	}
	
	@Override
	public String toString() {
		return " " + rooms.values() + devices.values();
	}
	
	
}
