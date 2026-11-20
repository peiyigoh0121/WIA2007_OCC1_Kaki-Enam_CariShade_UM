package com.um.carishade.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShadeSegment {

    public enum ShadeType {
        INDOOR_AC,        // Air-conditioned indoor corridors / buildings
        COVERED_WALKWAY,  // Roofed / sheltered walkways
        TREE_CANOPY,      // Tree-shaded campus paths
        EXPOSED_SUN       // Open / unshaded sun-exposed paths
    }

    private final String segmentId;
    private final CampusNode startNode;
    private final CampusNode endNode;
    private final double distanceMeters;
    private final ShadeType shadeType;
    private final List<double[]> polylineCoords; // Each item: new double[]{lat, lng}
    private final String instructionText;

    public ShadeSegment(String segmentId,
                        CampusNode startNode,
                        CampusNode endNode,
                        double distanceMeters,
                        ShadeType shadeType,
                        List<double[]> polylineCoords,
                        String instructionText) {
        this.segmentId = segmentId;
        this.startNode = startNode;
        this.endNode = endNode;
        this.distanceMeters = distanceMeters;
        this.shadeType = shadeType;
        this.polylineCoords = polylineCoords != null
                ? new ArrayList<>(polylineCoords)
                : new ArrayList<>();
        this.instructionText = instructionText;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public CampusNode getStartNode() {
        return startNode;
    }

    public CampusNode getEndNode() {
        return endNode;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public ShadeType getShadeType() {
        return shadeType;
    }

    public boolean isShaded() {
        return shadeType == ShadeType.INDOOR_AC
                || shadeType == ShadeType.COVERED_WALKWAY
                || shadeType == ShadeType.TREE_CANOPY;
    }

    public List<double[]> getPolylineCoords() {
        return Collections.unmodifiableList(polylineCoords);
    }

    public String getInstructionText() {
        return instructionText;
    }
}