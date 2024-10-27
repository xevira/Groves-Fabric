package github.xevira.groves.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.groves.Groves;
import net.minecraft.util.math.BlockPos;
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

    private static Optional<Integer> getInt(JsonArray array, int index)
    {
        if (index < 0 || index >= array.size()) return Optional.empty();

        JsonElement e = array.get(index);
        if (e.isJsonPrimitive())
        {
            JsonPrimitive p = e.getAsJsonPrimitive();

            if (p.isNumber())
            {
                return Optional.of(p.getAsInt());
            }
        }

        return Optional.empty();
    }

    private static final int[] EMPTY_INT_ARRAY = new int[0];
    public static int[] getIntArray(JsonObject json, String key, int length)
    {
        if (!json.has(key)) return EMPTY_INT_ARRAY;

        JsonElement element = json.get(key);
        if (!element.isJsonArray()) return EMPTY_INT_ARRAY;

        JsonArray array = element.getAsJsonArray();
        if (array.size() != length) return EMPTY_INT_ARRAY;

        int[] data = new int[length];

        for(int i = 0; i < length; i++)
        {
            Optional<Integer> value = getInt(array, i);

            if (value.isPresent())
                data[i] = value.get();
            else
                return EMPTY_INT_ARRAY;
        }

        return data;
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

    public static JsonObject BlockPosToJson(BlockPos pos)
    {
        JsonObject o = new JsonObject();
        o.add("x", new JsonPrimitive(pos.getX()));
        o.add("y", new JsonPrimitive(pos.getY()));
        o.add("z", new JsonPrimitive(pos.getZ()));
        return o;
    }

    public static Optional<BlockPos> JsonToBlockPos(JsonObject json, String key)
    {
        if (!json.has(key)) return Optional.empty();

        JsonElement element = json.get(key);
        if (!element.isJsonObject()) return Optional.empty();

        return JsonToBlockPos(element.getAsJsonObject());
    }

    public static Optional<BlockPos> JsonToBlockPos(JsonObject json)
    {
        Optional<Integer> x = getInt(json, "x");
        Optional<Integer> y = getInt(json, "y");
        Optional<Integer> z = getInt(json, "z");

        if (x.isPresent() && y.isPresent() && z.isPresent())
            return Optional.of(new BlockPos(x.get(), y.get(), z.get()));

        return Optional.empty();
    }


}
