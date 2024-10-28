package github.xevira.groves.network;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class Networking {

    @Environment(EnvType.CLIENT)
    public static void registerClient()
    {
        // - Client Side
        ClientPlayNetworking.registerGlobalReceiver(UpdateSunlightPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                handler.setSunlight(payload.sunlight());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(UpdateTotalFoliagePayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                handler.setFoliage(payload.foliage());
            }
        });
    }

    public static void register()
    {
        // Packet Registration
        // - Client -> Server
        PayloadTypeRegistry.playC2S().register(OpenGrovesRequestPayload.ID, OpenGrovesRequestPayload.PACKET_CODEC);

        // - Server -> Client
        PayloadTypeRegistry.playS2C().register(UpdateSunlightPayload.ID, UpdateSunlightPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateTotalFoliagePayload.ID, UpdateTotalFoliagePayload.PACKET_CODEC);

        // Packet Handlers
        // - Server Side
        ServerPlayNetworking.registerGlobalReceiver(OpenGrovesRequestPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            // Open Screen (if present)
            sanctuary.ifPresent(groveSanctuary -> context.player().openHandledScreen(groveSanctuary));

            // If they don't have a sanctuary, do nothing.
        });

    }
}
