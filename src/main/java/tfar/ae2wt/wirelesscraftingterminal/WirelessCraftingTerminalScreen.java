package tfar.ae2wt.wirelesscraftingterminal;

import appeng.api.config.ActionItems;
import appeng.client.gui.me.common.MEMonitorableScreen;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
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
import tfar.ae2wt.net.server.C2SDeleteTrashPacket;
import tfar.ae2wt.net.PacketHandler;
import tfar.ae2wt.net.server.C2SSetMagnetModePacket;
import tfar.ae2wt.util.ItemButton;
import tfar.ae2wt.wirelesscraftingterminal.magnet_card.MagnetMode;
import tfar.ae2wt.wirelesscraftingterminal.magnet_card.MagnetSettings;
import tfar.ae2wt.wut.CycleTerminalButton;
import tfar.ae2wt.wut.IUniversalTerminalCapable;

import java.lang.reflect.Field;

public class WirelessCraftingTerminalScreen extends ItemTerminalScreen<WirelessCraftingTerminalContainer> implements IUniversalTerminalCapable {

    private int rows = 0;
    private AETextField searchField;
    private final int reservedSpace;
    ItemButton magnetCardToggleButton;

    public WirelessCraftingTerminalScreen(WirelessCraftingTerminalContainer container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title,style);
        reservedSpace = 73;
    }

    @Override
    public void init() {
        super.init();
        ActionButton clearBtn = addButton(new ActionButton(ActionItems.STASH, btn -> clear()));//guiLeft+ 92 + 43, guiTop+ySize - 156 - 4,
        clearBtn.setHalfSize(true);

       // IconButton deleteButton = addButton(new ActionButton(guiLeft+ 92 + 25, guiTop+ySize - 104, btn -> delete()));
       // deleteButton.setHalfSize(true);
       // deleteButton.setMessage(new TranslationTextComponent("gui.ae2wtlib.emptytrash").appendString("\n").appendSibling(new TranslationTextComponent("gui.ae2wtlib.emptytrash.desc")));

        magnetCardToggleButton = addButton(new ItemButton(guiLeft + 92 + 60, guiTop + ySize - 114, btn -> setMagnetMode(), new ResourceLocation("ae2wtlib", "textures/magnet_card.png")));

        magnetCardToggleButton.setHalfSize(true);
        container.setScreen(this);

        if(container.isWUT()) addButton(new CycleTerminalButton(guiLeft- 18, guiTop+ 88, btn -> cycleTerminal()));

        try {
            Field field = MEMonitorableScreen.class.getDeclaredField("rows");
            field.setAccessible(true);
            Object value = field.get(this);
            rows = (int) value;
        } catch(IllegalAccessException | NoSuchFieldException ignored) {}
        try {
            Field field = MEMonitorableScreen.class.getDeclaredField("searchField");
            field.setAccessible(true);
            Object value = field.get(this);
            searchField = (AETextField) value;
        } catch(IllegalAccessException | NoSuchFieldException ignored) {}
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {

        bindTexture(getBackground());
        final int x_width = 197;
        blit(matrices, offsetX, offsetY, 0, 0, x_width, 18);

        for(int x = 0; x < this.rows; x++) {
            blit(matrices, offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
        }

        blit(matrices, offsetX, offsetY + 16 + rows * 18, 0, 106 - 18 - 18, x_width, 99 + reservedSpace);

        searchField.render(matrices, mouseX, mouseY, partialTicks);

        if(minecraft.player != null)
            InventoryScreen.drawEntityOnScreen(offsetX + 52, offsetY + 94 + rows * 18, 30, (float) (offsetX + 52) - mouseX, (float) offsetY + 55 + rows * 18 - mouseY, minecraft.player);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawFG(matrices, offsetX, offsetY, mouseX, mouseY);
        font.drawText(matrices, GuiText.CraftingTerminal.text(), 8,ySize - 96 + 1 - reservedSpace, 4210752);
    }

    private void clear() {
        Slot s = null;
        for(final Slot j : container.inventorySlots) {
            if(j instanceof CraftingMatrixSlot) {
                s = j;
            }
        }

        if(s != null) {
            final InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, s.slotNumber, 0);
            NetworkHandler.instance().sendToServer(p);
        }
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