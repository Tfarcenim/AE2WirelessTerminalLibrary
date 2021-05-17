package appeng.container.implementations;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import tfar.ae2wt.net.TermFactoryStatus;
import tfar.ae2wt.terminal.ItemWT;
import net.minecraft.util.text.ITextComponent;
import tfar.ae2wt.wirelesscraftingterminal.WCTGuiObject;
import tfar.ae2wt.wpt.WPTGuiObject;

public class WirelessCraftingStatusContainer extends CraftingCPUContainer implements CraftingCPUCyclingContainer {
    public static ContainerType<WirelessCraftingStatusContainer> TYPE;

    public static WirelessCraftingStatusContainer openClient(int windowId, PlayerInventory inv) {
        PlayerEntity player = inv.player;
        ItemStack it = inv.player.getHeldItem(Hand.MAIN_HAND);
        ContainerLocator locator = ContainerLocator.forHand(inv.player, Hand.MAIN_HAND);
        WCTGuiObject host = new WCTGuiObject((ItemWT) it.getItem(), it, player, locator.getItemIndex());
        return new WirelessCraftingStatusContainer(windowId, inv, host);
    }

    public static void openServer(PlayerEntity player, ContainerLocator locator) {
        ItemStack it = player.inventory.getStackInSlot(locator.getItemIndex());
        WPTGuiObject accessInterface = new WPTGuiObject((ItemWT) it.getItem(), it, player, locator.getItemIndex());

        if (locator.hasItemIndex()) {
            player.openContainer(new TermFactoryStatus(accessInterface,locator));
        }
    }

    public WirelessCraftingStatusContainer(int id, PlayerInventory ip, ITerminalHost terminalHost) {
        super(TYPE, id, ip, terminalHost);
    }

    private final CraftingCPUCycler cpuCycler = new CraftingCPUCycler(this::cpuMatches, this::onCPUSelectionChanged);

    @GuiSync(6)
    public boolean noCPU = true;

    @GuiSync(7)
    public ITextComponent cpuName;

    @Override
    public void detectAndSendChanges() {
        IGrid network = this.getNetwork();
        if (isServer() && network != null) {
            cpuCycler.detectAndSendChanges(network);
        }

        super.detectAndSendChanges();
    }

    private boolean cpuMatches(final ICraftingCPU c) {
        return c.isBusy();
    }

    private void onCPUSelectionChanged(CraftingCPURecord cpuRecord, boolean cpusAvailable) {
        noCPU = !cpusAvailable;
        if (cpuRecord == null) {
            cpuName = null;
            setCPU(null);
        } else {
            cpuName = cpuRecord.getName();
            setCPU(cpuRecord.getCpu());
        }
    }

    @Override
    public void cycleSelectedCPU(boolean forward) {
        cpuCycler.cycleCpu(forward);
    }
}