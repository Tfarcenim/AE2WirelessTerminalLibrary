package tfar.ae2wtlib.terminal;

import appeng.items.storage.ViewCellItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
//todo
public class FixedViewCellInventory implements IItemHandlerModifiable {

    private static final int viewCellCount = 5;
    private final ItemStack hostStack;

    public FixedViewCellInventory(ItemStack host) {
        hostStack = host;
    }

    @Override
    public int getSlots() {
        return 5;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        if(i < viewCellCount) return ItemWT.getSavedSlot(hostStack, "viewCell" + i);
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isItemValid(int i, ItemStack itemStack) {
        return i < viewCellCount && (itemStack.getItem() instanceof ViewCellItem || itemStack.isEmpty());
    }

    @Override
    public void setStackInSlot(int i, ItemStack itemStack) {
        if(isItemValid(i, itemStack)) {
            ItemWT.setSavedSlot(hostStack, itemStack, "viewCell" + i);
        }
    }

    /*public ItemStack[] getViewCells() {
        ItemStack[] viewCells = new ItemStack[viewCellCount];
        for(int i = 0; i < viewCellCount; i++) {
            viewCells[i] = getInvStack(i);
        }
        return viewCells;
    }*/

    @Override
    public ItemStack extractItem(int slot,int maxCount,boolean simulate) {
        if(slot > viewCellCount) return ItemStack.EMPTY;
        ItemStack is = getStackInSlot(slot);
        if(!simulate) setStackInSlot(slot, ItemStack.EMPTY);
        return is;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }
}