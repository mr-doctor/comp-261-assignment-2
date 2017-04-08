import java.awt.Point;
import java.util.Arrays;

public class QuadTree {
	
	// stores the number of children and the data
	private QuadTree[] children = new QuadTree[4];
	private Node n;
	private Location centre;
	private double size;

	// constructer that uses only size and centre, for making the root node
	public QuadTree(Location centre, double size) {
		this.centre = centre;
		this.size = size;
	}
	
	// contstructor for any child node
	public QuadTree(Location centre, double size, Node n) {
		this.centre = centre;
		this.size = size;
		this.n = n;
	}

	public void insert(Node newNode) {
		//System.out.println(newNode);
		if (this.isLeaf()) {
			// the child quadtree that this.node will be placed in
			int existingIndex = findQuadrantIndex(this.n.getLocation());
			//System.out.println("existing index = " + existingIndex);
			// splits this tree and transfers the node to a child
			this.children[existingIndex] = new QuadTree(generateCentres()[existingIndex], this.size / 2, this.n);
			this.n = null;

		}
		
		// gets the quadrant index of the location of the node to be inserted
		int insertIndex = findQuadrantIndex(newNode.getLocation());
		//System.out.println("insert index = " + insertIndex);
		
		// if the quadrant is taken, split it, otherwise set it
		if (this.children[insertIndex] != null) {
			this.children[insertIndex].insert(newNode);
		} else {
			this.children[insertIndex] = new QuadTree(generateCentres()[insertIndex], this.size / 2, newNode);
		}		
	}
	
	// determine the centres of the four child quadtrants
	private Location[] generateCentres() {
		return new Location[]{
				new Location(this.centre.x - this.size/4, this.centre.y + this.size/4),
				new Location(this.centre.x + this.size/4, this.centre.y + this.size/4),
				new Location(this.centre.x - this.size/4, this.centre.y - this.size/4),
				new Location(this.centre.x + this.size/4, this.centre.y - this.size/4)
		};
	}
	
	// determine what quadrant a location lies in
/*	follows the following pattern:
		[0][1]
		[2][3]  */
	private int findQuadrantIndex(Location location) {
		Location[] centres = generateCentres();
		int best = 0;
		// finds the closest centre to the index
		for (int i=1; i<centres.length; i++) {
			double bestValue = centres[best].distance(location);
			if (centres[i].distance(location) < bestValue){
				best = i;
			}
		}
		return best;
	}
	
	// getters and setters
	public boolean isLeaf() {
		return this.n != null;
	}

	public String toString() {
		if (this.isLeaf()) {
			return "{" + this.centre + "*: Node(" + this.n + ")}";
		} else {
			return "{" + this.centre + ": " + Arrays.toString(this.children) + "}";
		}
	}

	// finds the closest node to a location
	public Node getClosest(Location l, Node best) {
		Node newBest = best;
		
		// if best is the closest point, return it
		if (best != null && this.centre.distance(l) > best.getLocation().distance(l)) {
			return best;
		} else if (this.isLeaf()) {
			// otherwise, the best to be this node
			if (best == null || this.n.getLocation().distance(l) < best.getLocation().distance(l)){
				newBest = this.n;
			}
		} else {
			// find the best index, and recurse until the best node is found
			int nextIdx = findQuadrantIndex(l);
			if (this.children[nextIdx] != null) {
				newBest = this.children[nextIdx].getClosest(l, newBest);
			}
		}

		return newBest;
	}
	
	
	/*	// main for testing
		public static void main(String[] args) {
			QuadTree root = new QuadTree(new Location(0, 0), 100);
			
			root.insert(new Node(0, new Location(49, 49)));
			root.insert(new Node(1, new Location(24, 49)));
			
			System.out.println(root);
			
			System.out.println(root.getClosest(new Location(20, 40)));
		}*/

}
