package tfar.ae2wtlib.net;

import tfar.ae2wtlib.AE2WirelessCraftingTerminals;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {

    public static SimpleChannel INSTANCE;
    static int i = 0;

    public static void registerPackets() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(AE2WirelessCraftingTerminals.MODID, AE2WirelessCraftingTerminals.MODID), () -> "1.0", s -> true, s -> true);

        INSTANCE.registerMessage(i++, C2SCycleTerminal.class,
                (message, buffer) -> {},
                buffer -> new C2SCycleTerminal(),
                C2SCycleTerminal::handle);
    }
}
