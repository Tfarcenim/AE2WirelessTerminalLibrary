package tfar.ae2wt.wirelessinterfaceterminal;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.client.gui.me.interfaceterminal.InterfaceSlot;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.slot.AppEngSlot;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.InventoryAction;
import appeng.items.misc.EncodedPatternItem;
import appeng.parts.misc.InterfacePart;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.InterfaceTileEntity;
import appeng.util.InventoryAdaptor;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.WrapperRangeItemHandler;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;
import tfar.ae2wt.WTConfig;
import tfar.ae2wt.init.Menus;
import tfar.ae2wt.net.PacketHandler;
import tfar.ae2wt.net.client.S2CInterfaceTerminalPacket;
import tfar.ae2wt.terminal.WTGuiObject;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import tfar.ae2wt.terminal.WTInventoryHandler;
import tfar.ae2wt.wut.WUTItem;

import java.util.HashMap;
import java.util.Map;

public class WirelessInterfaceTerminalContainer extends AEBaseContainer {

    public static WirelessInterfaceTerminalContainer openClient(int windowId, PlayerInventory inv) {
        PlayerEntity player = inv.player;
        ItemStack it = inv.player.getHeldItem(Hand.MAIN_HAND);
        ContainerLocator locator = ContainerLocator.forHand(inv.player, Hand.MAIN_HAND);
        WTGuiObject host = new WTGuiObject((AbstractWirelessTerminalItem) it.getItem(), it, player, locator.getItemIndex());
        return new WirelessInterfaceTerminalContainer(windowId, inv, host);
    }

    public static void openServer(PlayerEntity player, ContainerLocator locator) {

        ItemStack it = player.inventory.getStackInSlot(locator.getItemIndex());
        WTGuiObject accessInterface = new WTGuiObject((AbstractWirelessTerminalItem) it.getItem(), it, player, locator.getItemIndex());

        if (locator.hasItemIndex()) {
            player.openContainer(new TermFactory(accessInterface,locator));
        }

    }

    private final WTGuiObject witGUIObject;
    private static long autoBase = Long.MIN_VALUE;
    private final Map<IInterfaceHost, WirelessInterfaceTerminalContainer.InvTracker> diList = new HashMap<>();
    private final Map<Long, WirelessInterfaceTerminalContainer.InvTracker> byId = new HashMap<>();
    private IGrid grid;
    private CompoundNBT data = new CompoundNBT();

    public WirelessInterfaceTerminalContainer(int id, final PlayerInventory ip, final WTGuiObject anchor) {
        super(Menus.WIT, id, ip, anchor);
        witGUIObject = anchor;

        if(isServer() && witGUIObject.getActionableNode() != null) {
            grid = witGUIObject.getActionableNode().getGrid();
        }

        this.createPlayerInventorySlots(ip);

        final WTInventoryHandler fixedWITInv = new WTInventoryHandler(ip, witGUIObject.getItemStack(), this);
        //173, 129
        addSlot(new AppEngSlot(fixedWITInv, WTInventoryHandler.INFINITY_BOOSTER_CARD));
    }

    private double powerMultiplier = 1;
    private int ticks = 0;

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if(!witGUIObject.rangeCheck()) {
            if(isValidContainer()) {
                getPlayerInventory().player.sendMessage(PlayerMessages.OutOfRange.get(), Util.DUMMY_UUID);
                getPlayerInventory().player.closeScreen();
            }
            setValidContainer(false);
        } else {
            powerMultiplier = WTConfig.getPowerMultiplier(witGUIObject.getRange(), witGUIObject.isOutOfRange());

            if(witGUIObject.extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.ONE) == 0) {
                if(isValidContainer()) {
                    getPlayerInventory().player.sendMessage(PlayerMessages.DeviceNotPowered.get(), Util.DUMMY_UUID);
                    getPlayerInventory().player.closeScreen();
                }
                setValidContainer(false);
            }
        }

        ticks++;
        if(ticks > 10) {
            witGUIObject.extractAEPower(powerMultiplier * ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
            ticks = 0;
        }

        if(grid == null) {
            return;
        }

        int total = 0;
        boolean missing = false;

        final IActionHost host = getActionHost();
        if(host != null) {
            final IGridNode agn = host.getActionableNode();
            if(agn != null && agn.isActive()) {
                for(final IGridNode gn : grid.getMachines(InterfaceTileEntity.class)) {
                    if(gn.isActive()) {
                        final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                        if(ih.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
                            continue;
                        }

                        final WirelessInterfaceTerminalContainer.InvTracker t = diList.get(ih);

                        if(t == null) {
                            missing = true;
                        } else {
                            final DualityInterface dual = ih.getInterfaceDuality();
                            if(!t.name.equals(dual.getTermName())) {
                                missing = true;
                            }
                        }

                        total++;
                    }
                }

                for(final IGridNode gn : grid.getMachines(InterfacePart.class)) {
                    if(gn.isActive()) {
                        final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                        if(ih.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO)
                            continue;

                        final WirelessInterfaceTerminalContainer.InvTracker t = diList.get(ih);

                        if(t == null) missing = true;
                        else {
                            final DualityInterface dual = ih.getInterfaceDuality();
                            if(!t.name.equals(dual.getTermName())) {
                                missing = true;
                            }
                        }
                        total++;
                    }
                }
            }
        }

        if(total != diList.size() || missing) regenList(data);
        else {
            for(final Map.Entry<IInterfaceHost, WirelessInterfaceTerminalContainer.InvTracker> en : diList.entrySet()) {
                final WirelessInterfaceTerminalContainer.InvTracker inv = en.getValue();
                for(int x = 0; x < inv.server.getSlots(); x++)
                    if(isDifferent(inv.server.getStackInSlot(x), inv.client.getStackInSlot(x))) addItems(data, inv, x, 1);
            }
        }

        if(!data.isEmpty()) {
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) getPlayerInventory().player),new S2CInterfaceTerminalPacket(data));
            data = new CompoundNBT();
        }
    }


    @Override
    public void doAction(final ServerPlayerEntity player, final InventoryAction action, final int slot, final long id) {
        final InvTracker inv = byId.get(id);
        if (inv != null) {
            final ItemStack is = inv.server.getStackInSlot(slot);
            final boolean hasItemInHand = !player.inventory.getItemStack().isEmpty();

            final InventoryAdaptor playerHand = new AdaptorItemHandler(new WrapperCursorItemHandler(player.inventory));

            final IItemHandler theSlot = new WrapperFilteredItemHandler(new WrapperRangeItemHandler(inv.server, slot, slot + 1), new PatternSlotFilter());
            final InventoryAdaptor interfaceSlot = new AdaptorItemHandler(theSlot);

            switch (action) {
                case PICKUP_OR_SET_DOWN:

                    if (hasItemInHand) {
                        ItemStack inSlot = theSlot.getStackInSlot(0);
                        if (inSlot.isEmpty()) {
                            player.inventory.setItemStack(interfaceSlot.addItems(player.inventory.getItemStack()));
                        }
                        else {
                            inSlot = inSlot.copy();
                            final ItemStack inHand = player.inventory.getItemStack().copy();

                            ItemHandlerUtil.setStackInSlot(theSlot, 0, ItemStack.EMPTY);
                            player.inventory.setItemStack(ItemStack.EMPTY);

                            player.inventory.setItemStack(interfaceSlot.addItems(inHand.copy()));

                            if (player.inventory.getItemStack().isEmpty()) {
                                player.inventory.setItemStack(inSlot);
                            }
                            else {
                                player.inventory.setItemStack(inHand);
                                ItemHandlerUtil.setStackInSlot(theSlot, 0, inSlot);
                            }
                        }
                    }
                    else {
                        ItemHandlerUtil.setStackInSlot(theSlot, 0, playerHand.addItems(theSlot.getStackInSlot(0)));
                    }

                    break;
                case SPLIT_OR_PLACE_SINGLE:

                    if (hasItemInHand) {
                        ItemStack extra = playerHand.removeItems(1, ItemStack.EMPTY, null);
                        if (!extra.isEmpty()) {
                            extra = interfaceSlot.addItems(extra);
                        }
                        if (!extra.isEmpty()) {
                            playerHand.addItems(extra);
                        }
                    }
                    else if (!is.isEmpty()) {
                        ItemStack extra = interfaceSlot.removeItems((is.getCount() + 1) / 2, ItemStack.EMPTY, null);
                        if (!extra.isEmpty()) {
                            extra = playerHand.addItems(extra);
                        }
                        if (!extra.isEmpty()) {
                            interfaceSlot.addItems(extra);
                        }
                    }

                    break;
                case SHIFT_CLICK:

                    final InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor(player);

                    ItemHandlerUtil.setStackInSlot(theSlot, 0, playerInv.addItems(theSlot.getStackInSlot(0)));

                    break;
                case MOVE_REGION:

                    final InventoryAdaptor playerInvAd = InventoryAdaptor.getAdaptor(player);
                    for (int x = 0; x < inv.server.getSlots(); x++) {
                        ItemHandlerUtil.setStackInSlot(inv.server, x, playerInvAd.addItems(inv.server.getStackInSlot(x)));
                    }

                    break;
                case CREATIVE_DUPLICATE:

                    if (player.abilities.isCreativeMode && !hasItemInHand) {
                        player.inventory.setItemStack(is.isEmpty() ? ItemStack.EMPTY : is.copy());
                    }

                    break;
                default:
                    return;
            }
            updateHeld(player);
        }
    }

    private boolean isValidPattern(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem;
    }

    private void regenList(final CompoundNBT data) {
        byId.clear();
        diList.clear();

        final IActionHost host = getActionHost();
        if(host != null) {
            final IGridNode agn = host.getActionableNode();
            if(agn != null && agn.isActive()) {
                for(final IGridNode gn : grid.getMachines(InterfaceTileEntity.class)) {
                    final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                    final DualityInterface dual = ih.getInterfaceDuality();
                    if(gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES)
                        diList.put(ih, new WirelessInterfaceTerminalContainer.InvTracker(dual, dual.getPatterns(), dual.getTermName()));
                }

                for(final IGridNode gn : grid.getMachines(InterfacePart.class)) {
                    final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                    final DualityInterface dual = ih.getInterfaceDuality();
                    if(gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES)
                        diList.put(ih, new WirelessInterfaceTerminalContainer.InvTracker(dual, dual.getPatterns(), dual.getTermName()));
                }
            }
        }

        data.putBoolean("clear", true);

        for(final Map.Entry<IInterfaceHost, WirelessInterfaceTerminalContainer.InvTracker> en : diList.entrySet()) {
            final WirelessInterfaceTerminalContainer.InvTracker inv = en.getValue();
            byId.put(inv.which, inv);
            addItems(data, inv, 0, inv.server.getSlots());
        }
    }

    private boolean isDifferent(final ItemStack a, final ItemStack b) {
        if(a.isEmpty() && b.isEmpty()) return false;
        if(a.isEmpty() || b.isEmpty()) return true;
        return !ItemStack.areItemsEqual(a, b);
    }

    private void addItems(final CompoundNBT data, final WirelessInterfaceTerminalContainer.InvTracker inv, final int offset, final int length) {
        final String name = '=' + Long.toString(inv.which, Character.MAX_RADIX);
        final CompoundNBT tag = data.getCompound(name);

        if(tag.isEmpty()) {
            tag.putLong("sortBy", inv.sortBy);
            tag.putString("un", ITextComponent.Serializer.toJson(inv.name));
        }

        for(int x = 0; x < length; x++) {
            final CompoundNBT itemNBT = new CompoundNBT();

            final ItemStack is = inv.server.getStackInSlot(x + offset);

            // "update" client side.
            ItemHandlerUtil.setStackInSlot(inv.client, x + offset, is.isEmpty() ? ItemStack.EMPTY : is.copy());

            if(!is.isEmpty()) is.write(itemNBT);

            tag.put(Integer.toString(x + offset), itemNBT);
        }
        data.put(name, tag);
    }

    private static class InvTracker {
        private final long sortBy;
        private final long which = autoBase++;
        private final ITextComponent name;
        private final IItemHandler client;
        private final IItemHandler server;

        public InvTracker(final DualityInterface dual, final IItemHandler patterns, final ITextComponent name) {
            this.server = patterns;
            this.client = new AppEngInternalInventory(null, server.getSlots());
            this.name = name;
            sortBy = dual.getSortValue();
        }
    }

    private static class PatternSlotFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(final IItemHandler inv, final int slot, final int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(final IItemHandler inv, final int slot, final ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem;
        }
    }


    public boolean isWUT() {
        return witGUIObject.getItemStack().getItem() instanceof WUTItem;
    }
}