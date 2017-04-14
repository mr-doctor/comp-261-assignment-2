import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AucklandRoads extends GUI {

	// hello there
	
	// stores the road names for searching
	private Trie roadNamesIndex = new Trie();
	// stores the road names for drawing
	private Map<String, Road> roadIndex = new HashMap<>();
	// stores the roads for accessing with a specific ID
	private Map<Integer, String> roadIDIndex = new HashMap<>();
	// stores the nodes for drawing
	private Map<Integer, Node> nodeIndex = new HashMap<>();
	// stores the nodes for searching from click
	//private QuadTree nodes = new QuadTree(Location.newFromLatLon(Location.CENTRE_LAT, Location.CENTRE_LON), 10000);
	
	// the amounts to translate the canvas by
	private double moveX;
	private double moveY;
	private double scale = 6;
	
	// a quick way to draw the selected node
	private Node selectedNode;
	private Node lastNode;
	
	private double oldScale = 1;
	
	private final GraphHandler graphHandler;
	private List<Node> path;
	private Set<Node> articulationPoints;
	private double pathLength = 0;
	private double time;
	private boolean showArticulation = false;
	
	public AucklandRoads() {
		graphHandler = new GraphHandler(nodeIndex);
	}
	
	// deals with all the files
	public void handleData(List<String> roadData, List<String> nodeData, List<String> segData, List<String> polyData) {
		
		// adds the road data to the road collections
		for (int i=1; i<roadData.size(); i++) {
			String[] splitData = roadData.get(i).split("\t");
			
			roadNamesIndex.insert(splitData[2]);
			roadIDIndex.put(Integer.parseInt(splitData[0]), splitData[2]);
			roadIndex.put(splitData[2], new Road(
				Integer.parseInt(splitData[0]), 		// ID
				splitData[2], 							// name
				splitData[3],							// city
				splitData[4].equals("1"),	 			// one way or not
				Integer.parseInt(splitData[5]),			// speed limit
				Integer.parseInt(splitData[6])));	 	// class
		}
		
		// adds the node data to the QuadTree and Map
		for (int i=0; i<nodeData.size(); i++) {
			String[] splitData = nodeData.get(i).split("\t");
			//debugging
/*			System.out.println(Location.newFromLatLon(
					Double.parseDouble(splitData[1]), 
					Double.parseDouble(splitData[2])));*/
			Node n = new Node(
					Integer.parseInt(splitData[0]),				// ID
					Location.newFromLatLon(
							Double.parseDouble(splitData[1]), 
							Double.parseDouble(splitData[2]))); // location
			
			//nodes.insert(n);
			nodeIndex.put(Integer.parseInt(splitData[0]), n);
			
		}
		
		// adds the segment data to the relevant collections
		for (int i=1; i<segData.size(); i++) {
			String[] splitData = segData.get(i).split("\t");
			
			String parentRoadName = roadIDIndex.get(Integer.parseInt(splitData[0]));
			Road parentRoad = roadIndex.get(parentRoadName);
			
			ArrayList<Location> locations = new ArrayList<Location>();
			for (int j=4; j<splitData.length; j+=2) {
				locations.add(Location.newFromLatLon(
						Double.parseDouble(splitData[j]), 		// latitude
						Double.parseDouble(splitData[j+1]))); 	// longitude
			}
			
			parentRoad.addSegment(new Segment(
					parentRoad,							// road
					Double.parseDouble(splitData[1]),	// length
					nodeIndex.get(Integer.parseInt(splitData[2])),		// node 1 ID
					nodeIndex.get(Integer.parseInt(splitData[3])),		// node 2 ID
					locations));						// locations
		}
		this.articulationPoints = graphHandler.initialiseArticulation();
	}
	
	// takes all the files and turns them into lists. Only handles the polygons data if it exists
	protected void onLoad(File nodes, File roads, File segments, File polygons) throws IOException {
		List<String> roadData = Files.readAllLines(roads.toPath());
		
		List<String> nodeData = Files.readAllLines(nodes.toPath());
		
		List<String> segData = Files.readAllLines(segments.toPath());
		
		List<String> polyData = new ArrayList<>();
		if (polygons != null) {
			polyData = Files.readAllLines(polygons.toPath());
		}
		
		handleData(roadData, nodeData, segData, polyData);
	}

	protected void redraw(Graphics g) {
		g.setColor(new Color(210, 200, 190));
		g.fillRect(0, 0, getDrawingAreaDimension().width, getDrawingAreaDimension().height);
		
		// translate the canvas to the centre of the map, then shifts it by the X and Y movements
		// the amount the camera moves is inversely proportional to the scale
		g.translate(
				getDrawingAreaDimension().width/2 + (int) (moveX), 
				getDrawingAreaDimension().height/2 + (int) (moveY));
		
		// for every road, draw all the segments
		for (Map.Entry<String, Road> entry : roadIndex.entrySet()) {
			Road road = entry.getValue();
			for (int i=0; i<road.getSegments().size(); i++) {
				road.getSegments().get(i).draw(g, scale);
			}
		}
		
		// draw every node. Iterates through a map, not a list, because this allows an iterative clicking search to be easily implemented
		for (Map.Entry<Integer, Node> entry : nodeIndex.entrySet()) {
			//debugging
			/*if (entry.getValue().getID() == 10) {
				this.selectedNode = entry.getValue();
				entry.getValue().setSelected(true);
			}*/
			if (this.articulationPoints.contains(entry.getValue())) {
				entry.getValue().setArticulation(true);
			}
			entry.getValue().draw(g, scale, showArticulation);
		}
		
		// draws the selected node, provided it exists at this stage
		if (this.selectedNode != null) {
			//debugging
/*			System.out.println(this.selectedNode.getLocation());
			System.out.println((int) (this.selectedNode.getNodePoint(scale).x - 7/2 + getDrawingAreaDimension().width/2 + (int) (moveX * 50 / (scale / 10))) + ", " 
					+ (int) (this.selectedNode.getNodePoint(scale).y - 7/2 + getDrawingAreaDimension().width/2 + (int) (moveX * 50 / (scale / 10))));*/
			
			//draws it as a green circle, instead of a blue square
			g.setColor(Color.GREEN);
			int size = 7;
			g.fillOval((int) (this.selectedNode.getNodePoint(scale).x - size/2), (int) (this.selectedNode.getNodePoint(scale).y - size/2), size, size);
		}
		
		if (this.lastNode != null) {
			g.setColor(Color.ORANGE);
			int size = 7;
			g.fillOval((int) (this.lastNode.getNodePoint(scale).x - size/2), (int) (this.lastNode.getNodePoint(scale).y - size/2), size, size);
		}
	}

	protected void onClick(MouseEvent e) {
		// calls the search, so that any highlighted road is not cleared
		onSearch();
		// creates a new location from the unscaled, unpanned mouse click point
		Location mouseL = canvasPointToLocation(e.getPoint());

		// iterative search
		if (this.selectedNode != null) {
			this.lastNode = this.selectedNode;
			this.selectedNode.setSelected(false);
		}

		// iterates through every node, starting with the first
		selectedNode = nodeIndex.values().iterator().next();
		double distance = Double.MAX_VALUE;
		for (Node entry : nodeIndex.values()) {
			// selects the closest node to the point
			if (entry.getLocation().distance(mouseL) < distance) {
				distance = entry.getLocation().distance(mouseL);

				this.selectedNode = entry;
			}
		}

		selectedNode.setSelected(true);
		printNodeInfo(this.selectedNode);
	}

	private Location canvasPointToLocation(Point p) {
		// reverses the translation of the panning
		AffineTransform transform = AffineTransform.getTranslateInstance(
				getDrawingAreaDimension().width/2 + moveX, 
				getDrawingAreaDimension().height/2 + moveY);
		Point2D.Double dest = new Point2D.Double();
		// reverses the scaling
		transform.scale(scale, -scale);
		try {
			// execute the inversion
			transform.inverseTransform(p, dest);
		} catch (NoninvertibleTransformException e1) {
			e1.printStackTrace();
		}
		return new Location(dest.getX(),dest.getY());
	}
	
	// prints the given node
	public void printNodeInfo(Node n) {
		String ID = Integer.toString(n.getID());
		// for storing all roads that come off it
		ArrayList<String> roads = new ArrayList<>();
		
/*		// for each road, check if any segments have the given node as an endpoint
		for (Map.Entry<String, Road> entry : roadIndex.entrySet()) {
			Road road = entry.getValue();
			for (Segment s : road.getSegments()) {
				// if they do have the node as an endpoint, add it to the list of roads
				if (s.getNode1ID() == n.getID() || s.getNode2ID() == n.getID()) {
					roads.add(entry.getKey());
					break;
				}
			}
		}*/
		
		for (Segment s : n.getSegments()) {
			if (!roads.contains(s.getParentRoad().getName())) {
				if (s.getParentRoad().getName().equals("-")) {
					roads.add("walkway");
				} else {
					roads.add(s.getParentRoad().getName());
				}
			}
		}
		
		// clear the text area
		getTextOutputArea().setText("");
		// print the node ID, and then print every connecting road
		getTextOutputArea().append(ID + "\n");
		for (String s : roads) {
			getTextOutputArea().append(s + "\n");
		}
	}

	protected void onSearch() {
		// clear the text area
		getTextOutputArea().setText("");
		// get the 
		String toSearch = getSearchBox().getText();
		// check if anything has been searched
		if(!getSearchBox().getText().equals("")) {
			// searches the Trie for the relevant road names that have the prefix
			ArrayList<String> roads = roadNamesIndex.search(toSearch);
			
			// check if there were any roads found
			if (!roads.isEmpty()) {
				// print and select all the roads
				for (String str : roads) {
					getTextOutputArea().append(str + "\n");
					roadIndex.get(str).setSelected(true);
				}
			}
		}
	}
	
	// handles movement
	protected void onMove(Move m) {
		int scaleChange = 0;
		
		// debugging
		//Location centre = canvasPointToLocation(new Point(getDrawingAreaDimension().width/2, getDrawingAreaDimension().height/2));
		
		if (m.equals(Move.ZOOM_IN)) {
			scaleChange = 5;
		} else if (m.equals(Move.ZOOM_OUT)) {
			scaleChange = -5;
		// alters the map movement values, added when the map draws
		} else if (m.equals(Move.NORTH)) {
			moveY+=100;
		} else if (m.equals(Move.SOUTH)) {
			moveY-=100;
		} else if (m.equals(Move.EAST)) {
			moveX-=100;
		} else if (m.equals(Move.WEST)){
			moveX+=100;
		}
		
		if (m.equals(Move.ZOOM_IN) || m.equals(Move.ZOOM_OUT)){
			
			// only allows the scale to change if is within the limits
			scale = clamp(6, scale + scaleChange, 150);
			
			if (scale != oldScale) {
				// shifts the map so that the scaling always occurs in the centre
				double factor = 1 + (scaleChange)/scale;
				moveX *= factor;
				moveY *= factor;
			}
			oldScale = scale;
		}
	}
	
	private double clamp(double i, double d, double j) {
		return Math.max(Math.min(d, j), i);
	}
	
	public Segment findSegment(Node n) {
		for (Map.Entry<String, Road> entry : roadIndex.entrySet()) {
			Road road = entry.getValue();
			for (int i=0; i<road.getSegments().size(); i++) {
				Segment s = road.getSegments().get(i);
				if (s.getNode1ID() == n.getID() || s.getNode2ID() == n.getID()) {
					return s;
				}
			}
		}
		return null;
	}
	
	protected void toggleArticulation() {
		this.showArticulation  = !this.showArticulation;
	}
	
	protected void pathfind(boolean shortestDistance) {
		if (this.path != null) {
			this.pathLength = 0;
			this.time = 0;
			for (Node n : this.path) {
				n.setOnPath(false);
				for (Iterator<Segment> it = n.getSegments().iterator(); it.hasNext();) {
					it.next().setOnPath(false);
				}
			}
		}
		
		if (this.selectedNode != null && this.lastNode != null) {
			this.path = graphHandler.findPath(this.lastNode, this.selectedNode, shortestDistance);
			if (this.path != null) {
				for (int i=0; i<this.path.size()-1; i++) {
					if (i != 0) {
						this.path.get(i).setOnPath(true);
					}
					for (Iterator<Segment> it = this.path.get(i).getSegments().iterator(); it.hasNext();) {
						Segment s = it.next();
						if (s.findOtherEnd(this.path.get(i)).equals(this.path.get(i+1))) {
							s.setOnPath(true);
							this.pathLength  += s.getLength();
							double speed = s.getParentRoad().getSpeedLimit();
							if (speed == 0) {
								speed = 5;
							} else {
								speed *= 20;
							}
							
							this.time += s.getLength() / speed;
						}
					}
				}
				printRoute();
			}
		}
	}
	
	private void printRoute() {
		String unit = "hours";
		int dp = 2;
		if (this.time < 1) {
			this.time *= 60;
			unit = "minutes";
			dp = 1;
		}
		getTextOutputArea().setText("");
		Map<String, Double> roadsAndLengths = new HashMap<>();
		
		for (Node n : this.path) {
			List<Segment> segs = setToList(n.getSegments());
			for (Segment s : segs) {
				String roadName = s.getParentRoad().getName();
				if (roadName.equals("-")) {
					roadName = "walkway";
				}
				if (roadsAndLengths.containsKey(roadName)) {
					roadsAndLengths.put(roadName, roadsAndLengths.get(roadName) + s.getLength());
				} else {
					roadsAndLengths.put(roadName, s.getLength());
				}
			}
		}
		for (Map.Entry<String, Double> entry : roadsAndLengths.entrySet()) {
			getTextOutputArea().append(entry.getKey() + ": " + round(entry.getValue(), 3) + "km\n");
		}
		
		getTextOutputArea().append("\nRoute length: " + round(this.pathLength, 3) + "km\n");
		getTextOutputArea().append("The route will take " + round(this.time, dp) + " " + unit);
	}

	public List<Segment> setToList(Set<Segment> set) {
		return new ArrayList<Segment>(set);
	}
	
	public Point getPoint(Location location, double scale) {
		return location.asPoint(new Location(0, 0), scale);
	}
	
	// helper function taken from stackoverflow: http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static void main(String[] arguments) {
		new AucklandRoads();
	}
}
