package tfar.ae2wtlib.net;

import tfar.ae2wtlib.wct.WCTContainer;
import tfar.ae2wtlib.wct.magnet_card.MagnetMode;
import tfar.ae2wtlib.wpt.WPTContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SGeneralPacket {

    private final String name;
    private final byte value;

    public C2SGeneralPacket(String name, byte value) {
        this.name = name;
        this.value = value;
    }

    public C2SGeneralPacket(PacketBuffer buf) {
        this.name = buf.readString(32767);
        this.value = buf.readByte();
    }

    public void encode(PacketBuffer buf) {
        buf.writeString(name);
        buf.writeByte(value);
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
            if (name.startsWith("PatternTerminal.") && c instanceof WPTContainer) {
                final WPTContainer container = (WPTContainer) c;
                switch (name) {
                    case "PatternTerminal.CraftMode":
                        container.getPatternTerminal().setCraftingRecipe(value != 0);
                        break;
                    case "PatternTerminal.Encode":
                        container.encode();
                        break;
                    case "PatternTerminal.Clear":
                        container.clear();
                        break;
                    case "PatternTerminal.Substitute":
                        container.getPatternTerminal().setSubstitution(value != 0);
                        break;
                }
            } else if (name.startsWith("CraftingTerminal.") && c instanceof WCTContainer) {
                final WCTContainer container = (WCTContainer) c;
                if (name.equals("CraftingTerminal.Delete")) container.deleteTrashSlot();
                else if (name.equals("CraftingTerminal.SetMagnetMode")) {
                    container.getMagnetSettings().magnetMode = MagnetMode.fromByte(value);
                    container.saveMagnetSettings();
                }
            }
        });
    }
}
