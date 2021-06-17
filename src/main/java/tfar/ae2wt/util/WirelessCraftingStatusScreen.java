package tfar.ae2wt.util;

import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.container.me.crafting.CraftingStatusContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class WirelessCraftingStatusScreen extends CraftingCPUScreen<WirelessCraftingStatusContainer> {

    private final ae2wtlibSubScreen subGui;
    private Button selectCPU;

    public WirelessCraftingStatusScreen(WirelessCraftingStatusContainer container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title,style);
        subGui = new ae2wtlibSubScreen(this, container.getTarget());
        subGui.addBackButton("back", this.widgets);
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
        if (container.noCPU) {
            return GuiText.NoCraftingJobs.text();
        } else {
            ITextComponent name = container.cpuName;
            if (name == null) {
                name = StringTextComponent.EMPTY;
            }

            return GuiText.SelectedCraftingCPU.text(name);
        }
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