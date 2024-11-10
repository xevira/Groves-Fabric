package github.xevira.groves.network;

import github.xevira.groves.Groves;
import github.xevira.groves.events.client.HudRenderEvents;
import github.xevira.groves.item.ImprintingSigilItem;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.GroveAbilities;
import github.xevira.groves.screenhandler.GrovesSanctuaryScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import javax.xml.stream.events.StartDocument;
import java.util.Optional;

public class Networking {

    @Environment(EnvType.CLIENT)
    public static void registerClient()
    {
        // - Client Side
        ClientPlayNetworking.registerGlobalReceiver(ImprintPayload.ID, (payload, context) -> {
            ImprintingSigilItem.clientImprintSuccessful(context.player());
        });

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

        ClientPlayNetworking.registerGlobalReceiver(ClaimChunkResponsePayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                if (payload.success())
                    handler.addChunk(payload.pos());
                else
                    handler.setErrorMessage(payload.reason());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(UpdateAvailableChunkPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                if (payload.add())
                    handler.getSanctuary().addAvailable(payload.pos());
                else
                    handler.getSanctuary().removeAvailable(payload.pos());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ResetAvailableChunksPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                handler.getSanctuary().setAvailableChunks(payload.chunks());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SetSpawnPointResponsePayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                if (payload.success())
                    handler.getSanctuary().setSpawnPoint(payload.pos());
                else
                    handler.setErrorMessage(payload.reason());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SanctuaryEnterPayload.ID, (payload, context) -> {
//            Groves.LOGGER.info("SanctuaryEnterPayload({}, {}, {}, {}, {})", payload.uuid(), payload.name(), payload.groveName(), payload.abandoned(), payload.entry());

            HudRenderEvents.setSanctuaryEntry(payload.uuid(), payload.name(), payload.groveName(), payload.abandoned(), payload.entry());
        });

        ClientPlayNetworking.registerGlobalReceiver(SanctuarySunlightPayload.ID, (payload, context) -> {
            HudRenderEvents.setSunlightPercent(payload.sunlightPercent());
        });

        ClientPlayNetworking.registerGlobalReceiver(SanctuaryDarknessPayload.ID, (payload, context) -> {
            HudRenderEvents.setDarknessPercent(payload.darknessPercent());
        });

        ClientPlayNetworking.registerGlobalReceiver(SetGroveNameResponsePayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                if (payload.success())
                    handler.getSanctuary().setGroveName(payload.name());
                else
                    handler.setErrorMessage(payload.reason());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(AddFriendResponsePayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                if (payload.success())
                    handler.getSanctuary().addFriend(payload.uuid(), payload.name());
                else
                    handler.setErrorMessage(payload.reason());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(RemoveFriendResponsePayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                if (payload.success())
                    handler.getSanctuary().removeFriend(payload.uuid());
                else
                    handler.setErrorMessage(payload.reason());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncChunkColorsPayload.ID, (payload, context) -> {
            RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(payload.worldId()));

            GrovesPOI.SetChunkColors(worldKey, payload.chunks(), payload.colors());
        });

        ClientPlayNetworking.registerGlobalReceiver(UpdateAbilityPayload.ID, (payload, context) -> {
            if (context.player().currentScreenHandler instanceof GrovesSanctuaryScreenHandler handler)
            {
                handler.getSanctuary().updateAbility(payload.name(), payload.active(), payload.start(), payload.end());
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
        PayloadTypeRegistry.playC2S().register(ClaimChunkPayload.ID, ClaimChunkPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SetSpawnPointPayload.ID, SetSpawnPointPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SetGroveNamePayload.ID, SetGroveNamePayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(AddFriendPayload.ID, AddFriendPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(RemoveFriendPayload.ID, RemoveFriendPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(StartGroveAbitlityPayload.ID, StartGroveAbitlityPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(StopGroveAbitlityPayload.ID, StopGroveAbitlityPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(UseGroveAbitlityPayload.ID, UseGroveAbitlityPayload.PACKET_CODEC);

        // - Server -> Client
        PayloadTypeRegistry.playS2C().register(UpdateSunlightPayload.ID, UpdateSunlightPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateDarknessPayload.ID, UpdateDarknessPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateTotalFoliagePayload.ID, UpdateTotalFoliagePayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateMoonwellPayload.ID, UpdateMoonwellPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ClaimChunkResponsePayload.ID, ClaimChunkResponsePayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateAvailableChunkPayload.ID, UpdateAvailableChunkPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ResetAvailableChunksPayload.ID, ResetAvailableChunksPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SetSpawnPointResponsePayload.ID, SetSpawnPointResponsePayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ImprintPayload.ID, ImprintPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SanctuaryEnterPayload.ID, SanctuaryEnterPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SetGroveNameResponsePayload.ID, SetGroveNameResponsePayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(AddFriendResponsePayload.ID, AddFriendResponsePayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(RemoveFriendResponsePayload.ID, RemoveFriendResponsePayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncChunkColorsPayload.ID, SyncChunkColorsPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncSanctuariesPayload.ID, SyncSanctuariesPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SanctuarySunlightPayload.ID, SanctuarySunlightPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SanctuaryDarknessPayload.ID, SanctuaryDarknessPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateAbilityPayload.ID, UpdateAbilityPayload.PACKET_CODEC);

        // Packet Handlers
        // - Server Side

        // Request to open the UI for the player's grove sanctuary
        ServerPlayNetworking.registerGlobalReceiver(OpenGrovesRequestPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            // Open Screen (if present)
            if(sanctuary.isPresent())
                sanctuary.get().openUI(context.player());
            else if(context.player().isCreativeLevelTwoOp())
            {
                sanctuary = GrovesPOI.getSanctuaryAbandoned(context.player().getServerWorld(), context.player().getBlockPos());
                sanctuary.ifPresent(groveSanctuary -> groveSanctuary.openUI(context.player()));
            }

            // If they don't have a sanctuary or not creative inside an abandoned grove, do nothing.
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

        ServerPlayNetworking.registerGlobalReceiver(ClaimChunkPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            sanctuary.ifPresent(groveSanctuary -> groveSanctuary.claimChunk(context.player(), payload.pos()));
        });

        ServerPlayNetworking.registerGlobalReceiver(SetSpawnPointPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            sanctuary.ifPresent(groveSanctuary -> {
                switch(groveSanctuary.setSpawnPoint(payload.pos()))
                {
                    case SUCCESS -> groveSanctuary.sendListeners(new SetSpawnPointResponsePayload(payload.pos(), true, Text.empty()));
                    case NOT_GROVE -> ServerPlayNetworking.send(context.player(), new SetSpawnPointResponsePayload(payload.pos(), false, Groves.text("error", "location.not_grove")));
                    case AIR -> ServerPlayNetworking.send(context.player(), new SetSpawnPointResponsePayload(payload.pos(), false, Groves.text("error", "location.air")));
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SetGroveNamePayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            sanctuary.ifPresent(groveSanctuary -> {
                // TODO: Validate name

                groveSanctuary.setGroveName(payload.name());
                ServerPlayNetworking.send(context.player(), new SetGroveNameResponsePayload(payload.name(), true, Text.empty()));
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(AddFriendPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            sanctuary.ifPresent(groveSanctuary -> groveSanctuary.addFriend(context.player(), payload.name()));
        });

        ServerPlayNetworking.registerGlobalReceiver(RemoveFriendPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            sanctuary.ifPresent(groveSanctuary -> groveSanctuary.removeFriend(context.player(), payload.uuid()));
        });

        ServerPlayNetworking.registerGlobalReceiver(StartGroveAbitlityPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            sanctuary.ifPresent(groveSanctuary -> GroveAbilities.startAbility(payload.name(), groveSanctuary, context.player()));
        });

        ServerPlayNetworking.registerGlobalReceiver(StopGroveAbitlityPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            sanctuary.ifPresent(groveSanctuary -> GroveAbilities.stopAbility(payload.name(), groveSanctuary, context.player()));
        });

        ServerPlayNetworking.registerGlobalReceiver(UseGroveAbitlityPayload.ID, (payload, context) -> {
            Optional<GrovesPOI.GroveSanctuary> sanctuary = GrovesPOI.getSanctuary(context.player());

            sanctuary.ifPresent(groveSanctuary -> GroveAbilities.useAbility(payload.name(), groveSanctuary, context.player()));
        });
    }
}
