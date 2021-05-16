package tfar.ae2wtlib.net;

import tfar.ae2wtlib.AE2WirelessTerminals;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {

    public static SimpleChannel INSTANCE;
    static int i = 0;

    public static void registerPackets() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(AE2WirelessTerminals.MODID, AE2WirelessTerminals.MODID), () -> "1.0", s -> true, s -> true);

        INSTANCE.registerMessage(i++, C2SCycleTerminalPacket.class,
                (message, buffer) -> {},
                buffer -> new C2SCycleTerminalPacket(),
                C2SCycleTerminalPacket::handle);
    }
}
