package tfar.ae2wtlib.wpt;

import appeng.container.ContainerLocator;
import appeng.core.AEConfig;
import tfar.ae2wtlib.AE2WirelessCraftingTerminals;
import tfar.ae2wtlib.terminal.IInfinityBoosterCardHolder;
import tfar.ae2wtlib.terminal.ItemWT;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;

public class ItemWPT extends ItemWT implements IInfinityBoosterCardHolder {

    public ItemWPT() {
        super(AEConfig.instance().getWirelessTerminalBattery(), new FabricItemSettings().group(AE2WirelessCraftingTerminals.ITEM_GROUP).maxCount(1));
    }

    @Override
    public void open(final PlayerEntity player, final ContainerLocator locator) {
        WPTContainer.open(player, locator);
    }
}