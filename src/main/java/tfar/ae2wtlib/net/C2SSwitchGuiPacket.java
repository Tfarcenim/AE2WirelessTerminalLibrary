package tfar.ae2wtlib.net;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.implementations.WirelessCraftingStatusContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.ae2wtlib.wct.WCTContainer;
import tfar.ae2wtlib.wit.WITContainer;
import tfar.ae2wtlib.wpt.WPatternTContainer;

import java.util.function.Supplier;

public class C2SSwitchGuiPacket {

    private String id;

    public C2SSwitchGuiPacket(String id) {
        this.id = id;
    }

    public C2SSwitchGuiPacket(PacketBuffer buf) {
        id = buf.readString(32767);
    }

    public void encode(PacketBuffer buf) {
        buf.writeString(id);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PlayerEntity player = ctx.get().getSender();

        if (player == null) return;

        ctx.get().enqueueWork(  ()->  {
            MinecraftServer server = player.getServer();
            server.execute(() -> {
                final Container c = player.openContainer;
                if(!(c instanceof AEBaseContainer)) {
                    return;
                }
                AEBaseContainer container = (AEBaseContainer) c;
                final ContainerLocator locator = container.getLocator();
                if(locator == null) {
                    return;
                }
                switch(id) {
                    case "wireless_crafting_terminal":
                        WCTContainer.open(player, locator);
                        break;
                    case "wireless_pattern_terminal":
                        WPatternTContainer.open(player, locator);
                        break;
                    case "wireless_interface_terminal":
                        WITContainer.open(player, locator);
                        break;
                    case "wireless_crafting_status":
                        WirelessCraftingStatusContainer.open(player, locator);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

}
