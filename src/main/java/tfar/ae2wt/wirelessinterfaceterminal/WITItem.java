package tfar.ae2wt.wirelessinterfaceterminal;

import appeng.container.ContainerLocator;
import appeng.core.AEConfig;
import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.terminal.IInfinityBoosterCardHolder;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import net.minecraft.entity.player.PlayerEntity;
import tfar.ae2wt.wirelessfluidterminal.WirelessFluidTerminalContainer;

public class WITItem extends AbstractWirelessTerminalItem implements IInfinityBoosterCardHolder {

    public WITItem() {
        super(AEConfig.instance().getWirelessTerminalBattery(), new Properties().group(AE2WirelessTerminals.ITEM_GROUP).maxStackSize(1));
    }

    @Override
    public void open(final PlayerEntity player, final ContainerLocator locator) {
        WirelessInterfaceTerminalContainer.openServer(player, locator);
    }
}