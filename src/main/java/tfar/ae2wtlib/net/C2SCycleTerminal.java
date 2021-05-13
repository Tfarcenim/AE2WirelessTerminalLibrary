package tfar.ae2wtlib.net;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SCycleTerminal {

    public void encode(PacketBuffer buf) {

    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PlayerEntity player = ctx.get().getSender();

        if (player == null) return;

        ctx.get().enqueueWork(  ()->  {
            ItemStack bag = player.getHeldItemMainhand();
         //   if (!(bag.getItem() instanceof DankItem)){
                bag = player.getHeldItemOffhand();
     //           if (!(bag.getItem() instanceof DankItem))return;
     //       }
      //      Utils.cycleMode(bag,player);
        });
        ctx.get().setPacketHandled(true);
    }

}
