package com.um.carishade;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.um.carishade.data.CampusPathRepository;
import com.um.carishade.logic.RouteScoreCalculator;
import com.um.carishade.logic.ShadedPathfinder;
import com.um.carishade.model.CampusNode;
import com.um.carishade.model.ShadeSegment;
import com.um.carishade.model.ShadedRoute;
import com.um.carishade.ui.NavigationOverlayFragment;
import com.um.carishade.ui.ShadedMapOverlayManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CariShade_PartD";

    private CampusPathRepository pathRepository;
    private ShadedPathfinder shadedPathfinder;
    private ShadedMapOverlayManager overlayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initPartDShadedRouting();
    }

    private void initPartDShadedRouting() {
        // 1. Initialize SQLite repository & seed default KK8 -> KK10 -> FSKTM segments
        pathRepository = new CampusPathRepository(this);
        List<ShadeSegment> allSegments = pathRepository.getSegments();

        // 2. Initialize score calculator & pathfinder
        RouteScoreCalculator calculator = new RouteScoreCalculator();
        shadedPathfinder = new ShadedPathfinder(calculator);

        // 3. Prepare map overlay manager (ready to attach GoogleMap from Part C later)
        overlayManager = new ShadedMapOverlayManager();
        overlayManager.renderShadedNetwork(allSegments);

        // 4. Test route calculation from KK8 to FSKTM under high UV conditions
        CampusNode origin = pathRepository.findNodeById("KK8");
        CampusNode destination = pathRepository.findNodeById("FSKTM");

        if (origin != null && destination != null) {
            ShadedRoute bestRoute = shadedPathfinder.findOptimalShadedRoute(
                    origin, destination, allSegments, true
            );
            overlayManager.highlightActiveRoute(bestRoute);

            Log.d(TAG, String.format(
                    "Route %s -> %s: %.0fm, %d min, %.1f%% shaded",
                    origin.getName(),
                    destination.getName(),
                    bestRoute.getTotalDistanceMeters(),
                    bestRoute.getEstimatedWalkingMinutes(),
                    bestRoute.getShadePercentage()
            ));

            for (String step : bestRoute.getTurnByTurnInstructions()) {
                Log.d(TAG, "Step: " + step);
            }

            // 5. Attach NavigationOverlayFragment to R.id.nav_overlay_container in activity_main.xml
            NavigationOverlayFragment navFragment = NavigationOverlayFragment.newInstance(bestRoute);
            navFragment.setOnNavigationActionListener(new NavigationOverlayFragment.OnNavigationActionListener() {
                @Override
                public void onNextStepClicked(int stepIndex) {
                    Log.d(TAG, "Advanced to navigation step index: " + stepIndex);
                }

                @Override
                public void onStopNavigationClicked() {
                    Log.d(TAG, "Navigation stopped by user");
                    View container = findViewById(R.id.nav_overlay_container);
                    if (container != null) {
                        container.setVisibility(View.GONE);
                    }
                }
            });

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_overlay_container, navFragment)
                    .commit();
        }
    }
}