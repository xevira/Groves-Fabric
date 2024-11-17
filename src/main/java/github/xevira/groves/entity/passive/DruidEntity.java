package github.xevira.groves.entity.passive;

import com.google.common.collect.ImmutableMap;
import github.xevira.groves.Registration;
import github.xevira.groves.entity.Trades;
import github.xevira.groves.item.UnlockScrollItem;
import github.xevira.groves.sanctuary.GroveAbilities;
import github.xevira.groves.sanctuary.GroveSanctuary;
import github.xevira.groves.sanctuary.GroveUnlock;
import github.xevira.groves.sanctuary.GroveUnlocks;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class DruidEntity  extends MerchantEntity implements ConsumableComponent.ConsumableSoundProvider {
    private static final Int2ObjectOpenHashMap<GroveUnlock> UNLOCKS = new Int2ObjectOpenHashMap<>(
            ImmutableMap.of(
                    1, GroveUnlocks.MOONWELL
            )
    );

    @Nullable
    private BlockPos wanderTarget;
    private int despawnDelay;

    private GroveSanctuary sanctuary;

    public DruidEntity(EntityType<? extends DruidEntity> entityType, World world) {
        super(entityType, world);
    }

    public void setSanctuary(GroveSanctuary sanctuary)
    {
        this.sanctuary = sanctuary;
    }

    public static DefaultAttributeContainer.Builder addAttributes() {
        return MobEntity.createMobAttributes();
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector
                .add(
                        0,
                        new HoldInHandsGoal<>(
                                this,
                                PotionContentsComponent.createStack(Items.POTION, Potions.INVISIBILITY),
                                Registration.DRUID_DISAPPEARED_SOUND,
                                wanderingTrader -> this.getWorld().isNight() && !wanderingTrader.isInvisible()
                        )
                );
        this.goalSelector
                .add(
                        0,
                        new HoldInHandsGoal<>(
                                this,
                                new ItemStack(Items.MILK_BUCKET),
                                Registration.DRUID_REAPPEARED_SOUND,
                                wanderingTrader -> this.getWorld().isDay() && wanderingTrader.isInvisible()
                        )
                );
        this.goalSelector.add(1, new StopFollowingCustomerGoal(this));
        this.goalSelector.add(1, new FleeEntityGoal<>(this, ZombieEntity.class, 8.0F, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<>(this, EvokerEntity.class, 12.0F, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<>(this, VindicatorEntity.class, 8.0F, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<>(this, VexEntity.class, 8.0F, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<>(this, PillagerEntity.class, 15.0F, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<>(this, IllusionerEntity.class, 12.0F, 0.5, 0.5));
        this.goalSelector.add(1, new FleeEntityGoal<>(this, ZoglinEntity.class, 10.0F, 0.5, 0.5));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 0.5));
        this.goalSelector.add(1, new LookAtCustomerGoal(this));
        this.goalSelector.add(2, new DruidEntity.WanderToTargetGoal(this, 2.0, 0.35));
        this.goalSelector.add(4, new GoToWalkTargetGoal(this, 0.35));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 0.35));
        this.goalSelector.add(9, new StopAndLookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
    }

    @Override
    protected void fillRecipes() {
        TradeOfferList tradeOfferList = this.getOffers();

        // Default trades
        TradeOffers.Factory[] factory = Trades.DRUID_TRADES.get(0);
        if (factory != null)
        {
            this.fillRecipesFromPool(tradeOfferList, factory, 5);
        }

        // Add unlocked trades
        if (this.sanctuary != null) {
            UNLOCKS.forEach((index, unlock) ->
            {
                if (this.sanctuary.hasUnlock(unlock))
                {
                    TradeOffers.Factory[] f = Trades.DRUID_TRADES.get((int)index);
                    if (f != null)
                    {
                        this.fillRecipesFromPool(tradeOfferList, f, 1);
                    }
                }
            });
        }

        // Add some random unlock scrolls (not-forbidden)
        for(int i = 0; i < 3; i++)
        {
            UnlockScrollItem scroll = GroveAbilities.randomScroll(true, false);

            if (scroll != null)
            {
                int cost = scroll.getRank() * 10;

                tradeOfferList.add(new TradeOffer(new TradedItem(Items.EMERALD, cost), Optional.of(new TradedItem(Registration.UNLOCK_SCROLL_ITEM)), new ItemStack(scroll), 1, scroll.getRank(), 1));
            }
        }
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public boolean isLeveledMerchant() { return false; }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isOf(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.hasCustomer() && !this.isBaby()) {
            if (hand == Hand.MAIN_HAND) {
                player.incrementStat(Stats.TALKED_TO_VILLAGER);
            }

            if (!this.getWorld().isClient) {
                if (this.getOffers().isEmpty()) {
                    return ActionResult.CONSUME;
                }

                this.setCustomer(player);
                this.sendOffers(player, this.getDisplayName(), 1);
            }

            return ActionResult.SUCCESS;
        } else {
            return super.interactMob(player, hand);
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        if (offer.shouldRewardPlayerExperience()) {
            int i = 3 + this.random.nextInt(4);
            this.getWorld().spawnEntity(new ExperienceOrbEntity(this.getWorld(), this.getX(), this.getY() + 0.5, this.getZ(), i));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasCustomer() ? Registration.DRUID_TRADE_SOUND : Registration.DRUID_AMBIENT_SOUND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Registration.DRUID_HURT_SOUND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Registration.DRUID_DEATH_SOUND;
    }

    @Override
    public SoundEvent getConsumeSound(ItemStack stack) {
        return stack.isOf(Items.MILK_BUCKET) ? Registration.DRUID_DRINK_MILK_SOUND : Registration.DRUID_DRINK_POTION_SOUND;
    }

    @Override
    protected SoundEvent getTradingSound(boolean sold) {
        return sold ? Registration.DRUID_YES_SOUND : Registration.DRUID_NO_SOUND;
    }

    @Override
    public SoundEvent getYesSound() {
        return Registration.DRUID_YES_SOUND;
    }

    public void setDespawnDelay(int despawnDelay) {
        this.despawnDelay = despawnDelay;
    }

    public int getDespawnDelay() {
        return this.despawnDelay;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient) {
            this.tickDespawnDelay();
        }
    }

    private void tickDespawnDelay() {
        if (this.despawnDelay > 0 && !this.hasCustomer() && --this.despawnDelay == 0) {
            this.discard();
        }
    }


    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("DespawnDelay", this.despawnDelay);
        if (this.wanderTarget != null) {
            nbt.put("wander_target", NbtHelper.fromBlockPos(this.wanderTarget));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("DespawnDelay", NbtElement.NUMBER_TYPE)) {
            this.despawnDelay = nbt.getInt("DespawnDelay");
        }

        NbtHelper.toBlockPos(nbt, "wander_target").ifPresent(wanderTarget -> this.wanderTarget = wanderTarget);
        this.setBreedingAge(Math.max(0, this.getBreedingAge()));
    }




    public void setWanderTarget(@Nullable BlockPos wanderTarget) {
        this.wanderTarget = wanderTarget;
    }

    @Nullable
    BlockPos getWanderTarget() {
        return this.wanderTarget;
    }

    class WanderToTargetGoal extends Goal {
        final DruidEntity trader;
        final double proximityDistance;
        final double speed;

        WanderToTargetGoal(final DruidEntity trader, final double proximityDistance, final double speed) {
            this.trader = trader;
            this.proximityDistance = proximityDistance;
            this.speed = speed;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public void stop() {
            this.trader.setWanderTarget(null);
            DruidEntity.this.navigation.stop();
        }

        @Override
        public boolean canStart() {
            BlockPos blockPos = this.trader.getWanderTarget();
            return blockPos != null && this.isTooFarFrom(blockPos, this.proximityDistance);
        }

        @Override
        public void tick() {
            BlockPos blockPos = this.trader.getWanderTarget();
            if (blockPos != null && DruidEntity.this.navigation.isIdle()) {
                if (this.isTooFarFrom(blockPos, 10.0)) {
                    Vec3d vec3d = new Vec3d(
                            (double)blockPos.getX() - this.trader.getX(), (double)blockPos.getY() - this.trader.getY(), (double)blockPos.getZ() - this.trader.getZ()
                    )
                            .normalize();
                    Vec3d vec3d2 = vec3d.multiply(10.0).add(this.trader.getX(), this.trader.getY(), this.trader.getZ());
                    DruidEntity.this.navigation.startMovingTo(vec3d2.x, vec3d2.y, vec3d2.z, this.speed);
                } else {
                    DruidEntity.this.navigation.startMovingTo((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), this.speed);
                }
            }
        }

        private boolean isTooFarFrom(BlockPos pos, double proximityDistance) {
            return !pos.isWithinDistance(this.trader.getPos(), proximityDistance);
        }
    }

}
