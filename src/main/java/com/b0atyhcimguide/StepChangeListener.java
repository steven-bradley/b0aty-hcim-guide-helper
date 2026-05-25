package com.b0atyhcimguide;

/**
 * Listener interface for step change events.
 * Implementations are notified when the current guide step changes.
 */
@FunctionalInterface
public interface StepChangeListener {
    /**
     * Called when the current step changes.
     *
     * @param newStep  the new current guide step
     * @param newIndex the index of the new step in the guide data
     */
    void onStepChanged(GuideStep newStep, int newIndex);
}
