package tfar.ae2wtlib.util;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.Scrollbar;
import appeng.container.implementations.WirelessCraftConfirmContainer;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.util.Platform;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WirelessCraftConfirmScreen extends AEBaseScreen<WirelessCraftConfirmContainer> {

    private final ae2wtlibSubScreen subGui;

    private final int rows = 5;

    private final IItemList<IAEItemStack> storage = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
    private final IItemList<IAEItemStack> pending = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
    private final IItemList<IAEItemStack> missing = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();

    private final List<IAEItemStack> visual = new ArrayList<>();

    private Button start;
    private Button selectCPU;
    private int tooltip = -1;

    public WirelessCraftConfirmScreen(WirelessCraftConfirmContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        subGui = new ae2wtlibSubScreen(this, container.getTarget());
        xSize = 238;
        ySize = 206;

        final Scrollbar scrollbar = new Scrollbar();
        setScrollBar(scrollbar);
    }

    @Override
    public void init() {
        super.init();

        start = new Button(guiLeft + 162, guiTop + ySize - 25, 50, 20, GuiText.Start.text(), btn -> start());
        start.active = false;
        addButton(start);

        selectCPU = new Button(guiLeft + (219 - 180) / 2, guiTop + ySize - 68, 180, 20, getNextCpuButtonLabel(), btn -> selectNextCpu());
        selectCPU.active = false;
        addButton(selectCPU);

        addButton(new Button(guiLeft + 6, guiTop + ySize - 25, 50, 20, GuiText.Cancel.text(), btn -> subGui.goBack()));

        setScrollBar();
    }

    @Override
    public void render(MatrixStack matrices, final int mouseX, final int mouseY, final float btn) {
        updateCPUButtonText();

        start.active = !(container.hasNoCPU() || isSimulation());
        selectCPU.active = !isSimulation();

        final int gx = (width - xSize) / 2;
        final int gy = (height - ySize) / 2;

        tooltip = -1;

        final int offY = 23;
        int y = 0;
        int x = 0;
        for(int z = 0; z <= 4 * 5; z++) {
            final int minX = gx + 9 + x * 67;
            final int minY = gy + 22 + y * offY;

            if(minX < mouseX && minX + 67 > mouseX) {
                if(minY < mouseY && minY + offY - 2 > mouseY) {
                    tooltip = z;
                    break;
                }
            }

            x++;

            if(x > 2) {
                y++;
                x = 0;
            }
        }
        super.render(matrices, mouseX, mouseY, btn);
    }

    private void updateCPUButtonText() {
        selectCPU.setMessage(getNextCpuButtonLabel());
    }

    private ITextComponent getNextCpuButtonLabel() {
        if(container.hasNoCPU()) {
            return GuiText.NoCraftingCPUs.text();
        }

        ITextComponent cpuName;
        if(container.cpuName == null) {
            cpuName = GuiText.Automatic.text();
        } else {
            cpuName = container.cpuName;
        }

        return GuiText.CraftingCPU.withSuffix(": ").appendSibling(cpuName);
    }

    private boolean isSimulation() {
        return container.isSimulation();
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        final long BytesUsed = container.getUsedBytes();
        final String byteUsed = NumberFormat.getInstance().format(BytesUsed);
        final ITextComponent Add = BytesUsed > 0 ? new StringTextComponent(byteUsed + " ").appendSibling(GuiText.BytesUsed.text()) : GuiText.CalculatingWait.text();
        font.drawText(matrices, GuiText.CraftingPlan.withSuffix(" - ").appendSibling(Add), 8, 7, 0x404040);

        ITextComponent dsp;

        if(isSimulation()) {
            dsp = GuiText.Simulation.text();
        } else {
            dsp = container.getCpuAvailableBytes() > 0 ? (GuiText.Bytes.withSuffix(": " + container.getCpuAvailableBytes() + " : ")
                    .appendSibling(GuiText.CoProcessors.text()).appendString(": " + container.getCpuCoProcessors()))
                    : GuiText.Bytes.withSuffix(": N/A : ").appendSibling(GuiText.CoProcessors.text()).appendString(": N/A");
        }

        final int offset = (219 - font.getStringPropertyWidth(dsp)) / 2;
        font.drawText(matrices, dsp, offset, 165, 4210752);

        final int sectionLength = 67;

        int x = 0;
        int y = 0;
        final int xo = 9;
        final int yo = 22;
        final int viewStart = getScrollBar().getCurrentScroll() * 3;
        final int viewEnd = viewStart + 3 * rows;

        List<ITextComponent> dspToolTip = new ArrayList<>();
        final List<ITextComponent> lineList = new ArrayList<>();
        int toolPosX = 0;
        int toolPosY = 0;

        final int offY = 23;

        for(int z = viewStart; z < Math.min(viewEnd, visual.size()); z++) {
            final IAEItemStack refStack = visual.get(z);// repo.getReferenceItem( z );
            if(refStack != null) {
                RenderSystem.pushMatrix();
                RenderSystem.scalef(0.5f, 0.5f, 0.5f);

                final IAEItemStack stored = storage.findPrecise(refStack);
                final IAEItemStack pendingStack = pending.findPrecise(refStack);
                final IAEItemStack missingStack = missing.findPrecise(refStack);

                int lines = 0;

                if(stored != null && stored.getStackSize() > 0) {
                    lines++;
                }
                if(missingStack != null && missingStack.getStackSize() > 0) {
                    lines++;
                }
                if(pendingStack != null && pendingStack.getStackSize() > 0) {
                    lines++;
                }

                final int negY = ((lines - 1) * 5) / 2;
                int downY = 0;

                int i = y * offY + yo + 6 - negY;
                if(stored != null && stored.getStackSize() > 0) {
                    String str = Long.toString(stored.getStackSize());
                    if(stored.getStackSize() >= 10000) {
                        str = Long.toString(stored.getStackSize() / 1000) + 'k';
                    }
                    if(stored.getStackSize() >= 10000000) {
                        str = Long.toString(stored.getStackSize() / 1000000) + 'm';
                    }

                    str = GuiText.FromStorage.getLocal() + ": " + str;
                    final int w = 4 + font.getStringWidth(str);
                    font.drawString(matrices, str, (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2),
                            (i + downY) * 2, 4210752);

                    if(tooltip == z - viewStart) {
                        lineList.add(GuiText.FromStorage.withSuffix(": " + stored.getStackSize()));
                    }

                    downY += 5;
                }

                boolean red = false;
                if(missingStack != null && missingStack.getStackSize() > 0) {
                    String str = Long.toString(missingStack.getStackSize());
                    if(missingStack.getStackSize() >= 10000) {
                        str = Long.toString(missingStack.getStackSize() / 1000) + 'k';
                    }
                    if(missingStack.getStackSize() >= 10000000) {
                        str = Long.toString(missingStack.getStackSize() / 1000000) + 'm';
                    }

                    str = GuiText.Missing.getLocal() + ": " + str;
                    final int w = 4 + font.getStringWidth(str);
                    font.drawString(matrices, str,
                            (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2),
                            (i + downY) * 2, 0x404040);

                    if(tooltip == z - viewStart) {
                        lineList.add(GuiText.Missing.withSuffix(": " + missingStack.getStackSize()));
                    }

                    red = true;
                    downY += 5;
                }

                if(pendingStack != null && pendingStack.getStackSize() > 0) {
                    String str = Long.toString(pendingStack.getStackSize());
                    if(pendingStack.getStackSize() >= 10000) {
                        str = Long.toString(pendingStack.getStackSize() / 1000) + 'k';
                    }
                    if(pendingStack.getStackSize() >= 10000000) {
                        str = Long.toString(pendingStack.getStackSize() / 1000000) + 'm';
                    }

                    str = GuiText.ToCraft.getLocal() + ": " + str;
                    final int w = 4 + font.getStringWidth(str);
                    font.drawString(matrices, str,
                            (int) ((x * (1 + sectionLength) + xo + sectionLength - 19 - (w * 0.5)) * 2),
                            (i + downY) * 2, 4210752);

                    if(tooltip == z - viewStart) {
                        lineList.add(GuiText.ToCraft.withSuffix(": " + pendingStack.getStackSize()));
                    }
                }

                RenderSystem.popMatrix();
                final int posX = x * (1 + sectionLength) + xo + sectionLength - 19;
                final int posY = y * offY + yo;

                final ItemStack is = refStack.asItemStackRepresentation();

                if(tooltip == z - viewStart) {
                    dspToolTip.add(Platform.getItemDisplayName(refStack));

                    if(lineList.size() > 0) {
                        dspToolTip.addAll(lineList);
                    }

                    toolPosX = x * (1 + sectionLength) + xo + sectionLength - 8;
                    toolPosY = y * offY + yo;
                }

                drawItem(posX, posY, is);

                if(red) {
                    final int startX = x * (1 + sectionLength) + xo;
                    final int startY = posY - 4;
                    fill(matrices, startX, startY, startX + sectionLength, startY + offY, 0x1AFF0000);
                }

                x++;

                if(x > 2) {
                    y++;
                    x = 0;
                }
            }
        }

        if(tooltip >= 0 && !dspToolTip.isEmpty()) drawTooltip(matrices, toolPosX, toolPosY + 10, dspToolTip);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        setScrollBar();
        bindTexture("guis/craftingreport.png");
        blit(matrices, offsetX, offsetY, 0, 0, xSize, ySize);
    }

    private void setScrollBar() {
        final int size = visual.size();

        getScrollBar().setTop(19).setLeft(218).setHeight(114);
        getScrollBar().setRange(0, (size + 2) / 3 - rows, 1);
    }

    public void postUpdate(final List<IAEItemStack> list, final byte ref) {
        switch(ref) {
            case 0:
                for(final IAEItemStack l : list) handleInput(storage, l);
                break;
            case 1:
                for(final IAEItemStack l : list) handleInput(pending, l);
                break;
            case 2:
                for(final IAEItemStack l : list) handleInput(missing, l);
                break;
        }

        for(final IAEItemStack l : list) {
            final long amt = getTotal(l);

            if(amt <= 0) {
                deleteVisualStack(l);
            } else {
                final IAEItemStack is = findVisualStack(l);
                is.setStackSize(amt);
            }
        }
        setScrollBar();
    }

    private void handleInput(final IItemList<IAEItemStack> s, final IAEItemStack l) {
        IAEItemStack a = s.findPrecise(l);

        if(l.getStackSize() <= 0) {
            if(a != null) a.reset();
        } else {
            if(a == null) {
                s.add(l.copy());
                a = s.findPrecise(l);
            }

            if(a != null) a.setStackSize(l.getStackSize());
        }
    }

    private long getTotal(final IAEItemStack is) {
        final IAEItemStack a = storage.findPrecise(is);
        final IAEItemStack c = pending.findPrecise(is);
        final IAEItemStack m = missing.findPrecise(is);

        long total = 0;

        if(a != null) total += a.getStackSize();

        if(c != null) total += c.getStackSize();

        if(m != null) total += m.getStackSize();

        return total;
    }

    private void deleteVisualStack(final IAEItemStack l) {
        final Iterator<IAEItemStack> i = visual.iterator();
        while(i.hasNext()) {
            final IAEItemStack o = i.next();
            if(o.equals(l)) {
                i.remove();
                return;
            }
        }
    }

    private IAEItemStack findVisualStack(final IAEItemStack l) {
        for(final IAEItemStack o : visual) {
            if(o.equals(l)) return o;
        }

        final IAEItemStack stack = l.copy();
        visual.add(stack);
        return stack;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if(!checkHotbarKeys(InputMappings.getInputByCode(keyCode, scanCode))) {
            if(keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                start();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    private void selectNextCpu() {
        final boolean backwards = isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("Terminal.Cpu", backwards ? "Prev" : "Next"));
    }

    private void start() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("Terminal.Start", "Start"));
    }
}