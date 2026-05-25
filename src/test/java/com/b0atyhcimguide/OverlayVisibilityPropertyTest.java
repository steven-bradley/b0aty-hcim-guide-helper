package com.b0atyhcimguide;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.runelite.client.config.ConfigManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Feature: b0aty-hcim-guide-overlay
 * Property 3: Overlay visibility respects configuration
 *
 * For any configuration state, the overlay is rendered if and only if
 * showOverlay is true.
 *
 * Validates: Requirements 2.5
 */
class OverlayVisibilityPropertyTest
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

    private B0atyHcimGuideConfig createConfig(boolean showOverlay)
    {
        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.showOverlay()).thenReturn(showOverlay);
        return config;
    }

    @Property(tries = 100)
    void overlayRendersOnlyWhenShowOverlayIsTrue(
        @ForAll boolean showOverlay,
        @ForAll @IntRange(min = 1, max = 20) int totalSteps,
        @ForAll @IntRange(min = 0, max = 19) int stepIndex,
        @ForAll("sectionNames") String section,
        @ForAll("instructions") String instruction)
    {
        Assume.that(stepIndex < totalSteps);
        Assume.that(!section.isEmpty());
        Assume.that(!instruction.isEmpty());

        List<GuideStep> steps = new ArrayList<>();
        for (int i = 0; i < totalSteps; i++)
        {
            if (i == stepIndex)
            {
                steps.add(new GuideStep(i + 1, section, instruction, List.of(), null));
            }
            else
            {
                steps.add(new GuideStep(i + 1, "Other Section", "Other instruction " + i, List.of(), null));
            }
        }

        ConfigManager configManager = createConfigManager(stepIndex);
        GuideDataStore store = createGuideDataStore(steps);
        B0atyHcimGuideConfig config = createConfig(showOverlay);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        GuideOverlay overlay = new GuideOverlay(config, tracker, store);

        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();

        Dimension result = overlay.render(graphics);

        if (showOverlay)
        {
            assertNotNull(result,
                "Overlay must render (return non-null Dimension) when showOverlay is true");
        }
        else
        {
            assertNull(result,
                "Overlay must not render (return null) when showOverlay is false");
        }

        graphics.dispose();
    }

    @Provide
    Arbitrary<String> sectionNames()
    {
        return Arbitraries.of(
            "Tutorial Island", "Lumbridge", "Varrock", "Falador",
            "Barbarian Village", "Al Kharid", "Draynor", "Edgeville",
            "Wilderness", "Morytania", "Karamja", "Ardougne"
        );
    }

    @Provide
    Arbitrary<String> instructions()
    {
        return Arbitraries.of(
            "Talk to the Gielinor Guide",
            "Pick up the bronze axe",
            "Chop a tree",
            "Light the logs",
            "Walk to the fishing spot",
            "Catch some shrimp",
            "Cook the shrimp on the fire",
            "Open the gate to the next area",
            "Mine a copper ore",
            "Smith a bronze dagger",
            "Kill the giant rat",
            "Complete the prayer tutorial"
        );
    }
}
