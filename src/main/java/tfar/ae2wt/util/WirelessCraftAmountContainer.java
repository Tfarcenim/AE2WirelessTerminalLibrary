package tfar.ae2wt.util;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.implementations.WirelessCraftingStatusContainer;
import appeng.container.slot.InaccessibleSlot;
import appeng.me.helpers.PlayerSource;
import appeng.tile.inventory.AppEngInternalInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import tfar.ae2wt.net.TermFactoryStatus;
import tfar.ae2wt.terminal.ItemWT;
import tfar.ae2wt.wirelesscraftingterminal.WCTGuiObject;
import tfar.ae2wt.wpt.WPTGuiObject;

public class WirelessCraftAmountContainer extends AEBaseContainer {

    public static ContainerType<WirelessCraftAmountContainer> TYPE;

    private final Slot craftingItem;
    private IAEItemStack itemToCreate;

    public WirelessCraftAmountContainer(int id, PlayerInventory ip, final ITerminalHost te) {
        super(TYPE, id, ip, te);

        craftingItem = new InaccessibleSlot(new AppEngInternalInventory(null, 1), 0, 34, 53);
        addSlot(getCraftingItem());
    }

    public static WirelessCraftAmountContainer openClient(int windowId, PlayerInventory inv) {
        PlayerEntity player = inv.player;
        ItemStack it = inv.player.getHeldItem(Hand.MAIN_HAND);
        ContainerLocator locator = ContainerLocator.forHand(inv.player, Hand.MAIN_HAND);
        WCTGuiObject host = new WCTGuiObject((ItemWT) it.getItem(), it, player, locator.getItemIndex());
        return new WirelessCraftAmountContainer(windowId, inv, host);
    }

    public static boolean openServer(PlayerEntity player, ContainerLocator locator) {
        ItemStack it = player.inventory.getStackInSlot(locator.getItemIndex());
        WCTGuiObject accessInterface = new WCTGuiObject((ItemWT) it.getItem(), it, player, locator.getItemIndex());

        if (locator.hasItemIndex()) {
            player.openContainer(new TermFactory(accessInterface,locator));
        }
        return true;
    }



    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        verifyPermissions(SecurityPermissions.CRAFT, false);
    }

    public IGrid getGrid() {
        final IActionHost h = ((IActionHost) getTarget());
        return h.getActionableNode().getGrid();
    }

    public World getWorld() {
        return getPlayerInv().player.world;
    }

    public IActionSource getActionSrc() {
        return new PlayerSource(getPlayerInv().player, (IActionHost) getTarget());
    }

    public Slot getCraftingItem() {
        return craftingItem;
    }

    public IAEItemStack getItemToCraft() {
        return itemToCreate;
    }

    public void setItemToCraft(final IAEItemStack itemToCreate) {
        this.itemToCreate = itemToCreate;
    }
}