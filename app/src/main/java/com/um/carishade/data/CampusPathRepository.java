package com.um.carishade.data;

import android.content.Context;

import com.um.carishade.model.CampusNode;
import com.um.carishade.model.ShadeSegment;
import com.um.carishade.model.ShadeSegment.ShadeType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CampusPathRepository {

    private final ShadeDatabaseHelper dbHelper;

    public CampusPathRepository(Context context) {
        this.dbHelper = new ShadeDatabaseHelper(context.getApplicationContext());
        seedDefaultCampusPathsIfNeeded();
    }

    public List<CampusNode> getNodes() {
        return dbHelper.getAllNodes();
    }

    public List<ShadeSegment> getSegments() {
        return dbHelper.getAllSegments();
    }

    private void seedDefaultCampusPathsIfNeeded() {
        if (dbHelper.hasSeedData()) {
            return;
        }

        // Sample UM campus nodes around KK8, KK10, Library corridor, and FSKTM
        CampusNode kk8 = new CampusNode("KK8", "Kolej Kediaman ke-8 (KK8)", 3.1246, 101.6512, false);
        CampusNode kk10 = new CampusNode("KK10", "Kolej Kediaman ke-10 (KK10)", 3.1238, 101.6528, false);
        CampusNode canopyJunction = new CampusNode("JUNC_A", "Tree-Lined Campus Junction", 3.1225, 101.6535, false);
        CampusNode libraryCorridor = new CampusNode("LIB_CORR", "Central Library Covered Walkway", 3.1214, 101.6548, true);
        CampusNode fsktm = new CampusNode("FSKTM", "FSKTM Faculty Entrance", 3.1208, 101.6562, true);

        List<CampusNode> nodes = Arrays.asList(kk8, kk10, canopyJunction, libraryCorridor, fsktm);
        for (CampusNode n : nodes) {
            dbHelper.insertOrUpdateNode(n);
        }

        // Shaded route vs shorter sun-exposed shortcut
        ShadeSegment seg1 = new ShadeSegment(
                "SEG_1", kk8, canopyJunction, 240.0, ShadeType.TREE_CANOPY,
                Arrays.asList(new double[]{3.1246, 101.6512}, new double[]{3.1225, 101.6535}),
                "Walk along the tree-canopied footpath toward the campus junction."
        );

        ShadeSegment seg2 = new ShadeSegment(
                "SEG_2", canopyJunction, libraryCorridor, 190.0, ShadeType.COVERED_WALKWAY,
                Arrays.asList(new double[]{3.1225, 101.6535}, new double[]{3.1214, 101.6548}),
                "Continue along the covered walkway past the Central Library."
        );

        ShadeSegment seg3 = new ShadeSegment(
                "SEG_3", libraryCorridor, fsktm, 160.0, ShadeType.INDOOR_AC,
                Arrays.asList(new double[]{3.1214, 101.6548}, new double[]{3.1208, 101.6562}),
                "Pass through the indoor sheltered corridor directly into FSKTM."
        );

        // Shorter direct path, but heavily exposed to direct sunlight
        ShadeSegment directSunnyShortcut = new ShadeSegment(
                "SEG_SUN_DIRECT", kk8, fsktm, 420.0, ShadeType.EXPOSED_SUN,
                Arrays.asList(new double[]{3.1246, 101.6512}, new double[]{3.1208, 101.6562}),
                "Direct open road to FSKTM (high sun exposure—not recommended during peak UV)."
        );

        ShadeSegment kk10ToJunction = new ShadeSegment(
                "SEG_KK10_JUNC", kk10, canopyJunction, 175.0, ShadeType.TREE_CANOPY,
                Arrays.asList(new double[]{3.1238, 101.6528}, new double[]{3.1225, 101.6535}),
                "Head south under the tree canopy from KK10 to the main junction."
        );

        List<ShadeSegment> segments = Arrays.asList(seg1, seg2, seg3, directSunnyShortcut, kk10ToJunction);
        for (ShadeSegment s : segments) {
            dbHelper.insertOrUpdateSegment(s);
        }
    }

    public CampusNode findNodeById(String nodeId) {
        for (CampusNode node : getNodes()) {
            if (node.getNodeId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    public List<ShadeSegment> getSegmentsForNode(String nodeId) {
        if (nodeId == null) return Collections.emptyList();
        return getSegments();
    }
}