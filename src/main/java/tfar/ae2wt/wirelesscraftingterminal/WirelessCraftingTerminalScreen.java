package tfar.ae2wt.wirelesscraftingterminal;

import appeng.api.config.ActionItems;
import appeng.client.gui.Icon;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.IconButton;
import appeng.container.slot.CraftingMatrixSlot;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tfar.ae2wt.net.PacketHandler;
import tfar.ae2wt.net.server.C2SDeleteTrashPacket;
import tfar.ae2wt.net.server.C2SSetMagnetModePacket;
import tfar.ae2wt.util.ItemButton;
import tfar.ae2wt.wirelesscraftingterminal.magnet_card.MagnetMode;
import tfar.ae2wt.wirelesscraftingterminal.magnet_card.MagnetSettings;
import tfar.ae2wt.wut.CycleTerminalButton;
import tfar.ae2wt.wut.IUniversalTerminalCapable;

import java.io.IOException;

public class WirelessCraftingTerminalScreen extends ItemTerminalScreen<WirelessCraftingTerminalContainer> implements IUniversalTerminalCapable {

    private int rows = 0;
    ItemButton magnetCardToggleButton;

    private static final ScreenStyle STYLE;

    static {
        ScreenStyle STYLE1;
        try {
            STYLE1 = StyleManager.loadStyleDoc("/screens/wtlib/wireless_crafting_terminal.json");
        } catch(IOException ignored) {
            STYLE1 = null;
        }
        STYLE = STYLE1;
    }

    public WirelessCraftingTerminalScreen(WirelessCraftingTerminalContainer container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title,STYLE);
        ActionButton clearBtn = new ActionButton(ActionItems.STASH, (btn) -> container.clearCraftingGrid());
        clearBtn.setHalfSize(true);
        widgets.add("clearCraftingGrid", clearBtn);
        IconButton deleteButton = new IconButton(btn -> delete()) {
            @Override
            protected Icon getIcon() {
                return Icon.CONDENSER_OUTPUT_TRASH;
            }
        };
        deleteButton.setHalfSize(true);
        deleteButton.setMessage(new TranslationTextComponent("gui.ae2wtlib.emptytrash").appendString("\n")
                .appendSibling(new TranslationTextComponent("gui.ae2wtlib.emptytrash.desc")));
        widgets.add("emptyTrash", deleteButton);

        magnetCardToggleButton = new ItemButton(new ResourceLocation("ae2wtlib", "textures/magnet_card.png"),btn -> setMagnetMode());
        magnetCardToggleButton.setHalfSize(true);
        widgets.add("magnetCardToggleButton", magnetCardToggleButton);
        resetMagnetSettings();
        getContainer().setScreen(this);

        if(getContainer().isWUT()) widgets.add("cycleTerminal", new CycleTerminalButton(btn -> cycleTerminal()));

        //widgets.add("player", new PlayerEntityWidget(MinecraftClient.getInstance().player));
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        super.drawBG(matrices, offsetX, offsetY, mouseX, mouseY, partialTicks);
        InventoryScreen.drawEntityOnScreen(offsetX + 52, offsetY + 150 + rows * 18, 30, (float) (offsetX + 52) - mouseX, (float) offsetY + 95 + rows * 18 - mouseY, minecraft.player);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawFG(matrices, offsetX, offsetY, mouseX, mouseY);
    }

    private void delete() {
        PacketHandler.INSTANCE.sendToServer(new C2SDeleteTrashPacket());
    }

    private MagnetSettings magnetSettings = null;

    public void resetMagnetSettings() {
        magnetSettings = container.getMagnetSettings();
        setMagnetModeText();
    }

    private void setMagnetMode() {
        switch(magnetSettings.magnetMode) {
            case NO_CARD:
                return;
            case OFF:
                magnetSettings.magnetMode = MagnetMode.PICKUP_INVENTORY;
                break;
            case PICKUP_INVENTORY:
                magnetSettings.magnetMode = MagnetMode.PICKUP_ME;
                break;
            case PICKUP_ME:
                magnetSettings.magnetMode = MagnetMode.OFF;
                break;
        }
        setMagnetModeText();
        PacketHandler.INSTANCE.sendToServer(new C2SSetMagnetModePacket(magnetSettings.magnetMode));
    }

    private void setMagnetModeText() {
        switch(magnetSettings.magnetMode) {
            case NO_CARD:
                magnetCardToggleButton.setVisibility(false);
                return;
            case OFF:
                magnetCardToggleButton.setMessage(new TranslationTextComponent("gui.ae2wtlib.magnetcard").appendString("\n").appendSibling(new TranslationTextComponent("gui.ae2wtlib.magnetcard.desc.off")));
                break;
            case PICKUP_INVENTORY:
                magnetCardToggleButton.setMessage(new TranslationTextComponent("gui.ae2wtlib.magnetcard").appendString("\n").appendSibling(new TranslationTextComponent("gui.ae2wtlib.magnetcard.desc.inv")));
                break;
            case PICKUP_ME:
                magnetCardToggleButton.setMessage(new TranslationTextComponent("gui.ae2wtlib.magnetcard").appendString("\n").appendSibling(new TranslationTextComponent("gui.ae2wtlib.magnetcard.desc.me")));
                break;
        }
        magnetCardToggleButton.setVisibility(true);
    }

    protected String getBackground() {
        return "wtlib/gui/crafting.png";
    }

    //todo jei
   /* @Override
    public List<Rectangle> getExclusionZones() {
        List<Rectangle> zones = super.getExclusionZones();
        zones.add(new Rectangle(guiTop + 195, y, 24,ySize - 110));
        return zones;
    }*/
}