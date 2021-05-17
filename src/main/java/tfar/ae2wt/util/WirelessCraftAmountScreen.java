package tfar.ae2wt.util;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.NumberEntryWidget;
import appeng.core.localization.GuiText;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import tfar.ae2wt.net.C2SCraftRequestPacket;
import tfar.ae2wt.net.PacketHandler;

public class WirelessCraftAmountScreen extends AEBaseScreen<WirelessCraftAmountContainer> {
    private final ae2wtlibSubScreen subGui;

    private NumberEntryWidget amountToCraft;

    private Button next;

    public WirelessCraftAmountScreen(WirelessCraftAmountContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        subGui = new ae2wtlibSubScreen(this, container.getTarget());
    }

    @Override
    public void init() {
        super.init();

        amountToCraft = new NumberEntryWidget(this, 20, 30, 138, 62, NumberEntryType.CRAFT_ITEM_COUNT);
        amountToCraft.setValue(1);
        amountToCraft.setTextFieldBounds(62, 57, 50);
        amountToCraft.setMinValue(1);
        amountToCraft.setHideValidationIcon(true);
        amountToCraft.addButtons(children::add, this::addButton);

        next = addButton(new Button(guiLeft + 128, guiTop + 51, 38, 20, GuiText.Next.text(), this::confirm));
        amountToCraft.setOnConfirm(() -> confirm(next));

        subGui.addBackButton(this::addButton, 154, 0);

        changeFocus(true);
    }

    private void confirm(Button button) {
        int amount = amountToCraft.getIntValue().orElse(0);
        if(amount <= 0) return;
        PacketHandler.INSTANCE.sendToServer(new C2SCraftRequestPacket(amount,hasShiftDown()));
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        font.drawText(matrices, GuiText.SelectAmount.text(), 8, 6, 0x404040);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        next.setMessage(hasShiftDown() ? GuiText.Start.text() : GuiText.Next.text());

        bindTexture("guis/craft_amt.png");
        blit(matrices, offsetX, offsetY, 0, 0, xSize, ySize);

        next.active = amountToCraft.getIntValue().orElse(0) > 0;

        amountToCraft.render(matrices, offsetX, offsetY, partialTicks);
    }
}