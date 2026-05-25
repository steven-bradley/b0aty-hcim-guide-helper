package com.b0atyhcimguide;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * References a specific game entity by type and game ID.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityReference {
    /**
     * The type of entity (NPC, Object, Ground Item, or Inventory Item).
     */
    private EntityType type;

    /**
     * The game ID for this entity (NPC ID, Object ID, or Item ID).
     */
    private int gameId;
}
