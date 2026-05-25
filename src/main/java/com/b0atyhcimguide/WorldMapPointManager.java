package com.b0atyhcimguide;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;

/**
 * Manages world map point markers for the current guide step.
 * Places a marker at the step's location when available and respects
 * the enableWorldMap config toggle.
 */
@Slf4j
@Singleton
public class WorldMapPointManager implements StepChangeListener
{
    private final net.runelite.client.ui.overlay.worldmap.WorldMapPointManager worldMapPointManager;
    private final B0atyHcimGuideConfig config;
    private final StepTracker stepTracker;

    private WorldMapPoint currentMarker;

    @Inject
    public WorldMapPointManager(
        net.runelite.client.ui.overlay.worldmap.WorldMapPointManager worldMapPointManager,
        B0atyHcimGuideConfig config,
        StepTracker stepTracker)
    {
        this.worldMapPointManager = worldMapPointManager;
        this.config = config;
        this.stepTracker = stepTracker;

        stepTracker.addStepChangeListener(this);
        updateMarkerForCurrentStep();
    }

    @Override
    public void onStepChanged(GuideStep newStep, int newIndex)
    {
        updateMarkerFromStep(newStep);
    }

    /**
     * Updates the world map marker for the given location and tooltip.
     * Removes any existing marker before placing a new one.
     *
     * @param location the world point to place the marker at
     * @param tooltip  the tooltip text to display on hover/click
     */
    public void updateMarker(WorldPoint location, String tooltip)
    {
        clearMarker();

        if (location == null)
        {
            return;
        }

        currentMarker = new WorldMapPoint(location, null);
        currentMarker.setTooltip(tooltip);
        currentMarker.setSnapToEdge(true);
        currentMarker.setJumpOnClick(true);
        worldMapPointManager.add(currentMarker);
    }

    /**
     * Removes the current world map marker if one exists.
     */
    public void clearMarker()
    {
        if (currentMarker != null)
        {
            worldMapPointManager.remove(currentMarker);
            currentMarker = null;
        }
    }

    /**
     * Cleans up listener registration and removes any active marker.
     */
    public void shutdown()
    {
        clearMarker();
        stepTracker.removeStepChangeListener(this);
    }

    /**
     * Returns the current world map marker, or null if none is active.
     * Exposed for testing.
     */
    public WorldMapPoint getCurrentMarker()
    {
        return currentMarker;
    }

    private void updateMarkerForCurrentStep()
    {
        GuideStep step = stepTracker.getCurrentStep();
        updateMarkerFromStep(step);
    }

    private void updateMarkerFromStep(GuideStep step)
    {
        if (!config.enableWorldMap())
        {
            clearMarker();
            return;
        }

        if (step == null || step.getLocation() == null)
        {
            clearMarker();
            return;
        }

        updateMarker(step.getLocation(), step.getInstruction());
    }
}
