package com.um.carishade.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShadedRoute {
    private final CampusNode origin;
    private final CampusNode destination;
    private final List<ShadeSegment> segments;
    private final double totalDistanceMeters;
    private final double shadedDistanceMeters;
    private final double shadePercentage;
    private final int estimatedWalkingMinutes;
    private final List<String> turnByTurnInstructions;

    public ShadedRoute(CampusNode origin,
                       CampusNode destination,
                       List<ShadeSegment> segments,
                       double totalDistanceMeters,
                       double shadedDistanceMeters,
                       double shadePercentage,
                       int estimatedWalkingMinutes,
                       List<String> turnByTurnInstructions) {
        this.origin = origin;
        this.destination = destination;
        this.segments = segments != null ? new ArrayList<>(segments) : new ArrayList<>();
        this.totalDistanceMeters = totalDistanceMeters;
        this.shadedDistanceMeters = shadedDistanceMeters;
        this.shadePercentage = shadePercentage;
        this.estimatedWalkingMinutes = estimatedWalkingMinutes;
        this.turnByTurnInstructions = turnByTurnInstructions != null
                ? new ArrayList<>(turnByTurnInstructions)
                : new ArrayList<>();
    }

    public CampusNode getOrigin() {
        return origin;
    }

    public CampusNode getDestination() {
        return destination;
    }

    public List<ShadeSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public double getTotalDistanceMeters() {
        return totalDistanceMeters;
    }

    public double getShadedDistanceMeters() {
        return shadedDistanceMeters;
    }

    public double getShadePercentage() {
        return shadePercentage;
    }

    public int getEstimatedWalkingMinutes() {
        return estimatedWalkingMinutes;
    }

    public List<String> getTurnByTurnInstructions() {
        return Collections.unmodifiableList(turnByTurnInstructions);
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }
}