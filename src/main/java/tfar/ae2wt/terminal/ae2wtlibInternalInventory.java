package tfar.ae2wt.terminal;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.IAEAppEngInventory;
import net.minecraft.item.ItemStack;

public class ae2wtlibInternalInventory extends AppEngInternalInventory {

    private final ItemStack terminal;
    private final String identifier;

    public ae2wtlibInternalInventory(IAEAppEngInventory inventory, int size, String identifier, ItemStack terminal) {
        super(inventory, size);
        this.terminal = terminal;
        this.identifier = identifier;
        for(int slot = 0; slot < size; slot++) super.setStackInSlot(slot, ItemWT.getSavedSlot(terminal, identifier + slot));
    }

    @Override
    public void setStackInSlot(int slot, ItemStack to) {
        super.setStackInSlot(slot, to);
        ItemWT.setSavedSlot(terminal, to, identifier + slot);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        //todo is this correct?
        ItemWT.setSavedSlot(terminal, getStackInSlot(slot), identifier + slot);
    }
}