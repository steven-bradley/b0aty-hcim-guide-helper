package com.b0atyhcimguide;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.runelite.client.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Feature: b0aty-hcim-guide-overlay
 * Property 8: Reset progress sets step correctly
 *
 * For any valid step number n (where 0 <= n < totalSteps),
 * resetting progress to n must result in the current step index being n.
 *
 * Validates: Requirements 6.5
 */
class StepResetProgressPropertyTest
{
    private GuideDataStore createGuideDataStore(int totalSteps)
    {
        GuideDataStore store = mock(GuideDataStore.class);
        when(store.getTotalSteps()).thenReturn(totalSteps);

        List<GuideStep> steps = new ArrayList<>();
        for (int i = 0; i < totalSteps; i++)
        {
            GuideStep step = new GuideStep(i + 1, "Section", "Step " + (i + 1), List.of(), null);
            steps.add(step);
            when(store.getStep(i)).thenReturn(step);
        }
        when(store.getAllSteps()).thenReturn(steps);

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
    void jumpToStepResultsInCorrectIndex(
        @ForAll @IntRange(min = 1, max = 200) int totalSteps,
        @ForAll @IntRange(min = 0, max = 199) int startIndex,
        @ForAll @IntRange(min = 0, max = 199) int targetIndex)
    {
        Assume.that(startIndex < totalSteps);
        Assume.that(targetIndex < totalSteps);

        ConfigManager configManager = createConfigManager(startIndex);
        GuideDataStore store = createGuideDataStore(totalSteps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        tracker.jumpToStep(targetIndex);

        assertEquals(targetIndex, tracker.getCurrentStepIndex(),
            String.format("jumpToStep(%d) from index %d with %d total steps should result in index %d",
                targetIndex, startIndex, totalSteps, targetIndex));
    }

    @Property(tries = 100)
    void jumpToStepClampsOutOfBoundsIndex(
        @ForAll @IntRange(min = 1, max = 100) int totalSteps,
        @ForAll @IntRange(min = 100, max = 500) int outOfBoundsIndex)
    {
        Assume.that(outOfBoundsIndex >= totalSteps);

        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(totalSteps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        tracker.jumpToStep(outOfBoundsIndex);

        assertEquals(totalSteps - 1, tracker.getCurrentStepIndex(),
            String.format("jumpToStep(%d) with %d total steps should clamp to %d",
                outOfBoundsIndex, totalSteps, totalSteps - 1));
    }

    @Property(tries = 100)
    void jumpToStepClampsNegativeIndex(
        @ForAll @IntRange(min = 1, max = 200) int totalSteps,
        @ForAll @IntRange(min = -500, max = -1) int negativeIndex)
    {
        ConfigManager configManager = createConfigManager(5 < totalSteps ? 5 : 0);
        GuideDataStore store = createGuideDataStore(totalSteps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        tracker.jumpToStep(negativeIndex);

        assertEquals(0, tracker.getCurrentStepIndex(),
            String.format("jumpToStep(%d) should clamp to 0", negativeIndex));
    }
}
