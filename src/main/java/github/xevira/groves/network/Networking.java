package github.xevira.groves.network;

import github.xevira.groves.Groves;
import github.xevira.groves.poi.GrovesPOI;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class Networking {

    public static void register()
    {
        // Packet Registration
        // - Client -> Server
        PayloadTypeRegistry.playC2S().register(OpenGrovesRequestPayload.ID, OpenGrovesRequestPayload.PACKET_CODEC);

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
