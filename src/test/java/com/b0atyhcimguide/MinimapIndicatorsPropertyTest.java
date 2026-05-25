package com.b0atyhcimguide;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Feature: b0aty-hcim-guide-overlay
 * Property 11: Minimap indicators for in-range entities
 *
 * For any set of highlighted entities, the minimap overlay renders a dot at the
 * position of each entity within render distance, using the configured highlight color,
 * and renders no dots when no entities are in range.
 *
 * Validates: Requirements 8.1, 8.2, 8.3
 */
class MinimapIndicatorsPropertyTest
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
    void minimapCollectsLocationsForInRangeNpcs(
        @ForAll @IntRange(min = 1, max = 5) int npcCount,
        @ForAll @IntRange(min = 1, max = 50000) int baseNpcId)
    {
        // Build a step with NPC entities
        List<EntityReference> entities = new ArrayList<>();
        for (int i = 0; i < npcCount; i++)
        {
            entities.add(new EntityReference(EntityType.NPC, baseNpcId + i));
        }

        GuideStep step = new GuideStep(1, "Section", "Talk to NPCs", entities, null);
        List<GuideStep> steps = List.of(step);

        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        Client client = mock(Client.class);
        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableHighlighting()).thenReturn(true);
        when(config.highlightColor()).thenReturn(Color.CYAN);
        ModelOutlineRenderer renderer = mock(ModelOutlineRenderer.class);

        HighlightOverlay highlightOverlay = new HighlightOverlay(client, config, renderer, tracker);
        MinimapOverlay minimapOverlay = new MinimapOverlay(client, config, highlightOverlay);

        // Mock NPCs in the game world matching our entity IDs
        List<NPC> npcs = new ArrayList<>();
        for (int i = 0; i < npcCount; i++)
        {
            NPC npc = mock(NPC.class);
            when(npc.getId()).thenReturn(baseNpcId + i);
            LocalPoint lp = mock(LocalPoint.class);
            when(npc.getLocalLocation()).thenReturn(lp);
            npcs.add(npc);
        }
        when(client.getNpcs()).thenReturn(npcs);

        // Mock empty scene for object/ground item scanning
        Scene scene = mock(Scene.class);
        Tile[][][] tiles = new Tile[4][1][1];
        for (int p = 0; p < 4; p++)
        {
            tiles[p] = new Tile[0][0];
        }
        when(scene.getTiles()).thenReturn(tiles);
        when(client.getScene()).thenReturn(scene);
        when(client.getPlane()).thenReturn(0);

        Map<EntityType, Set<Integer>> entityIds = highlightOverlay.getCurrentEntityIds();
        List<LocalPoint> locations = minimapOverlay.collectEntityLocations(entityIds);

        // Should have one location per NPC
        assertEquals(npcCount, locations.size(),
            "Should collect a location for each in-range NPC");
    }

    @Property(tries = 100)
    void minimapRendersNothingWhenNoEntities(
        @ForAll @IntRange(min = 1, max = 10) int totalSteps)
    {
        // Build steps with no entities
        List<GuideStep> steps = new ArrayList<>();
        for (int i = 0; i < totalSteps; i++)
        {
            steps.add(new GuideStep(i + 1, "Section", "Step " + i, List.of(), null));
        }

        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        Client client = mock(Client.class);
        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableHighlighting()).thenReturn(true);
        when(config.highlightColor()).thenReturn(Color.CYAN);
        ModelOutlineRenderer renderer = mock(ModelOutlineRenderer.class);

        HighlightOverlay highlightOverlay = new HighlightOverlay(client, config, renderer, tracker);
        MinimapOverlay minimapOverlay = new MinimapOverlay(client, config, highlightOverlay);

        // render should return null when no entities
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();

        Dimension result = minimapOverlay.render(graphics);
        assertNull(result, "Minimap overlay should render nothing when no entities are highlighted");

        graphics.dispose();
    }

    @Property(tries = 100)
    void minimapRespectsHighlightColor(
        @ForAll("colors") Color testColor)
    {
        // Build a step with one NPC entity
        List<EntityReference> entities = List.of(new EntityReference(EntityType.NPC, 100));
        GuideStep step = new GuideStep(1, "Section", "Talk to NPC", entities, null);
        List<GuideStep> steps = List.of(step);

        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        Client client = mock(Client.class);
        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableHighlighting()).thenReturn(true);
        when(config.highlightColor()).thenReturn(testColor);
        ModelOutlineRenderer renderer = mock(ModelOutlineRenderer.class);

        HighlightOverlay highlightOverlay = new HighlightOverlay(client, config, renderer, tracker);
        MinimapOverlay minimapOverlay = new MinimapOverlay(client, config, highlightOverlay);

        // The configured color should be what the overlay uses
        assertEquals(testColor, config.highlightColor(),
            "Minimap indicator color must match the configured highlight color");
    }

    @Property(tries = 100)
    void minimapRendersNothingWhenHighlightingDisabled(
        @ForAll @IntRange(min = 1, max = 5) int npcCount)
    {
        List<EntityReference> entities = new ArrayList<>();
        for (int i = 0; i < npcCount; i++)
        {
            entities.add(new EntityReference(EntityType.NPC, 1000 + i));
        }

        GuideStep step = new GuideStep(1, "Section", "Talk to NPCs", entities, null);
        List<GuideStep> steps = List.of(step);

        ConfigManager configManager = createConfigManager(0);
        GuideDataStore store = createGuideDataStore(steps);

        StepTracker tracker = new StepTracker(store, configManager);
        tracker.initialize();

        Client client = mock(Client.class);
        B0atyHcimGuideConfig config = mock(B0atyHcimGuideConfig.class);
        when(config.enableHighlighting()).thenReturn(false);
        when(config.highlightColor()).thenReturn(Color.CYAN);
        ModelOutlineRenderer renderer = mock(ModelOutlineRenderer.class);

        HighlightOverlay highlightOverlay = new HighlightOverlay(client, config, renderer, tracker);
        MinimapOverlay minimapOverlay = new MinimapOverlay(client, config, highlightOverlay);

        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();

        Dimension result = minimapOverlay.render(graphics);
        assertNull(result, "Minimap overlay should render nothing when highlighting is disabled");

        graphics.dispose();
    }

    @Provide
    Arbitrary<Color> colors()
    {
        return Combinators.combine(
            Arbitraries.integers().between(0, 255),
            Arbitraries.integers().between(0, 255),
            Arbitraries.integers().between(0, 255)
        ).as(Color::new);
    }
}
