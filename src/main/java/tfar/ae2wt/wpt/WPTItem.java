package tfar.ae2wt.wpt;

import appeng.container.ContainerLocator;
import appeng.core.AEConfig;
import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.terminal.IInfinityBoosterCardHolder;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import net.minecraft.entity.player.PlayerEntity;

public class WPTItem extends AbstractWirelessTerminalItem implements IInfinityBoosterCardHolder {

    public WPTItem() {
        super(AEConfig.instance().getWirelessTerminalBattery(), new Properties().group(AE2WirelessTerminals.ITEM_GROUP).maxStackSize(1));
    }

    @Override
    public void open(final PlayerEntity player, final ContainerLocator locator) {
        WirelessPatternTerminalContainer.openServer(player, locator);
    }
}