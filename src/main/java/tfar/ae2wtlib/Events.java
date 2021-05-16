package tfar.ae2wtlib;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import tfar.ae2wtlib.wct.magnet_card.MagnetHandler;

public class Events {

    public static void serverTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            MagnetHandler.doMagnet(ServerLifecycleHooks.getCurrentServer());
        }
    }
}
