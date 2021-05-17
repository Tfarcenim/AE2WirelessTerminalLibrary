package tfar.ae2wt.net;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.ae2wt.wpt.WPatternTContainer;

import java.util.function.Supplier;

public class C2SPatternSlotPacket {

    IAEItemStack slotItem;
    boolean shift;
    IAEItemStack[] pattern = new IAEItemStack[9];

    public C2SPatternSlotPacket(IAEItemStack slotItem, boolean shift, IAEItemStack[] pattern) {
        this.slotItem = slotItem;
        this.shift = shift;
        this.pattern = pattern;
    }

    public C2SPatternSlotPacket(PacketBuffer buf) {
        slotItem = AEItemStack.fromPacket(buf);
        shift = buf.readBoolean();
        for (int i = 0; i < 9;i++) {
            pattern[i] = AEItemStack.fromPacket(buf);
        }
    }

    public void encode(PacketBuffer buf) {
        slotItem.writeToPacket(buf);
        buf.writeBoolean(shift);
        for (int i = 0;i  < 9;i++) {
            pattern[i].writeToPacket(buf);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PlayerEntity player = ctx.get().getSender();

        if (player == null) return;

        ctx.get().enqueueWork(  ()->  {
            MinecraftServer server = player.getServer();
            server.execute(() -> {
                if(player.openContainer instanceof WPatternTContainer) {
                    final WPatternTContainer patternTerminal = (WPatternTContainer) player.openContainer;
                    patternTerminal.craftOrGetItem(slotItem,shift,pattern);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
