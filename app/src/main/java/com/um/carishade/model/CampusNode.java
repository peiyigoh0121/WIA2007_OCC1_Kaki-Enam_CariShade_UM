package com.um.carishade.model;

import java.util.Objects;

public class CampusNode {
    private final String nodeId;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final boolean isIndoorShelter;

    public CampusNode(String nodeId, String name, double latitude, double longitude, boolean isIndoorShelter) {
        this.nodeId = nodeId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isIndoorShelter = isIndoorShelter;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isIndoorShelter() {
        return isIndoorShelter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CampusNode)) return false;
        CampusNode that = (CampusNode) o;
        return Objects.equals(nodeId, that.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    @Override
    public String toString() {
        return name + " (" + nodeId + ")";
    }
}