package github.xevira.groves.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface OwnableBlockEntity {
    String OWNER_NBT = "ownerUUID";

    void setOwner(UUID owner);

    @Nullable UUID getOwner();

    boolean isOwner(@NotNull PlayerEntity player);

    boolean canInteract(@NotNull PlayerEntity player);

    boolean canBreak(@NotNull PlayerEntity player);

    default void writeOwnerNBT(UUID owner, NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
    {
        if (owner != null)
            nbt.putUuid(OWNER_NBT, owner);
    }

    default @Nullable UUID readOwnerNBT(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
    {
        if (nbt.containsUuid(OWNER_NBT))
            return nbt.getUuid(OWNER_NBT);

        return null;
    }
}
