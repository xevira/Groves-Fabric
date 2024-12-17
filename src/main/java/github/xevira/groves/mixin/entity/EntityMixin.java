package github.xevira.groves.mixin.entity;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    @Final
    protected static int GLOWING_FLAG_INDEX;

    @Shadow
    protected abstract void setFlag(int index, boolean value);

    @Shadow
    public abstract boolean isSpectator();

//    @Inject(method = "setVelocity(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"))
//    private void setVelocityMixin(Vec3d velocity, CallbackInfo cb)
//    {
//        if(((Entity)(Object)this) instanceof ServerPlayerEntity) {
//            Groves.LOGGER.info("server.setVelocity({})", velocity.y);
//        }
//        else if(((Entity)(Object)this) instanceof ClientPlayerEntity) {
//            Groves.LOGGER.info("client.setVelocity({})", velocity.y);
//        }
//    }

    @Inject(method = "isInsideWall()Z", at = @At("HEAD"), cancellable = true)
    private void isInsideWallMixin(CallbackInfoReturnable<Boolean> cir)
    {
        if (((Entity)(Object)this) instanceof PlayerEntity player && isIntangible(player))
        {
            cir.setReturnValue(false);
        }
    }

    private boolean isIntangible(PlayerEntity player)
    {
        return player.getAttributeValue(Registration.INTANGIBLE_ATTRIBUTE) > 0.0;
    }

    // When under the effect of Inferno, render them as if they are on "fire"
    @Inject(method = "doesRenderOnFire()Z", at = @At("RETURN"), cancellable = true)
    private void doesRenderOnFireMixin(CallbackInfoReturnable<Boolean> cir)
    {
        if (((Entity)(Object)this) instanceof LivingEntity living && !this.isSpectator())
        {
            if (living.hasStatusEffect(Registration.INFERNO_STATUS_EFFECT))
            {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "setGlowing(Z)V", at = @At("TAIL"), cancellable = true)
    @Final
    private void setGlowingMixin(boolean glowing, CallbackInfo cb)
    {
        this.setFlag(GLOWING_FLAG_INDEX, glowing);
    }
}
