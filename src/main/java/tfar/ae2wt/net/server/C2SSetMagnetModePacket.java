package tfar.ae2wt.net.server;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.ae2wt.wirelesscraftingterminal.WirelessCraftingTerminalContainer;
import tfar.ae2wt.wirelesscraftingterminal.magnet_card.MagnetMode;

import java.util.function.Supplier;

public class C2SSetMagnetModePacket {

    MagnetMode mode;

    public C2SSetMagnetModePacket(MagnetMode mode) {
        this.mode = mode;
    }

    public C2SSetMagnetModePacket(PacketBuffer buf) {
        mode = MagnetMode.modes[buf.readInt()];
    }

    public void encode(PacketBuffer buf) {
        buf.writeInt(mode.ordinal());
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
            if (c instanceof WirelessCraftingTerminalContainer) {
                final WirelessCraftingTerminalContainer container = (WirelessCraftingTerminalContainer) c;
                    container.getMagnetSettings().magnetMode = mode;
                    container.saveMagnetSettings();
                }
        });
    }
}
