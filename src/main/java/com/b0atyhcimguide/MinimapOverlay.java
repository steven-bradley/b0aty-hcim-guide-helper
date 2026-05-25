package com.b0atyhcimguide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

/**
 * Overlay that renders colored dots on the minimap at positions of highlighted
 * entities within render distance.
 */
@Singleton
public class MinimapOverlay extends Overlay
{
    private static final int DOT_RADIUS = 4;

    private final Client client;
    private final B0atyHcimGuideConfig config;
    private final HighlightOverlay highlightOverlay;

    @Inject
    public MinimapOverlay(Client client, B0atyHcimGuideConfig config, HighlightOverlay highlightOverlay)
    {
        this.client = client;
        this.config = config;
        this.highlightOverlay = highlightOverlay;

        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.enableHighlighting())
        {
            return null;
        }

        Map<EntityType, Set<Integer>> entityIds = highlightOverlay.getCurrentEntityIds();
        if (entityIds == null || allEmpty(entityIds))
        {
            return null;
        }

        Color color = config.highlightColor();
        List<LocalPoint> entityLocations = collectEntityLocations(entityIds);

        for (LocalPoint localPoint : entityLocations)
        {
            renderMinimapDot(graphics, localPoint, color);
        }

        return null;
    }

    /**
     * Collects local points of all in-range entities matching the current highlight set.
     * Exposed for testing.
     */
    List<LocalPoint> collectEntityLocations(Map<EntityType, Set<Integer>> entityIds)
    {
        List<LocalPoint> locations = new ArrayList<>();

        // Collect NPC locations
        Set<Integer> npcIds = entityIds.get(EntityType.NPC);
        if (npcIds != null && !npcIds.isEmpty())
        {
            for (NPC npc : client.getNpcs())
            {
                if (npc != null && npcIds.contains(npc.getId()))
                {
                    LocalPoint lp = npc.getLocalLocation();
                    if (lp != null)
                    {
                        locations.add(lp);
                    }
                }
            }
        }

        // Collect object and ground item locations from scene tiles
        Set<Integer> objectIds = entityIds.get(EntityType.OBJECT);
        Set<Integer> groundItemIds = entityIds.get(EntityType.GROUND_ITEM);
        boolean hasObjects = objectIds != null && !objectIds.isEmpty();
        boolean hasGroundItems = groundItemIds != null && !groundItemIds.isEmpty();

        if (hasObjects || hasGroundItems)
        {
            Scene scene = client.getScene();
            Tile[][][] tiles = scene.getTiles();
            int plane = client.getPlane();

            for (int x = 0; x < tiles[plane].length; x++)
            {
                for (int y = 0; y < tiles[plane][x].length; y++)
                {
                    Tile tile = tiles[plane][x][y];
                    if (tile == null)
                    {
                        continue;
                    }

                    if (hasObjects)
                    {
                        for (TileObject obj : getObjectsOnTile(tile))
                        {
                            if (obj != null && objectIds.contains(obj.getId()))
                            {
                                LocalPoint lp = tile.getLocalLocation();
                                if (lp != null)
                                {
                                    locations.add(lp);
                                }
                                break;
                            }
                        }
                    }

                    if (hasGroundItems)
                    {
                        List<TileItem> groundItems = tile.getGroundItems();
                        if (groundItems != null)
                        {
                            for (TileItem item : groundItems)
                            {
                                if (item != null && groundItemIds.contains(item.getId()))
                                {
                                    LocalPoint lp = tile.getLocalLocation();
                                    if (lp != null)
                                    {
                                        locations.add(lp);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return locations;
    }

    private void renderMinimapDot(Graphics2D graphics, LocalPoint localPoint, Color color)
    {
        Point minimapPoint = Perspective.localToMinimap(client, localPoint);
        if (minimapPoint == null)
        {
            return;
        }

        graphics.setColor(color);
        graphics.fillOval(
            minimapPoint.getX() - DOT_RADIUS / 2,
            minimapPoint.getY() - DOT_RADIUS / 2,
            DOT_RADIUS,
            DOT_RADIUS
        );
    }

    private boolean allEmpty(Map<EntityType, Set<Integer>> entityIds)
    {
        for (Set<Integer> ids : entityIds.values())
        {
            if (ids != null && !ids.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    private TileObject[] getObjectsOnTile(Tile tile)
    {
        return new TileObject[]
        {
            tile.getWallObject(),
            tile.getDecorativeObject(),
            tile.getGroundObject(),
            tile.getGameObjects() != null && tile.getGameObjects().length > 0
                ? tile.getGameObjects()[0] : null
        };
    }
}
