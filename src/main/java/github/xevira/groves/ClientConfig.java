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

@Environment(EnvType.CLIENT)
public class ClientConfig {
    private static final String CONFIG_FILE_NAME = Groves.MOD_ID + ".json";
    private static final int CONFIG_VERSION = 1;

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

            KeyInputHandler.saveConfig(root);

            root.add("config_version", new JsonPrimitive(CONFIG_VERSION));

            JSONHelper.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
        }
    }
}
