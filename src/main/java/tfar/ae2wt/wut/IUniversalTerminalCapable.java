package tfar.ae2wt.wut;

import tfar.ae2wt.net.C2SCycleTerminalPacket;
import tfar.ae2wt.net.PacketHandler;

public interface IUniversalTerminalCapable {
    default void cycleTerminal() {
        PacketHandler.INSTANCE.sendToServer(new C2SCycleTerminalPacket());
    }
}