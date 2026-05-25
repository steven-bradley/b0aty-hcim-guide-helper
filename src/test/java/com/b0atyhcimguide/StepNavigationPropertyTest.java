package com.b0atyhcimguide;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.runelite.client.config.ConfigManager;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Feature: b0aty-hcim-guide-overlay
 * Property 4: Step navigation correctness
 *
 * For any index i, verify nextStep produces min(i+1, total-1)
 * and previousStep produces max(i-1, 0).
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.8, 3.9
 */
class StepNavigationPropertyTest
{
    /**
     * Creates a GuideDataStore with the given number of steps.
     */
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

    /**
     * Creates a ConfigManager mock that stores and retrieves the current step.
     */
    private ConfigManager createConfigManager(int initialStep)
    {
        ConfigManager configManager = mock(ConfigManager.class);
        when(configManager.getConfiguration("b0atyhcimguide", "currentStep"))
            .thenReturn(String.valueOf(initialStep));
        return configManager;
    }

    @Property(tries = 100)
    void nextStepProducesMinIPlusOneTotalMinusOne(
        @ForAll @IntRange(min = 1, max = 200) int totalSteps,
        @ForAll @IntRange(min = 0, max = 199) int startIndex)
    {
        Assume.that(startIndex < totalSteps);

        ConfigManager configManager = createConfigManager(startIndex);
        GuideDataStore store = createGuideDataStore(totalSteps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        assertEquals(startIndex, tracker.getCurrentStepIndex());

        tracker.nextStep();

        int expected = Math.min(startIndex + 1, totalSteps - 1);
        assertEquals(expected, tracker.getCurrentStepIndex(),
            String.format("nextStep from index %d with %d total steps should produce %d",
                startIndex, totalSteps, expected));
    }

    @Property(tries = 100)
    void previousStepProducesMaxIMinusOneZero(
        @ForAll @IntRange(min = 1, max = 200) int totalSteps,
        @ForAll @IntRange(min = 0, max = 199) int startIndex)
    {
        Assume.that(startIndex < totalSteps);

        ConfigManager configManager = createConfigManager(startIndex);
        GuideDataStore store = createGuideDataStore(totalSteps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        assertEquals(startIndex, tracker.getCurrentStepIndex());

        tracker.previousStep();

        int expected = Math.max(startIndex - 1, 0);
        assertEquals(expected, tracker.getCurrentStepIndex(),
            String.format("previousStep from index %d with %d total steps should produce %d",
                startIndex, totalSteps, expected));
    }

    @Property(tries = 100)
    void nextStepAtLastIndexStaysAtLast(
        @ForAll @IntRange(min = 1, max = 200) int totalSteps)
    {
        int lastIndex = totalSteps - 1;
        ConfigManager configManager = createConfigManager(lastIndex);
        GuideDataStore store = createGuideDataStore(totalSteps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        tracker.nextStep();

        assertEquals(lastIndex, tracker.getCurrentStepIndex(),
            "nextStep at last index should stay at last index");
    }

    @Property(tries = 100)
    void previousStepAtFirstIndexStaysAtFirst(
        @ForAll @IntRange(min = 1, max = 200) int totalSteps)
    {
        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(totalSteps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        tracker.previousStep();

        assertEquals(0, tracker.getCurrentStepIndex(),
            "previousStep at first index should stay at 0");
    }
}
