package tfar.ae2wt.wirelessfluidterminal;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.me.common.MEMonitorableScreen;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IScrollSource;
import appeng.container.me.common.GridInventoryEntry;
import appeng.container.me.fluids.FluidTerminalContainer;
import appeng.core.AELog;
import appeng.fluids.client.gui.FluidBlitter;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import tfar.ae2wt.ae2copies.FluidRepo;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WirelessFluidTerminalScreen extends MEMonitorableScreen<IAEFluidStack, WirelessFluidTerminalContainer> {
    public WirelessFluidTerminalScreen(WirelessFluidTerminalContainer container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
    }

    protected Repo<IAEFluidStack> createRepo(IScrollSource scrollSource) {
        return new FluidRepo(scrollSource, this);
    }

    protected IPartitionList<IAEFluidStack> createPartitionList(List<ItemStack> viewCells) {
        return null;
    }

    protected void renderGridInventoryEntry(MatrixStack matrices, int x, int y, GridInventoryEntry<IAEFluidStack> entry) {
        IAEFluidStack fs = entry.getStack();
        FluidBlitter.create(fs.getFluidStack()).dest(x, y, 16, 16).blit(matrices, this.getBlitOffset());
    }

    protected void renderGridInventoryEntryTooltip(MatrixStack matrices, GridInventoryEntry<IAEFluidStack> entry, int x, int y) {
        IAEFluidStack fluidStack = entry.getStack();
        String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format((double)entry.getStoredAmount() / 1000.0D) + " B";
        String modName = Platform.getModName(Platform.getModId(fluidStack));
        List<ITextComponent> list = new ArrayList<>();
        list.add(fluidStack.getFluidStack().getDisplayName());
        list.add(new StringTextComponent(formattedAmount));
        list.add(new StringTextComponent(modName));
        this.renderWrappedToolTip(matrices, list, x, y, this.font);
    }

    protected void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry<IAEFluidStack> entry, int mouseButton, ClickType clickType) {
        if (clickType == ClickType.PICKUP) {
            if (mouseButton == 0 && entry != null) {
                AELog.debug("mouse0 GUI STACK SIZE %s", entry.getStoredAmount());
                this.container.handleInteraction(entry.getSerial(), InventoryAction.FILL_ITEM);
            } else {
                if (entry != null) {
                    AELog.debug("mouse1 GUI STACK SIZE %s", entry.getStoredAmount());
                }

                this.container.handleInteraction(-1L, InventoryAction.EMPTY_ITEM);
            }
        }

    }
}
