package github.xevira.groves.network;

import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveAbilities;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

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

        ClientPlayNetworking.registerGlobalReceiver(UpdateMoonwellPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                handler.setMoonwell(payload.pos());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(BuyChunkResponsePayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                if (payload.success())
                    handler.addChunk(payload.pos());
                else
                    handler.setBuyChunkReason(payload.reason());
            }
        });
    }

    public static void register()
    {
        // Packet Registration
        // - Client -> Server
        PayloadTypeRegistry.playC2S().register(OpenGrovesRequestPayload.ID, OpenGrovesRequestPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(GroveAbitlityKeybindPayload.ID, GroveAbitlityKeybindPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SetChunkLoadingPayload.ID, SetChunkLoadingPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(BuyChunkPayload.ID, BuyChunkPayload.PACKET_CODEC);

        // - Server -> Client
        PayloadTypeRegistry.playS2C().register(UpdateSunlightPayload.ID, UpdateSunlightPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateTotalFoliagePayload.ID, UpdateTotalFoliagePayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateMoonwellPayload.ID, UpdateMoonwellPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(BuyChunkResponsePayload.ID, BuyChunkResponsePayload.PACKET_CODEC);

        // Packet Handlers
        // - Server Side

        // Request to open the UI for the player's grove sanctuary
        ServerPlayNetworking.registerGlobalReceiver(OpenGrovesRequestPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            // Open Screen (if present)
            sanctuary.ifPresent(groveSanctuary -> context.player().openHandledScreen(groveSanctuary));

            // If they don't have a sanctuary, do nothing.
        });

        // Execute the specified grove ability
        ServerPlayNetworking.registerGlobalReceiver(GroveAbitlityKeybindPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            sanctuary.ifPresent(groveSanctuary -> GroveAbilities.executeKeybind(payload.name(), groveSanctuary, context.player()));
        });

        // Update the chunk loading state for the specified chunk
        ServerPlayNetworking.registerGlobalReceiver(SetChunkLoadingPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            sanctuary.ifPresent(groveSanctuary -> groveSanctuary.setChunkLoadingForChunk(payload.pos(), payload.loaded()));
        });

        ServerPlayNetworking.registerGlobalReceiver(BuyChunkPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            sanctuary.ifPresent(groveSanctuary -> groveSanctuary.buyChunk(payload.pos()));
        });


    }
}
