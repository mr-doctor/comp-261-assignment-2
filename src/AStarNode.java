import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AStarNode implements Comparable<AStarNode> {
	
	private Node n;
	private double costFromStart;
	private double heuristicCost;
	private double actualCost;
	private AStarNode parent;
	
	public AStarNode(AStarNode p, Node n, double c, double h) {
		this.parent = p;
		this.n = n;
		this.costFromStart = c;
		this.heuristicCost = h;
		this.actualCost = this.heuristicCost + this.costFromStart;
	}
	
	public void setCostFromStart(double d) {
		this.costFromStart = d;
	}
	
	public void setHeuristicCost(double d) {
		this.heuristicCost = d;
	}

	public void setActualCost(double d) {
		this.actualCost = d;
	}
	
	public double getHeuristicCost() {
		return this.heuristicCost;
	}
	
	public double getCostFromStart() {
		return this.costFromStart;
	}

	public double getActualCost() {
		return this.actualCost;
	}
	
	public AStarNode getParent() {
		return this.parent;
	}
	
	public Node getNode() {
		return this.n;
	}
	
	public Set<Segment> getSegments() {
		return n.getSegments();
	}
	
	public List<AStarNode> getNeighbours() {
		return n.getSegments().stream()
				.map(seg -> seg.findOtherEnd(n))
				.map(node -> new AStarNode(this, node, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY))
				.collect(Collectors.toList());
	}

	public void setParent(AStarNode current) {
		this.parent = current;
		
	}

	public int compareTo(AStarNode o) {
		if (this.actualCost < o.getActualCost()) {
			return -1;
		} else if (this.actualCost > o.getActualCost()) {
			return 1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((n == null) ? 0 : n.hashCode());
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
		AStarNode other = (AStarNode) obj;
		if (n == null) {
			if (other.n != null)
				return false;
		} else if (!n.equals(other.n))
			return false;
		return true;
	}
	
}
