package tfar.ae2wt.util;

import appeng.client.gui.widgets.ITooltip;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class ItemButton extends Button implements ITooltip {

    private final ResourceLocation texture;
    public static final ResourceLocation TEXTURE_STATES = new ResourceLocation("appliedenergistics2", "textures/guis/states.png");
    private boolean halfSize = false;

    public ItemButton(ResourceLocation texture, Button.IPressable onPress) {
        super(0, 0, 16, 16, StringTextComponent.EMPTY, onPress);
        this.texture = texture;
    }

    public void setVisibility(final boolean vis) {
        visible = vis;
        active = vis;
    }

    @Override
    public void playDownSound(SoundHandler soundManager) {
        super.playDownSound(soundManager);
    }

    @Override
    public void render(MatrixStack matrices, final int mouseX, final int mouseY, float partial) {

        Minecraft minecraft = Minecraft.getInstance();

        if(visible) {
            TextureManager textureManager = minecraft.getTextureManager();
            textureManager.bindTexture(TEXTURE_STATES);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            if(halfSize) {
                width = 8;
                height = 8;

                RenderSystem.pushMatrix();
                RenderSystem.translatef(x, y, 0.0F);

                RenderSystem.scalef(0.5f, 0.5f, 0.5f);
            } else {
                RenderSystem.pushMatrix();
                RenderSystem.translatef(x, y, 0.0F);
            }
            blit(matrices, 0, 0, 256 - 16, 256 - 16, 16, 16);
            RenderSystem.scalef(1f / 16f, 1f / 16f, 1f / 16f);
            if(active) {
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                RenderSystem.color4f(0.5f, 0.5f, 0.5f, 1.0f);
            }
            textureManager.bindTexture(texture);
            blit(matrices, 0, 0, 0, 0, 256, 256);
            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);

            if(isHovered()) {
                renderToolTip(matrices, mouseX, mouseY);
            }
        }
    }

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
        return halfSize ? 8 : 16;
    }

    @Override
    public int getTooltipAreaHeight() {
        return halfSize ? 8 : 16;
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return visible;
    }

    public void setHalfSize(final boolean halfSize) {
        this.halfSize = halfSize;
    }
}