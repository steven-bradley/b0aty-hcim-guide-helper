package com.b0atyhcimguide;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.Color;

/**
 * Configuration interface for the B0aty HCIM Guide plugin.
 */
@ConfigGroup("b0atyhcimguide")
public interface B0atyHcimGuideConfig extends Config
{
    @ConfigItem(
        keyName = "highlightColor",
        name = "Highlight Color",
        description = "Color used for entity highlights and minimap indicators",
        position = 1
    )
    default Color highlightColor()
    {
        return Color.CYAN;
    }

    @ConfigItem(
        keyName = "showOverlay",
        name = "Show Overlay",
        description = "Toggle the guide overlay panel visibility",
        position = 2
    )
    default boolean showOverlay()
    {
        return true;
    }

    @ConfigItem(
        keyName = "enableHighlighting",
        name = "Enable Highlighting",
        description = "Toggle entity highlighting in the game world",
        position = 3
    )
    default boolean enableHighlighting()
    {
        return true;
    }

    @ConfigItem(
        keyName = "enableWorldMap",
        name = "Enable World Map Markers",
        description = "Toggle world map markers for step locations",
        position = 4
    )
    default boolean enableWorldMap()
    {
        return true;
    }

    @ConfigItem(
        keyName = "currentStep",
        name = "Current Step",
        description = "Current step index (persisted across sessions)",
        hidden = true
    )
    default int currentStep()
    {
        return 0;
    }

    @ConfigItem(
        keyName = "nextStepMouseButton",
        name = "Next Step Mouse Button",
        description = "Mouse button binding to advance to the next step",
        position = 5
    )
    default MouseButton nextStepMouseButton()
    {
        return MouseButton.NONE;
    }

    @ConfigItem(
        keyName = "prevStepMouseButton",
        name = "Previous Step Mouse Button",
        description = "Mouse button binding to return to the previous step",
        position = 6
    )
    default MouseButton prevStepMouseButton()
    {
        return MouseButton.NONE;
    }
}
