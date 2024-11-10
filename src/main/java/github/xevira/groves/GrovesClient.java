package github.xevira.groves;

import github.xevira.groves.client.event.KeyInputHandler;
import github.xevira.groves.client.renderer.MoonwellFluidLevelBER;
import github.xevira.groves.client.screen.GrovesSanctuaryScreen;
import github.xevira.groves.client.screen.MoonwellScreen;
import github.xevira.groves.events.client.HudRenderEvents;
import github.xevira.groves.item.MoonPhialItem;
import github.xevira.groves.network.Networking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class GrovesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Render Layers
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
                Registration.MOONWELL_BASIN_BLOCK);

        // Block Renderers
        BlockEntityRendererFactories.register(Registration.MOONWELL_FAKE_FLUID_BLOCK_ENTITY, MoonwellFluidLevelBER::new);

        // Fluid Renderers
        FluidRenderHandlerRegistry.INSTANCE.register(Registration.BLESSED_MOON_WATER_FLUID, Registration.FLOWING_BLESSED_MOON_WATER_FLUID,
                new SimpleFluidRenderHandler(Groves.id("block/blessed_moon_water_still"), Groves.id("block/blessed_moon_water_flowing")));

        FluidRenderHandlerRegistry.INSTANCE.register(Registration.MOONLIGHT_FLUID, Registration.FLOWING_MOONLIGHT_FLUID,
                new SimpleFluidRenderHandler(Groves.id("block/moonlight_still"), Groves.id("block/moonlight_flowing")));

        // Model Predicates
        ModelPredicateProviderRegistry.register(Groves.id("lunar_phase"), MoonPhialItem::getModelPredicate);

        // ScreenHandlers
        HandledScreens.register(Registration.MOONWELL_SCREEN_HANDLER, MoonwellScreen::new);
        HandledScreens.register(Registration.GROVES_SANCTUARY_SCREEN_HANDLER, GrovesSanctuaryScreen::new);

        // HudRenderers
        HudRenderCallback.EVENT.register(HudRenderEvents::renderSanctuaryEntry);
        HudRenderCallback.EVENT.register(HudRenderEvents::renderSanctuaryMeters);

        // Keybinds
        KeyInputHandler.load();

        Networking.registerClient();
    }
}
