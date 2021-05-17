package tfar.ae2wt.wpt;

import appeng.container.ContainerLocator;
import appeng.core.AEConfig;
import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.terminal.IInfinityBoosterCardHolder;
import tfar.ae2wt.terminal.ItemWT;
import net.minecraft.entity.player.PlayerEntity;

public class ItemWPT extends ItemWT implements IInfinityBoosterCardHolder {

    public ItemWPT() {
        super(AEConfig.instance().getWirelessTerminalBattery(), new Properties().group(AE2WirelessTerminals.ITEM_GROUP).maxStackSize(1));
    }

    @Override
    public void open(final PlayerEntity player, final ContainerLocator locator) {
        WPatternTContainer.openServer(player, locator);
    }
}