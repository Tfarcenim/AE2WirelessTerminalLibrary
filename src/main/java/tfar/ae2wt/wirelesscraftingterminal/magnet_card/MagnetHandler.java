package tfar.ae2wt.wirelesscraftingterminal.magnet_card;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EntityPredicates;
import tfar.ae2wt.wirelesscraftingterminal.CraftingTerminalHandler;

import java.util.List;

public class MagnetHandler {
    public static void doMagnet(MinecraftServer server) {
        List<ServerPlayerEntity> playerList = server.getPlayerList().getPlayers();
        for(ServerPlayerEntity player : playerList) {
            ItemStack magnetCardHolder = CraftingTerminalHandler.getCraftingTerminalHandler(player).getCraftingTerminal();
            if(ItemMagnetCard.isActiveMagnet(magnetCardHolder)) {
                List<ItemEntity> entityItems = player.getServerWorld().getEntitiesWithinAABB(ItemEntity.class, player.getBoundingBox().grow(16.0D), EntityPredicates.HAS_INVENTORY);
                for(ItemEntity entityItemNearby : entityItems) {
                    if(!player.isSneaking()) {
                        entityItemNearby.onCollideWithPlayer(player);
                    }
                }
            }
        }
    }
}