package github.xevira.groves.poi;

import com.google.gson.JsonObject;
import github.xevira.groves.Groves;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.core.jmx.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class POIManager {

    // -- SERVER

    private static Path getServerPath(MinecraftServer server)
    {
        return server.getSavePath(WorldSavePath.ROOT).resolve(Groves.MOD_ID + "/poi.json");
    }

    /** Called when the {@link net.minecraft.server.MinecraftServer} is loaded. **/
    public static void onServerStarted(MinecraftServer server)
    {
        Path path = getServerPath(server);

        try {
            if (Files.notExists(path)) {
                return;
            }

            String jsonStr = Files.readString(path);
            JsonObject json = Groves.GSON.fromJson(jsonStr, JsonObject.class);

            deserializeServer(json, server);
        } catch (IOException exception) {
            Groves.LOGGER.error("Failed to read POI file!", exception);
        }
    }

    /** Called when the {@link net.minecraft.server.MinecraftServer} is saved. **/
    public static void onServerStopped(MinecraftServer server)
    {
        onAfterSave(server, true, true);

        // Purge all data
        purgeServer();
    }

    public static void onAfterSave(MinecraftServer server, boolean flush, boolean force)
    {
        Path path = getServerPath(server);

        try {
            Files.createDirectories(path.getParent());
            JsonObject json = serializeServer(server);
            Files.writeString(path, Groves.GSON.toJson(json));
        } catch (IOException exception) {
            Groves.LOGGER.error("Failed to write POI file!", exception);
        }
    }


    // -- WORLD

    private static Path getWorldPath(MinecraftServer server, ServerWorld world)
    {
        return DimensionType.getSaveDirectory(world.getRegistryKey(), server.getSavePath(WorldSavePath.ROOT)).resolve(Groves.MOD_ID + "/poi.json");
    }

    /** Called when a {@link net.minecraft.server.world.ServerWorld} is loaded. **/
    public static void onWorldLoad(MinecraftServer server, ServerWorld world)
    {
        Path path = getWorldPath(server, world);

        try {
            if (Files.notExists(path)) {
                return;
            }

            String jsonStr = Files.readString(path);
            JsonObject json = Groves.GSON.fromJson(jsonStr, JsonObject.class);

            deserializeWorld(json, server, world);
        } catch (IOException exception) {
            Groves.LOGGER.error("Failed to read POI file!", exception);
        }
    }

    /** Called when a {@link net.minecraft.server.world.ServerWorld} is saved. **/
    public static void onWorldUnload(MinecraftServer server, ServerWorld world)
    {
        Path path = DimensionType.getSaveDirectory(world.getRegistryKey(), server.getSavePath(WorldSavePath.ROOT)).resolve(Groves.MOD_ID + "/poi.json");

        try {
            Files.createDirectories(path.getParent());
            JsonObject json = serializeWorld(server, world);
            Files.writeString(path, Groves.GSON.toJson(json));
        } catch (IOException exception) {
            Groves.LOGGER.error("Failed to write POI file!", exception);
        }
    }

    /** Deserializes {@link net.minecraft.server.MinecraftServer} level POI data. **/
    private static void deserializeServer(JsonObject json, MinecraftServer server)
    {
        if (!GrovesPOI.deserializeServer(json, server))
        {
            Groves.LOGGER.error("Failed to load Groves POI data.");
        }

        if (!WindChimes.deserializeServer(json, server))
        {
            Groves.LOGGER.error("Failed to load Wind Chime data.");
        }
    }

    /**
     *  <p>Deserializes {@link net.minecraft.server.world.ServerWorld} level POI data.</p>
     *
     *  <p>Executed for each {@link net.minecraft.server.world.ServerWorld}.</p>
     ***/
    private static void deserializeWorld(JsonObject json, MinecraftServer server, ServerWorld world)
    {
    }

    /** Serializes {@link net.minecraft.server.MinecraftServer} level POI data. **/
    private static JsonObject serializeServer(MinecraftServer server)
    {
        JsonObject json = new JsonObject();

        GrovesPOI.serializeServer(json, server);
        WindChimes.serializeServer(json, server);

        return json;
    }

    /**
     * <p>Serializes {@link net.minecraft.server.world.ServerWorld} level POI data.</p>
     *
     * <p>Executed for each {@link net.minecraft.server.world.ServerWorld}.</p>
     **/
    private static JsonObject serializeWorld(MinecraftServer server, ServerWorld world)
    {
        JsonObject json = new JsonObject();

        return json;
    }

    private static void purgeServer()
    {
        GrovesPOI.cleanup();
    }


    public static boolean canSpawn(ServerWorld world, BlockPos pos)
    {
        if (!WindChimes.canSpawn(world, pos)) return false;

        if (!GrovesPOI.canSpawn(world, pos)) return false;

        return true;
    }

}
