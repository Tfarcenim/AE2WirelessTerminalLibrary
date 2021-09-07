package tfar.ae2wt.wpt;

import appeng.api.config.ActionItems;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.TabButton;
import appeng.container.SlotSemantic;
import appeng.core.localization.GuiText;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import tfar.ae2wt.net.PacketHandler;
import tfar.ae2wt.net.server.C2SClearPatternPacket;
import tfar.ae2wt.net.server.C2SEncodePatternPacket;
import tfar.ae2wt.net.server.C2STogglePatternCraftingModePacket;
import tfar.ae2wt.net.server.C2STogglePatternSubsitutionPacket;
import tfar.ae2wt.wut.CycleTerminalButton;
import tfar.ae2wt.wut.IUniversalTerminalCapable;

public class WirelessPatternTerminalScreen extends ItemTerminalScreen<WirelessPatternTerminalContainer> implements IUniversalTerminalCapable {


    private static final boolean SUBSITUTION_DISABLE = false;
    private static final boolean SUBSITUTION_ENABLE = true;

    private static final boolean CRAFTMODE_CRAFTING = true;
    private static final boolean CRAFTMODE_PROCESSING = false;

    private final TabButton tabCraftButton;
    private final TabButton tabProcessButton;
    private final ActionButton substitutionsEnabledBtn;
    private final ActionButton substitutionsDisabledBtn;

    public WirelessPatternTerminalScreen(WirelessPatternTerminalContainer container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title,style);

        tabCraftButton = new TabButton(new ItemStack(Blocks.CRAFTING_TABLE), GuiText.CraftingPattern.text(), itemRenderer, btn -> toggleCraftMode(CRAFTMODE_PROCESSING));
        widgets.add("craftingPatternMode", tabCraftButton);

        tabProcessButton = new TabButton(new ItemStack(Blocks.FURNACE), GuiText.ProcessingPattern.text(), itemRenderer, btn -> toggleCraftMode(CRAFTMODE_CRAFTING));
        widgets.add("processingPatternMode", tabProcessButton);

        substitutionsEnabledBtn = new ActionButton(ActionItems.ENABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSITUTION_DISABLE));
        substitutionsEnabledBtn.setHalfSize(true);
        widgets.add("substitutionsEnabled", substitutionsEnabledBtn);

        substitutionsDisabledBtn = new ActionButton(ActionItems.DISABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSITUTION_ENABLE));
        substitutionsDisabledBtn.setHalfSize(true);
        widgets.add("substitutionsDisabled", substitutionsDisabledBtn);

        ActionButton clearBtn = addButton(new ActionButton(ActionItems.CLOSE, btn -> clear()));
        clearBtn.setHalfSize(true);
        widgets.add("clearPattern", clearBtn);
        widgets.add("encodePattern", new ActionButton(ActionItems.ENCODE, act -> encode()));

        if(container.isWUT()) widgets.add("cycleTerminal", new CycleTerminalButton(btn -> cycleTerminal()));
    }

    private void toggleCraftMode(boolean mode) {
        PacketHandler.INSTANCE.sendToServer(new C2STogglePatternCraftingModePacket(mode));
    }

    private void toggleSubstitutions(boolean mode) {
        PacketHandler.INSTANCE.sendToServer(new C2STogglePatternSubsitutionPacket(mode));
    }

    private void encode() {
        PacketHandler.INSTANCE.sendToServer(new C2SEncodePatternPacket());
    }

    private void clear() {
        PacketHandler.INSTANCE.sendToServer(new C2SClearPatternPacket());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if(container.isCraftingMode()) {
            tabCraftButton.visible = true;
            tabProcessButton.visible = false;
            if(container.substitute) {
                substitutionsEnabledBtn.visible = true;
                substitutionsDisabledBtn.visible = false;
            } else {
                substitutionsEnabledBtn.visible = false;
                substitutionsDisabledBtn.visible = true;
            }
        } else {
            tabCraftButton.visible = false;
            tabProcessButton.visible = true;
            substitutionsEnabledBtn.visible = false;
            substitutionsDisabledBtn.visible = false;
        }

        setSlotsHidden(SlotSemantic.CRAFTING_RESULT, !container.isCraftingMode());
        setSlotsHidden(SlotSemantic.PROCESSING_RESULT, container.isCraftingMode());
    }

    @Override
    public void drawBG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
        if(container.isCraftingMode()) return;
        Blitter.texture("guis/pattern_modes.png").src(100, 77, 18, 54).dest(guiLeft + 109, guiTop + ySize - 159).blit(matrixStack, getBlitOffset());
    }

    /*@Override
    public List<Rectangle> getExclusionZones() {
        List<Rectangle> zones = super.getExclusionZones();
        zones.add(new Rectangle(guiLeft + 195, y, 24, backgroundHeight-110));
        return zones;
    }*/
}