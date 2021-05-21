package tfar.ae2wt.util;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.TabButton;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.net.C2SSwitchGuiPacket;
import tfar.ae2wt.net.PacketHandler;
import tfar.ae2wt.wirelesscraftingterminal.WCTContainer;
import tfar.ae2wt.wirelesscraftingterminal.WCTGuiObject;
import tfar.ae2wt.wpt.WPatternTContainer;
import tfar.ae2wt.wpt.WPTGuiObject;

import java.util.function.Consumer;

public final class ae2wtlibSubScreen {

    private final AEBaseScreen<?> gui;
    private final ContainerType<?> previousContainerType;
    private final ItemStack previousContainerIcon;

    /**
     * Based on the container we're opening for, try to determine what it's "primary" GUI would be so that we can go
     * back to it.
     */
    public ae2wtlibSubScreen(AEBaseScreen<?> gui, Object containerTarget) {
        this.gui = gui;
        if(containerTarget instanceof WCTGuiObject) {//TODO don't hardcode
            previousContainerIcon = new ItemStack(AE2WirelessTerminals.CRAFTING_TERMINAL);
            previousContainerType = WCTContainer.WCT;
        } else if(containerTarget instanceof WPTGuiObject) {
            previousContainerIcon = new ItemStack(AE2WirelessTerminals.PATTERN_TERMINAL);
            previousContainerType = WPatternTContainer.TYPE;
        } else {
            previousContainerIcon = null;
            previousContainerType = null;
        }
    }

    public final TabButton addBackButton(Consumer<TabButton> buttonAdder, int x, int y) {
        return addBackButton(buttonAdder, x, y, null);
    }

    public final TabButton addBackButton(Consumer<TabButton> buttonAdder, int x, int y, ITextComponent label) {
        if(previousContainerType != null && !previousContainerIcon.isEmpty()) {
            if(label == null) label = previousContainerIcon.getDisplayName();
            ItemRenderer itemRenderer = gui.getMinecraft().getItemRenderer();
            TabButton button = new TabButton(gui.getGuiLeft() + x, gui.getGuiTop() + y, previousContainerIcon, label, itemRenderer, btn -> goBack());
            buttonAdder.accept(button);
            return button;
        }
        return null;
    }

    public final void goBack() {
        PacketHandler.INSTANCE.sendToServer(new C2SSwitchGuiPacket(Registry.MENU.getKey(previousContainerType).getPath()));
    }
}