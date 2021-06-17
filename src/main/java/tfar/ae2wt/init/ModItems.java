package tfar.ae2wt.init;

import appeng.core.AEConfig;
import net.minecraft.item.Item;
import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.terminal.ItemInfinityBooster;
import tfar.ae2wt.wirelesscraftingterminal.WCTItem;
import tfar.ae2wt.wirelesscraftingterminal.magnet_card.ItemMagnetCard;
import tfar.ae2wt.wirelessfluidterminal.WirelessFluidTerminalItem;
import tfar.ae2wt.wirelessinterfaceterminal.WITItem;
import tfar.ae2wt.wpt.WPTItem;
import tfar.ae2wt.wut.WUTItem;

public class ModItems {
    public static final WCTItem CRAFTING_TERMINAL = new WCTItem();
    public static final WPTItem PATTERN_TERMINAL = new WPTItem();
    public static final WITItem INTERFACE_TERMINAL = new WITItem();
    public static final WUTItem UNIVERSAL_TERMINAL = new WUTItem();
    public static final WirelessFluidTerminalItem WIRELESS_FLUID_TERMINAL = new WirelessFluidTerminalItem(AEConfig.instance().getWirelessTerminalBattery(),
            new Item.Properties().group(AE2WirelessTerminals.ITEM_GROUP).maxStackSize(1));
    public static final ItemInfinityBooster INFINITY_BOOSTER_CARD = new ItemInfinityBooster();
    public static final ItemMagnetCard MAGNET_CARD = new ItemMagnetCard(new Item.Properties().group(AE2WirelessTerminals.ITEM_GROUP).maxStackSize(1));
}
