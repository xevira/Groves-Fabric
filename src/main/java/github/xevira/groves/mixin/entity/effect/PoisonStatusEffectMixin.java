package github.xevira.groves.mixin.entity.effect;

import github.xevira.groves.Registration;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.PoisonStatusEffect;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// PoisonStatusEffect was made accessible via access widener
@Mixin(PoisonStatusEffect.class)
public abstract class PoisonStatusEffectMixin {

    @Inject(method = "applyUpdateEffect(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;I)Z",
            at = @At("HEAD"), cancellable = true)
    private void applyUpdateEffectMixin(ServerWorld world, LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir)
    {
        // Block the damage from poison if you have Antidote
        if (entity.hasStatusEffect(Registration.ANTIDOTE_STATUS_EFFECT))
        {
            cir.setReturnValue(true);
        }
    }
}
