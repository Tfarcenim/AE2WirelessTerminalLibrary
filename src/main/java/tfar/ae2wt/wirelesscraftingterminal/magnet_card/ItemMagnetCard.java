package tfar.ae2wt.wirelesscraftingterminal.magnet_card;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tfar.ae2wt.terminal.SlotType;
import tfar.ae2wt.terminal.AbstractWirelessTerminalItem;

import java.util.List;

public class ItemMagnetCard extends Item {

    public ItemMagnetCard(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines, final ITooltipFlag advancedTooltips) {
        super.addInformation(stack, world, lines, advancedTooltips);
        lines.add(new TranslationTextComponent("item.ae2wtlib.magnet_card.desc"));
    }

    public static void saveMagnetSettings(ItemStack magnetCardHolder, MagnetSettings magnetSettings) {
        ItemStack magnetCard = AbstractWirelessTerminalItem.getSavedSlot(magnetCardHolder, SlotType.magnetCard);
        magnetSettings.saveTo(magnetCard);
        AbstractWirelessTerminalItem.setSavedSlot(magnetCardHolder, magnetCard,  SlotType.magnetCard);
    }

    public static MagnetSettings loadMagnetSettings(ItemStack magnetCardHolder) {
        ItemStack magnetCard = AbstractWirelessTerminalItem.getSavedSlot(magnetCardHolder,  SlotType.magnetCard);
        return MagnetSettings.from(magnetCard);
    }

    public static boolean isActiveMagnet(ItemStack magnetCardHolder) {
        if(magnetCardHolder.isEmpty()) return false;
        MagnetSettings settings = loadMagnetSettings(magnetCardHolder);
        return settings.magnetMode.isActive();
    }

    public static boolean isPickupME(ItemStack magnetCardHolder) {
        if(magnetCardHolder.isEmpty()) return false;
        MagnetSettings settings = loadMagnetSettings(magnetCardHolder);
        return settings.magnetMode == MagnetMode.PICKUP_ME;
    }
}