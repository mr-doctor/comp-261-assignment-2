import java.util.ArrayList;
import java.util.List;

public class Road {
	
	// all information
	private int ID;
	private String name;
	private String city;
	private boolean oneWay;
	private int speedLimit;
	private List<Segment> segments = new ArrayList<Segment>();
	private boolean selected = false;
	
	public Road(int ID, String name, String city, boolean oneWay, int speedLimit) {
		this.ID = ID;
		this.name = name;
		this.city = city;
		this.oneWay = oneWay;
		this.speedLimit = speedLimit;
	}
	
	// getters and setters 
	
	public void addSegment(Segment s) {
		segments.add(s);
	}
	
	public List<Segment> getSegments() {
		return this.segments;
	}
	
	public int getID() {
		return this.ID;
	}

	public String getName() {
		return this.name;
	}
	
	public String getCity() {
		return this.city;
	}
	
	public boolean getOneWay() {
		return this.oneWay;
	}
	
	public int getSpeedLimit() {
		return this.speedLimit;
	}
	
	public boolean isSelected() {
		return this.selected;
	}
	
	public void setSelected(boolean b) {
		this.selected = b;
		for (Segment s : this.segments) {
			s.setSelected(b);
		}
		this.selected = false;
	}
}
