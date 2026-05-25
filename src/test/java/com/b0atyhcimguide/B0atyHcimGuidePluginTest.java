package com.b0atyhcimguide;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.MouseManager;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

/**
 * Unit tests for B0atyHcimGuidePlugin lifecycle and event handling.
 */
class B0atyHcimGuidePluginTest
{
    @Mock private GuideDataStore guideDataStore;
    @Mock private StepTracker stepTracker;
    @Mock private GuideOverlay guideOverlay;
    @Mock private HighlightOverlay highlightOverlay;
    @Mock private MinimapOverlay minimapOverlay;
    @Mock private WorldMapPointManager worldMapPointManager;
    @Mock private MouseButtonInputListener mouseButtonInputListener;
    @Mock private OverlayManager overlayManager;
    @Mock private MouseManager mouseManager;

    private B0atyHcimGuidePlugin plugin;

    @BeforeEach
    void setUp() throws Exception
    {
        MockitoAnnotations.openMocks(this);
        plugin = new B0atyHcimGuidePlugin();

        injectField("guideDataStore", guideDataStore);
        injectField("stepTracker", stepTracker);
        injectField("guideOverlay", guideOverlay);
        injectField("highlightOverlay", highlightOverlay);
        injectField("minimapOverlay", minimapOverlay);
        injectField("worldMapPointManager", worldMapPointManager);
        injectField("mouseButtonInputListener", mouseButtonInputListener);
        injectField("overlayManager", overlayManager);
        injectField("mouseManager", mouseManager);
    }

    @Test
    void startUpRegistersOverlaysAndInputListener() throws Exception
    {
        plugin.startUp();

        verify(guideDataStore).load();
        verify(stepTracker).initialize();
        verify(overlayManager).add(guideOverlay);
        verify(overlayManager).add(highlightOverlay);
        verify(overlayManager).add(minimapOverlay);
        verify(mouseManager).registerMouseListener(mouseButtonInputListener);
    }

    @Test
    void shutDownUnregistersOverlaysAndClearsState() throws Exception
    {
        plugin.shutDown();

        verify(overlayManager).remove(guideOverlay);
        verify(overlayManager).remove(highlightOverlay);
        verify(overlayManager).remove(minimapOverlay);
        verify(mouseManager).unregisterMouseListener(mouseButtonInputListener);
        verify(worldMapPointManager).shutdown();
        verify(guideOverlay).shutdown();
        verify(highlightOverlay).shutdown();
    }

    @Test
    void onConfigChangedForCurrentStepTriggersJumpToStep()
    {
        ConfigChanged event = new ConfigChanged();
        event.setGroup("b0atyhcimguide");
        event.setKey("currentStep");
        event.setNewValue("5");

        plugin.onConfigChanged(event);

        verify(stepTracker).jumpToStep(5);
    }

    @Test
    void onConfigChangedIgnoresOtherGroups()
    {
        ConfigChanged event = new ConfigChanged();
        event.setGroup("otherPlugin");
        event.setKey("currentStep");
        event.setNewValue("5");

        plugin.onConfigChanged(event);

        verifyNoInteractions(stepTracker);
    }

    @Test
    void onConfigChangedIgnoresOtherKeys()
    {
        ConfigChanged event = new ConfigChanged();
        event.setGroup("b0atyhcimguide");
        event.setKey("highlightColor");
        event.setNewValue("#FF0000");

        plugin.onConfigChanged(event);

        verify(stepTracker, never()).jumpToStep(anyInt());
    }

    @Test
    void onConfigChangedHandlesInvalidStepValue()
    {
        ConfigChanged event = new ConfigChanged();
        event.setGroup("b0atyhcimguide");
        event.setKey("currentStep");
        event.setNewValue("notanumber");

        // Should not throw
        plugin.onConfigChanged(event);

        verify(stepTracker, never()).jumpToStep(anyInt());
    }

    private void injectField(String fieldName, Object value) throws Exception
    {
        Field field = B0atyHcimGuidePlugin.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(plugin, value);
    }
}
