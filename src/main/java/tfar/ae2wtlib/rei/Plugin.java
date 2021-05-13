package tfar.ae2wtlib.rei;

import tfar.ae2wtlib.AE2WirelessCraftingTerminals;
import tfar.ae2wtlib.wct.WCTContainer;
import tfar.ae2wtlib.wpt.WPTContainer;
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
        recipeHelper.registerAutoCraftingHandler(new CraftingRecipeTransferHandler(WCTContainer.class));
        recipeHelper.registerAutoCraftingHandler(new PatternRecipeTransferHandler(WPTContainer.class));

        recipeHelper.registerWorkingStations(DefaultPlugin.CRAFTING, EntryStack.create(AE2WirelessCraftingTerminals.CRAFTING_TERMINAL));
    }
}