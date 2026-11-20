package com.um.carishade.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.um.carishade.R;
import com.um.carishade.model.ShadedRoute;

import java.util.List;
import java.util.Locale;

public class NavigationOverlayFragment extends Fragment {

    public interface OnNavigationActionListener {
        void onNextStepClicked(int stepIndex);
        void onStopNavigationClicked();
    }

    private ShadedRoute currentRoute;
    private int currentStepIndex = 0;
    private OnNavigationActionListener actionListener;

    private TextView tvRouteSummary;
    private TextView tvShadeBadge;
    private TextView tvCurrentInstruction;
    private Button btnNextStep;
    private Button btnStopNav;

    public static NavigationOverlayFragment newInstance(ShadedRoute route) {
        NavigationOverlayFragment fragment = new NavigationOverlayFragment();
        fragment.setRoute(route);
        return fragment;
    }

    public void setOnNavigationActionListener(OnNavigationActionListener listener) {
        this.actionListener = listener;
    }

    public void setRoute(ShadedRoute route) {
        this.currentRoute = route;
        this.currentStepIndex = 0;
        refreshUi();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_overlay, container, false);
        tvRouteSummary = view.findViewById(R.id.tvRouteSummary);
        tvShadeBadge = view.findViewById(R.id.tvShadeBadge);
        tvCurrentInstruction = view.findViewById(R.id.tvCurrentInstruction);
        btnNextStep = view.findViewById(R.id.btnNextStep);
        btnStopNav = view.findViewById(R.id.btnStopNav);
        refreshUi();
        return view;
    }

    private void refreshUi() {
        if (currentRoute == null || tvRouteSummary == null || tvShadeBadge == null || tvCurrentInstruction == null) {
            return;
        }

        String summary = String.format(Locale.getDefault(),
                "%d min • %.0f m total",
                currentRoute.getEstimatedWalkingMinutes(),
                currentRoute.getTotalDistanceMeters());
        tvRouteSummary.setText(summary);

        String shadeText = String.format(Locale.getDefault(),
                "%.0f%% Shaded",
                currentRoute.getShadePercentage());
        tvShadeBadge.setText(shadeText);

        List<String> steps = currentRoute.getTurnByTurnInstructions();
        if (steps.isEmpty()) {
            tvCurrentInstruction.setText("Select start and destination points on campus.");
        } else {
            int safeIndex = Math.min(currentStepIndex, steps.size() - 1);
            String stepText = steps.get(safeIndex);
            tvCurrentInstruction.setText(!TextUtils.isEmpty(stepText) ? stepText : "");
        }

        if (btnNextStep != null) {
            btnNextStep.setOnClickListener(v -> {
                if (!steps.isEmpty() && currentStepIndex < steps.size() - 1) {
                    currentStepIndex++;
                    refreshUi();
                }
                if (actionListener != null) {
                    actionListener.onNextStepClicked(currentStepIndex);
                }
            });
        }

        if (btnStopNav != null) {
            btnStopNav.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onStopNavigationClicked();
                }
            });
        }
    }
}