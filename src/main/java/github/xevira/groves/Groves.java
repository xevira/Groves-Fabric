package github.xevira.groves;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import github.xevira.groves.events.*;
import github.xevira.groves.poi.POIManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Groves implements ModInitializer {
	public static final String MOD_ID = "groves";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


	@Override
	public void onInitialize() {
		Registration.load();

		ServerLifecycleEvents.SERVER_STARTED.register(POIManager::onServerStared);
		ServerLifecycleEvents.SERVER_STOPPED.register(POIManager::onServerStopped);
		ServerLifecycleEvents.AFTER_SAVE.register(POIManager::onAfterSave);

		ServerWorldEvents.LOAD.register(POIManager::onWorldLoad);
		ServerWorldEvents.UNLOAD.register(POIManager::onWorldUnload);

		UseItemCallback.EVENT.register(ModUseItemEvents::onUseItem);
		ServerTickEvents.START_WORLD_TICK.register(ModServerTickEvents::onStartWorldTick);
		ServerTickEvents.END_SERVER_TICK.register(ModServerTickEvents::onEndServerTick);
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static String textPath(String prefix, String path)
	{
		return prefix + "." + MOD_ID + "." + path;
	}

	public static MutableText text(String prefix, String path) { return Text.translatable(textPath(prefix, path)); }

	public static MutableText text(String prefix, String path, Object... objects) { return Text.translatable(textPath(prefix, path), objects); }
}