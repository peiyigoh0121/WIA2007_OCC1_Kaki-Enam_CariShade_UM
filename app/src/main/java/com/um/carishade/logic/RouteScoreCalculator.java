package com.um.carishade.logic;

import com.um.carishade.model.ShadeSegment;
import com.um.carishade.model.ShadeSegment.ShadeType;

import java.util.List;

public class RouteScoreCalculator {

    // Average walking speed: ~1.25 m/s (~75 meters per minute)
    private static final double WALKING_METERS_PER_MINUTE = 75.0;

    // Lower multiplier = more preferred (more shade)
    // Higher multiplier = penalized (high sun exposure)
    public double calculateSegmentCost(ShadeSegment segment, boolean highUvMode) {
        double baseDistance = segment.getDistanceMeters();
        double weight;

        ShadeType type = segment.getShadeType();
        if (type == ShadeType.INDOOR_AC) {
            weight = highUvMode ? 0.65 : 0.80;
        } else if (type == ShadeType.COVERED_WALKWAY) {
            weight = highUvMode ? 0.75 : 0.85;
        } else if (type == ShadeType.TREE_CANOPY) {
            weight = highUvMode ? 0.90 : 0.95;
        } else { // EXPOSED_SUN
            weight = highUvMode ? 2.80 : 1.80;
        }

        return baseDistance * weight;
    }

    public double calculateTotalDistance(List<ShadeSegment> segments) {
        double total = 0.0;
        for (ShadeSegment seg : segments) {
            total += seg.getDistanceMeters();
        }
        return total;
    }

    public double calculateShadedDistance(List<ShadeSegment> segments) {
        double shaded = 0.0;
        for (ShadeSegment seg : segments) {
            if (seg.isShaded()) {
                shaded += seg.getDistanceMeters();
            }
        }
        return shaded;
    }

    public double calculateShadePercentage(List<ShadeSegment> segments) {
        double total = calculateTotalDistance(segments);
        if (total <= 0.0) return 0.0;
        double shaded = calculateShadedDistance(segments);
        return (shaded / total) * 100.0;
    }

    public int estimateWalkingMinutes(List<ShadeSegment> segments) {
        double totalMeters = calculateTotalDistance(segments);
        if (totalMeters <= 0.0) return 0;
        return Math.max(1, (int) Math.round(totalMeters / WALKING_METERS_PER_MINUTE));
    }
}