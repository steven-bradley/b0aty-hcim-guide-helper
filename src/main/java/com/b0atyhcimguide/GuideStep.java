package com.b0atyhcimguide;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.runelite.api.coords.WorldPoint;

/**
 * Represents a single step in the B0aty HCIM Guide V3.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuideStep {
    /**
     * The sequential step number within the guide.
     */
    private int stepNumber;

    /**
     * The section name this step belongs to (e.g., "Tutorial Island").
     */
    private String section;

    /**
     * The instruction text describing what the player should do.
     */
    private String instruction;

    /**
     * The list of game entities relevant to this step.
     */
    private List<EntityReference> entities;

    /**
     * The world location for this step's map marker. May be null if no location is relevant.
     */
    private WorldPoint location;
}
