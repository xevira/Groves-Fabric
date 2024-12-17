package github.xevira.groves.mixin.world;

import github.xevira.groves.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Mixin(CollisionView.class)
public interface CollisionMixin {

    @Inject(method = "canCollide(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Z", at = @At("HEAD"), cancellable = true)
    private void canCollideMixin(@Nullable Entity entity, Box box, CallbackInfoReturnable<Boolean> cir)
    {
        if (entity instanceof PlayerEntity player && isIntangible(player))
        {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getBlockCollisions", at = @At("HEAD"), cancellable = true)
    private void getBlockCollisionsMixin(Entity entity, Box box, CallbackInfoReturnable<Iterable<VoxelShape>> cir)
    {
        if (entity instanceof PlayerEntity player && isIntangible(player))
        {
            Iterable<VoxelShape> original = getOriginalBlockCollisions(entity, box);

            List<VoxelShape> filteredBlockCollisions = StreamSupport.stream(original.spliterator(), false)
                    .filter(shape -> isVerticalCollision(shape, box, player))
                    .toList();

            cir.setReturnValue(filteredBlockCollisions);
        }
    }

    default Iterable<VoxelShape> getOriginalBlockCollisions(Entity entity, Box box)
    {
        return () -> new BlockCollisionSpliterator<>((CollisionView)this, entity, box, false, (pos, voxelShape) -> voxelShape);
    }

    default boolean isVerticalCollision(VoxelShape shape, Box box, PlayerEntity player)
    {
        World world = player.getWorld();
        BlockPos blockPos = new BlockPos((int) shape.getMin(Direction.Axis.X), (int) shape.getMin(Direction.Axis.Y), (int) shape.getMin(Direction.Axis.Z));
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getHardness(world, blockPos) < 0)
            return true;

        double maxY = shape.getMax(Direction.Axis.Y);
        double minY = box.minY;

        return Math.abs(maxY - minY) < 0.75;


    }

    default boolean isIntangible(PlayerEntity player)
    {
        return player.getAttributeValue(Registration.INTANGIBLE_ATTRIBUTE) > 0.0;
    }
}
