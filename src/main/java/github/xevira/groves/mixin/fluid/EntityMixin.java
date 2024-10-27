package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import github.xevira.groves.fluid.FluidSystem;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract World getWorld();

    @Shadow
    private BlockPos blockPos;

    @Shadow
    public abstract boolean updateMovementInFluid(TagKey<Fluid> tag, double speed);

    @Shadow
    protected boolean firstUpdate;

    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Shadow
    protected boolean touchingWater;

    @Shadow
    public abstract void onLanding();

    @Shadow
    public abstract void extinguish();

    @Shadow
    private EntityDimensions dimensions;

    @Shadow
    protected abstract SoundEvent getSplashSound();

    @Shadow
    protected abstract SoundEvent getHighSpeedSplashSound();

    @ModifyExpressionValue(method = "updateSwimming",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesUpdateSwimming(boolean original) {
        if (original)
            return true;

        FluidState state = getWorld().getFluidState(this.blockPos);
        FluidSystem data = FluidSystem.FLUIDS.get(state.getFluid());
        return data != null && data.canSwim();
    }

    @ModifyExpressionValue(method = "updateWaterState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;updateMovementInFluid(Lnet/minecraft/registry/tag/TagKey;D)Z"))
    private boolean grovesUpdateWaterState(boolean original, @Local double ultrawarmModifier) {
        if (original)
            return true;

        Entity entity = (Entity) (Object) this;

        for (FluidSystem fluidDatum : FluidSystem.FLUIDS.values()) {
            if (updateMovementInFluid(fluidDatum.fluidTag(), fluidDatum.fluidMovementSpeed().apply(entity, ultrawarmModifier))) {
                return true;
            }
        }

        return false;
    }

    @Inject(method = "applyGravity", at = @At("HEAD"), cancellable = true)
    private void grovesApplyGravity(CallbackInfo callback) {
        if ((Entity) (Object) this instanceof ItemEntity itemEntity) {
            for (FluidSystem fluidSystem : FluidSystem.FLUIDS.values()) {
                if (!this.firstUpdate && this.fluidHeight.getDouble(fluidSystem.fluidTag()) > 0.0D) {
                    fluidSystem.applyBuoyancy().accept(itemEntity);
                    callback.cancel();
                }
            }
        }
    }

    @Inject(method = "checkWaterState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;updateMovementInFluid(Lnet/minecraft/registry/tag/TagKey;D)Z"),
            cancellable = true)
    private void grovesCheckWaterState(CallbackInfo callback) {
        Entity entity = (Entity) (Object) this;
        for (FluidSystem fluidSystem : FluidSystem.FLUIDS.values()) {
            if (!fluidSystem.canSwim())
                continue;

            if (updateMovementInFluid(fluidSystem.fluidTag(), fluidSystem.fluidMovementSpeed().apply(entity, 0.0D))) {
                if (!this.touchingWater && !this.firstUpdate) {
                    grovesOnSwimmingStart(entity, dimensions, getSplashSound(), getHighSpeedSplashSound(), fluidSystem);
                }

                if (fluidSystem.shouldBreakLanding())
                    onLanding();
                this.touchingWater = true;
                if (fluidSystem.shouldExtinguish())
                    extinguish();

                callback.cancel();
            }
        }
    }

    @Unique
    private static void grovesOnSwimmingStart(Entity thisEntity, EntityDimensions dimensions, SoundEvent entitySplashSound, SoundEvent entityHighSpeedSplashSound, FluidSystem fluidSystem) {
        Entity entity = Objects.requireNonNullElse(thisEntity.getControllingPassenger(), thisEntity);
        float distanceModifier = entity == thisEntity ? 0.2F : 0.9F;

        Vec3d velocity = entity.getVelocity();
        float volume = Math.min(1.0F, (float) Math.sqrt(velocity.x * velocity.x * 0.2F + velocity.y * velocity.y + velocity.z * velocity.z * 0.2F) * distanceModifier);
        if (volume < 0.25F) {
            thisEntity.playSound(entitySplashSound, volume,
                    1.0F + (thisEntity.getRandom().nextFloat() - thisEntity.getRandom().nextFloat()) * 0.4F);
        } else {
            thisEntity.playSound(entityHighSpeedSplashSound, volume,
                    1.0F + (thisEntity.getRandom().nextFloat() - thisEntity.getRandom().nextFloat()) * 0.4F);
        }

        float yPos = (float) MathHelper.floor(thisEntity.getY());
        for (int i = 0; (float) i < 1.0F + dimensions.width() * 20.0F; i++) {
            double xOffset = (thisEntity.getRandom().nextDouble() * 2.0 - 1.0) * (double) dimensions.width();
            double yOffset = (thisEntity.getRandom().nextDouble() * 2.0 - 1.0) * (double) dimensions.width();
            thisEntity.getWorld().addParticle(fluidSystem.bubbleParticle(),
                    thisEntity.getX() + xOffset,
                    yPos + 1.0F,
                    thisEntity.getZ() + yOffset,
                    velocity.x,
                    velocity.y - thisEntity.getRandom().nextDouble() * 0.2F,
                    velocity.z);
        }

        for (int i = 0; (float) i < 1.0F + dimensions.width() * 20.0F; i++) {
            double xOffset = (thisEntity.getRandom().nextDouble() * 2.0 - 1.0) * (double) dimensions.width();
            double yOffset = (thisEntity.getRandom().nextDouble() * 2.0 - 1.0) * (double) dimensions.width();
            thisEntity.getWorld().addParticle(fluidSystem.splashParticle(),
                    thisEntity.getX() + xOffset,
                    yPos + 1.0F,
                    thisEntity.getZ() + yOffset,
                    velocity.x,
                    velocity.y,
                    velocity.z);
        }

        thisEntity.emitGameEvent(GameEvent.SPLASH);
    }
}