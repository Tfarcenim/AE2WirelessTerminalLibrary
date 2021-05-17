package tfar.ae2wt.rei;

import tfar.ae2wt.AE2WirelessTerminals;
import tfar.ae2wt.wirelesscraftingterminal.WCTContainer;
import tfar.ae2wt.wpt.WPatternTContainer;
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
        recipeHelper.registerAutoCraftingHandler(new PatternRecipeTransferHandler(WPatternTContainer.class));

        recipeHelper.registerWorkingStations(DefaultPlugin.CRAFTING, EntryStack.create(AE2WirelessTerminals.CRAFTING_TERMINAL));
    }
}