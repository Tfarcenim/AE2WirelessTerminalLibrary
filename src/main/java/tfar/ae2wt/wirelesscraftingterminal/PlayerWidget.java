package tfar.ae2wt.wirelesscraftingterminal;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

public class PlayerWidget extends Widget {
    public PlayerWidget() {
        super(0, 0, 0, 0, new StringTextComponent(""));
    }


    //why doesn't the fabric port use the helper method?
    @Override
    protected void renderBg(MatrixStack matrixStack, Minecraft minecraft, int mouseX, int mouseY) {

        InventoryScreen.drawEntityOnScreen(x, y, 30, x - mouseX, y - mouseY - 50, minecraft.player);
    }
}
