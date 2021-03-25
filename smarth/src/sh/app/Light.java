package sh.app;

public class Light {

		private String name;
		private boolean state;
		private int level;
		private String color;
		
		//Constructor
		public Light(String n) {
			
			name = n;
			state = true;
			level = 100;
			color = "White";
		}
		
		//Getters
		public String getName() {
			return name;
		}
		
		public String getState() {
			if (state) return "ON";
			else return "OFF";
		}
		
		public int getLevel() {
			return level;
		}
		
		public String getColor() {
			return color;
		}
		
		//Setters
		public void setName(String name) {
			this.name = name;
		}
		
		public void setState(boolean state) {
			this.state = state;
		}
		
		public void setLevel(int level) {
			this.level = level;
		}
		
		public void setColor(String color) {
			this.color = color;
		}
		
		public void toggleState() {
			state = !state;
		}
		
		@Override
		public String toString() {
			return "Name = " + name + ", State = " + this.getState() + ", Level = " + level +", Color = "+ color +"]";
		}
		
		public String toStringSingle() {
			return "Name = "+name + "\nState = " + state + "\nLevel = " + level +"\nColor = "+ color;
		}
		
		
}
