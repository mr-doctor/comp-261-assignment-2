import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class Node {
	
	// node's information
	private int ID;
	private Location location;
	private boolean selected = false;
	private Set<Segment> segments = new HashSet<>();
	private boolean onPath = false;
	
	public Node(int ID, Location loc) {
		this.ID = ID;
		this.location = loc;
	}
	
	// getters and setters
	public int getID() {
		return this.ID;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public void setSelected(boolean b) {
		this.selected = b;
	}
	
	public boolean isSelected() {
		return this.selected;
	}
	
	public void setOnPath(boolean b) {
		this.onPath  = b;
	}
	
	public String toString() {
		return this.ID + ": " + this.location;
	}
	
	// returns a point to draw at, given the scale
	public Point getNodePoint(double scale) {
		return this.location.asPoint(new Location(0, 0), scale);
	}
	
	public void draw(Graphics g, double scale) {
		// changes the size of the node based on the scale
		int size = Math.min((int) (0.05 * (scale)), 3);
		g.setColor(Color.BLUE);
		
		if (this.onPath) {
			g.setColor(Color.RED);
		}
		// draws it at the scaled point
		g.fillRect((int) (this.getNodePoint(scale).x - size/2), (int) (this.getNodePoint(scale).y - size/2), size, size);
		
		// debugging
		//System.out.println(this.ID + " {Location: " + this.getLocation() + "\n Point: " +this.getNodePoint(scale) + "}\n");
	}
	public Set<Segment> getSegments() {
		return segments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass()) 
			return false;
		Node other = (Node) obj;
		if (ID != other.ID)
			return false;
		return true;
	}
	
}
