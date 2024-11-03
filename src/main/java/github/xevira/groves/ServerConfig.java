package github.xevira.groves;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.groves.sanctuary.GroveAbilities;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.util.JSONHelper;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServerConfig {
    private static final String CONFIG_PATH = "config/" + Groves.MOD_ID + "-server.json";
    private static final String BACKUP_PATH = "config/" + Groves.MOD_ID + "-server.json.bak";

    private static ServerConfig currentConfig;

    private final Map<Integer, Boolean> allowAbility = new HashMap<>();

    private float solarRepairBaseChance;
    private float solarRepairExtraChance;

    private long costClaimChunkBase;

    public static void onServerLoad(MinecraftServer server) {
        currentConfig = readConfig(server);
    }

    public static void onServerSave(MinecraftServer server) {
        if (currentConfig == null)
            currentConfig = new ServerConfig();

        writeConfig(currentConfig, server);
    }

    public ServerConfig()
    {
        for(GroveAbility ability : GroveAbilities.ABILITIES.values())
        {
            this.allowAbility.put(ability.getId(), ability.getDefaultAllow());
        }

        this.solarRepairBaseChance = 0.05f;
        this.solarRepairExtraChance = 0.025f;

        this.costClaimChunkBase = 50000L;
    }

    public boolean isAbilityAllowed(int id)
    {
        if (allowAbility.containsKey(id))
            return allowAbility.get(id);

        return false;
    }

    private static ServerConfig readConfig(MinecraftServer server) {
        Path configPath = server.getPath(CONFIG_PATH);
        try {
            var config = new ServerConfig();
            if (Files.notExists(configPath)) {
                writeConfig(config, server);
                return config;
            }

            String jsonStr = Files.readString(configPath);
            JsonObject json = Groves.GSON.fromJson(jsonStr, JsonObject.class);
            config.deserialize(json);
            return config;
        } catch (IOException exception) {
            Groves.LOGGER.error("Failed to read config file!", exception);

            // make a backup of the config file
            backupConfig(server);

            var config = new ServerConfig();
            writeConfig(config, server);
            return config;
        }
    }

    private static void writeConfig(ServerConfig config, MinecraftServer server) {
        Path configPath = server.getPath(CONFIG_PATH);
        try {
            Files.createDirectories(configPath.getParent());
            JsonObject json = config.serialize();
            Files.writeString(configPath, Groves.GSON.toJson(json));
        } catch (IOException exception) {
            Groves.LOGGER.error("Failed to write config file!", exception);
        }
    }

    private static void backupConfig(MinecraftServer server) {
        Path configPath = server.getPath(CONFIG_PATH);
        if (Files.notExists(configPath))
            return;

        Path backupPath = server.getPath(BACKUP_PATH);
        try {
            Files.createDirectories(backupPath.getParent());
            Files.move(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            Groves.LOGGER.error("Failed to backup config file!", exception);
        }
    }

    private JsonObject serialize() {
        JsonObject json = new JsonObject();

        JsonObject allow = new JsonObject();

        for(int id : allowAbility.keySet())
        {
            boolean allowed = allowAbility.get(id);

            String name = GroveAbilities.getNameById(id);
            if (name != null)
            {
                allow.add(name, new JsonPrimitive(allowed));
            }
        }

        json.add("allow", allow);

        json.add("solarRepairBaseChance", new JsonPrimitive(this.solarRepairBaseChance));
        json.add("solarRepairExtraChance", new JsonPrimitive(this.solarRepairExtraChance));

        return json;
    }

    private void deserialize(JsonObject json) {

        Optional<JsonObject> allow = JSONHelper.getObject(json, "allow");

        allowAbility.clear();

        if (allow.isPresent())
        {
            JsonObject allowObj = allow.get();

            Map<String, JsonElement> allowMap = allowObj.asMap();

            for(String key : allowMap.keySet())
            {
                int id = GroveAbilities.getIdByName(key);

                if (id < 0) continue;

                JsonElement element = allowMap.get(key);

                if (element.isJsonPrimitive())
                {
                    JsonPrimitive p = element.getAsJsonPrimitive();

                    if (p.isBoolean())
                    {
                        boolean allowed = p.getAsBoolean();

                        allowAbility.put(id, allowed);
                    }
                }
            }
        }

        Optional<JsonObject> costs = JSONHelper.getObject(json, "costs");

        if (costs.isPresent())
        {
            Map<String, JsonElement> costMap = costs.get().asMap();

            if (costMap.containsKey("baseClaimChunk"))
            {
                JsonElement element = costMap.get("baseClaimChunk");

                if (element.isJsonPrimitive())
                {
                    JsonPrimitive p = element.getAsJsonPrimitive();

                    if (p.isNumber())
                        this.costClaimChunkBase = p.getAsLong();
                }
            }
        }



        Optional<Float> solarRepairBaseChance = JSONHelper.getFloat(json, "solarRepairBaseChance");
        solarRepairBaseChance.ifPresent(aFloat -> this.solarRepairBaseChance = aFloat);

        Optional<Float> solarRepairExtraChance = JSONHelper.getFloat(json, "solarRepairExtraChance");
        solarRepairExtraChance.ifPresent(aFloat -> this.solarRepairExtraChance = aFloat);
    }


    public static float getSolarRepairBaseChance() { return currentConfig.solarRepairBaseChance; }
    public static float getSolarRepairExtraChance() { return currentConfig.solarRepairExtraChance; }

    public static long getBaseCostClaimChunk() { return currentConfig.costClaimChunkBase; }
}
