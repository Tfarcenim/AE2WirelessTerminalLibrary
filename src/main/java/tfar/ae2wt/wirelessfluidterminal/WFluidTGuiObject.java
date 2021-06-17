package tfar.ae2wt.wirelessfluidterminal;

import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IViewCellStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import tfar.ae2wt.terminal.WTGuiObject;

public class WFluidTGuiObject extends WTGuiObject implements IPortableCell , IViewCellStorage {

    public WFluidTGuiObject(final IWirelessTermHandler wh, final ItemStack is, final PlayerEntity ep, int inventorySlot) {
        super(wh, is, ep, inventorySlot);
    }
}