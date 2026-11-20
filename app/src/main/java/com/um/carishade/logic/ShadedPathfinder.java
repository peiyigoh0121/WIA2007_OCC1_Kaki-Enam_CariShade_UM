package com.um.carishade.logic;

import com.um.carishade.model.CampusNode;
import com.um.carishade.model.ShadeSegment;
import com.um.carishade.model.ShadedRoute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class ShadedPathfinder {

    private final RouteScoreCalculator scoreCalculator;

    public ShadedPathfinder(RouteScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }

    public ShadedRoute findOptimalShadedRoute(CampusNode start,
                                              CampusNode goal,
                                              List<ShadeSegment> allSegments,
                                              boolean highUvMode) {
        if (start == null || goal == null) {
            return emptyRoute(start, goal);
        }
        if (start.equals(goal)) {
            return new ShadedRoute(
                    start, goal, Collections.emptyList(),
                    0.0, 0.0, 100.0, 0,
                    Collections.singletonList("You are already at " + goal.getName() + ".")
            );
        }

        // Build adjacency list from segments
        Map<String, List<ShadeSegment>> adjacency = new HashMap<>();
        Map<String, CampusNode> nodesById = new HashMap<>();
        nodesById.put(start.getNodeId(), start);
        nodesById.put(goal.getNodeId(), goal);

        for (ShadeSegment seg : allSegments) {
            CampusNode u = seg.getStartNode();
            CampusNode v = seg.getEndNode();
            nodesById.put(u.getNodeId(), u);
            nodesById.put(v.getNodeId(), v);
            adjacency.computeIfAbsent(u.getNodeId(), k -> new ArrayList<>()).add(seg);
        }

        Map<String, Double> distCost = new HashMap<>();
        Map<String, ShadeSegment> cameFromEdge = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (String nodeId : nodesById.keySet()) {
            distCost.put(nodeId, Double.POSITIVE_INFINITY);
        }
        distCost.put(start.getNodeId(), 0.0);

        PriorityQueue<NodeCost> pq = new PriorityQueue<>(Comparator.comparingDouble(nc -> nc.cost));
        pq.add(new NodeCost(start.getNodeId(), 0.0));

        while (!pq.isEmpty()) {
            NodeCost current = pq.poll();
            String currentId = current.nodeId;

            if (visited.contains(currentId)) continue;
            visited.add(currentId);

            if (currentId.equals(goal.getNodeId())) break;

            List<ShadeSegment> outgoing = adjacency.getOrDefault(currentId, Collections.emptyList());
            for (ShadeSegment edge : outgoing) {
                String nextId = edge.getEndNode().getNodeId();
                if (visited.contains(nextId)) continue;

                double edgeCost = scoreCalculator.calculateSegmentCost(edge, highUvMode);
                double newCost = distCost.get(currentId) + edgeCost;

                if (newCost < distCost.get(nextId)) {
                    distCost.put(nextId, newCost);
                    cameFromEdge.put(nextId, edge);
                    pq.add(new NodeCost(nextId, newCost));
                }
            }
        }

        // Reconstruct path
        List<ShadeSegment> pathSegments = new ArrayList<>();
        String traceId = goal.getNodeId();
        while (cameFromEdge.containsKey(traceId)) {
            ShadeSegment edge = cameFromEdge.get(traceId);
            pathSegments.add(edge);
            traceId = edge.getStartNode().getNodeId();
        }
        Collections.reverse(pathSegments);

        if (pathSegments.isEmpty()) {
            return emptyRoute(start, goal);
        }

        List<String> instructions = new ArrayList<>();
        for (int i = 0; i < pathSegments.size(); i++) {
            ShadeSegment seg = pathSegments.get(i);
            instructions.add((i + 1) + ". " + seg.getInstructionText()
                    + " (" + Math.round(seg.getDistanceMeters()) + "m • "
                    + formatShadeLabel(seg.getShadeType()) + ")");
        }
        instructions.add("Arrive at " + goal.getName() + ".");

        double totalDist = scoreCalculator.calculateTotalDistance(pathSegments);
        double shadedDist = scoreCalculator.calculateShadedDistance(pathSegments);
        double shadePct = scoreCalculator.calculateShadePercentage(pathSegments);
        int mins = scoreCalculator.estimateWalkingMinutes(pathSegments);

        return new ShadedRoute(
                start, goal, pathSegments,
                totalDist, shadedDist, shadePct, mins, instructions
        );
    }

    private ShadedRoute emptyRoute(CampusNode start, CampusNode goal) {
        return new ShadedRoute(
                start, goal, Collections.emptyList(),
                0.0, 0.0, 0.0, 0,
                Collections.singletonList("No shaded route found between selected points.")
        );
    }

    private String formatShadeLabel(ShadeSegment.ShadeType type) {
        switch (type) {
            case INDOOR_AC:
                return "Indoor AC";
            case COVERED_WALKWAY:
                return "Covered Walkway";
            case TREE_CANOPY:
                return "Tree Canopy";
            case EXPOSED_SUN:
            default:
                return "Sun Exposed";
        }
    }

    private static class NodeCost {
        final String nodeId;
        final double cost;

        NodeCost(String nodeId, double cost) {
            this.nodeId = nodeId;
            this.cost = cost;
        }
    }
}