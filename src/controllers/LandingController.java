package controllers;

import ui.LandingPanel;

public class LandingController extends BaseController<LandingPanel> {

    private final Runnable onStartRequested;

    public LandingController(LandingPanel view, Runnable onStartRequested) {
        super(view);
        this.onStartRequested = onStartRequested;
        attach();
    }

    private void attach() {
        view.setOnStartHere(() -> {
            if (onStartRequested != null) {
                onStartRequested.run();
            }
        });
    }
}
