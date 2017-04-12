import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Segment {
	
	private Road parentRoad;
	private double length;
	private Node node1;
	private Node node2;
	private List<Location> locations = new ArrayList<>();
	private boolean selected = false;
	private boolean onPath = false;
	
	public Segment(Road p, double l, Node node1, Node node2, ArrayList<Location> locs) {
		this.parentRoad = p;
		this.length = l;
		this.node1 = node1;
		this.node2 = node2;
		this.locations = locs;
		node1.getSegments().add(this);
		node2.getSegments().add(this);
		
	}
	
	// getters and setters
	
	public int getParentRoadID(){
		return this.getParentRoad().getID();
	}
	
	public double getLength() {
		return this.length;
	}
	
	public int getNode1ID() {
		return this.node1.getID();
	}
	
	public int getNode2ID() {
		return this.node2.getID();
	}
	
	public List<Location> getLocations() {
		return this.locations;
	}
	
	public boolean isSelected() {
		return this.selected;
	}
	
	public void setSelected(boolean b) {
		this.selected = b;
	}
	
	public void setOnPath(boolean b) {
		this.onPath = b;
	}
	
	public Node findOtherEnd(Node n) {
		if (n.equals(this.node1)) {
			return this.node2;
		} else {
			return this.node1;
		}
	}
	
	public Point getPoint(Location location, double scale) {
		return location.asPoint(new Location(0, 0), scale);
	}
	
	public void draw(Graphics g, double scale) {
		// draw the node as red if selected, grey if otherwise
		if (!this.selected) {
			g.setColor(Color.GRAY);
		} else {
			g.setColor(Color.RED);
			// makes sure that the default selection state is false
			this.selected = false;
		}
		if (this.parentRoad.isOneWay()) {
			g.setColor(new Color(255, 0, 255));
		}
		if (this.onPath) {
			g.setColor(Color.RED.darker());
		}
		
		// for every two locations, draw a line between them at the scaled points
		for (int i=0; i<locations.size()-1; i++) {
			Location l1 = locations.get(i);
			Location l2 = locations.get(i+1);
			Point p1 = getPoint(l1, scale);
			Point p2 = getPoint(l2, scale);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}

	public Road getParentRoad() {
		return this.parentRoad;
	}
}
