package tfar.ae2wtlib.wct.magnet_card;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tfar.ae2wtlib.AE2WirelessTerminals;
import tfar.ae2wtlib.terminal.ItemWT;

import java.util.List;

public class ItemMagnetCard extends Item {

    public ItemMagnetCard() {
        super(new Properties().group(AE2WirelessTerminals.ITEM_GROUP).maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines, final ITooltipFlag advancedTooltips) {
        super.addInformation(stack, world, lines, advancedTooltips);
        lines.add(new TranslationTextComponent("item.ae2wtlib.magnet_card.desc"));
    }

    public static void saveMagnetSettings(ItemStack magnetCardHolder, MagnetSettings magnetSettings) {
        ItemStack magnetCard = ItemWT.getSavedSlot(magnetCardHolder, "magnetCard");
        magnetCard.getOrCreateTag().put("magnet_settings", magnetSettings.toTag());
        ItemWT.setSavedSlot(magnetCardHolder, magnetCard, "magnetCard");
    }

    public static MagnetSettings loadMagnetSettings(ItemStack magnetCardHolder) {
        ItemStack magnetCard = ItemWT.getSavedSlot(magnetCardHolder, "magnetCard");
        if(magnetCard.isEmpty()) return new MagnetSettings();
        return new MagnetSettings(magnetCard.getOrCreateTag().getCompound("magnet_settings"));
    }

    public static boolean isActiveMagnet(ItemStack magnetCardHolder) {
        if(magnetCardHolder.isEmpty()) return false;
        MagnetSettings settings = loadMagnetSettings(magnetCardHolder);
        return settings.magnetMode == MagnetMode.PICKUP_INVENTORY || settings.magnetMode == MagnetMode.PICKUP_ME;
    }

    public static boolean isPickupME(ItemStack magnetCardHolder) {
        if(magnetCardHolder.isEmpty()) return false;
        MagnetSettings settings = loadMagnetSettings(magnetCardHolder);
        return settings.magnetMode == MagnetMode.PICKUP_ME;
    }
}