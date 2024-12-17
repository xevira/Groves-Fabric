package github.xevira.groves.mixin.entity;

import github.xevira.groves.Registration;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
@Debug(export = true)
public abstract class PlayerEntityMixin {

    @Inject(method = "createPlayerAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
        at = @At("RETURN"), cancellable = true)
    private static void createPlayerAttributesMixin(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir)
    {
        DefaultAttributeContainer.Builder builder = cir.getReturnValue();
        builder.add(Registration.INTANGIBLE_ATTRIBUTE);
    }

    @Inject(method = "attack(Lnet/minecraft/entity/Entity;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;onTargetDamaged(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;)V",
                    shift = At.Shift.AFTER))
    private void attackMixin(Entity target, CallbackInfo cb)
    {
        StatusEffectInstance inferno = ((PlayerEntity)(Object)this).getStatusEffect(Registration.INFERNO_STATUS_EFFECT);
        if (inferno != null)
        {
            target.setOnFireFor(4.0f * (inferno.getAmplifier() + 1));
        }
    }
}
