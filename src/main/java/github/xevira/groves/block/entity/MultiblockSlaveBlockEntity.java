package github.xevira.groves.block.entity;

import github.xevira.groves.Groves;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract class MultiblockSlaveBlockEntity extends BlockEntity {
    private BlockPos master;
    private boolean decorative;

    private BlockState originalState;

    public MultiblockSlaveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        this.master = null;
        this.decorative = false;
        this.originalState = null;
    }

    public final boolean isDecorative()
    {
        return this.decorative;
    }

    public void setOriginalState(BlockState state)
    {
        this.originalState = state;
    }

    public void setMaster(@Nullable MultiblockMasterBlockEntity blockEntity)
    {
        if (blockEntity != null) {
            this.master = blockEntity.getPos().mutableCopy();
            this.decorative = blockEntity.isFormed();
        } else {
            this.master = null;
            this.decorative = true;
        }
    }

    public final @Nullable BlockPos getMaster()
    {
        return this.master;
    }

    public final @Nullable BlockState getOriginalState()
    {
        return this.originalState;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        if (this.master != null)
            nbt.putLong("multiblockMaster", this.master.asLong());

        nbt.putBoolean("decorative", this.decorative);

        if (this.originalState != null)
            nbt.put("originalState", NbtHelper.fromBlockState(this.originalState));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        RegistryEntryLookup<Block> registryEntryLookup = (this.world != null
                ? this.world.createCommandRegistryWrapper(RegistryKeys.BLOCK)
                : Registries.BLOCK.getReadOnlyWrapper());

        if (nbt.contains("multiblockMaster", NbtElement.LONG_TYPE))
            this.master = BlockPos.fromLong(nbt.getLong("multiblockMaster"));
        else
            this.master = null;

        if (nbt.contains("decorative", NbtElement.BYTE_TYPE))
            this.decorative = nbt.getBoolean("decorative");
        else
            this.decorative = false;

        if (nbt.contains("originalState", NbtElement.COMPOUND_TYPE))
            this.originalState = NbtHelper.toBlockState(registryEntryLookup, nbt.getCompound("originalState"));
        else
            this.originalState = null;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = super.toInitialChunkDataNbt(registryLookup);
        writeNbt(nbt, registryLookup);
        return nbt;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

}
