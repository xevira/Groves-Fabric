package github.xevira.groves.poi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.ServerConfig;
import github.xevira.groves.block.WindChimeBlock;
import github.xevira.groves.util.JSONHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import java.util.*;

public class WindChimes {
    private static final Map<RegistryKey<World>, Set<BlockPos>> CHIMES = new HashMap<>();

    public static void addChime(World world, BlockPos pos)
    {
        RegistryKey<World> key = world.getRegistryKey();
        Set<BlockPos> pos_set;
        if (!CHIMES.containsKey(key)) {
            pos_set = new HashSet<>();
            CHIMES.put(key, pos_set);
        } else {
            pos_set = CHIMES.get(key);
        }

        pos_set.add(pos);
    }

    public static void removeChime(World world, BlockPos pos)
    {
        RegistryKey<World> key = world.getRegistryKey();

        if (CHIMES.containsKey(key))
        {
            Set<BlockPos> pos_set = CHIMES.get(key);
            pos_set.remove(pos);
        }
    }

    public static boolean canSpawn(ServerWorld world, BlockPos pos)
    {
        RegistryKey<World> key = world.getRegistryKey();
        if (CHIMES.containsKey(key))
        {
            Set<BlockPos> set = CHIMES.get(key);

            BlockPos chimePos = set.stream().filter(p -> (p.getSquaredDistance(pos) <= ServerConfig.getWindChimeRangeSquare())).findFirst().orElse(null);

            if (chimePos != null)
            {
                Groves.LOGGER.info("Blocking spawn at {} by chime {}", pos, chimePos);
                WindChimeBlock.protect(world, chimePos);
                return false;
            }
        }

        return true;
    }

    public static void serializeServer(JsonObject json, MinecraftServer server)
    {
        JsonObject chimes = new JsonObject();

        for(RegistryKey<World> worldKey : CHIMES.keySet())
        {
            Set<BlockPos> pos_set = CHIMES.get(worldKey);

            JsonArray array = new JsonArray();

            for(BlockPos pos : pos_set) {
                array.add(JSONHelper.BlockPosToJson(pos));
            }

            chimes.add(worldKey.getValue().toString(), array);
        }

        json.add("chimes", chimes);
    }

    public static boolean deserializeServer(JsonObject json, MinecraftServer server)
    {
        CHIMES.clear();

        if (json.has("chimes"))
        {
            JsonElement element = json.get("chimes");

            if (element.isJsonObject())
            {
                Map<String, JsonElement> map = element.getAsJsonObject().asMap();

                for(String key : map.keySet())
                {
                    JsonElement element2 = map.get(key);
                    if (element2.isJsonArray()) {
                        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(key));

                        Set<BlockPos> set = new HashSet<>();

                        for(JsonElement item : element2.getAsJsonArray())
                        {
                            if (item.isJsonObject())
                            {
                                Optional<BlockPos> pos = JSONHelper.JsonToBlockPos(item.getAsJsonObject());

                                pos.ifPresent(set::add);
                            }
                        }

                        CHIMES.put(worldKey, set);
                    }
                }

                return true;
            }

            return false;
        }

        return true;
    }

}
