package com.b0atyhcimguide;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.runelite.client.config.ConfigManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Feature: b0aty-hcim-guide-overlay
 * Property 10: Jump to section lands on first step
 *
 * For any section name present in the guide data, jumping to that section
 * must set the current step index to the index of the first step whose
 * section field equals that name.
 *
 * Validates: Requirements 7.2
 */
class StepJumpToSectionPropertyTest
{
    private static final String[] SECTION_POOL = {
        "Tutorial Island", "Lumbridge", "Varrock", "Falador",
        "Draynor", "Al Kharid", "Edgeville", "Barbarian Village"
    };

    /**
     * Creates a GuideDataStore with multiple sections, each containing
     * the specified number of steps per section.
     */
    private GuideDataStore createGuideDataStore(List<String> sectionOrder, Map<String, Integer> stepsPerSection)
    {
        GuideDataStore store = mock(GuideDataStore.class);

        List<GuideStep> steps = new ArrayList<>();
        Map<String, Integer> firstStepIndex = new LinkedHashMap<>();

        for (String section : sectionOrder)
        {
            int count = stepsPerSection.get(section);
            if (!firstStepIndex.containsKey(section))
            {
                firstStepIndex.put(section, steps.size());
            }
            for (int j = 0; j < count; j++)
            {
                GuideStep step = new GuideStep(steps.size() + 1, section,
                    "Step " + (steps.size() + 1) + " in " + section, List.of(), null);
                steps.add(step);
            }
        }

        when(store.getTotalSteps()).thenReturn(steps.size());
        when(store.getAllSteps()).thenReturn(steps);
        when(store.getSectionNames()).thenReturn(new ArrayList<>(firstStepIndex.keySet()));

        for (int i = 0; i < steps.size(); i++)
        {
            when(store.getStep(i)).thenReturn(steps.get(i));
        }

        for (Map.Entry<String, Integer> entry : firstStepIndex.entrySet())
        {
            when(store.getFirstStepOfSection(entry.getKey())).thenReturn(entry.getValue());
        }

        // Unknown sections return -1
        when(store.getFirstStepOfSection(argThat(arg ->
            arg != null && !firstStepIndex.containsKey(arg)))).thenReturn(-1);

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
    void jumpToSectionLandsOnFirstStepOfThatSection(
        @ForAll @IntRange(min = 2, max = 6) int numSections,
        @ForAll @IntRange(min = 1, max = 10) int stepsPerSectionCount,
        @ForAll @IntRange(min = 0, max = 5) int targetSectionIdx)
    {
        int actualNumSections = Math.min(numSections, SECTION_POOL.length);
        Assume.that(targetSectionIdx < actualNumSections);

        List<String> sectionOrder = new ArrayList<>();
        Map<String, Integer> stepsPerSection = new LinkedHashMap<>();

        for (int i = 0; i < actualNumSections; i++)
        {
            String section = SECTION_POOL[i];
            sectionOrder.add(section);
            stepsPerSection.put(section, stepsPerSectionCount);
        }

        GuideDataStore store = createGuideDataStore(sectionOrder, stepsPerSection);
        int totalSteps = store.getTotalSteps();

        // Start at an arbitrary valid position (middle of the guide)
        int startIndex = Math.min(totalSteps / 2, totalSteps - 1);
        ConfigManager configManager = createConfigManager(startIndex);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        String targetSection = sectionOrder.get(targetSectionIdx);
        tracker.jumpToSection(targetSection);

        int expectedIndex = store.getFirstStepOfSection(targetSection);
        assertEquals(expectedIndex, tracker.getCurrentStepIndex(),
            String.format("jumpToSection('%s') should set index to %d (first step of that section)",
                targetSection, expectedIndex));
    }

    @Property(tries = 100)
    void jumpToSectionFromAnyStartingPosition(
        @ForAll @IntRange(min = 2, max = 5) int numSections,
        @ForAll @IntRange(min = 2, max = 8) int stepsPerSectionCount,
        @ForAll @IntRange(min = 0, max = 39) int startIndex,
        @ForAll @IntRange(min = 0, max = 4) int targetSectionIdx)
    {
        int actualNumSections = Math.min(numSections, SECTION_POOL.length);
        Assume.that(targetSectionIdx < actualNumSections);

        List<String> sectionOrder = new ArrayList<>();
        Map<String, Integer> stepsPerSection = new LinkedHashMap<>();

        for (int i = 0; i < actualNumSections; i++)
        {
            String section = SECTION_POOL[i];
            sectionOrder.add(section);
            stepsPerSection.put(section, stepsPerSectionCount);
        }

        GuideDataStore store = createGuideDataStore(sectionOrder, stepsPerSection);
        int totalSteps = store.getTotalSteps();
        Assume.that(startIndex < totalSteps);

        ConfigManager configManager = createConfigManager(startIndex);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        String targetSection = sectionOrder.get(targetSectionIdx);
        tracker.jumpToSection(targetSection);

        int expectedIndex = store.getFirstStepOfSection(targetSection);
        assertEquals(expectedIndex, tracker.getCurrentStepIndex(),
            String.format("From index %d, jumpToSection('%s') should land on index %d",
                startIndex, targetSection, expectedIndex));
    }

    @Property(tries = 100)
    void jumpToUnknownSectionDoesNotChangeIndex(
        @ForAll @IntRange(min = 1, max = 100) int totalSteps,
        @ForAll @IntRange(min = 0, max = 99) int startIndex)
    {
        Assume.that(startIndex < totalSteps);

        List<String> sectionOrder = List.of("Tutorial Island");
        Map<String, Integer> stepsPerSection = new LinkedHashMap<>();
        stepsPerSection.put("Tutorial Island", totalSteps);

        GuideDataStore store = createGuideDataStore(sectionOrder, stepsPerSection);
        ConfigManager configManager = createConfigManager(startIndex);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        tracker.jumpToSection("NonExistentSection");

        assertEquals(startIndex, tracker.getCurrentStepIndex(),
            "jumpToSection with unknown section should not change the current index");
    }
}
