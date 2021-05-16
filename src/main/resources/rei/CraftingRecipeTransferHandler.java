package tfar.ae2wtlib.rei;

import tfar.ae2wtlib.wct.WCTContainer;
import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.RecipeDisplay;

public class CraftingRecipeTransferHandler extends RecipeTransferHandler<WCTContainer> {

    public CraftingRecipeTransferHandler(Class<WCTContainer> containerClass) {
        super(containerClass);
    }

    @Override
    protected AutoTransferHandler.Result doTransferRecipe(WCTContainer container, RecipeDisplay recipe, AutoTransferHandler.Context context) {
        return null;
    }

    @Override
    protected boolean isCrafting() {
        return true;
    }
}