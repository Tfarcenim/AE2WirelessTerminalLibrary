package tfar.ae2wt.net;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.container.ContainerLocator;
import appeng.container.implementations.WirelessCraftConfirmContainer;
import appeng.core.AELog;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.ae2wt.util.WirelessCraftAmountContainer;

import java.util.concurrent.Future;
import java.util.function.Supplier;

public class C2SCraftRequestPacket {

    private int amount;
    private boolean shift;

    public C2SCraftRequestPacket(int amount,boolean shift) {
        this.amount = amount;
        this.shift = shift;
    }
    
    public C2SCraftRequestPacket(PacketBuffer buf) {
        amount = buf.readInt();
        shift = buf.readBoolean();
    }
    
    public void encode(PacketBuffer buf) {
        buf.writeInt(amount);
        buf.writeBoolean(shift);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PlayerEntity player = ctx.get().getSender();

        if (player == null) return;

        ctx.get().enqueueWork(  ()->  {
            MinecraftServer server = player.getServer();
                server.execute(() -> {
                    if(player.openContainer instanceof WirelessCraftAmountContainer) {
                        final WirelessCraftAmountContainer cca = (WirelessCraftAmountContainer) player.openContainer;
                        final Object target = cca.getTarget();
                        if(target instanceof IActionHost) {
                            final IActionHost ah = (IActionHost) target;
                            final IGridNode gn = ah.getActionableNode();
                            if(gn == null) return;

                            final IGrid g = gn.getGrid();
                            if(cca.getItemToCraft() == null) return;

                            cca.getItemToCraft().setStackSize(amount);

                            Future<ICraftingJob> futureJob = null;
                            try {
                                final ICraftingGrid cg = g.getCache(ICraftingGrid.class);
                                futureJob = cg.beginCraftingJob(cca.getWorld(), cca.getGrid(), cca.getActionSrc(), cca.getItemToCraft(), null);

                                final ContainerLocator locator = cca.getLocator();
                                if(locator != null) {
                                    WirelessCraftConfirmContainer.openServer(player, locator);

                                    if(player.openContainer instanceof WirelessCraftConfirmContainer) {
                                        final WirelessCraftConfirmContainer ccc = (WirelessCraftConfirmContainer) player.openContainer;
                                        ccc.setAutoStart(shift);
                                        ccc.setJob(futureJob);
                                        cca.detectAndSendChanges();
                                    }
                                }
                            } catch(final Throwable e) {
                                if(futureJob != null) futureJob.cancel(true);
                                AELog.info(e);
                            }
                        }
                    }
                });
            });
        ctx.get().setPacketHandled(true);
    }
}
