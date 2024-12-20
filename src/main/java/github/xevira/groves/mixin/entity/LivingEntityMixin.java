package github.xevira.groves.mixin.entity;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.events.BouncingHandler;
import github.xevira.groves.network.BouncePayload;
import github.xevira.groves.network.UpdateVelocityPayload;
import github.xevira.groves.util.EnchantHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("deprecation")
@Mixin(LivingEntity.class)
@Debug(export = true)
public abstract class LivingEntityMixin extends Entity {

    @Unique
    private int mixinAir;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

    @Shadow
    public abstract StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect);

    @Shadow
    public abstract float getJumpVelocity();

    @Inject(method = "baseTick()V", at = @At("HEAD"))
    private void baseTickPre(CallbackInfo cb)
    {
        this.mixinAir = this.getAir();
    }

    @Inject(method = "baseTick()V", at = @At("TAIL"))
    private void baseTickPost(CallbackInfo cb)
    {
        if (this.hasStatusEffect(Registration.GILLS_STATUS_EFFECT))
        {
            if (this.isAlive() && !this.isInsideWaterOrBubbleColumn()) {
                this.setAir(this.mixinAir - 1);
                if (this.getAir() == -20) {
                    this.setAir(0);
                    this.serverDamage(this.getDamageSources().create(Registration.SUFFOCATION_DAMAGE), 2.0F);
                }
            } else {
                this.setAir(300);
            }
        }
    }

    @Inject(method = "isInvulnerableTo(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void isInvulnerableToMixin(ServerWorld world, DamageSource source, CallbackInfoReturnable<Boolean> cir)
    {
        // While the damage source is a lightning bolt
        if (source.isOf(DamageTypes.LIGHTNING_BOLT))
        {
            if ((LivingEntity)(Object)this instanceof PlayerEntity player) {
                ItemStack stack = player.getEquippedStack(EquipmentSlot.MAINHAND);
                // ... and you are wielding a mace
                if (stack != null && stack.isOf(Items.MACE)) {
                    // ... that has the Thundering enchant on it,
                    if (EnchantHelper.getEnchantLevel(stack, Registration.THUNDERING_ENCHANTMENT_KEY) > 0) {
                        // you are immune to it, due to having to be right next to the lightning bolt if used.
                        cir.setReturnValue(true);
                    }
                }
            }
        }
        else if (source.isOf(DamageTypes.WITHER))
        {
            if (hasStatusEffect(Registration.RADIANCE_STATUS_EFFECT))
            {
                cir.setReturnValue(true);
            }
        }
        else if (source.isOf(DamageTypes.FREEZE))
        {
            if (hasStatusEffect(Registration.WARMING_STATUS_EFFECT))
            {
                cir.setReturnValue(true);
            }
        }
        // These fire types are separate in case other status effects wish to handle specific types, like lava immunity but not fire in general.
        else if (source.isOf(DamageTypes.IN_FIRE))
        {
            if( hasStatusEffect(Registration.INFERNO_STATUS_EFFECT))
            {
                cir.setReturnValue(true);
            }
        }
        else if (source.isOf(DamageTypes.ON_FIRE))
        {
            if( hasStatusEffect(Registration.INFERNO_STATUS_EFFECT))
            {
                cir.setReturnValue(true);
            }
        }
        else if (source.isOf(DamageTypes.LAVA))
        {
            if( hasStatusEffect(Registration.INFERNO_STATUS_EFFECT))
            {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method="getJumpBoostVelocityModifier()F", at=@At("RETURN"), cancellable = true)
    private void getJumpBoostVelocityModifierMixin(CallbackInfoReturnable<Float> cir)
    {
        StatusEffectInstance gravity = getStatusEffect(Registration.GRAVITY_STATUS_EFFECT);
        if (gravity != null)
        {
            cir.setReturnValue(cir.getReturnValue() - 0.1F * (gravity.getAmplifier()+1));
        }
    }

    @Inject(method = "handleFallDamage(FFLnet/minecraft/entity/damage/DamageSource;)Z", at = @At("HEAD"), cancellable = true)
    private void handleFallDamageMixin(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) LocalFloatRef distance, @Local(ordinal = 1) LocalFloatRef multiplier)
    {
        // Handle gravity
        StatusEffectInstance gravity = getStatusEffect(Registration.GRAVITY_STATUS_EFFECT);
        if (gravity != null)
        {
            multiplier.set(multiplier.get() * (1.5f + 0.5f * gravity.getAmplifier()));
        }

        // Handle bouncy
        StatusEffectInstance bouncy = getStatusEffect(Registration.BOUNCY_STATUS_EFFECT);
        if (bouncy != null) {
            World world = this.getWorld();

            Groves.LOGGER.info("Bouncy: {}, {}, {}", world.isClient, this.isSneaking(), distance.get());

            if (!this.isSneaking() && distance.get() > 2.0f) {
                multiplier.set(0.0f);
                distance.set(0.0f);
                this.fallDistance = 0;  // Reset fall distance

                if (world.isClient) {
                    Vec3d motion = this.getVelocity();
                    this.setVelocity(motion.x, -0.9 * motion.y, motion.z);
                    this.setOnGround(false);

                    Groves.LOGGER.info("Bouncy: {}, {}", this.getVelocity(), this.isOnGround());

//                    ClientPlayNetworking.send(new BouncePayload());
                }

                if (world instanceof ServerWorld serverWorld)
                {
                    for(ServerPlayerEntity player : serverWorld.getPlayers())
                    {
                        ServerPlayNetworking.send(player, new BouncePayload(this.getId(), this.getVelocity()));
                    }
                }

                this.playSound(Registration.MOB_EFFECT_BOUNCY_BOUNCE_SOUND, 1f, 1f);
                BouncingHandler.addHandler((LivingEntity) (Object) this, -0.9 * this.getVelocity().y);
            } else if (!world.isClient && this.isSneaking())
                multiplier.set(multiplier.get() * 0.2f);
        }

    }

}
