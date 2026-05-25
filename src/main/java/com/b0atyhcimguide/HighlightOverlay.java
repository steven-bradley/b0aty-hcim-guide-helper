package com.b0atyhcimguide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

/**
 * Overlay that highlights game entities (NPCs, objects, ground items, inventory items)
 * relevant to the current guide step.
 */
@Singleton
public class HighlightOverlay extends Overlay implements StepChangeListener
{
    private static final int OUTLINE_WIDTH = 4;
    private static final int OUTLINE_FEATHER = 3;
    private static final int INVENTORY_COLUMNS = 4;
    private static final int INVENTORY_ITEM_WIDTH = 36;
    private static final int INVENTORY_ITEM_HEIGHT = 32;

    private final Client client;
    private final B0atyHcimGuideConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final StepTracker stepTracker;

    private final Map<EntityType, Set<Integer>> currentEntityIds = new EnumMap<>(EntityType.class);

    @Inject
    public HighlightOverlay(Client client, B0atyHcimGuideConfig config,
                            ModelOutlineRenderer modelOutlineRenderer, StepTracker stepTracker)
    {
        this.client = client;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.stepTracker = stepTracker;

        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.DYNAMIC);

        stepTracker.addStepChangeListener(this);
        updateEntityIds(stepTracker.getCurrentStep());
    }

    @Override
    public void onStepChanged(GuideStep newStep, int newIndex)
    {
        updateEntityIds(newStep);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.enableHighlighting())
        {
            return null;
        }

        Color highlightColor = config.highlightColor();

        highlightNpcs(highlightColor);
        highlightObjects(graphics, highlightColor);
        highlightGroundItems(graphics, highlightColor);
        highlightInventoryItems(graphics, highlightColor);

        return null;
    }

    /**
     * Returns an unmodifiable view of the current entity IDs partitioned by type.
     */
    public Map<EntityType, Set<Integer>> getCurrentEntityIds()
    {
        return Collections.unmodifiableMap(currentEntityIds);
    }

    /**
     * Cleans up listener registration.
     */
    public void shutdown()
    {
        stepTracker.removeStepChangeListener(this);
    }

    private void updateEntityIds(GuideStep step)
    {
        currentEntityIds.clear();
        for (EntityType type : EntityType.values())
        {
            currentEntityIds.put(type, new HashSet<>());
        }

        if (step == null || step.getEntities() == null)
        {
            return;
        }

        for (EntityReference ref : step.getEntities())
        {
            if (ref.getType() != null)
            {
                currentEntityIds.get(ref.getType()).add(ref.getGameId());
            }
        }
    }

    private void highlightNpcs(Color color)
    {
        Set<Integer> npcIds = currentEntityIds.get(EntityType.NPC);
        if (npcIds == null || npcIds.isEmpty())
        {
            return;
        }

        List<NPC> npcs = client.getNpcs();
        for (NPC npc : npcs)
        {
            if (npc.getId() != -1 && npcIds.contains(npc.getId()))
            {
                modelOutlineRenderer.drawOutline(npc, OUTLINE_WIDTH, color, OUTLINE_FEATHER);
            }
        }
    }

    private void highlightObjects(Graphics2D graphics, Color color)
    {
        Set<Integer> objectIds = currentEntityIds.get(EntityType.OBJECT);
        if (objectIds == null || objectIds.isEmpty())
        {
            return;
        }

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

                for (TileObject obj : getObjectsOnTile(tile))
                {
                    if (obj != null && objectIds.contains(obj.getId()))
                    {
                        modelOutlineRenderer.drawOutline(obj, OUTLINE_WIDTH, color, OUTLINE_FEATHER);
                    }
                }
            }
        }
    }

    private void highlightGroundItems(Graphics2D graphics, Color color)
    {
        Set<Integer> groundItemIds = currentEntityIds.get(EntityType.GROUND_ITEM);
        if (groundItemIds == null || groundItemIds.isEmpty())
        {
            return;
        }

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

                List<TileItem> groundItems = tile.getGroundItems();
                if (groundItems == null)
                {
                    continue;
                }

                for (TileItem item : groundItems)
                {
                    if (item != null && groundItemIds.contains(item.getId()))
                    {
                        renderTileHighlight(graphics, tile, color);
                        break;
                    }
                }
            }
        }
    }

    private void highlightInventoryItems(Graphics2D graphics, Color color)
    {
        Set<Integer> inventoryItemIds = currentEntityIds.get(EntityType.INVENTORY_ITEM);
        if (inventoryItemIds == null || inventoryItemIds.isEmpty())
        {
            return;
        }

        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget == null || inventoryWidget.isHidden())
        {
            return;
        }

        Widget[] children = inventoryWidget.getDynamicChildren();
        if (children == null)
        {
            return;
        }

        for (Widget child : children)
        {
            if (child != null && inventoryItemIds.contains(child.getItemId()))
            {
                Rectangle bounds = child.getBounds();
                if (bounds != null)
                {
                    graphics.setColor(color);
                    graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
                }
            }
        }
    }

    private void renderTileHighlight(Graphics2D graphics, Tile tile, Color color)
    {
        LocalPoint lp = tile.getLocalLocation();
        if (lp == null)
        {
            return;
        }

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly == null)
        {
            return;
        }

        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
        graphics.fillPolygon(poly);
        graphics.setColor(color);
        graphics.drawPolygon(poly);
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
