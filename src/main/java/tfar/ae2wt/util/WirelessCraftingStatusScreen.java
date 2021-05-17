package tfar.ae2wt.util;

import appeng.client.gui.implementations.CraftingCPUScreen;
import appeng.container.implementations.WirelessCraftingStatusContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class WirelessCraftingStatusScreen extends CraftingCPUScreen<WirelessCraftingStatusContainer> {

    private final ae2wtlibSubScreen subGui;
    private Button selectCPU;

    public WirelessCraftingStatusScreen(WirelessCraftingStatusContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        subGui = new ae2wtlibSubScreen(this, container.getTarget());
    }

    @Override
    public void init() {
        super.init();

        selectCPU = new Button(guiLeft + 8, guiTop + ySize - 25, 150, 20, getNextCpuButtonLabel(), btn -> selectNextCpu());
        addButton(selectCPU);

        subGui.addBackButton(btn -> {
            addButton(btn);
            btn.setHideEdge(true);
        }, 213, -4);
    }

    @Override
    public void render(MatrixStack matrices, final int mouseX, final int mouseY, final float btn) {
        updateCPUButtonText();
        super.render(matrices, mouseX, mouseY, btn);
    }

    private void updateCPUButtonText() {
        selectCPU.setMessage(getNextCpuButtonLabel());
    }

    private ITextComponent getNextCpuButtonLabel() {
        if(container.noCPU) {
            return GuiText.NoCraftingJobs.text();
        }
        return GuiText.CraftingCPU.withSuffix(": ").appendSibling(container.cpuName);
    }

    @Override
    protected ITextComponent getGuiDisplayName(final ITextComponent in) {
        return in;
    }

    private void selectNextCpu() {
        final boolean backwards = isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("Terminal.Cpu", backwards ? "Prev" : "Next"));
    }
}