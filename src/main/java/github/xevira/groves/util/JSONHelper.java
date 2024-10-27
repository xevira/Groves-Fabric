package github.xevira.groves.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.groves.Groves;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class JSONHelper {
    public static String getString(JsonObject json, String key)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonPrimitive())
            {
                JsonPrimitive p = e.getAsJsonPrimitive();

                if (p.isString())
                {
                    return p.getAsString();
                }
            }
        }

        return null;
    }

    public static Optional<JsonObject> getObject(JsonObject json, String key)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonObject())
            {
                return Optional.of(e.getAsJsonObject());
            }
        }

        return Optional.empty();

    }

    public static Optional<UUID> getUUID(JsonObject json, String key)
    {
        String uuidString = getString(json, key);

        if (uuidString == null) return Optional.empty();

        try {
            UUID uuid = UUID.fromString(uuidString);

            return Optional.of(uuid);
        } catch(IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public static Optional<Boolean> getBoolean(JsonObject json, String key)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonPrimitive())
            {
                JsonPrimitive p = e.getAsJsonPrimitive();

                if (p.isBoolean())
                {
                    return Optional.of(p.getAsBoolean());
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<Integer> getInt(JsonObject json, String key)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonPrimitive())
            {
                JsonPrimitive p = e.getAsJsonPrimitive();

                if (p.isNumber())
                {
                    return Optional.of(p.getAsInt());
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<Long> getLong(JsonObject json, String key)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonPrimitive())
            {
                JsonPrimitive p = e.getAsJsonPrimitive();

                if (p.isNumber())
                {
                    return Optional.of(p.getAsLong());
                }
            }
        }

        return Optional.empty();
    }

    public static JsonObject ChunkPosToJson(ChunkPos pos)
    {
        JsonObject o = new JsonObject();
        o.add("x", new JsonPrimitive(pos.x));
        o.add("z", new JsonPrimitive(pos.z));
        return o;
    }

    public static Optional<ChunkPos> JsonToChunkPos(JsonObject json, String key)
    {
        if (!json.has(key)) return Optional.empty();

        JsonElement element = json.get(key);
        if (!element.isJsonObject()) return Optional.empty();

        return JsonToChunkPos(element.getAsJsonObject());
    }

    public static Optional<ChunkPos> JsonToChunkPos(JsonObject json)
    {
        Optional<Integer> x = getInt(json, "x");
        Optional<Integer> z = getInt(json, "z");

        if (x.isPresent() && z.isPresent())
            return Optional.of(new ChunkPos(x.get(), z.get()));

        return Optional.empty();
    }

}
