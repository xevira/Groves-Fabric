package github.xevira.groves.screenhandler;

import github.xevira.groves.Registration;
import github.xevira.groves.block.entity.MoonwellMultiblockMasterBlockEntity;
import github.xevira.groves.network.BlockPosPayload;
import github.xevira.groves.network.MoonwellScreenPayload;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MoonwellScreenHandler extends ScreenHandler {
    private final MoonwellMultiblockMasterBlockEntity blockEntity;
    private final ScreenHandlerContext context;
    private final PlayerEntity player;
    private final PropertyDelegate propertyDelegate;
    //private final RecipeInputInventory input = new CraftingInventory(this, 3, 3);
    //private final CraftingResultInventory result = new CraftingResultInventory();

    private final SimpleInventory input = new SimpleInventory(9);
    private final SimpleInventory result = new SimpleInventory(1);

    @SuppressWarnings("DataFlowIssue")
    public MoonwellScreenHandler(int syncId, PlayerInventory playerInventory, MoonwellScreenPayload payload)
    {
        this(syncId, playerInventory, (MoonwellMultiblockMasterBlockEntity) playerInventory.player.getWorld().getBlockEntity(payload.pos()), new MoonwellPropertyDelegate(payload.day(), payload.phase()));
    }

    public MoonwellScreenHandler(int syncId, PlayerInventory playerInventory, MoonwellMultiblockMasterBlockEntity blockEntity, PropertyDelegate propertyDelegate) {
        super(Registration.MOONWELL_SCREEN_HANDLER, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());
        this.player = playerInventory.player;

        this.propertyDelegate = propertyDelegate;
        this.addProperties(propertyDelegate);

        // Output - 101,35 x 16,16
        this.addSlot(new CraftingResultSlot(playerInventory.player, this.input, this.result, 0, 101, 35));

        // Crafting Grid - 12,17 x 16,16 x 3x3, +18,+18
        addCraftingGrid(12, 17);

        // Inventory - 12, 84 (height - 85) x 16,16, 9x3, +18,+18
        addPlayerInventory(playerInventory, 12, 84);

        // Hotbar - 12, 142 (height - 24) x 16, 16
        addPlayerHotbar(playerInventory, 12, 142);
    }

    private void addCraftingGrid(int x, int y)
    {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.addSlot(new Slot(this.input, j + i * 3, x + j * 18, y + i * 18));
            }
        }
    }

    private void addPlayerInventory(PlayerInventory playerInv, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInv, 9 + (column + (row * 9)), x + (column * 18), y + (row * 18)));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInv, int x, int y) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInv, column, x + (column * 18), y));
        }
    }

    // WIP
    protected static void updateResult(ScreenHandler handler,
           World world,
           PlayerEntity player,
           RecipeInputInventory craftingInventory,
           CraftingResultInventory resultInventory,
           @Nullable RecipeEntry<CraftingRecipe> recipe)
    {
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        // Copied from the CraftingScreenHandler

        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot == 0) {
                this.context.run((world, pos) -> itemStack2.getItem().onCraftByPlayer(itemStack2, world, player));
                if (!this.insertItem(itemStack2, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }

                slot2.onQuickTransfer(itemStack2, itemStack);
            } else if (slot >= 10 && slot < 46) {
                if (!this.insertItem(itemStack2, 1, 10, false)) {
                    if (slot < 37) {
                        if (!this.insertItem(itemStack2, 37, 46, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.insertItem(itemStack2, 10, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.insertItem(itemStack2, 10, 46, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot2.onTakeItem(player, itemStack2);
            if (slot == 0) {
                player.dropItem(itemStack2, false);
            }
        }

        return itemStack;
    }

    private static boolean canUseMoonwell(ScreenHandlerContext context, PlayerEntity player)
    {
        return context.get((world, pos) -> world.getBlockState(pos).isIn(Registration.MOONWELL_INTERACTION_BLOCKS) && player.canInteractWithBlockAt(pos, 4.0), true);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        // TODO: Add checks for ownership of the moonwell

        return canUseMoonwell(this.context, player);
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.result && super.canInsertIntoSlot(stack, slot);
    }

    // Properties
    public boolean isDay()
    {
        return this.propertyDelegate.get(0) != 0;
    }

    public int getMoonPhase()
    {
        return this.propertyDelegate.get(1);
    }

    public SingleFluidStorage getFluidStorage()
    {
        return this.blockEntity.getFluidStorage();
    }

    public boolean hasRecipe()
    {
        return !this.result.isEmpty();
    }

    public MoonwellMultiblockMasterBlockEntity getBlockEntity()
    {
        return this.blockEntity;
    }


    protected static class _CraftingResultInventory implements Inventory
    {
        private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(1, ItemStack.EMPTY);

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack itemStack : this.stacks) {
                if (!itemStack.isEmpty()) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public ItemStack getStack(int slot) {
            return this.stacks.get(0);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            return Inventories.removeStack(this.stacks, 0);
        }

        @Override
        public ItemStack removeStack(int slot) {
            return Inventories.removeStack(this.stacks, 0);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            this.stacks.set(0, stack);
        }

        @Override
        public void markDirty() {
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return true;
        }

        @Override
        public void clear() {
            this.stacks.clear();
        }
    }

    protected static class CraftingResultSlot extends Slot
    {
        private final Inventory input;
        private final PlayerEntity player;
        private int amount;

        public CraftingResultSlot(PlayerEntity player, Inventory input, Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
            this.player = player;
            this.input = input;
        }


        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack takeStack(int amount) {
            if (this.hasStack()) {
                this.amount = this.amount + Math.min(amount, this.getStack().getCount());
            }

            return super.takeStack(amount);
        }

        @Override
        protected void onCrafted(ItemStack stack, int amount) {
            this.amount += amount;
            this.onCrafted(stack);
        }

        @Override
        protected void onTake(int amount) {
            this.amount += amount;
        }

        @Override
        protected void onCrafted(ItemStack stack) {
            if (this.amount > 0) {
                stack.onCraftByPlayer(this.player.getWorld(), this.player, this.amount);
            }

//            if (this.inventory instanceof RecipeUnlocker recipeUnlocker) {
//                recipeUnlocker.unlockLastRecipe(this.player, this.input.getHeldStacks());
//            }

            this.amount = 0;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            this.onCrafted(stack);
//            CraftingRecipeInput.Positioned positioned = this.input.createPositionedRecipeInput();
//            CraftingRecipeInput craftingRecipeInput = positioned.input();
            //DefaultedList<ItemStack> defaultedList = player.getWorld().getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, craftingRecipeInput, player.getWorld());

            for (int k = 0; k < 3; k++) {
                for (int l = 0; l < 3; l++) {
                    int m = l + k * 3;
                    ItemStack itemStack = this.input.getStack(m);
                    ItemStack itemStack2 = ItemStack.EMPTY;//defaultedList.get(m);
                    if (!itemStack.isEmpty()) {
                        this.input.removeStack(m, 1);
                        itemStack = this.input.getStack(m);
                    }

                    if (!itemStack2.isEmpty()) {
                        if (itemStack.isEmpty()) {
                            this.input.setStack(m, itemStack2);
                        } else if (ItemStack.areItemsAndComponentsEqual(itemStack, itemStack2)) {
                            itemStack2.increment(itemStack.getCount());
                            this.input.setStack(m, itemStack2);
                        } else if (!this.player.getInventory().insertStack(itemStack2)) {
                            this.player.dropItem(itemStack2, false);
                        }
                    }
                }
            }
        }

        @Override
        public boolean disablesDynamicDisplay() {
            return true;
        }
    }

    public static class MoonwellPropertyDelegate implements PropertyDelegate {
        private int[] data = new int[2];

        public MoonwellPropertyDelegate(boolean day, int phase)
        {
            data[0] = day ? 1 : 0;
            data[1] = phase;
        }

        @Override
        public int get(int index) {
            return data[index];
        }

        @Override
        public void set(int index, int value) {
            data[index] = value;
        }

        @Override
        public int size() {
            return 2;
        }
    }
}
