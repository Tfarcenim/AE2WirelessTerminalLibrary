package tfar.ae2wt.net.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.ae2wt.wirelessinterfaceterminal.WITScreen;

import java.util.function.Supplier;

public class S2CInterfaceTerminalPacket {

    private CompoundNBT nbt;

    public S2CInterfaceTerminalPacket(CompoundNBT nbt) {
        this.nbt = nbt;
    }

    public S2CInterfaceTerminalPacket(PacketBuffer buf) {
        nbt = buf.readCompoundTag();
    }

    public void encode(PacketBuffer buf) {
         buf.writeCompoundTag(nbt);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player == null) return;

            final Screen screen = Minecraft.getInstance().currentScreen;
            if (screen instanceof WITScreen) {
                WITScreen s = (WITScreen) screen;
                if (nbt != null) s.postUpdate(nbt);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
