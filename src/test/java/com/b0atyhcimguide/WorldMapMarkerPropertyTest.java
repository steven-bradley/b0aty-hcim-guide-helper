package com.b0atyhcimguide;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Feature: b0aty-hcim-guide-overlay
 * Property 7: World map marker matches current step location
 *
 * For any current step, if the step has a non-null location, a world map marker
 * exists at exactly those coordinates; if the step has no location, no world map
 * marker exists.
 *
 * Validates: Requirements 5.1, 5.2
 */
class WorldMapMarkerPropertyTest
{
    private GuideDataStore createGuideDataStore(List<GuideStep> steps)
    {
        GuideDataStore store = mock(GuideDataStore.class);
        when(store.getTotalSteps()).thenReturn(steps.size());
        when(store.getAllSteps()).thenReturn(steps);
        for (int i = 0; i < steps.size(); i++)
        {
            when(store.getStep(i)).thenReturn(steps.get(i));
        }
        List<String> sections = new ArrayList<>();
        for (GuideStep step : steps)
        {
            if (!sections.contains(step.getSection()))
            {
                sections.add(step.getSection());
            }
        }
        when(store.getSectionNames()).thenReturn(sections);
        return store;
    }

    private ConfigManager createConfigManager(int initialStep)
    {
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getConfiguration("b0atyhcimguide", "currentStep"))
            .thenReturn(String.valueOf(initialStep));
        return configManager;
    }

    @Property(tries = 100)
    void markerExistsAtStepLocationWhenNonNull(
        @ForAll @IntRange(min = 1, max = 10000) int x,
        @ForAll @IntRange(min = 1, max = 13000) int y,
        @ForAll @IntRange(min = 0, max = 3) int plane)
    {
        WorldPoint location = new WorldPoint(x, y, plane);
        GuideStep step = new GuideStep(1, "Section", "Go to location", List.of(), location);
        List<GuideStep> steps = List.of(step);

        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableWorldMap()).thenReturn(true);

        net.runelite.client.ui.overlay.worldmap.WorldMapPointManager runeliteManager =
            mock(net.runelite.client.ui.overlay.worldmap.WorldMapPointManager.class);

        WorldMapPointManager manager = new WorldMapPointManager(runeliteManager, config, tracker);

        WorldMapPoint marker = manager.getCurrentMarker();
        assertNotNull(marker, "Marker should exist when step has a location");
        assertEquals(location, marker.getWorldPoint(),
            "Marker world point should match step location");
    }

    @Property(tries = 100)
    void noMarkerWhenStepLocationIsNull(
        @ForAll @IntRange(min = 1, max = 20) int totalSteps)
    {
        // Build steps with no location
        List<GuideStep> steps = new ArrayList<>();
        for (int i = 0; i < totalSteps; i++)
        {
            steps.add(new GuideStep(i + 1, "Section", "Step " + i, List.of(), null));
        }

        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableWorldMap()).thenReturn(true);

        net.runelite.client.ui.overlay.worldmap.WorldMapPointManager runeliteManager =
            mock(net.runelite.client.ui.overlay.worldmap.WorldMapPointManager.class);

        WorldMapPointManager manager = new WorldMapPointManager(runeliteManager, config, tracker);

        WorldMapPoint marker = manager.getCurrentMarker();
        assertNull(marker, "No marker should exist when step has no location");
    }

    @Property(tries = 100)
    void markerUpdatesOnStepChange(
        @ForAll @IntRange(min = 1, max = 10000) int x1,
        @ForAll @IntRange(min = 1, max = 13000) int y1,
        @ForAll @IntRange(min = 1, max = 10000) int x2,
        @ForAll @IntRange(min = 1, max = 13000) int y2)
    {
        WorldPoint loc1 = new WorldPoint(x1, y1, 0);
        WorldPoint loc2 = new WorldPoint(x2, y2, 0);

        GuideStep step1 = new GuideStep(1, "Section A", "Go here", List.of(), loc1);
        GuideStep step2 = new GuideStep(2, "Section A", "Go there", List.of(), loc2);
        List<GuideStep> steps = List.of(step1, step2);

        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableWorldMap()).thenReturn(true);

        net.runelite.client.ui.overlay.worldmap.WorldMapPointManager runeliteManager =
            mock(net.runelite.client.ui.overlay.worldmap.WorldMapPointManager.class);

        WorldMapPointManager manager = new WorldMapPointManager(runeliteManager, config, tracker);

        // Initially at step 1
        WorldMapPoint marker1 = manager.getCurrentMarker();
        assertNotNull(marker1);
        assertEquals(loc1, marker1.getWorldPoint());

        // Advance to step 2
        tracker.nextStep();

        WorldMapPoint marker2 = manager.getCurrentMarker();
        assertNotNull(marker2);
        assertEquals(loc2, marker2.getWorldPoint(),
            "Marker should update to new step's location on step change");
    }

    @Property(tries = 100)
    void markerClearedWhenNavigatingToStepWithNoLocation(
        @ForAll @IntRange(min = 1, max = 10000) int x,
        @ForAll @IntRange(min = 1, max = 13000) int y)
    {
        WorldPoint location = new WorldPoint(x, y, 0);

        GuideStep stepWithLoc = new GuideStep(1, "Section", "Go here", List.of(), location);
        GuideStep stepNoLoc = new GuideStep(2, "Section", "Do something", List.of(), null);
        List<GuideStep> steps = List.of(stepWithLoc, stepNoLoc);

        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableWorldMap()).thenReturn(true);

        net.runelite.client.ui.overlay.worldmap.WorldMapPointManager runeliteManager =
            mock(net.runelite.client.ui.overlay.worldmap.WorldMapPointManager.class);

        WorldMapPointManager manager = new WorldMapPointManager(runeliteManager, config, tracker);

        // Initially has marker
        assertNotNull(manager.getCurrentMarker());

        // Advance to step without location
        tracker.nextStep();

        assertNull(manager.getCurrentMarker(),
            "Marker should be cleared when navigating to step with no location");
    }

    @Property(tries = 100)
    void markerTooltipMatchesStepInstruction(
        @ForAll("instructions") String instruction,
        @ForAll @IntRange(min = 1, max = 10000) int x,
        @ForAll @IntRange(min = 1, max = 13000) int y)
    {
        WorldPoint location = new WorldPoint(x, y, 0);
        GuideStep step = new GuideStep(1, "Section", instruction, List.of(), location);
        List<GuideStep> steps = List.of(step);

        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableWorldMap()).thenReturn(true);

        net.runelite.client.ui.overlay.worldmap.WorldMapPointManager runeliteManager =
            mock(net.runelite.client.ui.overlay.worldmap.WorldMapPointManager.class);

        WorldMapPointManager manager = new WorldMapPointManager(runeliteManager, config, tracker);

        WorldMapPoint marker = manager.getCurrentMarker();
        assertNotNull(marker);
        assertEquals(instruction, marker.getTooltip(),
            "Marker tooltip should match step instruction text");
    }

    @Property(tries = 100)
    void noMarkerWhenWorldMapDisabled(
        @ForAll @IntRange(min = 1, max = 10000) int x,
        @ForAll @IntRange(min = 1, max = 13000) int y)
    {
        WorldPoint location = new WorldPoint(x, y, 0);
        GuideStep step = new GuideStep(1, "Section", "Go here", List.of(), location);
        List<GuideStep> steps = List.of(step);

        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableWorldMap()).thenReturn(false);

        net.runelite.client.ui.overlay.worldmap.WorldMapPointManager runeliteManager =
            mock(net.runelite.client.ui.overlay.worldmap.WorldMapPointManager.class);

        WorldMapPointManager manager = new WorldMapPointManager(runeliteManager, config, tracker);

        assertNull(manager.getCurrentMarker(),
            "No marker should exist when world map markers are disabled");
    }

    @Provide
    Arbitrary<String> instructions()
    {
        return Arbitraries.of(
            "Talk to the Gielinor Guide",
            "Pick up the bronze axe",
            "Walk to Lumbridge",
            "Mine copper ore",
            "Fish shrimps at the coast",
            "Chop a tree for logs",
            "Complete the quest",
            "Bank your items"
        );
    }
}
