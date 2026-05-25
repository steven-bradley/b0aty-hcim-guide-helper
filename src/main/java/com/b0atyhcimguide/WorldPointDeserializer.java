package com.b0atyhcimguide;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.runelite.api.coords.WorldPoint;

/**
 * Custom Gson deserializer for RuneLite's WorldPoint class.
 * Expects JSON in the format: {"x": 3094, "y": 3107, "plane": 0}
 */
public class WorldPointDeserializer implements JsonDeserializer<WorldPoint> {
    @Override
    public WorldPoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        int x = obj.get("x").getAsInt();
        int y = obj.get("y").getAsInt();
        int plane = obj.has("plane") ? obj.get("plane").getAsInt() : 0;
        return new WorldPoint(x, y, plane);
    }
}
