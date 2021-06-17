package tfar.ae2wt.rei;

import tfar.ae2wt.wirelesscraftingterminal.WirelessCraftingTerminalContainer;
import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.RecipeDisplay;

public class CraftingRecipeTransferHandler extends RecipeTransferHandler<WirelessCraftingTerminalContainer> {

    public CraftingRecipeTransferHandler(Class<WirelessCraftingTerminalContainer> containerClass) {
        super(containerClass);
    }

    @Override
    protected AutoTransferHandler.Result doTransferRecipe(WirelessCraftingTerminalContainer container, RecipeDisplay recipe, AutoTransferHandler.Context context) {
        return null;
    }

    @Override
    protected boolean isCrafting() {
        return true;
    }
}