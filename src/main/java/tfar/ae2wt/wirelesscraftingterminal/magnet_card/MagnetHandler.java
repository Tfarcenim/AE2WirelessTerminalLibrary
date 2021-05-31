package tfar.ae2wt.wirelesscraftingterminal.magnet_card;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EntityPredicates;
import tfar.ae2wt.wirelesscraftingterminal.CraftingTerminalHandler;

import java.util.List;

public class MagnetHandler {
    public static void doMagnet(PlayerEntity player) {
        ItemStack magnetCardHolder = CraftingTerminalHandler.getCraftingTerminalHandler(player).getCraftingTerminal();
        if (ItemMagnetCard.isActiveMagnet(magnetCardHolder)) {
            List<ItemEntity> entityItems = player.world.getEntitiesWithinAABB(ItemEntity.class, player.getBoundingBox().grow(16.0D));
            for (ItemEntity entityItemNearby : entityItems) {
                if (!player.isSneaking()) {
                    entityItemNearby.onCollideWithPlayer(player);
                }
            }
        }
    }
}