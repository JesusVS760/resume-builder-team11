package controllers;

import ui.LandingPanel;

public class LandingController extends BaseController<LandingPanel> {

    // For when the user clicks "Start Here" on the home page
    private final Runnable onStartRequested;

    public LandingController(LandingPanel view, Runnable onStartRequested) {
        super(view);
        this.onStartRequested = onStartRequested;
        attach(); // Wires the listeners
    }

    private void attach() {
        // When the user clicks "Start Here"
        view.setOnStartHere(() -> {
            if (onStartRequested != null) {
                onStartRequested.run();
            }
        });
    }
}
