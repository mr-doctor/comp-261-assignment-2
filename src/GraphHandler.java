import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.Queue;

public class GraphHandler {
	
	private Map<Integer, Node> nodes;
	private Set<Node> articulationPoints = new HashSet<>();
	
	public GraphHandler(Map<Integer, Node> m) {
		this.nodes = m;
	}
	
	public List<Node> findPath(Node start, Node goal) {
		
		Queue<HandlerNode> frontier = new PriorityQueue<>();
		Set<HandlerNode> closed = new HashSet<>();

		HandlerNode firstNode = new HandlerNode(null, start, 0, calculateHeuristic(start, goal));
		frontier.add(firstNode);
		
		while (!frontier.isEmpty()) {
			HandlerNode current = frontier.poll();
			
			if (current.getNode().equals(goal)) {
				return retrace(current);
			}
		
			for (HandlerNode neighbour : current.getNeighbours()) {
				if (closed.contains(neighbour)) {
					continue;
				}
				int cID = current.getNode().getID();
				int nID = neighbour.getNode().getID();
				Segment seg = current.getSegments().stream()
						.filter(s -> cID == s.getNode1ID() && nID == s.getNode2ID() || cID == s.getNode2ID() && nID == s.getNode1ID())
						.findAny()
						.get();
				
				double actualCost = current.getActualCost() + seg.getLength();
				
				if (!frontier.contains(neighbour)) {
					frontier.add(neighbour);
				} else if (actualCost >= neighbour.getActualCost()) {
					continue;
				}
				neighbour.setParent(current);
				neighbour.setActualCost(actualCost);
				neighbour.setHeuristicCost(calculateHeuristic(neighbour.getNode(), goal));
				neighbour.setCostFromStart(neighbour.getActualCost() + neighbour.getHeuristicCost());
			}
			closed.add(current);
		}
		return null;
	}
	
	public double calculateHeuristic(Node a, Node b) {
		return a.getLocation().distance(b.getLocation());
	}
	
	public List<Node> retrace(HandlerNode goal) {
		List<Node> path = new ArrayList<>();
		while (goal != null) {
			path.add(goal.getNode());
			goal = goal.getParent();
		}
		return path;
	}
	
	public Set<Node> initialiseArticulation() {
		for (Entry<Integer, Node> entry : nodes.entrySet()) {
			entry.getValue().count = Integer.MAX_VALUE;
		}
		Node start = nodes.values().iterator().next();
		start.count = 0;
		int numSubtrees = 0;
		
		for (Node neighbour : start.getNeighbours()) {
			if (neighbour.count == Integer.MAX_VALUE) {
				iterArticulationPoints(neighbour, start);
				numSubtrees++;
			}
		}
		if (numSubtrees > 1) {
			articulationPoints.add(start);
		}
		
		return articulationPoints;
	}
	
	public void iterArticulationPoints(Node firstNode, Node root) {
		Stack<ArticulationNode> stack = new Stack<>();
		stack.push(new ArticulationNode(firstNode, 1, root));
		while (!stack.isEmpty()) {
			ArticulationNode aNode = stack.peek();
			if (aNode.getNode().count == Integer.MAX_VALUE) {
				aNode.getNode().count = aNode.count;
				aNode.getNode().reachBack = aNode.count;
				aNode.getNode().children = new ArrayDeque<>();
				for (Node neighbour : aNode.getNode().getNeighbours()) {
					if (!neighbour.equals(aNode.getParent())) {
						aNode.getNode().children.add(neighbour);
					}
				}
			} else if (!aNode.getNode().children.isEmpty()) {
				Node child = aNode.getNode().children.poll();
				if (child.count < Integer.MAX_VALUE) {
					aNode.getNode().reachBack = Math.min(aNode.getNode().reachBack, child.count);
				} else {
					stack.push(new ArticulationNode(child, aNode.getNode().count+1, aNode.getNode()));
				}
			} else {
				if (!aNode.getNode().equals(firstNode)) {
					if (aNode.getNode().reachBack >= aNode.getParent().count) {
						System.out.println("adding...");
						articulationPoints.add(aNode.getParent());
						aNode.getParent().reachBack = Math.min(aNode.getParent().reachBack, aNode.getNode().reachBack);
					}
				}
				stack.pop();
			}
		}
	}
	
}
