package github.xevira.groves.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import github.xevira.groves.fluid.FluidSystem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity {
    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/ZombieEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean grovesTick(boolean original) {
        if (original)
            return true;

        return FluidSystem.FLUIDS.values().stream()
                .filter(FluidSystem::willZombiesConvert)
                .anyMatch(fluidData -> isSubmergedIn(fluidData.fluidTag()));
    }
}