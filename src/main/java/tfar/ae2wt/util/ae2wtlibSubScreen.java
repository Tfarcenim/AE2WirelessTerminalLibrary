package tfar.ae2wt.util;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import tfar.ae2wt.init.Menus;
import tfar.ae2wt.init.ModItems;
import tfar.ae2wt.net.C2SSwitchGuiPacket;
import tfar.ae2wt.net.PacketHandler;
import tfar.ae2wt.wirelesscraftingterminal.WCTGuiObject;
import tfar.ae2wt.wpt.WPTGuiObject;

import javax.annotation.Nullable;
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
            previousContainerIcon = new ItemStack(ModItems.CRAFTING_TERMINAL);
            previousContainerType = Menus.WCT;
        } else if(containerTarget instanceof WPTGuiObject) {
            previousContainerIcon = new ItemStack(ModItems.PATTERN_TERMINAL);
            previousContainerType = Menus.PATTERN;
        } else {
            previousContainerIcon = null;
            previousContainerType = null;
        }
    }

    public TabButton addBackButton(String id, WidgetContainer widgets) {
        return this.addBackButton(id, widgets, null);
    }

    public TabButton addBackButton(String id, WidgetContainer widgets, @Nullable ITextComponent label) {
        if (this.previousContainerType != null && !this.previousContainerIcon.isEmpty()) {
            if (label == null) {
                label = this.previousContainerIcon.getDisplayName();
            }

            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            TabButton button = new TabButton(this.previousContainerIcon, label, itemRenderer, (btn) -> {
                this.goBack();
            });
            widgets.add(id, button);
            return button;
        } else {
            return null;
        }
    }

    public final void goBack() {
        PacketHandler.INSTANCE.sendToServer(new C2SSwitchGuiPacket(Registry.MENU.getKey(previousContainerType).getPath()));
    }
}