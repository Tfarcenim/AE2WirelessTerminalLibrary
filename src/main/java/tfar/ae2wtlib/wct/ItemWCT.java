package tfar.ae2wtlib.wct;

import appeng.container.ContainerLocator;
import appeng.core.AEConfig;
import tfar.ae2wtlib.AE2WirelessCraftingTerminals;
import tfar.ae2wtlib.terminal.IInfinityBoosterCardHolder;
import tfar.ae2wtlib.terminal.ItemWT;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class ItemWCT extends ItemWT implements IInfinityBoosterCardHolder {

    public ItemWCT() {
        super(AEConfig.instance().getWirelessTerminalBattery(), new FabricItemSettings().group(AE2WirelessCraftingTerminals.ITEM_GROUP).maxCount(1));
    }

    @Override
    public void open(final PlayerEntity player, final ContainerLocator locator) {
        WCTContainer.open(player, locator);
    }

    @Override
    public boolean canHandle(ItemStack is) {
        return is.getItem() instanceof ItemWCT;
    }
}