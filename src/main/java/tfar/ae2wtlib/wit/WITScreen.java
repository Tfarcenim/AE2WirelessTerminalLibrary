package tfar.ae2wtlib.wit;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.me.ClientDCInternalInv;
import appeng.client.me.SlotDisconnected;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import com.google.common.collect.HashMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import tfar.ae2wtlib.wut.CycleTerminalButton;
import tfar.ae2wtlib.wut.IUniversalTerminalCapable;

import java.util.*;

public class WITScreen extends AEBaseScreen<WITContainer> implements IUniversalTerminalCapable {

    private static final int LINES_ON_PAGE = 6;

    private final HashMap<Long, ClientDCInternalInv> byId = new HashMap<>();
    private final HashMultimap<String, ClientDCInternalInv> byName = HashMultimap.create();
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<Object> lines = new ArrayList<>();

    private final Map<String, Set<Object>> cachedSearches = new WeakHashMap<>();

    private boolean refreshList = false;
    private AETextField searchField;
    private final WITContainer container;

    public WITScreen(WITContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        setScrollBar(new Scrollbar().setLeft(175).setTop(18).setHeight(106));
        xSize = 195;
        ySize = 222;
    }

    @Override
    public void init() {
        super.init();

        searchField = new AETextField(font, guiLeft + 104, guiTop + 4, 65, 12);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setMaxStringLength(25);
        searchField.setTextColor(0xFFFFFF);
        searchField.setSelectionColor(0xFF008000);
        searchField.setResponder(str -> refreshList());
        addListener(searchField);
        changeFocus(true);

        if(container.isWUT()) addButton(new CycleTerminalButton(guiLeft - 18, guiTop + 8, btn -> cycleTerminal()));
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        font.drawText(matrices, getGuiDisplayName(GuiText.InterfaceTerminal.text()), 8, 6, 4210752);
        font.drawText(matrices, GuiText.inventory.text(), 8, ySize - 96 + 3, 4210752);

        final int ex = getScrollBar().getCurrentScroll();

        container.inventorySlots.removeIf(slot -> slot instanceof SlotDisconnected);

        int offset = 17;
        for(int x = 0; x < LINES_ON_PAGE && ex + x < lines.size(); x++) {
            final Object lineObj = lines.get(ex + x);
            if(lineObj instanceof ClientDCInternalInv) {
                final ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;
                for(int z = 0; z < inv.getInventory().getSlots(); z++) {
                    container.inventorySlots.add(new SlotDisconnected(inv, z, z * 18 + 8, 1 + offset));
                }
            } else if(lineObj instanceof String) {
                String name = (String) lineObj;
                final int rows = byName.get(name).size();
                if(rows > 1) {
                    name = name + " (" + rows + ')';
                }

                while(name.length() > 2 && font.getStringWidth(name) > 155) {
                    name = name.substring(0, name.length() - 1);
                }

                font.drawString(matrices, name, 10, 6 + offset, 0x404040);
            }
            offset += 18;
        }
    }

    @Override
    public boolean mouseClicked(final double xCoord, final double yCoord, final int btn) {
        if(btn == 1 && searchField.isMouseOver(xCoord, yCoord)) {
            searchField.setText("");
            return true;
        }

        return super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        bindTexture("wtlib/gui/interface.png");
        blit(matrices, offsetX, offsetY, 0, 0, xSize, ySize);

        int offset = 17;
        final int ex = getScrollBar().getCurrentScroll();

        for(int x = 0; x < LINES_ON_PAGE && ex + x < lines.size(); x++) {
            final Object lineObj = lines.get(ex + x);
            if(lineObj instanceof ClientDCInternalInv) {
                final ClientDCInternalInv inv = (ClientDCInternalInv) lineObj;

                final int width = inv.getInventory().getSlots() * 18;
                blit(matrices, offsetX + 7, offsetY + offset, 7, 139, width, 18);
            }
            offset += 18;
        }

        if(searchField != null) searchField.render(matrices, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean charTyped(char character, int key) {
        if(character == ' ' && searchField.getText().isEmpty()) return true;
        return super.charTyped(character, key);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if(keyCode != GLFW.GLFW_KEY_ESCAPE) {
            if(/*AppEng.instance().isActionKey(ActionKey.TOGGLE_FOCUS, keyCode, scanCode)*/true) {
                //todo mixin?
              //  searchField.setFocused(!searchField.isFocused());
                return true;
            }

            // Forward keypresses to the search field
            if(searchField.isFocused()) {
                if(keyCode == GLFW.GLFW_KEY_ENTER) {
          //          searchField.setFocused(false);
                    return true;
                }

                searchField.keyPressed(keyCode, scanCode, p_keyPressed_3_);

                // We need to swallow key presses if the field is focused because typing 'e'
                // would otherwise close the screen
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    public void postUpdate(final CompoundNBT in) {
        if(in.getBoolean("clear")) {
            byId.clear();
            refreshList = true;
        }

        for(final String key : in.keySet()) {
            if(key.startsWith("=")) {
                try {
                    final long id = Long.parseLong(key.substring(1), Character.MAX_RADIX);
                    final CompoundNBT invData = in.getCompound(key);
                    ITextComponent un = ITextComponent.Serializer.getComponentFromJson(invData.getString("un"));
                    final ClientDCInternalInv current = getById(id, invData.getLong("sortBy"), un);

                    for(int x = 0; x < current.getInventory().getSlots(); x++) {
                        final String which = Integer.toString(x);
                        if(invData.contains(which))
                            current.getInventory().setStackInSlot(x, ItemStack.read(invData.getCompound(which)));
                    }
                } catch(final NumberFormatException ignored) {}
            }
        }

        if(refreshList) {
            refreshList = false;
            // invalid caches on refresh
            cachedSearches.clear();
            refreshList();
        }
    }

    /**
     * Rebuilds the list of interfaces.
     * <p>
     * Respects a search term if present (ignores case) and adding only matching patterns.
     */
    private void refreshList() {
        byName.clear();

        final String searchFilterLowerCase = searchField.getText().toLowerCase();

        final Set<Object> cachedSearch = getCacheForSearchTerm(searchFilterLowerCase);
        final boolean rebuild = cachedSearch.isEmpty();

        for(final ClientDCInternalInv entry : byId.values()) {
            // ignore inventory if not doing a full rebuild or cache already marks it as
            // miss.
            if(!rebuild && !cachedSearch.contains(entry)) continue;

            // Shortcut to skip any filter if search term is ""/empty
            boolean found = searchFilterLowerCase.isEmpty();

            // Search if the current inventory holds a pattern containing the search term.
            if(!found) {
                for(final ItemStack itemStack : entry.getInventory()) {
                    found = itemStackMatchesSearchTerm(itemStack, searchFilterLowerCase);
                    if(found) break;
                }
            }

            // if found, filter skipped or machine name matching the search term, add it
            if(found || entry.getSearchName().contains(searchFilterLowerCase)) {
                byName.put(entry.getFormattedName(), entry);
                cachedSearch.add(entry);
            } else cachedSearch.remove(entry);
        }

        names.clear();
        names.addAll(byName.keySet());

        Collections.sort(names);

        lines.clear();
        lines.ensureCapacity(getMaxRows());

        for(final String n : names) {
            lines.add(n);

            List<ClientDCInternalInv> clientInventories = new ArrayList<>(byName.get(n));

            Collections.sort(clientInventories);
            lines.addAll(clientInventories);
        }

        getScrollBar().setRange(0, lines.size() - LINES_ON_PAGE, 2);
    }

    private boolean itemStackMatchesSearchTerm(final ItemStack itemStack, final String searchTerm) {
        if(itemStack.isEmpty()) return false;

        final CompoundNBT encodedValue = itemStack.getTag();

        if(encodedValue == null) return false;

        // Potential later use to filter by input
        // ListNBT inTag = encodedValue.getTagList( "in", 10 );
        final ListNBT outTag = encodedValue.getList("out", 10);

        for(int i = 0; i < outTag.size(); i++) {

            final ItemStack parsedItemStack = ItemStack.read(outTag.getCompound(i));
            if(!parsedItemStack.isEmpty()) {
                final String displayName = Platform.getItemDisplayName(Api.instance().storage()
                        .getStorageChannel(IItemStorageChannel.class).createStack(parsedItemStack)).getString().toLowerCase();
                if(displayName.contains(searchTerm)) return true;
            }
        }
        return false;
    }

    /**
     * Tries to retrieve a cache for a with search term as keyword.
     * <p>
     * If this cache should be empty, it will populate it with an earlier cache if available or at least the cache for
     * the empty string.
     *
     * @param searchTerm the corresponding search
     * @return a Set matching a superset of the search term
     */
    private Set<Object> getCacheForSearchTerm(final String searchTerm) {
        if(!cachedSearches.containsKey(searchTerm)) cachedSearches.put(searchTerm, new HashSet<>());

        final Set<Object> cache = cachedSearches.get(searchTerm);

        if(cache.isEmpty() && searchTerm.length() > 1) {
            cache.addAll(getCacheForSearchTerm(searchTerm.substring(0, searchTerm.length() - 1)));
            return cache;
        }

        return cache;
    }

    /**
     * The max amount of unique names and each inv row. Not affected by the filtering.
     *
     * @return max amount of unique names and each inv row
     */
    private int getMaxRows() {
        return names.size() + byId.size();
    }

    private ClientDCInternalInv getById(final long id, final long sortBy, final ITextComponent name) {
        ClientDCInternalInv o = byId.get(id);

        if(o == null) {
            byId.put(id, o = new ClientDCInternalInv(9, id, sortBy, name));
            refreshList = true;
        }
        return o;
    }
}