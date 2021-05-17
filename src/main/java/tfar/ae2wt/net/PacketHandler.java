package tfar.ae2wt.net;

import tfar.ae2wt.AE2WirelessTerminals;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tfar.ae2wt.net.client.S2CInterfaceTerminalPacket;
import tfar.ae2wt.net.server.*;

public class PacketHandler {

    public static SimpleChannel INSTANCE;
    static int i = 0;

    public static void registerPackets() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(AE2WirelessTerminals.MODID, AE2WirelessTerminals.MODID), () -> "1.0", s -> true, s -> true);

        INSTANCE.registerMessage(i++, C2SCycleTerminalPacket.class,
                (message, buffer) -> {},
                buffer -> new C2SCycleTerminalPacket(),
                C2SCycleTerminalPacket::handle);

        INSTANCE.registerMessage(i++, C2SDeleteTrashPacket.class,
                C2SDeleteTrashPacket::encode,
                C2SDeleteTrashPacket::new,
                C2SDeleteTrashPacket::handle);

        INSTANCE.registerMessage(i++, C2SEncodePatternPacket.class,
                C2SEncodePatternPacket::encode,
                C2SEncodePatternPacket::new,
                C2SEncodePatternPacket::handle);

        INSTANCE.registerMessage(i++, C2SClearPatternPacket.class,
                C2SClearPatternPacket::encode,
                C2SClearPatternPacket::new,
                C2SClearPatternPacket::handle);

        INSTANCE.registerMessage(i++, C2STogglePatternSubsitutionPacket.class,
                C2STogglePatternSubsitutionPacket::encode,
                C2STogglePatternSubsitutionPacket::new,
                C2STogglePatternSubsitutionPacket::handle);

        INSTANCE.registerMessage(i++, C2STogglePatternCraftingModePacket.class,
                C2STogglePatternCraftingModePacket::encode,
                C2STogglePatternCraftingModePacket::new,
                C2STogglePatternCraftingModePacket::handle);

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
