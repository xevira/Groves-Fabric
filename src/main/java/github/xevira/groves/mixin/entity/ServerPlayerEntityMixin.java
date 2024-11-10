package github.xevira.groves.mixin.entity;

import com.mojang.authlib.GameProfile;
import github.xevira.groves.Registration;
import github.xevira.groves.util.EnchantHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity {


    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "isInvulnerableTo(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void isInvulnerableToMixin(ServerWorld world, DamageSource source, CallbackInfoReturnable<Boolean> clr)
    {
        // While the damage source is a lightning bolt
        if (source.isOf(DamageTypes.LIGHTNING_BOLT))
        {
            ItemStack stack = this.getEquippedStack(EquipmentSlot.MAINHAND);
            // ... and you are wielding a mace
            if (stack != null && stack.isOf(Items.MACE))
            {
                // ... that has the Thundering enchant on it,
                if (EnchantHelper.getEnchantLevel(stack, Registration.THUNDERING_ENCHANTMENT_KEY) > 0)
                {
                    // you are immune to it, due to having to be right next to the lightning bolt if used.
                    clr.setReturnValue(true);
                }
            }
        }
    }
}
