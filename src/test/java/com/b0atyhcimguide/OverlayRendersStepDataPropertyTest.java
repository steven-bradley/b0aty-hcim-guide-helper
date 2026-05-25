package com.b0atyhcimguide;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Feature: b0aty-hcim-guide-overlay
 * Property 2: Overlay renders current step data
 *
 * For any guide step that is the current step, the overlay render output
 * must contain the step's step number, section name, and instruction text.
 *
 * Validates: Requirements 2.1, 2.3, 7.3
 */
class OverlayRendersStepDataPropertyTest
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

    private static String getField(Object obj, String fieldName) throws Exception
    {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(obj);
        return value != null ? value.toString() : null;
    }

    @Property(tries = 100)
    void overlayRendersCurrentStepData(
        @ForAll @IntRange(min = 1, max = 50) int totalSteps,
        @ForAll @IntRange(min = 0, max = 49) int stepIndex,
        @ForAll("sectionNames") String section,
        @ForAll("instructions") String instruction)
    {
        Assume.that(stepIndex < totalSteps);
        Assume.that(!section.isEmpty());
        Assume.that(!instruction.isEmpty());

        // Build steps list with the target step at stepIndex
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
        B0atyHcimGuideConfig config = createConfig(true);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        GuideOverlay overlay = new GuideOverlay(config, tracker, store);

        // Disable automatic clearing of children after render so we can inspect them
        overlay.setClearChildren(false);

        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();

        overlay.render(graphics);

        // Inspect rendered components
        List<LayoutableRenderableEntity> children = overlay.getPanelComponent().getChildren();

        boolean foundSection = false;
        boolean foundStepNumber = false;
        boolean foundInstruction = false;

        String expectedStepDisplay = (stepIndex + 1) + " / " + totalSteps;

        try
        {
            for (LayoutableRenderableEntity component : children)
            {
                if (component instanceof TitleComponent)
                {
                    String text = getField(component, "text");
                    if (section.equals(text))
                    {
                        foundSection = true;
                    }
                }
                else if (component instanceof LineComponent)
                {
                    String left = getField(component, "left");
                    String right = getField(component, "right");

                    if (right != null && right.equals(expectedStepDisplay))
                    {
                        foundStepNumber = true;
                    }
                    if (left != null && left.equals(instruction))
                    {
                        foundInstruction = true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to read component fields via reflection", e);
        }

        assertTrue(foundSection,
            "Overlay must render section name '" + section + "' as title");
        assertTrue(foundStepNumber,
            "Overlay must render step number display '" + expectedStepDisplay + "'");
        assertTrue(foundInstruction,
            "Overlay must render instruction text '" + instruction + "'");

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
