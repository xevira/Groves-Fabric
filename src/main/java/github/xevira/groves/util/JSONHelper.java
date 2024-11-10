package github.xevira.groves.util;

import com.google.gson.*;
import github.xevira.groves.Groves;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static github.xevira.groves.Groves.GSON;

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

    public static String getString(JsonObject json, String key, String defaultStr)
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

        return defaultStr;
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

    public static boolean getBoolean(JsonObject json, String key, boolean defaultValue)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonPrimitive())
            {
                JsonPrimitive p = e.getAsJsonPrimitive();

                if (p.isBoolean())
                {
                    return p.getAsBoolean();
                }
            }
        }

        return defaultValue;
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

    public static int getInt(JsonObject json, String key, int defaultValue)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonPrimitive())
            {
                JsonPrimitive p = e.getAsJsonPrimitive();

                if (p.isNumber())
                {
                    return p.getAsInt();
                }
            }
        }

        return defaultValue;
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

    public static int getInt(JsonObject json, String key, int defaultValue, int minValue, int maxValue)
    {
        AtomicReference<Integer> value = new AtomicReference<>(defaultValue);

        getInt(json, key).ifPresent(aInteger -> value.set(MathHelper.clamp(aInteger, minValue, maxValue)));

        return value.get();
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

    public static long getLong(JsonObject json, String key, long defaultValue, long minValue, long maxValue)
    {
        AtomicReference<Long> value = new AtomicReference<>(defaultValue);

        getLong(json, key).ifPresent(aLong -> value.set(MathHelper.clamp(aLong, minValue, maxValue)));

        return value.get();
    }

    public static Optional<Float> getFloat(JsonObject json, String key)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonPrimitive())
            {
                JsonPrimitive p = e.getAsJsonPrimitive();

                if (p.isNumber())
                {
                    return Optional.of(p.getAsFloat());
                }
            }
        }

        return Optional.empty();
    }

    public static float getFloat(JsonObject json, String key, float defaultValue, float minValue, float maxValue)
    {
        AtomicReference<Float> value = new AtomicReference<>(defaultValue);

        getFloat(json, key).ifPresent(aFloat -> value.set(MathHelper.clamp(aFloat, minValue, maxValue)));

        return value.get();
    }

    public static Optional<Double> getDouble(JsonObject json, String key)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonPrimitive())
            {
                JsonPrimitive p = e.getAsJsonPrimitive();

                if (p.isNumber())
                {
                    return Optional.of(p.getAsDouble());
                }
            }
        }

        return Optional.empty();
    }


    public static double getDouble(JsonObject json, String key, double defaultValue, double minValue, double maxValue)
    {
        AtomicReference<Double> value = new AtomicReference<>(defaultValue);

        getDouble(json, key).ifPresent(aDouble -> value.set(MathHelper.clamp(aDouble, minValue, maxValue)));

        return value.get();
    }

    public static Optional<Integer> JsonToColor(JsonObject json, String key)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonPrimitive())
            {
                JsonPrimitive p = e.getAsJsonPrimitive();

                if (p.isString())
                {
                    String str = p.getAsString();

                    if (str.startsWith("#"))
                    {
                        try {
                            int color = Integer.parseInt(str.substring(1), 16);
                            return Optional.of(color);
                        } catch(NumberFormatException ignored) {
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    public static int JsonToColor(JsonObject json, String key, int defaultColor)
    {
        Optional<Integer> color = JsonToColor(json, key);

        return color.orElse(defaultColor);
    }

    public static JsonPrimitive ColorToJson(int color)
    {
        return new JsonPrimitive("#" + Integer.toHexString(color).toUpperCase());
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

    @Nullable
    public static JsonElement parseJsonFile(File file)
    {
        if (file != null && file.exists() && file.isFile() && file.canRead())
        {
            String fileName = file.getAbsolutePath();

            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))
            {
                return JsonParser.parseReader(reader);
            }
            catch (Exception e)
            {
                Groves.LOGGER.error("Failed to parse the JSON file '{}'", fileName, e);
            }
        }

        return null;
    }

    public static boolean writeJsonToFile(JsonObject root, File file)
    {
        File fileTmp = new File(file.getParentFile(), file.getName() + ".tmp");

        if (fileTmp.exists())
        {
            fileTmp = new File(file.getParentFile(), UUID.randomUUID() + ".tmp");
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileTmp), StandardCharsets.UTF_8))
        {
            writer.write(GSON.toJson(root));
            writer.close();

            if (file.exists() && file.isFile() && !file.delete())
            {
                Groves.LOGGER.warn("Failed to delete file '{}'", file.getAbsolutePath());
            }

            return fileTmp.renameTo(file);
        }
        catch (Exception e)
        {
            Groves.LOGGER.warn("Failed to write JSON data to file '{}'", fileTmp.getAbsolutePath(), e);
        }

        return false;
    }
}


