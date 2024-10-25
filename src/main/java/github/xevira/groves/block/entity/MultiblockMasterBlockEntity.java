package github.xevira.groves.block.entity;

import github.xevira.groves.Groves;
import github.xevira.groves.util.BlockStateHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MultiblockMasterBlockEntity extends BlockEntity {
    private final List<BlockPos> slaves = new ArrayList<>();
    private final List<BlockPos> decorations = new ArrayList<>();
    private boolean multiblockFormed;
    private @Nullable BlockState originalState;

    public MultiblockMasterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        this.multiblockFormed = false;
        this.originalState = null;
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.world != null)
            this.world.updateListeners(this.pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

    public void markFormed()
    {
        this.multiblockFormed = true;
    }

    public final boolean isFormed()
    {
        return this.multiblockFormed;
    }

    public final int getTotalDecorations()
    {
        return this.decorations.size();
    }

    public void addSlave(MultiblockSlaveBlockEntity blockEntity, BlockState originalState)
    {
        if (this.multiblockFormed)
            this.decorations.add(blockEntity.getPos());
        else
            this.slaves.add(blockEntity.getPos());
        blockEntity.setMaster(this);
        blockEntity.setOriginalState(originalState);
    }

    public final List<BlockPos> getSlaves()
    {
        return this.slaves;
    }

    public final List<BlockPos> getDecorations()
    {
        return this.decorations;
    }

    public void setOriginalState(BlockState state)
    {
        this.originalState = state;
    }

    public void removeDecoration(BlockPos decorPos)
    {
        this.decorations.remove(decorPos);
        markDirty();
    }

    public void breakSlaveBlock(BlockPos slavePos)
    {
        assert this.world != null;

        if (this.world.getBlockEntity(slavePos) instanceof MultiblockSlaveBlockEntity slaveBlockEntity)
        {
            slaveBlockEntity.setMaster(null);   // Detach

            BlockState origState = slaveBlockEntity.getOriginalState();
            BlockState state = this.world.getBlockState(slavePos);

            if (origState != null) {
                this.world.setBlockState(slavePos, origState.getBlock().getStateWithProperties(state));
            }
        }
    }

    public void breakMultiblock(boolean brokeMaster)
    {
        if(this.world == null) return;

        this.slaves.forEach(this::breakSlaveBlock);
        this.decorations.forEach(this::breakSlaveBlock);

        if (!brokeMaster && this.originalState != null)
        {
//            Groves.LOGGER.info("breakMultiblock: Master {} -> {}", this.pos, this.originalState);
            this.world.setBlockState(this.pos, this.originalState);
        }
    }

    private void writeMultiblockNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
    {
        NbtCompound multiblock = new NbtCompound();

        if (!this.slaves.isEmpty())
            multiblock.putLongArray("slaves", this.slaves.stream().map(BlockPos::asLong).toList());

        if (!this.decorations.isEmpty())
            multiblock.putLongArray("decorations", this.decorations.stream().map(BlockPos::asLong).toList());

        if (this.originalState != null)
            multiblock.put("originalState", NbtHelper.fromBlockState(this.originalState));

        multiblock.putBoolean("formed", this.multiblockFormed);

        nbt.put("multiblock", multiblock);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        writeMultiblockNbt(nbt, registryLookup);
    }

    private void readMultiblockNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

        if (nbt.contains("multiblock", NbtElement.COMPOUND_TYPE))
        {
            NbtCompound multiblock = nbt.getCompound("multiblock");

            RegistryEntryLookup<Block> registryEntryLookup = (this.world != null
                    ? this.world.createCommandRegistryWrapper(RegistryKeys.BLOCK)
                    : Registries.BLOCK.getReadOnlyWrapper());

            this.slaves.clear();
            if (multiblock.contains("slaves", NbtElement.LONG_ARRAY_TYPE))
                Arrays.stream(multiblock.getLongArray("slaves")).mapToObj(BlockPos::fromLong).forEachOrdered(this.slaves::add);

            this.decorations.clear();
            if (multiblock.contains("decorations", NbtElement.LONG_ARRAY_TYPE))
                Arrays.stream(multiblock.getLongArray("decorations")).mapToObj(BlockPos::fromLong).forEachOrdered(this.slaves::add);

            if (multiblock.contains("originalState", NbtElement.COMPOUND_TYPE))
                this.originalState = NbtHelper.toBlockState(registryEntryLookup, multiblock.getCompound("originalState"));
            else
                this.originalState = null;

            if (multiblock.contains("formed", NbtElement.BYTE_TYPE))
                this.multiblockFormed = multiblock.getBoolean("formed");
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        readMultiblockNbt(nbt, registryLookup);
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
