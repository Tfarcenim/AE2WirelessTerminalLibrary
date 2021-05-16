package tfar.ae2wtlib.wpt;

import appeng.api.config.ActionItems;
import appeng.client.gui.implementations.MEMonitorableScreen;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;
import tfar.ae2wtlib.net.C2SGeneralPacket;
import tfar.ae2wtlib.net.PacketHandler;
import tfar.ae2wtlib.wut.CycleTerminalButton;
import tfar.ae2wtlib.wut.IUniversalTerminalCapable;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;

public class WPTScreen extends MEMonitorableScreen<WPatternTContainer> implements IUniversalTerminalCapable {

    private int rows = 0;
    private AETextField searchField;
    private final int reservedSpace;
    private final WPatternTContainer container;

    private static final byte SUBSITUTION_DISABLE = 0;
    private static final byte SUBSITUTION_ENABLE = 1;

    private static final byte CRAFTMODE_CRAFTING = 1;
    private static final byte CRAFTMODE_PROCESSING = 0;

    private TabButton tabCraftButton;
    private TabButton tabProcessButton;
    private ActionButton substitutionsEnabledBtn;
    private ActionButton substitutionsDisabledBtn;

    public WPTScreen(WPatternTContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        reservedSpace = 81;

        try {
            Field f = MEMonitorableScreen.class.getDeclaredField("reservedSpace");
            f.setAccessible(true);
            f.set(this, reservedSpace);
            f.setAccessible(false);
        } catch(IllegalAccessException | NoSuchFieldException ignored) {}
    }

    @Override
    public void init() {
        super.init();

        tabCraftButton = new TabButton(guiLeft + 173, guiTop + ySize - 177,
                new ItemStack(Blocks.CRAFTING_TABLE), GuiText.CraftingPattern.text(), itemRenderer,
                btn -> toggleCraftMode(CRAFTMODE_PROCESSING));
        addButton(tabCraftButton);

        tabProcessButton = new TabButton(guiLeft + 173, guiTop + ySize - 177,
                new ItemStack(Blocks.FURNACE), GuiText.ProcessingPattern.text(), itemRenderer,
                btn -> toggleCraftMode(CRAFTMODE_CRAFTING));
        addButton(tabProcessButton);

        substitutionsEnabledBtn = new ActionButton(guiLeft + 84, guiTop + ySize - 165, ActionItems.ENABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSITUTION_DISABLE));
        substitutionsEnabledBtn.setHalfSize(true);
        addButton(substitutionsEnabledBtn);

        substitutionsDisabledBtn = new ActionButton(guiLeft + 84, guiTop + ySize - 165, ActionItems.DISABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSITUTION_ENABLE));
        substitutionsDisabledBtn.setHalfSize(true);
        addButton(substitutionsDisabledBtn);

        ActionButton clearBtn = addButton(new ActionButton(guiLeft + 74, guiTop + ySize - 165, ActionItems.CLOSE, btn -> clear()));
        clearBtn.setHalfSize(true);

        ActionButton encodeBtn = new ActionButton(guiLeft + 147, guiTop + ySize - 144, ActionItems.ENCODE, act -> encode());
        addButton(encodeBtn);

        if(container.isWUT()) addButton(new CycleTerminalButton(guiLeft - 18, guiTop + 88, btn -> cycleTerminal()));

        try {
            Field field = MEMonitorableScreen.class.getDeclaredField("rows");
            field.setAccessible(true);
            Object value = field.get(this);
            field.setAccessible(false);
            rows = (int) value;
        } catch(IllegalAccessException | NoSuchFieldException ignored) {}
        try {
            Field field = MEMonitorableScreen.class.getDeclaredField("searchField");
            field.setAccessible(true);
            Object value = field.get(this);
            field.setAccessible(false);
            searchField = (AETextField) value;
        } catch(IllegalAccessException | NoSuchFieldException ignored) {}
    }

    private void toggleCraftMode(byte mode) {
        PacketHandler.INSTANCE.sendToServer(new C2SGeneralPacket("PatternTerminal.CraftMode",mode));
    }

    private void toggleSubstitutions(byte mode) {
        PacketHandler.INSTANCE.sendToServer(new C2SGeneralPacket("PatternTerminal.Subsitute",mode));
    }

    private void encode() {
        PacketHandler.INSTANCE.sendToServer(new C2SGeneralPacket("PatternTerminal.Encode",0));
    }

    private void clear() {
        PacketHandler.INSTANCE.sendToServer(new C2SGeneralPacket("PatternTerminal.Clear",0));
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {

        bindTexture(getBackground());
        final int x_width = 197;
        blit(matrices, offsetX, offsetY, 0, 0, x_width, 18);

        for(int x = 0; x < rows; x++) blit(matrices, offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);

        blit(matrices, offsetX, offsetY + 16 + rows * 18, 0, 106 - 18 - 18, x_width, 99 + reservedSpace);

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
            blit(matrices, offsetX + 109, offsetY + 36 + rows * 18, 109, 108, 18, 18);
            blit(matrices, offsetX + 109, offsetY + 72 + rows * 18, 109, 108, 18, 18);
        }
        searchField.render(matrices, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawFG(matrices, offsetX, offsetY, mouseX, mouseY);
        font.drawText(matrices, GuiText.PatternTerminal.text(), 8, ySize - 96 + 1 - reservedSpace, 4210752);
    }

    @Override
    protected String getBackground() {
        return "wtlib/gui/pattern.png";
    }

    /*@Override
    public List<Rectangle> getExclusionZones() {
        List<Rectangle> zones = super.getExclusionZones();
        zones.add(new Rectangle(guiLeft + 195, y, 24, backgroundHeight-110));
        return zones;
    }*/
}