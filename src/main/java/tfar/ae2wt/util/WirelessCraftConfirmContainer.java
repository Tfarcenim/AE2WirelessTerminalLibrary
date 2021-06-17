package tfar.ae2wt.util;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.me.crafting.CraftAmountContainer;
import appeng.container.me.crafting.CraftingCPUCyclingContainer;
import appeng.container.me.crafting.CraftingPlanSummary;
import appeng.core.AELog;
import appeng.core.sync.packets.CraftConfirmPlanPacket;
import appeng.me.helpers.PlayerSource;
import appeng.util.Platform;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import tfar.ae2wt.cpu.CraftingCPUCycler;
import tfar.ae2wt.cpu.CraftingCPURecord;
import tfar.ae2wt.init.Menus;
import tfar.ae2wt.net.TermFactoryConfirm;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import tfar.ae2wt.wirelesscraftingterminal.WirelessCraftingTerminalContainer;
import tfar.ae2wt.wirelesscraftingterminal.WCTGuiObject;
import tfar.ae2wt.wpt.WPTGuiObject;
import tfar.ae2wt.wpt.WirelessPatternTerminalContainer;

import java.util.concurrent.Future;

public class WirelessCraftConfirmContainer extends AEBaseContainer implements CraftingCPUCyclingContainer {

    private CraftingPlanSummary plan;
    private IAEItemStack itemToCreate;

    public static WirelessCraftConfirmContainer openClient(int windowId, PlayerInventory inv) {
        PlayerEntity player = inv.player;
        ItemStack it = inv.player.getHeldItem(Hand.MAIN_HAND);
        ContainerLocator locator = ContainerLocator.forHand(inv.player, Hand.MAIN_HAND);
        WCTGuiObject host = new WCTGuiObject((AbstractWirelessTerminalItem) it.getItem(), it, player, locator.getItemIndex());
        return new WirelessCraftConfirmContainer(windowId, inv, host);
    }

    public static boolean openServer(PlayerEntity player, ContainerLocator locator) {
        ItemStack it = player.inventory.getStackInSlot(locator.getItemIndex());
        WCTGuiObject accessInterface = new WCTGuiObject((AbstractWirelessTerminalItem) it.getItem(), it, player, locator.getItemIndex());

        if(!Platform.checkPermissions(player, accessInterface, SecurityPermissions.CRAFT, true)) return false;


        if (locator.hasItemIndex()) {
            player.openContainer(new TermFactoryConfirm(accessInterface,locator));
        }
        return true;
    }

    private final CraftingCPUCycler cpuCycler;

    private ICraftingCPU selectedCpu;

    private Future<ICraftingJob> job;
    private ICraftingJob result;
    @GuiSync(0)
    public long bytesUsed;
    @GuiSync(3)
    public boolean autoStart = false;
    @GuiSync(4)
    public boolean simulation = true;

    // Indicates whether any CPUs are available
    @GuiSync(6)
    public boolean noCPU = true;

    // Properties of the currently selected crafting CPU, this can be null
    // if no CPUs are available, or if an automatic one is selected
    @GuiSync(1)
    public long cpuBytesAvail;
    @GuiSync(2)
    public int cpuCoProcessors;
    @GuiSync(7)
    public ITextComponent cpuName;

    public WirelessCraftConfirmContainer(int id, PlayerInventory ip, ITerminalHost te) {
        super(Menus.WCC, id, ip, te);
        cpuCycler = new CraftingCPUCycler(this::cpuMatches, this::onCPUSelectionChanged);
        // A player can select no crafting CPU to use a suitable one automatically
        cpuCycler.setAllowNoSelection(true);
    }

    @Override
    public void cycleSelectedCPU(final boolean next) {
        cpuCycler.cycleCpu(next);
    }

    @Override
    public void detectAndSendChanges() {
        if (!this.isClient()) {
            this.cpuCycler.detectAndSendChanges(this.getGrid());
            super.detectAndSendChanges();
            if (this.job != null && this.job.isDone()) {
                try {
                    this.result = this.job.get();
                    if (!this.result.isSimulation() && this.isAutoStart()) {
                        this.startJob();
                        return;
                    }

                    this.plan = CraftingPlanSummary.fromJob(this.getGrid(), this.getActionSrc(), this.result);
                    this.sendPacketToClient(new CraftConfirmPlanPacket(this.plan));
                } catch (Throwable var2) {
                    this.getPlayerInventory().player.sendMessage(new StringTextComponent("Error: " + var2.toString()), Util.DUMMY_UUID);
                    AELog.debug(var2);
                    this.setValidContainer(false);
                    this.result = null;
                }

                this.setJob(null);
            }

            this.verifyPermissions(SecurityPermissions.CRAFT, false);
        }
    }

    private IGrid getGrid() {
        final IActionHost h = ((IActionHost) getTarget());
        return h.getActionableNode().getGrid();
    }

    private boolean cpuMatches(final ICraftingCPU c) {
        return c.getAvailableStorage() >= getUsedBytes() && !c.isBusy();
    }

    public void startJob() {
        ContainerType<?> originalGui = null;

        final IActionHost ah = getActionHost();

        if(ah instanceof WCTGuiObject) {
            originalGui = Menus.WCT;
        } else if(ah instanceof WPTGuiObject) {
            originalGui = Menus.PATTERN;
        }

        if(result != null && !isSimulation()) {
            final ICraftingGrid cc = getGrid().getCache(ICraftingGrid.class);
            final ICraftingLink g = cc.submitJob(result, null, selectedCpu, true, getActionSrc());
            setAutoStart(false);
            if(g != null && originalGui != null && getLocator() != null) {
                if(originalGui.equals(Menus.WCT))
                    WirelessCraftingTerminalContainer.openServer(getPlayerInventory().player, getLocator());
                else if(originalGui.equals(Menus.PATTERN))
                    WirelessPatternTerminalContainer.openServer(getPlayerInventory().player, getLocator());
            }
        }
    }

    private IActionSource getActionSrc() {
        return new PlayerSource(getPlayerInventory().player, (IActionHost) getTarget());
    }

    @Override
    public void removeListener(final IContainerListener c) {
        super.removeListener(c);
        if(getJob() != null) {
            getJob().cancel(true);
            setJob(null);
        }
    }

    @Override
    public void onContainerClosed(final PlayerEntity par1PlayerEntity) {
        super.onContainerClosed(par1PlayerEntity);
        if(getJob() != null) {
            getJob().cancel(true);
            setJob(null);
        }
    }

    private void onCPUSelectionChanged(CraftingCPURecord cpuRecord, boolean cpusAvailable) {
        noCPU = !cpusAvailable;

        if(cpuRecord == null) {
            cpuBytesAvail = 0;
            cpuCoProcessors = 0;
            cpuName = null;
            selectedCpu = null;
        } else {
            cpuBytesAvail = cpuRecord.getSize();
            cpuCoProcessors = cpuRecord.getProcessors();
            cpuName = cpuRecord.getName();
            selectedCpu = cpuRecord.getCpu();
        }
    }

    public World getWorld() {
        return getPlayerInventory().player.world;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(final boolean autoStart) {
        this.autoStart = autoStart;
    }

    public long getUsedBytes() {
        return bytesUsed;
    }

    private void setUsedBytes(final long bytesUsed) {
        this.bytesUsed = bytesUsed;
    }

    public long getCpuAvailableBytes() {
        return cpuBytesAvail;
    }

    public int getCpuCoProcessors() {
        return cpuCoProcessors;
    }

    public ITextComponent getName() {
        return cpuName;
    }

    public boolean hasNoCPU() {
        return noCPU;
    }

    public boolean isSimulation() {
        return simulation;
    }

    private void setSimulation(final boolean simulation) {
        this.simulation = simulation;
    }

    private Future<ICraftingJob> getJob() {
        return job;
    }

    public void setItemToCreate(IAEItemStack itemToCreate) {
        this.itemToCreate = itemToCreate;
    }

    public void setJob(final Future<ICraftingJob> job) {
        this.job = job;
    }

    public void goBack() {
        PlayerEntity player = this.getPlayerInventory().player;
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            if (this.itemToCreate != null) {
                CraftAmountContainer.open(serverPlayer, this.getLocator(), this.itemToCreate, (int)this.itemToCreate.getStackSize());
            }
        } else {
            this.sendClientAction("back");
        }
    }

    public CraftingPlanSummary getPlan() {
        return plan;
    }
}