package tfar.ae2wtlib.net.server;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.ae2wtlib.wpt.WPatternTContainer;

import java.util.function.Supplier;

public class C2SClearPatternPacket {

    public C2SClearPatternPacket() {
    }

    public C2SClearPatternPacket(PacketBuffer buf) {
    }

    public void encode(PacketBuffer buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PlayerEntity player = ctx.get().getSender();
        if (player == null) return;
        ctx.get().enqueueWork(() -> run(player));
        ctx.get().setPacketHandled(true);
    }

    public void run(PlayerEntity player) {
        MinecraftServer server = player.getServer();
        server.execute(() -> {
            final Container c = player.openContainer;
            if (c instanceof WPatternTContainer) {
                final WPatternTContainer container = (WPatternTContainer) c;
                container.clearPattern();
            }
        });
    }
}
