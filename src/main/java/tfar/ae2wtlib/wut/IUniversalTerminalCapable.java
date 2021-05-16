package tfar.ae2wtlib.wut;

import tfar.ae2wtlib.net.C2SCycleTerminalPacket;
import tfar.ae2wtlib.net.PacketHandler;

public interface IUniversalTerminalCapable {
    default void cycleTerminal() {
        PacketHandler.INSTANCE.sendToServer(new C2SCycleTerminalPacket());
    }
}