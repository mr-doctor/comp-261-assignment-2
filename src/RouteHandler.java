import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Queue;

public class RouteHandler {
	
	public List<Node> findPath(Node start, Node goal) {
		
		Queue<AStarNode> frontier = new PriorityQueue<>();
		Set<AStarNode> closed = new HashSet<>();

		AStarNode firstNode = new AStarNode(null, start, 0, calculateHeuristic(start, goal));
		frontier.add(firstNode);
		
		while (!frontier.isEmpty()) {
			AStarNode current = frontier.poll();
			
			if (current.getNode().equals(goal)) {
				return retrace(current);
			}
		
			for (AStarNode neighbour : current.getNeighbours()) {
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
	
	public List<Node> retrace(AStarNode goal) {
		List<Node> path = new ArrayList<>();
		while (goal != null) {
			path.add(goal.getNode());
			goal = goal.getParent();
		}
		return path;
	}
	
	public List<Node> findArticulationPoints(Node n) {
		
		return null;
	}
	
}
