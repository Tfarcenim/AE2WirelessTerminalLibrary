package tfar.ae2wt;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import tfar.ae2wt.wirelesscraftingterminal.magnet_card.MagnetHandler;

public class Events {

    public static void serverTick(TickEvent.PlayerTickEvent e) {
        if (e.phase == TickEvent.Phase.START && !e.player.world.isRemote) {
            MagnetHandler.doMagnet(e.player);
        }
    }
}
