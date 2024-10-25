package github.xevira.groves;

import github.xevira.groves.events.*;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Groves implements ModInitializer {
	public static final String MOD_ID = "groves";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Registration.load();

		UseItemCallback.EVENT.register(ModUseItemEvents::onUseItem);
		ServerTickEvents.START_WORLD_TICK.register(ModServerTickEvents::onStartTick);
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