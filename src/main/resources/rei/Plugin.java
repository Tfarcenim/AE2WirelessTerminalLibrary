package tfar.ae2wt.rei;

import tfar.ae2wt.init.ModItems;
import tfar.ae2wt.wirelesscraftingterminal.WirelessCraftingTerminalContainer;
import tfar.ae2wt.wpt.WirelessPatternTerminalContainer;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.util.Identifier;

public class Plugin implements REIPluginV0 {

    @Override
    public Identifier getPluginIdentifier() {
        return new Identifier("ae2wtlib", "rei");
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        recipeHelper.registerAutoCraftingHandler(new CraftingRecipeTransferHandler(WirelessCraftingTerminalContainer.class));
        recipeHelper.registerAutoCraftingHandler(new PatternRecipeTransferHandler(WirelessPatternTerminalContainer.class));

        recipeHelper.registerWorkingStations(DefaultPlugin.CRAFTING, EntryStack.create(ModItems.CRAFTING_TERMINAL));
    }
}