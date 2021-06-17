package tfar.ae2wt.wirelesscraftingterminal;

import appeng.api.features.ILocatable;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.DimensionalCoord;
import appeng.core.Api;
import appeng.tile.networking.WirelessTileEntity;
import tfar.ae2wt.terminal.IInfinityBoosterCardHolder;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;
import tfar.ae2wt.wut.WUTItem;
import tfar.ae2wt.wut.WUTHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class CraftingTerminalHandler {

    private static final HashMap<UUID, CraftingTerminalHandler> players = new HashMap<>();
    private final PlayerEntity player;
    private ItemStack craftingTerminal = ItemStack.EMPTY;
    private ILocatable securityStation;
    private IGrid targetGrid;

    private CraftingTerminalHandler(PlayerEntity player) {
        this.player = player;
    }

    public static CraftingTerminalHandler getCraftingTerminalHandler(PlayerEntity player) {
        if(players.containsKey(player.getUniqueID())) return players.get(player.getUniqueID());
        CraftingTerminalHandler handler = new CraftingTerminalHandler(player);
        players.put(player.getUniqueID(), handler);
        return handler;
    }

    public ItemStack getCraftingTerminal() {//TODO trinkets/curios
        PlayerInventory inv = player.inventory;
        if((!craftingTerminal.isEmpty()) && inv.hasItemStack(craftingTerminal)) return craftingTerminal;

        for(int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack terminal = inv.getStackInSlot(i);
            if(terminal.getItem() instanceof WCTItem || (terminal.getItem() instanceof WUTItem && WUTHandler.hasTerminal(terminal, "crafting"))) {
                return craftingTerminal = terminal;
            }
        }
        return ItemStack.EMPTY;
    }

    public ILocatable getSecurityStation() {
        if(securityStation != null) return securityStation;
        final String unParsedKey = ((AbstractWirelessTerminalItem) craftingTerminal.getItem()).getEncryptionKey(craftingTerminal);
        if(unParsedKey.isEmpty()) return null;
        final long parsedKey = Long.parseLong(unParsedKey);
        return securityStation = Api.instance().registries().locatable().getLocatableBy(parsedKey);
    }

    public IGrid getTargetGrid() {
        if(getSecurityStation() == null) return targetGrid = null;
        final IGridNode n = ((IActionHost) securityStation).getActionableNode();

        if(n != null) {
            return targetGrid = n.getGrid();
        }
        return targetGrid = null;
    }

    private IWirelessAccessPoint myWap;
    private double sqRange = Double.MAX_VALUE;

    public boolean inRange() {
        if(((IInfinityBoosterCardHolder) craftingTerminal.getItem()).hasBoosterCard(craftingTerminal)) return true;
        sqRange = Double.MAX_VALUE;

        if(getTargetGrid() == null) return false;
        if(targetGrid != null) {
            if(myWap != null) {
                if(myWap.getGrid() == targetGrid) {
                    if(testWap(myWap)) return true;
                }
            }

            final IMachineSet tw = targetGrid.getMachines(WirelessTileEntity.class);

            myWap = null;

            for(final IGridNode n : tw) {
                final IWirelessAccessPoint wap = (IWirelessAccessPoint) n.getMachine();
                if(testWap(wap)) {
                    myWap = wap;
                }
            }

            return myWap != null;
        }
        return false;
    }

    private boolean testWap(final IWirelessAccessPoint wap) {
        double rangeLimit = wap.getRange();
        rangeLimit *= rangeLimit;

        final DimensionalCoord dc = wap.getLocation();

        if(dc.getWorld() == player.world) {
            final double offX = dc.x - player.getPosX();
            final double offY = dc.y - player.getPosY();
            final double offZ = dc.z - player.getPosZ();

            final double r = offX * offX + offY * offY + offZ * offZ;
            if(r < rangeLimit && sqRange > r) {
                if(wap.isActive()) {
                    sqRange = r;
                    return true;
                }
            }
        }
        return false;
    }
}