package tfar.ae2wtlib.wut;

import appeng.container.ContainerLocator;
import appeng.core.AEConfig;
import tfar.ae2wtlib.AE2WirelessCraftingTerminals;
import tfar.ae2wtlib.terminal.IInfinityBoosterCardHolder;
import tfar.ae2wtlib.terminal.ItemWT;
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
        super(AEConfig.instance().getWirelessTerminalBattery(), new Item.Properties().group(AE2WirelessCraftingTerminals.ITEM_GROUP).maxStackSize(1));
    }

    @Override
    public void open(final PlayerEntity player, final ContainerLocator locator) {
        WUTHandler.open(player, locator);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines, final ITooltipFlag advancedTooltips) {
        lines.add(new TranslationTextComponent("item.ae2wtlib.wireless_universal_terminal.desc").fillStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        if(WUTHandler.hasTerminal(stack, "crafting")) lines.add(new TranslatableText("item.ae2wtlib.wireless_crafting_terminal").fillStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        if(WUTHandler.hasTerminal(stack, "interface")) lines.add(new TranslatableText("item.ae2wtlib.wireless_interface_terminal").fillStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        if(WUTHandler.hasTerminal(stack, "pattern")) lines.add(new TranslatableText("item.ae2wtlib.wireless_pattern_terminal").fillStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        super.appendTooltip(stack, world, lines, advancedTooltips);
    }
}