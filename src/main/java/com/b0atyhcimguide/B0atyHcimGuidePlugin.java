package com.b0atyhcimguide;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import com.google.inject.Provides;

/**
 * Main plugin class for the B0aty HCIM Guide overlay.
 * Orchestrates all components: guide data loading, step tracking,
 * overlays, world map markers, and input handling.
 */
@Slf4j
@PluginDescriptor(
    name = "B0aty HCIM Guide",
    description = "In-game overlay for B0aty's Hardcore Ironman Guide V3",
    tags = {"b0aty", "hcim", "ironman", "guide", "overlay"}
)
public class B0atyHcimGuidePlugin extends Plugin
{
    private static final String CONFIG_GROUP = "b0atyhcimguide";

    @Inject
    private Client client;

    @Inject
    private B0atyHcimGuideConfig config;

    @Inject
    private GuideDataStore guideDataStore;

    @Inject
    private StepTracker stepTracker;

    @Inject
    private GuideOverlay guideOverlay;

    @Inject
    private HighlightOverlay highlightOverlay;

    @Inject
    private MinimapOverlay minimapOverlay;

    @Inject
    private WorldMapPointManager worldMapPointManager;

    @Inject
    private MouseButtonInputListener mouseButtonInputListener;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private MouseManager mouseManager;

    @Provides
    B0atyHcimGuideConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(B0atyHcimGuideConfig.class);
    }

    @Override
    protected void startUp()
    {
        guideDataStore.load();
        stepTracker.initialize();

        overlayManager.add(guideOverlay);
        overlayManager.add(highlightOverlay);
        overlayManager.add(minimapOverlay);

        mouseManager.registerMouseListener(mouseButtonInputListener);

        log.info("B0aty HCIM Guide plugin started");
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(guideOverlay);
        overlayManager.remove(highlightOverlay);
        overlayManager.remove(minimapOverlay);

        mouseManager.unregisterMouseListener(mouseButtonInputListener);

        worldMapPointManager.shutdown();
        guideOverlay.shutdown();
        highlightOverlay.shutdown();

        log.info("B0aty HCIM Guide plugin stopped");
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!CONFIG_GROUP.equals(event.getGroup()))
        {
            return;
        }

        if ("currentStep".equals(event.getKey()))
        {
            // Reset progress: jump to the step specified in config
            try
            {
                int step = Integer.parseInt(event.getNewValue());
                stepTracker.jumpToStep(step);
            }
            catch (NumberFormatException e)
            {
                log.warn("Invalid step value in config change: {}", event.getNewValue());
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        // GameTick subscription ensures entity highlights are refreshed
        // as NPCs/objects spawn or despawn in the game world.
        // The actual rendering is handled by HighlightOverlay and MinimapOverlay
        // which read live entity state each frame.
    }
}
