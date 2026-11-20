package com.um.carishade.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.um.carishade.model.CampusNode;
import com.um.carishade.model.ShadeSegment;
import com.um.carishade.model.ShadeSegment.ShadeType;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShadeDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "carishade_shade_routing.db";
    private static final int DATABASE_VERSION = 1;

    // Nodes table
    public static final String TABLE_NODES = "campus_nodes";
    public static final String COL_NODE_ID = "node_id";
    public static final String COL_NODE_NAME = "name";
    public static final String COL_LAT = "latitude";
    public static final String COL_LNG = "longitude";
    public static final String COL_INDOOR_SHELTER = "is_indoor_shelter";

    // Segments table
    public static final String TABLE_SEGMENTS = "shade_segments";
    public static final String COL_SEGMENT_ID = "segment_id";
    public static final String COL_START_NODE_ID = "start_node_id";
    public static final String COL_END_NODE_ID = "end_node_id";
    public static final String COL_DISTANCE_M = "distance_meters";
    public static final String COL_SHADE_TYPE = "shade_type";
    public static final String COL_POLYLINE_JSON = "polyline_json";
    public static final String COL_INSTRUCTION = "instruction_text";

    public ShadeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createNodesTable = "CREATE TABLE " + TABLE_NODES + " ("
                + COL_NODE_ID + " TEXT PRIMARY KEY, "
                + COL_NODE_NAME + " TEXT NOT NULL, "
                + COL_LAT + " REAL NOT NULL, "
                + COL_LNG + " REAL NOT NULL, "
                + COL_INDOOR_SHELTER + " INTEGER NOT NULL DEFAULT 0"
                + ");";

        String createSegmentsTable = "CREATE TABLE " + TABLE_SEGMENTS + " ("
                + COL_SEGMENT_ID + " TEXT PRIMARY KEY, "
                + COL_START_NODE_ID + " TEXT NOT NULL, "
                + COL_END_NODE_ID + " TEXT NOT NULL, "
                + COL_DISTANCE_M + " REAL NOT NULL, "
                + COL_SHADE_TYPE + " TEXT NOT NULL, "
                + COL_POLYLINE_JSON + " TEXT NOT NULL, "
                + COL_INSTRUCTION + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_START_NODE_ID + ") REFERENCES " + TABLE_NODES + "(" + COL_NODE_ID + "), "
                + "FOREIGN KEY(" + COL_END_NODE_ID + ") REFERENCES " + TABLE_NODES + "(" + COL_NODE_ID + ")"
                + ");";

        db.execSQL(createNodesTable);
        db.execSQL(createSegmentsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEGMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES);
        onCreate(db);
    }

    public void insertOrUpdateNode(CampusNode node) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NODE_ID, node.getNodeId());
        values.put(COL_NODE_NAME, node.getName());
        values.put(COL_LAT, node.getLatitude());
        values.put(COL_LNG, node.getLongitude());
        values.put(COL_INDOOR_SHELTER, node.isIndoorShelter() ? 1 : 0);

        db.insertWithOnConflict(TABLE_NODES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void insertOrUpdateSegment(ShadeSegment segment) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SEGMENT_ID, segment.getSegmentId());
        values.put(COL_START_NODE_ID, segment.getStartNode().getNodeId());
        values.put(COL_END_NODE_ID, segment.getEndNode().getNodeId());
        values.put(COL_DISTANCE_M, segment.getDistanceMeters());
        values.put(COL_SHADE_TYPE, segment.getShadeType().name());
        values.put(COL_POLYLINE_JSON, coordsToJson(segment.getPolylineCoords()));
        values.put(COL_INSTRUCTION, segment.getInstructionText());

        db.insertWithOnConflict(TABLE_SEGMENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<CampusNode> getAllNodes() {
        List<CampusNode> nodes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(TABLE_NODES, null, null, null, null, null, COL_NODE_NAME + " ASC")) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_NODE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NODE_NAME));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LNG));
                boolean isShelter = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INDOOR_SHELTER)) == 1;
                nodes.add(new CampusNode(id, name, lat, lng, isShelter));
            }
        }
        return nodes;
    }

    public List<ShadeSegment> getAllSegments() {
        List<ShadeSegment> segments = new ArrayList<>();
        Map<String, CampusNode> nodeMap = new HashMap<>();
        for (CampusNode node : getAllNodes()) {
            nodeMap.put(node.getNodeId(), node);
        }

        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_SEGMENTS, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                String segId = cursor.getString(cursor.getColumnIndexOrThrow(COL_SEGMENT_ID));
                String startId = cursor.getString(cursor.getColumnIndexOrThrow(COL_START_NODE_ID));
                String endId = cursor.getString(cursor.getColumnIndexOrThrow(COL_END_NODE_ID));
                double dist = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DISTANCE_M));
                String shadeStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_SHADE_TYPE));
                String jsonStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_POLYLINE_JSON));
                String instruction = cursor.getString(cursor.getColumnIndexOrThrow(COL_INSTRUCTION));

                CampusNode startNode = nodeMap.get(startId);
                CampusNode endNode = nodeMap.get(endId);
                if (startNode == null || endNode == null) continue;

                ShadeType shadeType = ShadeType.valueOf(shadeStr);
                List<double[]> coords = jsonToCoords(jsonStr);
                segments.add(new ShadeSegment(segId, startNode, endNode, dist, shadeType, coords, instruction));
            }
        }
        return segments;
    }

    public boolean hasSeedData() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SEGMENTS, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        }
        return false;
    }

    private String coordsToJson(List<double[]> coords) {
        JSONArray root = new JSONArray();
        for (double[] pt : coords) {
            if (pt != null && pt.length >= 2) {
                try {
                    JSONArray pair = new JSONArray();
                    pair.put(pt[0]);
                    pair.put(pt[1]);
                    root.put(pair);
                } catch (JSONException ignored) {
                }
            }
        }
        return root.toString();
    }
    private List<double[]> jsonToCoords(String jsonStr) {
        List<double[]> coords = new ArrayList<>();
        try {
            JSONArray root = new JSONArray(jsonStr);
            for (int i = 0; i < root.length(); i++) {
                JSONArray pair = root.getJSONArray(i);
                if (pair.length() >= 2) {
                    coords.add(new double[]{pair.getDouble(0), pair.getDouble(1)});
                }
            }
        } catch (JSONException ignored) {
        }
        return coords;
    }
}