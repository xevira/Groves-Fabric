package github.xevira.groves;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.groves.client.event.KeyInputHandler;
import github.xevira.groves.util.JSONHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ClientConfig {

    public static final boolean DEBUG_FILTER_PROFANITY = false;

    private static final String CONFIG_FILE_NAME = Groves.MOD_ID + ".json";
    private static final int CONFIG_VERSION = 1;

    private static ClientConfig currentConfig;

    private boolean devMode;
    private int colorChunkClaimed;
    private int colorOrigin;
    private int colorChunkLoaded;
    private int colorOriginLoaded;
    private int colorChunkAvailable;

    ClientConfig()
    {
        this.devMode = false;
        this.colorChunkClaimed = 0xFF63E363;
        this.colorChunkAvailable = 0xFFE3E363;
        this.colorOrigin = 0xFF00FF00;
        this.colorChunkLoaded = 0xFFE36363;
        this.colorOriginLoaded = 0xFFFF0000;
    }

    public static File getConfigDirectory()
    {
        return new File(MinecraftClient.getInstance().runDirectory, "config");
    }

    public static void load()
    {
        File configFile = new File(getConfigDirectory(), CONFIG_FILE_NAME);
        if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
            Groves.LOGGER.info("loading config {}", configFile.getPath());
            JsonElement element = JSONHelper.parseJsonFile(configFile);
            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();

                KeyInputHandler.loadConfig(root);

                int version = JSONHelper.getInt(root, "config_version", 0);

                currentConfig = new ClientConfig();

                Optional<JsonObject> colorsObj = JSONHelper.getObject(root, "colors");
                currentConfig.devMode = JSONHelper.getBoolean(root, "dev", false);

                colorsObj.ifPresent(colors -> {
                    currentConfig.colorOrigin = JSONHelper.JsonToColor(colors, "origin", 0xFF00FF00);
                    currentConfig.colorOriginLoaded = JSONHelper.JsonToColor(colors, "originLoaded", 0xFFFF0000);
                    currentConfig.colorChunkClaimed = JSONHelper.JsonToColor(colors, "chunkClaimed", 0xFF63E363);
                    currentConfig.colorChunkLoaded = JSONHelper.JsonToColor(colors, "chunkLoaded", 0xFFE36363);
                    currentConfig.colorChunkAvailable = JSONHelper.JsonToColor(colors, "available", 0xFFE3E363);
                });
            }
        }
        else
            save();
    }

    public static void save() {
        File dir = getConfigDirectory();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs())
        {
            Groves.LOGGER.info("saving config {}", new File(dir, CONFIG_FILE_NAME).getPath());

            JsonObject root = new JsonObject();

            if (currentConfig == null)
                currentConfig = new ClientConfig();

            KeyInputHandler.saveConfig(root);

            root.add("config_version", new JsonPrimitive(CONFIG_VERSION));

            if(currentConfig.devMode)
                root.add("dev", new JsonPrimitive(currentConfig.devMode));

            JsonObject colors = new JsonObject();

            colors.add("origin", JSONHelper.ColorToJson(currentConfig.colorOrigin));
            colors.add("originLoaded", JSONHelper.ColorToJson(currentConfig.colorOriginLoaded));
            colors.add("chunkClaimed", JSONHelper.ColorToJson(currentConfig.colorChunkClaimed));
            colors.add("chunkLoaded", JSONHelper.ColorToJson(currentConfig.colorChunkLoaded));
            colors.add("available", JSONHelper.ColorToJson(currentConfig.colorChunkAvailable));

            root.add("colors", colors);

            JSONHelper.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
        }
    }

    public static boolean isDevMode() { return currentConfig != null && currentConfig.devMode; }

    public static int colorChunkClaimed() { return currentConfig.colorChunkClaimed; }
    public static int colorChunkLoaded() { return currentConfig.colorChunkLoaded; }
    public static int colorOrigin() { return currentConfig.colorOrigin; }
    public static int colorOriginLoaded() { return currentConfig.colorOriginLoaded; }
    public static int colorChunkAvailable() { return currentConfig.colorChunkAvailable; }
}
