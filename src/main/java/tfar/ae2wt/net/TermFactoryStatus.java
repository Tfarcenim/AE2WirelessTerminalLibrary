package tfar.ae2wt.net;

import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerLocator;
import appeng.container.implementations.WirelessCraftingStatusContainer;
import appeng.core.localization.GuiText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import tfar.ae2wt.wirelesscraftingterminal.WCTContainer;
import tfar.ae2wt.wirelesscraftingterminal.WCTGuiObject;

import javax.annotation.Nullable;

public class TermFactoryStatus implements INamedContainerProvider {

    private final ITerminalHost obj;
    private final ContainerLocator locator;

    public TermFactoryStatus(ITerminalHost obj, ContainerLocator locator) {

        this.obj = obj;
        this.locator = locator;
    }

    @Override
    public ITextComponent getDisplayName() {
        return  GuiText.Terminal.text();
    }

    @Nullable
    @Override
    public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {

        WirelessCraftingStatusContainer c = new WirelessCraftingStatusContainer(p_createMenu_1_, p_createMenu_2_, obj);
        // Set the original locator on the opened server-side container for it to more
        // easily remember how to re-open after being closed.
        c.setLocator(locator);
        return c;
    }
}
