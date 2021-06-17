package tfar.ae2wt.net.server;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.ae2wt.wpt.WirelessPatternTerminalContainer;

import java.util.function.Supplier;

public class C2STogglePatternSubsitutionPacket {

    boolean sub;

    public C2STogglePatternSubsitutionPacket(boolean sub) {
        this.sub = sub;
    }

    public C2STogglePatternSubsitutionPacket(PacketBuffer buf) {
        sub = buf.readBoolean();
    }

    public void encode(PacketBuffer buf) {
        buf.writeBoolean(sub);
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
            if (c instanceof WirelessPatternTerminalContainer) {
                final WirelessPatternTerminalContainer container = (WirelessPatternTerminalContainer) c;
                container.getPatternTerminal().setSubstitution(sub);
            }
        });
    }
}
