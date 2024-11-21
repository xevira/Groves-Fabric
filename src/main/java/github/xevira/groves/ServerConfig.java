package github.xevira.groves;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.groves.sanctuary.GroveAbilities;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.util.JSONHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

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

    private final Map<String, Boolean> allowAbility = new HashMap<>();

    private float solarRepairBaseChance;
    private float solarRepairExtraChance;

    private final Map<String, Long> costMap = new HashMap<>();

    private float foliagePowerClear;
    private float foliagePowerRaining;
    private float foliageEnchantedMultiplier;

    private long sunlightPerChunk;
    private int maxDarkness;

    private final Map<RegistryKey<Block>, Integer> foliageMap = new HashMap<>();

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
        resetAllowedAbilities();
        resetFoliagePower();
        resetCosts();

        this.solarRepairBaseChance = 0.05f;
        this.solarRepairExtraChance = 0.025f;

        this.sunlightPerChunk = 1000000L;
        this.maxDarkness = 1000000;

        this.foliagePowerClear = 0.15f;
        this.foliagePowerRaining = 0.05f;
        this.foliageEnchantedMultiplier = 1.5f;

    }

    private void resetCosts()
    {
        this.costMap.clear();
        this.costMap.put("baseClaimChunk", 10000L);
        this.costMap.put("formMoonwell", 100000L);
    }

    private long getCost(String name)
    {
        return this.costMap.getOrDefault(name, 0L);
    }

    private void resetAllowedAbilities()
    {
        this.allowAbility.clear();
        for(GroveAbility ability : GroveAbilities.ABILITIES.values())
        {
            this.allowAbility.put(ability.getName(), ability.getDefaultAllow());
        }
    }

    private void resetFoliagePower()
    {
        this.foliageMap.clear();
        addFoliagePower(Registration.SANCTUM_LEAVES_BLOCK, 10);     // SUPER powerful
        addFoliagePower(Blocks.ACACIA_LEAVES, 1);
        addFoliagePower(Blocks.AZALEA_LEAVES, 1);
        addFoliagePower(Blocks.BIRCH_LEAVES, 1);
        addFoliagePower(Blocks.OAK_LEAVES, 1);
        addFoliagePower(Blocks.PALE_OAK_LEAVES, 1);
        addFoliagePower(Blocks.DARK_OAK_LEAVES, 2);
        addFoliagePower(Blocks.FLOWERING_AZALEA_LEAVES, 2);
        addFoliagePower(Blocks.SPRUCE_LEAVES, 3);
        addFoliagePower(Blocks.JUNGLE_LEAVES, 3);
        addFoliagePower(Blocks.MANGROVE_LEAVES, 3);
        addFoliagePower(Blocks.CHERRY_LEAVES, 4);
    }

    private void addFoliagePower(RegistryKey<Block> block, int power)
    {
        this.foliageMap.put(block, Math.max(power, 0));
    }

    private void addFoliagePower(Block block, int power)
    {
        Optional<RegistryKey<Block>> key = Registries.BLOCK.getKey(block);
        key.ifPresent(blockRegistryKey -> addFoliagePower(blockRegistryKey, power));
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

    private void serializeAllowed(JsonObject json)
    {
        JsonObject allow = new JsonObject();

        this.allowAbility.forEach((name, allowed) -> {
            allow.add(name, new JsonPrimitive(allowed));
        });

        json.add("allow", allow);
    }

    private void serializeCosts(JsonObject json)
    {
        JsonObject costs = new JsonObject();

        this.costMap.forEach((name, cost) -> {
            costs.add(name, new JsonPrimitive(cost));
        });

        json.add("costs", costs);
    }

    private void serializeFoliagePower(JsonObject json)
    {
        JsonObject foliage = new JsonObject();

        this.foliageMap.forEach((k, v) -> {
            foliage.add(k.getValue().toString(), new JsonPrimitive(v));
        });

        json.add("foliage", foliage);
    }

    private JsonObject serialize() {
        JsonObject json = new JsonObject();

        serializeAllowed(json);
        serializeCosts(json);
        serializeFoliagePower(json);

        json.add("solarRepairBaseChance", new JsonPrimitive(this.solarRepairBaseChance));
        json.add("solarRepairExtraChance", new JsonPrimitive(this.solarRepairExtraChance));
        json.add("foliagePowerClear", new JsonPrimitive(this.foliagePowerClear));
        json.add("foliagePowerRaining", new JsonPrimitive(this.foliagePowerRaining));
        json.add("foliageEnchantedMultiplier", new JsonPrimitive(this.foliageEnchantedMultiplier));
        json.add("sunlightPerChunk", new JsonPrimitive(this.sunlightPerChunk));
        json.add("maxDarkness", new JsonPrimitive(this.maxDarkness));

        return json;
    }

    private void deserializeAllow(JsonObject json)
    {
        resetAllowedAbilities();

        JSONHelper.getObject(json, "allow").ifPresent(allow -> {
            Map<String, JsonElement> map = allow.asMap();

            map.forEach((name, allowedElem) -> {
                if (allowedElem.isJsonPrimitive())
                {
                    JsonPrimitive p = allowedElem.getAsJsonPrimitive();
                    if (p.isBoolean())
                    {
                        allowAbility.put(name, p.getAsBoolean());
                    }
                }
            });
        });
    }

    private void deserializeFoliage(JsonObject json)
    {
        resetFoliagePower();

        JSONHelper.getObject(json, "foliage").ifPresent(foliage -> {
            Map<String, JsonElement> map = foliage.asMap();

            map.forEach((name, foliageElem) -> {
                if (foliageElem.isJsonPrimitive())
                {
                    JsonPrimitive p = foliageElem.getAsJsonPrimitive();
                    if (p.isNumber())
                    {
                        foliageMap.put(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(name)), MathHelper.clamp(p.getAsInt(), 0, 10));
                    }
                }
            });
        });
    }


    private void deserializeCosts(JsonObject json)
    {
        resetCosts();

        JSONHelper.getObject(json, "costs").ifPresent(costs -> {
            Map<String, JsonElement> map = costs.asMap();

            map.forEach((name, costElem) -> {
                if (costElem.isJsonPrimitive())
                {
                    JsonPrimitive p = costElem.getAsJsonPrimitive();
                    if (p.isNumber())
                    {
                        this.costMap.put(name, MathHelper.clamp(p.getAsLong(), 0L, 10000000L));
                    }
                }
            });
        });
    }



    private void deserialize(JsonObject json) {

        deserializeAllow(json);
        deserializeCosts(json);
        deserializeFoliage(json);

        this.solarRepairBaseChance = JSONHelper.getFloat(json, "solarRepairBaseChance", 0.05f, 0.0f, 1.0f);
        this.solarRepairExtraChance = JSONHelper.getFloat(json, "solarRepairExtraChance", 0.025f, 0.0f, 1.0f);
        this.foliagePowerClear = JSONHelper.getFloat(json, "foliagePowerClear", 0.15f, 0.0f, 1.0f);
        this.foliagePowerRaining = JSONHelper.getFloat(json, "foliagePowerRaining", 0.055f, 0.0f, 1.0f);
        this.foliageEnchantedMultiplier = JSONHelper.getFloat(json, "foliageEnchantedMultiplier", 1.5f, 1.0f, 2.0f);
        this.sunlightPerChunk = JSONHelper.getLong(json, "sunlightPerChunk", 1000000L, 0L, 10000000L);
        this.maxDarkness = JSONHelper.getInt(json, "maxDarkness", 1000000, 0, 10000000);
    }

    public static float getSolarRepairBaseChance() { return currentConfig.solarRepairBaseChance; }
    public static float getSolarRepairExtraChance() { return currentConfig.solarRepairExtraChance; }

    public static long sunlightPerChunk() { return currentConfig.sunlightPerChunk; }
    public static int maxDarkness() { return currentConfig.maxDarkness; }

    public static long getBaseCostClaimChunk() { return currentConfig.getCost("baseClaimChunk"); }
    public static long getFormMoonwellCost() { return currentConfig.getCost("formMoonwell"); }

    public static float getFoliagePowerRating(boolean raining)
    {
        return raining ? currentConfig.foliagePowerRaining : currentConfig.foliagePowerClear;
    }

    public static int getFoliagePower(Block block) {
        Optional<RegistryKey<Block>> key = Registries.BLOCK.getKey(block);
        if (key.isPresent()) {
            if (currentConfig.foliageMap.containsKey(key.get()))
                return currentConfig.foliageMap.get(key.get());
        }

        return 1;
    }

    public static float getFoliageEnchantedMultiplier() { return currentConfig.foliageEnchantedMultiplier; }



}
