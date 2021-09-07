package tfar.ae2wt.wirelessinterfaceterminal;

import appeng.api.features.IWirelessTermHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import tfar.ae2wt.init.Menus;
import tfar.ae2wt.terminal.WTGuiObject;

public class WITGuiObject extends WTGuiObject {
    public WITGuiObject(IWirelessTermHandler wh, ItemStack is, PlayerEntity ep, int inventorySlot) {
        super(wh, is, ep, inventorySlot);
    }

    @Override
    public ContainerType<?> getType() {
        return Menus.WIT;
    }
}
