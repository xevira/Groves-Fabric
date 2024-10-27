package github.xevira.groves.block.entity;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.network.BlockPosPayload;
import github.xevira.groves.network.MoonwellScreenPayload;
import github.xevira.groves.screenhandler.MoonwellScreenHandler;
import github.xevira.groves.util.ServerTickableBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MoonwellMultiblockMasterBlockEntity extends MultiblockMasterBlockEntity implements ExtendedScreenHandlerFactory<MoonwellScreenPayload>, ServerTickableBlockEntity {
    public static final Text TITLE = Groves.text("gui", "moonwell.title");

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

    private static final FluidVariant MOONLIGHT = FluidVariant.of(Registration.MOONLIGHT_FLUID);

    private final SingleFluidStorage fluidStorage = SingleFluidStorage.withFixedCapacity(
            FluidConstants.BUCKET * 10,
            this::markDirty);

    private boolean was_day = false;
    private int last_phase = 8;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch(index)
            {
                case 0 -> MoonwellMultiblockMasterBlockEntity.this.isDay() ? 1 : 0;
                case 1 -> MoonwellMultiblockMasterBlockEntity.this.getMoonPhase();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {

        }

        @Override
        public int size() {
            return 2;
        }
    };

    public MoonwellMultiblockMasterBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.MOONWELL_MULTIBLOCK_MASTER_BLOCK_ENTITY, pos, state);
    }

    public int getMoonPhase()
    {
        if (this.world == null) return 0;

        if (this.world.isDay()) return -1;

        return this.world.getMoonPhase();
    }

    public boolean isDay()
    {
        if (this.world == null) return false;

        return this.world.isDay();
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
        if (!this.world.isSkyVisible(this.pos.up())) return;

        // Only do this once a second
        if (this.world.getTime() % 20 != 0) return;

        int phase = this.world.getMoonPhase();
        if (!this.world.isDay()) {
            int power = 100 + this.getTotalDecorations() * 2;

            int fill = PHASE_RATES[phase] * power / 100;

            try (Transaction transaction = Transaction.openOuter()) {
                long inserted = this.fluidStorage.insert(MOONLIGHT, fill, transaction);

                if (inserted > 0) {
                    transaction.commit();
                    markDirty();
                }
            }
        }

        if (this.was_day != this.world.isDay() || this.last_phase != phase)
        {
            this.was_day = this.world.isDay();
            this.last_phase = phase;
            markDirty();
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

    @Override
    public MoonwellScreenPayload getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
        return new MoonwellScreenPayload(this.pos, isDay(), getMoonPhase());
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new MoonwellScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);

        this.was_day = world.isDay() && !world.isNight();
    }
}
