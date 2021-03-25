package sh.app;

public class Security {
		
		private String name;
		private boolean state;
		private String pin;
		
		//Constructor
		public Security(String n) {
			name = n;
			state = false;
			pin = "0000";
		}
		
		public String getName() {
			return name;
		}
		
		public String getPin() {
			return pin;
		}
		
		public String getState() {
			if (state) return "ARMED";
			else return "DISARMED";
		}
		
		public void toggleState() {
			state = !state;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public void setPin(String pin) {
			this.pin = pin;
		}
		
		public void setState(boolean state) {
			this.state = state;
		}
		
		@Override
		public String toString() {
			return "Device [name = " + name + ", state = " + state + "]";
		}
}
