package tfar.ae2wt.wirelesscraftingterminal;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.container.ContainerLocator;
import appeng.container.ContainerNull;
import appeng.container.SlotSemantic;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.CraftingMatrixSlot;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.IContainerCraftingPacket;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperInvItemHandler;

import tfar.ae2wt.WTConfig;
import tfar.ae2wt.init.Menus;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import tfar.ae2wt.terminal.InternalInventory;
import tfar.ae2wt.terminal.SlotType;
import tfar.ae2wt.terminal.WTInventoryHandler;
import tfar.ae2wt.wirelesscraftingterminal.magnet_card.ItemMagnetCard;
import tfar.ae2wt.wirelesscraftingterminal.magnet_card.MagnetSettings;
import tfar.ae2wt.wut.WUTItem;

import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;

public class WirelessCraftingTerminalContainer extends ItemTerminalContainer implements IContainerCraftingPacket, IAEAppEngInventory {
    public static WirelessCraftingTerminalContainer openClient(int windowId, PlayerInventory inv) {
        PlayerEntity player = inv.player;
        ItemStack it = inv.player.getHeldItem(Hand.MAIN_HAND);
        ContainerLocator locator = ContainerLocator.forHand(inv.player, Hand.MAIN_HAND);
        WCTGuiObject host = new WCTGuiObject((AbstractWirelessTerminalItem) it.getItem(), it, player, locator.getItemIndex());
        return new WirelessCraftingTerminalContainer(windowId, inv, host);
    }

    private final int slot;
    private int ticks = 0;
    private double powerMultiplier = 0.5;

    private final IItemHandler craftingGridInv;
    private final CraftingMatrixSlot[] craftingSlots = new CraftingMatrixSlot[9];
    private final WirelessCraftingTermSlot outputSlot;
    private IRecipe<CraftingInventory> currentRecipe;

    private final WCTGuiObject wctGUIObject;
    final WTInventoryHandler wtInventoryHandler;

    public static void openServer(PlayerEntity player, ContainerLocator locator) {
        ItemStack it = player.inventory.getStackInSlot(locator.getItemIndex());
        WCTGuiObject accessInterface = new WCTGuiObject((AbstractWirelessTerminalItem) it.getItem(), it, player, locator.getItemIndex());

        if (locator.hasItemIndex()) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new TermFactory(accessInterface, locator));
        }
    }

    public WirelessCraftingTerminalContainer(int id, final PlayerInventory ip, final WCTGuiObject host) {
        super(Menus.WCT, id, ip, host, false);
        wctGUIObject = Objects.requireNonNull(host);
        wtInventoryHandler = new WTInventoryHandler(getPlayerInventory(), wctGUIObject.getItemStack(), this);
        if (host instanceof IInventorySlotAware) {
            this.slot = ((IInventorySlotAware) wctGUIObject).getInventorySlot();
        } else {
            this.slot = ip.currentItem;
        }

        craftingGridInv = new InternalInventory(this, 9, SlotType.crafting, wctGUIObject.getItemStack());

        for(int i = 0; i < 9; ++i) {
            this.addSlot(this.craftingSlots[i] = new CraftingMatrixSlot(this, craftingGridInv, i), SlotSemantic.CRAFTING_GRID);
        }

        this.addSlot(this.outputSlot =
                new WirelessCraftingTermSlot(this.getPlayerInventory().player, this.getActionSource(), this.powerSource, host.getIStorageGrid(), craftingGridInv, craftingGridInv, this), SlotSemantic.CRAFTING_RESULT);

        this.lockPlayerInventorySlot(this.slot);
        this.createPlayerInventorySlots(ip);

        this.onCraftMatrixChanged(new WrapperInvItemHandler(craftingGridInv));

        addSlot(new AppEngSlot(wtInventoryHandler, 3) {// 8, -76

            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getBackground() {
                return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET);
            }
        },SlotSemantic.MACHINE_INPUT);
        addSlot(new AppEngSlot(wtInventoryHandler, 2) {//, 8, -58
            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getBackground() {
                return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE);
            }
        },SlotSemantic.MACHINE_PROCESSING);
        addSlot(new AppEngSlot(wtInventoryHandler, 1) {//, 8, -40
            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getBackground() {
                return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS);
            }
        },SlotSemantic.MACHINE_OUTPUT);
        addSlot(new AppEngSlot(wtInventoryHandler, 0) {//, 8, -22
            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getBackground() {
                return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS);
            }
        },SlotSemantic.MACHINE_CRAFTING_GRID);

        addSlot(new AppEngSlot(wtInventoryHandler, WTInventoryHandler.OFFHAND) {//, 80, -22
            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getBackground() {
                return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
            }
        },SlotSemantic.PROCESSING_RESULT);

        addSlot(new AppEngSlot(wtInventoryHandler, WTInventoryHandler.TRASH),SlotSemantic.INSCRIBER_PLATE_BOTTOM);//, 98, -22
        addSlot(new AppEngSlot(wtInventoryHandler, WTInventoryHandler.INFINITY_BOOSTER_CARD),SlotSemantic.BIOMETRIC_CARD);//, 134, -20
        addSlot(new AppEngSlot(wtInventoryHandler, WTInventoryHandler.MAGNET_CARD),SlotSemantic.INSCRIBER_PLATE_TOP);//TODO fetch texture for card background , 152, -20

        //onCraftMatrixChanged(null);

    }

    @Override
    public void detectAndSendChanges() {
        this.ticks++;
        if (this.ticks > 10) {
            this.wctGUIObject.extractAEPower(this.getPowerMultiplier() * this.ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
            this.ticks = 0;
        }

        if(this.wctGUIObject.extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.ONE) == 0) {
            if(this.isValidContainer()) {
                this.getPlayerInventory().player.sendMessage(PlayerMessages.DeviceNotPowered.get(), Util.DUMMY_UUID);
                this.getPlayerInventory().player.closeScreen();
            }
            this.setValidContainer(false);
        }

        super.detectAndSendChanges();

        if(!this.wctGUIObject.rangeCheck()) {
            if(isServer() && this.isValidContainer()) {
                this.getPlayerInventory().player.sendMessage(PlayerMessages.OutOfRange.get(), Util.DUMMY_UUID);
                this.getPlayerInventory().player.closeScreen();
            }
            this.setValidContainer(false);
        } else {
            this.setPowerMultiplier(WTConfig.getPowerMultiplier(wctGUIObject.getRange(), wctGUIObject.isOutOfRange()));
        }
    }

    private double getPowerMultiplier() {
        return this.powerMultiplier;
    }

    void setPowerMultiplier(final double powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }

    //todo, support things outside of mainhand
    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return wctGUIObject.getItemStack() == player.getHeldItemMainhand();
    }

    @Override
    public IGridNode getNetworkNode() {
        return wctGUIObject.getActionableNode();
    }

    public void deleteTrashSlot() {
        wtInventoryHandler.setStackInSlot(WTInventoryHandler.TRASH, ItemStack.EMPTY);
    }

    private MagnetSettings magnetSettings;

    public MagnetSettings getMagnetSettings() {
        if(magnetSettings == null) return reloadMagnetSettings();
        return magnetSettings;
    }

    public void saveMagnetSettings() {
        ItemMagnetCard.saveMagnetSettings(wctGUIObject.getItemStack(), magnetSettings);
    }

    public MagnetSettings reloadMagnetSettings() {
        magnetSettings = ItemMagnetCard.loadMagnetSettings(wctGUIObject.getItemStack());
        //todo, this needs to be done another way
  //      if(isClient() && screen != null) screen.resetMagnetSettings();
        return magnetSettings;
    }

    // private WirelessCraftingTerminalScreen screen;

    // public void setScreen(WirelessCraftingTerminalScreen screen) {
    //     this.screen = screen;
    // }

    public boolean isWUT() {
        return wctGUIObject.getItemStack().getItem() instanceof WUTItem;
    }

    @Override
    public List<ItemStack> getViewCells() {
        return wctGUIObject.getViewCellStorage().getViewCells();
    }

    @Override
    public void saveChanges() {
    }

    @Override
    public void onChangeInventory(IItemHandler iItemHandler, int i, InvOperation invOperation, ItemStack itemStack, ItemStack itemStack1) {
    }

    /**
     * Callback for when the crafting matrix is changed.
     */

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        final ContainerNull cn = new ContainerNull();
        final CraftingInventory ic = new CraftingInventory(cn, 3, 3);

        for (int x = 0; x < 9; x++) {
            ic.setInventorySlotContents(x, this.craftingSlots[x].getStack());
        }

        World world = this.getPlayerInventory().player.world;
        if (this.currentRecipe == null || !this.currentRecipe.matches(ic, world)) {
            this.currentRecipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, ic, world).orElse(null);
        }

        if(currentRecipe == null) {
            this.outputSlot.putStack(ItemStack.EMPTY);
        } else {
            outputSlot.putStack(this.currentRecipe.getCraftingResult(ic));
        }
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("player")) {
            return new PlayerInvWrapper(this.getPlayerInventory());
        }
        return this.craftingGridInv;
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    public IRecipe<CraftingInventory> getCurrentRecipe() {
        return this.currentRecipe;
    }

    public void clearCraftingGrid() {
        Preconditions.checkState(isClient());
        CraftingMatrixSlot slot = craftingSlots[0];
        final InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, slot.slotNumber, 0);
        NetworkHandler.instance().sendToServer(p);
    }

    @Override
    public boolean hasItemType(ItemStack itemStack, int amount) {
        // In addition to the base item repo, also check the crafting grid if it
        // already contains some of the needed items
        for (Slot slot : getSlots(SlotSemantic.CRAFTING_GRID)) {
            ItemStack stackInSlot = slot.getStack();
            if (!stackInSlot.isEmpty() && Platform.itemComparisons().isSameItem(itemStack, stackInSlot)) {
                if (itemStack.getCount() >= amount) {
                    return true;
                }
                amount -= itemStack.getCount();
            }

        }

        return super.hasItemType(itemStack, amount);
    }
}
