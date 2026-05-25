package com.b0atyhcimguide;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.runelite.client.config.ConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Feature: b0aty-hcim-guide-overlay
 * Property 5: Step persistence round trip
 *
 * For any valid step index, persisting it via ConfigManager and then
 * reading it back must return the same index value.
 *
 * Validates: Requirements 3.5
 */
class StepPersistencePropertyTest
{
    private static final String CONFIG_GROUP = "b0atyhcimguide";
    private static final String CURRENT_STEP_KEY = "currentStep";

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
     * Creates a ConfigManager mock that simulates real storage behavior
     * by backing setConfiguration/getConfiguration with a HashMap.
     */
    private ConfigManager createStoringConfigManager(int initialStep)
    {
        Map<String, String> storage = new HashMap<>();
        storage.put(CURRENT_STEP_KEY, String.valueOf(initialStep));

        ConfigManager configManager = mock(ConfigManager.class);

        doAnswer(invocation ->
        {
            String key = invocation.getArgument(1);
            Object value = invocation.getArgument(2);
            storage.put(key, String.valueOf(value));
            return null;
        }).when(configManager).setConfiguration(anyString(), anyString(), any(int.class));

        when(configManager.getConfiguration(eq(CONFIG_GROUP), eq(CURRENT_STEP_KEY)))
            .thenAnswer(invocation -> storage.get(CURRENT_STEP_KEY));

        return configManager;
    }

    @Property(tries = 100)
    void persistingStepIndexAndReadingBackReturnsSameValue(
        @ForAll @IntRange(min = 2, max = 200) int totalSteps,
        @ForAll @IntRange(min = 0, max = 199) int targetIndex)
    {
        Assume.that(targetIndex < totalSteps);

        // Start at index 0, then jump to targetIndex which triggers persistence
        ConfigManager configManager = createStoringConfigManager(0);
        GuideDataStore store = createGuideDataStore(totalSteps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        // Jump to the target step (this persists the index)
        tracker.jumpToStep(targetIndex);

        // Create a new StepTracker that reads the persisted value
        StepTracker tracker2 = new StepTracker(store, configManager);
        tracker2.initialize();

        assertEquals(targetIndex, tracker2.getCurrentStepIndex(),
            String.format("Persisting step %d and reading back should return %d",
                targetIndex, targetIndex));
    }

    @Property(tries = 100)
    void navigationPersistsCorrectly(
        @ForAll @IntRange(min = 3, max = 200) int totalSteps,
        @ForAll @IntRange(min = 1, max = 198) int startIndex)
    {
        Assume.that(startIndex < totalSteps - 1);

        ConfigManager configManager = createStoringConfigManager(startIndex);
        GuideDataStore store = createGuideDataStore(totalSteps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        // Advance one step
        tracker.nextStep();
        int expectedAfterNext = startIndex + 1;

        // Read back from a fresh tracker
        StepTracker tracker2 = new StepTracker(store, configManager);
        tracker2.initialize();

        assertEquals(expectedAfterNext, tracker2.getCurrentStepIndex(),
            "After nextStep, persisted value should be read back correctly");
    }

    @Property(tries = 100)
    void persistedValueSurvivesMultipleNavigations(
        @ForAll @IntRange(min = 5, max = 100) int totalSteps,
        @ForAll @IntRange(min = 0, max = 99) int startIndex,
        @ForAll @IntRange(min = 1, max = 10) int forwardSteps)
    {
        Assume.that(startIndex < totalSteps);

        ConfigManager configManager = createStoringConfigManager(startIndex);
        GuideDataStore store = createGuideDataStore(totalSteps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        // Navigate forward multiple times
        for (int i = 0; i < forwardSteps; i++)
        {
            tracker.nextStep();
        }

        int expectedIndex = Math.min(startIndex + forwardSteps, totalSteps - 1);

        // Read back from a fresh tracker
        StepTracker tracker2 = new StepTracker(store, configManager);
        tracker2.initialize();

        assertEquals(expectedIndex, tracker2.getCurrentStepIndex(),
            String.format("After %d nextStep calls from %d, persisted value should be %d",
                forwardSteps, startIndex, expectedIndex));
    }
}
