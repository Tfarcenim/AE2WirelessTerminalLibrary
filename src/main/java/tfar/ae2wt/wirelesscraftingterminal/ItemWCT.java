package tfar.ae2wt.wirelesscraftingterminal;

import appeng.container.ContainerLocator;
import appeng.core.AEConfig;
import net.minecraft.item.Item;
import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.terminal.IInfinityBoosterCardHolder;
import tfar.ae2wt.terminal.ItemWT;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class ItemWCT extends ItemWT implements IInfinityBoosterCardHolder {

    public ItemWCT() {
        super(AEConfig.instance().getWirelessTerminalBattery(), new Item.Properties().group(AE2WirelessTerminals.ITEM_GROUP).maxStackSize(1));
    }

    @Override
    public void open(final PlayerEntity player, final ContainerLocator locator) {
        WCTContainer.open(player, locator);
    }

    @Override
    public boolean canHandle(ItemStack is) {
        return is.getItem() instanceof ItemWCT;
    }


    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}