package tfar.ae2wt.wut;

import appeng.container.ContainerLocator;
import appeng.core.AEConfig;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.terminal.IInfinityBoosterCardHolder;
import tfar.ae2wt.terminal.ItemWT;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ItemWUT extends ItemWT implements IInfinityBoosterCardHolder {

    public ItemWUT() {
        super(AEConfig.instance().getWirelessTerminalBattery(), new Item.Properties().group(AE2WirelessTerminals.ITEM_GROUP).maxStackSize(1));
    }

    @Override
    public void open(final PlayerEntity player, final ContainerLocator locator) {
        WUTHandler.open(player, locator);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines, final ITooltipFlag advancedTooltips) {
        lines.add(new TranslationTextComponent("item.ae2wtlib.wireless_universal_terminal.desc").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY)));
        if(WUTHandler.hasTerminal(stack, "crafting")) lines.add(new TranslationTextComponent("item.ae2wtlib.wireless_crafting_terminal").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY)));
        if(WUTHandler.hasTerminal(stack, "interface")) lines.add(new TranslationTextComponent("item.ae2wtlib.wireless_interface_terminal").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY)));
        if(WUTHandler.hasTerminal(stack, "pattern")) lines.add(new TranslationTextComponent("item.ae2wtlib.wireless_pattern_terminal").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY)));
        super.addInformation(stack, world, lines, advancedTooltips);
    }
}