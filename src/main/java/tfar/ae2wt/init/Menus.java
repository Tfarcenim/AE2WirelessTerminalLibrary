package tfar.ae2wt.init;

import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.core.Api;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.WTConfig;
import tfar.ae2wt.wirelesscraftingterminal.WirelessCraftingTerminalContainer;
import tfar.ae2wt.wirelessfluidterminal.WirelessFluidTerminalContainer;
import tfar.ae2wt.wirelessinterfaceterminal.WirelessInterfaceTerminalContainer;
import tfar.ae2wt.wpt.WirelessPatternTerminalContainer;
import tfar.ae2wt.wut.WUTHandler;

public class Menus {
    public static ContainerType<WirelessInterfaceTerminalContainer> WIT = IForgeContainerType.create((int windowId1, PlayerInventory inv1, PacketBuffer inv12) -> WirelessInterfaceTerminalContainer.openClient(windowId1, inv1));
    public static ContainerType<WirelessCraftingTerminalContainer> WCT = new ContainerType<>(WirelessCraftingTerminalContainer::openClient);
    public static ContainerType<WirelessPatternTerminalContainer> PATTERN = IForgeContainerType.create((windowId1, inv1, buf1) -> WirelessPatternTerminalContainer.openClient(windowId1, inv1));
    public static ContainerType<WirelessFluidTerminalContainer> WIRELESS_FLUID_TERMINAL = new ContainerType<>(WirelessFluidTerminalContainer::openClient);

    public static void menus(RegistryEvent.Register<ContainerType<?>> e) {
        AE2WirelessTerminals.register("wireless_crafting_terminal",WCT,e.getRegistry());
        AE2WirelessTerminals.register( "wireless_pattern_terminal",PATTERN ,e.getRegistry());
        AE2WirelessTerminals.register( "wireless_interface_terminal",WIT,e.getRegistry());
        AE2WirelessTerminals.register( "wireless_fluid_terminal", WIRELESS_FLUID_TERMINAL,e.getRegistry());

        WUTHandler.addTerminal("crafting", ModItems.CRAFTING_TERMINAL::open);
        WUTHandler.addTerminal("pattern", ModItems.PATTERN_TERMINAL::open);
        WUTHandler.addTerminal("interface", ModItems.INTERFACE_TERMINAL::open);

        ContainerOpener.addOpener(WCT, (new CheckedOpener(WirelessCraftingTerminalContainer::openServer))::open);
        ContainerOpener.addOpener(PATTERN, (new CheckedOpener(WirelessPatternTerminalContainer::openServer))::open);
        ContainerOpener.addOpener(WIT, (new CheckedOpener(WirelessInterfaceTerminalContainer::openServer))::open);
        ContainerOpener.addOpener(WIRELESS_FLUID_TERMINAL, (new CheckedOpener(WirelessFluidTerminalContainer::openServer))::open);

        Api.instance().registries().charger().addChargeRate(ModItems.CRAFTING_TERMINAL, WTConfig.getChargeRate());
        Api.instance().registries().charger().addChargeRate(ModItems.PATTERN_TERMINAL, WTConfig.getChargeRate());
        Api.instance().registries().charger().addChargeRate(ModItems.INTERFACE_TERMINAL, WTConfig.getChargeRate());
        Api.instance().registries().charger().addChargeRate(ModItems.UNIVERSAL_TERMINAL, WTConfig.getChargeRate() * WTConfig.WUTChargeRateMultiplier());
        Api.instance().registries().charger().addChargeRate(ModItems.WIRELESS_FLUID_TERMINAL, WTConfig.getChargeRate());
    }

    private static class CheckedOpener {
        private final UncheckedOpener opener;
        public CheckedOpener(UncheckedOpener opener) {
            this.opener = opener;
        }

        public boolean open(PlayerEntity player, ContainerLocator locator) {
            if (!(player instanceof ServerPlayerEntity)) {
                // Cannot open containers on the client or for non-players
                return false;
            }

            if (!locator.hasItemIndex()) {
                return false;
            }

            opener.open(player, locator);
            return true;
        }
    }

    @FunctionalInterface
    public interface UncheckedOpener {
        void open(PlayerEntity player, ContainerLocator locator);
    }
}
