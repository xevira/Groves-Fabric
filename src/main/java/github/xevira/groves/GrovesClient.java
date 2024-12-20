package github.xevira.groves;

import com.terraformersmc.terraform.boat.api.client.TerraformBoatClientHelper;
import github.xevira.groves.client.event.KeyInputHandler;
import github.xevira.groves.client.item.MoonPhaseProperty;
import github.xevira.groves.client.renderer.DruidEntityRenderer;
import github.xevira.groves.client.renderer.MoonwellFluidLevelBER;
import github.xevira.groves.client.screen.GrovesSanctuaryScreen;
import github.xevira.groves.client.screen.MoonwellScreen;
import github.xevira.groves.events.BouncingHandler;
import github.xevira.groves.events.client.DangerSenseHandler;
import github.xevira.groves.events.client.HudRenderEvents;
import github.xevira.groves.network.Networking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.ModelTransformer;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.render.item.property.numeric.NumericProperties;

public class GrovesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Render Layers
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
                Registration.SANCTUM_SAPLING_BLOCK,
                Registration.SANCTUM_DOOR_BLOCK,
                Registration.SANCTUM_TRAPDOOR_BLOCK,
                Registration.MOONWELL_BASIN_BLOCK,
                Registration.WIND_CHIME_BLOCK,
                Registration.WORN_WIND_CHIME_BLOCK,
                Registration.DAMAGED_WIND_CHIME_BLOCK);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
                Registration.SANCTUM_LEAVES_BLOCK);

        // Boats
        TerraformBoatClientHelper.registerModelLayers(Registration.SANCTUM_BOAT_ID);

        // Model Layers
        ModelTransformer modelTransformer = ModelTransformer.scaling(0.9375F);
        EntityModelLayerRegistry.registerModelLayer(DruidEntityRenderer.MAIN_LAYER, () -> TexturedModelData.of(VillagerResemblingModel.getModelData(), 64, 64).transform(modelTransformer));

        // Block Renderers
        BlockEntityRendererFactories.register(Registration.MOONWELL_FAKE_FLUID_BLOCK_ENTITY, MoonwellFluidLevelBER::new);

        // Entity Renderers
        EntityRendererRegistry.register(Registration.DRUID_ENTITY, DruidEntityRenderer::new);

        // Fluid Renderers
        FluidRenderHandlerRegistry.INSTANCE.register(Registration.BLESSED_MOON_WATER_FLUID, Registration.FLOWING_BLESSED_MOON_WATER_FLUID,
                new SimpleFluidRenderHandler(Groves.id("block/blessed_moon_water_still"), Groves.id("block/blessed_moon_water_flowing")));

        FluidRenderHandlerRegistry.INSTANCE.register(Registration.MOONLIGHT_FLUID, Registration.FLOWING_MOONLIGHT_FLUID,
                new SimpleFluidRenderHandler(Groves.id("block/moonlight_still"), Groves.id("block/moonlight_flowing")));

        // Model Predicates
        //ModelPredicateProviderRegistry.register(Groves.id("lunar_phase"), MoonPhialItem::getModelPredicate);

        // ScreenHandlers
        HandledScreens.register(Registration.MOONWELL_SCREEN_HANDLER, MoonwellScreen::new);
        HandledScreens.register(Registration.GROVES_SANCTUARY_SCREEN_HANDLER, GrovesSanctuaryScreen::new);

        // HudRenderers
        HudRenderCallback.EVENT.register(HudRenderEvents::renderSanctuaryEntry);
        HudRenderCallback.EVENT.register(HudRenderEvents::renderSanctuaryMeters);

        // Keybinds
        KeyInputHandler.load();

        Networking.registerClient();

        // Special handlers
        ClientTickEvents.END_CLIENT_TICK.register(ignored -> DangerSenseHandler.INSTANCE.tick());
        ClientTickEvents.START_CLIENT_TICK.register(ignored -> DangerSenseHandler.MobHandler.onClientStartTick());
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, clientWorld) -> DangerSenseHandler.MobHandler.onEntityUnload(entity));
        ClientTickEvents.END_CLIENT_TICK.register(ignored -> BouncingHandler.onEndTick());

        NumericProperties.ID_MAPPER.put(Groves.id("moon_phase_overworld"), MoonPhaseProperty.Overworld.CODEC);
        NumericProperties.ID_MAPPER.put(Groves.id("moon_phase_unknown"), MoonPhaseProperty.Unknown.CODEC);
    }
}
