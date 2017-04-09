import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Queue;

public class GraphHandler {
	
	private Map<Integer, Node> nodes;
	
	public GraphHandler(Map<Integer, Node> nodes) {
		this.nodes = nodes;
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
	
	public void findArticulationPoints(Node start) {
		HandlerNode startHandler = new HandlerNode(null, start, 0, 0);
		for (HandlerNode neighbour : startHandler.getNeighbours()) {
			if (neighbour.count == Integer.MAX_VALUE) {
				addArticulationPoint(neighbour, 1, startHandler);
				startHandler.numSubtrees++;
			}
		}
	}
	
	public int addArticulationPoint(HandlerNode node, int count, HandlerNode fromNode) {
		node.count = count;
		int reachBack = count;
		for (HandlerNode neighbour : node.getNeighbours()) {
			if (neighbour.count < Integer.MAX_VALUE) {
				reachBack = Math.min(neighbour.count, reachBack);
			} else {
				int childReach = addArticulationPoint(neighbour, count+1, node);
				if (childReach >= count) {
					fromNode.articulationPoints().add(node);
					System.out.println(node);
				}
				reachBack = Math.min(childReach, reachBack);
			}
		}
		return reachBack;
	}
	
}
