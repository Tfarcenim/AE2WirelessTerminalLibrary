package tfar.ae2wt.wpt;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.ICraftingHelper;
import appeng.api.definitions.IDefinitions;
import appeng.api.networking.IGridNode;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerLocator;
import appeng.container.ContainerNull;
import appeng.container.SlotSemantic;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.container.slot.*;
import appeng.core.Api;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.packets.PatternSlotPacket;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ViewCellItem;
import appeng.me.helpers.MachineSource;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import tfar.ae2wt.WTConfig;
import tfar.ae2wt.init.Menus;
import tfar.ae2wt.mixin.ContainerAccess;
import tfar.ae2wt.net.PacketHandler;
import tfar.ae2wt.net.server.C2STogglePatternCraftingModePacket;
import tfar.ae2wt.net.server.C2STogglePatternSubsitutionPacket;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import tfar.ae2wt.terminal.WTInventoryHandler;
import tfar.ae2wt.wut.WUTItem;

public class WirelessPatternTerminalContainer extends ItemTerminalContainer implements IOptionalSlotHost, IContainerCraftingPacket {

    public static WirelessPatternTerminalContainer openClient(int windowId, PlayerInventory inv) {
        PlayerEntity player = inv.player;
        ItemStack it = inv.player.getHeldItem(Hand.MAIN_HAND);
        ContainerLocator locator = ContainerLocator.forHand(inv.player, Hand.MAIN_HAND);
        WPTGuiObject host = new WPTGuiObject((AbstractWirelessTerminalItem) it.getItem(), it, player, locator.getItemIndex());
        return new WirelessPatternTerminalContainer(windowId, inv, host);
    }

    private final FakeCraftingMatrixSlot[] craftingSlots = new FakeCraftingMatrixSlot[9];
    private final OptionalFakeSlot[] processingOutputSlots = new OptionalFakeSlot[3];
    private ICraftingRecipe currentRecipe;
    private final AppEngInternalInventory cOut = new AppEngInternalInventory(null, 1);
    private final AppEngInternalInventory craftingGridInv;
    private final WirelessPatternTermSlot craftSlot;
    private final RestrictedInputSlot blankPatternSlot;
    private final RestrictedInputSlot encodedPatternSlot;
    private final ICraftingHelper craftingHelper = Api.INSTANCE.crafting();

    public static void openServer(PlayerEntity player, ContainerLocator locator) {
        ItemStack it = player.inventory.getStackInSlot(locator.getItemIndex());
        WPTGuiObject accessInterface = new WPTGuiObject((AbstractWirelessTerminalItem) it.getItem(), it, player, locator.getItemIndex());

        if (locator.hasItemIndex()) {
            player.openContainer(new TermFactory(accessInterface,locator));
        }
    }

    private final WPTGuiObject wptGUIObject;

    @GuiSync(97)
    public boolean craftingMode;
    @GuiSync(96)
    public boolean substitute;

    public WirelessPatternTerminalContainer(int id, final PlayerInventory ip, final WPTGuiObject gui) {
        super(Menus.PATTERN, id, ip, gui, true);
        wptGUIObject = gui;

        final int slotIndex = ((IInventorySlotAware) wptGUIObject).getInventorySlot();
        lockPlayerInventorySlot(slotIndex);
        final AppEngInternalInventory patternInv = getPatternTerminal().getInventoryByName("pattern");
        final AppEngInternalInventory output = getPatternTerminal().getInventoryByName("output");

        final WTInventoryHandler fixedWPTInv = new WTInventoryHandler(getPlayerInventory(), wptGUIObject.getItemStack(), this);

        craftingGridInv = getPatternTerminal().getInventoryByName("crafting");

        for (int y = 0; y < 9; y++) {
                addSlot(craftingSlots[y] = new FakeCraftingMatrixSlot(craftingGridInv, y), SlotSemantic.CRAFTING_GRID);
        }

        addSlot(craftSlot = new WirelessPatternTermSlot(ip.player, getActionSource(), powerSource, gui, craftingGridInv,
                        patternInv,  this, 2, this)
                , SlotSemantic.CRAFTING_RESULT);
        craftSlot.setIcon(null);

        for (int y = 0; y < 3; y++) {
            this.addSlot(this.processingOutputSlots[y] = new PatternOutputsSlot(output, this, y, 1), SlotSemantic.PROCESSING_RESULT);
            this.processingOutputSlots[y].setRenderDisabled(false);
            this.processingOutputSlots[y].setIcon(null);
        }

        addSlot(new AppEngSlot(fixedWPTInv, WTInventoryHandler.INFINITY_BOOSTER_CARD));//, 80, -20

        this.addSlot(this.blankPatternSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.BLANK_PATTERN, patternInv, 0), SlotSemantic.BLANK_PATTERN);
        this.addSlot(this.encodedPatternSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN, patternInv, 1), SlotSemantic.ENCODED_PATTERN);
        this.encodedPatternSlot.setStackLimit(1);
        this.createPlayerInventorySlots(ip);

        if (isClient()) {//FIXME set craftingMode and substitute serverside
            craftingMode = AbstractWirelessTerminalItem.getBoolean(wptGUIObject.getItemStack(), "craftingMode");
            substitute = AbstractWirelessTerminalItem.getBoolean(wptGUIObject.getItemStack(), "substitute");

            PacketHandler.INSTANCE.sendToServer(new C2STogglePatternCraftingModePacket(craftingMode));

            PacketHandler.INSTANCE.sendToServer(new C2STogglePatternSubsitutionPacket(substitute));
        }
    }

    private int ticks = 0;

    @Override
    public void detectAndSendChanges() {
        if (isClient()) return;
        super.detectAndSendChanges();

        if (!wptGUIObject.rangeCheck()) {
            if (isValidContainer()) {
                getPlayerInventory().player.sendMessage(PlayerMessages.OutOfRange.get(), Util.DUMMY_UUID);
                getPlayerInventory().player.closeScreen();
            }
            setValidContainer(false);
        } else {
            double powerMultiplier = WTConfig.getPowerMultiplier(wptGUIObject.getRange(), wptGUIObject.isOutOfRange());
            ticks++;
            if (ticks > 10) {
                wptGUIObject.extractAEPower((powerMultiplier) * ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
                ticks = 0;
            }

            if (wptGUIObject.extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.ONE) == 0) {
                if (isValidContainer()) {
                    getPlayerInventory().player.sendMessage(PlayerMessages.DeviceNotPowered.get(), Util.DUMMY_UUID);
                    getPlayerInventory().player.closeScreen();
                }
                setValidContainer(false);
            }
        }

        if (isCraftingMode() != getPatternTerminal().isCraftingRecipe()) {
            setCraftingMode(getPatternTerminal().isCraftingRecipe());
        }

        if (substitute != getPatternTerminal().isSubstitution()) {
            substitute = getPatternTerminal().isSubstitution();
            AbstractWirelessTerminalItem.setBoolean(wptGUIObject.getItemStack(), substitute, "substitute");
        }
    }

    @Override
    public void onSlotChange(final Slot s) {
        if (s == encodedPatternSlot && isServer()) {
            for (final IContainerListener listener : ((ContainerAccess) this).getListeners()) {
                for (int i = 0; i < inventorySlots.size(); i++) {
                    Slot slot = inventorySlots.get(i);
                    if (slot instanceof OptionalFakeSlot || slot instanceof FakeCraftingMatrixSlot)
                        listener.sendSlotContents(this, i, slot.getStack());
                }
                if (listener instanceof ServerPlayerEntity)
                    ((ServerPlayerEntity) listener).isChangingQuantityOnly = false;
            }
            detectAndSendChanges();
        }

        if (s == craftSlot && isClient()) getAndUpdateOutput();

        if (isClient() && isCraftingMode()) {
            for (Slot slot : craftingSlots) if (s == slot) getAndUpdateOutput();
            for (Slot slot : processingOutputSlots) if (s == slot) getAndUpdateOutput();
        }
    }

    /**
     * Callback for when the crafting matrix is changed.
     */

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return true;
    }

    public void encode() {
        ItemStack output = encodedPatternSlot.getStack();

        final ItemStack[] in = getInputs();
        final ItemStack[] out = getOutputs();

        // if there is no input, this would be silly.
        if (in == null || out == null || isCraftingMode() && currentRecipe == null) return;

        // first check the output slots, should either be null, or a pattern
        if (!output.isEmpty() && !craftingHelper.isEncodedPattern(output))
            return; //if nothing is there we should snag a new pattern.
        else if (output.isEmpty()) {
            output = blankPatternSlot.getStack();
            if (output.isEmpty() || !isPattern(output)) return; // no blanks.

            // remove one, and clear the input slot.
            output.setCount(output.getCount() - 1);
            if (output.getCount() == 0) blankPatternSlot.putStack(ItemStack.EMPTY);

            // let the crafting helper create a new encoded pattern
            output = null;
        }

        if (isCraftingMode())
            output = craftingHelper.encodeCraftingPattern(output, currentRecipe, in, out[0], isSubstitute());
        else output = craftingHelper.encodeProcessingPattern(output, in, out);
        encodedPatternSlot.putStack(output);
    }

    private ItemStack[] getInputs() {
        final ItemStack[] input = new ItemStack[9];
        boolean hasValue = false;

        for (int x = 0; x < craftingSlots.length; x++) {
            input[x] = craftingSlots[x].getStack();
            if (!input[x].isEmpty()) hasValue = true;
        }

        if (hasValue) return input;
        return null;
    }

    private ItemStack[] getOutputs() {
        if (isCraftingMode()) {
            final ItemStack out = getAndUpdateOutput();
            if (!out.isEmpty() && out.getCount() > 0) return new ItemStack[]{out};
        } else {
            boolean hasValue = false;
            final ItemStack[] list = new ItemStack[3];

            for (int i = 0; i < processingOutputSlots.length; i++) {
                final ItemStack out = processingOutputSlots[i].getStack();
                list[i] = out;
                if (!out.isEmpty()) hasValue = true;
            }
            if (hasValue) return list;
        }

        return null;
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("player")) return new InvWrapper(getPlayerInventory());
        return getPatternTerminal().getInventoryByName(name);
    }

    private boolean isPattern(final ItemStack output) {
        if (output.isEmpty()) return false;

        final IDefinitions definitions = Api.instance().definitions();
        return definitions.materials().blankPattern().isSameAs(output);
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        if (idx == 1) return isServer() ? !getPatternTerminal().isCraftingRecipe() : !isCraftingMode();
        else if (idx == 2) return isServer() ? getPatternTerminal().isCraftingRecipe() : isCraftingMode();
        else return false;
    }

    public void craftOrGetItem(final IAEItemStack slotItem, final boolean shift, final IAEItemStack[] pattern) {
        if (slotItem != null && this.monitor != null /*
         * TODO should this check powered / powerSource?
         */) {
            final IAEItemStack out = slotItem.copy();
            InventoryAdaptor inv = new AdaptorItemHandler(
                    new WrapperCursorItemHandler(this.getPlayerInventory().player.inventory));
            final InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor(this.getPlayerInventory().player);

            if (shift) {
                inv = playerInv;
            }

            if (!inv.simulateAdd(out.createItemStack()).isEmpty()) {
                return;
            }

            final IAEItemStack extracted = Platform.poweredExtraction(this.powerSource, this.monitor,
                    out, this.getActionSource());
            final PlayerEntity p = this.getPlayerInventory().player;

            if (extracted != null) {
                inv.addItems(extracted.createItemStack());
                if (p instanceof ServerPlayerEntity) {
                    this.updateHeld((ServerPlayerEntity) p);
                }
                this.detectAndSendChanges();
                return;
            }

            final CraftingInventory ic = new CraftingInventory(new ContainerNull(), 3, 3);
            final CraftingInventory real = new CraftingInventory(new ContainerNull(), 3, 3);

            for (int x = 0; x < 9; x++) {
                ic.setInventorySlotContents(x, pattern[x] == null ? ItemStack.EMPTY
                        : pattern[x].createItemStack());
            }

            final IRecipe<CraftingInventory> r = p.world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, ic, p.world)
                    .orElse(null);

            if (r == null) {
                return;
            }

            final IMEMonitor<IAEItemStack> storage = this.getPatternTerminal()
                    .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
            final IItemList<IAEItemStack> all = storage.getStorageList();

            final ItemStack is = r.getCraftingResult(ic);

            for (int x = 0; x < ic.getSizeInventory(); x++) {
                if (!ic.getStackInSlot(x).isEmpty()) {
                    final ItemStack pulled = Platform.extractItemsByRecipe(this.powerSource,
                            this.getActionSource(), storage, p.world, r, is, ic, ic.getStackInSlot(x), x, all,
                            Actionable.MODULATE, ViewCellItem.createFilter(this.getViewCells()));
                    real.setInventorySlotContents(x, pulled);
                }
            }

            final IRecipe<CraftingInventory> rr = p.world.getRecipeManager()
                    .getRecipe(IRecipeType.CRAFTING, real, p.world).orElse(null);

            if (rr == r && Platform.itemComparisons().isSameItem(rr.getCraftingResult(real), is)) {
                final CraftResultInventory craftingResult = new CraftResultInventory();
                craftingResult.setRecipeUsed(rr);

                final CraftingResultSlot sc = new CraftingResultSlot(p, real, craftingResult, 0, 0, 0);
                sc.onTake(p, is);

                for (int x = 0; x < real.getSizeInventory(); x++) {
                    final ItemStack failed = playerInv.addItems(real.getStackInSlot(x));

                    if (!failed.isEmpty()) {
                        p.dropItem(failed, false);
                    }
                }

                inv.addItems(is);
                if (p instanceof ServerPlayerEntity) {
                    this.updateHeld((ServerPlayerEntity) p);
                }
                this.detectAndSendChanges();
            } else {
                for (int x = 0; x < real.getSizeInventory(); x++) {
                    final ItemStack failed = real.getStackInSlot(x);
                    if (!failed.isEmpty()) {
                        this.monitor.injectItems(AEItemStack.fromItemStack(failed), Actionable.MODULATE,
                                new MachineSource(this.getPatternTerminal()));
                    }
                }
            }
        }
    }

    private ItemStack getAndUpdateOutput() {
        final World world = getPlayerInventory().player.world;
        final CraftingInventory ic = new CraftingInventory(this, 3, 3);

        for (int x = 0; x < ic.getSizeInventory(); x++) ic.setInventorySlotContents(x, craftingGridInv.getStackInSlot(x));

        if (currentRecipe == null || !currentRecipe.matches(ic, world))
            currentRecipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, ic, world).orElse(null);

        final ItemStack is;

        if (currentRecipe == null) is = ItemStack.EMPTY;
        else is = currentRecipe.getCraftingResult(ic);

        cOut.setStackInSlot(0, is);
        return is;
    }

    @Override
    public boolean useRealItems() {
        return false;
    }

    public WPTGuiObject getPatternTerminal() {
        return wptGUIObject;
    }

    private boolean isSubstitute() {
        return substitute;
    }

    public boolean isCraftingMode() {
        return craftingMode;
    }

    public void setCraftingMode(final boolean craftingMode) {
        if (craftingMode != this.craftingMode) {
            this.craftingMode = craftingMode;
            AbstractWirelessTerminalItem.setBoolean(wptGUIObject.getItemStack(), craftingMode, "craftingMode");
        }
    }

    public void clearPattern() {
        for (final Slot s : craftingSlots) s.putStack(ItemStack.EMPTY);
        for (final Slot s : processingOutputSlots) s.putStack(ItemStack.EMPTY);

        detectAndSendChanges();
        getAndUpdateOutput();
    }

    @Override
    public IGridNode getNetworkNode() {
        return wptGUIObject.getActionableNode();
    }

    public boolean isWUT() {
        return wptGUIObject.getItemStack().getItem() instanceof WUTItem;
    }

    //@Override
    //public ItemStack[] getViewCells() {
    //    return wptGUIObject.getViewCellStorage().getViewCells();
   // }
}