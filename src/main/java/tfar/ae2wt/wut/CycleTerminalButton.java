package tfar.ae2wt.wut;

import appeng.client.gui.widgets.ITooltip;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CycleTerminalButton extends Button implements ITooltip {

    public CycleTerminalButton(int x, int y, IPressable onPress) {
        super(x, y, 16, 16, new TranslationTextComponent("gui.ae2wtlib.cycle_terminal"), onPress);
        visible = true;
        active = true;
    }

   // @Override
   // public ITextComponent getTooltipMessage() {
   //     return new TranslationTextComponent("gui.ae2wtlib.cycle_terminal.desc");
   // }

    @Override
    public int getTooltipAreaX() {
        return x;
    }

    @Override
    public int getTooltipAreaY() {
        return y;
    }

    @Override
    public int getTooltipAreaWidth() {
        return 16;
    }

    @Override
    public int getTooltipAreaHeight() {
        return 16;
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return true;//TODO
    }

    public static final ResourceLocation TEXTURE_STATES = new ResourceLocation("appliedenergistics2", "textures/guis/states.png");

    @Override
    public void render(MatrixStack matrices, final int mouseX, final int mouseY, float partial) {
        Minecraft minecraft = Minecraft.getInstance();
        if(this.visible) {
            final int iconIndex = 6;

            TextureManager textureManager = minecraft.getTextureManager();
            textureManager.bindTexture(TEXTURE_STATES);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();

            if(this.active) RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            else RenderSystem.color4f(0.5f, 0.5f, 0.5f, 1.0f);

            final int uv_y = iconIndex / 16;

            blit(matrices, this.x, this.y, 256 - 16, 256 - 16, 16, 16);
            blit(matrices, this.x, this.y, iconIndex * 16, uv_y * 16, 16, 16);

            RenderSystem.enableDepthTest();
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);

            if(isHovered()) renderToolTip(matrices, mouseX, mouseY);
        }
    }
}