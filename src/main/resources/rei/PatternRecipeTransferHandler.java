package tfar.ae2wt.rei;

import tfar.ae2wt.wpt.WirelessPatternTerminalContainer;
import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;

public class PatternRecipeTransferHandler extends RecipeTransferHandler<WirelessPatternTerminalContainer> {

    PatternRecipeTransferHandler(Class<WirelessPatternTerminalContainer> containerClass) {
        super(containerClass);
    }

    protected AutoTransferHandler.Result doTransferRecipe(WirelessPatternTerminalContainer container, RecipeDisplay recipe, AutoTransferHandler.Context context) {
        if(container.isCraftingMode() && recipe.getRecipeCategory() != DefaultPlugin.CRAFTING) {
            return AutoTransferHandler.Result.createFailed("jei.appliedenergistics2.requires_processing_mode");
        }

        if(recipe.getResultingEntries().isEmpty()) {
            return AutoTransferHandler.Result.createFailed("jei.appliedenergistics2.no_output");
        }
        return null;
    }

    @Override
    protected boolean isCrafting() {
        return false;
    }
}