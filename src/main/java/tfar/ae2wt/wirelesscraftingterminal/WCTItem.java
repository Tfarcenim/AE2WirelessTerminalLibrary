package tfar.ae2wt.wirelesscraftingterminal;

import appeng.container.ContainerLocator;
import appeng.core.AEConfig;
import net.minecraft.item.Item;
import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.terminal.IInfinityBoosterCardHolder;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class WCTItem extends AbstractWirelessTerminalItem implements IInfinityBoosterCardHolder {

    public WCTItem() {
        super(AEConfig.instance().getWirelessTerminalBattery(), new Item.Properties().group(AE2WirelessTerminals.ITEM_GROUP).maxStackSize(1));
    }

    @Override
    public void open(final PlayerEntity player, final ContainerLocator locator) {
        WirelessCraftingTerminalContainer.openServer(player, locator);
    }

    @Override
    public boolean canHandle(ItemStack is) {
        return is.getItem() instanceof WCTItem;
    }

}