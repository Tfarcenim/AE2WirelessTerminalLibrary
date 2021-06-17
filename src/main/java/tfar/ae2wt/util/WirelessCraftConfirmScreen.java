package tfar.ae2wt.util;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.AbstractTableRenderer;
import appeng.client.gui.me.crafting.CraftConfirmTableRenderer;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.container.me.crafting.CraftingPlanSummary;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.text.NumberFormat;

public class WirelessCraftConfirmScreen extends AEBaseScreen<WirelessCraftConfirmContainer> {

   // private final ae2wtlibSubScreen subGui;

    private final CraftConfirmTableRenderer table = new CraftConfirmTableRenderer(this, 9, 19);
    private final Button start;
    private final Button selectCPU;
    private final Scrollbar scrollbar;

    public WirelessCraftConfirmScreen(WirelessCraftConfirmContainer container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
        this.scrollbar = this.widgets.addScrollBar("scrollbar");
        this.start = this.widgets.addButton("start", GuiText.Start.text(), this::start);
        this.start.active = false;
        this.selectCPU = this.widgets.addButton("selectCpu", this.getNextCpuButtonLabel(), this::selectNextCpu);
        this.selectCPU.active = false;
        this.widgets.addButton("cancel", GuiText.Cancel.text(), container::goBack);
    }

    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.selectCPU.setMessage(this.getNextCpuButtonLabel());
        CraftingPlanSummary plan = this.container.getPlan();
        boolean planIsStartable = plan != null && !plan.isSimulation();
        this.start.active = !(this.container).hasNoCPU() && planIsStartable;
        this.selectCPU.active = planIsStartable;
        ITextComponent planDetails = GuiText.CalculatingWait.text();
        ITextComponent cpuDetails = StringTextComponent.EMPTY;
        if (plan != null) {
            String byteUsed = NumberFormat.getInstance().format(plan.getUsedBytes());
            planDetails = GuiText.BytesUsed.text(byteUsed);
            if (plan.isSimulation()) {
                cpuDetails = GuiText.Simulation.text();
            } else if ((this.container).getCpuAvailableBytes() > 0L) {
                cpuDetails = GuiText.ConfirmCraftCpuStatus.text(this.container.getCpuAvailableBytes(), this.container.getCpuCoProcessors());
            } else {
                cpuDetails = GuiText.ConfirmCraftNoCpu.text();
            }
        }

        this.setTextContent("dialog_title", GuiText.CraftingPlan.text(planDetails));
        this.setTextContent("cpu_status", cpuDetails);
        int size = plan != null ? plan.getEntries().size() : 0;
        this.scrollbar.setRange(0, AbstractTableRenderer.getScrollableRows(size), 1);
    }

    private ITextComponent getNextCpuButtonLabel() {
        if ((this.container).hasNoCPU()) {
            return GuiText.NoCraftingCPUs.text();
        } else {
            ITextComponent cpuName;
            if ((this.container).cpuName == null) {
                cpuName = GuiText.Automatic.text();
            } else {
                cpuName = (this.container).cpuName;
            }

            return GuiText.SelectedCraftingCPU.text(cpuName);
        }
    }

    public void drawFG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY) {
        CraftingPlanSummary plan = (this.container).getPlan();
        if (plan != null) {
            this.table.render(matrixStack, mouseX, mouseY, plan.getEntries(), this.scrollbar.getCurrentScroll());
        }

    }

    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (this.checkHotbarKeys(InputMappings.getInputByCode(keyCode, scanCode)) || keyCode != 257 && keyCode != 335) {
            return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
        } else {
            this.start();
            return true;
        }
    }

    private void selectNextCpu() {
        final boolean backwards = isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("Terminal.Cpu", backwards ? "Prev" : "Next"));
    }

    private void start() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("Terminal.Start", "Start"));
    }
}