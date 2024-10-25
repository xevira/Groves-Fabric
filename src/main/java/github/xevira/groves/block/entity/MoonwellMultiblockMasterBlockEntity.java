package github.xevira.groves.block.entity;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.util.ServerTickableBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public class MoonwellMultiblockMasterBlockEntity extends MultiblockMasterBlockEntity implements ServerTickableBlockEntity {
    private static final int[] PHASE_RATES = new int[] {
            100,            // Full Moon
            75,             // Waning Gibbous
            50,             // Third Quarter
            24,             // Waning Crescent
            0,              // New Moon
            25,             // Waxing Crescent
            50,             // First Quarter
            75              // Waxing Gibbous
    };

    private static final FluidVariant MOON_WATER = FluidVariant.of(Registration.BLESSED_MOON_WATER_FLUID);

    private final SingleFluidStorage fluidStorage = SingleFluidStorage.withFixedCapacity(
            FluidConstants.BUCKET * 10,
            this::markDirty);

    public MoonwellMultiblockMasterBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.MOONWELL_MULTIBLOCK_MASTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void breakMultiblock(boolean brokeMaster) {
        super.breakMultiblock(brokeMaster);

        if (this.world != null)
            world.playSound(null, this.pos, Registration.MOONWELL_DEACTIVATE_SOUND, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    public long getMoonlightAmount()
    {
        return this.fluidStorage.getAmount();
    }

    public long getMoonlightCapacity()
    {
        return this.fluidStorage.getCapacity();
    }

    public int getMoonlightPercent()
    {
        return (int) (100 * this.fluidStorage.getAmount() / this.fluidStorage.getCapacity());
    }

    @Override
    public void serverTick() {
        if (this.world == null) return;

        if (this.world.getDimension().hasFixedTime()) return;
        if (this.world.isDay()) return;

        if (!this.world.isSkyVisible(this.pos.up())) return;

        // Only do this once a second
        if (this.world.getTime() % 20 != 0) return;

        int phase = this.world.getMoonPhase();
        int power = 100 + this.getTotalDecorations() * 2;

        int fill = PHASE_RATES[phase] * power / 100;

        try(Transaction transaction  = Transaction.openOuter()) {
            long inserted = this.fluidStorage.insert(MOON_WATER, fill, transaction);

            if (inserted > 0)
            {
                transaction.commit();
                markDirty();
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        NbtCompound fluidNbt = new NbtCompound();
        this.fluidStorage.writeNbt(fluidNbt, registryLookup);
        nbt.put("fluidStorage", fluidNbt);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.contains("fluidStorage", NbtElement.COMPOUND_TYPE))
            this.fluidStorage.readNbt(nbt.getCompound("fluidStorage"), registryLookup);
    }

    public SingleFluidStorage getFluidStorage()
    {
        return this.fluidStorage;
    }
}
