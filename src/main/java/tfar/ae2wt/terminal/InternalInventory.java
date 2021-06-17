package tfar.ae2wt.terminal;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.IAEAppEngInventory;
import net.minecraft.item.ItemStack;

public class InternalInventory extends AppEngInternalInventory {

    private final ItemStack terminal;
    private final SlotType identifier;

    public InternalInventory(IAEAppEngInventory inventory, int size, SlotType identifier, ItemStack terminal) {
        super(inventory, size);
        this.terminal = terminal;
        this.identifier = identifier;
        for(int slot = 0; slot < size; slot++) super.setStackInSlot(slot, AbstractWirelessTerminalItem.getSavedSlot(terminal, identifier , slot));
    }

    @Override
    public void setStackInSlot(int slot, ItemStack to) {
        super.setStackInSlot(slot, to);
        AbstractWirelessTerminalItem.setSavedSlot(terminal, to, identifier , slot);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        //todo is this correct?
        AbstractWirelessTerminalItem.setSavedSlot(terminal, getStackInSlot(slot), identifier , slot);
    }
}