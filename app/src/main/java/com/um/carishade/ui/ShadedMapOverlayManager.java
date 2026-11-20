package com.um.carishade.ui;

import android.graphics.Color;

import com.um.carishade.model.ShadeSegment;
import com.um.carishade.model.ShadeSegment.ShadeType;
import com.um.carishade.model.ShadedRoute;

import java.util.List;

// TODO: Import com.google.android.gms.maps.GoogleMap;
// TODO: Import com.google.android.gms.maps.model.LatLng;
// TODO: Import com.google.android.gms.maps.model.PolylineOptions;

public class ShadedMapOverlayManager {

    // private GoogleMap googleMap;

    // public void attachMap(GoogleMap map) {
    //     this.googleMap = map;
    // }

    public int getColorForShadeType(ShadeType shadeType) {
        switch (shadeType) {
            case INDOOR_AC:
                return Color.parseColor("#0D9488"); // Teal for indoor AC corridors
            case COVERED_WALKWAY:
                return Color.parseColor("#15803D"); // Deep green for covered walkways
            case TREE_CANOPY:
                return Color.parseColor("#65A30D"); // Leaf green for tree shade
            case EXPOSED_SUN:
            default:
                return Color.parseColor("#EA580C"); // Warm orange for sun exposure
        }
    }

    public void renderShadedNetwork(List<ShadeSegment> segments) {
        if (segments == null || segments.isEmpty()) return;

        for (ShadeSegment segment : segments) {
            int color = getColorForShadeType(segment.getShadeType());
            List<double[]> coords = segment.getPolylineCoords();

            // TODO: Once GoogleMap SDK is added in Part C, draw polylines like this:
            // if (googleMap != null && coords.size() >= 2) {
            //     PolylineOptions options = new PolylineOptions()
            //             .color(color)
            //             .width(10f);
            //     for (double[] latLng : coords) {
            //         options.add(new LatLng(latLng[0], latLng[1]));
            //     }
            //     googleMap.addPolyline(options);
            // }
            @SuppressWarnings("unused")
            int unusedColor = color;
            @SuppressWarnings("unused")
            int pointCount = coords.size();
        }
    }

    public void highlightActiveRoute(ShadedRoute route) {
        if (route == null || route.isEmpty()) return;

        // TODO: Clear previous active route polyline and render highlighted route segments
        // renderShadedNetwork(route.getSegments());
    }
}