package com.b0atyhcimguide;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Feature: b0aty-hcim-guide-overlay
 * Property 6: Highlight set matches current step entities
 *
 * For any current step, the set of highlighted entity IDs (partitioned by type)
 * must exactly equal the set of entity references defined in that step's data.
 * No entities from any other step are highlighted.
 *
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5
 */
class HighlightSetMatchesStepPropertyTest
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
    void highlightSetMatchesCurrentStepEntities(
        @ForAll @IntRange(min = 1, max = 20) int totalSteps,
        @ForAll @IntRange(min = 0, max = 19) int stepIndex,
        @ForAll("entityLists") List<EntityReference> entities)
    {
        Assume.that(stepIndex < totalSteps);

        // Build steps with the target step containing the generated entities
        List<GuideStep> steps = new ArrayList<>();
        for (int i = 0; i < totalSteps; i++)
        {
            if (i == stepIndex)
            {
                steps.add(new GuideStep(i + 1, "Section", "Instruction " + i, entities, null));
            }
            else
            {
                // Other steps have different entities that should NOT appear in highlights
                List<EntityReference> otherEntities = List.of(
                    new EntityReference(EntityType.NPC, 99990 + i));
                steps.add(new GuideStep(i + 1, "Section", "Other " + i, otherEntities, null));
            }
        }

        ConfigManager configManager = createConfigManager(stepIndex);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        Client client = mock(Client.class);
        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableHighlighting()).thenReturn(true);
        ModelOutlineRenderer renderer = mock(ModelOutlineRenderer.class);

        HighlightOverlay overlay = new HighlightOverlay(client, config, renderer, tracker);

        // Build expected entity ID sets partitioned by type
        Map<EntityType, Set<Integer>> expected = new EnumMap<>(EntityType.class);
        for (EntityType type : EntityType.values())
        {
            expected.put(type, new HashSet<>());
        }
        for (EntityReference ref : entities)
        {
            if (ref.getType() != null)
            {
                expected.get(ref.getType()).add(ref.getGameId());
            }
        }

        // Verify the overlay's internal entity ID sets match exactly
        Map<EntityType, Set<Integer>> actual = overlay.getCurrentEntityIds();

        for (EntityType type : EntityType.values())
        {
            assertEquals(expected.get(type), actual.get(type),
                "Entity IDs for type " + type + " must match current step's references");
        }
    }

    @Property(tries = 100)
    void highlightSetUpdatesOnStepChange(
        @ForAll @IntRange(min = 2, max = 10) int totalSteps,
        @ForAll @IntRange(min = 0, max = 8) int startIndex,
        @ForAll("entityLists") List<EntityReference> entitiesA,
        @ForAll("entityLists") List<EntityReference> entitiesB)
    {
        Assume.that(startIndex < totalSteps - 1);

        int nextIndex = startIndex + 1;

        // Build steps with different entities at startIndex and nextIndex
        List<GuideStep> steps = new ArrayList<>();
        for (int i = 0; i < totalSteps; i++)
        {
            if (i == startIndex)
            {
                steps.add(new GuideStep(i + 1, "Section", "Step A", entitiesA, null));
            }
            else if (i == nextIndex)
            {
                steps.add(new GuideStep(i + 1, "Section", "Step B", entitiesB, null));
            }
            else
            {
                steps.add(new GuideStep(i + 1, "Section", "Other " + i, List.of(), null));
            }
        }

        ConfigManager configManager = createConfigManager(startIndex);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        Client client = mock(Client.class);
        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableHighlighting()).thenReturn(true);
        ModelOutlineRenderer renderer = mock(ModelOutlineRenderer.class);

        HighlightOverlay overlay = new HighlightOverlay(client, config, renderer, tracker);

        // Navigate to next step
        tracker.nextStep();

        // Build expected for step B
        Map<EntityType, Set<Integer>> expected = new EnumMap<>(EntityType.class);
        for (EntityType type : EntityType.values())
        {
            expected.put(type, new HashSet<>());
        }
        for (EntityReference ref : entitiesB)
        {
            if (ref.getType() != null)
            {
                expected.get(ref.getType()).add(ref.getGameId());
            }
        }

        Map<EntityType, Set<Integer>> actual = overlay.getCurrentEntityIds();

        for (EntityType type : EntityType.values())
        {
            assertEquals(expected.get(type), actual.get(type),
                "After step change, entity IDs for type " + type + " must match new step's references");
        }
    }

    @Provide
    Arbitrary<List<EntityReference>> entityLists()
    {
        Arbitrary<EntityReference> entityRef = Combinators.combine(
            Arbitraries.of(EntityType.values()),
            Arbitraries.integers().between(1, 50000)
        ).as(EntityReference::new);

        return entityRef.list().ofMinSize(0).ofMaxSize(5);
    }
}
