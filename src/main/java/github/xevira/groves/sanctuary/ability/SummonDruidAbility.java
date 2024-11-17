package github.xevira.groves.sanctuary.ability;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.groves.Registration;
import github.xevira.groves.entity.passive.DruidEntity;
import github.xevira.groves.sanctuary.GroveAbility;
import github.xevira.groves.sanctuary.GroveSanctuary;
import github.xevira.groves.util.JSONHelper;
import net.minecraft.entity.SpawnLocation;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class SummonDruidAbility extends GroveAbility {
    private long waitSeconds = -1L;
    private long lastSummonTime = -1L;
    private final Random random = Random.create();

    // Special case Manual
    public SummonDruidAbility() {
        super("summon_druid", false, true, true, true, false, 1);
    }

    @Override
    public Supplier<? extends GroveAbility> getConstructor() {
        return SummonDruidAbility::new;
    }

    @Override
    public @Nullable Item getRecipeIngredient(int rank) {
        return null;
    }

    @Override
    public String getEnglishTranslation() {
        return "Summon Druid";
    }

    @Override
    public String getEnglishLoreTranslation(int rank) {
        return "Calls forth a wandering druid to provide you with resources.";
    }

    @Override
    public String getEnglishStartCostTranslation() {
        return "";
    }

    @Override
    public String getEnglishTickCostTranslation() {
        return "";
    }

    @Override
    public String getEnglishUseCostTranslation() {
        return "";
    }

    @Override
    public void sendFailure(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {

    }

    @Override
    protected void onActivate(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        this.waitSeconds = rng.nextBetween(15, 30);
    }

    @Nullable
    private BlockPos getNearbySpawnPos(WorldView world, BlockPos pos, int range) {
        BlockPos blockPos = null;
        SpawnLocation spawnLocation = SpawnRestriction.getLocation(Registration.DRUID_ENTITY);

        for (int i = 0; i < 10; i++) {
            int j = pos.getX() + this.random.nextInt(range * 2) - range;
            int k = pos.getZ() + this.random.nextInt(range * 2) - range;
            int l = world.getTopY(Heightmap.Type.WORLD_SURFACE, j, k);
            BlockPos blockPos2 = new BlockPos(j, l, k);
            if (spawnLocation.isSpawnPositionOk(world, blockPos2, Registration.DRUID_ENTITY)) {
                blockPos = blockPos2;
                break;
            }
        }

        return blockPos;
    }

    private boolean doesNotSuffocateAt(BlockView world, BlockPos pos) {
        for (BlockPos blockPos : BlockPos.iterate(pos, pos.add(1, 2, 1))) {
            if (!world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private boolean trySpawn(ServerWorld world, GroveSanctuary sanctuary, ServerPlayerEntity owner)
    {
        if (owner == null) return false;

        BlockPos blockPos = owner.getBlockPos();
        int range = 48;
        PointOfInterestStorage pointOfInterestStorage = world.getPointOfInterestStorage();
        Optional<BlockPos> optional = pointOfInterestStorage.getPosition(
                poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING), pos -> true, blockPos, range, PointOfInterestStorage.OccupationStatus.ANY
        );

        BlockPos blockPos2 = (BlockPos)optional.orElse(blockPos);
        BlockPos blockPos3 = this.getNearbySpawnPos(world, blockPos2, range);

        if (blockPos3 != null && this.doesNotSuffocateAt(world, blockPos3)) {
            DruidEntity druid = Registration.DRUID_ENTITY.spawn(world, blockPos3, SpawnReason.EVENT);
            if (druid != null) {
                druid.setDespawnDelay(48000);
                druid.setWanderTarget(blockPos2);
                druid.setPositionTarget(blockPos2, 16);
                druid.setSanctuary(sanctuary);
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onDeactivate(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        this.lastSummonTime = sanctuary.getWorld().getTimeOfDay();
        ServerPlayerEntity owner = sanctuary.getOwnerPlayer();
        // Summon the druid
        if (owner != null) {
            if (!trySpawn(sanctuary.getWorld(), sanctuary, owner))
            {
                owner.sendMessage(Text.literal("A wandering druid appears!  ").append(Text.literal("*TADA!*").formatted(Formatting.GREEN, Formatting.ITALIC)), false);
            }
        }
    }

    @Override
    protected void onDeactivateCooldown(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        int days = rng.nextBetween(1, 4);

        ServerWorld world = sanctuary.getWorld();
        long time = world.getTimeOfDay();

        // Back to the last dawn, then add DAYS days.
        setCooldown(world, days * 24000L - (time % 24000));
    }

    @Override
    public boolean onServerTick(MinecraftServer server, GroveSanctuary sanctuary) {
        return (--this.waitSeconds <= 0);
    }

    @Override
    public boolean canUse(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        return !isActive() && !inCooldown(sanctuary.getWorld());
    }

    @Override
    protected boolean onUse(MinecraftServer server, GroveSanctuary sanctuary, PlayerEntity player) {
        this.activate(server, sanctuary, player);
        return false;
    }

    @Override
    public void serializeExtra(JsonObject json) {
        JsonObject extra = new JsonObject();

        if (this.waitSeconds > 0)
            extra.add("waitSeconds", new JsonPrimitive(this.waitSeconds));

        if (this.lastSummonTime > 0)
            extra.add("lastSummonTime", new JsonPrimitive(this.lastSummonTime));

        json.add("extra", extra);
    }

    @Override
    public boolean deserializeExtra(JsonObject json) {
        JsonObject extra = JSONHelper.getObject(json, "extra").orElse(null);
        if (extra != null)
        {
            this.waitSeconds = JSONHelper.getLong(extra, "waitSeconds").orElse(0L);
            this.lastSummonTime = JSONHelper.getLong(extra, "lastSummonTime").orElse(0L);
        }
        return true;
    }
}
