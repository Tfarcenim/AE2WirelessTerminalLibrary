package tfar.ae2wtlib.terminal;

import net.minecraftforge.items.IItemHandlerModifiable;
import tfar.ae2wtlib.wct.WCTContainer;
import tfar.ae2wtlib.wct.magnet_card.ItemMagnetCard;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class FixedWTInv implements IItemHandlerModifiable {

    public static final int OFFHAND = 4;
    public static final int TRASH = 5;
    public static final int INFINITY_BOOSTER_CARD = 6;
    public static final int MAGNET_CARD = 7;

    private final PlayerInventory playerInventory;
    private final ItemStack wt;
    private final IWTInvHolder host;

    private static final int slotOffset = 36;
    private static final int offHandSlot = 40;

    public FixedWTInv(PlayerInventory playerInventory, ItemStack wt, IWTInvHolder host) {
        this.playerInventory = playerInventory;
        this.wt = wt;
        this.host = host;
    }

    @Override
    public int getSlots() {
        return 8;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        if(i < 4 && i >= 0) {
            return playerInventory.getStackInSlot(i + slotOffset);
        } else if(i == OFFHAND) return playerInventory.getStackInSlot(offHandSlot);
        else if(i == TRASH && wt.getItem() instanceof ItemWT)
            return ItemWT.getSavedSlot(wt, "trash");
        else if(i == INFINITY_BOOSTER_CARD && wt.getItem() instanceof IInfinityBoosterCardHolder)
            return ((IInfinityBoosterCardHolder) wt.getItem()).getBoosterCard(wt);
        else if(i == MAGNET_CARD && wt.getItem() instanceof ItemWT)
            return ItemWT.getSavedSlot(wt, "magnetCard");
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isItemValid(int i, ItemStack itemStack) {
        if(i == 0) {
            return playerInventory.isItemValidForSlot(slotOffset, itemStack) && itemStack.getItem() instanceof ArmorItem && ((ArmorItem) itemStack.getItem()).getEquipmentSlot().equals(EquipmentSlotType.FEET);
        } else if(i == 1) {
            return playerInventory.isItemValidForSlot(slotOffset + 1, itemStack) && itemStack.getItem() instanceof ArmorItem && ((ArmorItem) itemStack.getItem()).getEquipmentSlot().equals(EquipmentSlotType.LEGS);
        } else if(i == 2) {
            return playerInventory.isItemValidForSlot(slotOffset + 2, itemStack) && itemStack.getItem() instanceof ArmorItem && ((ArmorItem) itemStack.getItem()).getEquipmentSlot().equals(EquipmentSlotType.CHEST);
        } else if(i == 3) {
            return playerInventory.isItemValidForSlot(slotOffset + 3, itemStack) && itemStack.getItem() instanceof ArmorItem && ((ArmorItem) itemStack.getItem()).getEquipmentSlot().equals(EquipmentSlotType.HEAD);
        } else if(i == OFFHAND) return playerInventory.isItemValidForSlot(offHandSlot, itemStack);
        else if(i == TRASH) return true;
        else if(i == INFINITY_BOOSTER_CARD)
            return itemStack.getItem() instanceof ItemInfinityBooster || itemStack.isEmpty();
        else if(i == MAGNET_CARD)
            return itemStack.getItem() instanceof ItemMagnetCard || itemStack.isEmpty();
        return false;
    }

    @Override
    public ItemStack insertItem(int i, ItemStack itemStack, boolean simulation) {
        if(i < 4 && i >= 0) {
            if(!simulation) playerInventory.setInventorySlotContents(i + slotOffset, itemStack);
            return ItemStack.EMPTY;
        } else if(i == OFFHAND) {
            if(!simulation) playerInventory.setInventorySlotContents(offHandSlot, itemStack);
            return ItemStack.EMPTY;
        } else if(i == TRASH) {
            if(!simulation) ItemWT.setSavedSlot(wt, itemStack, "trash");
            return ItemStack.EMPTY;
        } else if(i == INFINITY_BOOSTER_CARD) {
            if(!(itemStack.getItem() instanceof ItemInfinityBooster) && !itemStack.isEmpty())
                return itemStack;
            if(!simulation) {
                ((IInfinityBoosterCardHolder) wt.getItem()).setBoosterCard(wt, itemStack);
            }
            return ItemStack.EMPTY;
        } else if(i == MAGNET_CARD) {
            if(!(itemStack.getItem() instanceof ItemMagnetCard) && !itemStack.isEmpty()) return itemStack;
            if(!simulation) {
                ItemWT.setSavedSlot(wt, itemStack, "magnetCard");
                if(host instanceof WCTContainer) ((WCTContainer) host).reloadMagnetSettings();
            }
            return ItemStack.EMPTY;
        }
        return itemStack;
    }

    @Override
    public ItemStack extractItem(int slot,int maxCount, boolean simulation) {
        if(slot == INFINITY_BOOSTER_CARD) {
            ItemStack boosterCard = ((IInfinityBoosterCardHolder) wt.getItem()).getBoosterCard(wt);
            if(!simulation) ((IInfinityBoosterCardHolder) wt.getItem()).setBoosterCard(wt, ItemStack.EMPTY);
            return boosterCard;
        } else if(slot == MAGNET_CARD) {
            ItemStack magnetCard = ItemWT.getSavedSlot(wt, "magnetCard");
            if(!simulation) {
                ItemWT.setSavedSlot(wt, ItemStack.EMPTY, "magnetCard");
                if(host instanceof WCTContainer) ((WCTContainer) host).reloadMagnetSettings();
            }
            return magnetCard;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {

    }
}