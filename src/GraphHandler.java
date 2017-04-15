import java.util.ArrayDeque;
import java.util.ArrayList;
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
	
	public List<Node> findPath(Node start, Node goal, boolean shortestDistance, boolean considerClass) {
		
		// initialise collections
		Queue<HandlerNode> frontier = new PriorityQueue<>();
		Set<HandlerNode> closed = new HashSet<>();

		// create the first node at the start
		HandlerNode firstNode = new HandlerNode(null, start, 0, calculateHeuristic(start, goal));
		frontier.add(firstNode);
		
		while (!frontier.isEmpty()) {
			HandlerNode current = frontier.poll();
			
			// if we've reached the goal, call retrace from this node
			if (current.getNode().equals(goal)) {
				return retrace(current);
			}
			
			// get neighbours of this node
			for (HandlerNode neighbour : current.getNeighbours()) {
				// if we've been here before, skip this neighbour
				if (closed.contains(neighbour)) {
					continue;
				}
				
				int cID = current.getNode().getID();
				int nID = neighbour.getNode().getID();
				
				// find a valid segment connecting two nodes
				Segment seg = null;
				for (Segment s : current.getSegments()) {
					// if the road is one-way, then we must go node1->node2 when we go current->neighbour
					if (s.getParentRoad().isOneWay()) {
						if (cID == s.getNode1ID() && nID == s.getNode2ID()) {
							seg = s;
						}
					// otherwise, the order is irrelevant
					} else if (cID == s.getNode1ID() && nID == s.getNode2ID() || cID == s.getNode2ID() && nID == s.getNode1ID()) {
						seg = s;
					}
				}
				// if there's no segment to connect these two nodes, skip this neighbour
				if (seg == null) {
					continue;
				}
				// only use the class if it has been enabled
				int roadClass = seg.getParentRoad().getRoadClass();
				if (!considerClass) {
					roadClass = 0;
				}
				
				// only use the speed if that is the sort of search we want to do
				double speed = speed(seg.getParentRoad().getSpeedLimit() + roadClass);
				if (shortestDistance) {
					speed = 1;
				}
				
				// calculate cost as either the time it takes to reach the neighbour, or the distance
				double actualCost = current.getActualCost() + (seg.getLength() / speed);
				
				// if the neighbour isn't in the path already, add it
				if (!frontier.contains(neighbour)) {
					frontier.add(neighbour);
				// if it is, and going through it again is a worse option, skip it
				} else if (actualCost >= neighbour.getActualCost()) {
					continue;
				}
				
				// update the neighbour's values
				neighbour.setParent(current);
				neighbour.setActualCost(actualCost);
				neighbour.setHeuristicCost(calculateHeuristic(neighbour.getNode(), goal));
				neighbour.setCostFromStart(neighbour.getActualCost() + neighbour.getHeuristicCost());
			}
			// add the current node to those visited
			closed.add(current);
		}
		// we haven't found a path
		return null;
	}
	
	// converts the speed from a 0-7 integer to km/h
	private double speed(int speed) {
		if (speed == 0) {
			return 5;
		} else {
			return speed * 20;
		}
	}
	
	// uses the Euclidean distance between two nodes, using Locations since they do not scale
	public double calculateHeuristic(Node a, Node b) {
		return a.getLocation().distance(b.getLocation());
	}
	
	// retrace the path from the goal back to the start
	public List<Node> retrace(HandlerNode goal) {
		List<Node> path = new ArrayList<>();
		while (goal != null) {
			path.add(goal.getNode());
			goal = goal.getParent();
		}
		return path;
	}
	
	// set up the articulation points search
	public Set<Node> initialiseArticulation() {
		for (Entry<Integer, Node> entry : nodes.entrySet()) {
			entry.getValue().count = Integer.MAX_VALUE;
		}
		// get the first node
		Node start = nodes.values().iterator().next();
		start.count = 0;
		int numSubtrees = 0;
		
		// call articulation points on all relevant nodes
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
		// uses a wrapper for the values
		stack.push(new ArticulationNode(firstNode, 1, root));
		while (!stack.isEmpty()) {
			ArticulationNode aNode = stack.peek();
			// if we haven't considered this node before, update its values
			if (aNode.getNode().count == Integer.MAX_VALUE) {
				aNode.getNode().count = aNode.count;
				aNode.getNode().reachBack = aNode.count;
				aNode.getNode().children = new ArrayDeque<>();
				// add all neighbouring nodes to this node's children except for the parent
				for (Node neighbour : aNode.getNode().getNeighbours()) {
					if (!neighbour.equals(aNode.getParent())) {
						aNode.getNode().children.add(neighbour);
					}
				}
			} else if (!aNode.getNode().children.isEmpty()) {
				// get the first child
				Node child = aNode.getNode().children.poll();
				// if we've been to this child before update its reachback
				if (child.count < Integer.MAX_VALUE) {
					aNode.getNode().reachBack = Math.min(aNode.getNode().reachBack, child.count);
				// otherwise, add it to the stack
				} else {
					stack.push(new ArticulationNode(child, aNode.getNode().count+1, aNode.getNode()));
				}
			// if we have considered it, if its reachback is higher than its parent's count, then add its parent to the list of points and update the reachback
			} else {
				if (!aNode.getNode().equals(firstNode)) {
					if (aNode.getNode().reachBack >= aNode.getParent().count) {
						articulationPoints.add(aNode.getParent());
						aNode.getParent().reachBack = Math.min(aNode.getParent().reachBack, aNode.getNode().reachBack);
					}
				}
				stack.pop();
			}
		}
	}
	
}
