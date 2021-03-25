package sh.app;

import java.util.Collection;
import java.util.HashMap;

public class Room {
	
		private String name;
		private HashMap<String, Light> lights;
		
		//constructor
		public Room(String n) {
			name = n;
			lights = new HashMap<String, Light>();
		}
		
		public String getName() {
			return name;
		}
		
		public void addLight(String name) {
			Light l = new Light(name);
			lights.put(name, l);
		}
		
		public void setLightState(String name, boolean s) {
			Light l = lights.get(name);
			if(l!=null) {
				l.setState(s);
			}
		}
		
		public void setLightColor(String name, String color) {
			Light l = lights.get(name);
			if(l!=null) {
				l.setColor(color);
			}
		}
		
		public void setLightLevel(String name, int level) {
			Light l = lights.get(name);
			if(l!=null) {
				l.setLevel(level);
			}
		}
		
		public void deleteLight(String name) {
			lights.remove(name);
		}
		
		public Collection<Light> getLights(){
			return lights.values();
		}
		
		@Override
		public String toString() {
			return "Room: "+ name + " " + lights.values();
		}
}
