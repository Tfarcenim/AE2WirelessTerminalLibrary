package tfar.ae2wtlib.net;

import tfar.ae2wtlib.AE2WirelessTerminals;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tfar.ae2wtlib.net.client.S2CInterfaceTerminalPacket;
import tfar.ae2wtlib.net.server.C2SDeleteTrashPacket;
import tfar.ae2wtlib.net.server.C2SGeneralPacket;

public class PacketHandler {

    public static SimpleChannel INSTANCE;
    static int i = 0;

    public static void registerPackets() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(AE2WirelessTerminals.MODID, AE2WirelessTerminals.MODID), () -> "1.0", s -> true, s -> true);

        INSTANCE.registerMessage(i++, C2SCycleTerminalPacket.class,
                (message, buffer) -> {},
                buffer -> new C2SCycleTerminalPacket(),
                C2SCycleTerminalPacket::handle);

        INSTANCE.registerMessage(i++, C2SGeneralPacket.class,
                C2SGeneralPacket::encode,
                C2SGeneralPacket::new,
                C2SGeneralPacket::handle);

        INSTANCE.registerMessage(i++, C2SDeleteTrashPacket.class,
                C2SDeleteTrashPacket::encode,
                C2SDeleteTrashPacket::new,
                C2SDeleteTrashPacket::handle);

        INSTANCE.registerMessage(i++, C2SSwitchGuiPacket.class,
                C2SSwitchGuiPacket::encode,
                C2SSwitchGuiPacket::new,
                C2SSwitchGuiPacket::handle);

        INSTANCE.registerMessage(i++, S2CInterfaceTerminalPacket.class,
                S2CInterfaceTerminalPacket::encode,
                S2CInterfaceTerminalPacket::new,
                S2CInterfaceTerminalPacket::handle);
    }
}
