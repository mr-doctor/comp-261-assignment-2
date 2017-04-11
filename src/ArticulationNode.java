public class ArticulationNode {
	private Node n;
	public int count = 0;
	private Node parent;
	
	public ArticulationNode(Node n, int c, Node p) {
		this.n = n;
		this.count = c;
		this.parent = p;
	}
	
	public Node getNode() {
		return this.n;
	}
	
	public Node getParent() {
		return this.parent;
	}
}
